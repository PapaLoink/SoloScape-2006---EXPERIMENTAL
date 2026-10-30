(() => {
  'use strict';

  const WIDTH = 765;
  const HEIGHT = 503;
  const canvas = document.getElementById('game');
  const ctx = canvas.getContext('2d', { alpha: false });
  const status = document.getElementById('status');
  const keyboardButton = document.getElementById('keyboard');
  const soundButton = document.getElementById('sound');
  const softInput = document.getElementById('softInput');
  const wsUrl = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/client`;

  let socket = null;
  let frameSeen = false;
  let decoding = false;
  let queuedFrame = null;
  let pointer = null;
  let lastMoveSent = 0;
  let mouseInside = false;
  let audioContext = null;
  let audioEnabled = false;
  const AudioContextClass = window.AudioContext || window.webkitAudioContext;
  const nextAudioTime = { 1: 0, 2: 0 };
  let lastReportedX = -1;
  let lastReportedY = -1;
  // In the 3D viewport, a finger swipe should simulate the existing middle
  // mouse camera drag. Inventory/chat gestures retain ordinary left dragging.
  const VIEWPORT_WIDTH = 512;
  const VIEWPORT_HEIGHT = 334;
  let touchGeneration = 0;
  const touches = new Map();
  let pinch = null;
  let pinchBlocked = false;
  const PINCH_STEP_PIXELS = 22;

  function pinchDistance() {
    if (touches.size !== 2) return null;
    const fingers = Array.from(touches.values());
    // Only world-area pinches zoom the 3D camera; never zoom the inventory/UI.
    if (!fingers[0].scene || !fingers[1].scene) return null;
    return Math.hypot(fingers[0].clientX - fingers[1].clientX,
                      fingers[0].clientY - fingers[1].clientY);
  }

  function stopPinchPointer(pointerId) {
    touches.delete(pointerId);
    if (!pinchBlocked) return false;
    const distance = pinchDistance();
    pinch = distance === null ? null : { distance };
    if (touches.size === 0) pinchBlocked = false;
    return true;
  }


  function setStatus(text, hide = false) {
    status.textContent = text;
    status.classList.toggle('hidden', hide);
  }

  function subscribeAudio() {
    if (socket && socket.readyState === WebSocket.OPEN && audioEnabled) {
      socket.send(new Uint8Array([8, 1]));
    }
  }

  function playAudio(payload) {
    if (!audioEnabled || !audioContext || audioContext.state !== 'running') return;
    const data = new DataView(payload);
    if (data.byteLength < 7 || data.getUint8(0) !== 65) return;
    const stream = data.getUint8(1);
    const channels = data.getUint8(2);
    const rate = data.getUint16(3, false);
    if ((stream !== 1 && stream !== 2) || (channels !== 1 && channels !== 2)
        || rate < 8000 || rate > 48000 || (data.byteLength - 5) % (channels * 2)) return;
    const frames = (data.byteLength - 5) / (channels * 2);
    if (frames === 0) return;
    const buffer = audioContext.createBuffer(channels, frames, rate);
    for (let channel = 0; channel < channels; channel++) {
      const samples = buffer.getChannelData(channel);
      for (let i = 0; i < frames; i++) {
        samples[i] = data.getInt16(5 + (i * channels + channel) * 2, true) / 32768;
      }
    }

    const now = audioContext.currentTime;
    // Never build up seconds of delay if a phone or Wi-Fi connection stalls.
    if (nextAudioTime[stream] > now + 0.35) return;
    const when = Math.max(now + 0.065, nextAudioTime[stream]);
    const source = audioContext.createBufferSource();
    source.buffer = buffer;
    source.connect(audioContext.destination);
    source.start(when);
    nextAudioTime[stream] = when + buffer.duration;
  }

  if (!AudioContextClass) {
    soundButton.textContent = 'Audio unavailable';
    soundButton.disabled = true;
  } else {
    soundButton.addEventListener('click', async () => {
      if (audioEnabled) {
        audioEnabled = false;
        if (socket && socket.readyState === WebSocket.OPEN) {
          socket.send(new Uint8Array([8, 0]));
        }
        await audioContext.suspend();
        nextAudioTime[1] = nextAudioTime[2] = 0;
        soundButton.textContent = 'Enable sound';
        return;
      }
      try {
        if (!audioContext) audioContext = new AudioContextClass();
        await audioContext.resume(); // iOS requires this call from a user gesture.
        audioEnabled = audioContext.state === 'running';
        if (!audioEnabled) return;
        nextAudioTime[1] = nextAudioTime[2] = 0;
        soundButton.textContent = 'Mute sound';
        subscribeAudio();
      } catch (error) {
        console.warn('Browser audio could not start:', error);
        soundButton.textContent = 'Tap to retry sound';
      }
    });
  }

  function connect() {
    setStatus('Connecting to Java client…');
    socket = new WebSocket(wsUrl);
    socket.binaryType = 'arraybuffer';
    socket.addEventListener('open', () => {
      setStatus('Connected · waiting for Java client frame…');
      subscribeAudio();
    });
    socket.addEventListener('message', (event) => {
      const data = event.data;
      if (data instanceof ArrayBuffer && data.byteLength > 5
          && new Uint8Array(data, 0, 1)[0] === 65) {
        playAudio(data);
      } else {
        queueFrame(data);
      }
    });
    socket.addEventListener('close', () => {
      setStatus('Java client disconnected · reconnecting…');
      setTimeout(connect, 1000);
    });
    socket.addEventListener('error', () => setStatus('Unable to connect to Java client'));
  }

  function queueFrame(arrayBuffer) {
    queuedFrame = arrayBuffer;
    if (!decoding) decodeNextFrame();
  }

  async function decodeNextFrame() {
    if (!queuedFrame) return;
    decoding = true;
    const bytes = queuedFrame;
    queuedFrame = null;
    try {
      const blob = new Blob([bytes], { type: 'image/png' });
      if ('createImageBitmap' in window) {
        const bitmap = await createImageBitmap(blob);
        ctx.drawImage(bitmap, 0, 0, WIDTH, HEIGHT);
        bitmap.close?.();
      } else {
        await drawBlobWithImage(blob);
      }
      if (!frameSeen) {
        frameSeen = true;
        setStatus('Java client live', true);
      }
    } catch (error) {
      console.error(error);
    } finally {
      decoding = false;
      if (queuedFrame) decodeNextFrame();
    }
  }

  function drawBlobWithImage(blob) {
    return new Promise((resolve, reject) => {
      const url = URL.createObjectURL(blob);
      const image = new Image();
      image.onload = () => {
        ctx.drawImage(image, 0, 0, WIDTH, HEIGHT);
        URL.revokeObjectURL(url);
        resolve();
      };
      image.onerror = () => {
        URL.revokeObjectURL(url);
        reject(new Error('Unable to decode Java client frame.'));
      };
      image.src = url;
    });
  }

  function send(bytes) {
    if (socket && socket.readyState === WebSocket.OPEN) socket.send(Uint8Array.from(bytes));
  }

  function u16(value) {
    const v = Math.max(0, Math.min(65535, Math.round(value)));
    return [v >>> 8 & 255, v & 255];
  }

  function gamePoint(event) {
    const rect = canvas.getBoundingClientRect();
    const scale = Math.min(rect.width / WIDTH, rect.height / HEIGHT);
    if (!(scale > 0)) return { x: 0, y: 0, inside: false };
    const drawnWidth = WIDTH * scale;
    const drawnHeight = HEIGHT * scale;
    const left = rect.left + (rect.width - drawnWidth) / 2;
    const top = rect.top + (rect.height - drawnHeight) / 2;
    return {
      x: Math.max(0, Math.min(WIDTH - 1, (event.clientX - left) / scale)),
      y: Math.max(0, Math.min(HEIGHT - 1, (event.clientY - top) / scale)),
      inside: event.clientX >= left && event.clientX < left + drawnWidth &&
              event.clientY >= top && event.clientY < top + drawnHeight
    };
  }

  function mouseMove(x, y, force = false) {
    const now = performance.now();
    if (!force && mouseInside && now - lastMoveSent < 16) return;
    send([1, ...u16(x), ...u16(y)]);
    mouseInside = true;
    lastReportedX = x;
    lastReportedY = y;
    lastMoveSent = now;
  }

  function mouseDrag(button, x, y, force = false) {
    const now = performance.now();
    if (!force && now - lastMoveSent < 16) return;
    send([7, button, ...u16(x), ...u16(y)]);
    mouseInside = true;
    lastReportedX = x;
    lastReportedY = y;
    lastMoveSent = now;
  }

  function mouseLeave() {
    if (!mouseInside) return;
    send([6]); // Native MOUSE_EXITED: don't clamp an off-canvas pointer to the minimap.
    mouseInside = false;
  }

  function mouseDown(button, x, y) {
    send([2, button, ...u16(x), ...u16(y)]);
  }

  function mouseUp(button, x, y) {
    send([3, button, ...u16(x), ...u16(y)]);
  }

  function click(button, x, y, holdMs = 80) {
    mouseMove(x, y, true);
    mouseDown(button, x, y);
    // A world click must survive both the client render/scene-picking pass
    // and the 20ms logic tick, even on a slow phone/remote desktop.
    setTimeout(() => mouseUp(button, x, y), holdMs);
  }

  function touchTap(button, point, heldMs) {
    // World object/ground interaction menus come from the last rendered frame.
    // Move the cursor at finger-down, then allow at least one render pass before
    // sending the press; sending move+press together at finger-up uses stale menus.
    const inScene = point.x < VIEWPORT_WIDTH && point.y < VIEWPORT_HEIGHT;
    const leadMs = inScene ? Math.max(0, 80 - heldMs) : 0;
    const generation = touchGeneration;
    const perform = () => {
      if (generation !== touchGeneration) return;
      click(button, point.x, point.y);
    };
    if (leadMs > 0) setTimeout(perform, leadMs);
    else perform();
  }

  canvas.addEventListener('pointerdown', (event) => {
    event.preventDefault();
    const point = gamePoint(event);
    if (!point.inside) return;
    canvas.setPointerCapture?.(event.pointerId);

    if (event.pointerType === 'mouse') {
      if (pointer || pinchBlocked) return;
      const button = event.button === 2 ? 3 : event.button === 1 ? 2 : 1;
      mouseMove(point.x, point.y, true);
      mouseDown(button, point.x, point.y);
      pointer = { id: event.pointerId, type: 'mouse', button, x: point.x, y: point.y, down: true };
      return;
    }

    if (pointer && pointer.type === 'mouse') return;
    touches.set(event.pointerId, {
      clientX: event.clientX,
      clientY: event.clientY,
      scene: point.x < VIEWPORT_WIDTH && point.y < VIEWPORT_HEIGHT
    });

    if (touches.size >= 2) {
      // The second finger cancels the first finger's pending tap or drag.
      // Keep suppressing clicks until *all* fingers lift, including after
      // one finger of a pinch is removed.
      touchGeneration++;
      if (pointer && pointer.down) {
        mouseUp(pointer.dragButton, pointer.x, pointer.y);
      }
      pointer = null;
      mouseLeave();
      pinchBlocked = true;
      const distance = pinchDistance();
      pinch = distance === null ? null : { distance };
      return;
    }
    if (pinchBlocked || pointer) return;

    // Prime the world hover immediately; the native client picks world
    // actions during rendering, so finger-up alone is too late for walking.
    touchGeneration++;
    mouseMove(point.x, point.y, true);
    pointer = {
      id: event.pointerId,
      type: 'touch',
      startX: point.x,
      startY: point.y,
      x: point.x,
      y: point.y,
      started: performance.now(),
      dragButton: point.x < VIEWPORT_WIDTH && point.y < VIEWPORT_HEIGHT ? 2 : 1,
      dragging: false,
      down: false
    };
  }, { passive: false });

  canvas.addEventListener('pointermove', (event) => {
    if (event.pointerType !== 'mouse' && touches.has(event.pointerId)) {
      const finger = touches.get(event.pointerId);
      finger.clientX = event.clientX;
      finger.clientY = event.clientY;
      if (pinchBlocked) {
        event.preventDefault();
        const distance = pinchDistance();
        if (pinch && distance !== null) {
          // Two fingers apart = negative camera distance offset (zoom in).
          // Two fingers together = positive offset (zoom out).
          const steps = Math.max(-10, Math.min(10,
              Math.trunc((distance - pinch.distance) / PINCH_STEP_PIXELS)));
          if (steps !== 0) {
            send([9, (-steps) & 255]);
            pinch.distance += steps * PINCH_STEP_PIXELS;
          }
        }
        return;
      }
    }
    const point = gamePoint(event);
    const active = pointer && pointer.id === event.pointerId;
    if (!active) {
      // Mouse hover is essential: RuneScape uses these coordinates to close
      // right-click menus when the cursor moves outside them.
      if (event.pointerType === 'mouse') {
        if (point.inside) mouseMove(point.x, point.y);
        else mouseLeave();
      }
      return;
    }

    event.preventDefault();
    if (!point.inside) {
      mouseLeave();
      return;
    }

    pointer.x = point.x;
    pointer.y = point.y;
    if (pointer.type === 'mouse') {
      if (pointer.down) mouseDrag(pointer.button, point.x, point.y);
      else mouseMove(point.x, point.y);
      return;
    }

    if (!pointer.dragging) {
      const distance = Math.hypot(point.x - pointer.startX, point.y - pointer.startY);
      if (distance > 7) {
        pointer.dragging = true;
        pointer.down = true;
        mouseMove(pointer.startX, pointer.startY, true);
        mouseDown(pointer.dragButton, pointer.startX, pointer.startY);
      }
    }
    if (pointer.dragging) mouseDrag(pointer.dragButton, point.x, point.y);
  }, { passive: false });

  canvas.addEventListener('pointerup', (event) => {
    if (event.pointerType !== 'mouse' && touches.has(event.pointerId)) {
      if (stopPinchPointer(event.pointerId)) {
        event.preventDefault();
        return; // No walk/right-click after either finger of a pinch lifts.
      }
    }
    if (!pointer || pointer.id !== event.pointerId) return;
    event.preventDefault();
    const current = pointer;
    pointer = null;
    const point = gamePoint(event);

    if (current.type === 'mouse') {
      if (point.inside) {
        if (point.x !== lastReportedX || point.y !== lastReportedY) {
          mouseDrag(current.button, point.x, point.y, true);
        }
      } else {
        mouseLeave();
      }
      if (current.down) mouseUp(current.button, point.inside ? point.x : current.x,
                                point.inside ? point.y : current.y);
      return;
    }

    if (current.dragging) {
      if (point.inside && (point.x !== lastReportedX || point.y !== lastReportedY)) {
        mouseDrag(current.dragButton, point.x, point.y, true);
      }
      if (current.down) mouseUp(current.dragButton,
                               point.inside ? point.x : current.x,
                               point.inside ? point.y : current.y);
      if (!point.inside) mouseLeave();
      return;
    }

    // Cancel a touch that ended outside the game instead of clicking a
    // clamped edge coordinate (which can accidentally click the minimap).
    if (!point.inside) {
      mouseLeave();
      return;
    }
    const held = performance.now() - current.started;
    touchTap(held >= 450 ? 3 : 1, current, held);
  }, { passive: false });

  canvas.addEventListener('pointercancel', (event) => {
    if (event.pointerType !== 'mouse' && touches.has(event.pointerId)) {
      if (stopPinchPointer(event.pointerId)) {
        touchGeneration++;
        return;
      }
    }
    if (!pointer || pointer.id !== event.pointerId) return;
    const current = pointer;
    pointer = null;
    if (current.down) mouseUp(current.dragButton || current.button || 1, current.x, current.y);
    touchGeneration++;
    mouseLeave();
  });

  canvas.addEventListener('pointerleave', () => {
    if (!pinchBlocked) mouseLeave();
  });
  window.addEventListener('blur', () => {
    if (pointer && pointer.down) {
      mouseUp(pointer.dragButton || pointer.button || 1, pointer.x, pointer.y);
    }
    pointer = null;
    touchGeneration++;
    touches.clear();
    pinch = null;
    pinchBlocked = false;
    mouseLeave();
  });

  // Native browser right-click and mobile long-press menus must be suppressed,
  // but the pointer/touch handlers above have ALREADY sent the RuneScape click.
  // Sending one here too opens a second menu and can trigger unwanted actions.
  canvas.addEventListener('contextmenu', (event) => event.preventDefault());

  function sendKey(type, code, character = 0) {
    send([type, ...u16(code), ...u16(character)]);
  }

  function sendKeyPair(code, character = 0) {
    sendKey(4, code, character);
    setTimeout(() => sendKey(5, code, character), 12);
  }

  function javaKeyCodeFromChar(character) {
    const code = character.charCodeAt(0);
    if (code >= 97 && code <= 122) return code - 32;
    if (code >= 65 && code <= 90) return code;
    if (code >= 48 && code <= 57) return code;
    const map = {
      ' ': 32, ',': 44, '-': 45, '.': 46, '/': 47, ';': 59, '=': 61,
      '[': 91, '\\': 92, ']': 93, '`': 192, "'": 222
    };
    return map[character] || 32;
  }

  function javaCodeFromKeyboardEvent(event) {
    const named = {
      Backspace: 8, Tab: 9, Enter: 10, Shift: 16, Control: 17, Alt: 18,
      Escape: 27, ' ': 32, PageUp: 33, PageDown: 34, End: 35, Home: 36,
      ArrowLeft: 37, ArrowUp: 38, ArrowRight: 39, ArrowDown: 40,
      Delete: 127
    };
    if (named[event.key] !== undefined) return named[event.key];
    if (event.key && event.key.length === 1) return javaKeyCodeFromChar(event.key);
    return event.keyCode || event.which || 0;
  }

  window.addEventListener('keydown', (event) => {
    if (document.activeElement === softInput) return;
    const code = javaCodeFromKeyboardEvent(event);
    const character = event.key && event.key.length === 1 ? event.key.charCodeAt(0) : 0;
    if (code) sendKey(4, code, character);
  });

  window.addEventListener('keyup', (event) => {
    if (document.activeElement === softInput) return;
    const code = javaCodeFromKeyboardEvent(event);
    const character = event.key && event.key.length === 1 ? event.key.charCodeAt(0) : 0;
    if (code) sendKey(5, code, character);
  });

  keyboardButton.addEventListener('click', () => {
    softInput.value = '';
    softInput.focus({ preventScroll: true });
  });

  softInput.addEventListener('beforeinput', (event) => {
    if (event.inputType === 'deleteContentBackward') {
      event.preventDefault();
      sendKeyPair(8, 8);
      softInput.value = '';
      return;
    }
    if (event.inputType === 'insertText' && event.data) {
      event.preventDefault();
      for (const character of event.data) {
        sendKeyPair(javaKeyCodeFromChar(character), character.charCodeAt(0));
      }
      softInput.value = '';
    }
  });

  softInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      sendKeyPair(10, 10);
      softInput.value = '';
    } else if (event.key.startsWith('Arrow')) {
      event.preventDefault();
      sendKeyPair(javaCodeFromKeyboardEvent(event), 0);
    }
  });

  ctx.fillStyle = '#000';
  ctx.fillRect(0, 0, WIDTH, HEIGHT);
  connect();

  if ('serviceWorker' in navigator && location.protocol !== 'file:') {
    addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => {}));
  }
})();

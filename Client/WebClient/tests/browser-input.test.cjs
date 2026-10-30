'use strict';

// Run from the repository root: node Client/WebClient/tests/browser-input.test.cjs
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const app = fs.readFileSync(path.join(__dirname, '../web/app.js'), 'utf8');

function browser(bounds = { left: 0, top: 0, width: 765, height: 503 }) {
  const callbacks = {};
  const sent = [];
  const timers = [];
  let now = 1000;

  const canvas = {
    getBoundingClientRect: () => bounds,
    getContext: () => ({ fillRect() {}, drawImage() {} }),
    setPointerCapture() {},
    addEventListener(name, handler) { callbacks['canvas:' + name] = handler; }
  };
  const status = { textContent: '', classList: { toggle() {} } };
  const keyboard = { addEventListener(name, handler) { callbacks['keyboard:' + name] = handler; } };
  const sound = { textContent: '', addEventListener(name, handler) { callbacks['sound:' + name] = handler; } };
  const softInput = { addEventListener(name, handler) { callbacks['input:' + name] = handler; } };
  const elements = { game: canvas, status, keyboard, sound, softInput };
  const document = { getElementById: name => elements[name], activeElement: null };
  const window = {
    addEventListener(name, handler) { callbacks['window:' + name] = handler; }
  };
  class WebSocket {
    static OPEN = 1;
    constructor() { this.readyState = WebSocket.OPEN; }
    addEventListener() {}
    send(bytes) { sent.push([...bytes]); }
  }

  vm.runInNewContext(app, {
    document, window, WebSocket,
    location: { protocol: 'http:', host: '127.0.0.1:8081' },
    performance: { now: () => now },
    setTimeout: (handler, delay) => timers.push({ handler, delay }),
    navigator: {},
    addEventListener() {},
    console
  }, { filename: 'web/app.js' });

  function emit(name, options = {}) {
    const target = name === 'blur' ? 'window:blur' : 'canvas:' + name;
    assert.equal(typeof callbacks[target], 'function', target + ' is registered');
    const event = {
      pointerId: 1, pointerType: 'mouse', button: 0,
      clientX: 100, clientY: 100, preventDefault() {},
      ...options
    };
    callbacks[target](event);
  }

  return {
    sent, timers, emit,
    advance(ms) { now += ms; },
    flushTimers() {
      while (timers.length) timers.shift().handler();
    }
  };
}
const count = (packets, type, button) =>
  packets.filter(packet => packet[0] === type &&
    (button === undefined || packet[1] === button)).length;
const coord = (packet, offset) => packet[offset] * 256 + packet[offset + 1];

{
  const b = browser();
  b.emit('pointermove', { clientX: 350, clientY: 200 });
  assert.equal(count(b.sent, 1), 1, 'ordinary hover must move the game cursor');
  b.emit('pointerleave');
  assert.equal(count(b.sent, 6), 1, 'canvas exit must reach JMouseListener');
  b.advance(30);
  b.emit('pointermove', { clientX: 200, clientY: 150 });
  assert.equal(count(b.sent, 1), 2, 'reentering must restore game cursor');

  b.emit('pointerdown', { button: 2 });
  b.emit('pointerup', { button: 2 });
  b.emit('contextmenu', { button: 2 });
  assert.equal(count(b.sent, 2, 3), 1, 'right click must press exactly once');
  assert.equal(count(b.sent, 3, 3), 1, 'right click must release exactly once');
  assert.equal(count(b.sent, 7), 0, 'stationary right-click cannot start a drag');
}
{
  const b = browser();
  b.emit('pointerdown', { pointerType: 'touch', clientX: 140, clientY: 110 });
  assert.equal(count(b.sent, 1), 1, 'finger down must prime world hover for scene picking');
  assert.equal(count(b.sent, 2), 0, 'finger down cannot accidentally move character');
  b.emit('pointerup', { pointerType: 'touch', clientX: 140, clientY: 110 });
  assert.equal(count(b.sent, 2, 1), 0, 'very fast scene tap waits for a rendered menu');
  assert.ok(b.timers[0].delay >= 80, 'allow a game render before world click');
  b.flushTimers();
  assert.equal(count(b.sent, 2, 1), 1, 'scene tap presses left after hover');
  assert.equal(count(b.sent, 3, 1), 1, 'scene tap releases after game tick');
}
{
  const b = browser();
  b.emit('pointerdown', { pointerType: 'touch' });
  b.advance(500);
  b.emit('contextmenu', { pointerType: 'touch' });
  b.emit('pointerup', { pointerType: 'touch' });
  b.flushTimers();
  assert.equal(count(b.sent, 2, 3), 1, 'long press must open only one right menu');
  assert.equal(count(b.sent, 3, 3), 1, 'long press must release once');
}
{
  const b = browser();
  b.emit('pointerdown', { pointerType: 'touch' });
  b.advance(300); // Hold finger on the 3D viewport before moving.
  b.emit('pointermove', { pointerType: 'touch', clientX: 135, clientY: 135 });
  b.emit('pointerup', { pointerType: 'touch', clientX: 135, clientY: 135 });
  assert.equal(count(b.sent, 2, 2), 1, '3D touch camera drag holds native middle mouse');
  assert.ok(count(b.sent, 7, 2) >= 1, '3D touch swipe rotates camera');
  assert.equal(count(b.sent, 3, 2), 1, 'camera drag releases middle mouse');
  assert.equal(count(b.sent, 2, 1), 0, 'camera motion cannot click/walk');
  assert.equal(b.timers.length, 0, 'camera drag must not schedule an extra tap');
}
{
  const b = browser();
  // Outside the 3D scene, inventory item drags must remain normal left drags.
  b.emit('pointerdown', { pointerType: 'touch', clientX: 600, clientY: 250 });
  b.emit('pointermove', { pointerType: 'touch', clientX: 625, clientY: 270 });
  b.emit('pointerup', { pointerType: 'touch', clientX: 625, clientY: 270 });
  assert.equal(count(b.sent, 2, 1), 1, 'inventory swipe presses left');
  assert.ok(count(b.sent, 7, 1) > 0, 'inventory swipe must drag item');
  assert.equal(count(b.sent, 3, 1), 1, 'inventory swipe releases left');
  assert.equal(count(b.sent, 2, 2), 0, 'inventory drag must not rotate camera');
}
{
  const b = browser();
  // UI tabs/minimap are not dependent on 3D scene-picking and stay responsive.
  b.emit('pointerdown', { pointerType: 'touch', clientX: 600, clientY: 80 });
  b.emit('pointerup', { pointerType: 'touch', clientX: 600, clientY: 80 });
  assert.equal(count(b.sent, 2, 1), 1, 'minimap tap presses without scene delay');
  b.flushTimers();
  assert.equal(count(b.sent, 3, 1), 1, 'minimap tap releases normally');
}
{
  const b = browser();
  // A held touch should allow the world menu to be built before tapping.
  b.emit('pointerdown', { pointerType: 'touch', clientX: 200, clientY: 160 });
  b.advance(130);
  b.emit('pointerup', { pointerType: 'touch', clientX: 200, clientY: 160 });
  assert.equal(count(b.sent, 2, 1), 1, 'normal-length world tap should click promptly');
  b.flushTimers();
  assert.equal(count(b.sent, 3, 1), 1, 'normal-length world tap releases');
}
{
  const b = browser();
  b.emit('pointerdown', { pointerType: 'touch' });
  b.emit('pointerup', { pointerType: 'touch', clientX: 1500, clientY: 100 });
  assert.equal(count(b.sent, 2), 0, 'finger released outside cannot click minimap');
  assert.equal(b.timers.length, 0, 'off-canvas touch cannot generate deferred click');
}
{
  const b = browser();
  b.emit('pointerdown', { clientX: 300, clientY: 300 });
  b.emit('pointermove', { clientX: 1200, clientY: 300 });
  b.emit('pointerup', { clientX: 1200, clientY: 300 });
  assert.equal(count(b.sent, 2, 1), 1, 'only one initial mouse click');
  assert.equal(count(b.sent, 6), 1, 'drag outside must send exit');
  assert.equal(count(b.sent, 3, 1), 1, 'mouse release outside must clear held button');
}
{
  const b = browser({ left: 120, top: 40, width: 382.5, height: 251.5 });
  b.emit('pointerdown', { clientX: 311.25, clientY: 165.75 });
  const press = b.sent.find(packet => packet[0] === 2);
  assert.ok(Math.abs(coord(press, 2) - 382) <= 1, 'scaled X maps to RuneScape X');
  assert.ok(Math.abs(coord(press, 4) - 251) <= 1, 'scaled Y maps to RuneScape Y');
  b.emit('blur');
  assert.equal(count(b.sent, 3, 1), 1, 'focus loss releases pressed mouse');
  assert.equal(count(b.sent, 6), 1, 'focus loss moves cursor off the game');
}

{
  const b = browser();
  // Pinch out (fingers apart) zooms the 3D camera in; pinch in zooms out.
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 1, clientX: 100, clientY: 100 });
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 2, clientX: 200, clientY: 100 });
  assert.equal(count(b.sent, 2), 0, 'starting pinch must not click/walk');
  b.emit('pointermove', { pointerType: 'touch', pointerId: 2, clientX: 252, clientY: 100 });
  const zoomIn = b.sent.filter(packet => packet[0] === 9);
  assert.equal(zoomIn.length, 1, 'pinch-out sends a camera zoom packet');
  assert.ok(zoomIn[0][1] > 127, 'pinch-out sends negative distance adjustment');
  b.emit('pointermove', { pointerType: 'touch', pointerId: 2, clientX: 126, clientY: 100 });
  const zoom = b.sent.filter(packet => packet[0] === 9);
  assert.equal(zoom.length, 2, 'pinch-in sends a camera zoom packet');
  assert.ok(zoom[1][1] > 0 && zoom[1][1] < 128,
      'pinch-in sends positive distance adjustment');
  b.emit('pointerup', { pointerType: 'touch', pointerId: 2, clientX: 126, clientY: 100 });
  b.emit('pointermove', { pointerType: 'touch', pointerId: 1, clientX: 130, clientY: 100 });
  b.emit('pointerup', { pointerType: 'touch', pointerId: 1, clientX: 130, clientY: 100 });
  b.flushTimers();
  assert.equal(count(b.sent, 2), 0, 'ending pinch must not click the world');
  assert.equal(count(b.sent, 7), 0, 'ending pinch must not rotate the camera');
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 3, clientX: 200, clientY: 120 });
  b.advance(150);
  b.emit('pointerup', { pointerType: 'touch', pointerId: 3, clientX: 200, clientY: 120 });
  b.flushTimers();
  assert.equal(count(b.sent, 2, 1), 1, 'regular touch-to-walk works after pinch');
}
{
  const b = browser();
  // Starting a second finger while rotating cancels the held middle button.
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 1 });
  b.emit('pointermove', { pointerType: 'touch', pointerId: 1, clientX: 130, clientY: 130 });
  assert.equal(count(b.sent, 2, 2), 1, 'one finger starts camera drag');
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 2, clientX: 210, clientY: 130 });
  assert.equal(count(b.sent, 3, 2), 1, 'pinch releases active middle-mouse drag');
  b.emit('pointermove', { pointerType: 'touch', pointerId: 2, clientX: 270, clientY: 130 });
  assert.ok(count(b.sent, 9) > 0, 'can zoom after starting a camera drag');
  b.emit('pointercancel', { pointerType: 'touch', pointerId: 1 });
  b.emit('pointerup', { pointerType: 'touch', pointerId: 2 });
  assert.equal(count(b.sent, 2, 1), 0, 'cancelling a pinch never triggers walking');
}
{
  const b = browser();
  // Two fingers over inventory must never zoom the 3D scene or select an item.
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 1, clientX: 590, clientY: 220 });
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 2, clientX: 650, clientY: 220 });
  b.emit('pointermove', { pointerType: 'touch', pointerId: 2, clientX: 730, clientY: 220 });
  b.emit('pointerup', { pointerType: 'touch', pointerId: 1 });
  b.emit('pointerup', { pointerType: 'touch', pointerId: 2 });
  b.flushTimers();
  assert.equal(count(b.sent, 9), 0, 'inventory pinch must not zoom world');
  assert.equal(count(b.sent, 2), 0, 'inventory pinch must not click an item');
}
{
  const b = browser({ left: 90, top: 20, width: 382.5, height: 251.5 });
  // CSS-scaled small-screen input still recognizes pinch gestures.
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 1, clientX: 145, clientY: 60 });
  b.emit('pointerdown', { pointerType: 'touch', pointerId: 2, clientX: 200, clientY: 60 });
  b.emit('pointermove', { pointerType: 'touch', pointerId: 2, clientX: 244, clientY: 60 });
  assert.ok(count(b.sent, 9) > 0, 'pinch works in a scaled phone-sized viewport');
  b.emit('blur');
  b.emit('pointerup', { pointerType: 'touch', pointerId: 1 });
  b.emit('pointerup', { pointerType: 'touch', pointerId: 2 });
  b.flushTimers();
  assert.equal(count(b.sent, 2), 0, 'losing focus during pinch cannot click');
}

console.log('SoloScape browser mouse/touch input regression checks passed.');

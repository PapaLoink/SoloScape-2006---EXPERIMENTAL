// ES5 view for the original RuneScape Windows Client's embedded IE browser.
(function () {
  'use strict';
  var width = 765;
  var height = 503;
  var canvas = document.getElementById('game');
  var status = document.getElementById('status');
  var screen = document.getElementById('screen');
  var socket;
  var decoding = false;
  var queuedFrame = null;
  var mouseButton = 0;
  var lastMove = 0;
  var lastPoint = { x: 0, y: 0 };
  var objectURL = window.URL || window.webkitURL;

  document.getElementById('sound').style.display = 'none';
  document.getElementById('keyboard').style.display = 'none';
  document.getElementById('softInput').style.display = 'none';
  screen.style.cssText = 'position:absolute;left:0;top:0;width:100%;height:100%;background:#000;overflow:auto;';
  status.style.cssText = 'position:absolute;left:0;top:0;padding:8px;color:white;background:black;font:12px Arial;z-index:3;';
  canvas.width = width;
  canvas.height = height;

  function resize() {
    var availableWidth = document.documentElement.clientWidth;
    var availableHeight = document.documentElement.clientHeight;
    // Preserve the original 1:1 game size.
    // Smaller windows can scroll rather than shrink the game or crop its controls.
    canvas.style.cssText = 'position:absolute;display:block;background:black;outline:none;border:0;width:' +
      width + 'px;height:' + height +
      'px;left:' + Math.max(0, Math.floor((availableWidth - width) / 2)) +
      'px;top:' + Math.max(0, Math.floor((availableHeight - height) / 2)) + 'px;';
  }
  window.addEventListener('resize', resize);
  resize();

  function setStatus(text) {
    status.style.display = text ? 'block' : 'none';
    status.textContent = text;
  }
  if (!window.WebSocket || !canvas.getContext || !objectURL) {
    setStatus('This launcher needs IE11 browser mode. Run Enable-Original-Client.bat, then reopen it.');
    return;
  }
  var ctx = canvas.getContext('2d');
  ctx.fillStyle = '#000';
  ctx.fillRect(0, 0, width, height);

  function decodeNext() {
    if (!queuedFrame || decoding) return;
    decoding = true;
    var bytes = queuedFrame;
    queuedFrame = null;
    var url = objectURL.createObjectURL(new Blob([bytes], { type: 'image/png' }));
    var image = new Image();
    function finish() {
      objectURL.revokeObjectURL(url);
      decoding = false;
      decodeNext();
    }
    image.onload = function () {
      ctx.drawImage(image, 0, 0, width, height);
      setStatus('');
      finish();
    };
    image.onerror = function () {
      setStatus('Unable to decode client frame');
      finish();
    };
    image.src = url;
  }
  function connect() {
    setStatus('Connecting to SoloScape...');
    socket = new WebSocket((location.protocol === 'https:' ? 'wss://' : 'ws://') + location.host + '/client');
    socket.binaryType = 'arraybuffer';
    socket.onmessage = function (event) {
      queuedFrame = event.data;
      decodeNext();
    };
    socket.onclose = function () {
      setStatus('Client disconnected. Reconnecting...');
      setTimeout(connect, 1000);
    };
    socket.onerror = function () { setStatus('Unable to connect to SoloScape'); };
  }
  function send(bytes) {
    if (socket && socket.readyState === 1) socket.send(new Uint8Array(bytes));
  }
  function u16(value) {
    value = Math.max(0, Math.min(65535, Math.round(value)));
    return [(value >>> 8) & 255, value & 255];
  }
  function point(event) {
    var rect = canvas.getBoundingClientRect();
    lastPoint = {
      x: Math.max(0, Math.min(width - 1, (event.clientX - rect.left) * width / (rect.right - rect.left))),
      y: Math.max(0, Math.min(height - 1, (event.clientY - rect.top) * height / (rect.bottom - rect.top)))
    };
    return lastPoint;
  }
  function mouse(type, button, position) {
    send((type === 1 ? [type] : [type, button]).concat(u16(position.x), u16(position.y)));
  }
  canvas.addEventListener('mousemove', function (event) {
    var position = point(event);
    if (Date.now() - lastMove < 20) return;
    lastMove = Date.now();
    mouse(1, 0, position);
  });
  canvas.addEventListener('mousedown', function (event) {
    event.preventDefault();
    var position = point(event);
    mouseButton = event.button === 2 ? 3 : event.button === 1 ? 2 : 1;
    mouse(1, 0, position);
    mouse(2, mouseButton, position);
    canvas.focus();
  });
  window.addEventListener('mouseup', function (event) {
    if (!mouseButton) return;
    mouse(3, mouseButton, point(event));
    mouseButton = 0;
  });
  canvas.addEventListener('contextmenu', function (event) { event.preventDefault(); });
  canvas.tabIndex = 0;

  function key(type, event) {
    var code = event.keyCode || event.which;
    if (code === 13) code = 10;
    if (code === 46) code = 127;
    var character = event.key && event.key.length === 1 ? event.key.charCodeAt(0) : 0;
    if (!character && code >= 65 && code <= 90) character = event.shiftKey ? code : code + 32;
    if (!character && code >= 48 && code <= 57 && !event.shiftKey) character = code;
    send([type].concat(u16(code), u16(character)));
    event.preventDefault();
  }
  window.addEventListener('keydown', function (event) { key(4, event); });
  window.addEventListener('keyup', function (event) { key(5, event); });
  window.addEventListener('blur', function () {
    if (mouseButton) mouse(3, mouseButton, lastPoint);
    mouseButton = 0;
  });
  connect();
}());

'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const html = fs.readFileSync('Client/WebClient/web/mobile.html', 'utf8');
for (const page of ['desktop.html', 'local-render.html']) {
  const content = fs.readFileSync('Client/WebClient/web/' + page, 'utf8');
  const script = content.split('<script>')[1].split('</script>')[0];
  new vm.Script(script);
  if (page === 'desktop.html') {
    assert.ok(!content.includes('id="keyboard"') && !content.includes('id="phone-input"'),
        'Desktop must not include phone keyboard controls');
    assert.ok(content.includes('enableInputMethods: true'), 'Desktop keyboard input must stay enabled');
  } else {
    for (const coarse of [false, true]) {
      let destination;
      vm.runInNewContext(script, { window: {
        matchMedia: () => ({ matches: coarse }),
        location: { search: '?test=1', hash: '#game', replace: value => { destination = value; } }
      } });
      assert.equal(destination, (coarse ? 'mobile.html' : 'desktop.html') + '?test=1#game',
          'Legacy link did not choose the correct client page');
    }
  }
}
const inline = html.match(/<script>\s*([\s\S]*?)<\/script>/)[1];
new vm.Script(inline);
const source = inline.slice(inline.indexOf('  // Local touch gestures'),
    inline.indexOf('  function fitClient()'));
assert.ok(source.includes('tapAfterHover'), 'Touch handlers were not found');
const handlers = {};
const calls = [];
let now = 0;
let frame = 0;
let autoRender = true;
let allowHover = true;
let hoveredPoint = null;
let consumeAfter = 2;
let consumePolls = 0;
let rect = { left: 0, top: 0, width: 765, height: 503 };
const display = {
  addEventListener(type, handler) { (handlers[type] ||= []).push(handler); },
  getBoundingClientRect() { return rect; },
  setPointerCapture() {}
};
const context = {
  display,
  window: { addEventListener() {} },
  clientBridge: {
    async sendMouse(id, button, x, y) {
      calls.push({ id, button, x, y, frame, consumed: consumePolls >= consumeAfter });
      if (id === 501) consumePolls = 0;
      if (id === 503) hoveredPoint = { x, y };
      return frame;
    },
    async sceneHoverReady(x, y, afterFrame) {
      if (autoRender) frame++;
      return allowHover && frame > afterFrame && hoveredPoint.x === x && hoveredPoint.y === y;
    },
    async clickConsumed() { return ++consumePolls >= consumeAfter; },
    async zoom(steps) { calls.push({ zoom: steps }); }
  },
  performance: { now: () => now },
  setTimeout(callback, delay) { now += delay; queueMicrotask(callback); },
  console
};
vm.runInNewContext(source, context);
function pointer(type, id, x, y, pointerType = 'touch') {
  const scale = Math.min(rect.width / 765, rect.height / 503);
  const event = {
    pointerType, pointerId: id,
    clientX: rect.left + (rect.width - 765 * scale) / 2 + x * scale,
    clientY: rect.top + (rect.height - 503 * scale) / 2 + y * scale,
    preventDefault() { this.prevented = true; },
    stopImmediatePropagation() { this.stopped = true; }
  };
  for (const handler of handlers[type] || []) {
    handler(event);
    if (event.stopped) break;
  }
  return event;
}
async function settle() {
  for (let n = 0; n < 1500; n++) await Promise.resolve();
}
(async () => {
  const down = pointer('pointerdown', 1, 100, 100);
  pointer('pointerup', 1, 100, 100);
  await settle();
  assert.ok(down.prevented && down.stopped, 'CheerpJ must not also synthesize a touch click');
  assert.deepEqual(calls.map(e => [e.id, e.button]), [[503, 0], [501, 1], [502, 1]]);
  assert.ok(calls[1].frame > calls[0].frame, 'Ground tap pressed before scene picking rendered');

  calls.length = 0;
  autoRender = false;
  pointer('pointerdown', 16, 150, 150);
  await settle();
  frame++; // A slow phone renders the hover while the finger is still down.
  const liftTime = now;
  pointer('pointerup', 16, 150, 150);
  await settle();
  assert.equal(calls.filter(e => e.id === 503).length, 1,
      'Finger lift unnecessarily restarted hover and waited for another frame');
  assert.equal(calls.filter(e => e.id === 501).length, 1,
      'A warmed tap must click without requiring any more rendered frames');
  assert.ok(now - liftTime <= 16,
      'Already rendered tap waited for a scene polling timer after finger lift');
  autoRender = true;

  calls.length = 0;
  pointer('pointerdown', 11, 100, 100);
  pointer('pointermove', 11, 103, 102);
  pointer('pointerup', 11, 105, 103);
  await settle();
  assert.equal(calls.filter(e => e.id === 501).length, 1, 'One tap must deliver one click');
  assert.deepEqual(calls.filter(e => e.id === 501).map(e => [e.x, e.y]), [[105, 103]],
      'Finger drift must prime and click the final tap position');
  assert.deepEqual(calls.filter(e => e.id === 503).map(e => [e.x, e.y]),
      [[100, 100], [105, 103]], 'Release coordinates were not hovered before clicking');

  calls.length = 0;
  pointer('pointerdown', 13, 100, 100);
  pointer('pointermove', 13, 109, 102);
  pointer('pointerup', 13, 110, 103);
  await settle();
  assert.equal(calls.filter(e => e.id === 501 && e.button === 1).length, 1,
      'Normal finger wobble incorrectly became a camera drag');
  assert.ok(!calls.some(e => e.button === 2), 'Small tap movement rotated the camera');

  calls.length = 0;
  consumeAfter = 6; // Model a slow client logic tick.
  pointer('pointerdown', 14, 100, 100);
  pointer('pointerup', 14, 100, 100);
  pointer('pointerdown', 15, 200, 200);
  pointer('pointerup', 15, 200, 200);
  await settle();
  assert.deepEqual(calls.filter(e => e.id === 501 || e.id === 502).map(e => [e.id, e.x]),
      [[501, 100], [502, 100], [501, 200], [502, 200]],
      'Quick successive taps must each be delivered once, in order');
  assert.ok(calls.filter(e => e.id === 502).every(e => e.consumed),
      'Tap released before the slow game tick consumed its press');
  consumeAfter = 2;

  calls.length = 0;
  allowHover = false;
  pointer('pointerdown', 12, 200, 200);
  pointer('pointerup', 12, 200, 200);
  await settle();
  assert.ok(!calls.some(e => e.id === 501),
      'Unrelated rendered frames must not acknowledge hover at the tap position');
  allowHover = true;

  calls.length = 0;
  rect = { left: 13, top: 7, width: 1000, height: 600 };
  pointer('pointerdown', 2, 700, 50);
  pointer('pointerup', 2, 700, 50);
  await settle();
  const click = calls.find(e => e.id === 501);
  assert.ok(Math.abs(click.x - 700) <= 1 && Math.abs(click.y - 50) <= 1,
      'Letterboxed display coordinates were mapped incorrectly');
  rect = { left: 0, top: 0, width: 765, height: 503 };

  calls.length = 0;
  pointer('pointerdown', 3, 100, 100);
  now += 30;
  pointer('pointermove', 3, 140, 120);
  pointer('pointerup', 3, 140, 120);
  await settle();
  assert.ok(calls.some(e => e.id === 506 && e.button === 2), 'World drag did not rotate');
  assert.ok(!calls.some(e => e.id === 501 && e.button === 1), 'Camera drag generated a walk');

  calls.length = 0;
  pointer('pointerdown', 4, 100, 100);
  pointer('pointerdown', 5, 200, 100);
  pointer('pointermove', 5, 248, 100);
  pointer('pointermove', 5, 176, 100);
  pointer('pointerup', 5, 176, 100);
  pointer('pointerup', 4, 100, 100);
  await settle();
  assert.deepEqual(calls.filter(e => e.zoom !== undefined).map(e => e.zoom), [-4, 6],
      'Pinching apart/together did not zoom in/out');
  assert.ok(!calls.some(e => e.id === 501), 'Pinch generated an unintended click');

  calls.length = 0;
  pointer('pointerdown', 6, 650, 400);
  now += 30;
  pointer('pointermove', 6, 680, 410);
  pointer('pointerup', 6, 680, 410);
  await settle();
  assert.ok(calls.some(e => e.id === 506 && e.button === 1), 'Inventory drag was lost');
  assert.ok(!calls.some(e => e.button === 2), 'Inventory drag rotated the camera');

  calls.length = 0;
  pointer('pointerdown', 7, 100, 100);
  pointer('pointerup', 7, 100, 100);
  pointer('pointerdown', 8, 100, 100);
  pointer('pointercancel', 8, 100, 100);
  await settle();
  assert.ok(!calls.some(e => e.id === 501), 'Cancelled tap was delivered late');

  calls.length = 0;
  pointer('pointerdown', 9, 100, 100);
  pointer('pointermove', 9, 140, 120);
  pointer('pointercancel', 9, 140, 120);
  await settle();
  assert.ok(calls.some(e => e.id === 502 && e.button === 2), 'Cancelled drag left camera pressed');

  const mouse = pointer('pointerdown', 10, 100, 100, 'mouse');
  assert.ok(!mouse.prevented, 'Desktop mouse input should pass through');
  console.log('Local renderer touch gesture checks passed.');
})().catch(error => { console.error(error); process.exitCode = 1; });

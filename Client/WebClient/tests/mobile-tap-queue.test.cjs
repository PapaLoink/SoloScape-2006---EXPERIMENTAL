'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const html = fs.readFileSync(__dirname + '/../web/mobile.html', 'utf8');
const source = html.slice(html.indexOf('  // Local touch gestures'), html.indexOf('  function fitClient()'));
assert.ok(source.length > 1000);
async function harness(overrides = {}) {
  let now = 0;
  const timers = [];
  const handlers = {};
  const calls = [];
  const bridge = {
    sendMouse: async (...args) => { calls.push(args); return 1; },
    sceneHoverReady: async () => true,
    clickConsumed: async () => true,
    zoom: async () => {},
    ...overrides
  };
  const context = vm.createContext({
    clientBridge: bridge, performance: { now: () => now }, console,
    setTimeout: (fn, ms) => { timers.push({ at: now + ms, fn }); },
    display: {
      getBoundingClientRect: () => ({ left: 0, top: 0, width: 765, height: 503 }),
      setPointerCapture() {},
      addEventListener: (name, fn) => { handlers[name] = fn; }
    },
    window: { addEventListener: (name, fn) => { handlers[name] = fn; } }
  });
  vm.runInContext(source, context);
  async function flush() { for (let i = 0; i < 40; i++) await Promise.resolve(); }
  async function advance(ms) {
    const end = now + ms;
    await flush();
    while (true) {
      timers.sort((a, b) => a.at - b.at);
      if (!timers.length || timers[0].at > end) break;
      const timer = timers.shift();
      now = timer.at;
      timer.fn();
      await flush();
    }
    now = end;
    await flush();
  }
  function event(type, x = 100, y = 100, id = 1) {
    handlers[type]({ pointerType: 'touch', pointerId: id, clientX: x, clientY: y,
      preventDefault() {}, stopImmediatePropagation() {} });
  }
  async function tap(x = 100, y = 100) {
    event('pointerdown', x, y); await flush();
    event('pointerup', x, y); await flush();
  }
  return { calls, event, tap, advance, flush };
}
const presses = h => h.calls.filter(c => c[0] === 501);
const releases = h => h.calls.filter(c => c[0] === 502);
(async () => {
  let h = await harness();
  await h.tap();
  assert.equal(presses(h).length, 1, 'ready scene taps press immediately');
  assert.equal(releases(h).length, 1, 'normal tap releases');
  assert.deepEqual(h.calls.map(c => c[0]), [503, 501, 502]);

  let ready = false;
  h = await harness({ sceneHoverReady: async () => ready });
  await h.tap(100);
  await h.advance(100);
  await h.tap(200);
  await h.advance(100);
  await h.tap(300);
  ready = true;
  await h.advance(32);
  assert.deepEqual(presses(h).map(c => c[2]), [300], 'only newest waiting tap fires');

  ready = false;
  h = await harness({ sceneHoverReady: async () => ready });
  await h.tap();
  await h.advance(600);
  ready = true;
  await h.advance(32);
  assert.equal(presses(h).length, 0, 'tap expires while waiting for scene');

  let unblock;
  let first = true;
  h = await harness({ sendMouse: (...args) => {
    h.calls.push(args);
    if (first) { first = false; return new Promise(resolve => { unblock = resolve; }); }
    return Promise.resolve(1);
  } });
  await h.tap(600, 400);
  await h.advance(200);
  await h.tap(650, 400);
  await h.advance(600);
  unblock(1); await h.flush();
  assert.equal(presses(h).length, 0, 'queue residence counts toward expiry even for UI taps');
  await h.tap(700, 400);
  assert.equal(presses(h).length, 1, 'fresh input recovers after bridge stall');

  h = await harness({ clickConsumed: async () => false });
  await h.tap();
  assert.equal(releases(h).length, 0);
  await h.advance(520);
  assert.equal(releases(h).length, 1, 'unconsumed press is always released within budget');

  h = await harness({ clickConsumed: async () => false });
  await h.tap();
  h.event('pointerdown', 200);
  await h.advance(16);
  assert.equal(releases(h).length, 1, 'new gesture releases previous held click');
  h.event('pointercancel'); await h.flush();
  await h.advance(600);
  assert.equal(presses(h).length, 1, 'cancel does not create a second press');

  h = await harness();
  h.event('pointerdown');
  await h.advance(460);
  h.event('pointerup'); await h.flush();
  assert.equal(presses(h)[0][1], 3, 'long press remains a right click');

  h = await harness();
  h.event('pointerdown');
  h.event('pointerdown', 200, 100, 2);
  h.event('pointerup'); h.event('pointerup', 200, 100, 2);
  await h.flush();
  assert.equal(presses(h).length, 0, 'pinch never replays a tap');
  console.log('Mobile tap queue regression checks passed');
})().catch(error => { console.error(error); process.exitCode = 1; });

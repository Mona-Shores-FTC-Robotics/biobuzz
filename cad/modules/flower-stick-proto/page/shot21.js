const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1200, height: 1500 } });
  const errs = []; p.on('pageerror', e => errs.push(String(e.stack || e)));
  const html = require('fs').readFileSync(process.argv[2], 'utf8');
  await p.setContent('<!doctype html><html><head><meta charset="utf-8"></head><body>' + html + '</body></html>', { waitUntil: 'networkidle' });
  await p.waitForTimeout(5000); await p.locator('#v10-svg').scrollIntoViewIfNeeded();
  const shots = [];
  for (const t of [1.0, 1.9, 2.6, 3.4, 4.4, 5.3]) { await p.evaluate(t => v10Seek(t), t); await p.waitForTimeout(200); await p.locator('#v10-svg').screenshot({ path: `shot21-${t}.png`, timeout: 60000, animations: 'disabled' }); shots.push(t); }
  const { createCanvas, loadImage } = (() => { try { return require('canvas'); } catch (e) { return {}; } })();
  console.log('errors:', errs, shots); await b.close();
})();

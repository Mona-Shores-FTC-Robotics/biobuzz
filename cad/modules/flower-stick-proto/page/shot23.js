const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1200, height: 1500 } });
  const errs = []; p.on('pageerror', e => errs.push(String(e.stack || e)));
  const html = require('fs').readFileSync(process.argv[2], 'utf8');
  await p.setContent('<!doctype html><html><head><meta charset="utf-8"></head><body>' + html + '</body></html>', { waitUntil: 'networkidle' });
  await p.waitForTimeout(6000);
  for (const k of ['v7', 'blockA', 'v8', 'final']) { await p.locator(`[data-preset="${k}"]`).click(); await p.waitForTimeout(2500); console.log(k + ':', await p.locator('#sim-result').textContent()); }
  for (const s of ['straight', 'ss']) { await p.locator(`[data-stack="${s}"]`).click(); await p.waitForTimeout(2500); console.log('final/' + s + ':', await p.locator('#sim-result').textContent()); }
  await p.locator('[data-stack="fb"]').click(); await p.locator('[data-preset="v7"]').click(); await p.waitForTimeout(2500);
  await p.evaluate(() => { const s = document.getElementById('sim-scrub'); s.value = s.max; s.dispatchEvent(new Event('input')); }); await p.waitForTimeout(500);
  await p.locator('#drive').screenshot({ path: 'shot23-drive.png', timeout: 120000, animations: 'disabled' });
  console.log('errors:', errs); await b.close();
})();

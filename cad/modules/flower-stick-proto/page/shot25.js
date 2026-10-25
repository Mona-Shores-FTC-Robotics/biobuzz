const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1200, height: 1500 } });
  const errs = []; p.on('pageerror', e => errs.push(String(e)));
  const html = require('fs').readFileSync(process.argv[2], 'utf8');
  await p.setContent('<!doctype html><html><head><meta charset="utf-8"></head><body>' + html + '</body></html>', { waitUntil: 'networkidle' });
  await p.waitForTimeout(6000);
  for (const k of ['v2', 'v6', 'final']) { await p.locator(`[data-preset="${k}"]`).click(); await p.waitForTimeout(2500); console.log(k + ':', (await p.locator('#sim-result').textContent()).slice(0, 160)); }
  await p.locator('#drive .bar').first().screenshot({ path: 'shot25-bar.png', timeout: 60000, animations: 'disabled' });
  console.log('errors:', errs); await b.close();
})();

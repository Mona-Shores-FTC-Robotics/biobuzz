const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1200, height: 1500 } });
  const errs = []; p.on('pageerror', e => errs.push(String(e.stack || e)));
  const html = require('fs').readFileSync(process.argv[2], 'utf8');
  await p.setContent('<!doctype html><html><head><meta charset="utf-8"></head><body>' + html + '</body></html>', { waitUntil: 'networkidle' });
  await p.waitForTimeout(6000);
  await p.locator('[data-poker="on"]').click(); await p.waitForTimeout(3000); console.log('poker:', await p.locator('#sim-result').textContent());
  await p.evaluate(() => { const s = document.getElementById('sim-scrub'); s.value = Math.round(s.max * 0.36); s.dispatchEvent(new Event('input')); }); await p.waitForTimeout(500);
  await p.locator('.views').screenshot({ path: 'shot22-views.png', timeout: 120000, animations: 'disabled' });
  console.log('errors:', errs); await b.close();
})();

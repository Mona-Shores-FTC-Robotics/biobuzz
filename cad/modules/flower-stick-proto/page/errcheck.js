const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1200, height: 1500 } });
  const errs = []; p.on('console', m => { if (m.type() === 'error') errs.push(m.text()); }); p.on('pageerror', e => errs.push(String(e.stack || e)));
  const html = require('fs').readFileSync(process.argv[2], 'utf8');
  await p.setContent('<!doctype html><html><head><meta charset="utf-8"></head><body>' + html + '</body></html>', { waitUntil: 'networkidle' });
  await p.waitForTimeout(3000);
  console.log('errors:', errs.slice(0, 5)); console.log('result:', await p.locator('#sim-result').textContent()); console.log('scrub max:', await p.locator('#sim-scrub').getAttribute('max'));
  await b.close();
})();

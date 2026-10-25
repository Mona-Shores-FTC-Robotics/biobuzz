const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 700, height: 840 } });
  const errs = []; p.on('pageerror', e => errs.push(String(e))); p.on('console', m => { if (m.type() === 'error') errs.push(m.text()); });
  await p.goto('file://' + process.cwd() + '/render_v10.html', { waitUntil: 'networkidle' }); await p.waitForTimeout(2500);
  await p.locator('.row').screenshot({ path: '/home/user/biobuzz/cad/modules/flower-stick-proto/v10.png' });
  console.log('errors', errs); await b.close();
})();

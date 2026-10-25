const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1400, height: 900 } });
  const errs = []; p.on('pageerror', e => errs.push(String(e)));
  await p.goto('file://' + process.cwd() + '/render_front.html', { waitUntil: 'networkidle' }); await p.waitForTimeout(4000);
  const views = { frontleft: [14, 16, 7, -2, 0, 3], top: [0.5, 0, 40, 0, 0, 0], side: [-2, -30, 4, -2, 0, 3], low: [12, 2, 2.5, -2, 0, 2.5] };
  for (const k in views) { await p.evaluate(v => view(...v), views[k]); await p.waitForTimeout(500); await p.screenshot({ path: `front_${k}.png` }); }
  console.log('errors', errs); await b.close();
})();

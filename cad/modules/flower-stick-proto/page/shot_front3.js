const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1400, height: 900 } });
  await p.goto('file://' + process.cwd() + '/render_front.html', { waitUntil: 'networkidle' }); await p.waitForTimeout(4000);
  const views = { servo: [8, 4, 9.5, -2.8, 0.3, 6.3], discs: [7, -5, 2.8, -2.8, 0, 1.6], discs_top: [-2.8, 0.01, 14, -2.8, 0, 1.6], tower: [4, 14, 9, -2, 5.4, 8] };
  for (const k in views) { await p.evaluate(v => view(...v), views[k]); await p.waitForTimeout(500); await p.screenshot({ path: `front_${k}.png` }); }
  await b.close();
})();

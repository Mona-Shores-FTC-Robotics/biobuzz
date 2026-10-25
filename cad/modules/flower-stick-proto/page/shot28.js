const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1400, height: 1700 } });
  const errs = []; p.on('pageerror', e => errs.push(String(e)));
  await p.setContent(require('fs').readFileSync('flower-stick-sim.html', 'utf8'), { waitUntil: 'networkidle' });
  await p.waitForTimeout(6000);
  await p.locator('[data-mount="low"]').click(); await p.waitForTimeout(2500);
  for (const [k, frac] of [['v7', 0.45], ['final', 0.6]]) {
    await p.locator(`[data-preset="${k}"]`).click(); await p.waitForTimeout(3000);
    console.log(k + ':', (await p.locator('#sim-result').textContent()).slice(0, 200));
    await p.evaluate(f => { const s = document.getElementById('sim-scrub'); s.value = Math.round(s.max * f); s.dispatchEvent(new Event('input')); }, frac);
    await p.waitForTimeout(800);
    await p.locator('#drive .views').screenshot({ path: `shot28-${k}.png`, timeout: 60000, animations: 'disabled' });
  }
  console.log('errors:', errs); await b.close();
})();

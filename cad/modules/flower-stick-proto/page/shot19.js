const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1200, height: 1500 } });
  const errs = []; p.on('console', m => { if (m.type() === 'error') errs.push(m.text()); }); p.on('pageerror', e => errs.push(String(e)));
  const html = require('fs').readFileSync(process.argv[2], 'utf8');
  await p.setContent('<!doctype html><html><head><meta charset="utf-8"></head><body>' + html + '</body></html>', { waitUntil: 'networkidle' });
  await p.waitForTimeout(6000);
  const toEnd = async () => { await p.evaluate(() => { const s = document.getElementById('sim-scrub'); s.value = s.max; s.dispatchEvent(new Event('input')); }); await p.waitForTimeout(500); };
  console.log('scrub box', await p.locator('#sim-scrub').boundingBox());
  await toEnd(); await p.locator('#drive').screenshot({ path: 'shot19-drive.png', timeout: 120000, animations: 'disabled' }); console.log('v7:', await p.locator('#sim-result').textContent());
  for (const k of ['v8', 'v10']) { await p.locator(`[data-preset="${k}"]`).click(); await p.waitForTimeout(2500); console.log(k + ':', await p.locator('#sim-result').textContent()); }
  await p.locator('[data-push="10"]').click(); await p.waitForTimeout(2500); console.log('v10 fast:', await p.locator('#sim-result').textContent());
  await p.locator('#blade').scrollIntoViewIfNeeded(); await p.waitForTimeout(1500); await p.locator('#blade').screenshot({ path: 'shot19-parts.png', timeout: 120000, animations: 'disabled' });
  console.log('errors:', errs, 'bytes', html.length); await b.close();
})();

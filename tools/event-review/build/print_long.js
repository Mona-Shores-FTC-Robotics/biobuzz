const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 1088, height: 1200 } });
  const errs = []; p.on('pageerror', (e) => errs.push(String(e)));
  await p.goto('file://' + process.argv[2]);
  await p.waitForFunction(() => window.__done === true);
  await p.emulateMedia({ media: 'screen' });
  const h = await p.evaluate(() => Math.ceil(document.documentElement.scrollHeight));
  await p.addStyleTag({ content: '@page { size: 1088px ' + (h + 2) + 'px; margin: 0; }' });
  await p.pdf({ path: process.argv[3], width: '1088px', height: (h + 2) + 'px', printBackground: true, margin: { top: '0', bottom: '0', left: '0', right: '0' }, pageRanges: '1' });
  console.log('height', h, 'errors:', JSON.stringify(errs));
  await b.close();
})();

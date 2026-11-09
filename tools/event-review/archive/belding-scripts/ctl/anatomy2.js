  dash.draw('anatomy', (el) => {
    if (!bz.ready(el, ['cycle_steps'])) return;
    const data = dash.data('cycle_steps').data;
    const W = el.clientWidth || 600;
    const left = 104, right = 60, barH = 30, zoomH = 24, funnel = 26, block = barH + funnel + zoomH + 34;
    const robots = ['20245', '19429'];
    const steps = (rb) => data.filter((d) => d.robot === rb && d.seconds != null).sort((a, b) => a.order - b.order);
    const totals = robots.map((rb) => d3.sum(steps(rb), (d) => d.seconds));
    const launchTot = robots.map((rb) => d3.sum(steps(rb).filter((d) => d.phase === 'launch'), (d) => d.seconds));
    const x = d3.scaleLinear([0, Math.ceil(d3.max(totals) / 5) * 5], [left, W - right]);
    const zx = d3.scaleLinear([0, Math.ceil(d3.max(launchTot) * 2) / 2], [left, W - right]);
    const H = robots.length * block + 4;
    const svg = d3.create('svg').attr('width', '100%').attr('viewBox', `0 0 ${W} ${H}`).attr('height', H);
    el.replaceChildren(svg.node());
    svg.style('display', 'block');
    const tip = bz.tip(el);
    const collectColor = (o) => `var(--c${o})`;
    const zoomColor = (o) => `var(--z${o - 3})`;
    const label = (x0, w, y, h, text, dark) => {
      const t = svg.append('text').attr('x', x0 + w / 2).attr('y', y + h / 2).attr('dy', '0.32em').attr('text-anchor', 'middle')
        .attr('pointer-events', 'none').style('fill', dark ? '#0A0C12' : '#EEF1F8').style('font-weight', '700').text(text);
      if (t.node().getComputedTextLength() > w - 6) t.remove();
    };
    robots.forEach((robot, ri) => {
      const y = 4 + ri * block, s = steps(robot);
      svg.append('text').attr('class', 'panel').attr('x', 0).attr('y', y + barH / 2).attr('dy', '0.32em').style('fill', bz.tc(robot)).text(bz.tn(robot));
      // main bar: collect steps, then one launch block
      let acc = 0;
      for (const d of s.filter((q) => q.phase === 'collect')) {
        const x0 = x(acc), w = x(acc + d.seconds) - x0;
        svg.append('rect').attr('x', x0 + 0.5).attr('y', y).attr('width', Math.max(1, w - 1)).attr('height', barH).attr('rx', 2).attr('fill', collectColor(d.order))
          .attr('data-source', 'cycle_steps').attr('data-where', `robot=${robot}&step=${encodeURIComponent(d.step)}`).attr('data-field', 'seconds')
          .on('pointermove', (ev) => tip.show(ev, [`${bz.TEAM[robot].name} · ${d.step}`, `${d3.format('.1f')(d.seconds)} s median`, `${d.samples} cycles`])).on('pointerleave', () => tip.hide());
        label(x0, w, y, barH, d3.format('.1f')(d.seconds), d.order === 3);
        acc += d.seconds;
      }
      const lt = launchTot[ri], lx0 = x(acc), lw = x(acc + lt) - lx0;
      svg.append('rect').attr('x', lx0 + 0.5).attr('y', y).attr('width', Math.max(2, lw - 1)).attr('height', barH).attr('rx', 2).attr('fill', 'var(--launch)')
        .on('pointermove', (ev) => tip.show(ev, [`${bz.TEAM[robot].name} · Launch`, `${d3.format('.1f')(lt)} s, broken down below`])).on('pointerleave', () => tip.hide());
      svg.append('text').attr('class', 'val').attr('x', x(acc + lt) + 6).attr('y', y + barH / 2).attr('dy', '0.32em').text(`${d3.format('.1f')(acc + lt)} s`);
      // funnel from the launch block to the zoom strip
      const zy = y + barH + funnel, zEnd = zx(lt);
      svg.append('path').attr('d', `M${lx0},${y + barH} L${lx0 + lw},${y + barH} L${zEnd},${zy} L${left},${zy} Z`)
        .attr('fill', 'var(--launch)').attr('fill-opacity', 0.12).attr('stroke', 'var(--launch)').attr('stroke-opacity', 0.45).attr('stroke-width', 1);
      // zoom strip: the launch steps on their own scale
      let za = 0;
      for (const d of s.filter((q) => q.phase === 'launch')) {
        const x0 = zx(za), w = zx(za + d.seconds) - x0;
        svg.append('rect').attr('x', x0 + 0.5).attr('y', zy).attr('width', Math.max(1, w - 1)).attr('height', zoomH).attr('rx', 2).attr('fill', zoomColor(d.order))
          .attr('data-source', 'cycle_steps').attr('data-where', `robot=${robot}&step=${encodeURIComponent(d.step)}`).attr('data-field', 'seconds')
          .on('pointermove', (ev) => tip.show(ev, [`${bz.TEAM[robot].name} · ${d.step}`, `${d3.format('.2f')(d.seconds)} s median`, `${d.samples} volleys`])).on('pointerleave', () => tip.hide());
        label(x0, w, zy, zoomH, d3.format('.1f')(d.seconds), true);
        za += d.seconds;
      }
      svg.append('text').attr('x', left - 8).attr('y', zy + zoomH / 2).attr('dy', '0.32em').attr('text-anchor', 'end').style('fill', 'var(--launch)').style('font-weight', '700').text('LAUNCH');
      svg.append('text').attr('class', 'val').attr('x', zx(za) + 6).attr('y', zy + zoomH / 2).attr('dy', '0.32em').text(`${d3.format('.1f')(za)} s`);
    });
  });


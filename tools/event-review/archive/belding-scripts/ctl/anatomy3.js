  dash.draw('anatomy', (el) => {
    if (!bz.ready(el, ['cycle_groups', 'cycle_group_summary'])) return;
    const data = dash.data('cycle_groups').data, summ = dash.data('cycle_group_summary').data;
    const W = el.clientWidth || 600;
    const left = 150, right = 64, funnel = 18, zoomH = 18, gapG = 16, gapR = 26;
    const robots = ['20245', '19429'], groups = ['Filled up', 'Shot before full'];
    const steps = (rb, g) => data.filter((d) => d.robot === rb && d.group === g && d.seconds != null).sort((a, b) => a.order - b.order);
    const tot = (rb, g, ph) => d3.sum(steps(rb, g).filter((d) => !ph || d.phase === ph), (d) => d.seconds);
    const maxN = d3.max(summ, (s) => s.cycles) || 1;
    const barH = (n) => Math.round(12 + 22 * n / maxN);
    const x = d3.scaleLinear([0, Math.ceil(d3.max(robots.flatMap((r) => groups.map((g) => tot(r, g)))) / 5) * 5], [left, W - right]);
    const zx = d3.scaleLinear([0, Math.ceil(d3.max(robots.flatMap((r) => groups.map((g) => tot(r, g, 'launch')))) * 2) / 2], [left, W - right]);
    let H = 4;
    for (const r of robots) { H += 20; for (const g of groups) { const s = summ.find((q) => q.robot === r && q.group === g); H += barH(s ? s.cycles : 0) + funnel + zoomH + gapG; } H += gapR; }
    const svg = d3.create('svg').attr('width', '100%').attr('viewBox', `0 0 ${W} ${H}`).attr('height', H);
    el.replaceChildren(svg.node()); svg.style('display', 'block');
    const tip = bz.tip(el);
    const colColor = (d) => d.step === 'Keep collecting, drive back' ? 'var(--c4)' : `var(--c${d.order})`;
    const label = (x0, w, y, h, text, dark) => {
      const t = svg.append('text').attr('x', x0 + w / 2).attr('y', y + h / 2).attr('dy', '0.32em').attr('text-anchor', 'middle')
        .attr('pointer-events', 'none').style('fill', dark ? '#0A0C12' : '#EEF1F8').style('font-weight', '700').text(text);
      if (t.node().getComputedTextLength() > w - 6 || h < 15) t.remove();
    };
    let y = 4;
    for (const robot of robots) {
      svg.append('text').attr('class', 'panel').attr('x', 0).attr('y', y + 10).style('fill', bz.tc(robot)).text(bz.tn(robot));
      y += 20;
      for (const g of groups) {
        const s = summ.find((q) => q.robot === robot && q.group === g); const st = steps(robot, g);
        const h = barH(s ? s.cycles : 0);
        svg.append('text').attr('x', 0).attr('y', y + h / 2 - 6).attr('dy', '0.32em').style('fill', 'var(--fg)').style('font-weight', '700').text(g);
        svg.append('text').attr('x', 0).attr('y', y + h / 2 + 8).attr('dy', '0.32em')
          .attr('data-source', 'cycle_group_summary').attr('data-where', `robot=${robot}&group=${encodeURIComponent(g)}`).attr('data-field', 'cycles')
          .text(s ? `${s.cycles} cycles · ${d3.format('.0%')(s.share)}` : '');
        let acc = 0;
        for (const d of st.filter((q) => q.phase === 'collect')) {
          const x0 = x(acc), w = x(acc + d.seconds) - x0;
          svg.append('rect').attr('x', x0 + 0.5).attr('y', y).attr('width', Math.max(1, w - 1)).attr('height', h).attr('rx', 2).attr('fill', colColor(d))
            .attr('data-source', 'cycle_groups').attr('data-where', `robot=${robot}&group=${encodeURIComponent(g)}&step=${encodeURIComponent(d.step)}`).attr('data-field', 'seconds')
            .on('pointermove', (ev) => tip.show(ev, [`${bz.TEAM[robot].name} · ${g} · ${d.step}`, `${d3.format('.1f')(d.seconds)} s median`, `${d.samples} cycles`])).on('pointerleave', () => tip.hide());
          label(x0, w, y, h, d3.format('.1f')(d.seconds), d.order === 3);
          acc += d.seconds;
        }
        const lt = tot(robot, g, 'launch'), lx0 = x(acc), lw = x(acc + lt) - lx0;
        svg.append('rect').attr('x', lx0 + 0.5).attr('y', y).attr('width', Math.max(2, lw - 1)).attr('height', h).attr('rx', 2).attr('fill', 'var(--launch)')
          .on('pointermove', (ev) => tip.show(ev, [`${bz.TEAM[robot].name} · ${g} · Launch`, `${d3.format('.1f')(lt)} s, broken down below`])).on('pointerleave', () => tip.hide());
        svg.append('text').attr('class', 'val').attr('x', x(acc + lt) + 6).attr('y', y + h / 2).attr('dy', '0.32em').text(`${d3.format('.1f')(acc + lt)} s`);
        const zy = y + h + funnel;
        svg.append('path').attr('d', `M${lx0},${y + h} L${lx0 + lw},${y + h} L${zx(lt)},${zy} L${left},${zy} Z`)
          .attr('fill', 'var(--launch)').attr('fill-opacity', 0.1).attr('stroke', 'var(--launch)').attr('stroke-opacity', 0.4);
        let za = 0;
        for (const d of st.filter((q) => q.phase === 'launch')) {
          if (!(d.seconds > 0)) continue;
          const x0 = zx(za), w = zx(za + d.seconds) - x0;
          svg.append('rect').attr('x', x0 + 0.5).attr('y', zy).attr('width', Math.max(1, w - 1)).attr('height', zoomH).attr('rx', 2).attr('fill', `var(--z${d.order - 3})`)
            .attr('data-source', 'cycle_groups').attr('data-where', `robot=${robot}&group=${encodeURIComponent(g)}&step=${encodeURIComponent(d.step)}`).attr('data-field', 'seconds')
            .on('pointermove', (ev) => tip.show(ev, [`${bz.TEAM[robot].name} · ${g} · ${d.step}`, `${d3.format('.2f')(d.seconds)} s median`, `${d.samples} volleys`])).on('pointerleave', () => tip.hide());
          label(x0, w, zy, zoomH, d3.format('.1f')(d.seconds), true);
          za += d.seconds;
        }
        svg.append('text').attr('x', left - 8).attr('y', zy + zoomH / 2).attr('dy', '0.32em').attr('text-anchor', 'end').style('fill', 'var(--launch)').style('font-weight', '700').style('font-size', '10.5px').text('LAUNCH');
        svg.append('text').attr('class', 'val').attr('x', zx(za) + 6).attr('y', zy + zoomH / 2).attr('dy', '0.32em').text(`${d3.format('.1f')(za)} s`);
        y = zy + zoomH + gapG;
      }
      y += gapR;
    }
  });


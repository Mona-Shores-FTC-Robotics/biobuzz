  dash.draw('ballsChart', (el) => {
    if (!bz.ready(el, ['pieces'])) return;
    const rows = dash.data('pieces').data;
    const up = [['auto_pollen', 'var(--pollen)', 1, 'Auto POLLEN'], ['teleop_pollen', 'var(--pollen)', 0.45, 'TeleOp POLLEN'], ['nectar_shot', 'var(--nectar)', 1, 'NECTAR launched'], ['unidentified', 'var(--unid)', 1, 'Not identified']];
    const W = el.clientWidth || 400;
    const ph = 130, dn = 34, gap = 26, top = 18, bottom = 74, left = 26;
    const H = 2 * (top + ph + dn + bottom) + gap;
    const svg = d3.create('svg').attr('width', '100%').attr('viewBox', `0 0 ${W} ${H}`).attr('height', H);
    el.replaceChildren(svg.node()); svg.style('display', 'block');
    const tip = bz.tip(el);
    const upMax = d3.max(rows, (r) => d3.sum(up, ([k]) => r[k] || 0)) || 1;
    const dnMax = Math.max(2, d3.max(rows, (r) => r.nectar_rejected || 0));
    ['20245', '19429'].forEach((robot, pi) => {
      const rs = rows.filter((r) => r.robot === robot).sort((a, b) => a.order - b.order);
      const oy = pi * (top + ph + dn + bottom + gap);
      const x = d3.scaleBand(rs.map((r) => r.match), [left, W - 2]).padding(0.28);
      const y0 = oy + top + ph;
      const y = d3.scaleLinear([0, upMax], [y0, oy + top + 6]);
      const yd = d3.scaleLinear([0, dnMax], [y0, y0 + dn - 4]);
      svg.append('text').attr('class', 'panel').attr('x', 0).attr('y', oy + 11).style('fill', bz.tc(robot)).text(bz.tn(robot));
      for (const v of y.ticks(3)) {
        svg.append('line').attr('x1', left).attr('x2', W - 2).attr('y1', y(v)).attr('y2', y(v)).attr('stroke', 'var(--grid)');
        svg.append('text').attr('x', left - 6).attr('y', y(v)).attr('dy', '0.32em').attr('text-anchor', 'end').text(v);
      }
      svg.append('line').attr('x1', left).attr('x2', W - 2).attr('y1', y0).attr('y2', y0).attr('stroke', 'var(--muted)');
      svg.append('text').attr('x', left - 6).attr('y', y0 + dn / 2).attr('dy', '0.32em').attr('text-anchor', 'end').style('font-size', '10px').text('out');
      for (const r of rs) {
        const bw = Math.min(28, x.bandwidth()), bx = x(r.match) + (x.bandwidth() - bw) / 2;
        const where = `robot=${robot}&match=${encodeURIComponent(r.match)}`;
        let acc = 0;
        for (const [k, col, op] of up) {
          if (!r[k]) continue;
          svg.append('rect').attr('x', bx).attr('y', y(acc + r[k])).attr('width', bw).attr('height', y(acc) - y(acc + r[k]))
            .attr('fill', col).attr('fill-opacity', op).attr('data-source', 'pieces').attr('data-where', where).attr('data-field', k);
          acc += r[k];
        }
        if (r.nectar_rejected) {
          svg.append('rect').attr('x', bx + 1).attr('y', y0 + 1).attr('width', bw - 2).attr('height', yd(r.nectar_rejected) - y0 - 1)
            .attr('fill', 'var(--nectar)').attr('fill-opacity', 0.15).attr('stroke', 'var(--nectar)').attr('stroke-width', 1.5).attr('stroke-dasharray', '3 2')
            .attr('data-source', 'pieces').attr('data-where', where).attr('data-field', 'nectar_rejected');
        }
        const cx = x(r.match) + x.bandwidth() / 2;
        svg.append('circle').attr('cx', cx).attr('cy', y0 + dn + 6).attr('r', 4).attr('fill', r.alliance === 'Red' ? 'var(--al-red)' : 'var(--al-blue)')
          .attr('data-source', 'pieces').attr('data-where', where).attr('data-field', 'alliance');
        const ly = y0 + dn + 16;
        svg.append('text').attr('x', cx).attr('y', ly).attr('text-anchor', 'end').attr('dy', '0.32em').attr('transform', `rotate(-35 ${cx} ${ly})`).text(r.match);
        svg.append('rect').attr('x', x(r.match)).attr('y', oy + top).attr('width', x.bandwidth()).attr('height', ph + dn).attr('fill', 'transparent')
          .on('pointermove', (ev) => tip.show(ev, [`${bz.TEAM[robot].name} · ${r.match} · ${r.alliance} alliance`, ...up.filter(([k]) => r[k]).map(([k, , , n]) => `${n}: ${r[k]}`), ...(r.nectar_rejected ? [`NECTAR rejected: ${r.nectar_rejected}`] : [])]))
          .on('pointerleave', () => tip.hide());
      }
    });
  });


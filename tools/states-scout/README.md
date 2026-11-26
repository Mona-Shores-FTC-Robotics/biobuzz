# States partner catalog

Every team at the Michigan FiM Championship NW (`USMINWCMP`, Midland, 10–12 Dec 2026), what its
AUTO scored this season, the partner archetype it maps to, and what we run beside it: our Auto, its
INIT settings and the queue questions. The drive coach looks a partner up on the phone page
(**States Partner Lookup**, https://claude.ai/artifact/MsF59dauheaGYvYVbNCDUm).

```
python3 tools/states-scout/fetch.py --refetch   # FTCScout REST, cached in cache/ (git-ignored)
python3 tools/states-scout/analyze.py           # out/analysis.json
python3 tools/states-scout/build.py             # TeamCode/autos/states-partners.json + out/states-partners.html
```

Then republish `out/states-partners.html` to the same artifact. Stdlib Python only.

- **What FTCScout gives.** LEAVE and AUTO PARK per robot; AUTO TIPs per alliance only. A team's own TIP
  rate (`tipRate`) is a ridge least-squares share of its alliances' AUTO TIPs over every match at every
  event any States team played (our two robots come out at about 0.8, which matches our logs). No start
  side, no times.
- **Archetypes** (`ARCHETYPES` in `build.py`, named as the partner reality-check work names them):
  RIGHT-SHOOT-PARK, LEFT-SHOOT-PARK, LEAVE-PARK, STILL, CROSSER. Each team also gets `shoots` (hits:
  own TIP rate 0.6 or more; misses: it tips, less often; none) and `lastShotS` where video gives it. Without video a team is *guessed* from its scores: LEAVE
  under half its last event's matches is still; a TIP share under 0.25 is LEAVE / park; a share over 1 with
  2+ TIPs in a third of its matches is a crosser; any other team that tips is a right-start shooter, since
  only the RIGHT CELL is up at the start. The queue's side question confirms it.
- **`overrides.json`** holds what we saw on video (the `scouting` branch's `scouting/2026/partners.json`):
  archetype, side, last-preload time and a one-line note. It always wins over the guess.
- **Recommendations** come from the latest sim-results runs of L-NoCross and R-NoCross beside each
  partner stand-in, cited in each archetype's `sim` line.

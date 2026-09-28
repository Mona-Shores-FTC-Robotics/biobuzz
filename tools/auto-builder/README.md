# Auto Builder: Visualizer patches (temporary home)

The Auto Builder editor is a fork of the Pedro Pathing Visualizer
(<https://github.com/Pedro-Pathing/Visualizer>, Apache-2.0). Until that fork has its own
repository, its commits live here as patch files so they are not lost. **Delete this folder once
the fork exists**; nothing in TeamCode uses it.

To rebuild the editor:

```
git clone https://github.com/Pedro-Pathing/Visualizer.git
cd Visualizer
git checkout -b auto-mode d0d538e      # the upstream commit the patches are based on
git am /path/to/biobuzz/tools/auto-builder/visualizer-patches/*.patch
npm install
npx vite            # or: npx vite build, then serve dist/
```

- Patches 0001-0003 fix the stock Visualizer's preview: one motion profile per exported path, so
  it no longer stops at every sub-path; the robot is placed by distance travelled; and "through"
  curves are drawn the way Pedro 3 follows them. They are candidates to offer upstream on their own.
- Patches 0004-0011 add Auto mode: the `auto` section of `.pp` files (documented in the fork's
  `docs/auto-format.md`), the card list and editor, the preview, and "Export Auto (Java)", which
  writes a class for `TeamCode/.../autokit`.
- `npm test` runs the editor's tests. `node scripts/export-auto.mjs file.pp [outDir]` exports from
  the command line.

`TeamCode/src/test/.../opmodes/auto/generated/HiveRushAuto.java` is that export of
`TeamCode/src/test/resources/auto-builder/hive-rush.pp`, and `GeneratedAutoTest` runs it through
autokit, so the editor and the robot library cannot drift apart unnoticed.

// Builds biobuzz-sample.pp with the Pedro Visualizer's own code, and records the Visualizer's own
// curve points for it in biobuzz-sample.visualizer-points.json.
//
// Why: the logger's desk checks should run on a file in the Visualizer's current format (1.5.0), on
// the BIOBUZZ field, exercising every feature a real Auto can use, and the Java side should be able
// to check that Pedro 3 draws the same curves the Visualizer does. Writing the file by hand would
// only test our reading of the format; serialising and reloading it with the Visualizer's code
// tests the format itself.
//
// The positions are illustrative, not strategy. Replace or add real Autos by saving them from the
// Visualizer into this folder.
//
// To regenerate (needs a checkout of github.com/Pedro-Pathing/Visualizer at VIS):
//   npx esbuild make-sample.ts --bundle --platform=node --outfile=/tmp/make-sample.js \
//       --alias:@vis=$VIS/src && node /tmp/make-sample.js .
import * as fs from "fs";
import * as path from "path";
import { serializeProject, PROJECT_VERSION } from "@vis/utils/project";
import { normalizePaths, normalizeStartPose, deriveSequence } from "@vis/utils/normalize";
import { flattenToAtomicSegments, getPointAndTangentAtProgress } from "@vis/utils/pathTraversal";
import { DEFAULT_SETTINGS, FIELD_SIZE } from "@vis/config/defaults";
import type { Path, SequenceItem } from "@vis/types";

const common = { color: "#ffc516", locked: false, waitBeforeMs: 0, waitAfterMs: 0, waitBeforeName: "", waitAfterName: "" };

const lines: Path[] = [
  {
    ...common, kind: "atomic", id: "leave-start", name: "Leave start",
    endPoint: { x: 56, y: 36 }, controlPoints: [],
    heading: { type: "linear", startDeg: 90, endDeg: 135 },
  },
  {
    ...common, kind: "atomic", id: "arc-to-score", name: "Arc to score",
    endPoint: { x: 40, y: 70 }, controlPoints: [{ x: 30, y: 44 }],
    heading: { type: "tangential", reverse: false },
  },
  {
    ...common, kind: "atomic", id: "face-the-hive", name: "Face the HIVE",
    endPoint: { x: 36, y: 100 }, controlPoints: [],
    heading: {
      type: "piecewise",
      piecewiseHeading: {
        segments: [
          { startProgress: 0, endProgress: 0.6, interpolationType: "linear", parameters: { startDeg: 90, endDeg: 120 } },
          { startProgress: 0.6, endProgress: 1, interpolationType: "facing-point", parameters: { point: { x: 70.75, y: 70.75 } } },
        ],
      },
    },
  },
  {
    ...common, kind: "compound", id: "sweep", name: "Sweep",
    heading: { type: "linear", startDeg: 120, endDeg: 180 },
    segments: [
      { ...common, kind: "atomic", id: "sweep-a", name: "Sweep A", endPoint: { x: 20, y: 120 }, controlPoints: [], heading: { type: "constant", degrees: 180 } },
      { ...common, kind: "atomic", id: "sweep-b", name: "Sweep B", endPoint: { x: 20, y: 132 }, controlPoints: [], heading: { type: "constant", degrees: 180 } },
    ],
  },
  {
    ...common, kind: "atomic", id: "through-pickups", name: "Through pickups",
    endPoint: { x: 60, y: 120 }, controlPoints: [], throughPoints: [{ x: 34, y: 124 }, { x: 48, y: 112 }],
    heading: { type: "constant", degrees: 90 },
  },
  {
    ...common, kind: "atomic", id: "return", name: "Return", waitAfterMs: 500, waitAfterName: "Settle",
    endPoint: { x: 40, y: 70 }, controlPoints: [{ x: 60, y: 90 }],
    heading: { type: "tangential", reverse: true },
  },
];

const sequence: SequenceItem[] = [
  { kind: "path", lineId: "leave-start" },
  { kind: "path", lineId: "arc-to-score" },
  { kind: "wait", id: "score-wait", name: "Score", durationMs: 750 },
  { kind: "path", lineId: "face-the-hive" },
  { kind: "path", lineId: "sweep-a" },
  { kind: "path", lineId: "sweep-b" },
  { kind: "path", lineId: "through-pickups" },
  { kind: "path", lineId: "return" },
];

const startPoint = { x: 56, y: 8, headingDeg: 90, name: "Start" };
const outDir = process.argv[2] ?? ".";
const json = serializeProject(
  { startPoint, lines, shapes: [], sequence, settings: { ...DEFAULT_SETTINGS, fieldMap: "biobuzz.webp" } },
  { pretty: true, overrides: { timestamp: "2026-09-27T00:00:00.000Z" } },
);
fs.writeFileSync(path.join(outDir, "biobuzz-sample.pp"), json + "\n");

// Load it back exactly as the Visualizer's File → Open does, and record what it draws.
const data = JSON.parse(json);
const loaded = normalizePaths(data.lines);
const start = normalizeStartPose(data.startPoint);
const seq = deriveSequence(data, loaded);
const segments = flattenToAtomicSegments(start, loaded).map((s) => ({
  id: s.line.id,
  hasThroughPoints: !!s.line.throughPoints?.length,
  arcLength: s.arcLength,
  samples: [0, 0.25, 0.5, 0.75, 1].map((t) => {
    const p = getPointAndTangentAtProgress(s.points, t).point;
    return { t, x: p.x, y: p.y };
  }),
}));
fs.writeFileSync(
  path.join(outDir, "biobuzz-sample.visualizer-points.json"),
  JSON.stringify({ visualizerVersion: PROJECT_VERSION, fieldSize: FIELD_SIZE, sequenceLength: seq.length, segments }, null, 2) + "\n",
);
console.log(`wrote biobuzz-sample.pp (format ${PROJECT_VERSION}, field ${FIELD_SIZE} in): ${loaded.length} top-level paths, ${segments.length} segments, ${seq.length} sequence steps`);

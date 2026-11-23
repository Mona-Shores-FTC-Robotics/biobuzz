#!/usr/bin/env python3
"""Tests for read_logs.py's Driver Hub battery reader and turret burst report, on made-up files in the formats it reads.

  python3 tools/pull-logs/test_read_logs.py

The formats are what Pull-MatchLogs.ps1 writes: a logcat "threadtime" Driver Station log, and
"dumpsys battery" + "dumpsys batterystats --history" snapshots. Neither has been checked against
a real Driver Hub yet (doc/robot-todo.md); when one has, replace these samples with its lines.
"""
import contextlib
import io
import os
import struct
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import read_logs  # noqa: E402
from read_logs import turret_bursts  # noqa: E402

DS_LOG = """--------- beginning of main
11-08 09:00:01.000  2101  2101 I RobotCore: ******************** START ********************
11-08 14:31:50.250  2101  2190 I BatteryChecker: percent remaining: 29 is charging: false
11-08 14:33:05.500  2101  2190 I BatteryChecker: { 1234 10.123} percent remaining: 28 is charging: false
11-08 14:40:00.000  2101  2190 I BatteryChecker: percent remaining: 28 is charging: true
"""

BATTERY = """=== 2026-11-08 14:45:00 Driver Hub ABC123: dumpsys battery; dumpsys batterystats --history ===
Current Battery Service state:
  AC powered: false
  USB powered: true
  Wireless powered: false
  status: 2
  level: 30
  scale: 100
Battery History (1% used, 3264 used of 256KB, 45 strings using 2690):
                    0 (15) RESET:TIME: 2026-11-08-09-00-00
                    0 (2) 100 status=discharging health=good plug=none temp=250 volt=4300
           +1h00m00s000ms (2) 099 +wake_lock
       +5h31m00s000ms (2) 031
       +5h33m30s000ms (2) 030 status=charging plug=usb
=== 2026-11-08 15:00:00 Driver Hub ABC123: dumpsys battery; dumpsys batterystats --history ===
  level: 32
Battery History (1% used):
                    0 (15) RESET:TIME: 2026-11-08-09-00-00
                    0 (2) 100 status=discharging plug=none
       +5h31m00s000ms (2) 031
                    0 (2) START
       +5h50m00s000ms (2) 031
"""


def wpilog(path, series, metadata):
    """A minimal .wpilog: a start record per key, then (seconds, double) samples."""
    out = bytearray(b'WPILOG' + struct.pack('<HI', 0x0100, 0))

    def rec(eid, ts_us, payload):
        out.extend(bytes([3 | (3 << 2) | (7 << 4)]) + struct.pack('<IIQ', eid, len(payload), ts_us) + payload)

    def s(x):
        b = x.encode()
        return struct.pack('<I', len(b)) + b

    eid = 1
    for key, value in metadata.items():
        rec(0, 0, b'\x00' + struct.pack('<I', eid) + s('/RealMetadata/' + key) + s('string') + s(''))
        rec(eid, 0, value.encode())
        eid += 1
    for key, samples in series.items():
        rec(0, 0, b'\x00' + struct.pack('<I', eid) + s(key) + s('double') + s(''))
        for t, v in samples:
            rec(eid, int(t * 1e6), struct.pack('<d', v))
        eid += 1
    with open(path, 'wb') as f:
        f.write(out)


class DriverHubBattery(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.dir = self.tmp.name
        self.ds = os.path.join(self.dir, '2026-11-08_DriverHub-ABC123.dslog.txt')
        self.bat = os.path.join(self.dir, '2026-11-08_DriverHub-ABC123.battery.txt')
        for path, text in ((self.ds, DS_LOG), (self.bat, BATTERY)):
            with open(path, 'w') as f:
                f.write(text)

    def tearDown(self):
        self.tmp.cleanup()

    def test_ds_log_lines_with_and_without_the_time_offset_prefix(self):
        got = read_logs.ds_log_samples(self.ds)
        self.assertEqual([(t.strftime('%Y-%m-%d %H:%M:%S'), p, c) for t, p, c in got], [
            ('2026-11-08 14:31:50', 29, False), ('2026-11-08 14:33:05', 28, False),
            ('2026-11-08 14:40:00', 28, True)])

    def test_history_offsets_from_the_reset_clock_and_plug_state(self):
        got = read_logs.battery_history_samples(self.bat)
        rows = [(t.strftime('%H:%M:%S'), p, c) for t, p, c in got]
        self.assertIn(('09:00:00', 100, False), rows)
        self.assertIn(('10:00:00', 99, False), rows)
        self.assertIn(('14:31:00', 31, False), rows)
        self.assertIn(('14:33:30', 30, True), rows)
        self.assertIn(('14:45:00', 30, True), rows)   # dumpsys battery at the pull, USB powered
        self.assertIn(('15:00:00', 32, False), rows)
        # After START (a reboot) there is no clock until the next TIME: that record is skipped.
        self.assertNotIn(('14:50:00', 31, False), rows)

    def test_duration(self):
        self.assertEqual(read_logs._duration_s('+1h02m03s456ms'), 3723.456)
        self.assertEqual(read_logs._duration_s('+5m'), 300)
        self.assertEqual(read_logs._duration_s('+1d00h00m00s001ms'), 86400.001)
        self.assertEqual(read_logs._duration_s('0'), 0)

    def test_matched_to_a_match_log_by_time(self):
        log = os.path.join(self.dir, '2026-11-08_14-32-10_R-Solo.wpilog')
        wpilog(log, {'/Robot/BatteryVolts': [(0.0, 12.9), (60.0, 11.8), (150.0, 12.1)]},
               {'MatchId': '2026-11-08_14-32-10', 'BatteryAtInit': '12.90 V'})
        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            read_logs.dsbattery([self.bat, self.ds, log])
        row = [l for l in out.getvalue().splitlines() if l.startswith('2026-11-08_14-32-10_R-Solo')]
        self.assertEqual(len(row), 1, out.getvalue())
        # Start 14:32:10: last sample before it is 14:31:50 (29, DS log; the history's 31 at
        # 14:31:00 is older). During 14:32:10-14:34:40: 28 at 14:33:05 and 30 (history) at 14:33:30.
        self.assertIn('29 / 28 / 30', row[0])
        self.assertIn('14:34:40', row[0])
        self.assertIn('12.90 V / 11.80 V', row[0])


# ------------------------------------------------------------------ turret burst bench

P = '/TurretBurst/'

class TurretReportTest(unittest.TestCase):
    def data(self):
        return {P+'Power': [(0, 0), (1, 1), (1.125, 0), (1.4, 0), (2, 1), (2.125, 0), (2.4, 0)],
                P+'AngleDeg': [(0, 2.1), (1, 1.98), (1.125, 1.9), (1.4, 1.85),
                               (2, 1.85), (2.125, 1.8), (2.4, 1.73)],
                P+'ZeroCounts': [(0, -63)], P+'ManualSingleBurst': [(0, 1)],
                P+'Holding': [(0, 0)], P+'ManualBurstMs': [(0, 125)],
                P+'PulseUs': [(0, 0), (1, 2500), (1.125, 0), (2, 2500), (2.125, 0)],
                P+'PwmEnabledSdk': [(0, 0), (1, 1), (1.125, 0), (2, 1), (2.125, 0)],
                P+'LastOnMs': [(0, 0), (1.125, 126.4), (2.125, 125.8)],
                P+'R2': [(0, 1)], P+'R1': [(0, 1)]}

    def test_absolute_readings_are_not_mistaken_for_movement(self):
        rows = turret_bursts(self.data())
        self.assertEqual(2, len(rows))
        self.assertAlmostEqual(-.08, rows[0]['drive_delta_deg'])
        self.assertAlmostEqual(-.13, rows[0]['settled_delta_deg'])
        self.assertEqual(126.4, rows[0]['on_ms'])
        self.assertEqual(125, rows[0]['configured_ms'])
        self.assertEqual(2500, rows[0]['pulse_us'])
        self.assertEqual(1, rows[0]['pwm_enabled'])

    def test_release_and_live_timing_change_are_visible(self):
        d = self.data()
        d[P+'R2'] += [(2.125, 0)]
        d[P+'ManualBurstMs'] += [(1.5, 500)]
        rows = turret_bursts(d)
        self.assertEqual(125, rows[0]['configured_ms'])
        self.assertEqual(500, rows[1]['configured_ms'])
        self.assertEqual('released', rows[1]['reason'])

    def test_no_settled_claim_if_next_burst_or_eof_cuts_the_wait(self):
        d = self.data()
        d[P+'Power'] = [(0, 0), (1, 1), (1.125, 0), (1.2, 1)]
        rows = turret_bursts(d)
        self.assertFalse(rows[0]['settled'])
        self.assertFalse(rows[1]['settled'])
        self.assertEqual('incomplete', rows[1]['reason'])

    def test_zero_change_invalidates_delta(self):
        import math
        d = self.data()
        d[P+'ZeroCounts'] += [(1.1, -80)]
        rows = turret_bursts(d)
        self.assertTrue(rows[0]['zero_changed'])
        self.assertTrue(math.isnan(rows[0]['drive_delta_deg']))
        self.assertFalse(rows[0]['settled'])


class WheelDips(unittest.TestCase):
    """read_logs.wheel_dips: one ball is one sharp dip of the launcher wheels."""

    @staticmethod
    def trace(dips_at, target=1500.0, step=0.02, length=4.0, depth=0.05):
        out = []
        for k in range(int(length / step)):
            t = k * step
            v = target * 1.005
            for d in dips_at:
                if 0 <= t - d < 0.10:
                    v = target * (1 - depth * (1 - (t - d) / 0.10))
            out.append((t, v))
        return out

    def test_counts_each_ball_once(self):
        balls = read_logs.wheel_dips(self.trace([1.5, 1.95, 2.4, 2.85]), 1500.0)
        self.assertEqual(4, len(balls))
        self.assertAlmostEqual(1.5, balls[0], places=1)

    def test_a_steady_wheel_fired_nothing(self):
        self.assertEqual([], read_logs.wheel_dips(self.trace([]), 1500.0))

    def test_a_shallow_ripple_is_not_a_ball(self):
        self.assertEqual([], read_logs.wheel_dips(self.trace([1.5, 2.2], depth=0.012), 1500.0))

    def test_a_dip_with_no_steady_level_before_it_is_not_counted(self):
        # The wheels arriving at a new target: low from the first sample.
        t = self.trace([0.0, 1.5])
        self.assertEqual(1, len(read_logs.wheel_dips(t, 1500.0)))

class CellSettles(unittest.TestCase):
    """read_logs.cell_settles: how long before a CELL settles its tag row was seen, and moving."""

    def test_a_rising_row_is_timed_from_where_it_left_its_rest(self):
        # DOWN at 35 in until 2.0 s, rising to 50 by 3.0 s, settled UP at 3.3 s.
        heights = [(t / 10, 35.0 if t <= 20 else min(50.0, 35.0 + (t - 20) * 1.5)) for t in range(0, 34)]
        states = [(0.0, 1), (2.2, 2), (3.3, 0)]
        settles = read_logs.cell_settles(heights, states)
        self.assertEqual(1, len(settles))
        t, a, b, seen, moved = settles[0]
        self.assertEqual((3.3, 'DOWN', 'UP'), (t, a, b))
        self.assertAlmostEqual(0.3, seen, places=6)
        self.assertAlmostEqual(2.1, moved, places=6)

    def test_a_row_first_seen_as_it_swings_has_no_moved_time(self):
        # A lowered CELL's tags face away: the row comes into view at 2.5 s, already rising.
        heights = [(t / 10, 40.0 + (t - 25)) for t in range(25, 34)]
        states = [(0.0, 1), (2.5, 2), (3.3, 0)]
        t, a, b, seen, moved = read_logs.cell_settles(heights, states)[0]
        self.assertAlmostEqual(2.5, seen, places=6)
        self.assertIsNone(moved)

    def test_the_same_state_again_is_not_a_settle(self):
        states = [(0.0, 0), (1.0, 2), (2.0, 0)]
        self.assertEqual([], read_logs.cell_settles([(0.5, 50.0)], states))


class CellRises(unittest.TestCase):
    """read_logs.cell_rises: a DOWN CELL's tags face away, so a rising one comes into view mid-swing."""

    def test_a_row_coming_into_view_rising_is_timed_to_its_up(self):
        # Unseen until 2.0 s, then 40 in and climbing 1 in a frame; reads UP at 2.9 s.
        heights = [(2.0 + i / 10, min(50.0, 40.0 + i)) for i in range(0, 15)]
        states = [(0.0, 1), (2.0, 2), (2.9, 0)]
        rises = read_logs.cell_rises(heights, states)
        self.assertEqual(1, len(rises))
        t, h, up = rises[0]
        self.assertEqual((2.0, 40.0, 2.9), (t, h, up))

    def test_a_row_seen_at_mid_height_but_not_rising_is_not_a_rise(self):
        # An odd frame or a row seen low from a steep angle: in the band, but it stays there.
        heights = [(2.0 + i / 10, 41.0) for i in range(0, 15)]
        states = [(0.0, 1), (3.0, 0)]
        self.assertEqual([], read_logs.cell_rises(heights, states))

    def test_a_row_already_in_view_is_not_a_new_appearance(self):
        heights = [(i / 10, 50.0) for i in range(0, 20)] + [(2.0 + i / 10, 40.0 + i) for i in range(0, 11)]
        states = [(0.0, 0)]
        self.assertEqual([], read_logs.cell_rises(heights, states))


class CellCreeps(unittest.TestCase):
    """read_logs.cell_creeps: an UP CELL's row sliding down before it falls, with the robot still."""

    @staticmethod
    def _fall(creep_from_s=8.0):
        # UP at 50 in, 20 frames a second; from creep_from_s down 0.5 in a second, then from 10 s a
        # fast fall of 2 in a frame. The tracker leaves UP below 47.5 in.
        heights, t = [], 0.0
        while t < 10.6:
            h = 50.0 if t < creep_from_s else 50.0 - 0.5 * (t - creep_from_s)
            if t >= 10.0:
                h = heights[-1][1] - 2.0
            heights.append((round(t, 2), h))
            t += 0.05
        left = next(ht for ht, h in heights if h < 47.5)
        return heights, [(0.0, 0), (left, 2)]

    def test_a_slow_creep_shows_before_the_fall(self):
        heights, states = self._fall()
        found = read_logs.cell_creeps(heights, states, lambda t, ref: True)
        self.assertEqual(1, len(found))
        t, rest, leads, left = found[0]
        self.assertAlmostEqual(50.0, rest)
        # 0.5 in below rest at 9.0 s; it crosses 44 in on the fast fall, about 1.1 s later.
        self.assertGreater(leads[0.5], 1.0)
        self.assertLess(leads[2.0], leads[0.5])
        self.assertIsNotNone(left)

    def test_nothing_while_the_robot_moves(self):
        heights, states = self._fall()
        self.assertEqual([], read_logs.cell_creeps(heights, states, lambda t, ref: t < 7.0))

    def test_a_row_back_in_view_already_low_is_not_a_fall(self):
        heights = [(i / 20, 50.0) for i in range(0, 120)] + [(7.0 + i / 20, 39.0) for i in range(0, 10)]
        self.assertEqual([], read_logs.cell_creeps(heights, [(0.0, 0)], lambda t, ref: True))


class Legs(unittest.TestCase):
    """leg_rows: a trip between two spots, timed from the kit's own card events."""

    def log(self):
        t0 = 100.0
        events = [(t0, 'PLAY: RED alliance'), (t0, 'auto: path to RIGHT_AUTO_HIVE'),
                  (t0 + 1.0, 'auto: command LaunchAll'), (t0 + 3.0, 'auto: path BACKWARD to WALL_FLOWER_STAGING'),
                  (t0 + 5.0, 'auto: command PickFlower'), (t0 + 8.0, 'auto: path to RIGHT_PARK')]
        ts = [t0 + i * 0.1 for i in range(300)]
        speed = [(t, 30.0 if t0 + 3.5 <= t <= t0 + 4.5 else 5.0) for t in ts]
        heading = [(t, 90.0 + (min(max(t - t0 - 3.0, 0.0), 2.0) * 45.0)) for t in ts]  # 90 to 180 over the leg
        return {'/Events': events, '/Drive/OdometrySpeedInPerSec': speed, '/Odometry/PedroInches/HeadingDeg': heading,
                '/Drive/OdometryTurnRadPerSec': [(t, 0.5) for t in ts], '/Robot/BatteryVoltsSteady': [(t, 12.0) for t in ts]}

    def test_each_drive_is_a_leg_from_the_last_spot_and_the_park_is_left_out(self):
        rows = read_logs.leg_rows(self.log())
        self.assertEqual([('start', 'RIGHT_AUTO_HIVE'), ('RIGHT_AUTO_HIVE', 'WALL_FLOWER_STAGING')],
                         [(r['came_from'], r['to']) for r in rows])
        back = rows[1]
        self.assertAlmostEqual(2.0, back['s'], places=6)
        self.assertEqual(30.0, back['peak_in_s'])
        self.assertAlmostEqual(90.0, back['turned_deg'], delta=1.0)
        self.assertAlmostEqual(12.0, back['volts'])

    def test_a_log_with_no_auto_has_no_legs(self):
        self.assertEqual([], read_logs.leg_rows({'/Events': [(1.0, 'OpMode init: Basic Drive')]}))


class Cycles(unittest.TestCase):
    """cycle_rows: a TeleOp volley's split, from the gate, the shot count, pieces in and fire presses."""

    GATE = [(0.0, False), (20.0, True), (21.5, False), (40.0, True), (40.3, False), (50.0, True), (52.0, False)]
    SHOTS = [(0.0, 0), (20.8, 1), (21.2, 2), (50.6, 3), (51.0, 4), (51.4, 5), (51.8, 6)]
    STILL = [(t / 10.0, 0.0) for t in range(0, 600)]

    def rows(self, presses, pieces):
        return read_logs.cycle_rows(self.GATE, self.SHOTS, pieces, presses, self.STILL, 0.0, 60.0)

    def test_each_phase_runs_to_the_next_event(self):
        rows, last = self.rows([19.6, 49.5], [5.0, 8.0, 30.0, 33.0, 44.0])
        self.assertEqual(2, len(rows))
        r = rows[0]
        self.assertEqual(2, r['shots'])
        self.assertAlmostEqual(5.0, r['drive to pieces'])
        self.assertAlmostEqual(3.0, r['pick up'])
        self.assertAlmostEqual(11.6, r['drive loaded'])
        self.assertAlmostEqual(0.4, r['spin-up and aim'], places=6)
        self.assertAlmostEqual(1.5, r['feed'])
        self.assertAlmostEqual(52.0, last)

    def test_a_gate_that_fired_nothing_is_an_empty_feed_on_the_next_volley(self):
        rows, _ = self.rows([19.6, 39.9, 49.5], [5.0, 30.0, 44.0])
        self.assertEqual(1, rows[1]['empty_feeds'])
        self.assertEqual(4, rows[1]['shots'])

    def test_a_press_before_the_last_piece_came_in_does_not_start_the_spin_up(self):
        # Circle at an empty lane (39.9: the gate opened, nothing came out), then a pickup, then the real press.
        rows, _ = self.rows([19.6, 39.9, 49.5], [5.0, 44.0, 46.0])
        r = rows[1]
        self.assertEqual(1, r['early_presses'])
        self.assertAlmostEqual(0.5, r['spin-up and aim'], places=6)
        self.assertAlmostEqual(2.0, r['pick up'])

    def test_the_second_volley_matches_the_practice_readouts_test(self):
        # CycleReadoutTest.theReportsFixtureGivesTheReportsSplit runs these events on the robot's
        # readout: the two must give the same numbers.
        rows, _ = self.rows([19.6, 49.5], [5.0, 8.0, 30.0, 33.0, 44.0])
        r = rows[1]
        self.assertAlmostEqual(30.5, r['end'] - r['start'])
        self.assertAlmostEqual(8.5, r['drive to pieces'])
        self.assertAlmostEqual(14.0, r['pick up'])
        self.assertAlmostEqual(5.5, r['drive loaded'])
        self.assertAlmostEqual(0.5, r['spin-up and aim'], places=6)
        self.assertAlmostEqual(2.0, r['feed'])
        self.assertEqual(1, r['empty_feeds'])


class Practice(unittest.TestCase):
    """practice: the readout's own event lines, listed with the cycles' best and median."""

    def test_lists_the_lines_and_the_cycles_best_and_median(self):
        d = {'/Events': [(10.0, 'turret at PLAY: AIM'), (12.4, 'practice: pick-up 2.4 s, 4 in, lane full'),
                         (21.2, 'practice: cycle 1 11.2 s, 4 balls: to pieces 2.0, pick up 2.4'),
                         (30.0, 'practice: cycle 2 8.8 s, 4 balls: to pieces 1.0, pick up 2.0'),
                         (40.0, 'practice: cycle 3 10.0 s, 3 balls: to pieces 1.0, pick up 2.0')]}
        old = read_logs.read
        read_logs.read = lambda path: d
        try:
            out = io.StringIO()
            with contextlib.redirect_stdout(out):
                read_logs.practice(['x.wpilog'])
        finally:
            read_logs.read = old
        text = out.getvalue()
        self.assertIn('   2.4  pick-up 2.4 s, 4 in, lane full', text)
        self.assertIn('3 cycles: best 8.8 s, median 10.0 s', text)


if __name__ == '__main__':
    unittest.main()

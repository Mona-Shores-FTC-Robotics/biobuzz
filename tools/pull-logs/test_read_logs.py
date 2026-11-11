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

if __name__ == '__main__':
    unittest.main()

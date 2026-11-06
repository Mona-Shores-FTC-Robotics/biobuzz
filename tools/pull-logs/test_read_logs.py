"""Regression tests for burst measurements from absolute encoder readings."""
import unittest
from read_logs import turret_bursts

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

if __name__ == '__main__':
    unittest.main()

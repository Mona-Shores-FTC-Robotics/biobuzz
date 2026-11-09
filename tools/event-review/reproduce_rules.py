"""Tolerances and known differences for test_reproduce.py.

TOL: {dataset: {field: absolute tolerance}}, each with its reason. KNOWN: {dataset: why it does
not reproduce}; a known dataset is reported but does not fail the check. Keep both short and
true: when a rule changes, rerun the check and update the reason here and in the README.
"""
TOL = {
    # Mean of 14 TeleOp IN durations is 1.045: the published 1.04 rounded it down, this rounds up.
    'flower_picks': {'push': 0.01},
}

KNOWN = {
    'floor_pickup': ('first_piece_s, to_full_s and collecting_share: issue #508\'s script is not in the '
                     'transcript, so the pipeline uses its written definitions (volley = shots < 1.5 s apart, '
                     'robot counter); per_min, between_s and the piece counts reproduce exactly'),
    'square_issues': ('presses, sticks_cancelled, dropout_matches and centre_refused (what the page draws) '
                      'reproduce; the other fields were read by hand from the events and are not made'),
}

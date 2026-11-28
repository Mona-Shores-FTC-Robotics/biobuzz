# LeftUnderHive NECTAR trial

This branch packages the existing [paired-auto-nectar study candidate](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/tree/ea0aa2c2/doc/studies/paired-auto-nectar/candidate) unchanged. It depends on PR #795's bounded nectar runtime and simulator. It does not change R-LeftUnderHive or the normal route geometry.

Deploy this branch to L and select **L-LeftUnderHive NECTAR TRIAL**. Run beside **R-LeftUnderHive** from this branch. The ordinary L menu slot binds to the pilot on this branch; the original generated L remains available for matched simulation. There is no additional match-menu entry. Switching back to master restores the ordinary L route. A Utility-only wrapper is deliberately avoided: RobotOpMode classifies AUTO behavior from the Autonomous annotation.

The source is `TeamCode/autos/drafts/l-left-under-hive-pilot.pp`; its existing export is `LLeftUnderHiveNectarAuto.java`. Both are byte-for-byte copies of study commit ea0aa2c2. Edit the drawing and export normally for future route changes; do not edit generated Java.

L fires each normal load first, then receives at its existing LEFT and RIGHT HIVE spots. Its receipt deadlines are 10.6 s and 23.1 s after PLAY. Our NECTAR and a fresh near-CELL check permit the bounded extra shot. R follows its original route. The LEFT roll approaches approximately 71 degrees off the intake axis; its real capture reliability is unverified.

## Existing evidence and this packaging

The [source study](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/ea0aa2c2/doc/studies/paired-auto-nectar/README.md) reports 55 to 68 clean triples on seeds 1–120 and 59 to 71 on held-out seeds 201–320, with unchanged clean two-TIP counts, both parks in every run and no robot contacts or crossings. These are simulation results from study simulation commit 2deecf9ffb81270ea51b88c24f3cbeccaa89f0e7; they are not physical validation or a rerun of this packaging commit.

`TeamCode/sim-request.json` retains the study's 120-seed matched nominal request, with an isolated publication folder `left-under-hive-nectar-trial-matched`. Both arms receive identical earned physical rolls. The folder prevents this trial from replacing normal LeftUnderHive's published latest result. The request runs R as the primary and compares ordinary L against the pilot as partner.

The nominal RED release points and rates are the study's: (5.5,95), first roll at 24 in/s toward LEFT_AUTO_HIVE and second at 26 in/s toward RIGHT_AUTO_HIVE, each one second after the corresponding earned TIP. Human consistency, intake approach, sensor receipt, counted extra feed and both parks remain robot checks. Blue mirroring and PARK FIRST require explicit replay before promotion.

This is an unmerged trial package. It has not been compiled locally because the execution workspace is unavailable. CI and the isolated simulation request must pass before claiming this exact branch verified. No other pair is promoted by this package.

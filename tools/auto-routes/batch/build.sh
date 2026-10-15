#!/bin/bash
# build.sh: compile the simulator, the generated Autos and the batch mains with javac (seconds, no Gradle or Android
# SDK). BIOBUZZ_JARS: a folder with pedro-core-3.0.1.jar, ivy-core-1.1.1.jar, ivy-pedro-1.1.1.jar (each library's
# classes.jar, unzipped from its .aar in the Gradle cache), junit-4.13.2.jar and hamcrest-core-1.3.jar.
# BIOBUZZ_CLASSES: where the classes go (default /tmp/biobuzz-batch/classes). Extra .java files (e.g. Autos exported
# to a scratch folder) can be passed as arguments.
set -e
ROOT=$(cd "$(dirname "$0")/../../.." && pwd)
: "${BIOBUZZ_JARS:?set BIOBUZZ_JARS (see the top of this file)}"
OUT=${BIOBUZZ_CLASSES:-/tmp/biobuzz-batch/classes}
mkdir -p "$OUT"
CP=$(ls "$BIOBUZZ_JARS"/*.jar | tr '\n' ':')
T=$ROOT/TeamCode/src/test/java
javac -nowarn -implicit:class -d "$OUT" -cp "$OUT:$CP" -sourcepath "$T:$ROOT/TeamCode/src/main/java:$ROOT/tools/auto-routes/batch/stub" \
    "$T"/org/firstinspires/ftc/teamcode/logging/*.java "$T"/org/firstinspires/ftc/teamcode/opmodes/auto/generated/*.java "$@" \
    2>&1 | grep -v '^Note' || true
echo "classes in $OUT"

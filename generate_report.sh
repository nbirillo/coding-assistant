
TASK_DIR="$1"
TASK_NAME="$2"
SOLUTION_SPACE="$3"
CODE_REPO="$4"
OUTPUT_DIR="$5"

find "$TASK_DIR"/"$TASK_NAME" -name \*.py -exec ./gradlew hintGenerationCli \
      -PtaskName="$TASK_NAME" \
      -PfragmentPath={} \
      -PsolutionSpacePath="$SOLUTION_SPACE" \
      -PcodeRepositoryPath="$CODE_REPO" \
      -PoutputDir="$OUTPUT_DIR"/"$TASK_NAME" \;

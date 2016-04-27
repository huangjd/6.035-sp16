#!/bin/bash
# MUST GENERATE MAIN.JAR THROUGH ECLIPSE->EXPORT BEFORE RUNNING THIS FILE

gitroot=$(git rev-parse --show-toplevel)
CODEGEN=$gitroot/tests/codegen
CODEGEN_HIDDEN=$gitroot/tests/codegen-hidden
DATAFLOW=$gitroot/tests/dataflow
OPTIMIZER=$gitroot/tests/optimizer
test_dirs=($CODEGEN $CODEGEN_HIDDEN $DATAFLOW $OPTIMIZER)

fail=0

runtests() {
  for prefix in "${test_dirs[@]}"
  do
    path="$prefix/input/*.dcf"
    
    for file in $path; do
      echo "Running file ${file}"
      java -jar $gitroot/Main.jar --debug -ea --target=assembly $file
    done
  done
}

runtests &> test_outputs

exit $fail;

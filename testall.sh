#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
$DIR/build.sh;
echo "Running codegen unoptimzed";
$DIR/tests/codegen/test-unoptimized.sh;
echo "Running codegen hidden unoptimzed";
$DIR/tests/codegen-hidden/test-unoptimized.sh;
echo "Running additional test unoptimzed";
$DIR/tests/huangjd-additional/test-unoptimized.sh;


echo "Running codegen optimzed";
$DIR/tests/codegen/test.sh;
echo "Running codegen hidden optimzed";
$DIR/tests/codegen-hidden/test.sh;
echo "Running additional test optimzed";
$DIR/tests/huangjd-additional/test.sh;

echo "Running dataflow optimzed";
$DIR/tests/dataflow/test.sh;
echo "Running optimizer optimzed";
$DIR/tests/optimizer/test.sh;

echo "Running scanner"
$DIR/tests/scanner/test.sh;
$DIR/tests/scanner-hidden/test.sh;

echo "Running parser"
$DIR/tests/parser/test.sh;
$DIR/tests/parser-hidden/test.sh;

echo "Running semantics"
$DIR/tests/semantics/test.sh;
$DIR/tests/semantics-hidden/test.sh;



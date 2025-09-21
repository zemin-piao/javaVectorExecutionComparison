#!/bin/bash

source ~/.sdkman/bin/sdkman-init.sh

echo "=== JIT Assembly Analysis: Java 17 vs Java 25 ==="
echo "Comparing Vector API and Scalar JIT compilation"
echo ""

# Create output directories
mkdir -p assembly_output

echo "Building benchmarks..."

# Java 17 Analysis
echo ""
echo "=== Java 17 Analysis ==="
sdk use java 17.0.12-amzn > /dev/null 2>&1
javac --add-modules jdk.incubator.vector VectorSimple.java
javac ScalarBenchmark.java

echo "Java 17 Vector API Assembly (with warm-up):"
java --add-modules jdk.incubator.vector \
     -XX:+UnlockDiagnosticVMOptions \
     -XX:+PrintAssembly \
     -XX:PrintAssemblyOptions=intel \
     -XX:CompileCommand=print,VectorSimple.sumSalariesVector \
     VectorSimple 10000000 2>&1 | grep -A 50 -B 5 "sumSalariesVector" > assembly_output/java17_vector.asm

echo "Java 17 Scalar Assembly (with warm-up):"
java -XX:+UnlockDiagnosticVMOptions \
     -XX:+PrintAssembly \
     -XX:PrintAssemblyOptions=intel \
     -XX:CompileCommand=print,ScalarBenchmark.sumSalariesScalar \
     ScalarBenchmark 10000000 2>&1 | grep -A 50 -B 5 "sumSalariesScalar" > assembly_output/java17_scalar.asm

# Java 25 Analysis
echo ""
echo "=== Java 25 Analysis ==="
sdk use java 25-amzn > /dev/null 2>&1
javac --add-modules jdk.incubator.vector VectorSimple.java
javac ScalarBenchmark.java

echo "Java 25 Vector API Assembly (with warm-up):"
java --add-modules jdk.incubator.vector \
     -XX:+UnlockDiagnosticVMOptions \
     -XX:+PrintAssembly \
     -XX:PrintAssemblyOptions=intel \
     -XX:CompileCommand=print,VectorSimple.sumSalariesVector \
     VectorSimple 10000000 2>&1 | grep -A 50 -B 5 "sumSalariesVector" > assembly_output/java25_vector.asm

echo "Java 25 Scalar Assembly (with warm-up):"
java -XX:+UnlockDiagnosticVMOptions \
     -XX:+PrintAssembly \
     -XX:PrintAssemblyOptions=intel \
     -XX:CompileCommand=print,ScalarBenchmark.sumSalariesScalar \
     ScalarBenchmark 10000000 2>&1 | grep -A 50 -B 5 "sumSalariesScalar" > assembly_output/java25_scalar.asm

echo ""
echo "Assembly files generated in assembly_output/ directory:"
ls -la assembly_output/

echo ""
echo "=== SIMD Instruction Analysis ==="
echo "Searching for SIMD instructions in Vector API implementations:"

echo ""
echo "Java 17 Vector SIMD instructions:"
grep -i "vmov\|vadd\|vfmadd\|vbroadcast" assembly_output/java17_vector.asm | head -10

echo ""
echo "Java 25 Vector SIMD instructions:"
grep -i "vmov\|vadd\|vfmadd\|vbroadcast" assembly_output/java25_vector.asm | head -10

echo ""
echo "=== Loop Structure Analysis ==="
echo "Comparing loop structures and vectorization patterns:"

echo ""
echo "Java 17 Vector loop patterns:"
grep -i "loop\|jmp\|cmp" assembly_output/java17_vector.asm | head -5

echo ""
echo "Java 25 Vector loop patterns:"
grep -i "loop\|jmp\|cmp" assembly_output/java25_vector.asm | head -5

echo ""
echo "Analysis complete. Review assembly_output/ files for detailed comparison."
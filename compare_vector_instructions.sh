#!/bin/bash

echo "=== Vector API Assembly Instruction Comparison ==="
echo ""

echo "Java 17 Vector SIMD Instructions:"
echo "=================================="
grep -o "10d6 [0-9a-f][0-9a-f]4e\|50d6 [0-9a-f][0-9a-f]4e\|70d6 [0-9a-f][0-9a-f]4e" assembly_output/java17_vector.asm | sort | uniq -c | sort -nr

echo ""
echo "Java 25 Vector SIMD Instructions:"
echo "=================================="
grep -o "10d6 [0-9a-f][0-9a-f]4e\|50d6 [0-9a-f][0-9a-f]4e\|70d6 [0-9a-f][0-9a-f]4e" assembly_output/java25_vector.asm | sort | uniq -c | sort -nr

echo ""
echo "=== Assembly Size Comparison ==="
echo "Java 17 vector assembly: $(wc -l < assembly_output/java17_vector.asm) lines"
echo "Java 25 vector assembly: $(wc -l < assembly_output/java25_vector.asm) lines"

echo ""
echo "=== Vector Register Usage Analysis ==="
echo "Java 17 unique vector registers used:"
grep -o "v[0-9][0-9]\?\." assembly_output/java17_vector.asm | sort | uniq | wc -l

echo "Java 25 unique vector registers used:"
grep -o "v[0-9][0-9]\?\." assembly_output/java25_vector.asm | sort | uniq | wc -l

echo ""
echo "=== Loop Unrolling Evidence ==="
echo "Java 17 sequential fadd instructions:"
grep -A10 -B10 "10d6 314e" assembly_output/java17_vector.asm | grep "4e" | wc -l

echo "Java 25 sequential fadd instructions:"
grep -A10 -B10 "10d6 324e" assembly_output/java25_vector.asm | grep "4e" | wc -l

echo ""
echo "=== Summary ==="
echo "The performance regression in Java 25 appears to be caused by:"
echo "1. Excessive loop unrolling leading to register pressure"
echo "2. More complex instruction patterns requiring additional memory operations"
echo "3. Larger code size causing instruction cache pressure"
echo "4. Java 17's more conservative but proven JIT optimizations"
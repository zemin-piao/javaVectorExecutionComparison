#!/bin/bash

source ~/.sdkman/bin/sdkman-init.sh

echo "=== 10-Iteration Statistical Benchmark ==="
echo "Using separated benchmark files for clean execution"
echo ""

# Java 8 Scalar (10 runs)
echo "Java 8 Scalar Benchmarks:"
sdk use java 8.0.422-amzn > /dev/null 2>&1
javac ScalarBenchmark.java
declare -a java8_times=()

for i in {1..10}; do
    output=$(java ScalarBenchmark 2>/dev/null | grep "Time:")
    time=$(echo "$output" | sed 's/Time: \([0-9.]*\) ms/\1/')
    java8_times+=($time)
    echo "Run $i: ${time}ms"
done

# Java 17 Scalar (10 runs)
echo ""
echo "Java 17 Scalar Benchmarks:"
sdk use java 17.0.12-amzn > /dev/null 2>&1
javac ScalarBenchmark.java
declare -a java17_scalar_times=()

for i in {1..10}; do
    output=$(java ScalarBenchmark 2>/dev/null | grep "Time:")
    time=$(echo "$output" | sed 's/Time: \([0-9.]*\) ms/\1/')
    java17_scalar_times+=($time)
    echo "Run $i: ${time}ms"
done

# Java 17 Vector Simple (10 runs)
echo ""
echo "Java 17 Vector Simple Benchmarks:"
javac --add-modules jdk.incubator.vector VectorSimple.java 2>/dev/null
declare -a java17_vector_times=()

for i in {1..10}; do
    output=$(java --add-modules jdk.incubator.vector VectorSimple 2>/dev/null | grep "Time:")
    time=$(echo "$output" | sed 's/Time: \([0-9.]*\) ms/\1/')
    java17_vector_times+=($time)
    echo "Run $i: ${time}ms"
done

# Java 17 Vector Unrolled (10 runs)
echo ""
echo "Java 17 Vector Unrolled Benchmarks:"
javac --add-modules jdk.incubator.vector VectorUnrolled.java 2>/dev/null
declare -a java17_unrolled_times=()

for i in {1..10}; do
    output=$(java --add-modules jdk.incubator.vector VectorUnrolled 2>/dev/null | grep "Time:")
    time=$(echo "$output" | sed 's/Time: \([0-9.]*\) ms/\1/')
    java17_unrolled_times+=($time)
    echo "Run $i: ${time}ms"
done

# Java 25 Scalar (10 runs)
echo ""
echo "Java 25 Scalar Benchmarks:"
sdk use java 25-amzn > /dev/null 2>&1
javac ScalarBenchmark.java
declare -a java25_scalar_times=()

for i in {1..10}; do
    output=$(java ScalarBenchmark 2>/dev/null | grep "Time:")
    time=$(echo "$output" | sed 's/Time: \([0-9.]*\) ms/\1/')
    java25_scalar_times+=($time)
    echo "Run $i: ${time}ms"
done

# Java 25 Vector Simple (10 runs)
echo ""
echo "Java 25 Vector Simple Benchmarks:"
javac --add-modules jdk.incubator.vector VectorSimple.java 2>/dev/null
declare -a java25_vector_times=()

for i in {1..10}; do
    output=$(java --add-modules jdk.incubator.vector VectorSimple 2>/dev/null | grep "Time:")
    time=$(echo "$output" | sed 's/Time: \([0-9.]*\) ms/\1/')
    java25_vector_times+=($time)
    echo "Run $i: ${time}ms"
done

# Java 25 Vector Unrolled (10 runs)
echo ""
echo "Java 25 Vector Unrolled Benchmarks:"
javac --add-modules jdk.incubator.vector VectorUnrolled.java 2>/dev/null
declare -a java25_unrolled_times=()

for i in {1..10}; do
    output=$(java --add-modules jdk.incubator.vector VectorUnrolled 2>/dev/null | grep "Time:")
    time=$(echo "$output" | sed 's/Time: \([0-9.]*\) ms/\1/')
    java25_unrolled_times+=($time)
    echo "Run $i: ${time}ms"
done

# Calculate averages and medians using awk
echo ""
echo "================================================================================"
echo "STATISTICAL RESULTS (10 iterations each)"
echo "================================================================================"

# Calculate averages
java8_avg=$(printf '%s\n' "${java8_times[@]}" | awk '{sum+=$1; count++} END {print sum/count}')
java17_scalar_avg=$(printf '%s\n' "${java17_scalar_times[@]}" | awk '{sum+=$1; count++} END {print sum/count}')
java17_vector_avg=$(printf '%s\n' "${java17_vector_times[@]}" | awk '{sum+=$1; count++} END {print sum/count}')
java17_unrolled_avg=$(printf '%s\n' "${java17_unrolled_times[@]}" | awk '{sum+=$1; count++} END {print sum/count}')
java25_scalar_avg=$(printf '%s\n' "${java25_scalar_times[@]}" | awk '{sum+=$1; count++} END {print sum/count}')
java25_vector_avg=$(printf '%s\n' "${java25_vector_times[@]}" | awk '{sum+=$1; count++} END {print sum/count}')
java25_unrolled_avg=$(printf '%s\n' "${java25_unrolled_times[@]}" | awk '{sum+=$1; count++} END {print sum/count}')

# Calculate medians
java8_median=$(printf '%s\n' "${java8_times[@]}" | sort -n | awk '{arr[NR]=$1} END {if(NR%2==1) print arr[(NR+1)/2]; else print (arr[NR/2]+arr[NR/2+1])/2}')
java17_scalar_median=$(printf '%s\n' "${java17_scalar_times[@]}" | sort -n | awk '{arr[NR]=$1} END {if(NR%2==1) print arr[(NR+1)/2]; else print (arr[NR/2]+arr[NR/2+1])/2}')
java17_vector_median=$(printf '%s\n' "${java17_vector_times[@]}" | sort -n | awk '{arr[NR]=$1} END {if(NR%2==1) print arr[(NR+1)/2]; else print (arr[NR/2]+arr[NR/2+1])/2}')
java17_unrolled_median=$(printf '%s\n' "${java17_unrolled_times[@]}" | sort -n | awk '{arr[NR]=$1} END {if(NR%2==1) print arr[(NR+1)/2]; else print (arr[NR/2]+arr[NR/2+1])/2}')
java25_scalar_median=$(printf '%s\n' "${java25_scalar_times[@]}" | sort -n | awk '{arr[NR]=$1} END {if(NR%2==1) print arr[(NR+1)/2]; else print (arr[NR/2]+arr[NR/2+1])/2}')
java25_vector_median=$(printf '%s\n' "${java25_vector_times[@]}" | sort -n | awk '{arr[NR]=$1} END {if(NR%2==1) print arr[(NR+1)/2]; else print (arr[NR/2]+arr[NR/2+1])/2}')
java25_unrolled_median=$(printf '%s\n' "${java25_unrolled_times[@]}" | sort -n | awk '{arr[NR]=$1} END {if(NR%2==1) print arr[(NR+1)/2]; else print (arr[NR/2]+arr[NR/2+1])/2}')

printf "%-25s | %10s | %10s | %12s\n" "Configuration" "Avg (ms)" "Median (ms)" "Throughput"
echo "--------------------------------------------------------------------------------"
printf "%-25s | %10.2f | %10.2f | %8.0f M/s\n" "Java 8 Scalar" "$java8_avg" "$java8_median" "$(echo "100 / ($java8_avg / 1000)" | bc)"
printf "%-25s | %10.2f | %10.2f | %8.0f M/s\n" "Java 17 Scalar" "$java17_scalar_avg" "$java17_scalar_median" "$(echo "100 / ($java17_scalar_avg / 1000)" | bc)"
printf "%-25s | %10.2f | %10.2f | %8.0f M/s\n" "Java 17 Vector Simple" "$java17_vector_avg" "$java17_vector_median" "$(echo "100 / ($java17_vector_avg / 1000)" | bc)"
printf "%-25s | %10.2f | %10.2f | %8.0f M/s\n" "Java 17 Vector Unrolled" "$java17_unrolled_avg" "$java17_unrolled_median" "$(echo "100 / ($java17_unrolled_avg / 1000)" | bc)"
printf "%-25s | %10.2f | %10.2f | %8.0f M/s\n" "Java 25 Scalar" "$java25_scalar_avg" "$java25_scalar_median" "$(echo "100 / ($java25_scalar_avg / 1000)" | bc)"
printf "%-25s | %10.2f | %10.2f | %8.0f M/s\n" "Java 25 Vector Simple" "$java25_vector_avg" "$java25_vector_median" "$(echo "100 / ($java25_vector_avg / 1000)" | bc)"
printf "%-25s | %10.2f | %10.2f | %8.0f M/s\n" "Java 25 Vector Unrolled" "$java25_unrolled_avg" "$java25_unrolled_median" "$(echo "100 / ($java25_unrolled_avg / 1000)" | bc)"

echo ""
echo "SPEEDUP ANALYSIS (vs respective scalar baseline):"
echo "--------------------------------------------------------------------------------"
printf "Java 17 Vector Simple speedup:   %.2fx (avg) | %.2fx (median)\n" $(echo "$java17_scalar_avg / $java17_vector_avg" | bc -l) $(echo "$java17_scalar_median / $java17_vector_median" | bc -l)
printf "Java 17 Vector Unrolled speedup: %.2fx (avg) | %.2fx (median)\n" $(echo "$java17_scalar_avg / $java17_unrolled_avg" | bc -l) $(echo "$java17_scalar_median / $java17_unrolled_median" | bc -l)
printf "Java 25 Vector Simple speedup:   %.2fx (avg) | %.2fx (median)\n" $(echo "$java25_scalar_avg / $java25_vector_avg" | bc -l) $(echo "$java25_scalar_median / $java25_vector_median" | bc -l)
printf "Java 25 Vector Unrolled speedup: %.2fx (avg) | %.2fx (median)\n" $(echo "$java25_scalar_avg / $java25_unrolled_avg" | bc -l) $(echo "$java25_scalar_median / $java25_unrolled_median" | bc -l)

echo ""
echo "CROSS-JAVA VERSION COMPARISON:"
echo "--------------------------------------------------------------------------------"
printf "Scalar Performance (Java 8 vs 17 vs 25):\n"
printf "  Java 17 vs Java 8:  %.2fx (%.2f vs %.2f ms)\n" $(echo "$java8_avg / $java17_scalar_avg" | bc -l) "$java8_avg" "$java17_scalar_avg"
printf "  Java 25 vs Java 8:  %.2fx (%.2f vs %.2f ms)\n" $(echo "$java8_avg / $java25_scalar_avg" | bc -l) "$java8_avg" "$java25_scalar_avg"
printf "  Java 25 vs Java 17: %.2fx (%.2f vs %.2f ms)\n" $(echo "$java17_scalar_avg / $java25_scalar_avg" | bc -l) "$java17_scalar_avg" "$java25_scalar_avg"

echo ""
echo "Vector API Evolution (Java 17 vs 25):"
printf "  Vector Simple:   Java 25 is %.2fx vs Java 17 (%.2f vs %.2f ms)\n" $(echo "$java17_vector_avg / $java25_vector_avg" | bc -l) "$java25_vector_avg" "$java17_vector_avg"
printf "  Vector Unrolled: Java 25 is %.2fx vs Java 17 (%.2f vs %.2f ms)\n" $(echo "$java17_unrolled_avg / $java25_unrolled_avg" | bc -l) "$java25_unrolled_avg" "$java17_unrolled_avg"

echo "================================================================================"
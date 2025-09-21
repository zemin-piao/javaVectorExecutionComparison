#!/bin/bash

# Multi-iteration benchmark runner for statistical analysis
# Runs each Java version 10 times and calculates average/median

source ~/.sdkman/bin/sdkman-init.sh

echo "=== Java Performance Comparison: Statistical Analysis ==="
echo "Running 10 iterations per configuration..."
echo "Dataset: 100,000,000 records (seed=12345)"
echo ""

# Function to extract time from benchmark output
extract_time() {
    echo "$1" | grep "Time:" | sed 's/.*Time: \([0-9.]*\) ms.*/\1/'
}

# Function to extract result from benchmark output
extract_result() {
    echo "$1" | grep "Result:" | sed 's/.*Result: \$\([0-9]*\).*/\1/'
}

# Function to extract throughput from benchmark output
extract_throughput() {
    echo "$1" | grep "Throughput:" | sed 's/.*Throughput: \([0-9]*\) M records\/sec.*/\1/'
}

# Function to calculate average
calculate_avg() {
    echo "$1" | tr ' ' '\n' | awk '{sum+=$1} END {print sum/NR}'
}

# Function to calculate median
calculate_median() {
    echo "$1" | tr ' ' '\n' | sort -n | awk '{
        arr[NR] = $1
    } END {
        if (NR % 2 == 1) {
            print arr[(NR+1)/2]
        } else {
            print (arr[NR/2] + arr[NR/2+1]) / 2
        }
    }'
}

# Arrays to store results
declare -a java8_times=()
declare -a java8_results=()
declare -a java8_throughputs=()

declare -a java17_scalar_times=()
declare -a java17_vector_times=()
declare -a java17_unrolled_times=()
declare -a java17_scalar_results=()
declare -a java17_vector_results=()
declare -a java17_unrolled_results=()

declare -a java25_scalar_times=()
declare -a java25_vector_times=()
declare -a java25_unrolled_times=()
declare -a java25_scalar_results=()
declare -a java25_vector_results=()
declare -a java25_unrolled_results=()

echo "=== JAVA 8 SCALAR BENCHMARKS ==="
sdk use java 8.0.422-amzn > /dev/null 2>&1
javac ScalarBenchmark.java

for i in {1..10}; do
    echo "Java 8 Run $i/10..."
    output=$(java ScalarBenchmark 2>/dev/null)
    time=$(extract_time "$output")
    result=$(extract_result "$output")
    throughput=$(extract_throughput "$output")

    java8_times+=($time)
    java8_results+=($result)
    java8_throughputs+=($throughput)

    echo "  Time: ${time}ms, Throughput: ${throughput}M/s"
done

echo ""
echo "=== JAVA 17 BENCHMARKS ==="
sdk use java 17.0.12-amzn > /dev/null 2>&1
javac ScalarBenchmark.java
javac --add-modules jdk.incubator.vector VectorBenchmark.java 2>/dev/null

for i in {1..10}; do
    echo "Java 17 Run $i/10..."

    # Scalar
    scalar_output=$(java ScalarBenchmark 2>/dev/null)
    scalar_time=$(extract_time "$scalar_output")
    scalar_result=$(extract_result "$scalar_output")
    java17_scalar_times+=($scalar_time)
    java17_scalar_results+=($scalar_result)

    # Vector
    vector_output=$(java --add-modules jdk.incubator.vector VectorBenchmark 2>/dev/null)
    vector_time=$(echo "$vector_output" | grep "Vector " | grep -v "Unrolled" | sed 's/.*| *\([0-9.]*\) |.*/\1/')
    vector_result=$(echo "$vector_output" | grep "Vector " | grep -v "Unrolled" | sed 's/.*| *[0-9.]* | *\([0-9]*\) |.*/\1/')
    unrolled_time=$(echo "$vector_output" | grep "Vector Unrolled" | sed 's/.*| *\([0-9.]*\) |.*/\1/')
    unrolled_result=$(echo "$vector_output" | grep "Vector Unrolled" | sed 's/.*| *[0-9.]* | *\([0-9]*\) |.*/\1/')

    java17_vector_times+=($vector_time)
    java17_vector_results+=($vector_result)
    java17_unrolled_times+=($unrolled_time)
    java17_unrolled_results+=($unrolled_result)

    echo "  Scalar: ${scalar_time}ms, Vector: ${vector_time}ms, Unrolled: ${unrolled_time}ms"
done

echo ""
echo "=== JAVA 25 BENCHMARKS ==="
sdk use java 25-amzn > /dev/null 2>&1
javac ScalarBenchmark.java
javac --add-modules jdk.incubator.vector VectorBenchmark.java 2>/dev/null

for i in {1..10}; do
    echo "Java 25 Run $i/10..."

    # Scalar
    scalar_output=$(java ScalarBenchmark 2>/dev/null)
    scalar_time=$(extract_time "$scalar_output")
    scalar_result=$(extract_result "$scalar_output")
    java25_scalar_times+=($scalar_time)
    java25_scalar_results+=($scalar_result)

    # Vector
    vector_output=$(java --add-modules jdk.incubator.vector VectorBenchmark 2>/dev/null)
    vector_time=$(echo "$vector_output" | grep "Vector " | grep -v "Unrolled" | sed 's/.*| *\([0-9.]*\) |.*/\1/')
    vector_result=$(echo "$vector_output" | grep "Vector " | grep -v "Unrolled" | sed 's/.*| *[0-9.]* | *\([0-9]*\) |.*/\1/')
    unrolled_time=$(echo "$vector_output" | grep "Vector Unrolled" | sed 's/.*| *\([0-9.]*\) |.*/\1/')
    unrolled_result=$(echo "$vector_output" | grep "Vector Unrolled" | sed 's/.*| *[0-9.]* | *\([0-9]*\) |.*/\1/')

    java25_vector_times+=($vector_time)
    java25_vector_results+=($vector_result)
    java25_unrolled_times+=($unrolled_time)
    java25_unrolled_results+=($unrolled_result)

    echo "  Scalar: ${scalar_time}ms, Vector: ${vector_time}ms, Unrolled: ${unrolled_time}ms"
done

# Calculate statistics
echo ""
echo "================================================================================"
echo "STATISTICAL ANALYSIS RESULTS (10 iterations each)"
echo "================================================================================"

printf "%-20s | %10s | %10s | %15s | %12s\n" "Configuration" "Avg (ms)" "Median (ms)" "Result (\$)" "Avg Throughput"
echo "--------------------------------------------------------------------------------"

# Java 8
java8_avg=$(calculate_avg "${java8_times[*]}")
java8_median=$(calculate_median "${java8_times[*]}")
java8_avg_throughput=$(calculate_avg "${java8_throughputs[*]}")
printf "%-20s | %10.2f | %10.2f | %15s | %8.0f M/s\n" "Java 8 Scalar" "$java8_avg" "$java8_median" "${java8_results[0]}" "$java8_avg_throughput"

# Java 17
java17_scalar_avg=$(calculate_avg "${java17_scalar_times[*]}")
java17_scalar_median=$(calculate_median "${java17_scalar_times[*]}")
java17_vector_avg=$(calculate_avg "${java17_vector_times[*]}")
java17_vector_median=$(calculate_median "${java17_vector_times[*]}")
java17_unrolled_avg=$(calculate_avg "${java17_unrolled_times[*]}")
java17_unrolled_median=$(calculate_median "${java17_unrolled_times[*]}")

printf "%-20s | %10.2f | %10.2f | %15s | %8.0f M/s\n" "Java 17 Scalar" "$java17_scalar_avg" "$java17_scalar_median" "${java17_scalar_results[0]}" "$(echo "100000000 / ($java17_scalar_avg / 1000) / 1000000" | bc)"
printf "%-20s | %10.2f | %10.2f | %15s | %8.0f M/s\n" "Java 17 Vector" "$java17_vector_avg" "$java17_vector_median" "${java17_vector_results[0]}" "$(echo "100000000 / ($java17_vector_avg / 1000) / 1000000" | bc)"
printf "%-20s | %10.2f | %10.2f | %15s | %8.0f M/s\n" "Java 17 Unrolled" "$java17_unrolled_avg" "$java17_unrolled_median" "${java17_unrolled_results[0]}" "$(echo "100000000 / ($java17_unrolled_avg / 1000) / 1000000" | bc)"

# Java 25
java25_scalar_avg=$(calculate_avg "${java25_scalar_times[*]}")
java25_scalar_median=$(calculate_median "${java25_scalar_times[*]}")
java25_vector_avg=$(calculate_avg "${java25_vector_times[*]}")
java25_vector_median=$(calculate_median "${java25_vector_times[*]}")
java25_unrolled_avg=$(calculate_avg "${java25_unrolled_times[*]}")
java25_unrolled_median=$(calculate_median "${java25_unrolled_times[*]}")

printf "%-20s | %10.2f | %10.2f | %15s | %8.0f M/s\n" "Java 25 Scalar" "$java25_scalar_avg" "$java25_scalar_median" "${java25_scalar_results[0]}" "$(echo "100000000 / ($java25_scalar_avg / 1000) / 1000000" | bc)"
printf "%-20s | %10.2f | %10.2f | %15s | %8.0f M/s\n" "Java 25 Vector" "$java25_vector_avg" "$java25_vector_median" "${java25_vector_results[0]}" "$(echo "100000000 / ($java25_vector_avg / 1000) / 1000000" | bc)"
printf "%-20s | %10.2f | %10.2f | %15s | %8.0f M/s\n" "Java 25 Unrolled" "$java25_unrolled_avg" "$java25_unrolled_median" "${java25_unrolled_results[0]}" "$(echo "100000000 / ($java25_unrolled_avg / 1000) / 1000000" | bc)"

echo "================================================================================"

# Speedup calculations
echo ""
echo "SPEEDUP ANALYSIS (vs respective scalar baseline):"
echo "--------------------------------------------------------------------------------"
printf "Java 17 Vector speedup:   %.2fx (avg) | %.2fx (median)\n" $(echo "$java17_scalar_avg / $java17_vector_avg" | bc -l) $(echo "$java17_scalar_median / $java17_vector_median" | bc -l)
printf "Java 17 Unrolled speedup: %.2fx (avg) | %.2fx (median)\n" $(echo "$java17_scalar_avg / $java17_unrolled_avg" | bc -l) $(echo "$java17_scalar_median / $java17_unrolled_median" | bc -l)
printf "Java 25 Vector speedup:   %.2fx (avg) | %.2fx (median)\n" $(echo "$java25_scalar_avg / $java25_vector_avg" | bc -l) $(echo "$java25_scalar_median / $java25_vector_median" | bc -l)
printf "Java 25 Unrolled speedup: %.2fx (avg) | %.2fx (median)\n" $(echo "$java25_scalar_avg / $java25_unrolled_avg" | bc -l) $(echo "$java25_scalar_median / $java25_unrolled_median" | bc -l)

echo ""
echo "Statistical analysis complete!"
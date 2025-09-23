#!/bin/bash

# Comprehensive JMH Benchmark Runner Script
# Runs Vector API vs Scalar benchmarks with Corretto 8, 17, 25 and GraalVM 17, 25

source ~/.sdkman/bin/sdkman-init.sh

echo "=============================================================================="
echo "JMH Vector API Performance Comparison - Comprehensive Edition"
echo "Testing: Amazon Corretto (8, 17, 25) vs GraalVM (17, 25)"
echo "=============================================================================="

# Function to run benchmarks with a specific Java version
run_benchmarks() {
    local java_identifier=$1
    local display_name=$2
    local java_path="$HOME/.sdkman/candidates/java/$java_identifier"

    echo ""
    echo "Building and running with $display_name ($java_identifier)..."
    echo "------------------------------------------------------------------------------"

    # Check if Java version exists
    if [ ! -d "$java_path" ]; then
        echo "❌ $display_name not found at $java_path"
        return 1
    fi

    # Set Java version
    export JAVA_HOME="$java_path"
    export PATH="$java_path/bin:$PATH"

    # Show Java version info
    echo "Java Info:"
    "$java_path/bin/java" -version 2>&1 | head -3 | sed 's/^/  /'

    # Clean and build
    echo "Building project..."

    # Use Java 8 specific POM for Java 8
    if [[ "$java_identifier" == *"8"* ]]; then
        mvn clean package -q -f pom-java8.xml
        jar_name="jmh-benchmarks-java8.jar"
    else
        mvn clean package -q
        jar_name="jmh-benchmarks.jar"
    fi

    if [ $? -eq 0 ]; then
        echo "✅ Build successful with $display_name"

        # Run benchmarks with comprehensive settings
        echo "Running JMH benchmarks (this may take several minutes)..."

        # Check if Vector API is supported (Java 17+)
        if [[ "$java_identifier" == *"17"* ]] || [[ "$java_identifier" == *"25"* ]]; then
            echo "Vector API supported - running full benchmark suite..."
            "$java_path/bin/java" --add-modules jdk.incubator.vector \
                -jar "target/$jar_name" AllBenchmarksJMH \
                -f 1 -wi 5 -i 10 -rf json -rff "jmh-results-${java_identifier}.json"
        else
            echo "Vector API not supported - running scalar benchmarks only..."
            "$java_path/bin/java" \
                -jar "target/$jar_name" ScalarBenchmarkJMH \
                -f 1 -wi 5 -i 10 -rf json -rff "jmh-results-${java_identifier}.json"
        fi

        echo "✅ Results saved to: jmh-results-${java_identifier}.json"
    else
        echo "❌ Build failed with $display_name"
    fi
}

echo ""
echo "Starting comprehensive benchmark execution..."
echo "=============================================================================="

# Run benchmarks with all JVM versions
run_benchmarks "8.0.462-amzn" "Amazon Corretto 8.0.462"
run_benchmarks "17.0.12-amzn" "Amazon Corretto 17.0.12"
run_benchmarks "25-amzn" "Amazon Corretto 25"
run_benchmarks "17.0.12-graal" "GraalVM 17.0.12"
run_benchmarks "25-graal" "GraalVM 25"

echo ""
echo "=============================================================================="
echo "Comprehensive benchmark execution completed!"
echo "=============================================================================="
echo ""
echo "Results Summary:"
echo "- JSON files: jmh-results-*.json (detailed statistical data)"
echo "- CSV files: jmh-results-*.csv (spreadsheet-friendly format)"
echo ""
echo "Available result files:"
ls -la jmh-results-*.{json,csv} 2>/dev/null | sed 's/^/  /' || echo "  No result files found"
echo ""
echo "Next steps:"
echo "1. Analyze JSON results for detailed performance metrics"
echo "2. Compare CSV files in spreadsheet software"
echo "3. Look for Vector API performance differences between JVM implementations"
echo "=============================================================================="
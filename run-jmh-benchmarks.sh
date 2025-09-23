#!/bin/bash

# JMH Benchmark Runner Script
# Runs Vector API vs Scalar benchmarks with different Java versions
# Supports both GraalVM and Amazon Corretto JDK implementations

source ~/.sdkman/bin/sdkman-init.sh

echo "=============================================================================="
echo "JMH Vector API Performance Comparison - Multi-JVM Edition"
echo "Testing: GraalVM vs Amazon Corretto across Java 8, 17 and 25"
echo "=============================================================================="

# Function to run benchmarks with a specific Java version
run_benchmarks() {
    local java_version=$1
    local java_path=$2
    local display_name=$3

    echo ""
    echo "Building and running with $display_name ($java_version)..."
    echo "------------------------------------------------------------------------------"

    # Set Java version
    export JAVA_HOME=$java_path

    # Show Java version info
    echo "Java Info:"
    $java_path/bin/java -version 2>&1 | head -3 | sed 's/^/  /'

    # Clean and build
    echo "Building project..."
    mvn clean package -q

    if [ $? -eq 0 ]; then
        echo "✅ Build successful with $display_name"

        # Run benchmarks with comprehensive settings
        echo "Running JMH benchmarks (this may take several minutes)..."

        # Check if Vector API is supported (Java 17+)
        if [[ "$java_version" == *"17"* ]] || [[ "$java_version" == *"25"* ]]; then
            echo "Vector API supported - running full benchmark suite..."
            $java_path/bin/java --add-modules jdk.incubator.vector \
                -jar target/jmh-benchmarks.jar AllBenchmarksJMH \
                -f 1 -wi 5 -i 10 -rf json -rff "jmh-results-${java_version}.json" \
                -rf csv -rff "jmh-results-${java_version}.csv"
        else
            echo "Vector API not supported - running scalar benchmarks only..."
            $java_path/bin/java \
                -jar target/jmh-benchmarks.jar ScalarBenchmarkJMH \
                -f 1 -wi 5 -i 10 -rf json -rff "jmh-results-${java_version}.json" \
                -rf csv -rff "jmh-results-${java_version}.csv"
        fi

        echo "✅ Results saved to: jmh-results-${java_version}.json and .csv"
    else
        echo "❌ Build failed with $display_name"
    fi
}

# Define JVM configurations to test
declare -A jvm_configs
jvm_configs=(
    ["corretto-8"]="~/.sdkman/candidates/java/8.0.462-amzn|Amazon Corretto 8.0.462"
    ["corretto-17"]="~/.sdkman/candidates/java/17.0.12-amzn|Amazon Corretto 17.0.12"
    ["corretto-25"]="~/.sdkman/candidates/java/25-amzn|Amazon Corretto 25"
    ["graalvm-17"]="~/.sdkman/candidates/java/17.0.12-graal|GraalVM 17.0.12"
    ["graalvm-25"]="~/.sdkman/candidates/java/25-graal|GraalVM 25"
)

# Check and install missing JVM versions
echo "Checking JVM installations..."
missing_jvms=()

for version in "${!jvm_configs[@]}"; do
    IFS='|' read -r path display_name <<< "${jvm_configs[$version]}"
    expanded_path=$(eval echo $path)

    if [ ! -d "$expanded_path" ]; then
        echo "⚠️  $display_name not found at $expanded_path"
        missing_jvms+=("$version")
    else
        echo "✅ $display_name found"
    fi
done

# Install missing JVMs
if [ ${#missing_jvms[@]} -gt 0 ]; then
    echo ""
    echo "Installing missing JVM versions..."
    for version in "${missing_jvms[@]}"; do
        case $version in
            "corretto-8")
                echo "Installing Amazon Corretto 8..."
                sdk install java 8.0.462-amzn
                ;;
            "corretto-17")
                echo "Installing Amazon Corretto 17..."
                sdk install java 17.0.12-amzn
                ;;
            "corretto-25")
                echo "Installing Amazon Corretto 25..."
                sdk install java 25-amzn
                ;;
            "graalvm-17")
                echo "Installing GraalVM 17..."
                sdk install java 17.0.12-graal
                ;;
            "graalvm-25")
                echo "Installing GraalVM 25..."
                sdk install java 25-graal
                ;;
        esac
    done
fi

echo ""
echo "Starting comprehensive benchmark execution..."
echo "=============================================================================="

# Run benchmarks with all available JVM versions
for version in "${!jvm_configs[@]}"; do
    IFS='|' read -r path display_name <<< "${jvm_configs[$version]}"
    expanded_path=$(eval echo $path)

    if [ -d "$expanded_path" ]; then
        run_benchmarks "$version" "$expanded_path" "$display_name"
    else
        echo "⚠️  Skipping $display_name - installation failed or path not found"
    fi
done

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
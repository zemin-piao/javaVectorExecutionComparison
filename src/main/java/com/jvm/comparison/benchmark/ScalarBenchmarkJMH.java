package com.jvm.comparison.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH Scalar Implementation Benchmark - Works on all Java versions
 *
 * Pure scalar for-loop implementation for baseline comparison using JMH framework.
 * Run: java -jar target/jmh-benchmarks.jar ScalarBenchmarkJMH
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ScalarBenchmarkJMH {

    @Param({"100000000"})  // 100M records
    private int dataSize;

    private static final long FIXED_SEED = 12345L;
    private float[] salaries;

    @Setup(Level.Trial)
    public void setup() {
        System.out.println("=== JMH Scalar Benchmark Setup ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Dataset: " + String.format("%,d", dataSize) + " records (seed=" + FIXED_SEED + ")");
        System.out.println("Memory: ~" + (dataSize * 4 / 1024 / 1024) + " MB\n");

        salaries = generateSalaryData(dataSize, FIXED_SEED);
    }

    @TearDown(Level.Trial)
    public void teardown() {
        salaries = null;
        System.gc();
    }

    private static float[] generateSalaryData(int size, long seed) {
        Random random = new Random(seed);
        float[] salaries = new float[size];
        for (int i = 0; i < size; i++) {
            salaries[i] = 30000.0f + random.nextFloat() * 120000.0f;
        }
        return salaries;
    }

    @Benchmark
    public void scalarSum(Blackhole bh) {
        double sum = 0.0;
        for (int i = 0; i < salaries.length; i++) {
            sum += salaries[i];
        }
        bh.consume(sum);
    }
}
package com.jvm.comparison.benchmark;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Complete JMH Benchmark Suite - All implementations in one class
 *
 * Comprehensive benchmark including Scalar, Vector Simple, and Vector Unrolled implementations.
 * Run: java --add-modules jdk.incubator.vector -jar target/jmh-benchmarks.jar AllBenchmarksJMH
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class AllBenchmarksJMH {

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    @Param({"100000000"})  // 100M records
    private int dataSize;

    private static final long FIXED_SEED = 12345L;
    private float[] salaries;

    @Setup(Level.Trial)
    public void setup() {
        System.out.println("=== JMH Complete Benchmark Suite Setup ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Vector Species: " + SPECIES);
        System.out.println("Vector Length: " + SPECIES.length());
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

    @Benchmark
    public void vectorSimple(Blackhole bh) {
        int vectorLength = SPECIES.length();
        int upperBound = SPECIES.loopBound(salaries.length);

        FloatVector sum = FloatVector.zero(SPECIES);

        // Vector processing
        int i = 0;
        for (; i < upperBound; i += vectorLength) {
            FloatVector v = FloatVector.fromArray(SPECIES, salaries, i);
            sum = sum.add(v);
        }

        double vectorSum = sum.reduceLanes(VectorOperators.ADD);

        // Scalar tail
        double scalarSum = 0.0;
        for (; i < salaries.length; i++) {
            scalarSum += salaries[i];
        }

        double totalSum = vectorSum + scalarSum;
        bh.consume(totalSum);
    }

    @Benchmark
    public void vectorUnrolled(Blackhole bh) {
        int vectorLength = SPECIES.length();
        int upperBound = SPECIES.loopBound(salaries.length);

        FloatVector sum1 = FloatVector.zero(SPECIES);
        FloatVector sum2 = FloatVector.zero(SPECIES);
        FloatVector sum3 = FloatVector.zero(SPECIES);
        FloatVector sum4 = FloatVector.zero(SPECIES);

        // Unrolled processing (4 vectors per iteration)
        int i = 0;
        int unrollBound = upperBound - (4 * vectorLength);
        for (; i < unrollBound; i += 4 * vectorLength) {
            FloatVector v1 = FloatVector.fromArray(SPECIES, salaries, i);
            FloatVector v2 = FloatVector.fromArray(SPECIES, salaries, i + vectorLength);
            FloatVector v3 = FloatVector.fromArray(SPECIES, salaries, i + 2 * vectorLength);
            FloatVector v4 = FloatVector.fromArray(SPECIES, salaries, i + 3 * vectorLength);

            sum1 = sum1.add(v1);
            sum2 = sum2.add(v2);
            sum3 = sum3.add(v3);
            sum4 = sum4.add(v4);
        }

        // Process remaining vectors (single at a time)
        for (; i < upperBound; i += vectorLength) {
            FloatVector v = FloatVector.fromArray(SPECIES, salaries, i);
            sum1 = sum1.add(v);
        }

        // Combine all sums
        FloatVector totalSum = sum1.add(sum2).add(sum3).add(sum4);
        double vectorSum = totalSum.reduceLanes(VectorOperators.ADD);

        // Scalar tail
        double scalarSum = 0.0;
        for (; i < salaries.length; i++) {
            scalarSum += salaries[i];
        }

        double finalSum = vectorSum + scalarSum;
        bh.consume(finalSum);
    }
}
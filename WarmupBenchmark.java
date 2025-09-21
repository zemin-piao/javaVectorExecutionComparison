import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;
import java.util.Random;

/**
 * Warm-up Impact Benchmark - Java 17+ Vector API
 *
 * Demonstrates the critical importance of JIT warm-up for Vector API performance.
 * Measures performance before and after warm-up to show the dramatic difference.
 *
 * Compile: javac --add-modules jdk.incubator.vector WarmupBenchmark.java
 * Run: java --add-modules jdk.incubator.vector WarmupBenchmark [size]
 */
public class WarmupBenchmark {

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
    private static final int DEFAULT_SIZE = 100_000_000;
    private static final long FIXED_SEED = 12345L;

    public static float[] generateSalaryData(int size, long seed) {
        Random random = new Random(seed);
        float[] salaries = new float[size];
        for (int i = 0; i < size; i++) {
            salaries[i] = 30000.0f + random.nextFloat() * 120000.0f;
        }
        return salaries;
    }

    public static double sumSalariesScalar(float[] salaries) {
        double sum = 0.0;
        for (int i = 0; i < salaries.length; i++) {
            sum += salaries[i];
        }
        return sum;
    }

    public static double sumSalariesVector(float[] salaries) {
        int vectorLength = SPECIES.length();
        int upperBound = SPECIES.loopBound(salaries.length);

        FloatVector sum = FloatVector.zero(SPECIES);

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

        return vectorSum + scalarSum;
    }

    public static void measureColdPerformance(String name, float[] data,
                                            java.util.function.Function<float[], Double> impl, int iterations) {
        System.out.println("\n=== " + name + " Cold Performance (No Warm-up) ===");

        long totalTime = 0;
        double result = 0.0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            result = impl.apply(data);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            totalTime += duration;

            System.out.printf("Cold run %d: %.2f ms | Result: $%.0f\n",
                            i + 1, duration / 1_000_000.0, result);
        }

        double avgTimeMs = (totalTime / (double) iterations) / 1_000_000.0;
        double throughputMps = (data.length / (avgTimeMs / 1000.0)) / 1_000_000.0;

        System.out.printf("\nCold Average: %.2f ms | Throughput: %.0f M/s\n", avgTimeMs, throughputMps);
    }

    public static void measureWarmPerformance(String name, float[] data,
                                            java.util.function.Function<float[], Double> impl,
                                            int warmupIterations, int benchmarkIterations) {
        System.out.println("\n=== " + name + " Warm Performance (After Warm-up) ===");

        // Warm-up phase
        System.out.println("Warming up...");
        for (int i = 0; i < warmupIterations; i++) {
            impl.apply(data);
            if (i % 5 == 0) {
                System.out.printf("Warm-up iteration %d/%d\n", i + 1, warmupIterations);
            }
        }

        // Benchmark phase
        System.out.println("Benchmarking after warm-up...");
        long totalTime = 0;
        double result = 0.0;

        for (int i = 0; i < benchmarkIterations; i++) {
            long startTime = System.nanoTime();
            result = impl.apply(data);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            totalTime += duration;

            System.out.printf("Warm run %d: %.2f ms | Result: $%.0f\n",
                            i + 1, duration / 1_000_000.0, result);
        }

        double avgTimeMs = (totalTime / (double) benchmarkIterations) / 1_000_000.0;
        double throughputMps = (data.length / (avgTimeMs / 1000.0)) / 1_000_000.0;

        System.out.printf("\nWarm Average: %.2f ms | Throughput: %.0f M/s\n", avgTimeMs, throughputMps);
    }

    public static void main(String[] args) {
        int dataSize = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_SIZE;

        System.out.println("=== Java 17 Vector API: Warm-up Impact Analysis ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Vector Species: " + SPECIES);
        System.out.println("Vector Length: " + SPECIES.length());
        System.out.println("Dataset: " + String.format("%,d", dataSize) + " records (seed=" + FIXED_SEED + ")");
        System.out.println("Memory: ~" + (dataSize * 4 / 1024 / 1024) + " MB");

        System.out.println("\nGenerating controlled dataset...");
        float[] salaries = generateSalaryData(dataSize, FIXED_SEED);

        // Create separate JVM instances by running this benchmark multiple times
        System.out.println("\n" + "=".repeat(80));
        System.out.println("WARM-UP IMPACT COMPARISON");
        System.out.println("=".repeat(80));

        // Measure scalar performance (baseline - should be consistent)
        measureColdPerformance("Scalar", salaries, WarmupBenchmark::sumSalariesScalar, 5);
        measureWarmPerformance("Scalar", salaries, WarmupBenchmark::sumSalariesScalar, 20, 5);

        System.out.println("\n" + "-".repeat(80));

        // Measure Vector API performance - dramatic difference expected
        measureColdPerformance("Vector API", salaries, WarmupBenchmark::sumSalariesVector, 5);
        measureWarmPerformance("Vector API", salaries, WarmupBenchmark::sumSalariesVector, 20, 5);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ANALYSIS");
        System.out.println("=".repeat(80));
        System.out.println("Expected Results:");
        System.out.println("- Scalar: Minimal difference between cold and warm performance");
        System.out.println("- Vector API: MASSIVE difference (potentially 100x+ slower when cold)");
        System.out.println("- This demonstrates why Vector API requires warm-up in production");
        System.out.println("\nNote: True cold-start impact is even more dramatic in fresh JVM instances.");
        System.out.println("For maximum impact, restart the JVM between measurements.");
    }
}
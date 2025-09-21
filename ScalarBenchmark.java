import java.util.Random;

/**
 * Scalar Implementation Benchmark - Works on all Java versions
 *
 * Pure scalar for-loop implementation for baseline comparison.
 * Compile: javac ScalarBenchmark.java
 * Run: java ScalarBenchmark [size]
 */
public class ScalarBenchmark {

    private static final int DEFAULT_SIZE = 100_000_000;
    private static final int WARMUP_ITERATIONS = 10;
    private static final int BENCHMARK_ITERATIONS = 10;
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

    public static void main(String[] args) {
        int dataSize = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_SIZE;

        System.out.println("=== Scalar Benchmark ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Dataset: " + String.format("%,d", dataSize) + " records (seed=" + FIXED_SEED + ")");
        System.out.println("Memory: ~" + (dataSize * 4 / 1024 / 1024) + " MB\n");

        System.out.println("Generating controlled dataset...");
        float[] salaries = generateSalaryData(dataSize, FIXED_SEED);

        // Warm-up
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            sumSalariesScalar(salaries);
        }

        // Benchmark
        System.out.println("Running benchmark...");
        long totalTime = 0;
        double result = 0.0;

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            result = sumSalariesScalar(salaries);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        double averageTimeMs = (totalTime / (double) BENCHMARK_ITERATIONS) / 1_000_000.0;
        double throughputMps = (dataSize / (averageTimeMs / 1000.0)) / 1_000_000.0;

        System.out.println("\n============================================================");
        System.out.println("SCALAR RESULTS");
        System.out.println("============================================================");
        System.out.printf("Time: %.2f ms\n", averageTimeMs);
        System.out.printf("Result: $%.0f\n", result);
        System.out.printf("Throughput: %.0f M records/sec\n", throughputMps);
        System.out.println("============================================================");
    }
}
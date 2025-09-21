import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;
import java.util.Random;

/**
 * Unrolled Vector API Implementation - Requires Java 17+
 *
 * Loop-unrolled SIMD implementation using Vector API for maximum performance.
 * Compile: javac --add-modules jdk.incubator.vector VectorUnrolled.java
 * Run: java --add-modules jdk.incubator.vector VectorUnrolled [size]
 */
public class VectorUnrolled {

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
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

    public static double sumSalariesVectorUnrolled(float[] salaries) {
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

        return vectorSum + scalarSum;
    }

    public static void main(String[] args) {
        int dataSize = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_SIZE;

        System.out.println("=== Vector API Unrolled Benchmark ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Vector Species: " + SPECIES);
        System.out.println("Vector Length: " + SPECIES.length());
        System.out.println("Dataset: " + String.format("%,d", dataSize) + " records (seed=" + FIXED_SEED + ")");
        System.out.println("Memory: ~" + (dataSize * 4 / 1024 / 1024) + " MB\n");

        System.out.println("Generating controlled dataset...");
        float[] salaries = generateSalaryData(dataSize, FIXED_SEED);

        // Warm-up
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            sumSalariesVectorUnrolled(salaries);
        }

        // Benchmark
        System.out.println("Running benchmark...");
        long totalTime = 0;
        double result = 0.0;

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            result = sumSalariesVectorUnrolled(salaries);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        double averageTimeMs = (totalTime / (double) BENCHMARK_ITERATIONS) / 1_000_000.0;
        double throughputMps = (dataSize / (averageTimeMs / 1000.0)) / 1_000_000.0;

        System.out.println("\n============================================================");
        System.out.println("VECTOR UNROLLED RESULTS");
        System.out.println("============================================================");
        System.out.printf("Time: %.2f ms\n", averageTimeMs);
        System.out.printf("Result: $%.0f\n", result);
        System.out.printf("Throughput: %.0f M records/sec\n", throughputMps);
        System.out.println("============================================================");
    }
}
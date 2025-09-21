import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;
import java.util.Random;

public class ControlledBenchmark {

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    // Generate IDENTICAL dataset with fixed seed
    public static float[] generateFixedSalaryData(int size) {
        float[] salaries = new float[size];
        Random random = new Random(12345); // Fixed seed for reproducibility
        for (int i = 0; i < size; i++) {
            salaries[i] = 30000.0f + random.nextFloat() * 120000.0f;
        }
        return salaries;
    }

    // Java 8 style scalar implementation
    public static double sumSalariesScalar(float[] salaries) {
        double sum = 0.0;
        for (int i = 0; i < salaries.length; i++) {
            sum += salaries[i];
        }
        return sum;
    }

    // Vector API implementation
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

        // Handle remaining elements
        double scalarSum = 0.0;
        for (; i < salaries.length; i++) {
            scalarSum += salaries[i];
        }

        return vectorSum + scalarSum;
    }

    // Unrolled Vector API implementation
    public static double sumSalariesVectorUnrolled(float[] salaries) {
        int vectorLength = SPECIES.length();
        int upperBound = SPECIES.loopBound(salaries.length);

        FloatVector sum1 = FloatVector.zero(SPECIES);
        FloatVector sum2 = FloatVector.zero(SPECIES);
        FloatVector sum3 = FloatVector.zero(SPECIES);
        FloatVector sum4 = FloatVector.zero(SPECIES);

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

        // Process remaining vectors
        for (; i < upperBound; i += vectorLength) {
            FloatVector v = FloatVector.fromArray(SPECIES, salaries, i);
            sum1 = sum1.add(v);
        }

        // Combine all sums
        FloatVector totalSum = sum1.add(sum2).add(sum3).add(sum4);
        double vectorSum = totalSum.reduceLanes(VectorOperators.ADD);

        // Handle remaining elements
        double scalarSum = 0.0;
        for (; i < salaries.length; i++) {
            scalarSum += salaries[i];
        }

        return vectorSum + scalarSum;
    }

    public static void benchmark(String name, float[] salaries, java.util.function.Function<float[], Double> implementation) {
        // Warm up
        for (int i = 0; i < 10; i++) {
            implementation.apply(salaries);
        }

        // Measure
        long startTime = System.nanoTime();
        double result = implementation.apply(salaries);
        long endTime = System.nanoTime();

        long duration = endTime - startTime;
        double throughput = (salaries.length / (duration / 1_000_000_000.0)) / 1_000_000;

        System.out.printf("%-20s: %12.2f ms | %8.2f M/sec | Result: $%,.2f%n",
            name,
            duration / 1_000_000.0,
            throughput,
            result);
    }

    public static void main(String[] args) {
        int dataSize = 10_000_000;

        System.out.println("=== CONTROLLED BENCHMARK - IDENTICAL DATASET ===");
        System.out.println("Dataset size: " + String.format("%,d", dataSize) + " records");
        System.out.println("Memory: ~" + (dataSize * 4 / 1024 / 1024) + " MB");
        System.out.println("Java version: " + System.getProperty("java.version"));

        try {
            System.out.println("Vector species: " + SPECIES);
            System.out.println("Vector length: " + SPECIES.length());
        } catch (Exception e) {
            System.out.println("Vector API not available in this Java version");
        }

        System.out.println("-".repeat(70));

        // Generate SAME dataset for all tests
        System.out.println("Generating fixed dataset (seed=12345)...");
        float[] salaries = generateFixedSalaryData(dataSize);

        // Test all implementations on SAME data
        benchmark("Scalar", salaries, ControlledBenchmark::sumSalariesScalar);

        try {
            benchmark("Vector", salaries, ControlledBenchmark::sumSalariesVector);
            benchmark("Vector Unrolled", salaries, ControlledBenchmark::sumSalariesVectorUnrolled);
        } catch (Exception e) {
            System.out.println("Vector API implementations not available: " + e.getMessage());
        }

        System.out.println("=".repeat(70));

        // Validate correctness
        System.out.println("\nCORRECTNESS VALIDATION:");
        double scalarResult = sumSalariesScalar(salaries);
        System.out.printf("Scalar result:    $%,.2f%n", scalarResult);

        try {
            double vectorResult = sumSalariesVector(salaries);
            double unrolledResult = sumSalariesVectorUnrolled(salaries);

            System.out.printf("Vector result:    $%,.2f%n", vectorResult);
            System.out.printf("Unrolled result:  $%,.2f%n", unrolledResult);

            double vectorError = Math.abs(vectorResult - scalarResult) / scalarResult * 100;
            double unrolledError = Math.abs(unrolledResult - scalarResult) / scalarResult * 100;

            System.out.printf("Vector error:     %.6f%%%n", vectorError);
            System.out.printf("Unrolled error:   %.6f%%%n", unrolledError);

            if (vectorError > 0.01 || unrolledError > 0.01) {
                System.out.println("⚠️  WARNING: Significant precision differences detected!");
            } else {
                System.out.println("✅ All results within acceptable precision range");
            }
        } catch (Exception e) {
            System.out.println("Vector validation not available: " + e.getMessage());
        }
    }
}
import java.util.Random;

public class ControlledBenchmarkJava8 {

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

    public static void benchmark(String name, float[] salaries) {
        // Warm up
        for (int i = 0; i < 10; i++) {
            sumSalariesScalar(salaries);
        }

        // Measure
        long startTime = System.nanoTime();
        double result = sumSalariesScalar(salaries);
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
        System.out.println("----------------------------------------------------------------------");

        // Generate SAME dataset
        System.out.println("Generating fixed dataset (seed=12345)...");
        float[] salaries = generateFixedSalaryData(dataSize);

        // Test scalar implementation
        benchmark("Scalar (Java 8)", salaries);

        System.out.println("======================================================================");

        // Show result for comparison
        double scalarResult = sumSalariesScalar(salaries);
        System.out.println("\nCORRECTNESS VALIDATION:");
        System.out.printf("Scalar result: $%,.2f%n", scalarResult);
        System.out.println("This is the REFERENCE result for comparison with Vector API versions");
    }
}
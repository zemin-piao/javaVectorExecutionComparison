import java.util.Arrays;

public class BenchmarkRunner {

    public static float[] generateSalaryData(int size) {
        float[] salaries = new float[size];
        for (int i = 0; i < size; i++) {
            salaries[i] = 30000.0f + (float)(Math.random() * 120000.0f);
        }
        return salaries;
    }

    public static double runJava8Implementation(float[] salaries) {
        double sum = 0.0;
        for (int i = 0; i < salaries.length; i++) {
            sum += salaries[i];
        }
        return sum;
    }

    public static void benchmarkImplementation(String name, float[] salaries, Runnable implementation) {
        // Warm up
        for (int i = 0; i < 5; i++) {
            implementation.run();
        }

        // Benchmark
        long totalTime = 0;
        int iterations = 10;
        double result = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            implementation.run();
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        double averageTime = totalTime / (double) iterations;
        System.out.println(String.format("%-25s: %8.2f ms | %8.2f M records/sec",
            name,
            averageTime / 1_000_000.0,
            (salaries.length / (averageTime / 1_000_000_000.0)) / 1_000_000));
    }

    public static void main(String[] args) {
        int[] dataSizes = {1_000_000, 5_000_000, 10_000_000};

        System.out.println("=== JVM Performance Comparison: Salary Sum ===");
        System.out.println("SQL Equivalent: SELECT SUM(salary) FROM employees;\n");

        for (int dataSize : dataSizes) {
            System.out.println("Dataset size: " + String.format("%,d", dataSize) + " records");
            System.out.println("Memory usage: ~" + (dataSize * 4 / 1024 / 1024) + " MB");
            System.out.println("-".repeat(60));

            float[] salaries = generateSalaryData(dataSize);

            // Java 8 style implementation
            benchmarkImplementation("Java 8 (for-loop)", salaries, () -> {
                runJava8Implementation(salaries);
            });

            System.out.println();

            // Calculate theoretical results for verification
            double java8Result = runJava8Implementation(salaries);
            System.out.println("Sum result: $" + String.format("%,.2f", java8Result));
            System.out.println("Average salary: $" + String.format("%,.2f", java8Result / dataSize));
            System.out.println("=".repeat(60) + "\n");
        }

        System.out.println("Instructions:");
        System.out.println("1. Run with Java 8:  javac SalarySumJava8.java && java SalarySumJava8");
        System.out.println("2. Run with Java 17: javac --add-modules jdk.incubator.vector SalarySumJava17.java && java --add-modules jdk.incubator.vector SalarySumJava17");
        System.out.println("3. Run with Java 25: javac --add-modules jdk.incubator.vector SalarySumJava25.java && java --add-modules jdk.incubator.vector SalarySumJava25");
        System.out.println("\nFor Vector API, ensure you're using a supported architecture (x86_64 with AVX2+ or ARM with NEON)");
    }
}
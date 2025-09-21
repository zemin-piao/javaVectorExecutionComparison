public class SalarySumEquivalent {

    // IDENTICAL algorithm across all Java versions
    public static double sumSalariesScalar(float[] salaries) {
        double sum = 0.0;
        for (int i = 0; i < salaries.length; i++) {
            sum += salaries[i];
        }
        return sum;
    }

    public static float[] generateSalaryData(int size) {
        float[] salaries = new float[size];
        for (int i = 0; i < size; i++) {
            salaries[i] = 30000.0f + (float)(Math.random() * 120000.0f);
        }
        return salaries;
    }

    public static void main(String[] args) {
        int dataSize = 100_000_000;

        System.out.println("Generating " + dataSize + " salary records...");
        float[] salaries = generateSalaryData(dataSize);

        String javaVersion = System.getProperty("java.version");
        System.out.println("Java Version: " + javaVersion);
        System.out.println("Implementation: IDENTICAL scalar algorithm");

        // Warm up
        for (int i = 0; i < 5; i++) {
            sumSalariesScalar(salaries);
        }

        long startTime = System.nanoTime();
        double totalSalary = sumSalariesScalar(salaries);
        long endTime = System.nanoTime();

        long duration = endTime - startTime;

        System.out.println("Total salary sum: $" + String.format("%.2f", totalSalary));
        System.out.println("Execution time: " + (duration / 1_000_000.0) + " ms");
        System.out.println("Throughput: " + String.format("%.2f", (dataSize / (duration / 1_000_000_000.0)) / 1_000_000) + " million records/sec");
    }
}
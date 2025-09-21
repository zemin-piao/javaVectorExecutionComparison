public class SalarySumJava8 {

    public static double sumSalaries(float[] salaries) {
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

        System.out.println("Java 8 - Simple for-loop implementation");

        long startTime = System.nanoTime();
        double totalSalary = sumSalaries(salaries);
        long endTime = System.nanoTime();

        long duration = endTime - startTime;

        System.out.println("Total salary sum: $" + String.format("%.2f", totalSalary));
        System.out.println("Execution time: " + (duration / 1_000_000.0) + " ms");
        System.out.println("Throughput: " + String.format("%.2f", (dataSize / (duration / 1_000_000_000.0)) / 1_000_000) + " million records/sec");
    }
}
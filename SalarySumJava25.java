import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;

public class SalarySumJava25 {

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    public static double sumSalariesVector(float[] salaries) {
        int vectorLength = SPECIES.length();
        int upperBound = SPECIES.loopBound(salaries.length);

        FloatVector sum = FloatVector.zero(SPECIES);

        // Process arrays in chunks using Vector API with Java 25 optimizations
        int i = 0;
        for (; i < upperBound; i += vectorLength) {
            FloatVector v = FloatVector.fromArray(SPECIES, salaries, i);
            sum = sum.add(v);
        }

        // Sum all vector lanes - Java 25 may have better reduction optimizations
        double vectorSum = sum.reduceLanes(VectorOperators.ADD);

        // Handle remaining elements (scalar tail)
        double scalarSum = 0.0;
        for (; i < salaries.length; i++) {
            scalarSum += salaries[i];
        }

        return vectorSum + scalarSum;
    }

    public static double sumSalariesVectorUnrolled(float[] salaries) {
        int vectorLength = SPECIES.length();
        int upperBound = SPECIES.loopBound(salaries.length);

        FloatVector sum1 = FloatVector.zero(SPECIES);
        FloatVector sum2 = FloatVector.zero(SPECIES);
        FloatVector sum3 = FloatVector.zero(SPECIES);
        FloatVector sum4 = FloatVector.zero(SPECIES);

        // Process with loop unrolling for better parallelism
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

        // Handle remaining elements (scalar tail)
        double scalarSum = 0.0;
        for (; i < salaries.length; i++) {
            scalarSum += salaries[i];
        }

        return vectorSum + scalarSum;
    }

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

        System.out.println("Vector species: " + SPECIES);
        System.out.println("Vector length: " + SPECIES.length());

        // Warm up JVM
        for (int i = 0; i < 5; i++) {
            sumSalariesVector(salaries);
            sumSalariesVectorUnrolled(salaries);
            sumSalariesScalar(salaries);
        }

        System.out.println("\nJava 25 - Vector API implementation");
        long startTime = System.nanoTime();
        double totalSalaryVector = sumSalariesVector(salaries);
        long endTime = System.nanoTime();
        long durationVector = endTime - startTime;

        System.out.println("\nJava 25 - Vector API with unrolling");
        startTime = System.nanoTime();
        double totalSalaryUnrolled = sumSalariesVectorUnrolled(salaries);
        endTime = System.nanoTime();
        long durationUnrolled = endTime - startTime;

        System.out.println("\nJava 25 - Scalar implementation");
        startTime = System.nanoTime();
        double totalSalaryScalar = sumSalariesScalar(salaries);
        endTime = System.nanoTime();
        long durationScalar = endTime - startTime;

        System.out.println("\n=== Results ===");
        System.out.println("Vector result: $" + String.format("%.2f", totalSalaryVector));
        System.out.println("Unrolled result: $" + String.format("%.2f", totalSalaryUnrolled));
        System.out.println("Scalar result: $" + String.format("%.2f", totalSalaryScalar));
        System.out.println("Vector time: " + (durationVector / 1_000_000.0) + " ms");
        System.out.println("Unrolled time: " + (durationUnrolled / 1_000_000.0) + " ms");
        System.out.println("Scalar time: " + (durationScalar / 1_000_000.0) + " ms");
        System.out.println("Vector speedup: " + String.format("%.2fx", (double)durationScalar / durationVector));
        System.out.println("Unrolled speedup: " + String.format("%.2fx", (double)durationScalar / durationUnrolled));
        System.out.println("Vector throughput: " + String.format("%.2f", (dataSize / (durationVector / 1_000_000_000.0)) / 1_000_000) + " million records/sec");
        System.out.println("Unrolled throughput: " + String.format("%.2f", (dataSize / (durationUnrolled / 1_000_000_000.0)) / 1_000_000) + " million records/sec");
    }
}
import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class Strassen extends RecursiveTask<int[][]> {
    private int[][] A;
    private int[][] B;
    private int size;

    public Strassen(int[][] A, int[][] B, int size) {
        this.A = A;
        this.B = B;
        this.size = size;
    }

    @Override
    protected int[][] compute() {
        if (size > 1) { // Utiliser l'algorithme de Strassen pour toutes les tailles de matrices
            int newSize = size / 2;

            int[][] A11 = new int[newSize][newSize];
            int[][] A12 = new int[newSize][newSize];
            int[][] A21 = new int[newSize][newSize];
            int[][] A22 = new int[newSize][newSize];
            int[][] B11 = new int[newSize][newSize];
            int[][] B12 = new int[newSize][newSize];
            int[][] B21 = new int[newSize][newSize];
            int[][] B22 = new int[newSize][newSize];

            splitMatrix(A, A11, A12, A21, A22);
            splitMatrix(B, B11, B12, B21, B22);

            Strassen p1 = new Strassen(sum(A11, A22), sum(B11, B22), newSize);
            Strassen p2 = new Strassen(sum(A21, A22), B11, newSize);
            Strassen p3 = new Strassen(A11, subtract(B12, B22), newSize);
            Strassen p4 = new Strassen(A22, subtract(B21, B11), newSize);
            Strassen p5 = new Strassen(sum(A11, A12), B22, newSize);
            Strassen p6 = new Strassen(subtract(A21, A11), sum(B11, B12), newSize);
            Strassen p7 = new Strassen(subtract(A12, A22), sum(B21, B22), newSize);

            p1.fork();
            p2.fork();
            p3.fork();
            p4.fork();
            p5.fork();
            p6.fork();
            p7.fork();

            int[][] C11 = subtract(sum(p1.join(), p4.join()), sum(p5.join(), p7.join()));
            int[][] C12 = sum(p3.join(), p5.join());
            int[][] C21 = sum(p2.join(), p4.join());
            int[][] C22 = subtract(sum(p1.join(), p3.join()), sum(p2.join(), p6.join()));

            return collectMatrix(C11, C12, C21, C22);
        } else {
            // Utiliser l'algorithme de multiplication de matrices standard pour les petites tailles
            return multiplyMatrix(A, B);
        }
    }

    private int[][] multiplyMatrix(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                for (int k = 0; k < n; k++)
                    C[i][j] += A[i][k] * B[k][j];

        return C;
    }

    private void splitMatrix(int[][] P, int[][] C11, int[][] C12, int[][] C21, int[][] C22) {
        int newSize = P.length;

        for (int i = 0; i < newSize; i++)
            for (int j = 0; j < newSize; j++) {
                C11[i][j] = P[i][j];
                C12[i][j] = P[i][j + newSize];
                C21[i][j] = P[i + newSize][j];
                C22[i][j] = P[i + newSize][j + newSize];
            }
    }

    private int[][] collectMatrix(int[][] C11, int[][] C12, int[][] C21, int[][] C22) {
        int newSize = C11.length * 2;
        int[][] result = new int[newSize][newSize];

        for (int i = 0; i < newSize / 2; i++)
            for (int j = 0; j < newSize / 2; j++) {
                result[i][j] = C11[i][j];
                result[i][j + newSize / 2] = C12[i][j];
                result[i + newSize / 2][j] = C21[i][j];
                result[i + newSize / 2][j + newSize / 2] = C22[i][j];
            }

        return result;
    }

    private int[][] sum(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] + B[i][j];

        return C;
    }

    private int[][] subtract(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] - B[i][j];

        return C;
    }



    public static int[][] strassenMatrixMultiplication(int[][] A, int[][] B) {
        int n = A.length;

        if (n != A[0].length || n != B.length || n != B[0].length || (n & (n - 1)) != 0) {
            throw new IllegalArgumentException("Les dimensions des matrices doivent être des puissances de 2 et carrées.");
        }

        return new ForkJoinPool().invoke(new Strassen(A, B, n));
    }


    public static void main(String[] args) {
        Random random = new Random();
        int matrixSize = 2;

        int[][] A = generateRandomMatrix(matrixSize, random);
        int[][] B = generateRandomMatrix(matrixSize, random);

        System.out.println("Matrice A :");
        printMatrix(A);

        System.out.println("Matrice B :");
        printMatrix(B);

        long startTime = System.nanoTime();

        int[][] result = strassenMatrixMultiplication(A, B);

        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;

        System.out.println("Résultat de la multiplication :");
        printMatrix(result);

        System.out.println("Temps d'exécution : " + executionTime + " millisecondes");
    }

    public static int[][] generateRandomMatrix(int size, Random random) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                matrix[i][j] = random.nextInt(10);
        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}

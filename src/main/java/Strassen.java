import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

/**
 * Classe permettant mettant en place l'algorithme de Strassen
 */
public class Strassen extends RecursiveTask<int[][]> {
    /**
     * Définition de la première matrice
     */
    private final int[][] A;  // Matrice A
    /**
     * Définition de la deuxième matrice
     */
    private final int[][] B;  // Matrice B
    /**
     * Définition de la taille de ces matrices
     */
    private final int size;   // Taille des matrices

    /**
     * Constructeur de classe
     * @param A Matrice A
     * @param B Matrice B
     * @param size Taille des matrices
     */
    public Strassen(int[][] A, int[][] B, int size) {
        this.A = A;
        this.B = B;
        this.size = size;
    }

    @Override
    protected int[][] compute() {
        if (size > 1) { // Utiliser l'algorithme de Strassen pour toutes les tailles de matrices
            int newSize = size / 2;

            // On divise A en 4 matrices
            int[][] A11 = new int[newSize][newSize];
            int[][] A12 = new int[newSize][newSize];
            int[][] A21 = new int[newSize][newSize];
            int[][] A22 = new int[newSize][newSize];

            // On divise B en 4 matrices
            int[][] B11 = new int[newSize][newSize];
            int[][] B12 = new int[newSize][newSize];
            int[][] B21 = new int[newSize][newSize];
            int[][] B22 = new int[newSize][newSize];

            // On remplit les matrices crées précedemment
            splitMatrix(A, A11, A12, A21, A22, newSize);
            splitMatrix(B, B11, B12, B21, B22, newSize);

            // Mise en place de la récursivité
            Strassen p1 = new Strassen(sum(A11, A22), sum(B11, B22), newSize);
            Strassen p2 = new Strassen(sum(A21, A22), B11, newSize);
            Strassen p3 = new Strassen(A11, subtract(B12, B22), newSize);
            Strassen p4 = new Strassen(A22, subtract(B21, B11), newSize);
            Strassen p5 = new Strassen(sum(A11, A12), B22, newSize);
            Strassen p6 = new Strassen(subtract(A21, A11), sum(B11, B12), newSize);
            Strassen p7 = new Strassen(subtract(A12, A22), sum(B21, B22), newSize);

            // Calculs en parallèle
            p1.fork();
            p2.fork();
            p3.fork();
            p4.fork();
            p5.fork();
            p6.fork();
            p7.fork();

            // Remplissage de la matrice résultat C
            int[][] C11 = subtract(sum(sum(p1.join(), p4.join()), p7.join()), p5.join());
            int[][] C12 = sum(p3.join(), p5.join());
            int[][] C21 = sum(p2.join(), p4.join());
            int[][] C22 = subtract(sum(sum(p1.join(), p3.join()), p6.join()), p2.join());

            return collectMatrix(C11, C12, C21, C22, newSize);
        } else {
            // Si matrice donnée de taille 1, multiplication normale de matrice
            int[][] C = new int[1][1];
            C[0][0] = A[0][0] * B[0][0];
            return C;
        }
    }

    /**
     * Fonction permettant de divisier la matrice donnée en entrée dans 4 sous-matrices données elles même en paramètre
     * @param P Matrice à diviser
     * @param C11 Première sous-matrice
     * @param C12 Deuxième sous-matrice
     * @param C21 Troisième sous-matrice
     * @param C22 Quatrième sous-matrice
     * @param newSize Taille des sous-matrices
     */
    private void splitMatrix(int[][] P, int[][] C11, int[][] C12, int[][] C21, int[][] C22, int newSize) {

        for (int i = 0; i < newSize; i++)
            for (int j = 0; j < newSize; j++) {
                C11[i][j] = P[i][j];
                C12[i][j] = P[i][j + newSize];
                C21[i][j] = P[i + newSize][j];
                C22[i][j] = P[i + newSize][j + newSize];
            }
    }

    /**
     * Fonction permettant de réunir les sous-matrices en une seule matrice
     * @param C11 Première matrice (en haut à gauche)
     * @param C12 Deuxième matrice (en haut à droite)
     * @param C21 Troisième matrice (en bas à gauche)
     * @param C22 Quatrième matrice (en bas à droite)
     * @param oldSize Taille des matrices
     * @return Super-matrice résultat de la fusion de ces quatre matrices
     */
    private int[][] collectMatrix(int[][] C11, int[][] C12, int[][] C21, int[][] C22, int oldSize) {
        int newSize = oldSize * 2;
        int[][] result = new int[newSize][newSize];

        for (int i = 0; i < oldSize; i++)
            for (int j = 0; j < oldSize; j++) {
                result[i][j] = C11[i][j];
                result[i][j + oldSize] = C12[i][j];
                result[i + oldSize][j] = C21[i][j];
                result[i + oldSize][j + oldSize] = C22[i][j];
            }

        return result;
    }

    /**
     * Fonction permettant d'effectuer la somme de deux matrices
     * @param A Première matrice
     * @param B Deuxième matrice
     * @return Matrice stockant le résultat de l'addition
     */
    private int[][] sum(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] + B[i][j];

        return C;
    }

    /**
     * Fonction permettant d'effectuer la soustraction de deux matrices
     * @param A Première matrice
     * @param B Deuxième matrice
     * @return Matrice stockant le résultat de la soustraction
     */
    private int[][] subtract(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] - B[i][j];

        return C;
    }


    /**
     * Fonction permettant de vérifier la possibilité d'usage de l'algorithme de Strassen
     * @param A Première matrice à multiplier
     * @param B Deuxième matrice à multiplier
     * @return Usage de l'algorithme (si usage possible)
     */
    public static int[][] strassenMatrixMultiplication(int[][] A, int[][] B) {
        int n = A.length;

        if (n != A[0].length || n != B.length || n != B[0].length || (n & (n - 1)) != 0) {
            throw new IllegalArgumentException("Les dimensions des matrices doivent être des puissances de 2 et carrées.");
        }

        return new ForkJoinPool().invoke(new Strassen(A, B, n));
    }


    /**
     * Main
     * @param args Arguments
     */
    public static void main(String[] args) {
        Random random = new Random();
        int matrixSize = 8;

        int[][] A = generateRandomMatrix(matrixSize, random);
        int[][] B = generateRandomMatrix(matrixSize, random);

        System.out.println("Matrice A :");
        printMatrix(A);

        System.out.println("Matrice B :");
        printMatrix(B);

        int[][] result = strassenMatrixMultiplication(A, B);

        System.out.println("Résultat de la multiplication :");
        printMatrix(result);
    }

    /**
     * Fonction permettant de générer un matrice aléatoire de taille donnée (en paramètre)
     * @param size Taille de la matrice à créer
     * @param random Randomizer
     * @return Matrice créée
     */
    public static int[][] generateRandomMatrix(int size, Random random) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                matrix[i][j] = random.nextInt(10);
        return matrix;
    }

    /**
     * Fonction permettant d'afficher une matrice
     * @param matrix Matrice à afficher
     */
    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}

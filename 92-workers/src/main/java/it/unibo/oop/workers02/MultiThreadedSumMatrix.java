package it.unibo.oop.workers02;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Multi-Threaded class which implements {@link SumMatrix} interface.
 * 
 */
public class MultiThreadedSumMatrix implements SumMatrix {

    /**
     * Nesting a SumMartrixWorker class in order to limit
     * his visibility inside the outer class
     */
    private class SumMatrixWorker extends Thread {
        /**
         * The matrix to compute in order to get
         * its sum
         */
        private final double[][] matrix;
        private final int starterPosition;
        private final int nElements;
        private double res;

        public SumMatrixWorker(final double[][] matrix, final int starterPosition, final int nElements) {
            final var tmp = matrix.length - starterPosition - nElements;
            this.matrix = matrix;
            this.starterPosition = starterPosition;
            
            if( tmp < 0){
                this.nElements = nElements + tmp; 
            }
            else{
                this.nElements = nElements;
            }
        }

        /**
         * {@inheritDoc}}
         */
        @Override
        public void run() {
            this.res = Arrays.stream(matrix,starterPosition,this.starterPosition + this.nElements)
                .mapToDouble(MathUtilities::sumElementsOfArray)
                .sum();
        }

        /**
         * 
         * @return Getting the result computed by the SumMatrixWorker istance
         */
        public double getResult() {
            return this.res;
        }

    }

    class MathUtilities{
        public static  double sumElementsOfArray(final double[] arr){
            return Arrays.stream(arr).reduce((a,b)-> a + b).getAsDouble();
        }
    }
    /**
     * Number of threads which perform the matrix' sum
     */
    private int nThreads;

    public MultiThreadedSumMatrix(final int nThreads) {
        this.nThreads = nThreads;
    }

    /**
     * This function "synchronize" the current thead with 
     * the Thread istance "target"
     * @param target  The Thread Istance to wait in order to procede
     * 
     */ 
    @SuppressWarnings("PMD.AvoidPrintStackTrace")
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public double sum(double[][] matrix) {
        //  Number of elements that every thread has to compute
        final int size = matrix.length / this.nThreads + matrix.length % this.nThreads;
        return IntStream
            .iterate(0, start -> start + size)
            .limit(this.nThreads)
            .mapToObj(start -> new SumMatrixWorker(matrix, start, size))
            // Start every worker
            .peek(Thread::run)
            // Joining every worker 
            .peek(MultiThreadedSumMatrix::joinUninterruptibly)
            // Getting the result of every worker
            .mapToDouble(SumMatrixWorker::getResult)
            // Summing all the results 
            .sum();
    }

}

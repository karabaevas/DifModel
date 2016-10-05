/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import static java.lang.Math.abs;

/**
 *
 * @author falcone
 */

public class ArrayTools {

    private static final double REL_EPS = 1e-4;
    private static final double ABS_EPS = 1e-4;

    
    private static boolean doubleEquals(double x, double y) {
        if (abs(x - y) < ABS_EPS) {
            return true;
        }
        double relativeError;
        if ( abs(y) > abs(x)) {
            relativeError = abs((x - y) / y);
        } else {
            relativeError = abs((x - y) / x);
        }
        if (relativeError <= REL_EPS) {
            return true;
        }
        //System.out.println( x + " " + y + " " + relativeError );
        return false;

    }

    static public boolean checkSquare(int dim, double[][] array) {
        if (array.length != dim) {
            return false;
        }
        for (int i = 0; i < dim; i++) {
            if (array[i].length != dim) {
                return false;
            }
        }
        return true;
    }
    static public boolean checkDiagonal(double[][] array) {
        final int DIM = array.length;
        if (!checkSquare(DIM, array)) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (  i != j && ! doubleEquals(array[i][j], 0 ) ) {
                    return false;
                }
            }
        }
        return true;
    }
}

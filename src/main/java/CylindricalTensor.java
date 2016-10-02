public class CylindricalTensor  {
    
    private static final int DIM = 3;
    
    private final int SIZE_X;
    private final int SIZE_Y;
    private final int SIZE_Z;
    private final int C_Y;
    private final int C_Z;
    private final double[][][][] TENSOR;
    private final double D_P;
    private final double D_W;
    
    public CylindricalTensor( int sizeX, int sizeY, int sizeZ, int centerY, int centerZ, double[][] tensor ) {
        if (!ArrayTools.checkSquare(DIM, tensor)) {
            throw new IllegalArgumentException("The Tensor must be square and" +
                    "of provided dimension");
        }
        if (!ArrayTools.checkDiagonal( tensor)) {
            throw new IllegalArgumentException("The Tensor must be diagonal.");
        }
        if ( tensor[0][0] != tensor[2][2] ) {
            throw new IllegalArgumentException("Dxx must be equal to Dzz");
        }
        D_W = tensor[1][1];
        D_P = tensor[2][2];
        SIZE_X = sizeX;
        SIZE_Y = sizeY;
        SIZE_Z = sizeZ;
        TENSOR = new double[SIZE_Y][SIZE_Z][DIM][DIM];
        C_Z = centerZ;
        C_Y = centerY;
        for( int y=0; y<SIZE_Y; y++ ) {
            for( int z=0; z<SIZE_Z; z++ ) {
                for( int i=0; i<DIM; i++ ) {
                    for( int j=0; j<DIM; j++ ) {
                        TENSOR[y][z][i][j] = computeTensor( y, z, i, j);
                    }
                }
            }
        }
    }
    

    public double value(int[] pos, int i, int j) {
        return TENSOR[ pos[1] ][ pos[2] ][i][j];
    }
	
	public double[][] values(int[] pos) {
        return TENSOR[ pos[1] ][ pos[2] ];
    }

    public int dimension() {
        return DIM;
    }

   /*             [     2                                   ]
                  [ Dp r          0                0        ]
                  [                                         ]
(%o11)            [             2       2                   ]
                  [   0     Dw z  + Dp y    Dw y z - Dp y z ]
                  [                                         ]
                  [                              2       2  ]
                  [   0    Dw y z - Dp y z   Dp z  + Dw y   ]
*/
    private double computeTensor(int y, int z, int i, int j) {
        final int Z = z - C_Z;
        final int Y = y - C_Y;
        final double R2 =  Y*Y + Z*Z;
        double value;
        if ( i==0 || j==0 ) {
            if ( i == j ) {
                value = D_P;
            } else {
                value = 0;
            }
        } else {
            if( i != j ) {
                value = D_W * Y * Z - D_P * Y * Z;
            } else  {
                if( i== 1 ) {
                    value = D_W * Z * Z + D_P * Y * Y;
                } else {
                    value = D_P * Z * Z + D_W * Y * Y;
                }
            }
            value /= R2;
        }
        return value;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Arrays;

/**
 *
 * @author falcone
 */
public class BasicDomain3D {
    public final static byte SOURCE = 2;
    public final static byte SINK = 0;

    private final int SIZE_X;
    private final int SIZE_Y;
    private final int SIZE_Z;

    private final int[] sizeArray;

    private double[][][] rho; // the conc matrix
    private double[][][] rho_; // a matrix containing futures changes
    
    private byte[][][] boundaries;
    
    private CylindricalTensor tensor;

    public double[][][] getRho_() {
        return rho_;
    }

    private boolean synchro;
    
    public BasicDomain3D( int sizeX, int sizeY, int sizeZ ) {
        SIZE_X = sizeX;
        SIZE_Y = sizeY;
        SIZE_Z = sizeZ;
        sizeArray = new int[] { SIZE_X, SIZE_Y, SIZE_Z };
        rho = new double[SIZE_X][SIZE_Y][SIZE_Z];
        boundaries = new byte[ SIZE_X ][ SIZE_Y ][ SIZE_Z ];
        setSynchronicity( true );
		for (int i = 0; i < SIZE_X; i++) {
			for (int j = 0; j < SIZE_Y; j++) {
				Arrays.fill(boundaries[i][j], (byte)1);
			}
		}
    }

    public int[] getSizesArray() {
        return sizeArray;
    }

    public BasicDomain3D setConcentration(int[] position, double concentration) {
        rho_[ position[0] ][ position[1] ][ position[2] ] = concentration;
        return this;
    }

    public double getConcentration(int[] position) {
        return rho[ position[0] ][ position[1] ][ position[2] ];
    }

    public BasicDomain3D setDiffusionTensor(CylindricalTensor tensor_) {
        if( tensor == null ) {
            tensor = tensor_;
        } else {
            throw new IllegalStateException( "The diffusion tensor was already initialized." );
        }
        return this;
    }

	public double[][] getDiffusionTensor(int[] position) {
        return tensor.values(position);
    }

    public final BasicDomain3D setSynchronicity(boolean synchronicity) {
        synchro = synchronicity;
        if( synchro ) {
            rho_ = new double[SIZE_X][SIZE_Y][SIZE_Z];
        } else {
            rho_ = rho;
        }
        return this;
    }

    public BasicDomain3D update() {
        if( synchro ) {
            double[][][] tmp = rho;
            rho = rho_;
            rho_ = tmp;
        }
        return this;
    }

    public void setConcentrationToDefault() {
        for (int x = 0; x < rho.length; x++) {
            for (int y = 0; y < rho[x].length; y++) {
                for (int z = 0; z < rho[x][y].length; z++) {
                    if (this.boundaries[x][y][z] == SOURCE) {
                        rho[x][y][z] = 1d;
                    } else if (this.boundaries[x][y][z] == SINK) {
                        rho[x][y][z] = 0d;
                    }
                }
            }
        }
    }

    public double[][][] getConcentrationMatrix() {
        return rho;
    }

	public int left(int x) {
        return (x-1+SIZE_X) % SIZE_X;
    }
    public int right(int x) {
        return (x+1) % SIZE_X;
    }
    public int up(int y) {
		return (y+1) % SIZE_Y;
    }
    public int down(int y) {
		return (y-1+SIZE_Y) % SIZE_Y;
    }

	public int rear(int z) {
        return (z-1+SIZE_Z) % SIZE_Z;
    }
    public int front(int z) {
        return (z+1) % SIZE_Z;
    }
	
	public void setBoundaryNodes(byte[][][] nodes) {
		this.boundaries = nodes;
	}

	public boolean trySetDefault(int[] pos) {
		switch (this.boundaries[pos[0]][pos[1]][pos[2]]) {
			case SOURCE:
				rho_[ pos[0] ][ pos[1] ][ pos[2] ] = 1d;
				return true;
			case SINK:
				rho_[ pos[0] ][ pos[1] ][ pos[2] ] = 0d;
				return true;
			default:
				return false;
		}
	}
}

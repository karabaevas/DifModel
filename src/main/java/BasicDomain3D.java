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
    private final int[] SIZE;
    
    private double[][][] rho; // the conc matrix
    private double[][][] rho_; // a matrix containing futures changes
    
    private byte[][][] boundaries;
    
    private CylindricalTensor tensor;

    public double[][][] getRho() {
        return rho;
    }

    public void setRho(double[][][] rho) {
        this.rho = rho;
    }

    public double[][][] getRho_() {
        return rho_;
    }

    public void setRho_(double[][][] rho_) {
        this.rho_ = rho_;
    }

    private boolean synchro;
    
    public BasicDomain3D( int sizeX, int sizeY, int sizeZ ) {
        SIZE_X = sizeX;
        SIZE_Y = sizeY;
        SIZE_Z = sizeZ;
        SIZE = new int[] { SIZE_X, SIZE_Y, SIZE_Z };
        rho = new double[SIZE_X][SIZE_Y][SIZE_Z];
        setSynchronicity( true );
        boundaries = new byte[ SIZE_X ][ SIZE_Y ][ SIZE_Z ];
		for (int i = 0; i < SIZE_X; i++) {
			for (int j = 0; j < SIZE_Y; j++) {
				Arrays.fill(boundaries[i][j], (byte)1);
			}
		}

    }

    public int[] size() {
        return SIZE;
    }

    public int dim() {
        return 3;
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
    
    public double getDiffusionTensor(int[] position, int i, int j) {
        return tensor.value(position, i, j);
    }
	
	public double[][] getDiffusionTensor(int[] position) {
        return tensor.values(position);
    }

    public BasicDomain3D setBoundaryNode(int[] position, byte type) {
        boundaries[ position[0] ][ position[1] ][ position[2] ] = type;
		trySetDefault(position);
        return this;
    }

    public int getBoundaryNode(int[] position) {
        return boundaries[ position[0] ][ position[1] ][ position[2] ];
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
    
    public boolean isSynchronous() {
       return synchro;
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
//				    if (this.boundaries[x][y][0] == Domain.SOURCE) {
//						rho[x][y][z] = 1d;
//					} else if (this.boundaries[x][y][rho[x][y].length-1] == Domain.SINK) {
//						rho[x][y][z] = 0d;
//					}
                    if (z == 0){
                        rho[x][y][z] = 1d;
                    }
                    else if (z == rho[x][y].length-1){
                        rho[x][y][z] = 0d;
                    }
				}
			}
		}
	}

    public double[][][] getConcentrationMatrix() {
        return rho;
    }

    public int[] left(int[] pos) {
        pos[0] = (pos[0]-1+SIZE_X) % SIZE_X;
		return pos;
    }
    public int[] right(int[] pos) {
        pos[0] = (pos[0]+1) % SIZE_X;
		return pos;
    }
    public int[] up(int[] pos) {
		pos[1] = (pos[1]+1) % SIZE_Y;
		return pos;
    }
    public int[] down(int[] pos) {
		pos[1] = (pos[1]-1+SIZE_Y) % SIZE_Y;
		return pos;
    }

	public int[] rear(int[] pos) {
        pos[2] = (pos[2]-1+SIZE_Z) % SIZE_Z;
		return pos;
    }
    public int[] front(int[] pos) {
        pos[2] = (pos[2]+1) % SIZE_Z;
		return pos;
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
	
	public int[] copy(int[] pos) {
		return new int[] { pos[0], pos[1], pos[2] };
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joris borgdorff
 */
public class Jacobi3DThreaded {

	private Jacobi3DThread[] threads;
	private int lastSize;
	private boolean stop;
	
	public Jacobi3DThreaded(int numThreads) {
		super();
		this.stop = true;
		this.threads = new Jacobi3DThread[numThreads];
		this.lastSize = 0;
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Jacobi3DThread(i);
		}				
	}

	public void start() {
		this.stop = false;
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
	}
	
	public void stop() {
		this.stop = true;
		for (int i = 0; i < threads.length; i++) {
			threads[i].reset();
		}
	}

    public double step(BasicDomain3D domain) {
		BasicDomain3D dom = domain;
		divideDomain(dom);
		try {
			for (int i = 0; i < threads.length; i++) {
				threads[i].step(dom);
			}

			double maxDiff = 0d;
			for (int i = 0; i < threads.length; i++) {
				//notifyAll тут. для !КАЖДОГО! потока
				maxDiff = Math.max(maxDiff, threads[i].getLastDiff());
			}

			dom.update();

			return maxDiff;
		} catch (InterruptedException ex) {
			Logger.getLogger(Jacobi3DThreaded.class.getName()).log(Level.SEVERE, "Solving of diffusion interrupted.", ex);
			return 0d;
		}
	}
	
	private void divideDomain(BasicDomain3D dom) {
		if (lastSize != dom.getSizesArray()[0]) {
			lastSize = dom.getSizesArray()[0];
			
			// Rounded down
			int colSize = lastSize / threads.length;
			int remainder = lastSize % threads.length;

			int from = 0, to;
			// Spread the 'remainder' out to the first threads
			int len = Math.min(threads.length, lastSize);
			for (int i = 0; i < len; i++) {
				to = (i < remainder ? from + colSize + 1 : from + colSize);
				threads[i].setInterval(from, to);
				from = to;
			}
			// Idle threads, no columns left
			for (int i = len; i < threads.length; i++) {
				threads[i].setInterval(0, 0);
			}
		}
	}

	private class Jacobi3DThread extends Thread {
		private final static int X = 0, Y = 1, Z = 2;
	
		private double lastDiff;
		private int from, to;
		private BasicDomain3D dom;
		
		Jacobi3DThread(int i) {
			super("DiffusionSolverThread-"+i);
			dom = null;
			lastDiff = 0;
			from = -1;
			to = 0;
		}
		
		@Override
		public void run() {
			while (!stop) {
				synchronized (this) {
					while (this.dom == null) {
						try {
							wait();
						} catch (InterruptedException ex) {
							Logger.getLogger(Jacobi3DThreaded.class.getName()).log(Level.SEVERE, "Solving thread interrupted.", ex);
						}
						if (stop) return;
					}
					this.lastDiff = step();
					this.reset();
				}
			}
		}
		
		private synchronized void reset() {
			this.dom = null;
			this.notifyAll();
		}
		
		public synchronized void step(BasicDomain3D domain) throws InterruptedException {
			// Wait for the previous computation to be finished.
			while (!stop && this.dom != null) {
				wait();
			}
			this.dom = domain;
			
			// We have to stop before the next step can be made.
			if (stop) {
				throw new InterruptedException("Could not finish computation.");
			}
			
			this.notifyAll();
		}

		public synchronized double getLastDiff() throws InterruptedException {
			// Wait for the last computation to be finished
			while (!stop && this.dom != null) {
				wait();
			}
			// No result was obtained but we have to stop.
			if (stop) {
				throw new InterruptedException("Could not finish computation.");
			}
			this.notifyAll();
			return this.lastDiff;
		}
		
		private double step() {
			double maxDiff = 0d;
			int size_y = dom.getSizesArray()[Y];
			int size_z = dom.getSizesArray()[Z];
			
			int[] pos = new int[3];
			for (pos[0] = this.from; pos[0] < this.to; pos[0]++) {
				for (pos[1] = 0; pos[1] < size_y; pos[1]++) {
					for (pos[2] = 0; pos[2] < size_z; pos[2]++) {
						if (!dom.trySetDefault(pos)) {
							final Double newConcentration = Math.min(1d, Math.max(0d, computeConcentration(dom, pos)));
							if ("NaN".equalsIgnoreCase(newConcentration.toString())){
								System.out.println(pos[0]+" "+ pos[1]+" "+pos[2]+" "+newConcentration);
							}
							final double diff = newConcentration - dom.getConcentration(pos);
							dom.setConcentration(pos, newConcentration);
							if (diff > maxDiff) {
								maxDiff = diff;
							}
						}
					}
				}
			}
			return maxDiff;
		}
		
		public synchronized void setInterval(int from_, int to_) {
			this.from = from_;
			this.to = to_;
		}
		
		private double computeConcentration(BasicDomain3D domain, int[] pos) {
			double[][] tensor = domain.getDiffusionTensor(pos);
			double[][][] conc = domain.getConcentrationMatrix();
			
			int x = pos[X], y = pos[Y], z = pos[Z];
			int left = domain.left(x);
			int right = domain.right(x);
			int up = domain.up(y);
			int down = domain.down(y);
			int rear = domain.rear(z);
			int front = domain.front(z);
			
			double dii = conc[left][y][z] + conc[right][y][z];
			double djj = conc[x][up][z] + conc[x][down][z];
			double dkk = conc[x][y][front] + conc[x][y][rear];

			double dij = conc[right][up][z]    + conc[left][down][z] - conc[left][up][z]    - conc[right][down][z]; 
			double dik = conc[right][y][front] + conc[left][y][rear] - conc[right][y][rear] - conc[left][y][front];
			double djk = conc[x][up][front]    + conc[x][down][rear] - conc[x][down][front] - conc[x][up][rear];

			double div = tensor[X][X] + tensor[Y][Y] + tensor[Z][Z];
//			System.out.println("x " + tensor[X][X]);
//			System.out.println("y" + tensor[Y][Y]);
//			System.out.println("z" + tensor[Z][Z]);
			double diag = tensor[X][X]*dii + tensor[Y][Y]*djj + tensor[Z][Z]*dkk;
			double offdiag =  tensor[X][Y]*dij + tensor[X][Z]*dik + tensor[Y][Z]*djk;
			return (2*diag + offdiag) / (4*div);
		}
	}
}

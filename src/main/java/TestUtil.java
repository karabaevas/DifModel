import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;


public class TestUtil {

    private double total_width = total_width = 3;
    private double length =3;
    private double deltaX = 1;
    private double steadyStateThreshold = 0.0001;
    private int maxIter = 100;
    private int sizeX, sizeY, sizeZ;
    private BasicDomain3D domain;
    private byte[][][] domainStruct;
    private int numThreads = 4;

    static Random random = new Random();


    public void checkCorrectness() {
        sizeX = discretize(length, deltaX);
        sizeY = discretize(total_width, deltaX);
        sizeZ = sizeY;
        domainStruct = initStruct(sizeX, sizeY, sizeZ);

        Instant start = Instant.now();
        SequentialExecution();
        Instant end = Instant.now();
        long seq = Duration.between(start, end).toMillis();
        System.out.println("Sequential time is " + seq);

        start = Instant.now();
        ParallelExecution(numThreads);
        end = Instant.now();
        long par = Duration.between(start, end).toMillis();
        System.out.println("Parallel time is " + par);


        ObjectMapper mapper = new ObjectMapper();
        double[][][] seqResult;
        double[][][] parResult;
        try {
            seqResult = mapper.readValue(new File("file1.json"), double[][][].class);
            parResult = mapper.readValue(new File("file"+numThreads+".json"), double[][][].class);
            System.out.println("same results - " + Arrays.deepEquals(parResult, seqResult));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Speedup is: " + (seq / par));

    }

    public double[][][] ParallelExecution(int numThreads) {
        return testMethod(numThreads);
    }

    public double[][][] SequentialExecution() {
        return testMethod(1);
    }

    static private int discretize(double d, double dx) {
        return (int) Math.ceil(d / dx);
    }


    protected double[][][] testMethod(int numThreads) {
        sizeX = discretize(length, deltaX);
        sizeY = discretize(total_width, deltaX);
        sizeZ = sizeY;

        domain = new BasicDomain3D(sizeX, sizeY, sizeZ);

        double[][] diffMatrix = {
                {10.0 / 7, 0, 0},
                {0, 1. / 7, 0},
                {0, 0, 10.0 / 7}
        };
        CylindricalTensor diffTensor = new CylindricalTensor(sizeX, sizeY, sizeZ, sizeY / 2, sizeZ / 2, diffMatrix);
        domain.setDiffusionTensor(diffTensor);

        Jacobi3DThreaded method = new Jacobi3DThreaded(numThreads);
        SteadyStateSolver ss = new SteadyStateSolver(method, numThreads);


        try {
            //стартуем потоки и..
            method.start();
            //спим ждем нотифай

//            while (domainConduit.hasNext()) {
//            final byte[][][] domainStruct = domainConduit.receive();
            domain.setBoundaryNodes(domainStruct);
//					for (int y = 0; y < domainStruct[printX].length; y++) {
//						for (int z = 0; z < domainStruct[printX][y].length; z++) {
//							System.out.print(domainStruct[printX][y][z]);
//						}
//						System.out.println();
//					}
            //считаем итерации тут
            int it = ss.solve(domain, maxIter, steadyStateThreshold);
//               concentrationConduit.send(domain.getConcentrationMatrix());
//                double[][] slice = domain.getConcentrationMatrix()[printX];
//                ImageWriter iw = new ImageWriter(new PGMFormat(1.0));
//                try {
//                    iw.write(getTmpPath() + "/truc.dat", slice);
//                } catch (Exception e) {
//                    throw new RuntimeException("Slice was not written", e);
//                }
//            }
            //notifyAll тут туу. стоп флаг тру дом = нул
            method.stop();
        } catch (Exception ex) {
        } finally {
            method.stop();
            return domain.getConcentrationMatrix();
        }
    }

//    public byte[][][] initStruct(int x, int y, int z) {
//        byte[][][] struct = new byte[x][y][z];
//        for (int i = 0; i < x; i++) {
//            for (int j = 0; j < y; j++) {
//                byte a = (byte) abs(random.nextInt() % 127 + 1);
//                Arrays.fill(struct[i][j], a);
//            }
//        }
//        return struct;
//    }
    public byte[][][] initStruct(int x, int y, int z) {
        byte[][][] struct = new byte[x][y][z];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                Arrays.fill(struct[i][j], (byte) 1);
            }
        }
        return struct;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author falcone
 */
public class SteadyStateSolver {
    ObjectMapper mapper = new ObjectMapper();
    int numThreads;
    private Jacobi3DThreaded method;

    public SteadyStateSolver(Jacobi3DThreaded method_, int numThreads) {
        method = method_;
        this.numThreads=numThreads;
    }

    public int solve(BasicDomain3D domain, int maxIterations, double epsilon) {
        checkDimensionsAndSynchronicity( domain );

        for (int t = 0; t < maxIterations; t++) {
            double maxChange = method.step(domain);
            if (maxChange < epsilon) {
                try {
                    mapper.writeValue(new File("file"+numThreads+".json"), domain.getRho_());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return t;
            }
        }
        return maxIterations;
    }

    public void solve(BasicDomain3D domain, int maxIterations) {
        checkDimensionsAndSynchronicity( domain );
        for (int t = 0; t < maxIterations; t++) {
            method.step(domain);
        }
    }

    private void checkDimensionsAndSynchronicity(BasicDomain3D domain) {
		domain.setConcentrationToDefault();
    }

}

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
        setConcentrationByDefault( domain );
        int t = 0;
        for (t = 0; t < maxIterations; t++) {
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
        return t;
    }

    private void setConcentrationByDefault(BasicDomain3D domain) {
		domain.setConcentrationToDefault();
    }

}

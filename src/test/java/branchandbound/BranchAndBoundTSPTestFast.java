package branchandbound;

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeFeedback;
import com.github.guillaumederval.javagrading.GradingRunnerWithParametersFactory;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import util.tsp.TSPInstance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(Enclosed.class)
public class BranchAndBoundTSPTestFast {

    public static class TestNotParameterized {
        @Test
        @Grade(value = 1, cpuTimeout = 1000)
        @GradeFeedback(message = "Sorry, TSP not working yet", onFail = true)
        public void readableTestToDebug() {
            // https://pasteboard.co/AWbeWgMG188K.png (use this link to visualize the instance and debug)
            int [] xCoord = new int[] {10,10,20,70,40,60,50};
            int [] yCoord = new int[] {10,30,60,40,10,20,60};
            TSPInstance tsp = new TSPInstance(xCoord,yCoord);
            List<Edge> edges;

            boolean [][] expectedEdges = new boolean[7][7];
            // 0-1, 1-2, 2-6, 6-3, 3-5, 5-4, 4-0
            expectedEdges[0][1] = expectedEdges[1][0] = true;
            expectedEdges[1][2] = expectedEdges[2][1] = true;
            expectedEdges[2][6] = expectedEdges[6][2] = true;
            expectedEdges[6][3] = expectedEdges[3][6] = true;
            expectedEdges[3][5] = expectedEdges[5][3] = true;
            expectedEdges[5][4] = expectedEdges[4][5] = true;
            expectedEdges[4][0] = expectedEdges[0][4] = true;

            OneTreeLowerBound [] algos = new OneTreeLowerBound[] {new SimpleOneTree(), new HeldKarpOneTree()};

            for (OneTreeLowerBound algo: algos) {
                edges = BranchAndBoundTSP.optimize(tsp,algo);
                assertEquals(7, edges.size());
                boolean [][] foundEdges = new boolean[7][7];
                for (Edge e: edges) {
                    foundEdges[e.v1()][e.v2()] = foundEdges[e.v2()][e.v1()] = true;
                }
                for (int i = 0; i < expectedEdges.length; i++) {
                    for (int j = 0; j < expectedEdges.length; j++) {
                        assertEquals(expectedEdges[i][j], foundEdges[i][j]);
                    }
                }
            }
        }


        @Test
        @Grade(value = 1, cpuTimeout = 1000)
        @GradeFeedback(message = "Comparison Two Bound on random tests", onFail = true)
        public void randomTestToDebug() {

            for (int seed = 0; seed < 100; seed++) {

                TSPInstance tsp = new TSPInstance(4, seed, 100);

                List<Edge> edges1 = BranchAndBoundTSP.optimize(tsp, new SimpleOneTree());
                int tot1 = edges1.stream().map(e -> e.cost()).mapToInt(e -> (int) Math.rint(e)).sum();

                List<Edge> edges2 = BranchAndBoundTSP.optimize(tsp, new HeldKarpOneTree());
                int tot2 = edges2.stream().map(e -> e.cost()).mapToInt(e -> (int) Math.rint(e)).sum();
                assertEquals(tot1, tot2);
            }
        }
    }

    public static List<Object[]> readTSPInstance(int size) {
        LinkedList<Object []> coll = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            String name = "data/TSP/instance_"+size+"_"+i+".xml";
            coll.add(new Object[] {name, new TSPInstance(name)});
        }
        return coll;
    }

    public static void testSolvingOptimality(TSPInstance instance, OneTreeLowerBound algo) {
        List<Edge> edges = BranchAndBoundTSP.optimize(instance,algo);
        int objective = instance.objective;
        double tourLength = 0;
        for (Edge e: edges) {
            tourLength += instance.distanceMatrix[e.v1()][e.v2()];
        }
        assertEquals(objective,tourLength, 0.001);
    }

    @RunWith(Parameterized.class)
    @Parameterized.UseParametersRunnerFactory(GradingRunnerWithParametersFactory.class)
    public static class TestParameterized8 {
        final TSPInstance instance;
        public TestParameterized8(String name, TSPInstance instance) {
            this.instance = instance;
        }
        @Parameterized.Parameters(name = "{0}")
        public static Collection<?> data() {
            return readTSPInstance(8);
        }

        @Test
        @Grade(value = 10, cpuTimeout = 2000)
        @GradeFeedback(message = "Sorry, something is wrong cannot find optimum", onFail=true)
        @GradeFeedback(message = "Your solver is too slow", onTimeout=true)
        public void testOneTree() throws Exception {
            testSolvingOptimality(instance, new SimpleOneTree());
        }

        @Test
        @Grade(value = 10, cpuTimeout = 2000)
        @GradeFeedback(message = "Sorry, something is wrong cannot find optimum", onFail=true)
        @GradeFeedback(message = "Your solver is too slow", onTimeout=true)
        public void testHeldKarp() throws Exception {
            testSolvingOptimality(instance, new HeldKarpOneTree());
        }
    }




}

import ilog.concert.*;
import ilog.cplex.*;

public class Solver {
	public void solve(MetroMap map) {
		Station[] stations = map.getStationsArray();
		int n = stations.length;
		int height = 14;
		int margin = 4;

		try {
			// define new model
			IloCplex cplex = new IloCplex();

			// variables
			IloIntVar[] x = cplex.intVarArray(n, 0, Integer.MAX_VALUE);
			IloIntVar[] y = cplex.intVarArray(n, 0, Integer.MAX_VALUE);

			// expressions

			// objective, add minimal distance between stations
	
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				objective.addTerm(x[i], 1);
				objective.addTerm(y[i], 1);
			}

			cplex.addMinimize(objective);

			// constraints
			Utility u = new Utility();
			for (int i = 0; i < n; i++) {
				Station stationA = stations[i];
				for (int j = i + 1; j < n; j++) {
					Station stationB = stations[j];
					if (stationA.getX() >= stationB.getX()) {
						cplex.addGe(x[i], x[j]);
					} else {
						cplex.addLe(x[i], x[j]);
					}

					if (stationA.getY() >= stationB.getY()) {
						cplex.addGe(y[i], y[j]);
					} else {
						cplex.addLe(y[i], y[j]);
					}

					cplex.addGe(
							cplex.sum(cplex.ge(x[i], cplex.sum(x[j], u.getStringWidth(stationB.getName()) + margin)),
									cplex.ge(x[j], cplex.sum(x[i], u.getStringWidth(stationB.getName()) + margin)),
									cplex.ge(y[i], cplex.sum(y[j], height + margin)),
									cplex.ge(y[j], cplex.sum(y[i], height + margin))
									),
							1);
				}
			}

			// solve model
			if (cplex.solve()) {
				System.out.println("obj = " + cplex.getObjValue());
				XMLParser parser = new XMLParser();
				parser.writeImage(map, cplex.getValues(x), cplex.getValues(y));
			} else {
				System.out.println("problem not solved");
			}

			cplex.end();

		} catch (IloException e) {
			e.printStackTrace();
		}
	}
}

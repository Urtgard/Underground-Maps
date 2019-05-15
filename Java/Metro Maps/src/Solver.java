import java.util.Map;

import ilog.concert.*;
import ilog.cplex.*;

public class Solver {
	public void solve(MetroMap map) {
		Station[] stations = map.getStationsArray();
		int n = stations.length;
		int height = 20;
		int margin = 30;

		try {
			// define new model
			IloCplex cplex = new IloCplex();
			cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.07);
			
			// variables
			IloIntVar[] x = cplex.intVarArray(n, 0, Integer.MAX_VALUE);
			IloIntVar[] y = cplex.intVarArray(n, 0, Integer.MAX_VALUE);
			IloNumVar[][] dx = new IloNumVar[n][n] ;
			IloNumVar[][] dy = new IloNumVar[n][n] ;
			for (int i = 0; i < n; i++) {
				dx[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				dy[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
			}
			
			// expressions

			// objective, das ist ein test
	
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				//objective.addTerm(x[i], 1);
				//objective.addTerm(y[i], 1);
				for (int j = 0; j < n; j++) {
					objective.addTerm(dx[i][j], 1);
					objective.addTerm(dy[i][j], 1);
				}

			}

			cplex.addMinimize(objective);

			// constraints
			Utility u = new Utility();
			for (int i = 0; i < n; i++) {
				Station stationA = stations[i];
				for (int j = i + 1; j < n; j++) {
					Station stationB = stations[j];
					cplex.addGe(
							cplex.sum(cplex.ge(x[i], cplex.sum(x[j], u.getStringWidth(stationB.getName()) + margin)),
									cplex.ge(x[j], cplex.sum(x[i], u.getStringWidth(stationA.getName()) + margin)),
									cplex.ge(y[i], cplex.sum(y[j], height + margin)),
									cplex.ge(y[j], cplex.sum(y[i], height + margin))
									),
							1);
					
				}
			}
			
			for (Map.Entry<String, Line> l : map.getLines().entrySet()) {
				Line line = l.getValue();	
				for (int k = 0; k < line.getStations().size() - 1; k++){
					Station stationA = line.getStations().get(k);
					Station stationB = line.getStations().get(k+1);
					int i = -1; int j = -1;
					for (int a = 0; a < stations.length; a++) {
						if (stationA == stations[a]) {
							i = a;
						} else if (stationB == stations[a]) {
							j = a;
						}
						if (i != -1 && j != -1){
							break;
						}
					}
					
					if (stationA.getX() >= stationB.getX()) {
						cplex.addGe(x[i], x[j]);
						cplex.addLe(cplex.diff(x[i], x[j]), dx[i][j]);
					} else {
						cplex.addLe(x[i], x[j]);
						cplex.addLe(cplex.diff(x[j], x[i]), dx[i][j]);
					}

					if (stationA.getY() >= stationB.getY()) {
						cplex.addGe(y[i], y[j]);
						cplex.addLe(cplex.diff(y[i], y[j]), dy[i][j]);
					} else {
						cplex.addLe(y[i], y[j]);
						cplex.addLe(cplex.diff(y[j], y[i]), dy[i][j]);
					}
												
					double m  = (stationA.getY() - stationB.getY())/(stationA.getX() - stationB.getX());		
					if (m >= 0.414 && m <= 2.414) {
						cplex.addEq(cplex.diff(y[i],y[j]),
								cplex.prod(1, cplex.diff(x[i],x[j])));
						/*cplex.addGe(
								cplex.diff(y[i],y[j]),
								cplex.prod(0.8, cplex.diff(x[i],x[j])));
						cplex.addLe(
								cplex.diff(y[i],y[j]),
								cplex.prod(1.2, cplex.diff(x[i],x[j])));*/
					} else if (m >= -2.414 && m <= -0.414) {
						cplex.addEq(cplex.diff(y[i],y[j]),
								cplex.prod(-1, cplex.diff(x[i],x[j])));
						/*cplex.addGe(
							cplex.diff(y[i],y[j]),
							cplex.prod(-1.2, cplex.diff(x[i],x[j])));
						cplex.addLe(
							cplex.diff(y[i],y[j]),
							cplex.prod(-0.8, cplex.diff(x[i],x[j])));*/
					} else if (m > 2.414) {
						cplex.addEq(x[i], x[j]);
						//cplex.addGe(								cplex.diff(y[i],y[j]),								cplex.prod(10, cplex.diff(x[i],x[j])));
					} else if (m < -2.414) {
						cplex.addEq(x[i], x[j]);
						//cplex.addLe(								cplex.diff(y[i],y[j]),								cplex.prod(-10, cplex.diff(x[i],x[j])));
					} else {
						cplex.addEq(y[i], y[j]);
					}
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

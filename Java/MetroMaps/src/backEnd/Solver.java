package backEnd;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import ilog.concert.*;
import ilog.cplex.*;

public class Solver {
	private int n;
	private int M = 100000;
	private IloCplex cplex;
	private IloIntVar[] x;
	private IloIntVar[] y;
	private MetroMap map;
	private	IloIntVar[][] dir;
	private	IloIntVar[][][] a;
	private	int marginX = 40;
	private	int marginY = 40;
	private	int height = 14;
	private	Utility utility = new Utility();

	public void solve(MetroMap map_) {
		Config config = Config.getInstance();
		this.map = map_;
		this.n = map.getStations().size();

		// populate nearest stations list
		for (int i = 0; i < n; i++) {
			Station stationA = map.getStation(i);
			for (int j = i + 1; j < n; j++) {
				Station stationB = map.getStation(j);
				double distance = Math.sqrt(Math.pow(stationB.getX() - stationA.getX(), 2)
						+ Math.pow(stationB.getY() - stationA.getY(), 2));
				stationA.addNearestStation(stationB, distance);
				stationB.addNearestStation(stationA, distance);
			}
		}

		try {
			// define new model
			this.cplex = new IloCplex();
			cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, config.MIPGap);
			if (config.TimeLimit > 0) {
				cplex.setParam(IloCplex.Param.TimeLimit, config.TimeLimit);
			} else {
				cplex.setParam(IloCplex.Param.TimeLimit, Integer.MAX_VALUE);
			}

			// variables
			this.x = new IloIntVar[n];
			this.y = new IloIntVar[n];
			IloNumVar[][] dx = new IloNumVar[n][n];
			IloNumVar[][] dy = new IloNumVar[n][n];
			IloNumVar[][] d = new IloNumVar[n][n];
			IloIntVar[][] biggap = new IloIntVar[n][n];
			IloNumVar[][] mCost = new IloNumVar[n][n];

			IloIntVar[][][] delta1 = new IloIntVar[n][n][n];
			IloIntVar[][][] delta2 = new IloIntVar[n][n][n];

			this.a = new IloIntVar[n][n][4];

			IloIntVar[][] Aprec = new IloIntVar[n][n];
			IloIntVar[][] Aorig = new IloIntVar[n][n];
			IloIntVar[][] Asucc = new IloIntVar[n][n];
			dir = new IloIntVar[n][n];
			IloIntVar[][][] deltaDir = new IloIntVar[n][n][n];
			IloIntVar[][] rpos = new IloIntVar[n][n];

			IloIntVar[][] beta = new IloIntVar[n][8];
			IloNumVar[][] lambda = new IloNumVar[n][n];
			IloIntVar[][] s4 = new IloIntVar[n][n];

			for (int i = 0; i < n; i++) {
				x[i] = cplex.intVar(0, Integer.MAX_VALUE);
				y[i] = cplex.intVar(0, Integer.MAX_VALUE);
				dx[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				dy[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				d[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				
				mCost[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				biggap[i] = cplex.boolVarArray(n);

				Aprec[i] = cplex.boolVarArray(n); // cplex.intVarArray(n, 0, 1);
				Aorig[i] = cplex.boolVarArray(n); // cplex.intVarArray(n, 0, 1);
				Asucc[i] = cplex.boolVarArray(n); // cplex.intVarArray(n, 0, 1);
				dir[i] = cplex.intVarArray(n, 0, 7);
				rpos[i] = cplex.intVarArray(n, 0, 1);
				
				beta[i] = cplex.boolVarArray(8);
				
				s4[i] = cplex.intVarArray(n, 0, 4);

				lambda[i] = cplex.numVarArray(n, 0, Double.MAX_VALUE);
				
	
				for (int j = 0; j <n;j++) {
					a[i][j] = cplex.boolVarArray(4);
					deltaDir[i][j] = cplex.intVarArray(n, 0, 7);
					delta1[i][j] = cplex.boolVarArray(n);
					delta2[i][j] = cplex.boolVarArray(n);
				}
			}
			
			

			// expressions

			// objective
			IloLinearIntExpr cost_S1 = cplex.linearIntExpr(); // Bend
			IloLinearNumExpr cost_S2 = cplex.linearNumExpr(); // Relative Lage
			IloLinearNumExpr cost_S3 = cplex.linearNumExpr(); // Abstand
		
		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (map.getStation(i).isNeighbour(map.getStation(j))) {
						cost_S2.addTerm(rpos[i][j], config.weights[1]);
						cost_S3.addTerm(lambda[i][j], config.weights[2]);
						for (int k = 0; k < n; k++) {
							cost_S1.addTerm(deltaDir[i][j][k], config.weights[0]);
						}
					}
				}

			}
			cplex.addMinimize(cplex.sum(cost_S1, cost_S2, cost_S3));

			// constraints
			int minDistance = 40;
			for (int i = 0; i < n; i++) {
				final Station stationA = map.getStation(i);

				cplex.addGe(x[i], utility.getStringWidth(stationA.getName())/2+10);


				for (Station stationB : stationA.getAdjacentStations()) {				
					int j = map.getStationIndex(stationB);

					if (config.lazyConstraints == true) {
						cplex.addGe(cplex.diff(x[i],
							cplex.sum(
									cplex.sum(x[j],
											utility.getStringWidth(stationB.getName()) / 2
													+ utility.getStringWidth(stationA.getName()) / 2 + marginX),
									cplex.prod(-M, cplex.diff(1, a[i][j][0])))),
							0);
					cplex.addGe(cplex.diff(x[j],
							cplex.sum(
									cplex.sum(x[i],
											utility.getStringWidth(stationB.getName()) / 2
													+ utility.getStringWidth(stationA.getName()) / 2 + marginX),
									cplex.prod(-M, cplex.diff(1, a[i][j][1])))),
							0);
					cplex.addGe(cplex.diff(y[i],
							cplex.sum(cplex.sum(y[j], marginY + height), cplex.prod(-M, cplex.diff(1, a[i][j][2])))),
							0);
					cplex.addGe(cplex.diff(y[j],
							cplex.sum(cplex.sum(y[i], marginY + height), cplex.prod(-M, cplex.diff(1, a[i][j][3])))),
							0);
					}

					cplex.add(cplex.eq(cplex.sum(Aprec[i][j], cplex.sum(Aorig[i][j], Asucc[i][j])), 1));
										
					cplex.add(Aprec[i][j]);
					cplex.add(Aorig[i][j]);
					
					int sec_u = sec(stationA, stationB);
					cplex.add(cplex.eq(dir[i][j], cplex.sum(cplex.prod(sec_u - 1 % 8, Aprec[i][j]),
							cplex.sum(cplex.prod(sec_u, Aorig[i][j]), cplex.prod(sec_u + 1 % 8, Asucc[i][j])))));

					int sec_v = sec(stationB, stationA);
					cplex.add(cplex.eq(dir[j][i], cplex.sum(cplex.prod(sec_v - 1 % 8, Aprec[i][j]),
							cplex.sum(cplex.prod(sec_v, Aorig[i][j]), cplex.prod(sec_v + 1 % 8, Asucc[i][j])))));

					double minSlope = 0.55785;
					double maxSlope = 2.41421;							

					if (sec_u - 1 % 8 == 0) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
					} else if (sec_u - 1 % 8 == 1) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), cplex.diff(x[j], x[i]))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aprec[i][j])), cplex.diff(x[j], x[i]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])),
											cplex.prod(minSlope, cplex.diff(x[j], x[i])))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aprec[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[j], x[i])))));
						}
					} else if (sec_u - 1 % 8 == 2) {
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
					} else if (sec_u - 1 % 8 == 3) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), cplex.diff(x[i], x[j]))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aprec[i][j])), cplex.diff(x[i], x[j]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])),
											cplex.prod(minSlope, cplex.diff(x[i], x[j])))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aprec[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[i], x[j])))));
						}
					} else if (sec_u - 1 % 8 == 4) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
					} else if (sec_u - 1 % 8 == 5) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), cplex.diff(x[i], x[j]))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aprec[i][j])), cplex.diff(x[i], x[j]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])),
											cplex.prod(minSlope, cplex.diff(x[i], x[j])))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aprec[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[i], x[j])))));
						}
					} else if (sec_u - 1 % 8 == 6) {
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
					} else if (sec_u - 1 % 8 == 7) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), cplex.diff(x[j], x[i]))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aprec[i][j])), cplex.diff(x[j], x[i]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])),
											cplex.prod(minSlope, cplex.diff(x[j], x[i])))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aprec[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[j], x[i])))));
						}
					}

					if (sec_u == 0) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
					} else if (sec_u == 1) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), cplex.diff(x[j], x[i]))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aorig[i][j])), cplex.diff(x[j], x[i]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])),
											cplex.prod(minSlope, cplex.diff(x[j], x[i])))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aorig[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[j], x[i])))));
						}
					} else if (sec_u == 2) {
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
					} else if (sec_u == 3) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), cplex.diff(x[i], x[j]))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aorig[i][j])), cplex.diff(x[i], x[j]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])),
											cplex.prod(minSlope, cplex.diff(x[i], x[j])))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aorig[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[i], x[j])))));
						}
					} else if (sec_u == 4) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
					} else if (sec_u == 5) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), cplex.diff(x[i], x[j]))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aorig[i][j])), cplex.diff(x[i], x[j]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])),
											cplex.prod(minSlope, cplex.diff(x[i], x[j])))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aorig[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[i], x[j])))));
						}
					} else if (sec_u == 6) {
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
					} else if (sec_u == 7) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), cplex.diff(x[j], x[i]))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aorig[i][j])), cplex.diff(x[j], x[i]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])),
											cplex.prod(minSlope, cplex.diff(x[j], x[i])))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Aorig[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[j], x[i])))));
						}
					}

					if (sec_u + 1 % 8 == 0) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
					} else if (sec_u + 1 % 8 == 1) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), cplex.diff(x[j], x[i]))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Asucc[i][j])), cplex.diff(x[j], x[i]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])),
											cplex.prod(minSlope, cplex.diff(x[j], x[i])))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Asucc[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[j], x[i])))));
						}
					} else if (sec_u + 1 % 8 == 2) {
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
					} else if (sec_u + 1 % 8 == 3) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), cplex.diff(x[i], x[j]))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Asucc[i][j])), cplex.diff(x[i], x[j]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])),
											cplex.prod(minSlope, cplex.diff(x[i], x[j])))));
							cplex.add(cplex.le(cplex.diff(y[j], y[i]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Asucc[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[i], x[j])))));
						}
					} else if (sec_u + 1 % 8 == 4) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
					} else if (sec_u + 1 % 8 == 5) {
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), cplex.diff(x[i], x[j]))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Asucc[i][j])), cplex.diff(x[i], x[j]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])),
											cplex.prod(minSlope, cplex.diff(x[i], x[j])))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Asucc[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[i], x[j])))));
						}
					} else if (sec_u + 1 % 8 == 6) {
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
					} else if (sec_u + 1 % 8 == 7) {
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));

						if (config.only45 == true) {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), cplex.diff(x[j], x[i]))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Asucc[i][j])), cplex.diff(x[j], x[i]))));
						} else {
							cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])),
											cplex.prod(minSlope, cplex.diff(x[j], x[i])))));
							cplex.add(cplex.le(cplex.diff(y[i], y[j]),
									cplex.sum(cplex.prod(M, cplex.diff(1, Asucc[i][j])),
											cplex.prod(maxSlope, cplex.diff(x[j], x[i])))));
						}
					}

					cplex.addLe(cplex.prod(-M, rpos[i][j]), cplex.diff(dir[i][j], sec_u));
					cplex.addLe(cplex.diff(dir[i][j], sec_u), cplex.prod(M, rpos[i][j]));

					cplex.addLe(cplex.diff(x[i], x[j]), lambda[i][j]);
					cplex.addLe(cplex.diff(x[j], x[i]), lambda[i][j]);
					cplex.addLe(cplex.diff(y[i], y[j]), lambda[i][j]);
					cplex.addLe(cplex.diff(y[j], y[i]), lambda[i][j]);
				}
				
				if (config.lazyConstraints == false) {
					for(int j = i+1; j<n; j++) {
						Station stationB = map.getStation(j);
						cplex.addGe(cplex.diff(x[i],
								cplex.sum(
										cplex.sum(x[j],
												utility.getStringWidth(stationB.getName()) / 2
														+ utility.getStringWidth(stationA.getName()) / 2 + marginX),
										cplex.prod(-M, cplex.diff(1, a[i][j][0])))),
								0);
						cplex.addGe(cplex.diff(x[j],
								cplex.sum(
										cplex.sum(x[i],
												utility.getStringWidth(stationB.getName()) / 2
														+ utility.getStringWidth(stationA.getName()) / 2 + marginX),
										cplex.prod(-M, cplex.diff(1, a[i][j][1])))),
								0);
						cplex.addGe(cplex.diff(y[i],
								cplex.sum(cplex.sum(y[j], marginY + height), cplex.prod(-M, cplex.diff(1, a[i][j][2])))),
								0);
						cplex.addGe(cplex.diff(y[j],
								cplex.sum(cplex.sum(y[i], marginY + height), cplex.prod(-M, cplex.diff(1, a[i][j][3])))),
								0);
					}
				}
				
				for (int j = 0; j < n; j++) {
					if (i != j) {
					IloLinearIntExpr sum = cplex.linearIntExpr();
					for (int k = 0; k < 4; k++) {
						sum.addTerm(a[i][j][k], 1);
					}
					cplex.addGe(sum, 1);
					}
				}			
				
				// circular vertex orders			
				int deg = stationA.getAdjacentStations().size();
				Collections.sort(stationA.getAdjacentStations(), new Comparator<Station>() {
					@Override
					public int compare(Station sA, Station sB)
					{
						return sec(stationA, sA) - sec(stationA, sB);
					}
				});

				if (deg >= 2) {
					IloLinearIntExpr sum = cplex.linearIntExpr();
					for(int k = 0; k < deg; k++) {
						sum.addTerm(beta[i][k], 1);
						
						int u = map.getStationIndex(stationA.getAdjacentStations().get(k));
						int u1;
						if (k + 1 == deg) {
							u1 = map.getStationIndex(stationA.getAdjacentStations().get(0));
						} else {
							u1 = map.getStationIndex(stationA.getAdjacentStations().get(k+1));
						}
						cplex.addLe(dir[i][u], cplex.sum(cplex.diff(dir[i][u1], 1), cplex.prod(8,beta[i][k])));
					}
					for (int k = deg; k < 8; k++){
						cplex.addEq(beta[i][k], 0);
					}
					cplex.addEq(sum, 1);
				}
			}

			// Line Bend
			Map<String, Line> lines = map.getLines();
			for (Entry<String, Line> line : lines.entrySet()) {
				Line vl = line.getValue();
				for (Station v : vl.getStations()) {
					for (int i = 0; i < v.getAdjacentStations().size(); i++) {
						Station u = v.getAdjacentStations().get(i);
						for (Line ul : u.getLines()){
							if (ul == vl) {
								for(int j = i+1; j < v.getAdjacentStations().size(); j++) {
									Station w = v.getAdjacentStations().get(j);
									for (Line wl : w.getLines()) {
										if (wl == vl) {
											int iU = map.getStationIndex(u);
											int iV = map.getStationIndex(v);
											int iW = map.getStationIndex(w);
											cplex.addLe(cplex.prod(deltaDir[iU][iV][iW],-1),
													cplex.sum(
															cplex.diff(cplex.diff(dir[iU][iV], dir[iV][iW]),
																	cplex.prod(8, delta1[iU][iV][iW])),
															cplex.prod(8, delta1[iU][iV][iW])));
											cplex.addGe(deltaDir[iU][iV][iW],
													cplex.sum(
															cplex.diff(cplex.diff(dir[iU][iV], dir[iV][iW]),
																	cplex.prod(8, delta1[iU][iV][iW])),
															cplex.prod(8, delta1[iU][iV][iW])));
											break;
										}
									}
								}
								break;
							}
						}
					}
				}
			}
				
			// solve model
			if (config.lazyConstraints == true) {
				cplex.use(new LazyConstraintCallback());
			}
			cplex.use(new InfoCallback());

			if (cplex.solve()) {
				Output output = new Output();
				output.createImage(map, cplex.getValues(x), cplex.getValues(y));
				ArrayList<ArrayList<int[]>> lageBez = new ArrayList<ArrayList<int[]>>();
				
				for(int i = 0; i < n; i++){
					ArrayList<int[]> N = new ArrayList<int[]>();
					for(Station neighbour : map.getStation(i).getAdjacentStations()) {
						int j = map.getStationIndex(neighbour);
						int sec = sec(i, j);
						int sec_;
											
						if(cplex.getValue(Aprec[i][j]) == 1){
							sec_ = -1;
						} else if (cplex.getValue(Aorig[i][j]) == 1){
							sec_ = 0;
						} else {
							sec_ = 1;
						}
						int[] abc = {(sec_+sec)%8, sec};
						N.add(abc);
					}
					lageBez.add(i, N);
				}
				
			output.createWindow(map, cplex.getValues(x), cplex.getValues(y), lageBez);
			} else {
				System.out.println("problem not solved");
			}

			cplex.end();

		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	// sec_u(v)
	private int sec(int u, int v) {
		return sec(map.getStation(u), map.getStation(v));
	}
	private int sec(Station stationA, Station stationB) {
		double m = (stationB.getY() - stationA.getY()) / (stationB.getX() - stationA.getX());
		if (m >= 0.414 && m <= 2.414) {
			if (stationA.getX() < stationB.getX()) {
				return 1;
			} else {
				return 5;
			}
		} else if (m >= -2.414 && m <= -0.414) {
			if (stationA.getX() < stationB.getX()) {
				return 7;
			} else {
				return 3;
			}
		} else if (m > 2.414) {
			if (stationA.getY() < stationB.getY()) {
				return 2;
			} else {
				return 6;
			}
		} else if (m < -2.414) {
			if (stationA.getY() < stationB.getY()) {
				return 2;
			} else {
				return 6;
			}
		} else if (stationA.getX() < stationB.getX()) {
			return 0;
		} else {
			return 4;
		}
	}

	double best = Double.MAX_VALUE;

	class InfoCallback extends IloCplex.MIPInfoCallback {
		public void main() throws IloException {
			if (getIncumbentObjValue() < best) {
				best = getIncumbentObjValue();
				Output output = new Output();
				output.createImage(map, getIncumbentValues(x), getIncumbentValues(y));
			}
		}
	}

	class LazyConstraintCallback extends IloCplex.LazyConstraintCallback {
		@Override
		public void main() throws IloException {
			System.out.println("LAZY");

			Utility utility = new Utility();
			Output output = new Output();
			output.createImage(map, getValues(x), getValues(y));

			for (int i = 0; i < n; i++) {
				Station stationA = map.getStation(i);
				for (int j = 0; j < n; j++) {
					if (j != i) {
						Station stationB = map.getStation(j);
						if (!((getValue(x[i]) - utility.getStringWidth(stationA.getName()) / 2 >= (getValue(x[j])
								+ utility.getStringWidth(stationB.getName()) / 2 + marginX))
								|| (getValue(x[j]) - utility.getStringWidth(stationB.getName()) / 2 >= (getValue(x[i])
										+ utility.getStringWidth(stationA.getName()) / 2 + marginX))
								|| (getValue(y[i]) >= (getValue(y[j]) + marginY + height))
								|| (getValue(y[j]) >= (getValue(y[i]) + marginY + height)))) {
									System.out.println(stationA + " and " + stationB + " overlap");
							this.add(cplex.ge(cplex.diff(x[i],
									cplex.sum(
											cplex.sum(x[j],
													utility.getStringWidth(stationB.getName()) / 2
															+ utility.getStringWidth(stationA.getName()) / 2 + marginX),
											cplex.prod(-M, cplex.diff(1, a[i][j][0])))),
									0));
							this.add(cplex.ge(cplex.diff(x[j],
									cplex.sum(
											cplex.sum(x[i],
													utility.getStringWidth(stationB.getName()) / 2
															+ utility.getStringWidth(stationA.getName()) / 2 + marginX),
											cplex.prod(-M, cplex.diff(1, a[i][j][1])))),
									0));
							this.add(cplex.ge(cplex.diff(y[i], cplex.sum(cplex.sum(y[j], marginY + height),
									cplex.prod(-M, cplex.diff(1, a[i][j][2])))), 0));
							this.add(cplex.ge(cplex.diff(y[j], cplex.sum(cplex.sum(y[i], marginY + height),
									cplex.prod(-M, cplex.diff(1, a[i][j][3])))), 0));
						}
					}
				}
			}
		}
	}
}



import java.util.Map;

import ilog.concert.*;
import ilog.cplex.*;

public class Solver {
	private int n;
	// private Station[] stations;
	private IloCplex cplex;
	private IloIntVar[] x;
	private IloIntVar[] y;
	private MetroMap map;

	IloNumVar[][] a;
	IloNumVar[][] b;

	int marginX = 20;
	int marginY = 20;
	int height = 14;

	public void solve(MetroMap map_) {
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
			// cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.1);

			// variables
			this.x = new IloIntVar[n];// cplex.intVarArray(n, 0,
										// Integer.MAX_VALUE);
			this.y = new IloIntVar[n]; // cplex.intVarArray(n, 0,
										// Integer.MAX_VALUE);
			IloNumVar[][] dx = new IloNumVar[n][n];
			IloNumVar[][] dy = new IloNumVar[n][n];
			IloNumVar[][] d = new IloNumVar[n][n];
			IloIntVar[][] biggap = new IloIntVar[n][n];
			IloNumVar[][] mCost = new IloNumVar[n][n];

			this.a = new IloNumVar[n][n];
			this.b = new IloNumVar[n][n];

			for (int i = 0; i < n; i++) {
				x[i] = cplex.intVar(0, Integer.MAX_VALUE);
				y[i] = cplex.intVar(0, Integer.MAX_VALUE);
				dx[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				dy[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				d[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				a[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				b[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				mCost[i] = cplex.numVarArray(n, 0, Integer.MAX_VALUE);
				biggap[i] = cplex.boolVarArray(n);
			}

			// expressions

			// objective, das ist ein test

			IloLinearNumExpr objective = cplex.linearNumExpr();
			IloLQNumExpr distance = cplex.lqNumExpr();
			for (int i = 0; i < n; i++) {
				// objective.addTerm(x[i], 0.1);
				// objective.addTerm(y[i], 0.1);
				for (int j = 0; j < n; j++) {
					// objective.addTerm(dx[i][j], 1);
					// objective.addTerm(dy[i][j], 1);
					objective.addTerm(mCost[i][j], 1);
					distance.addTerm(1, dx[i][j], dx[i][j]);
					distance.addTerm(1, dy[i][j], dy[i][j]);
				}

			}

			cplex.addMinimize(cplex.sum(objective, distance));
			// cplex.addMinimize(distance);

			// constraints
			boolean initialConstraints[][] = new boolean[n][n];
			Utility u = new Utility();
			for (int i = 0; i < n; i++) {
				Station stationA = map.getStation(i);
				for (int j = i + 1; j < n; j++) {
					Station stationB = map.getStation(j);
					cplex.add(a[i][j]);
					cplex.add(a[j][i]);
					cplex.add(b[i][j]);
					cplex.add(b[j][i]);

					cplex.addGe(cplex.sum(cplex.ge(a[i][j], 1), cplex.ge(b[i][j], 1)), 1);
					cplex.addGe(cplex.sum(cplex.ge(a[j][i], 1), cplex.ge(b[j][i], 1)), 1);
				}

				for (Station stationB : stationA.getAdjacentStations()) {
					int j = map.getStationIndex(stationB);
					if (initialConstraints[i][j] != true || initialConstraints[j][i] != true) {
						initialConstraints[i][j] = true;
						initialConstraints[j][i] = true;

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

						cplex.addGe(cplex.sum(
								cplex.ge(x[i], cplex.sum(x[j], u.getStringWidth(stationB.getName()) + marginX)),
								cplex.ge(x[j], cplex.sum(x[i], u.getStringWidth(stationA.getName()) + marginX)),
								cplex.ge(y[i], cplex.sum(y[j], height + marginY)),
								cplex.ge(y[j], cplex.sum(y[i], height + marginY))), 1);

						double m = (stationB.getY() - stationA.getY()) / (stationB.getX() - stationA.getX());
						double mMax = 2.0;
						double mMin = 0.5;

						if (m >= 0.414 && m <= 2.414) {
							if (stationA.getAdjacentStations().size() <= 2
									&& stationB.getAdjacentStations().size() <= 2) {
								cplex.addEq(cplex.diff(y[i], y[j]), cplex.prod(1, cplex.diff(x[i], x[j])));
							} else {
								if (stationB.getY() - stationA.getY() > 0) {
									cplex.addLe(cplex.diff(y[j], y[i]), cplex.prod(mMax, cplex.diff(x[j], x[i])));
									cplex.addLe(cplex.prod(mMin, cplex.diff(x[j], x[i])), cplex.diff(y[j], y[i]));
								} else {
									cplex.addLe(cplex.diff(y[i], y[j]), cplex.prod(mMax, cplex.diff(x[i], x[j])));
									cplex.addLe(cplex.prod(mMin, cplex.diff(x[i], x[j])), cplex.diff(y[i], y[j]));
								}
							}
						} else if (m >= -2.414 && m <= -0.414) {
							if (stationA.getAdjacentStations().size() <= 2
									&& stationB.getAdjacentStations().size() <= 2) {
								cplex.addEq(cplex.diff(y[i], y[j]), cplex.prod(-1, cplex.diff(x[i], x[j])));
							} else {
								if (stationB.getY() - stationA.getY() > 0) {
									cplex.addLe(cplex.diff(y[j], y[i]), cplex.prod(mMax, cplex.diff(x[i], x[j])));
									cplex.addLe(cplex.prod(mMin, cplex.diff(x[i], x[j])), cplex.diff(y[j], y[i]));
								} else {
									cplex.addLe(cplex.diff(y[i], y[j]), cplex.prod(mMax, cplex.diff(x[j], x[i])));
									cplex.addLe(cplex.prod(mMin, cplex.diff(x[j], x[i])), cplex.diff(y[i], y[j]));
								}
							}
						} else if (m > 2.414) {
							cplex.addEq(x[i], x[j]);
							// cplex.addGe(cplex.diff(y[i], y[j]),
							// cplex.prod(10, cplex.diff(x[i], x[j])));
						} else if (m < -2.414) {
							cplex.addEq(x[i], x[j]);
							// cplex.addLe(cplex.diff(y[i], y[j]),
							// cplex.prod(-10, cplex.diff(x[i], x[j])));
						} else {
							cplex.addEq(y[i], y[j]);
						}
					}
				}
			}

			// solve model
			cplex.use(new LazyConstraintCallback());
			cplex.use(new InfoCallback());

			if (cplex.solve()) {
				System.out.println("obj = " + cplex.getObjValue());
				Output output = new Output();
				output.createImage(map, cplex.getValues(x), cplex.getValues(y));
			} else {
				System.out.println("problem not solved");
			}

			cplex.end();

		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	class InfoCallback extends IloCplex.MIPInfoCallback {
		public void main() throws IloException {
			// getBestObjValue()
			// getIncumbentObjValue()
		}
	}

	class LazyConstraintCallback extends IloCplex.LazyConstraintCallback {
		@Override
		public void main() throws IloException {

			Utility u = new Utility();
			Output output = new Output();
			output.createImage(map, getValues(x), getValues(y));

			boolean overlapping = false;
			boolean cuts[][][] = new boolean[n][n][n];
			for (int i = 0; i < n; i++) {
				Station stationA = map.getStation(i);

				for (int k = 0; k < 10; k++) {
					Station stationB = stationA.getNearestStations().get(k);
					int j = map.getStationIndex(stationB);

					// overlapping labels
					if (!((getValue(x[i]) >= (getValue(x[j]) + u.getStringWidth(stationB.getName()) + marginX))
							|| (getValue(x[j]) >= (getValue(x[i]) + u.getStringWidth(stationA.getName()) + marginX))
							|| (getValue(y[i]) >= (getValue(y[j]) + marginY))
							|| (getValue(y[j]) >= (getValue(y[i]) + marginY)))) {
						overlapping = true;
						System.out.println(
								stationA.getName() + " & " + stationB.getName() + " overlapping. Adding constraint");

						if (stationA.getX() <= stationB.getX()) {
							this.add(cplex.ge(cplex.diff(cplex.diff(x[j], x[i]),
									cplex.prod(a[i][j], u.getStringWidth(stationA.getName()) + marginX)), 0));

							if (stationA.getY() <= stationB.getY()) {
								this.add(cplex.ge(
										cplex.diff(cplex.diff(y[j], y[i]), cplex.prod(b[i][j], height + marginY)), 0));
							} else {
								this.add(cplex.ge(
										cplex.diff(cplex.diff(y[i], y[j]), cplex.prod(b[i][j], height + marginY)), 0));
							}

						} else {
							this.add(cplex.ge(cplex.diff(cplex.diff(x[i], x[j]),
									cplex.prod(a[i][j], u.getStringWidth(stationB.getName()) + marginX)), 0));

							if (stationA.getY() >= stationB.getY()) {
								this.add(cplex.ge(
										cplex.diff(cplex.diff(y[i], y[j]), cplex.prod(b[i][j], height + marginY)), 0));
							} else {
								this.add(cplex.ge(
										cplex.diff(cplex.diff(y[j], y[i]), cplex.prod(b[i][j], height + marginY)), 0));
							}
						}
					}
				}
			}
			if (overlapping == false) {
				for (int i = 0; i < n; i++) {
					Station stationA = map.getStation(i);

					for (int j = i + 1; j < n; j++) {
						Station stationB = map.getStation(j);

						// overlapping labels
						if (!((getValue(x[i]) >= (getValue(x[j]) + u.getStringWidth(stationB.getName()) + marginX))
								|| (getValue(x[j]) >= (getValue(x[i]) + u.getStringWidth(stationA.getName()) + marginX))
								|| (getValue(y[i]) >= (getValue(y[j]) + marginY))
								|| (getValue(y[j]) >= (getValue(y[i]) + marginY)))) {
							overlapping = true;
							System.out.println(stationA.getName() + " & " + stationB.getName()
									+ " overlapping. Adding constraint");
							if (stationA.getX() <= stationB.getX()) {
								this.add(cplex.ge(
										cplex.diff(cplex.diff(x[j], x[i]),
												cplex.prod(a[i][j], u.getStringWidth(stationA.getName()) + marginX)),
										0));

								if (stationA.getY() <= stationB.getY()) {
									this.add(cplex.ge(
											cplex.diff(cplex.diff(y[j], y[i]), cplex.prod(b[i][j], height + marginY)),
											0));
								} else {
									this.add(cplex.ge(
											cplex.diff(cplex.diff(y[i], y[j]), cplex.prod(b[i][j], height + marginY)),
											0));
								}

							} else {
								this.add(cplex.ge(
										cplex.diff(cplex.diff(x[i], x[j]),
												cplex.prod(a[i][j], u.getStringWidth(stationB.getName()) + marginX)),
										0));

								if (stationA.getY() >= stationB.getY()) {
									this.add(cplex.ge(
											cplex.diff(cplex.diff(y[i], y[j]), cplex.prod(b[i][j], height + marginY)),
											0));
								} else {
									this.add(cplex.ge(
											cplex.diff(cplex.diff(y[j], y[i]), cplex.prod(b[i][j], height + marginY)),
											0));
								}
							}
						}
					}

					if (overlapping == false) {
						for (int j = 0; j < map.getStations().size(); j++) {
							if (j != i) {
								Station stationB = map.getStation(j);
								for (Station stationC : stationB.getAdjacentStations()) {
									int k = map.getStationIndex(stationC);
									if (cuts[i][j][k] == false) {
										if (getValue(x[j]) == getValue(x[k])) {
											if (getValue(x[i]) < getValue(x[j]) && getValue(x[i])
													+ u.getStringWidth(stationA.getName()) + marginX > getValue(x[j])) {
												if (getValue(y[i]) < Math.max(getValue(y[j]), getValue(y[k]))
														&& getValue(y[i]) > Math.min(getValue(y[j]), getValue(y[k]))) {
													System.out.println(
															stationA + " overlaps " + stationB + "-" + stationC);
													this.add(cplex.ge(
															cplex.diff(x[j], cplex.sum(x[i],
																	u.getStringWidth(stationA.getName()) + marginX)),
															0));
													cuts[i][j][k] = true;
													cuts[i][k][j] = true;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

		}
	}
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import ilog.concert.*;
import ilog.cplex.*;

public class Solver {
	private class edge {
		int u;
		int v;

		edge(int i, int j) {
			u = i;
			v = j;
		}
	}

	ArrayList<edge> edges = new ArrayList<>();

	private int n;
	int M = 10000;
	// private Station[] stations;
	private IloCplex cplex;
	private IloIntVar[] x;
	private IloIntVar[] y;
	private MetroMap map;

	IloIntVar[] labelX;
	IloIntVar[] labelY;

	IloIntVar[] labelL;
	IloIntVar[] labelT;
	IloIntVar[] labelR;
	IloIntVar[] labelB;
	IloIntVar[] labelTL;
	IloIntVar[] labelTR;
	IloIntVar[] labelBL;
	IloIntVar[] labelBR;

	IloIntVar[][] dir;
	
	IloIntVar[][][] a;
	IloLinearIntExpr[][] b;

	IloNumVar[][][] r_;
	
	int marginX = 22;
	int marginY = 22;
	int height = 14;
	int labelDistance = 12;

	int dmin = 12;

	Utility utility = new Utility();
	
	IloIntVar[][][] i_uv;
	IloIntVar[][][] i_uvX;

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
			cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.9);
//			cplex.setParam(IloCplex.Param.Parallel, 1);
//			cplex.setParam(IloCplex.Param.Threads, 4);

			// variables
			this.x = new IloIntVar[n];
			this.y = new IloIntVar[n];
			IloNumVar[][] dx = new IloNumVar[n][n];
			IloNumVar[][] dy = new IloNumVar[n][n];
			IloNumVar[][] d = new IloNumVar[n][n];
			IloIntVar[][] biggap = new IloIntVar[n][n];
			IloNumVar[][] mCost = new IloNumVar[n][n];

			this.a = new IloIntVar[n][n][4];
			this.b = new IloLinearIntExpr[n][8];

			IloIntVar[][] Aprec = new IloIntVar[n][n];
			IloIntVar[][] Aorig = new IloIntVar[n][n];
			IloIntVar[][] Asucc = new IloIntVar[n][n];
			dir = new IloIntVar[n][n];
			IloIntVar[][] rpos = new IloIntVar[n][n];

			IloIntVar[][] beta = new IloIntVar[n][8];
			//IloIntVar[][][] gamma = new IloIntVar[n*n][n*n][8];
			IloNumVar[][] lambda = new IloNumVar[n][n];
			IloIntVar[][] s4 = new IloIntVar[n][n];
			

			labelX = cplex.intVarArray(n, 0, Integer.MAX_VALUE);
			labelY = cplex.intVarArray(n, 0, Integer.MAX_VALUE);
			labelL = cplex.boolVarArray(n);
			labelT = cplex.boolVarArray(n);
			labelR = cplex.boolVarArray(n);
			labelB = cplex.boolVarArray(n);
			
			labelTL = cplex.boolVarArray(n);
			labelTR = cplex.boolVarArray(n);
			labelBL = cplex.boolVarArray(n);
			labelBR = cplex.boolVarArray(n);

			r_ = new IloNumVar[n][n][n];//cplex.numVarArray(n,-Double.MAX_VALUE,Double.MAX_VALUE)
			
			i_uv = new IloIntVar[n][n][n];
			i_uvX = new IloIntVar[n][n][n];


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
					
					i_uv[i][j] = cplex.boolVarArray(n);
					i_uvX[i][j] = cplex.boolVarArray(n);
					
					r_[i][j] = cplex.numVarArray(n,-Double.MAX_VALUE,Double.MAX_VALUE);
				}
				
				for (int j=0;j<8;j++){
					b[i][j] = cplex.linearIntExpr();
				}
			}
			
			

			// expressions

			// objective
			IloLinearNumExpr objective = cplex.linearNumExpr();
			IloLQNumExpr distance = cplex.lqNumExpr();
			IloLinearNumExpr cost_S2 = cplex.linearNumExpr();
			IloLinearNumExpr cost_S3 = cplex.linearNumExpr();
			IloLinearNumExpr cost_S4 = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				// cplex.add(x[i]);
				// cplex.add(y[i]);
				// objective.addTerm(x[i], 1);
				// objective.addTerm(y[i], 1);
				for (int j = 0; j < n; j++) {
					// objective.addTerm(dx[i][j], 1);
					// objective.addTerm(dy[i][j], 1);
					// objective.addTerm(mCost[i][j], 1);
					// distance.addTerm(1, dx[i][j], dx[i][j]);
					// distance.addTerm(1, dy[i][j], dy[i][j]);

					cost_S2.addTerm(rpos[i][j], 1000);
					cost_S3.addTerm(lambda[i][j], 5);
					cost_S4.addTerm(s4[i][j], -0.1);
				}

			}

			cplex.addMinimize(cplex.sum(objective, distance, cost_S2, cost_S3));
			
			// cplex.addMinimize(distance);

			// constraints
			boolean initialConstraints[][] = new boolean[n][n];
			
			int minDistance = 40;
			for (int i = 0; i < n; i++) {
				final Station stationA = map.getStation(i);

				cplex.addGe(x[i], utility.getStringWidth(stationA.getName())/2+10);
				// label position
				boolean labels = true;
				if (labels == true) {
//					cplex.add(cplex.eq(cplex.sum(
//						labelTL[i],
//						cplex.sum(labelTR[i],
//							cplex.sum(labelBL[i],
//								cplex.sum(labelBR[i],
//									cplex.sum(labelB[i],
//										cplex.sum(labelR[i],
//											cplex.sum(labelL[i], labelT[i])
//										)
//									)
//								)))), 1));
//					if(stationA.getAdjacentStations().size() <= 3) {
//						cplex.addEq(labelTL[i], 0);
//						cplex.addEq(labelTR[i], 0);
//						cplex.addEq(labelBL[i], 0);
//						cplex.addEq(labelBR[i], 0);
//					}
//
//					// label left
//					cplex.addGe(
//							cplex.diff(cplex.diff(cplex.diff(x[i], utility.getStringWidth(stationA.getName())), labelX[i]),
//									labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelL[i])));
//					cplex.addLe(
//							cplex.diff(cplex.diff(cplex.diff(x[i], utility.getStringWidth(stationA.getName())), labelX[i]),
//									labelDistance),
//							cplex.prod(M, cplex.diff(1, labelL[i])));
//					cplex.addGe(cplex.diff(cplex.sum(labelY[i], height / 2 - 2), y[i]),
//							cplex.prod(-M, cplex.diff(1, labelL[i])));
//					cplex.addLe(cplex.diff(cplex.sum(labelY[i], height / 2 - 2), y[i]),
//							cplex.prod(M, cplex.diff(1, labelL[i])));
//
//					// label right
//					cplex.addGe(cplex.diff(cplex.diff(labelX[i], x[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelR[i])));
//					cplex.addLe(cplex.diff(cplex.diff(labelX[i], x[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelR[i])));
//					cplex.addGe(cplex.diff(cplex.sum(labelY[i], height / 2 - 2), y[i]),
//							cplex.prod(-M, cplex.diff(1, labelR[i])));
//					cplex.addLe(cplex.diff(cplex.sum(labelY[i], height / 2 - 2), y[i]),
//							cplex.prod(M, cplex.diff(1, labelR[i])));
//
//					// label top
//					cplex.addGe(cplex.diff(x[i], labelX[i]), cplex.sum(cplex.prod(-M, cplex.diff(1, labelT[i])),
//							utility.getStringWidth(stationA.getName()) / 2));
//					cplex.addLe(cplex.diff(x[i], labelX[i]), cplex.sum(cplex.prod(M, cplex.diff(1, labelT[i])),
//							utility.getStringWidth(stationA.getName()) / 2));
//					// cplex.addGe(cplex.diff(x[i], labelX[i]), cplex.prod(-M,
//					// cplex.diff(1, labelT[i])));
//					// cplex.addLe(cplex.diff(x[i], labelX[i]), cplex.prod(M,
//					// cplex.diff(1, labelT[i])));
//					// cplex.addLe(cplex.diff(x[i], labelX[i]),
//					// cplex.sum(cplex.prod(M, cplex.diff(1, labelT[i])),
//					// utility.getStringWidth(stationA.getName())/2));
//					cplex.addGe(cplex.diff(cplex.diff(labelY[i], y[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelT[i])));
//					cplex.addLe(cplex.diff(cplex.diff(labelY[i], y[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelT[i])));
//
//					// label bottom
//					cplex.addGe(cplex.diff(x[i], labelX[i]), cplex.sum(cplex.prod(-M, cplex.diff(1, labelB[i])),
//							utility.getStringWidth(stationA.getName()) / 2));
//					cplex.addLe(cplex.diff(x[i], labelX[i]), cplex.sum(cplex.prod(M, cplex.diff(1, labelB[i])),
//							utility.getStringWidth(stationA.getName()) / 2));
//					// cplex.addGe(cplex.diff(x[i], labelX[i]), cplex.prod(-M,
//					// cplex.diff(1, labelB[i])));
//					// cplex.addLe(cplex.diff(x[i], labelX[i]), cplex.prod(M,
//					// cplex.diff(1, labelB[i])));
//					// cplex.addLe(cplex.diff(x[i], labelX[i]),
//					// cplex.sum(cplex.prod(M, cplex.diff(1, labelB[i])),
//					// utility.getStringWidth(stationA.getName())/2));
//					cplex.addGe(cplex.diff(cplex.diff(cplex.diff(y[i], height), labelY[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelB[i])));
//					cplex.addLe(cplex.diff(cplex.diff(cplex.diff(y[i], height), labelY[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelB[i])));
//					// label top left
//					cplex.addGe(
//							cplex.diff(cplex.diff(cplex.diff(x[i], utility.getStringWidth(stationA.getName())), labelX[i]),
//									labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelTL[i])));
//					cplex.addLe(
//							cplex.diff(cplex.diff(cplex.diff(x[i], utility.getStringWidth(stationA.getName())), labelX[i]),
//									labelDistance),
//							cplex.prod(M, cplex.diff(1, labelTL[i])));
//					cplex.addGe(cplex.diff(cplex.diff(labelY[i], y[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelTL[i])));
//					cplex.addLe(cplex.diff(cplex.diff(labelY[i], y[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelTL[i])));
//					// label top right
//					cplex.addGe(cplex.diff(cplex.diff(labelX[i], x[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelTR[i])));
//					cplex.addLe(cplex.diff(cplex.diff(labelX[i], x[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelTR[i])));
//					cplex.addGe(cplex.diff(cplex.diff(labelY[i], y[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelTR[i])));
//					cplex.addLe(cplex.diff(cplex.diff(labelY[i], y[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelTR[i])));
//					// label bottom left
//					cplex.addGe(
//							cplex.diff(cplex.diff(cplex.diff(x[i], utility.getStringWidth(stationA.getName())), labelX[i]),
//									labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelBL[i])));
//					cplex.addLe(
//							cplex.diff(cplex.diff(cplex.diff(x[i], utility.getStringWidth(stationA.getName())), labelX[i]),
//									labelDistance),
//							cplex.prod(M, cplex.diff(1, labelBL[i])));
//					cplex.addGe(cplex.diff(cplex.diff(cplex.diff(y[i], height), labelY[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelBL[i])));
//					cplex.addLe(cplex.diff(cplex.diff(cplex.diff(y[i], height), labelY[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelBL[i])));
//					// label bottom right
//					cplex.addGe(cplex.diff(cplex.diff(labelX[i], x[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelBR[i])));
//					cplex.addLe(cplex.diff(cplex.diff(labelX[i], x[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelBR[i])));
//					cplex.addGe(cplex.diff(cplex.diff(cplex.diff(y[i], height), labelY[i]), labelDistance),
//							cplex.prod(-M, cplex.diff(1, labelBR[i])));
//					cplex.addLe(cplex.diff(cplex.diff(cplex.diff(y[i], height), labelY[i]), labelDistance),
//							cplex.prod(M, cplex.diff(1, labelBR[i])));
					
				}
				cplex.add(labelX[i]);
				cplex.add(labelY[i]);

				for (Station stationB : stationA.getAdjacentStations()) {				
					int j = map.getStationIndex(stationB);
//					cplex.addEq(cplex.sum(cplex.eq(labelL[i],labelL[j]),
//					cplex.sum(cplex.eq(labelR[i],labelR[j]),
//					cplex.sum(cplex.eq(labelT[i],labelT[j]),
//					cplex.eq(labelB[i],labelB[j])))),s4[i][j]);

					edges.add(new edge(i, j));

					cplex.add(cplex.eq(cplex.sum(Aprec[i][j], cplex.sum(Aorig[i][j], Asucc[i][j])), 1));

					int sec_u = sec(stationA, stationB);
					cplex.add(cplex.eq(dir[i][j], cplex.sum(cplex.prod(sec_u - 1 % 8, Aprec[i][j]),
							cplex.sum(cplex.prod(sec_u, Aorig[i][j]), cplex.prod(sec_u + 1 % 8, Asucc[i][j])))));

					int sec_v = sec(stationB, stationA);
					cplex.add(cplex.eq(dir[j][i], cplex.sum(cplex.prod(sec_v - 1 % 8, Aprec[i][j]),
							cplex.sum(cplex.prod(sec_v, Aorig[i][j]), cplex.prod(sec_v + 1 % 8, Asucc[i][j])))));

					if (sec_u - 1 % 8 == 0) {
						b[i][0].addTerm(Aprec[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
					} else if (sec_u - 1 % 8 == 1) {
						b[i][1].addTerm(Aprec[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
					} else if (sec_u - 1 % 8 == 2) {
						b[i][2].addTerm(Aprec[i][j], 1);
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
					} else if (sec_u - 1 % 8 == 3) {
						b[i][3].addTerm(Aprec[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
					} else if (sec_u - 1 % 8 == 4) {
						b[i][4].addTerm(Aprec[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
					} else if (sec_u - 1 % 8 == 5) {
						b[i][5].addTerm(Aprec[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
					} else if (sec_u - 1 % 8 == 6) {
						b[i][6].addTerm(Aprec[i][j], 1);
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Aprec[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
					} else if (sec_u - 1 % 8 == 7) {
						b[i][7].addTerm(Aprec[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aprec[i][j])), minDistance)));
					}

					if (sec_u == 0) {
						b[i][0].addTerm(Aorig[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
					} else if (sec_u == 1) {
						b[i][1].addTerm(Aorig[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
					} else if (sec_u == 2) {
						b[i][2].addTerm(Aorig[i][j], 1);
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
					} else if (sec_u == 3) {
						b[i][3].addTerm(Aorig[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
					} else if (sec_u == 4) {
						b[i][4].addTerm(Aorig[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
					} else if (sec_u == 5) {
						b[i][5].addTerm(Aorig[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
					} else if (sec_u == 6) {
						b[i][6].addTerm(Aorig[i][j], 1);
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Aorig[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
					} else if (sec_u == 7) {
						b[i][7].addTerm(Aorig[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Aorig[i][j])), minDistance)));
					}

					if (sec_u + 1 % 8 == 0) {
						b[i][0].addTerm(Asucc[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
					} else if (sec_u + 1 % 8 == 1) {
						b[i][1].addTerm(Asucc[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
					} else if (sec_u + 1 % 8 == 2) {
						b[i][2].addTerm(Asucc[i][j], 1);
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
					} else if (sec_u + 1 % 8 == 3) {
						b[i][3].addTerm(Asucc[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[j], y[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
					} else if (sec_u + 1 % 8 == 4) {
						b[i][4].addTerm(Asucc[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
						cplex.add(cplex.le(cplex.diff(y[i], y[j]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.le(cplex.diff(y[j], y[i]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
					} else if (sec_u + 1 % 8 == 5) {
						b[i][5].addTerm(Asucc[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[i], x[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
					} else if (sec_u + 1 % 8 == 6) {
						b[i][6].addTerm(Asucc[i][j], 1);
						cplex.add(cplex.le(cplex.diff(x[i], x[j]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.le(cplex.diff(x[j], x[i]), cplex.prod(M, cplex.diff(1, Asucc[i][j]))));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
					} else if (sec_u + 1 % 8 == 7) {
						b[i][7].addTerm(Asucc[i][j], 1);
						cplex.add(cplex.ge(cplex.diff(x[j], x[i]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
						cplex.add(cplex.ge(cplex.diff(y[i], y[j]),
								cplex.sum(cplex.prod(-M, cplex.diff(1, Asucc[i][j])), minDistance)));
					}

					cplex.addLe(cplex.prod(-M, rpos[i][j]), cplex.diff(dir[i][j], sec_u));
					cplex.addLe(cplex.diff(dir[i][j], sec_u), cplex.prod(M, rpos[i][j]));

					cplex.addLe(cplex.diff(x[i], x[j]), lambda[i][j]);
					cplex.addLe(cplex.diff(x[j], x[i]), lambda[i][j]);
					cplex.addLe(cplex.diff(y[i], y[j]), lambda[i][j]);
					cplex.addLe(cplex.diff(y[j], y[i]), lambda[i][j]);


					// overlap
//					cplex.addGe(
//							cplex.sum(cplex.ge(x[i], cplex.sum(x[j], utility.getStringWidth(stationB.getName()) + marginX)),
//									cplex.ge(x[j], cplex.sum(x[i], utility.getStringWidth(stationA.getName()) + marginX)),
//									cplex.ge(y[i], cplex.sum(y[j], height + marginY)),
//									cplex.ge(y[j], cplex.sum(y[i], height + marginY))),
//							1);

				}
				
				if (stationA.getAdjacentStations().size() <= 7 ){
					///cplex.addLe(labelL[i], cplex.prod(M, cplex.diff(1,b[i][2])));
				//	cplex.addLe(labelL[i], cplex.prod(M, cplex.diff(1,b[i][6])));
					cplex.addLe(labelR[i], cplex.prod(M, cplex.diff(1,b[i][0])));
					cplex.addLe(labelL[i], cplex.prod(M, cplex.diff(1,b[i][4])));			
					cplex.addLe(labelT[i],cplex.prod(M,cplex.diff(1,cplex.ge(cplex.sum(cplex.sum(b[i][1],b[i][2]),b[i][3]),1))));
					cplex.addLe(labelB[i],cplex.prod(M,cplex.diff(1,cplex.ge(cplex.sum(cplex.sum(b[i][5],b[i][6]),b[i][7]),1))));
					
					if (stationA.getAdjacentStations().size() >= 4) {
						cplex.addLe(labelTR[i], cplex.prod(M, cplex.diff(1,b[i][1])));
						cplex.addLe(labelTL[i], cplex.prod(M, cplex.diff(1,b[i][3])));
						cplex.addLe(labelBR[i], cplex.prod(M, cplex.diff(1,b[i][7])));
						cplex.addLe(labelBL[i], cplex.prod(M, cplex.diff(1,b[i][5])));
					}
				} 
				
				for (int j = i+1; j < n; j++) {
					IloLinearIntExpr sum = cplex.linearIntExpr();
					for (int k = 0; k < 4; k++) {
						sum.addTerm(a[i][j][k], 1);
					}
					cplex.addGe(sum, 1);
				}
					
//					for (int m = 0; m < edges.size(); m++) {
//						edge e = edges.get(m);
//						int u = e.u;
//						int v = e.v;
//						if(u != i && v != i) {
//							cplex.addEq(
//							cplex.sum(cplex.diff(cplex.diff(cplex.prod(labelX[i],cplex.diff(y[u],y[v])),cplex.prod(labelY[i],cplex.diff(x[u],x[v]))),
//							cplex.prod(x[u],cplex.diff(y[u],y[v]))),
//							cplex.prod(y[u],cplex.diff(x[u],x[v]))),
//							
//							cplex.sum(cplex.diff(cplex.diff(cplex.prod(labelX[i],cplex.diff(y[u],y[v])),cplex.prod(cplex.sum(labelY[i],height),cplex.diff(x[u],x[v]))),
//									cplex.prod(x[u],cplex.diff(y[u],y[v]))),
//									cplex.prod(y[u],cplex.diff(x[u],x[v]))))
//							;
//							//cplex.addGe(cplex.sum(x[u],cplex.prod(cplex.diff(x[u],x[v]),r_[u][v][i]),x[i]));
//							}
//						}*/
					
				
				// circular vertex orders			
				int deg = stationA.getAdjacentStations().size();
				Collections.sort(stationA.getAdjacentStations(), new Comparator<Station>() {

					@Override
					public int compare(Station sA, Station sB)
					{
						return sec(stationA, sA) - sec(stationA, sB);
					}
				});
//				System.out.println(stationA+": "+stationA.getAdjacentStations());
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
				//			cplex.addLe(dir[i][u], cplex.sum(cplex.diff(dir[i][u1], 1), cplex.prod(8,beta[i][k])));
						}
						cplex.addLe(dir[i][u], cplex.sum(cplex.diff(dir[i][u1], 1), cplex.prod(8,beta[i][k])));
					}
					for (int k = deg; k < 8; k++){
						cplex.addEq(beta[i][k], 0);
					}
					cplex.addEq(sum, 1);
				}
				
				/*
					cplex.add(
					
					cplex.ge(
						cplex.sum(cplex.ge(labelX[i], x[i]),cplex.ge(x[u], cplex.sum(labelX[i], utility.getStringWidth(stationA.getName())))),
						cplex.prod(-M, cplex.diff(1, 
						cplex.eq(
						cplex.sum(dir[u][v],
						cplex.eq(cplex.sum(
							cplex.eq(
								cplex.sum(
									cplex.ge(labelY[i],y[u]),
									cplex.ge(y[v],labelY[i])),2),
							cplex.eq(
									cplex.sum(
										cplex.ge(x[u], labelX[i]),
										cplex.ge(cplex.sum(labelX[i],utility.getStringWidth(stationA.getName())),x[u])),2))
						,2)),2)))
							
						));
				}*/
			}

			for (int i = 0; i < n; i++) {
				Station stationA = map.getStation(i);
				for (int m = 0; m < edges.size(); m++) {
					edge e = edges.get(m);
					int u = e.u;
					int v = e.v;

//					cplex.addEq(i_uv[i][u][v], cplex.eq(
//					cplex.sum(cplex.eq(dir[u][v],2),
//						cplex.eq(cplex.sum(
//							cplex.eq(
//								cplex.sum(
//									cplex.ge(labelY[i],y[u]),
//									cplex.ge(y[v],labelY[i])),2),
//							cplex.eq(
//									cplex.sum(
//										cplex.ge(x[u], labelX[i]),
//										cplex.ge(cplex.sum(labelX[i],utility.getStringWidth(stationA.getName())),x[u])),2))
//						,2)),2));
//					cplex.addEq(i_uvX[i][u][v],cplex.eq(2,
//						cplex.sum(cplex.ge(labelX[i], x[u]),cplex.ge(x[u], cplex.sum(labelX[i], utility.getStringWidth(stationA.getName()))))));
//					cplex.add(i_uv[i][u][v]);
//					cplex.add(i_uvX[i][u][v]);
				}
			}

			// solve model
			cplex.use(new LazyConstraintCallback());
			cplex.use(new InfoCallback());

			if (cplex.solve()) {
//				System.out.println("obj = " + cplex.getObjValue());
				Output output = new Output();
				output.createImage(map, cplex.getValues(x), cplex.getValues(y), cplex.getValues(labelX),
						cplex.getValues(labelY));
				ArrayList<ArrayList<int[]>> lageBez = new ArrayList<ArrayList<int[]>>();
				
				for(int i = 0; i<n; i++){
					for(int j = 0; j<n;j++){
						int sec = sec(i, j);
						if(cplex.getValue(Aprec[i][j]) == 1){
							int[] abc = {-1,sec};
							lageBez.add(abc);
						} else if (cplex.getValue(Aprec[i][j]) == 1){
							int[] abc = {0,sec};
							lageBez.add(abc);
//							, cplex.sum(Aorig[i][j], Asucc[i][j]
						} else {
							int[] abc = {1,sec};
							lageBez.add(abc);
						}
					}
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
				output.createImage(map, getIncumbentValues(x), getIncumbentValues(y), getIncumbentValues(labelX),
						getIncumbentValues(labelY));
		//		output.createWindow(map, cplex.getValues(x), cplex.getValues(y));
			}
			// System.out.println(getBestObjValue());
			// System.out.println(getIncumbentObjValue());
		}
	}

	class LazyConstraintCallback extends IloCplex.LazyConstraintCallback {
		@Override
		public void main() throws IloException {
			System.out.println("LAZY");

			Utility utility = new Utility();
			Output output = new Output();
			output.createImage(map, getValues(x), getValues(y), getValues(labelX), getValues(labelY));
//			output.createWindow(map, cplex.getValues(x), cplex.getValues(y));
			boolean overlapping = false;
			boolean cuts[][][] = new boolean[n][n][n];

			
			for (int m = 0; m < edges.size(); m++) {
				edge e1 = edges.get(m);
				for (int n = m + 1; n < edges.size(); n++) {
					edge e2 = edges.get(n);
					if (e1.u != e2.u && e1.u != e2.v && e1.v != e2.u && e1.v != e2.v) {
						
					}
				}
			}
			
			for (int i = 0; i < n; i++) {
				Station stationA = map.getStation(i);

				for (int j = i + 1; j < n; j++) {
					Station stationB = map.getStation(j);
					// overlapping labels
					if (!((getValue(
							x[i]) - utility.getStringWidth(stationA.getName())/2 >= (getValue(x[j]) + utility.getStringWidth(stationB.getName())/2 + marginX))
							|| (getValue(x[j]) - utility.getStringWidth(stationB.getName())/2 >= (getValue(x[i]) + utility.getStringWidth(stationA.getName())/2
									+ marginX))
							|| (getValue(labelY[i]) >= (getValue(labelY[j]) + marginY + height))
							|| (getValue(labelY[j])  >= (getValue(labelY[i]) + marginY + height)))) {
						
						
						if (!(getValue(
								x[i]) - utility.getStringWidth(stationA.getName())/2 >= (getValue(x[j]) + utility.getStringWidth(stationB.getName())/2 + marginX))) {
							this.add(cplex.ge(cplex.diff(x[i],
									cplex.sum(cplex.sum(x[j], utility.getStringWidth(stationB.getName())/2 +  utility.getStringWidth(stationA.getName())/2 + marginX),
											cplex.prod(-M, cplex.diff(1, a[i][j][0])))),
									0));
						} 
//						
						if (!(getValue(x[j]) - utility.getStringWidth(stationB.getName())/2 >= (getValue(x[i]) + utility.getStringWidth(stationA.getName())/2
								+ marginX))) {
						
						this.add(cplex.ge(cplex.diff(x[j],
								cplex.sum(cplex.sum(x[i],  utility.getStringWidth(stationB.getName())/2 + utility.getStringWidth(stationA.getName())/2 + marginX),
										cplex.prod(-M, cplex.diff(1, a[i][j][1])))),
								0));

						} 
						if (!(getValue(y[i]) >= (getValue(y[j]) + marginY + height))){
							this.add(cplex.ge(cplex.diff(y[i],
								cplex.sum(cplex.sum(y[j], marginY + height), cplex.prod(-M, cplex.diff(1, a[i][j][2])))),
								0));

						} 
						if(!(getValue(y[j]) >= (getValue(y[i]) + marginY + height))){
						this.add(cplex.ge(cplex.diff(y[j],
								cplex.sum(cplex.sum(y[i], marginY + height), cplex.prod(-M, cplex.diff(1, a[i][j][3])))),
								0));

						}
					}
				}

				int deg = stationA.getAdjacentStations().size();
				int [] adj = new int[8];
				if (deg <= 2) {
					for(Station stationB : stationA.getAdjacentStations()) {
						int j = map.getStationIndex(stationB);


					}
				}
			}
			for (int i = 0; i < n; i++) {
				Station stationA = map.getStation(i);
				
				for (int m = 0; m < edges.size(); m++) {
					edge e = edges.get(m);
					int u = e.u;
					int v = e.v;
//					if(u == i || v == i) {
//						if (!(Math.max(getValue(x[u]), getValue(x[v])) + marginX < getValue(labelX[i]) ||
//							Math.min(getValue(x[u]), getValue(x[v])) > getValue(labelX[i]) + utility.getStringWidth(stationA.getName()) + marginX ||
//							Math.max(getValue(y[u]), getValue(y[v])) + marginY < getValue(labelY[i]) ||
//							Math.min(getValue(y[u]), getValue(y[v])) > getValue(labelY[i]) + marginY)) {
//					if((Math.max(getValue(y[u]), getValue(y[v])) > getValue(labelY[i]) &&
//						Math.min(getValue(y[u]), getValue(y[v])) < getValue(labelY[i])) && ! (Math.max(getValue(x[u]), getValue(x[v])) < getValue(labelX[i]) || Math.min(getValue(x[u]), getValue(x[v])) > getValue(labelX[i])) ) {
							if (getValue(dir[u][v]) == 2 && sec(u,v) == 2) {
								if(Math.max(getValue(y[u]), getValue(y[v])) > getValue(labelY[i]) &&
								Math.min(getValue(y[u]), getValue(y[v])) < getValue(labelY[i])){
									if(getValue(x[u]) > getValue(labelX[i]) -12 && getValue(x[u]) <12+ getValue(labelX[i]) + utility.getStringWidth(stationA.getName()) ) {
								//cplex.eq(cp)
//										this.add(cplex.eq(
//								cplex.sum(
//										this.add(cplex.ge(cplex.diff(1,cplex.ge(cplex.sum(
//												cplex.ge(cplex.diff(labelY[i],y[u]), 0),
//												cplex.ge(cplex.diff(y[v],labelY[i]), 0)),1)
//												),0));
								//this.add(	cplex.eq(cplex.sum(cplex.ge(labelY[i], y[u]),cplex.le(labelY[i], y[v])),2));
//								,
//										cplex.eq(cplex.sum(cplex.ge(labelX[i], x[u]),cplex.le(cplex.sum(labelX[i],utility.getStringWidth(stationA.getName())), x[u])),2)),2));
//								if (stationA.getX() < map.getStation(u).getX() && stationA.getX() < map.getStation(v).getX()){
//										System.out.println(stationA + " " + map.getStation(u) + " - " + map.getStation(v));
//										this.add(cplex.ge(cplex.diff(x[u],cplex.sum(labelX[i], 12 + utility.getStringWidth(stationA.getName()))),0));
//										this.add(cplex.ge(cplex.diff(x[v],cplex.sum(labelX[i], 12 + utility.getStringWidth(stationA.getName()))),0));
//								} else if (stationA.getX() > map.getStation(u).getX() && stationA.getX() > map.getStation(v).getX()) {
//									System.out.println(stationA + " " + map.getStation(u) + " - " + map.getStation(v));
//									this.add(cplex.ge(cplex.diff(cplex.sum(12,labelX[i]), x[u]),0));
//									this.add(cplex.ge(cplex.diff(cplex.sum(12,labelX[i]), x[v]),0));
//								}
//								this.add(cplex.ge(cplex.diff(i_uvX[i][u][v],cplex.prod(-M,cplex.diff(1,i_uv[i][u][v]))),0));
									}
							}
							}
				}
			}
//			
////							if (getValue(dir[u][v]) == 2) {
////								if (getValue(x[u]) > getValue(labelX[i]) && getValue(x[u]) < getValue(labelX[i])
////										+ utility.getStringWidth(stationA.getName())) {
////									
////										this.add(cplex.eq(
////												cplex.diff(cplex.prod(cplex.sum(labelL[i], labelR[i]), 2), dir[u][v]),
////												0));
////										// this.add(cplex.ge(cplex.diff(cplex.diff(labelX[i],x[u]),cplex.prod(M,
////										// cplex.diff(dir[u][v], 2))),0));
////										System.out.println(stationA + " cuts " + map.getStation(u).getName() + " - "
////												+ map.getStation(v));
////									
//								}
//							} else if (getValue(dir[u][v]) == 2) {
//								if (getValue(x[u]) > getValue(labelX[i]) && getValue(x[u]) < getValue(labelX[i])
//										+ utility.getStringWidth(stationA.getName())) {
//									if (deg <= 2) {
//										this.add(cplex.eq(
//												cplex.diff(cplex.prod(cplex.sum(labelL[i], labelR[i]), 2), dir[u][v]),
//												0));
//										// this.add(cplex.ge(cplex.diff(cplex.diff(labelX[i],x[u]),cplex.prod(M,
//										// cplex.diff(dir[u][v], 2))),0));
//										System.out.println(stationA + " cuts " + map.getStation(u).getName() + " - "
//												+ map.getStation(v));
//									}
//								}
//							} 
//								//this.add(cplex.ge(cplex.prod(cplex.diff(x[u],x[v]),r_[u][v][i]),0));
//						}
//					}
				}
					}
			

						
	
		
	}



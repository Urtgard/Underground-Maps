package GuiTool;


import java.util.ArrayList;
import java.util.List;


public class Button {
	private ArrayList<String> linienName;
	private String name;
	private double x;
	private double y;
	private ArrayList<Neighbour> N;
	
	
	//constructor
	public Button(ArrayList<String> linienName, String name, double x, double y, ArrayList<Neighbour> N) {
		this.linienName = linienName;
		this.name = name;
		this.x = x;
		this.y = y;
		this.N = N;
	}
	
	
	//getter und setter
	
	public String getName() {
		return name;
	}

	public ArrayList<String> getLinienName() {
		return linienName;
	}


	public void setLinienName(ArrayList<String> linienName) {
		this.linienName = linienName;
	}


	public void setName(String name) {
		this.name = name;
	}
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}


	public ArrayList<Neighbour> getN() {
		return N;
	}


	public void setN(ArrayList<Neighbour> n) {
		N = n;
	}

	
	
	//methoden
	
	
}

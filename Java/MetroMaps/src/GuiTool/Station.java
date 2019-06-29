package GuiTool;


import java.util.List;


public class Station {
	private String name;
	private double x;
	private double y;
	private  List<String> constraints;
	
	//constructor
	public Station(String name, double x, double y, List<String> constraints) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.constraints = constraints;
	}
	
	
	//getter und setter
	public String getName() {
		return name;
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

	public List<String> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<String> constraints) {
		this.constraints = constraints;
	}
	
	//methoden
	
	
}

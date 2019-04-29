
public class Station {
	private double xCoord;
	private double yCoord;
	private String name;

	Station(String n, double x, double y) {
		xCoord = x;
		yCoord = y;
		name = n;
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

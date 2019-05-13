//add station list, add line dependency
public class Station {
	private double xCoord;
	private double yCoord;
	String name;
	private boolean dummy;

	Station(String n, double x, double y) {
		xCoord = x;
		yCoord = y;
		name = n;
	}

	Station(String n, double x, double y, boolean d) {
		xCoord = x;
		yCoord = y;
		name = n;
		dummy = d;
	}

	public double getX() {
		return xCoord;
	}

	public double getY() {
		return yCoord;
	}

	public String getName() {
		if (dummy == true) {
			return "dummy";
		} else {
			return name;
		}
	}

	public boolean isDummy() {
		return dummy;
	}

	@Override
	public String toString() {
		return name;
	}

}

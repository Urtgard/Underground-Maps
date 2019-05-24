import java.util.ArrayList;

public class Station {
	private double xCoord;
	private double yCoord;
	private String name;
	private boolean dummy;
	private ArrayList<Line> lines = new ArrayList<>();
	private ArrayList<Station> adjacentStations = new ArrayList<>();

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
	
	public void addLine(Line line) {
		if (lines.indexOf(line) == -1) {
			lines.add(line);
		}
	}
	
	public ArrayList<Line> getLines() {
		return lines;
	}
	
	public void addAdjacentStation(Station station) {
		if (adjacentStations.indexOf(station) == -1 && station.isDummy() == false) {
			adjacentStations.add(station);
		}
	}

	public ArrayList<Station> getAdjacentStations() {
		return adjacentStations;
	}
	
	@Override
	public String toString() {
		return name;
	}

}

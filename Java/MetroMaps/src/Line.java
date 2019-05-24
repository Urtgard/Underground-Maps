import java.awt.Color;
import java.util.ArrayList;

public class Line {
	private String name;
	private ArrayList<Station> stations = new ArrayList<>();
	private Color color;

	Line(String n) {
		name = n;
	}

	public void addStation(Station station) {
		int sourceIndex = stations.indexOf(station);
		if (sourceIndex == -1) {
			stations.add(station);
		}
	}

	public void addStations(Station stationA, Station stationB) {
		addStation(stationA);
		addStation(stationB);
		stationA.addAdjacentStation(stationB);
		stationB.addAdjacentStation(stationA);
		stationA.addLine(this);
		stationB.addLine(this);
	}

	public String getName() {
		return name;
	}

	public ArrayList<Station> getStations() {
		return stations;
	}

	public void sort() {
		Station station = stations.get(0);
		for (Station s : stations) {
			int n = 0;
			ArrayList<Station> adjacentStations = s.getAdjacentStations();
			for (Station adjacentStation : adjacentStations) {
				ArrayList<Line> lines = adjacentStation.getLines();
				for (Line line : lines) {
					if (line == this) {
						n++;
					}
				}
			}
			if (n == 1) {
				station = s;
				break;
			}
		}

		Station prevStation = station;
		for (int i = 0; i < stations.size(); i++) {
			stations.remove(station);
			stations.add(i, station);

			ArrayList<Station> adjacentStations = station.getAdjacentStations();
			searchNextStation:
			for (Station adjacentStation : adjacentStations) {
				ArrayList<Line> lines = adjacentStation.getLines();
				for (Line line : lines) {
					if (line == this && adjacentStation != prevStation) {
						prevStation = station;
						station = adjacentStation;
						break searchNextStation;
					}
				}
			}
		}
	}
	
	public void setColor(Color c) {
		color = c;
	}
	
	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		String result = name + ":";
		for (Station station : stations) {
			result += "\n  " + station.getName();
		}
		return result;
	}
}

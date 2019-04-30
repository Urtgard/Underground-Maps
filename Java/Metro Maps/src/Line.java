import java.util.ArrayList;

public class Line {
	private String name;
	private ArrayList<Station> stations = new ArrayList<>();

	Line(String n) {
		name = n;
	}

	public void addStation(Station station) {
		int sourceIndex = stations.indexOf(station);
		if (sourceIndex == -1) {
			stations.add(station);
		}
	}

	public String getName() {
		return name;
	}

	public ArrayList<Station> getStations() {
		return stations;
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

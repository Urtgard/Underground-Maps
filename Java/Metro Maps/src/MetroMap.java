import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetroMap {
	private Map<String, Station> stations = new HashMap<String, Station>();
	private ArrayList<Line> lines = new ArrayList<>();

	public void addStation(Station station) {
		stations.put(station.getName(), station);
	}

	public Station getStation(String name) {
		assert stations.get(name) != null : "Station \"" + name + "\" doesn't exists";
		return stations.get(name);
	}

	public void addLine(Line l) {
		lines.add(l);
	}

	@Override
	public String toString() {
		String result = "Stations:";
		for (Map.Entry station : stations.entrySet()) {
			result += "\n " + station.getKey();
		}

		result += "\n\nLines:";
		for (Line line : lines) {
			result += "\n " + line;
		}

		return result;
	}
}

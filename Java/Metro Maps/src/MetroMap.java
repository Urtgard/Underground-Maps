import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetroMap {
	private Map<String, Station> stations = new HashMap<String, Station>();
	private Map<String, Line> lines = new HashMap<String, Line>();

	public void addStation(Station station) {
		stations.put(station.getName(), station);
	}

	public Station getStation(String name) {
		assert stations.get(name) != null : "Station \"" + name + "\" doesn't exists";
		return stations.get(name);
	}

	public Map<String, Station> getStations() {
		return stations;
	}
	
	public Station[] getStationsArray(){
		return stations.values().toArray(new Station[0]);
	}

	public void addLine(Line line) {
		lines.put(line.getName(), line);
	}

	public Line getLine(String name) {
		return lines.get(name);
	}

	public Map<String, Line> getLines() {
		return lines;
	}

	@Override
	public String toString() {
		String result = "Stations:";
		for (Map.Entry station : stations.entrySet()) {
			result += "\n " + station.getKey();
		}

		result += "\n\nLines:";
		for (Map.Entry line : lines.entrySet()) {
			result += "\n " + line.getValue();
		}

		return result;
	}
}

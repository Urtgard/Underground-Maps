package backEnd;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class MetroMap {
	private Map<String, Station> stations = new HashMap<String, Station>();
	private Map<String, Line> lines = new HashMap<String, Line>();
	private Map<Station, Integer> indexByStation = new HashMap<Station, Integer>();
	private Map<Integer, Station> stationByIndex = new HashMap<Integer, Station>();
	private Color[] colors = new Color[] {
			
			new Color(255, 51, 51), //rot
			new Color(0, 153, 0), //grün
			new Color(51, 153, 255), //blau
			new Color(255, 102, 255), //pink
			new Color(139,69,19), //braun
			new Color(0, 0, 0),//schwarz
			new Color(153, 0, 255), //lila
			new Color(87,87,87), //grau
			new Color(255, 255, 51), //gelb
			new Color(51, 255, 255),//hellblau
			new Color(255, 153, 51), //orange
			
			new Color(255, 51, 51), //rot
			new Color(0, 153, 0), //grün
			new Color(51, 153, 255), //blau
			new Color(255, 102, 255), //pink
			new Color(139,69,19), //braun
			new Color(0, 0, 0),//schwarz
			new Color(153, 0, 255), //lila
			new Color(87,87,87), //grau
			new Color(255, 255, 51), //gelb
			new Color(51, 255, 255),//hellblau
			new Color(255, 153, 51), //orange
			
			
			new Color(255, 51, 51), //rot
			new Color(0, 153, 0), //grün
			new Color(51, 153, 255), //blau
			new Color(255, 102, 255), //pink
			new Color(139,69,19), //braun
			new Color(0, 0, 0),//schwarz
			new Color(153, 0, 255), //lila
			new Color(87,87,87), //grau
			new Color(255, 255, 51), //gelb
			new Color(51, 255, 255),//hellblau
			new Color(255, 153, 51), //orange
			};

	public void addStation(Station station) {
		stations.put(station.getName(), station);
		int n = indexByStation.size();
		indexByStation.put(station, n);
		stationByIndex.put(n, station);
	}

	public Station getStation(String name) {
		assert stations.get(name) != null : "Station \"" + name + "\" doesn't exists";
		return stations.get(name);
	}

	public Station getStation(int index) {
		return stationByIndex.get(index);
	}

	public int getStationIndex(Station station) {
		return indexByStation.get(station);
	}

	public Map<Integer, Station> getStations() {
		return stationByIndex;
	}

	public Station[] getStationsArray() {
		return stations.values().toArray(new Station[0]);
	}

	public void addLine(Line line) {
		lines.put(line.getName(), line);
		line.setColor(colors[lines.size() - 1]);
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
		for (Map.Entry<String, Station> station : stations.entrySet()) {
			result += "\n " + station.getKey();
		}

		result += "\n\nLines:";
		for (Map.Entry<String, Line> line : lines.entrySet()) {
			result += "\n " + line.getValue();
		}

		return result;
	}
}

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

public class XMLParser {
	public MetroMap getMapFromXML(String file) {
		try {
			Document doc = new SAXBuilder().build(file);
			Element root = doc.getRootElement();
			switch (root.getName()) {
			case "omm-file":
				return readOpenMetroMap(root);
			case "graphml":
				return readGraphML(root);
			}
		} catch (Exception e) {
		}
		return new MetroMap();
	}

	public MetroMap readOpenMetroMap(Element root) {
		MetroMap map = new MetroMap();

		List<Element> stationsXML = root.getChild("stations").getChildren("station");
		for (Element stationXML : stationsXML) {
			String name = stationXML.getAttributeValue("name");
			double x = Double.valueOf(stationXML.getAttributeValue("lon"));
			double y = Double.valueOf(stationXML.getAttributeValue("lat"));

			Station station = new Station(name, x, y);
			map.addStation(station);
		}

		List<Element> linesXML = root.getChild("lines").getChildren("line");
		for (Element lineXML : linesXML) {
			String name = lineXML.getAttributeValue("name");
			Line line = new Line(name);

			List<Element> stopsXML = lineXML.getChildren("stop");
			for (Element stopXML : stopsXML) {
				name = stopXML.getAttributeValue("station");

				Station station = map.getStation(name);
				line.addStation(station);
			}

			map.addLine(line);
		}

		return map;
	}

	public MetroMap readGraphML(Element root) {
		MetroMap map = new MetroMap();
		Namespace ns = root.getNamespace();
		Map<String, Station> stationsById = new HashMap<String, Station>();

		List<Element> nodesXML = root.getChild("graph", ns).getChildren("node", ns);
		for (Element nodeXML : nodesXML) {
			String id = nodeXML.getAttributeValue("id");
			double x = 0;
			double y = 0;
			String name = "";
			Boolean dummy = false;

			List<Element> datasXML = nodeXML.getChildren("data", ns);
			for (Element dataXML : datasXML) {
				switch (dataXML.getAttributeValue("key")) {
				case "x":
					x = Double.valueOf(dataXML.getText());
					break;
				case "y":
					y = Double.valueOf(dataXML.getText());
					break;
				case "label":
					name = dataXML.getText();
					break;
				case "dummy":
					dummy = Boolean.valueOf(dataXML.getText());
					break;
				}
			}

			Station station;
			if (dummy == true) {
				station = new Station(name, x, y, dummy);
			} else {
				station = new Station(name, x, y);
				map.addStation(station);
			}
			stationsById.put(id, station);
		}

		List<Element> edgesXML = root.getChild("graph", ns).getChildren("edge", ns);
		for (Element edgeXML : edgesXML) {
			Station source = stationsById.get(edgeXML.getAttributeValue("source"));
			Station target = stationsById.get(edgeXML.getAttributeValue("target"));

			List<Element> datasXML = edgeXML.getChildren("data", ns);
			for (Element dataXML : datasXML) {
				String name = dataXML.getAttributeValue("key");
				if (Pattern.matches("l\\d+", name) == true) {
					Line line;
					if (map.getLine(name) == null) {
						line = new Line(name);
						map.addLine(line);
					} else {
						line = map.getLine(name);
					}
					line.addStation(source);
					line.addStation(target);
				}
			}
		}

		for (Map.Entry<String, Line> line : map.getLines().entrySet()) {
			Line l = line.getValue();
			l.getStations().removeIf(s -> s.isDummy() == true);
		}

		return map;
	}
}

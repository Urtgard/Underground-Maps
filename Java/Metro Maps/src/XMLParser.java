import java.util.List;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class XMLParser {
	public MetroMap getMapFromXML(String file) {
		try {
			Document doc = new SAXBuilder().build(file);
			Element root = doc.getRootElement();
			switch (root.getName()) {
			case "omm-file":
				return parseOpenMetroMap(root);
			}
		} catch (Exception e) {
		}
		return new MetroMap();
	}

	public MetroMap parseOpenMetroMap(Element root) {
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
			Boolean circular = Boolean.valueOf(lineXML.getAttributeValue("circular"));
			Line line = new Line(name, circular);

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
}

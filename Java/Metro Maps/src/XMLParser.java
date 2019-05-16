import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

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
					line.addStations(source, target);
				}
			}
		}

		// remove dummy stations and sort lines
		for (Map.Entry<String, Line> l : map.getLines().entrySet()) {
			Line line = l.getValue();
			Iterator<Station> itr = line.getStations().iterator();
			while (itr.hasNext()) {
				Station station = itr.next();
				if (station.isDummy() == true) {
					for (Line ll : station.getLines()) {
						Station stationA = station;
						Station stationB = station;
						boolean first = true;
						for (Station s : station.getAdjacentStations()) {
							if (s.getLines().indexOf(ll) != -1) {
								if (first == true) {
									stationA = s;
									first = false;
								} else {
									stationB = s;
									break;
								}
							}
						}
						stationA.addAdjacentStation(stationB);
						stationB.addAdjacentStation(stationA);
					}
					itr.remove();
				}
			}
			line.sort();
		}
		return map;
	}

	public void writeImage(MetroMap map, double[] x, double[] y) {
		Station[] stations = map.getStationsArray();
		int width = 20;
		int height = 20;
		for (double value : x) {
			if (value > width) {
				width = (int) Math.round(value);
			}
		}
		for (double value : y) {
			if (value > height) {
				height = (int) Math.round(value);
			}
		}
		width += 14 + 200;
		height += 14;

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(Color.BLACK);
		graphics.setFont(new Font("Arial", Font.PLAIN, 14));

		Utility u = new Utility();
		int i = 0;
		for (Station station : stations) {
			int n = station.getLines().size();
			for(int j = 0; j < n; j++){
				graphics.setColor(station.getLines().get(j).getColor());
				graphics.fillRect((int) Math.round(x[i]), height - (int) Math.round(y[i]) - 12 + 14/n*j, u.getStringWidth(station.getName()), 14/n);
			}
			graphics.setColor(Color.BLACK);
			graphics.drawString(station.getName(), (int) Math.round(x[i]), height - (int) Math.round(y[i]));
			i++;
		}

		try {
			ImageIO.write(bufferedImage, "png", new File("map.png"));
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("Image Created");
	}
}


public class Core {
	public static void main(String[] args) {
		XMLParser parser = new XMLParser();
		MetroMap map = parser.getMapFromXML("berlin.xml");
		// MetroMap map = parser.getMapFromXML("montreal.graphml");
		System.out.println(map);
	}
}

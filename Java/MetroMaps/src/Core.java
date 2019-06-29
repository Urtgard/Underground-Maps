
public class Core {
	public static void main(String[] args) {
		Config config = Config.getInstance();
		config.name = "wien";
		
		XMLParser parser = new XMLParser();
		// MetroMap map = parser.getMapFromXML("berlin.xml");
		MetroMap map = parser.getMapFromXML(config.name + ".graphml");
		// System.out.println(map);
		Solver s = new Solver();
		s.solve(map);
	}
}

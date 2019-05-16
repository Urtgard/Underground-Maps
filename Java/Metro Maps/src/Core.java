
public class Core {
	public static void main(String[] args) {
		XMLParser parser = new XMLParser();
		// MetroMap map = parser.getMapFromXML("berlin.xml");
		MetroMap map = parser.getMapFromXML("wien.graphml");
		//System.out.println(map);
		Solver s = new Solver();
		s.solve(map);
	}
}

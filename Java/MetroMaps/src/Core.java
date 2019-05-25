

public class Core {
	public static String graphName = "wien";
	
	
	public static void main(String[] args) {
		XMLParser parser = new XMLParser();
		// MetroMap map = parser.getMapFromXML("berlin.xml");
		Core c = new Core();
		MetroMap map = parser.getMapFromXML(graphName+".graphml");
		//System.out.println(map);
		Solver s = new Solver();
		s.solve(map);
	}
}

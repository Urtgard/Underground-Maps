package backEnd;

public class Core {
	public static void main(String[] args) {
		Config config = Config.getInstance();
		config.path = "input/montreal.graphml";
		config.name = "montreal";
		config.weights = new int[] { 25, 1, 50 };
		config.only45 = true;
		config.lazyConstraints = false;
		config.MIPGap = 0;
		config.TimeLimit = 0;
		
		XMLParser parser = new XMLParser();
		MetroMap map = parser.getMapFromXML(config.path);
		System.out.println(map);
		Solver s = new Solver();
		s.solve(map);
	}
}

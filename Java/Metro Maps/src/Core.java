
public class Core {
	public static void main(String[] args) {	
		XMLParser parser = new XMLParser();
		MetroMap map = parser.getMapFromXML("berlin.xml");
		System.out.println(map);
	}
}

package GuiTool;



public class Neighbour {
	
	String name;
	Button b;
	int eins; // Richtung im Plan
	int zwei; // Richtung in echter Karte
	
	public Neighbour(String name, int eins, int zwei){
		this.name = name;
		this.eins = eins;
		this.zwei = zwei;
		this.b = null;
	}
	
}

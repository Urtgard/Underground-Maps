package backEnd;
import GuiTool.*;
public class Config {
	private static Config instance;
	
	String name;
	int weights[];
			
	private Config() {
	}

	public static Config getInstance() {
		if (Config.instance == null) {
			Config.instance = new Config();
		}
		return Config.instance;
	}
}

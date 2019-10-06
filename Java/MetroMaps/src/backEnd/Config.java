package backEnd;

public class Config {
	private static Config instance;
	
	String path;
	String name;
	int weights[];
	boolean only45;
	boolean lazyConstraints;
	int MIPGap;
	int TimeLimit;
			
	private Config() {
	}

	public static Config getInstance() {
		if (Config.instance == null) {
			Config.instance = new Config();
		}
		return Config.instance;
	}
}

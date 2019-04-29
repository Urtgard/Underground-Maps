import java.util.ArrayList;

public class Line {
	private String name;
	private Boolean circular;
	private ArrayList<Station> stations = new ArrayList<>();
	
	Line(String n, Boolean c) {
		name = n;
		circular = c;
	}

	public void addStation(Station station) {
		stations.add(station);
	}
	
	@Override
	public String toString() { 
	    String result = name+":";
	    for (Station station: stations) {
	    	result += "\n "+station.getName();
	    }
	    return result;
	} 
}

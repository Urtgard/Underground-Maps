package GuiTool;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Displayer {

	
	
	public Displayer(List <Station> stations) {
		
		
		JFrame window = createDisplay();
		setButtons(window, stations);
	}
	
	
	//erstellt Hintergrund
	public JFrame createDisplay() {
		
		JFrame frame = new JFrame();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();	
		frame.setSize((int)dim.getWidth()-100, (int)dim.getHeight()-100);
		frame.setLocation(0, 0);
		frame.setTitle("U-Bahn Visualisation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);	
		return frame;
	}
	
	//soll Buttons initalisieren
	public void setButtons(JFrame window, List <Station> stations) {
		
		//stellt Größe des Bildschirms fest
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();	
		int width = (int)dim.getWidth()-100;
		int height = (int)dim.getHeight()-100;
		
		
		double maxX = stations.get(0).getX();
		double minX = maxX;
		double maxY = stations.get(0).getY();
		double minY = maxY;
		
		
		//finde max und min Koordinaten
		for(int i=0; i< stations.size();i++) {
			if(stations.get(i).getX()>maxX) {
				maxX = stations.get(i).getX();
			}
			if(stations.get(i).getX()<minX) {
				minX = stations.get(i).getX();
			}	
			if(stations.get(i).getY()>maxY) {
				maxY = stations.get(i).getY();
			}		
			if(stations.get(i).getX()<minY) {
				minY = stations.get(i).getY();
			}
		}
		
		//DIESER TEIL IST NOCH NCIHT FERTIG. DIES IST ALSO NUR EINE VORLAGE
		
		//platziere Buttons
		for(int i=0;i<stations.size();i++) {
			JButton tmp = new JButton(stations.get(i).getName());
			
			
			
		}
		
		
	}
}

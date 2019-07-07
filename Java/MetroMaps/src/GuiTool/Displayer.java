package GuiTool;

import java.awt.Color;

import java.awt.Dimension;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import backEnd.Line;
import java.util.ArrayList;
import java.util.Map;
import java.awt.image.BufferedImage;




import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;



public class Displayer {

	ArrayList<JButton> list;

	
	public Displayer(ArrayList <Button> buttons) {
		
		
		JFrame window = createDisplay();

		this.setButtons(window, buttons);

		setButtons(window, buttons);
		window.setVisible(true);

	}
	
	
	//erstellt Hintergrund
	public JFrame createDisplay() {
		
		JFrame frame = new JFrame();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();	
		frame.setSize((int)dim.getWidth(), (int)dim.getHeight());
		frame.setLocation(0, 0);
		frame.setTitle("U-Bahn Visualisation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);	
		return frame;
	}
	
	//soll Buttons initalisieren
	public void setButtons(JFrame window, ArrayList <Button> buttons) {
		
		JPanel p = new JPanel();
		
		//stellt Größe des Bildschirms fest
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();	
		int width = (int)dim.getWidth()-100;
		int height = (int)dim.getHeight()-40;
		
		p.setSize((int)dim.getWidth(), (int)dim.getHeight());
		p.setLayout(null);
		
		double maxX = buttons.get(0).getX();
		
		double maxY = buttons.get(0).getY();
		
		
		
		//finde max und min Koordinaten
		for(int i=0; i< buttons.size();i++) {
			if(buttons.get(i).getX()>maxX) {
				maxX = buttons.get(i).getX();
			}
			if(buttons.get(i).getY()>maxY) {
				maxY = buttons.get(i).getY();
			}	
		}
		maxX = width/maxX;
		maxY = height/maxY;
		
		
		
		ArrayList<JButton> list = new ArrayList<JButton>();
		
		//platziere Buttons
		for(int i=0;i<buttons.size();i++) {
			JButton tmp = new JButton(buttons.get(i).getName());
			Listener l = new Listener(buttons.get(i));
			tmp.addActionListener(l);
			//erzeuge maße von Button und Location
			int breite =  getStringWidth(buttons.get(i).getName());
			tmp.setBounds((int)(((buttons.get(i).getX()-breite/2)*maxX)+50), (int)(height - (buttons.get(i).getY()+7)*maxY +20),(int)(breite*maxX), (int)(14*maxY));
			
			list.add(tmp);

			p.add(tmp);
		}
	
		/*
		Map<String, Line> m = buttons.get(0).getL();
		ArrayList<Line> goodList = new ArrayList<Line>();
		
		for(Line value : m.values()) {
			goodList.add(value);
		}
		
		for(int i=0;i<goodList.size();i++) {
			for(int q=0;q<goodList.get(i).getStations().size()-1;q++) {
				for(int z=0;z<buttons.size();z++) {
					if(buttons.get(z).getName()==goodList.get(i).getStations().get(q).getName()) {
						for(int b=0;b<buttons.size();b++) {
							if(buttons.get(b).getName()==goodList.get(i).getStations().get(q+1).getName()) {
								paintLines(p, (int)(buttons.get(z).getX()*maxX+50), (int)(height - (buttons.get(z).getY()*maxY +20)), )
							}
						}
					}
				}
			}
		}
		*/
		
		//Speichere Buttons, um später darauf zugreifen zu können
		this.list = list;
		p.setVisible(true);
		window.add(p);
	}
	
	public int getStringWidth(String str) {
		BufferedImage i2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = i2.createGraphics();
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		FontMetrics fm = g.getFontMetrics();
		// Rectangle2D bounds = fm.getStringBounds(str, g);
		// return (int) bounds.getWidth();
		return fm.stringWidth(str);
	}

	
	protected void paintLines(Graphics g, int StartX, int StartY, int EndX, int EndY, int lineNumber) {
		 
		if(lineNumber ==0) {
			
		}
		
		g.setColor( Color.red );
		// X Start, Y Start, X End, Y End
		// X = <---------->
		g.drawLine ( 0, 0, 240, 50 );
 
	}
}

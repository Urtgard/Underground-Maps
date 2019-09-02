package GuiTool;



import java.awt.Dimension;

import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.Graphics2D;
import java.awt.Toolkit;

import java.util.ArrayList;
import java.util.Map;
import java.awt.image.BufferedImage;




import javax.swing.JButton;
import javax.swing.JFrame;

import backEnd.Line;




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
		
		//weißt einem Button seine Nachbarn zu
		for(int i=0;i<buttons.size();i++) {
			for(int z =0;z<buttons.get(i).getN().size();z++) {
				for(int p=0;p<buttons.size();p++) {
					if(buttons.get(i).getN().get(z).name==buttons.get(p).getName()) {
						buttons.get(i).getN().get(z).b = buttons.get(p);
					}
				}
			}
		}
		/*
		for(int i=0;i<buttons.size();i++) {
			System.out.println("Station is "+buttons.get(i).getName());
			System.out.println("Neighbours are");
			for(int z =0;z<buttons.get(i).getN().size();z++) {
				System.out.println(buttons.get(i).getN().get(z).name);
			}
			
		}
		*/
		panel p = new panel();
		
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
			Listener l = new Listener(buttons.get(i), p);
			tmp.addActionListener(l);
			//erzeuge maße von Button und Location
			int breite =  getStringWidth(buttons.get(i).getName());
			
			tmp.setBounds((int)(((buttons.get(i).getX()-breite/2)*maxX)+50), (int)(height - (buttons.get(i).getY()+7)*maxY +20),(int)(breite*maxX), (int)(14*maxY));
			
			list.add(tmp);

			p.add(tmp);
		}
	
		
		//Speichere Buttons, um später darauf zugreifen zu können
		this.list = list;
		
		paintLines(p , buttons, width, height, maxX, maxY);
		
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

	public void paintLines(panel p, ArrayList<Button> buttons, int width, int height, double maxX, double maxY) {
		
		if(p.segments != null) {
			p.repaint();
		}
		else {
			p.initialised=true;
			p.buttons = buttons;
			p.width = width;
			p.height = height;
			p.maxX = maxX;
			p.maxY = maxY;
			
			Map<String, Line> m = buttons.get(0).getL();
			ArrayList<Line> goodList = new ArrayList<Line>();
			
			for (Line value : m.values()) {
				goodList.add(value);
			}

			ArrayList<Segment> segments = new ArrayList<Segment>();

			for (int i = 0; i < goodList.size(); i++) {
				
				for (int q = 0; q < goodList.get(i).getStations().size() - 1; q++) {
					for (int z = 0; z < buttons.size(); z++) {

						if (buttons.get(z).getName() == goodList.get(i).getStations().get(q).getName()) {

							for (int b = 0; b < buttons.size(); b++) {

								if (buttons.get(b).getName() == goodList.get(i).getStations().get(q + 1)
										.getName()) {

									int xStart = (int) (buttons.get(z).getX() * maxX + 50);
									int yStart = (int) (height - (buttons.get(z).getY()) * maxY + 20);
									int xEnd = (int) (buttons.get(b).getX() * maxX + 50);
									int yEnd = (int) (height - (buttons.get(b).getY()) * maxY + 20);
									
									//System.out.println(xStart+" " + yStart +" " + xEnd +" "+ yEnd);
									Segment seg = new Segment(xStart,
											yStart,
											xEnd,
											yEnd, i % 10);

									segments.add(seg);
									
									/*
									g.drawLine((int) (buttons.get(z).getX() * maxX + 50),
											(int) (height - (buttons.get(z).getY()) * maxY + 20),
											(int) (buttons.get(b).getX() * maxX + 50),
											(int) (height - (buttons.get(b).getY()) * maxY + 20));
											*/
								}
							}
						}
					}
				}
			}
			p.segments = segments;	
			p.repaint();
		}
	}
	
	
}

package GuiTool;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


import javax.swing.JButton;
import javax.swing.JFrame;

public class Displayer {

	
	
	public Displayer(ArrayList <Button> buttons) {
		
		
		JFrame window = createDisplay();
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
		
		//stellt Größe des Bildschirms fest
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();	
		int width = (int)dim.getWidth()-100;
		int height = (int)dim.getHeight()-40;
		
		
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
		
		
		
		
		//platziere Buttons
		for(int i=0;i<buttons.size();i++) {
			JButton tmp = new JButton(buttons.get(i).getName());
			Listener l = new Listener(buttons.get(i));
			tmp.addActionListener(l);
			//erzeuge maße von Button und Location
			int breite =  getStringWidth(buttons.get(i).getName());
			tmp.setBounds((int)(((buttons.get(i).getX()-breite/2)*maxX)+50), (int)(height - (buttons.get(i).getY()+7)*maxY +20),(int)(breite*maxX), (int)(14*maxY));
			
			window.add(tmp);
			
		}
		
		
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

}

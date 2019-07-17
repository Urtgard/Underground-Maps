package GuiTool;

import java.awt.BasicStroke;
import java.awt.Color;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JPanel;
import backEnd.Line;

public class panel extends JPanel {

	public boolean initialised = false;
	ArrayList<Button> buttons;
	int width;
	int height;
	double maxX;
	double maxY;
	boolean drawConstraint = false;
	Button toDraw;
	ArrayList<Segment> segments = null;

	public void drawSec(int x1, int y1, int sec, Graphics2D g2d) {
		float[] dash1 = { 2f, 0f, 2f };
		BasicStroke bs1 = new BasicStroke(2, 
		        BasicStroke.CAP_BUTT, 
		        BasicStroke.JOIN_ROUND, 
		        1.0f, 
		        dash1,
		        2f);
	    g2d.setStroke(bs1);
		
		int x2 = 0;
		int y2 = 0;
		//g.setColor(Color.BLUE);
		switch(sec){
			case 0:
				x2 = x1 + 40;
				y2 = y1;
				break;
			case 1:
				x2 = x1 + 28;
				y2 = y1 - 28;
				break;
			case 2:
				x2 = x1;
				y2 = y1 - 40;
				break;
			case 3:
				x2 = x1 - 28;
				y2 = y1 - 28;
				break;
			case 4:
				x2 = x1 - 40;
				y2 = y1;
				break;
			case 5:
				x2 = x1 - 28;
				y2 = y1 + 28;
				break;
			case 6:
				x2 = x1;
				y2 = y1 + 40;
				break;
			case 7:
				x2 = x1 + 28;
				y2 = y1 + 28;
				break;
		}
		g2d.drawLine(x1, y1, x2, y2);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		
		// called through repaint

		if (this.drawConstraint) {

			super.paint(g);
			//Hier muss die Linie zur eigentlichen Position erstellt werden
			Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK, Color.LIGHT_GRAY,
					Color.MAGENTA, Color.ORANGE, Color.PINK, Color.CYAN };
			for (int i = 0; i < this.toDraw.getN().size(); i++) {
				int step;
				int secOrig = this.toDraw.getN().get(i).zwei;
				if (this.toDraw.getN().get(i).eins == this.toDraw.getN().get(i).zwei) {
					step = 0;
				} else if (this.toDraw.getN().get(i).eins == (this.toDraw.getN().get(i).zwei+1)%8) {
					step = 1;
				} else {
					step = -1;
				}
					
				int x1 = (int) (toDraw.getX() * maxX + 50);
				int y1 = (int) (height - (toDraw.getY()) * maxY + 20);
				
				g2d.setStroke(new BasicStroke(2));
				g2d.setColor(colors[i]);
				int x2 = (int) (toDraw.getN().get(i).b.getX() * maxX + 50);
				int y2 = (int) (height - (toDraw.getN().get(i).b.getY()) * maxY + 20);
				g2d.drawLine(x1, y1, x2, y2);
				
				if (step == 0 || step == -1) {
					// zeichne nächsten Sektor
					drawSec(x1+2*i, y1+2*i, (secOrig+1)%8, g2d);
				}
				
				if (step == 0 || step == 1) {
					// zeichne vorherigen Sektor
					drawSec(x1+2*i, y1+2*i, (secOrig-1+8)%8, g2d);
				}
				
				if (step == -1 || step == 1) {
					// zeichne originalen Sektor
					drawSec(x1+2*i, y1+2*i, secOrig, g2d);
				}
			}
			/*
			Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK, Color.LIGHT_GRAY,
					Color.MAGENTA, Color.ORANGE, Color.PINK, Color.CYAN };
	
			if (this.segments != null) {
				// System.out.println(segments.size());

				for (int i = 0; i < segments.size(); i++) {
					g.setColor(colors[segments.get(i).getColor()]);
					g.drawLine(segments.get(i).getXStart(), segments.get(i).getYStart(), segments.get(i).getXEnd(),
							segments.get(i).getYEnd());
				}
			}
			*/
			this.toDraw = null;
			this.drawConstraint = false;

		} else {

			if (this.initialised) {

				Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK, Color.LIGHT_GRAY,
						Color.MAGENTA, Color.ORANGE, Color.PINK, Color.CYAN };
				super.paint(g);

				if (this.segments != null) {
					// System.out.println(segments.size());

					for (int i = 0; i < segments.size(); i++) {
						g.setColor(colors[segments.get(i).getColor()]);
						/*
						 * System.out.println("New Segment");
						 * System.out.println(segments.get(i).getXStart());
						 * System.out.println(segments.get(i).getYStart());
						 * System.out.println(segments.get(i).getXEnd());
						 * System.out.println(segments.get(i).getYEnd());
						 * 
						 * System.out.println(segments.get(i).getColor());
						 */
						g.drawLine(segments.get(i).getXStart(), segments.get(i).getYStart(), segments.get(i).getXEnd(),
								segments.get(i).getYEnd());
					}
				}
			}
		}
	}

}

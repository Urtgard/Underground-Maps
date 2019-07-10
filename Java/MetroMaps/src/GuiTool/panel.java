package GuiTool;

import java.awt.Color;

import java.awt.Graphics;
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

	@Override
	public void paint(Graphics g) {

		// called through repaint

		if (this.drawConstraint) {

			super.paint(g);
			//Hier muss die Linie zur eigentlichen Position erstellt werden
			
			for (int i = 0; i < this.toDraw.getN().size(); i++) {

				if (this.toDraw.getN().get(i).eins != this.toDraw.getN().get(i).zwei) {
					g.setColor(Color.RED);

					g.drawLine((int) (toDraw.getX() * maxX + 50), (int) (height - (toDraw.getY()) * maxY + 20),
							(int) (toDraw.getN().get(i).b.getX() * maxX + 50),
							(int) (height - (toDraw.getN().get(i).b.getY()) * maxY + 20));
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

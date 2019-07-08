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

	@Override
	public void paint(Graphics g) {

		if (this.initialised) {
			super.paint(g);
			
			Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK ,Color.LIGHT_GRAY,Color.MAGENTA, Color.ORANGE,Color.PINK, Color.CYAN};

			Map<String, Line> m = buttons.get(0).getL();
			ArrayList<Line> goodList = new ArrayList<Line>();

			for (Line value : m.values()) {
				goodList.add(value);
			}

			for (int i = 0; i < goodList.size(); i++) {
				g.setColor(colors[i%10]);
				for (int q = 0; q < goodList.get(i).getStations().size() - 1; q++) {
					for (int z = 0; z < buttons.size(); z++) {
						
						if (buttons.get(z).getName() == goodList.get(i).getStations().get(q).getName()) {
							
							for (int b = 0; b < buttons.size(); b++) {
								
								if (buttons.get(b).getName() == goodList.get(i).getStations().get(q + 1).getName()) {

									
									g.drawLine((int) (buttons.get(z).getX() * maxX+50),
											(int) (height - (buttons.get(z).getY()) * maxY+20),
											(int) (buttons.get(b).getX() * maxX+50),
											(int) (height - (buttons.get(b).getY()) * maxY+20));
								}
							}
						}
					}
				}
			}

		}
	}
	
	
	

}

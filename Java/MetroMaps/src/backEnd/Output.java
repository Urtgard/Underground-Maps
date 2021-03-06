package backEnd;
import java.awt.BasicStroke;
import GuiTool.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;

public class Output {
	boolean bigline = false;
	
	public void createImage(MetroMap map, double[] x, double[] y) {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH-mm-ss");
		Date date = new Date();
		String name = dateFormat.format(date);
		createImage(map, x, y, name);
	}

	public void createWindow(MetroMap map, double[] x, double[] y, ArrayList<ArrayList<int[]>> lageBez){
		
		ArrayList<Button> buttonlist = new ArrayList<Button>();
		for(int i=0;i<map.getStations().size();i++){
			Station s = map.getStation(i);
			
			ArrayList<Neighbour> N = new ArrayList<Neighbour>();
			for(int j = 0; j < s.getAdjacentStations().size(); j++){
				Neighbour n = new Neighbour(s.getAdjacentStations().get(j).getName(), lageBez.get(i).get(j)[0], lageBez.get(i).get(j)[1]);
				N.add(n);
			}

			ArrayList<String> linien = new ArrayList<String>();
			for(Line l : map.getStations().get(i).getLines()){
				linien.add(l.getName());
			}
			
			Button b = new Button(linien, s.getName(), x[i], y[i], getStringWidth(s.getName()), N, map.getLines());
			buttonlist.add(b);
		}
	}

	public void createImage(MetroMap map, double[] x, double[] y, String name) {
		int n = map.getStations().size();
		boolean[][] lineDrawn = new boolean[n][n];
		int width = 20;
		int height = 20;
		for (double value : x) {
			if (value > width) {
				width = (int) Math.round(value);
			}
		}

		for (double value : y) {
			if (value > height) {
				height = (int) Math.round(value);
			}
		}

		width += 14 + 200;
		height += 14;
		width = Math.min(width, 20000);
		height = Math.min(height, 20000);
		BufferedImage bufferedImage = new BufferedImage(width, height + 20, BufferedImage.TYPE_INT_RGB);

		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height + 20);
		graphics.setColor(Color.BLACK);
		graphics.setFont(new Font("Arial", Font.PLAIN, 14));

		Utility u = new Utility();
		for (int i = 0; i < n; i++) {
			
			Station station = map.getStation(i);
			int numLines = station.getLines().size();
			for (int j = 0; j < numLines; j++) {
				graphics.setColor(station.getLines().get(j).getColor());
				graphics.drawLine((int) Math.round(x[i]) - u.getStringWidth(station.getName())/2, height - (int) Math.round(y[i]) - 12 + 14 / numLines * j,
						(int) Math.round(x[i]) + u.getStringWidth(station.getName())/2, height - (int) Math.round(y[i]) - 12 + 14 / numLines * j);
			}
			graphics.setColor(Color.BLACK);
			Graphics2D g2 = (Graphics2D) graphics;
			g2.setStroke(new BasicStroke(2));
			
	graphics.drawString(station.getName(), (int) x[i] - u.getStringWidth(station.getName())/2, height - (int) y[i] + 6);

			if (bigline == true) {
				for (Station s : station.getAdjacentStations()) {
					int j = map.getStationIndex(s);
					if (lineDrawn[i][j] == false) {
						if (station.getY() != s.getY()) {
							if (station.getY() > s.getY()) {
								int xpoints[] = { (int) x[i], (int) x[i] + u.getStringWidth(station.getName()),
										(int) x[j] + u.getStringWidth(s.getName()), (int) x[j] };
								int ypoints[] = { height - (int) y[i] + 2, height - (int) y[i] + 2,
										height - (int) y[j] - 12, height - (int) y[j] - 12 };
								int npoints = 4;
								graphics.setColor(new Color(0, 0, 0, 128));
								graphics.fillPolygon(xpoints, ypoints, npoints);
								lineDrawn[i][j] = true;
								lineDrawn[j][i] = true;
							}
						} else {
							if (station.getX() < s.getX()) {
								int xpoints[] = { (int) x[i] + u.getStringWidth(station.getName()),
										(int) x[i] + u.getStringWidth(station.getName()), (int) x[j], (int) x[j] };
								int ypoints[] = { height - (int) y[i] + 2, height - (int) y[i] - 12,
										height - (int) y[j] - 12, height - (int) y[j] + 2 };
								int npoints = 4;
								graphics.setColor(new Color(0, 0, 0, 128));
								graphics.fillPolygon(xpoints, ypoints, npoints);
								lineDrawn[i][j] = true;
								lineDrawn[j][i] = true;
							}
						}
					}
				}
			}
		}

		try {
			String OS = System.getProperty("os.name").toLowerCase();
			Config config = Config.getInstance();
			String graphName = config.name;
			String weights = config.weights[0]+"-"+config.weights[1]+"-"+config.weights[2];
			name = weights +"_"+ name;

			if (OS.contains("win")) {
				File theDirOut = new File("output");
				if (!theDirOut.exists()) {
					try {
						theDirOut.mkdir();
					} catch (SecurityException se) {
						System.out.println("Problem mit der Output Folder Erstellung bei Windows");
					}
				}
				File theDir = new File("output/" + graphName);
				if (!theDir.exists()) {
					theDir.mkdirs();
				}
				
				ImageIO.write(bufferedImage, "png",
						new File("output\\" + graphName +"\\" + name + ".png"));

			} else if (OS.contains("mac")) {
				File theDirOut = new File("output");
				if (!theDirOut.exists()) {
					try {
						theDirOut.mkdir();
						File theDir = new File("output/" + graphName);
						if (!theDir.exists()) {
							theDir.mkdirs();
						}
						ImageIO.write(bufferedImage, "png",
								new File("output/" + graphName +"/" + name + ".png"));
					} catch (SecurityException se) {
						System.out.println("Problem mit der Output Folder Erstellung bei Mac");
					}
				} else {
					try {
						File theDir = new File("output/" + graphName);
						if (!theDir.exists()) {
							theDir.mkdirs();
						}
						ImageIO.write(bufferedImage, "png",
								new File("output/" + graphName + "/" + name + ".png"));
					} catch (SecurityException se) {
						System.out.println("Problem mit der Output Folder Erstellung bei Mac");
					}
				}

			} else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
				File theDirOut = new File("output");
				if (!theDirOut.exists()) {
					try {
						theDirOut.mkdir();
						File theDir = new File("output/" + graphName);
						if (!theDir.exists()) {
							theDir.mkdirs();
						}
						ImageIO.write(bufferedImage, "png",
								new File("output/" + graphName +"/" + name + ".png"));
					} catch (SecurityException se) {
						System.out.println("Problem mit der Output Folder Erstellung bei Linux");
					}
				} else {
					try {
						File theDir = new File("output/" + graphName);
						if (!theDir.exists()) {
							theDir.mkdirs();
						}
						ImageIO.write(bufferedImage, "png",
								new File("output/" + graphName + "/" + name + ".png"));
					} catch (SecurityException se) {
						System.out.println("Problem mit der Output Folder Erstellung bei Linux");
					}
				}
			}

		} catch (

		Exception e) {
			System.out.println(e);
		}

		System.out.println("Image Created in Directory " + System.getProperty("user.dir"));
	}
	//eingefügt, im Breite für jeden Button zu bestimmen
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

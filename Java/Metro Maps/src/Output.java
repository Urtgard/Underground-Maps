import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class Output {
	public void createImage(MetroMap map, double[] x, double[] y) {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH-mm-ss");
		Date date = new Date();
		String name = dateFormat.format(date);
		createImage(map, x, y, name);
	}
	
	public void createImage(MetroMap map, double[] x, double[] y, String name) {
		Station[] stations = map.getStationsArray();
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

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(Color.BLACK);
		graphics.setFont(new Font("Arial", Font.PLAIN, 14));

		Utility u = new Utility();
		int i = 0;
		for (Station station : stations) {
			int n = station.getLines().size();
			for(int j = 0; j < n; j++){
				graphics.setColor(station.getLines().get(j).getColor());
				graphics.fillRect((int) Math.round(x[i]), height - (int) Math.round(y[i]) - 12 + 14/n*j, u.getStringWidth(station.getName()), 14/n);
			}
			graphics.setColor(Color.BLACK);
			graphics.drawString(station.getName(), (int) Math.round(x[i]), height - (int) Math.round(y[i]));
			i++;
		}

		try {
			ImageIO.write(bufferedImage, "png", new File("output\\"+name+".png"));
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("Image Created");
	}
}

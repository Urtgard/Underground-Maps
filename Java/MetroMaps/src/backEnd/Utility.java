package backEnd;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Utility {
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

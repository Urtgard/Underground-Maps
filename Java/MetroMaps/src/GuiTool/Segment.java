package GuiTool;

public class Segment {

	private int XStart;
	private int YStart;
	private int XEnd;
	private int YEnd;
	private int color;

	public Segment(int XStart, int YStart, int XEnd, int YEnd, int color) {
		this.XStart = XStart;
		this.YStart = YStart;
		this.XEnd = XEnd;
		this.YEnd = YEnd;
		this.color = color;
	}

	// getters und setter
	public int getXStart() {
		return XStart;
	}

	public void setXStart(int xStart) {
		XStart = xStart;
		
	}

	public int getYStart() {
		return YStart;
	}

	public void setYStart(int yStart) {
		YStart = yStart;
	}

	public int getXEnd() {
		return XEnd;
	}

	public void setXEnd(int xEnd) {
		XEnd = xEnd;
	}

	public int getYEnd() {
		return YEnd;
	}

	public void setYEnd(int yEnd) {
		YEnd = yEnd;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

}

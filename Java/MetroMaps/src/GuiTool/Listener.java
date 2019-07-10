package GuiTool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Listener implements ActionListener{

	public Button s;
	public panel p;
	public boolean clicked = true;
	
	public Listener(Button s, panel p) {
		this.s = s;
		this.p =p;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(clicked) {
			
			this.p.toDraw = s;
			this.p.drawConstraint = true;
			this.p.repaint();
			clicked = false;
		}else {
			
			this.p.repaint();
			clicked = true;
		}
		
		
		
		
		
		
		/*
		System.out.println(this.s.getName());

		System.out.println("  Linien: ");
		for(int i=0;i<this.s.getLinienName().size();i++) {
			System.out.println("    "+this.s.getLinienName().get(i));
		}
		System.out.println("  Nachbarn: ");
		for(int i=0;i<this.s.getN().size();i++) {
			System.out.println("    "+this.s.getN().get(i).name);
			System.out.println("      SekPlan: "+this.s.getN().get(i).eins);
			System.out.println("      SekOrig: "+this.s.getN().get(i).zwei+"\n");
		}
		*/
	}

}

package GuiTool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Listener implements ActionListener{

	public Button s;
	
	public Listener(Button s) {
		this.s = s;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(this.s.getName());
		
		for(int i=0;i<this.s.getLinienName().size();i++) {
			System.out.println(this.s.getLinienName().get(i));
		}
		System.out.println("");
		for(int i=0;i<this.s.getN().size();i++) {
			System.out.println(this.s.getN().get(i).name);
			System.out.println(this.s.getN().get(i).eins);
			System.out.println(this.s.getN().get(i).zwei);
		}
	}

}

package GuiTool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Listener implements ActionListener{

	public Station s;
	public Listener(Station s) {
		this.s = s;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(this.s.getName());
		for(int i=0;i<this.s.getConstraints().size();i++) {
			System.out.println(this.s.getConstraints().get(i));
		}
	}

}

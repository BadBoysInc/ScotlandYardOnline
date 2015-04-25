package player;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import scotlandyard.ScotlandYardView;
import gui.Gui;


@SuppressWarnings("serial")
public class GUI2 extends Gui{

	public GUI2(ScotlandYardView arg0, String arg1, String arg2) throws IOException {
		super(arg0, arg1, arg2);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowClosed(WindowEvent e) {System.exit(0);}
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
        });
	}
	
	

}

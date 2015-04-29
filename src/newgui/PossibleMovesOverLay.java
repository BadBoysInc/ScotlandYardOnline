package newgui;

import java.awt.Graphics2D;
import java.io.IOException;

import javax.swing.JFrame;

public class PossibleMovesOverLay extends JFrame {

	public PossibleMovesOverLay(){
		super();
		setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
	
		try {
			add(new PossiblePositionsCanvas());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.setSize(1018, 809);
		
		this.setVisible(true);
	}
	
}

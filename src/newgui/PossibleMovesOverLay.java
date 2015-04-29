package newgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class PossibleMovesOverLay extends JFrame{

	static List<Integer> pos;
		
	public PossibleMovesOverLay(){
		super();
		setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
		setSize(1030, 840);
		setLocation(280, 0);
		try {
			pos = new ArrayList<Integer>();
			pos.add(new Integer(1));
			pos.add(new Integer(88));
			add(new PossiblePositionsPanel());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		this.setVisible(true);
	}

	
    
	public void updatePositions(List<Integer> possibleLocations){
	
		System.out.println("Notified");
		
		pos = possibleLocations;
		
		
	}	
	
	
}

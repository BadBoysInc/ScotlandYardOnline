package newgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import scotlandyard.Move;

public class PossibleMovesOverLay extends JFrame{

	static List<Integer> pos = null;
	static int recommendedMove = 0;
		
	public PossibleMovesOverLay(){
		super();
		setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
		setSize(1030, 840);
		setLocation(280, 0);
		try {
			pos = new ArrayList<Integer>();
			add(new PossiblePositionsPanel());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		this.setVisible(true);
	}

	
	public void clear(){
		recommendedMove = 0;
	}
    
	public void updatePositions(List<Integer> possibleLocations){
		pos = possibleLocations;
	}



	public void updatePositions(List<Integer> possibleLocations, int bestMove) {
		updatePositions(possibleLocations);
		recommendedMove = bestMove;
	}	
	
	
}

package newgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import scotlandyard.Move;

/**
 * A Gui that indicates the possible locations of Mr X and the recommend moves for the detective.
 */
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

	/**
	 * Hides the last recommended location.
	 */
	public void clear(){
		recommendedMove = 0;
	}
    
	/**
	 * Updates the possible locations show to 'possibleLocations'
	 * @param possibleLocations
	 */
	public void updatePositions(List<Integer> possibleLocations){
		pos = possibleLocations;
	}

	/**
	 * Updates the possible locations show to 'possibleLocations' and the recommended location to 'bestMove'
	 * @param possibleLocations
	 * @param bestMove
	 */

	public void updatePositions(List<Integer> possibleLocations, int bestMove) {
		updatePositions(possibleLocations);
		recommendedMove = bestMove;
	}	
	
	
}

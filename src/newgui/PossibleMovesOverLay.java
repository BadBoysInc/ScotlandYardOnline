package newgui;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;

import scotlandyard.Move;
import scotlandyard.MoveDouble;
import scotlandyard.MoveTicket;
import scotlandyard.Spectator;
import scotlandyard.Ticket;

public class PossibleMovesOverLay extends JFrame implements Spectator {

	static Set<Integer> pos;
		
	public PossibleMovesOverLay(){
		super();
		setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
		setSize(1030, 840);
		setLocation(280, 0);
		try {
			pos = new HashSet<Integer>();
			pos.add(new Integer(1));
			pos.add(new Integer(88));
			add(new PossiblePositionsPanel());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		this.setVisible(true);
	}

	
    
	
	@Override
	public void notify(Move move) {
		System.out.println("Notified");
		
		if(move instanceof MoveTicket){
			pos.add(((MoveTicket) move).target);			
		}else if(move instanceof MoveDouble){
			pos.add(((MoveDouble) move).move2.target);			
		}
		
		
	}
	
}

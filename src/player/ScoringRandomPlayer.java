package player;

import java.util.Random;
import java.util.Set;

import scotlandyard.Move;
import scotlandyard.ScotlandYardView;

public class ScoringRandomPlayer extends RandomPlayer {

	ScotlandYardView view;
	String graphFilename;
	
	public ScoringRandomPlayer(ScotlandYardView view, String graphFilename) {
		super(view, graphFilename);
		this.view = view;
		this.graphFilename = graphFilename;
	}
	
	private int score(){
		return -1;
	}

	@Override
    public Move notify(int location, Set<Move> moves) {
        //TODO: Some clever AI here ...
		System.out.println(score());
		return super.notify(location, moves);
    }
	
	

}

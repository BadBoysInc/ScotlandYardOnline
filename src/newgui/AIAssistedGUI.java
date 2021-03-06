package newgui;
import gui.Gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import player.ScoreBoard;
import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.MoveDouble;
import scotlandyard.MoveTicket;
import scotlandyard.Route;
import scotlandyard.ScotlandYardGraphReader;
import scotlandyard.ScotlandYardView;
import scotlandyard.Ticket;


@SuppressWarnings("serial")
public class AIAssistedGUI extends Gui{
	
	List<Integer> possibleLocations;
	ScotlandYardView view;
	final private Graph<Integer, Route> graph;
	List<Edge<Integer, Route>> edges;
	Colour firstPlayer;
	Move mrXMove;	
	Ticket mrXT1;
	Ticket mrXT2;
	PossibleMovesOverLay overlay;
	
	public AIAssistedGUI(ScotlandYardView v, String imageFilename, String positionsFilename, PossibleMovesOverLay overlay) throws IOException {
		super(v, imageFilename, positionsFilename);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		super.addWindowListener(new WindowListener() {
			@Override
			public void windowClosed(WindowEvent e) {
				overlay.dispose();
				System.exit(0);
				}
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
		
		view = v;	
		firstPlayer = null;
		possibleLocations = new ArrayList<Integer>();			
		this.overlay = overlay;
		ScotlandYardGraphReader reader 	= new ScotlandYardGraphReader();
		graph = reader.readGraph("./resources/graph.txt/");
		edges = new ArrayList<Edge<Integer, Route>>(graph.getEdges());
		
	}
	
	/**
	 * Called to ask for detective move to make.
	 */
	public Move notify(int location, Set<Move> moves){
		consoleAssist(moves);
		return super.notify(location, moves);
	}
	
	/**
	 * Called as spectator whenever a move is made.
	 */
	public void notify(Move move){
		if(move.colour.equals(Colour.Black) && view.getPlayerLocation(Colour.Black) != 0){
			mrXMove = move;
			if(move instanceof MoveTicket){
				mrXT1 = ((MoveTicket) move).ticket;			
			}else if(move instanceof MoveDouble){
				mrXT1	= ((MoveDouble) move).move1.ticket;
				mrXT2	= ((MoveDouble) move).move2.ticket;
			}
			if(mrXMove instanceof MoveDouble && view.getRounds().get(view.getRound()-1)){
				mrXMove = ((MoveDouble) mrXMove).move2;
				mrXT1 	= ((MoveDouble) mrXMove).move2.ticket;
				possibleLocations = new ArrayList<Integer>();
				possibleLocations.add(view.getPlayerLocation(Colour.Black));
			}
			if(view.getRounds().get(view.getRound())){
				possibleLocations = new ArrayList<Integer>();
				possibleLocations.add(view.getPlayerLocation(Colour.Black));
			}else{SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run() {
					updatePossibleLocations();
				}
				
			});
				
			}
		}
		super.notify(move);
	}
	
	/**
	 * Update the possible places MrX.
	 */
	private void updatePossibleLocations() {
		if(view.getPlayerLocation(Colour.Black) != 0){
			List<Integer> newPossibleLocations = new ArrayList<Integer>();
			for(Integer l: possibleLocations){
				for(Move m: validMoves(l, Colour.Black)){
					if(mrXMove instanceof MoveTicket && m instanceof MoveTicket){
						if(((MoveTicket) m).ticket == mrXT1 && !newPossibleLocations.contains(((MoveTicket) m).target))
							newPossibleLocations.add(((MoveTicket) m).target);
					}else if(mrXMove instanceof MoveDouble && m instanceof MoveDouble){
						if(((MoveDouble) m).move1.ticket == mrXT1 && ((MoveDouble) m).move2.ticket == mrXT2 && !newPossibleLocations.contains(((MoveDouble) m).move2.target))
							newPossibleLocations.add(((MoveDouble) m).move2.target);
					}
				}
			}
			possibleLocations = newPossibleLocations;
		}
	}
	
    /**
     * Returns the possible moves a player can make.
     * @param location
     * @param player
     * @return List of valid Moves
     */
    protected List<Move> validMoves(int location, Colour player) {
    	//Adds all the moves around a players current location.
        List<Move> movesSingle = singleMoves(location, player);
        List<Move> moves = new ArrayList<Move>(movesSingle);
        //Adds double-moves to Mr.X's valid moves.
        if(hasTickets(Ticket.Double, player, 1)){
        	for(Move m: movesSingle){
        		List<Move> doubleMoves = singleMoves(((MoveTicket) m).target, player);
        		for(Move dm: doubleMoves){
        			if((((MoveTicket) dm).ticket == ((MoveTicket) m).ticket)){
        				if(hasTickets(((scotlandyard.MoveTicket) m).ticket, player, 2))
        					moves.add(MoveDouble.instance(player, ((MoveTicket)m), ((MoveTicket)dm)));
        			}else if(hasTickets(((scotlandyard.MoveTicket) dm).ticket, player, 1)){
        				moves.add(MoveDouble.instance(player, (MoveTicket)m, (MoveTicket)dm));
        			}
        		}
        	}
        }
        return moves;
    }
    
    /**
     * Returns the list of moves around the players current location.
     * @param location
     * @param player
     * @return List of single moves
     */
    private List<Move> singleMoves(int location, Colour player) {
    	List<Move> moves = new ArrayList<Move>();
    	for(Edge<Integer, Route> e: graph.getEdges()){	       	
    		if(e.source()==location){   			
    			Move m = MoveTicket.instance(player, Ticket.fromRoute(e.data()), e.other(location));
        		if(!playerPresent(e.other(location), player) && hasTickets(((scotlandyard.MoveTicket) m).ticket, player, 1)){ 
        			moves.add(m);
        		}
        		//Add a secret ticket alternative for Mr. X.
        		if(hasTickets(Ticket.Secret, player, 1) && ((MoveTicket) m).ticket != Ticket.Secret && !playerPresent(e.other(location), player)){
        			Move secretm = MoveTicket.instance(player, Ticket.Secret, e.other(location));
        			moves.add(secretm);
        		}
        	}
        }
    	return moves;
    }
	
    /**
     * Checks whether a player has n tickets of the specified type.
     * @param Ticket
     * @param player
     * @param number of tickets
     * @return If the player has this number of these tickets 
     */
    private boolean hasTickets(Ticket t, Colour player, int n) {
    	return (view.getPlayerTickets(player, t) >= n);
    }
    
    /**
     * See's if a player is on a tile.
     * @param location
     * @param player
     * @return If there is another player at the node that is not player.
     */
    boolean playerPresent(int location, Colour player) {
    	for(Colour c: view.getPlayers()){
    		if((view.getPlayerLocation(c) == location) && (c != Colour.Black))
    			return true;
    	}
    	return false;
    }
    
    /**
     * Talks to the overlay.
     * @param Possible moves
     */
    private void consoleAssist(Set<Move> moves){
    	if(view.getPlayerLocation(Colour.Black) != 0){

	    	int suggestedMove = 1;
	    	int bestScore = Integer.MAX_VALUE;
	    	for(Move m: moves){
	    		if(m instanceof MoveTicket){
	    			int score = ScoreBoard.score(getLocations(m.colour, ((MoveTicket) m).target), graph.getNodes(), edges, 0);
		    		if(score < bestScore){
		    			bestScore = score;
		    			suggestedMove = ((MoveTicket) m).target;
		    		}
	    		}else if(m instanceof MoveDouble){
	    			int score = ScoreBoard.score(getLocations(m.colour, ((MoveDouble) m).move2.target), graph.getNodes(), edges, 0);
		    		if(score < bestScore){
		    			bestScore = score;
		        		suggestedMove = ((MoveDouble) m).move2.target;
		    		}
	    		}	    			
	    	}
	    	overlay.updatePositions(possibleLocations, suggestedMove);
    	}else{
	    	overlay.updatePositions(possibleLocations);

    	}
    }
	
    /**
     * Get Map of detective colours to locations from view updated from parameters
     * @param colour
     * @param target
     * @return
     */
    private EnumMap<Colour, Integer> getLocations(Colour colour, int target){
    	EnumMap<Colour, Integer> map = new EnumMap<Colour, Integer>(Colour.class);
    	for(Colour c: view.getPlayers()){
    		if(c.equals(colour)){
    			map.put(c, target);
    		}else{
        		map.put(c, view.getPlayerLocation(c));
    		}
    	}
    	return map;
    }
}

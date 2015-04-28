package newgui;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;

import player.ScotlandYardModelX;

import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.MoveDouble;
import scotlandyard.MovePass;
import scotlandyard.MoveTicket;
import scotlandyard.Route;
import scotlandyard.ScotlandYardGraphReader;
import scotlandyard.ScotlandYardView;
import scotlandyard.Ticket;
import gui.Gui;


@SuppressWarnings("serial")
public class GUI2 extends Gui{
	
	int mrX;
	List<Integer> possibleLocations;
	ScotlandYardView view;
	final private Graph<Integer, Route> graph;
	
	public GUI2(ScotlandYardView v, String imageFilename, String positionsFilename) throws IOException {
		super(v, imageFilename, positionsFilename);
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
		
		view = v;
		
		mrX = v.getPlayerLocation(Colour.Black);
		possibleLocations = new ArrayList<Integer>();
		possibleLocations.add(mrX);
		
		ScotlandYardGraphReader reader 	= new ScotlandYardGraphReader();
		graph = reader.readGraph("/resources/graph.txt/");
		
	}

	public void notify(Move move){
		updateValidLocations();
		super.notify(move);
	}

	private void updateValidLocations() {
		if(view.getPlayerLocation(Colour.Black) != mrX){
			mrX = view.getPlayerLocation(Colour.Black);
			possibleLocations = new ArrayList<Integer>();
			possibleLocations.add(mrX);
		}else if(mrX != 0){
			List<Integer> newPossibleLocations = new ArrayList<Integer>();
			for(Integer l: possibleLocations){
				//newPossibleLocations.addAll(validMoves(Colour.Black));
			}
		}
		
	}
	
    //Returns the possible moves a player can make.
    protected List<Move> validMoves(Colour player) {
    	//Adds all the moves around a players current location.
        List<Move> movesSingle = singleMoves(view.getPlayerLocation(player), player);
        List<Move> moves = new ArrayList<scotlandyard.Move>(movesSingle);
        //Adds double-moves to Mr.X's valid moves.
        if(hasTickets(Ticket.Double, player, 1)){
        	for(Move m: movesSingle){
        		List<scotlandyard.Move> doubleMoves = singleMoves(((MoveTicket)  m).target, player);
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
        //Adds a pass move if there is no possible moves.
        if(moves.isEmpty() && player != Colour.Black)
        	moves.add(MovePass.instance(player));
     
        return moves;
    }
    
    //Returns the list of moves around the players current location.
    private List<Move> singleMoves(int location, Colour player) {
    	List<Move> moves = new ArrayList<Move>();
    	for(Edge<Integer, Route> e: graph.getEdges()){	       	
    		if(e.source()==location||e.target()==location){   			
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
	
    //Checks whether a player has n tickets of the specified type.
    private boolean hasTickets(Ticket t, Colour player, int n) {
    	return (view.getPlayerTickets(player, t) >= n);
    }
    
    boolean playerPresent(int location, Colour player) {
    	for(Colour c: view.getPlayers()){
    		if((view.getPlayerLocation(c) == location) && (c != Colour.Black))
    			return true;
    	}
    	return false;
    }
	
}

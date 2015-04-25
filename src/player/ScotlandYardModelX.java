package player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.MoveDouble;
import scotlandyard.MovePass;
import scotlandyard.MoveTicket;
import scotlandyard.Node;
import scotlandyard.Player;
import scotlandyard.Route;
import scotlandyard.ScotlandYardGraphReader;
import scotlandyard.Spectator;
import scotlandyard.Ticket;



public class ScotlandYardModelX{
	
	//Game Constants:
	final private int numberOfDetectives;
	final private Graph<Integer, Route> graph;
	final private List<Boolean> rounds;
	
	//Participants:
	final private List<Spectator> spectators;
	
	//Game Variables:
	private int round;
	private List<Colour> players;
	private Colour currentPlayer;
	int MrXsLastKnownLocation;
	
	//For Game Helper
	private Set<Integer> mrXPossibleLocations;
	
	private HashMap<Colour, Map<Ticket, Integer>> Tickets;
	private HashMap<Colour, Integer> Locations;
	
	
    public ScotlandYardModelX(int numberOfDetectives, List<Boolean> rounds, String graphFileName) throws IOException {
    	
    	super();
		
		//Get the Graph from the Input File.
    	ScotlandYardGraphReader reader 	= new ScotlandYardGraphReader();
		graph = reader.readGraph(graphFileName);
		
		//Initialise Game Constants.
		this.rounds = rounds;
		this.numberOfDetectives = numberOfDetectives;
		
		//Initialise Observer Lists.		
		spectators = new ArrayList<Spectator>();

		//Initialise Game Variables.
		MrXsLastKnownLocation = 0;
		round = 0;
		currentPlayer = Colour.Black;
		
		mrXPossibleLocations = new HashSet<Integer>();
		players = new ArrayList<Colour>();
		Locations = new HashMap<Colour, Integer>();
		Tickets = new HashMap<Colour, Map<Ticket, Integer>>();
    }

    public void turn(Move move) {
        play(move);
        nextPlayer();
      }
    
    //Changes currentPlayer to the next player in the ArrayList.
    private void nextPlayer() {
    	
    	int i = players.indexOf(currentPlayer); 
    	if(i == players.size()-1){
    		currentPlayer = players.get(0);
    	}else{
    		currentPlayer = players.get(i+1);
    	}
    }
    
    private void play(Move move){
    	if (move instanceof MoveTicket) play((MoveTicket) move);
        if (move instanceof MoveDouble) play((MoveDouble) move);
        if (move instanceof MovePass) play((MovePass) move);
    }
    
    //Changes the game-state to after a move has been played and notifies the spectators. 
    private void play(MoveTicket move) {
    	makeMove(move);
    	if((move.colour != Colour.Black) || (getRounds().get(getRound()) == true)){
    		notifySpectators(move);
    	}else{
    		notifySpectators(getDummyTicket(move));
    	}
    }
        
    //Changes the game-state to after a double-move has been played and notifies the spectators.
    private void play(MoveDouble move) {
    	MoveTicket dummy1 = move.move1;
    	MoveTicket dummy2 = move.move2;
    	if(getRounds().get(getRound()+1) != true)
    		dummy1 = getDummyTicket(move.move1);
    	if(getRounds().get(getRound()+2) != true)
    		dummy2 = getDummyTicket(move.move2);
    	notifySpectators(MoveDouble.instance(move.colour, dummy1, dummy2));
    	
    	makeMove((MoveTicket)move.move1);
    	notifySpectators(dummy1);
    	makeMove((MoveTicket)move.move2);
    	notifySpectators(dummy2);
    	
    	Map<Ticket, Integer> tickets = Tickets.get(Colour.Black);
    	tickets.put(Ticket.Double, tickets.get(Ticket.Double)-1);
    }
    
    //Notifies the spectators that nothing has changed.
    private void play(MovePass move) {
    	notifySpectators(move);
    }
    
    //Changes the game-state to after a move has been played.
    private void makeMove(MoveTicket move) {
    	
    	Locations.put(currentPlayer, move.target);
    	Map<Ticket, Integer> tickets = Tickets.get(currentPlayer);
    	tickets.put(move.ticket, tickets.get(move.ticket)-1);
    	
    	if(!currentPlayer.equals(Colour.Black)){
    		tickets = Tickets.get(Colour.Black);
        	tickets.put(move.ticket, tickets.get(move.ticket)+1);
    	}else{
    		round = round + 1;
    		if(getRounds().get(getRound()) == true){
    			MrXsLastKnownLocation = Locations.get(Colour.Black);
    			/*
        		mrXPossibleLocations = new HashSet<Integer>();
        		mrXPossibleLocations.add(MrXsLastKnownLocation);
        		*/
    		}else{
    			//mrXPossibleLocations = computePossibleLocations(mrXPossibleLocations, move.ticket);
    		}
    		
    		
    		
    	}
    }
    

	//Creates a dummy ticket with Mr. X's last known location. 
    private MoveTicket getDummyTicket(MoveTicket move) {
    	return MoveTicket.instance(Colour.Black, move.ticket, MrXsLastKnownLocation);
    }

    //Returns the possible moves a player can make.
    protected Set<Move> validMoves(Colour player) {
    	//Adds all the moves around a players current location.
        Set<MoveTicket> movesSingle = singleMoves(Locations.get(player), player);
        Set<Move> allMoves = new HashSet<Move>(movesSingle);
        //Adds double-moves to Mr.X's valid moves.
        if(hasTickets(Ticket.Double, player, 1) && false){
        	for(MoveTicket firstMove: movesSingle){
        		Set<MoveTicket> secondMoves = singleMoves(((MoveTicket)  firstMove).target, player);
        		for(MoveTicket secondMove: secondMoves){
        			if(( secondMove.ticket == ((MoveTicket) firstMove).ticket)){
        				if(hasTickets(((MoveTicket) firstMove).ticket, player, 2))
        					allMoves.add(MoveDouble.instance(player, firstMove, secondMove));
        			}else if(hasTickets(((MoveTicket) secondMove).ticket, player, 1)){
        				allMoves.add(MoveDouble.instance(player, firstMove, secondMove));
        			}
        		}
        	}
        }
        //Adds a pass move if there is no possible moves.
        if(allMoves.isEmpty() && player != Colour.Black)
        	allMoves.add(MovePass.instance(player));
     
        return allMoves;
    }
    
    //Returns the list of moves around the players current location.
    private Set<MoveTicket> singleMoves(int location, Colour player) {
    	Set<MoveTicket> moves = new HashSet<MoveTicket>();
    	for(Edge<Integer, Route> e: graph.getEdges()){	       	
    		if(e.source()==location||e.target()==location){   			
    			MoveTicket m = MoveTicket.instance(player, Ticket.fromRoute(e.data()), e.other(location));
        		if(!playerPresent(e.other(location), player) && hasTickets(((MoveTicket) m).ticket, player, 1)){ 
        			moves.add(m);
        		}
        		//Add a secret ticket alternative for Mr. X.
        		if(hasTickets(Ticket.Secret, player, 1) && ((MoveTicket) m).ticket != Ticket.Secret && !playerPresent(e.other(location), player)){
        			MoveTicket secretm = MoveTicket.instance(player, Ticket.Secret, e.other(location));
        			moves.add(secretm);
        		}
        	}
        }
    	return moves;
    }

    //Checks whether a player is on the specified location.
    private boolean playerPresent(int location, Colour player) {
    	for(Colour c: players){
    		if( (Locations.get(c).equals(location)) && (!c.equals(player)) && (!c.equals(Colour.Black)))
    			return true;
    	}
    	return false;
    }
    
    //Checks whether a player has n tickets of the specified type.
    private boolean hasTickets(Ticket t, Colour player, int n) {
    	return (Tickets.get(player).get(t)) >= n;
    }
    
    //Notifies all spectators that a move has been made.
    private void notifySpectators(Move move) {
    	for(Spectator s: spectators){
    		s.notify(move);
    	}
    }

    public boolean join(Player player, Colour colour, int location, Map<Ticket, Integer> tickets) {
    	
    	players.add(colour);
		Locations.put(colour, location);
		Tickets.put(colour, tickets);
    	
    	if(playerExists(colour)){
    		return false;
    	}else{
    		if((colour == Colour.Black) && (getRounds().get(0) == true))
        		MrXsLastKnownLocation = location;
    		
    		return true;
    	}
    }
    
    //Gets a list of all the players in the game.
    public List<Colour> getPlayers() {
    	return players;
    }
    
    //Gets a set of the winning players.
    public Set<Colour> getWinningPlayers() {
    	Set<Colour> winners = new HashSet<Colour>();
    	if(isGameOver()){
        	if(allDetectivesAreStuck() || endOfFinalRound()){
        		winners.add(Colour.Black);
        	}else{
        		winners =  new HashSet<Colour>(getPlayers());
        		winners.remove(Colour.Black);
        	}
    	}
     	return winners;
    }

    //Gets a Detectives current location or MrX's last known location.
    public int getPlayerLocation(Colour colour) {
    	if(playerExists(colour)){
			return Locations.get(colour);    				
    	}else{
    		return 0;
    	}
    }

    //Returns the number of tickets a player has of a specified type.
    public int getPlayerTickets(Colour colour, Ticket ticket) {
    	if(playerExists(colour)){
    		return Tickets.get(colour).get(ticket);
    	}else{
    		return 0;
    	}
    }

    //Checks whether the conditions for the game's termination have been met.
    public boolean isGameOver() {
    	
    	if(!isReady()){
    		return false;
    	}
    	if(allDetectivesAreStuck()){
    		return true;
    	}
    	if(endOfFinalRound()){
    		return true;
    	}
    	if(isMrXCaught()){
    		return true;
    	}
    	if(MrXHasNowhereToGo()){
    		return true;
    	}
    	return false;
    }

    //Checks whether MrX has no valid moves.
    private boolean MrXHasNowhereToGo() {
    	return validMoves(Colour.Black).isEmpty();
	}
    
    //Checks whether a Detective has landed on Mr. X's location.
	private boolean isMrXCaught() {
		
		for(Colour col: players){
			//System.out.println(col);
			if(!col.equals(Colour.Black)){
				//System.out.println(col+" not equal: "+Locations.get(col)+" vs "+Locations.get(Colour.Black));
				if(Locations.get(col).equals(Locations.get(Colour.Black))){
					//System.out.println(col+" not equal, same location");
					return true;
				}
				
			}
		}
		return false;
	}
	
	//Checks whether all rounds are completed.
	private boolean endOfFinalRound() {
		return round>=(rounds.size()-1) && currentPlayer == Colour.Black;
 	}

	//Checks whether all Detectives can no longer move.
	private boolean allDetectivesAreStuck() {
		for(Colour c: players){
			if(!c.equals(Colour.Black)){
				if(!validMoves(c).contains(MovePass.instance(c))){
					return false;
				}	
			}
		}
		return true;
	}

	public boolean isReady() {
		return (players.size() == (numberOfDetectives));
    }

    //Returns the player who is currently playing.
    public Colour getCurrentPlayer() {
        return currentPlayer;
    }

    //Returns the round number.
    public int getRound() {
        return round;
    }

    //Returns a list of booleans relating to whether Mr. X reveals his location.
    public List<Boolean> getRounds() {
        return rounds;
    }
    
    //Checks where a player has joined the game.
    private boolean playerExists(Colour colour) {
    	return players.contains(colour);
    }
    
    //Get Mr.X's real location.
    public int getlastMrXLocation(){
    	return MrXsLastKnownLocation;
    }
    
    //Returns Mr.X's possible locations.
    public Set<Integer> getMrXPossibleLocations(){
    	return mrXPossibleLocations;
    }

	public void setData(HashMap<Colour, Map<Ticket, Integer>> tickets, HashMap<Colour, Integer> locations, Colour colour, int round){
		Tickets = new HashMap<Colour, Map<Ticket, Integer>>();
		for(Colour c: tickets.keySet()){
			Tickets.put(c, new HashMap<Ticket, Integer>(tickets.get(c)));
		}
		
		Locations = new HashMap<Colour, Integer>(locations);
		currentPlayer = colour;
		setRound(round);
	}
	
	public void setCurrentPlayer(Colour player){
		currentPlayer = player;
	}
	
	public void setRound(int r){
		round = r;
	}
	
	public HashMap<Colour, Map<Ticket, Integer>> getTickets(){
		return Tickets;
	}
	
	public HashMap<Colour, Integer> getLocations(){
		return Locations;
	}
	
	public Set<Node<Integer>> getNodes(){
		return graph.getNodes();
	}
	
	public Set<Edge<Integer, Route>> getEdges(){
		return graph.getEdges();
	}
	
	
    
}
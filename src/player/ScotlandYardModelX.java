package player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Move;
import scotlandyard.MoveDouble;
import scotlandyard.MovePass;
import scotlandyard.MoveTicket;
import scotlandyard.Player;
import scotlandyard.Route;
import scotlandyard.Ticket;


/**
 * A model that can be used for simulating different moves and situations.
 * @author Samuel
 */
public class ScotlandYardModelX{
	
	//Game Constants:
	final int numberOfDetectives;
	final private List<Boolean> rounds;
	List<Edge<Integer, Route>> edges;
		
	//Game Variables:
	private int round;
	private List<Colour> players;
	private Colour currentPlayer;
	int MrXsLastKnownLocation;
	

	
	private EnumMap<Colour, Map<Ticket, Integer>> Tickets;
	private EnumMap<Colour, Integer> Locations;
	
	/**
	 * Initialises variables
	 * @param NumberOfDetectives
	 * @param Rounds
	 * @param Edges in graph
	 * @throws IOException
	 */
    public ScotlandYardModelX(int numberOfDetectives, List<Boolean> rounds, List<Edge<Integer, Route>> edges2) throws IOException {
    		
		this.edges = edges2;
		
		//Initialise Game Constants.
		this.rounds = rounds;
		this.numberOfDetectives = numberOfDetectives;
		
		//Initialise Game Variables.
		MrXsLastKnownLocation = 0;
		round = 0;
		currentPlayer = Colour.Black;
		
		players = new ArrayList<Colour>();
		Locations = new EnumMap<Colour, Integer>(Colour.class);
		Tickets = new EnumMap<Colour, Map<Ticket, Integer>>(Colour.class);
    }

    /**
     * Makes move in model and incrementing the turn
     * @param move
     */
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
    }
        
    //Changes the game-state to after a double-move has been played and notifies the spectators.
    private void play(MoveDouble move) {
    	makeMove((MoveTicket)move.move1);
    	makeMove((MoveTicket)move.move2);
    	
    	Map<Ticket, Integer> tickets = Tickets.get(Colour.Black);
    	tickets.put(Ticket.Double, tickets.get(Ticket.Double)-1);
    }
    
    //Notifies the spectators that nothing has changed.
    private void play(MovePass move) {

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
    

	//Returns the possible moves a player can make.
    protected List<Move> validMoves(Colour player, boolean b) {
    	//Adds all the moves around a players current location.
        List<MoveTicket> movesSingle = singleMoves(Locations.get(player), player);
        List<Move> allMoves = new ArrayList<Move>(movesSingle);
        //Adds double-moves to Mr.X's valid moves.
        if(hasTickets(Ticket.Double, player, 1) && b){
        	for(MoveTicket firstMove: movesSingle){
        		List<MoveTicket> secondMoves = singleMoves(((MoveTicket)  firstMove).target, player);
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
    private List<MoveTicket> singleMoves(int location, Colour player) {
    	List<MoveTicket> moves = new ArrayList<MoveTicket>();
    	for(Edge<Integer, Route> e: edges){	       	
    		if(e.source()==location/*||e.target()==location*/){ 		
    			MoveTicket m = MoveTicket.instance(player, Ticket.fromRoute(e.data()), e.other(location));
        		if(!playerPresent(e.other(location), player) && hasTickets(((MoveTicket) m).ticket, player, 1)){ 
        			moves.add(m);
        		}
        		//Add a secret ticket alternative for Mr. X.
        		/*
        		if(hasTickets(Ticket.Secret, player, 1) && ((MoveTicket) m).ticket != Ticket.Secret && !playerPresent(e.other(location), player)){
        			MoveTicket secretm = MoveTicket.instance(player, Ticket.Secret, e.other(location));
        			moves.add(secretm);
        		}
        		*/
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
    	return !hasGotValidMoves(Colour.Black);
	}
    
    private boolean hasGotValidMoves(Colour c) {
    	Integer location = Locations.get(c);
		for(Edge<Integer, Route> e: edges){
			if(e.source().equals(location) || e.target().equals(location)){
				if(!playerPresent(e.other(location), c) && hasTickets(Ticket.fromRoute( e.data()), c, 1)){
					return true;
				}
			}
		}
		return false;
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
				if(hasGotValidMoves(c)){
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
    

	public void setData(EnumMap<Colour, Map<Ticket, Integer>> tickets2, EnumMap<Colour, Integer> locations2, Colour colour, int round){
		Tickets = new EnumMap<Colour, Map<Ticket, Integer>>(Colour.class);
		for(Colour c: tickets2.keySet()){
			Tickets.put(c, new HashMap<Ticket, Integer>(tickets2.get(c)));
		}
		
		Locations = new EnumMap<Colour, Integer>(locations2);
		currentPlayer = colour;
		setRound(round);
	}
	
	public void setCurrentPlayer(Colour player){
		currentPlayer = player;
	}
	
	public void setRound(int r){
		round = r;
	}
	
	public EnumMap<Colour, Map<Ticket, Integer>> getTickets(){
		return Tickets;
	}
	
	public EnumMap<Colour, Integer> getLocations(){
		return Locations;
	}
	
	
    
}
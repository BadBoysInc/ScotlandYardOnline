package player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.TimeLimitExceededException;

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
import scotlandyard.ScotlandYardView;
import scotlandyard.Ticket;



public class MyAIPlayer implements Player{

	ScotlandYardView view;
	String graphFilename;
	Set<Integer> detectives;
	Set<Colour> players;
	EnumMap<Colour, Map<Ticket, Integer>> Tickets;
	EnumMap<Colour, Integer> Locations;
	GraphDisplay graphDisplay;
	ScotlandYardModelX model;
	boolean loaded = false;
	boolean singleMovesAllFail;
	
	ScoreBoard score;
	
	List<Edge<Integer, Route>> edges;
	Set<Node<Integer>> nodes;
	
	long init;
	int winningBonus = 1000;

	/**
	 * @param Shared View
	 * @param graphFilename
	 */
	public MyAIPlayer(ScotlandYardView view, String graphFilename) {
		this.view = view;
		this.graphFilename = graphFilename;
		
		ScotlandYardGraphReader r = new ScotlandYardGraphReader();
		try {
			Graph<Integer, Route> g = r.readGraph(graphFilename);
			edges = new ArrayList<Edge<Integer, Route>>(g.getEdges());
			nodes = g.getNodes();
			g = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Sets up the model
	 * @param Mr X position
	 * @throws IOException
	 */
	void setup(int mrX) throws IOException{
		
		model = new ScotlandYardModelX(view.getPlayers().size(), view.getRounds(), edges);
		
		//getting detective locations
		detectives = new HashSet<Integer>();
		Locations = new EnumMap<Colour, Integer>(Colour.class);
		players = new HashSet<Colour>();
		Tickets = new EnumMap<Colour, Map<Ticket, Integer>>(Colour.class);
		
		for(Colour c: view.getPlayers()){
			EnumMap<Ticket, Integer> playerTickets = new EnumMap<Ticket, Integer>(Ticket.class);
			for(Ticket t: Ticket.values()){
				playerTickets.put(t, view.getPlayerTickets(c, t));
			}
			Tickets.put(c, playerTickets);
			int location;
			if(c.equals(Colour.Black)){
				location = mrX;
			}else{
				location = this.view.getPlayerLocation(c);
			}
			
			if(c != Colour.Black){
				detectives.add(location);
			}
			
			Locations.put(c, location);
			
			
			players.add(c);
			model.join(null, c, location, Tickets.get(c));
		}
		
		model.setCurrentPlayer(Colour.Black);
		model.setRound(view.getRound());
		
		graphDisplay = new GraphDisplay();
		
		singleMovesAllFail = false;
				
	}
	
	/**
	 * Wrapper of min-max that matches the score to the moves and chooses the best.
	 * @param Depth of search
	 * @param If it has tried with single moves and all end up losing the game.
	 * @return The best move to take
	 * @throws TimeLimitExceededException if 14 seconds has elapsed
	 */
	Move minMaxHelper(int x) throws TimeLimitExceededException{
		
		HashMap<Move, Integer> MrXMoves = new HashMap<Move, Integer>();
		
		int savedRound = view.getRound();
		
		if(x>4)
			model.setData(Tickets, Locations, Colour.Black, savedRound);
		
		Map<Integer, Integer> dists = ScoreBoard.breathfirstNodeSearch(Locations.get(Colour.Black), ScoreBoard.getDetectivePositions(Locations), nodes, edges);
		
		int closeDetectives=0;
		for(Integer detectivesPos: dists.keySet()){
			if(dists.get(detectivesPos)<360)
				closeDetectives++;
		}
		
		List<Move> singlemoves;
		if(closeDetectives>2 || (closeDetectives>1 && view.getRounds().get(view.getRound()+2).equals(false)) || singleMovesAllFail){
			singlemoves = model.validMoves(Colour.Black, true);
		}else{
			singlemoves = model.validMoves(Colour.Black, false);
		}

		Integer bestChildScore = Integer.MIN_VALUE;

		for(Move MrXMove: singlemoves){
			if(Debug.printOutEndGame)System.out.println("Analysing "+MrXMove);
			
			model.setData(Tickets, Locations, Colour.Black, savedRound);
			
			model.turn(MrXMove);
			
			int score = min(x, bestChildScore, true);
			
			MrXMoves.put(MrXMove, score);
			bestChildScore = Math.max(bestChildScore, score);
		}	
		
		
		int bestScore = Integer.MIN_VALUE;
		Move bestMove = null;
		
		for(Move m: MrXMoves.keySet()){
			int score = MrXMoves.get(m);
			if(score>=bestScore){
				bestScore = score;
				bestMove = m;
			}
		}
		
		if(bestScore == Integer.MIN_VALUE && singleMovesAllFail == false){
			singleMovesAllFail = true;
			return minMaxHelper(x);
		}
		
		return bestMove;
	}
	
	/**
	 * Min section of the min-max that picks the worst score for Mr X
	 * @param current level
	 * @param bestPreComputedSibling for Alpha Beta pruning
	 * @param is the current detective after Mr X
	 * @return worse score for Mr X
	 * @throws TimeLimitExceededException if 14 seconds has elapsed
	 */
	int min(int level, Integer bestPreComputedSibling, boolean afterMrX) throws TimeLimitExceededException{
		
		//interrupts if the time limit has been reached.
		checkTime();
		
		if(model.isGameOver()){
			if(model.getWinningPlayers().contains(Colour.Black)){
				if(Debug.printOutEndGame)System.out.println("Winning model");
				return ScoreBoard.score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black,false).size()) + winningBonus;
			}
			
			if(Debug.printOutEndGame)System.out.println("Losing model.");
			return Integer.MIN_VALUE;
		}
			
		if(level == 0)
			return ScoreBoard.score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black, false).size());
		
		
		
		EnumMap<Colour, Integer> saveLocations = new EnumMap<Colour, Integer>(model.getLocations());
		
		EnumMap<Colour, Map<Ticket, Integer>> tmp = model.getTickets();
		EnumMap<Colour, Map<Ticket, Integer>> saveTickets = new EnumMap<Colour, Map<Ticket, Integer>>(Colour.class);
		for(Colour c: tmp.keySet())
			saveTickets.put(c, new EnumMap<Ticket, Integer>(tmp.get(c)));
		
		Colour savedColour = model.getCurrentPlayer();
		int savedRound = model.getRound();
		
		Integer bestChildScore = 0;
		int currentPlayer = model.getPlayers().indexOf(model.getCurrentPlayer());
		List<Move> set = oneLookAhead(oneLookAhead(model.validMoves(model.getCurrentPlayer(), false)));

		if(afterMrX){
			
			bestChildScore = Integer.MAX_VALUE;
			
			for(Move currentMove: set){
				model.setData(saveTickets, saveLocations, savedColour, savedRound);
				
				model.turn(currentMove);
				
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				int score;
				if(currentPlayer == model.numberOfDetectives-1){
					 score = max(level-1, bestChildScore);
				}else{
					 score = min(level-1, bestChildScore, false);
				}
				
				
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				
				bestChildScore = Math.min(bestChildScore, score);
				
				if(score<bestPreComputedSibling || score == Integer.MIN_VALUE){
					if(Debug.printOutOptimise)System.out.println("Pruned");
					break;
				}
			}
			
		}else{
							
			bestChildScore = Integer.MAX_VALUE;
			
			for(Move currentMove: set){
				
				model.setData(saveTickets, saveLocations, savedColour, savedRound);
				model.turn(currentMove);
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				
				int score;
				if(currentPlayer == model.numberOfDetectives-1){
					 score = max(level-1, bestChildScore);
				}else{
					 score = min(level-1, bestChildScore, false);
				}
				
				bestChildScore = Math.min(score, bestChildScore);
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				
				if(score == Integer.MIN_VALUE){
					if(Debug.printOutOptimise)System.out.println("Can't win");
					break;
				}
			}
		}
		
		return bestChildScore;
	}
	
	/**
	 * Max section of the min-max, that picks the best score for Mr X
	 * @param current level
	 * @param bestPreComputedSibling for Alpha Beta pruning
	 * @param is the current detective after Mr X
	 * @return Best score for Mr X
	 * @throws TimeLimitExceededException if 14 seconds has elapsed
	 */
	int max(int level, Integer bestPreComputedSibling) throws TimeLimitExceededException{
		
		//interrupts if the time limit has been reached.
				checkTime();
		
		if(model.isGameOver()){
			if(model.getWinningPlayers().contains(Colour.Black)){
				if(Debug.printOutEndGame)System.out.println("Winning model");
				return ScoreBoard.score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black,false).size()) + winningBonus;
			}
			if(Debug.printOutEndGame)System.out.println("Losing model.");
			return Integer.MIN_VALUE;
		}
			
		if(level == 0)
			return ScoreBoard.score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black, false).size());
		
		
		
		EnumMap<Colour, Integer> saveLocations = new EnumMap<Colour, Integer>(model.getLocations());
		
		EnumMap<Colour, Map<Ticket, Integer>> tmp = model.getTickets();
		EnumMap<Colour, Map<Ticket, Integer>> saveTickets = new EnumMap<Colour, Map<Ticket, Integer>>(Colour.class);
		for(Colour c: tmp.keySet())
			saveTickets.put(c, new EnumMap<Ticket, Integer>(tmp.get(c)));
		
		Colour savedColour = model.getCurrentPlayer();
		int savedRound = model.getRound();
		
		Integer bestChildScore = 0;
		List<Move> set = model.validMoves(model.getCurrentPlayer(), false);
		
		if(model.getCurrentPlayer().equals(Colour.Black)){
			
			bestChildScore = Integer.MIN_VALUE;
			
			for(Move currentMove: set){
				model.setData(saveTickets, saveLocations, savedColour, savedRound);
				model.turn(currentMove);
				
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				
				int score = min(level-1, bestChildScore, true);
				
				if(Debug.printOutGeneral)System.out.println(printLevel(level));

				bestChildScore = Math.max(bestChildScore, score);
				
				if(score>bestPreComputedSibling){
					if(Debug.printOutOptimise)System.out.println("Pruned");
					break;
				}
			}
		}
		return bestChildScore;
	}

	/**
	 * Throws exception if time since global variable init has reached 14 seconds.
	 * @throws TimeLimitExceededException
	 */
	private void checkTime() throws TimeLimitExceededException {
		if(new Date().getTime()-init >12000)
			throw new TimeLimitExceededException("Time Limit Reached");
	}

	/**
	 * Prunes out the bad moves the detectives wouldn't make, so we can go deeper in the game tree.
	 * @param validMoves
	 * @return a pruned version of valid moves.
	 */
	private List<Move> oneLookAhead(List<Move> validMoves) {
		
		Collections.sort(validMoves, new Comparator<Move>(){

			@Override
			public int compare(Move m1, Move m2) {
				int targetm1 = 0;
				if(m1 instanceof MoveTicket){
					targetm1 = ((MoveTicket)m1).target;
				}else if(m1 instanceof MoveDouble){
					targetm1 = ((MoveDouble)m1).move2.target;
				}else{
					targetm1 = model.getPlayerLocation(m1.colour);
				}
				
				int targetm2 = 0;
				if(m2 instanceof MoveTicket){
					targetm2 = ((MoveTicket)m2).target;
				}else if(m2 instanceof MoveDouble){
					targetm2 = ((MoveDouble)m2).move2.target;
				}else{
					targetm2 = model.getPlayerLocation(m2.colour);
				}
					
				return(simpleDetectiveScore(targetm1)-simpleDetectiveScore(targetm2));
			}			
		});
		
		int l = validMoves.size();
		while(l>2){
			validMoves.remove(l-1);
			l--;
		}
		return validMoves;	
	}
	
	/**
	 * Uses a simple Heuristic to score a detective's move.
	 * @param location
	 * @return score of 'location'
	 */
	private int simpleDetectiveScore(int location){
		//getting location
		Integer mrX = Locations.get(Colour.Black);
		if(mrX.equals(0)){
			//System.out.println("mrX=0");
			return 0;
		}
		//getting distance to detectives
		int totalDistanceToDetectives  = ScoreBoard.pairBreathfirstNodeSearch(mrX, location, nodes, edges);
				
		int positionOnBoard = Math.abs(graphDisplay.getX(location) - 509) + Math.abs(graphDisplay.getY(location) - 404);

		//Scaling factors
		int distanceFromDetectivesScale = 100;
		double positionScale = 0.2;
				
		return (distanceFromDetectivesScale*totalDistanceToDetectives + ((int)positionScale*positionOnBoard));
	}

	/**
	 * For Debugging purposes, prints out the current level with tabbing to indicate level graphically
	 * @param level
	 * @return
	 */
	private String printLevel(int l){
		String s = "";
		for(int x = 0; x<l;x++){
			s = s + "\t";
		}
		s = s + "Level "+Integer.toString(l);
		return s;
	}

	/**
	 * Called to ask, out of moves, which to make.
	 * @param location
	 * @param Set of possible moves to choose from.
	 * @return Choosen move
	 */
	@Override
    public Move notify(int location, Set<Move> moves) {
		init = new Date().getTime();
		System.out.println("Current MrX Location: "+location);
		
		try {
			setup(location);
			Locations.put(Colour.Black, location);
			System.out.println("Trying simple one move ahead");
			Move bestMove = oneMoveLookAhead(location, moves);
			
			
			int x = 4;
			while(new Date().getTime()-init <12000){
				System.out.println("Trying using "+x+" depth");
				try {
					bestMove = minMaxHelper(x);
				} catch (TimeLimitExceededException e) {
					System.out.println(x + " failed, fall back to "+ (x-1));
				}
				x++;
			}
			
			System.out.println("Move Choosen: "+bestMove);
			
			if(view.getPlayerLocation(Colour.Black) != 0){
				
				boolean taxi = false;
				boolean bus = false;
				boolean underground = false;
				for(Move move: moves){
					if(move instanceof MoveTicket){
						if(((MoveTicket) move).ticket.equals(Ticket.Taxi)){
							taxi = true;
						}
						if(((MoveTicket) move).ticket.equals(Ticket.Bus)){
							bus = true;
						}
						if(((MoveTicket) move).ticket.equals(Ticket.Underground)){
							underground = true;
						}
					}
				}
				
				if(taxi && bus && underground && Tickets.get(Colour.Black).get(Ticket.Secret)>0){
					if(bestMove instanceof MoveTicket){
						System.out.println("Move Secrefied");
						return MoveTicket.instance(Colour.Black, Ticket.Secret, ((MoveTicket) bestMove).target);
					}else if(bestMove instanceof MoveDouble){
						System.out.println("Move Secrefied");
						return MoveDouble.instance(Colour.Black, MoveTicket.instance(Colour.Black, Ticket.Secret, ((MoveDouble) bestMove).move1.target), ((MoveDouble) bestMove).move2);
					}
				}
			}
			
			return bestMove;
			
		} catch (IOException e) {e.printStackTrace();}
		System.err.println("Someting has gone wrong");
		return moves.iterator().next();
    }

	/**
	 * Really simple choice to fall back on if all else fails
	 * @param Current location
	 * @param Valid moves
	 * @return best Move according to one look ahead.
	 */
	private Move oneMoveLookAhead(int location, Set<Move> moves) {
		int bestScore = Integer.MAX_VALUE;
		Move bestMove = null;
		int score = Integer.MAX_VALUE;
		
		for(Move move: moves){
			
			int newLocation;
			int ticketScale = 0;
			
			Ticket t = Ticket.Taxi; 
			
			if(move instanceof MoveTicket){
				newLocation = ((MoveTicket) move).target;
				t = ((MoveTicket) move).ticket;
				if(t.equals(Ticket.Underground)){
					ticketScale = 300;
				}else if(t.equals(Ticket.Bus)){
					ticketScale = 150;
				}else if(t.equals(Ticket.Taxi)){
					ticketScale = 100;
				}
			}else if(move instanceof MoveDouble){
				newLocation = ((MoveDouble) move).move2.target;
			}else if(move instanceof MovePass){
				return move;
			}else{
				throw new Error("Move isn't real");
			}
			
			EnumMap<Colour, Integer> tmp = model.getLocations();
			tmp.put(move.colour, newLocation);
			score = ScoreBoard.score(tmp,nodes, edges, 0);
			
			if(move instanceof MoveTicket)
				score = score - (Tickets.get(Colour.Black).get(t)*ticketScale);
			
			if(score<=bestScore){
				bestScore = score;
				bestMove = move;
			}
		}
		return bestMove;
	}

}

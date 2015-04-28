package player;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.MoveDouble;
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
	
	Set<Edge<Integer, Route>> edges;
	Set<Node<Integer>> nodes;
	
	//Varaiables.
	int winningBonus = 1000;
	int distanceFromDetectivesScale = 75;
	int currentOptionsScale = 2;
	int minDistanceScale = 500;
	int positionScale = 1;
	
	public MyAIPlayer(ScotlandYardView view, String graphFilename) {
		this.view = view;
		this.graphFilename = graphFilename;
		
		ScotlandYardGraphReader r = new ScotlandYardGraphReader();
		try {
			Graph<Integer, Route> g = r.readGraph(graphFilename);
			edges = g.getEdges();
			nodes = g.getNodes();
			g = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void scoreInit(int mrX) throws IOException{
			
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
				
	}
	
	/**
	 * @param Mr X's location 
	 * @param His valid moves 
	 * @return Integer score of board, from distance to detectives and number of possible moves.
	 */
	private int score(EnumMap<Colour, Integer> locations, Set<Node<Integer>> nodes, Set<Edge<Integer, Route>> edges, List<Move> validMoves){
		
		//getting location
		Integer mrX = locations.get(Colour.Black);
		
		Set<Integer> detectivesPos = new HashSet<Integer>();
		for(Colour c: locations.keySet()){
			if(!c.equals(Colour.Black)){
				detectivesPos.add(locations.get(c));
			}
		}
		
		
		//getting distance to detectives
		int totalDistanceToDetectives = 0;
		Hashtable<Integer, Integer> detectiveDistances = breathfirstNodeSearch(mrX, detectivesPos, nodes, edges);
		if(detectiveDistances != null){
			for(Integer i: detectiveDistances.keySet()){
				totalDistanceToDetectives += detectiveDistances.get(i);
			}
		}
		
		int minDistanceToDetectives = Integer.MAX_VALUE;
		for(Integer i: detectiveDistances.keySet()){
			minDistanceToDetectives = Math.min(detectiveDistances.get(i), minDistanceToDetectives);
		}
		
		
		int positionOnBoard = 509 - Math.abs(graphDisplay.getX(mrX) - 509) + 404 - Math.abs(graphDisplay.getY(mrX) - 404);
		
		
		//getting number of valid moves
		int MrXcurrentOptions = validMoves.size();
		
		return (distanceFromDetectivesScale*totalDistanceToDetectives + 
				currentOptionsScale*MrXcurrentOptions + 
				minDistanceScale*minDistanceToDetectives + 
				positionScale*positionOnBoard);
		
	}

	/**
	 * @param mrX location
	 * @param detectives locations
	 * @param graph 
	 * @return total distance from Mr X to detectives.
	 */
	private Hashtable<Integer, Integer>  breathfirstNodeSearch(Integer mrX, Set<Integer> d, Set<Node<Integer>> nodes2, Set<Edge<Integer, Route>> edges2) {
			
			Set<Edge<Integer, Route>> edges = new HashSet<Edge<Integer, Route>>(edges2);
			Set<Node<Integer>> nodes = new HashSet<Node<Integer>>(nodes2);
			Set<Integer> detectives = new HashSet<Integer>(d);
			
			
			int currentDistance = 0;
			
			//hash table of detective location against distance.
			Hashtable<Integer, Integer> detectiveDistances = new Hashtable<Integer, Integer>();
			
			//Initialise distance to maximum.
			for(Integer i: detectives){
				detectiveDistances.put(i, Integer.MAX_VALUE);
			}
			
			//Start at Mr X location.
			Set<Node<Integer>> currentNodes =  new HashSet<Node<Integer>>();
			Node<Integer> mrXNode = findNode(mrX, nodes);
			if(mrXNode == null){
				System.err.println("Mr X not on valid location");
			}
			currentNodes.add(mrXNode);
			//Remove visited Nodes.
			nodes.remove(mrXNode);
			//while there are detective still to reach.
			while(!detectives.isEmpty()){
				//Get nodes one step away.
				Set<Node<Integer>> neighbours = getNeighbours(currentNodes, nodes, edges);
				currentDistance++;
				//Remove seen nodes.
				nodes.remove(neighbours);
				
				//If they are detective locations update the shortest distance.
				for(Node<Integer> n: neighbours){
					if(detectives.contains(n.data())){
						if(currentDistance < detectiveDistances.get(n.data())){
							detectiveDistances.put((Integer) n.data(), currentDistance);
							//Remove from detectives still to get.
							detectives.remove(n.data());
						}
					}				
				}
				
				currentNodes = neighbours;
			}
			
			//Add the distances to give a score
			
			return detectiveDistances;
	}

	/**
	 * @param Set of currentNodes
	 * @param Set of all not-reached nodes
	 * @param Set of all edges
	 * @return Set of neighbouring nodes to currentNodes
	 */
	private Set<Node<Integer>> getNeighbours(Set<Node<Integer>> currentNodes, Set<Node<Integer>> nodes, Set<Edge<Integer, Route>> edges) {
		Set<Node<Integer>> neighbours = new HashSet<Node<Integer>>();
		for(Edge<Integer, Route> e: edges){
			for(Node<Integer> currentNode: currentNodes){
				//check if current edge is connected to current node.
				if(e.source().equals(currentNode.data()) || e.target().equals(currentNode.data()) ){
					//If node is still to be reached (Ie. still in "nodes") add to neighbour set.
					Node<Integer> n = findNode((Integer) e.other((Integer) currentNode.data()), nodes);
					if(n != null){
						neighbours.add(n);
					}
				}
			}
		}
		return neighbours;
	}
	
	/**
	 * @param Int location
	 * @param Set of nodes
	 * @return Node from set with matching data, null if none match.
	 */
	private Node<Integer> findNode(Integer i, Set<Node<Integer>> nodes) {
		for(Node<Integer> node: nodes){
			if(node.data().equals(i)){
				return node;
			}
		}
		return null;
	}

	private Move MinMaxTree(){
		System.out.println("Old");
		HashMap<Move, Integer> MrXMoves = new HashMap<Move, Integer>();
		
		int savedRound = model.getRound();
		
		List<Move> singlemoves = model.validMoves(Colour.Black);

		if(model.isReady() && !model.isGameOver()){
			
			Integer bestChildScore = Integer.MIN_VALUE;
			
			
			
			for(Move MrXMove: singlemoves){
				
				if(Debug.printOutEndGame)System.out.println("Analysing "+MrXMove);
				
				model.setData(Tickets, Locations, Colour.Black, savedRound);
				
				model.turn(MrXMove);
				
				//get detective moves?;
				
				int score = minMaxCalc(5, bestChildScore, true);
				
				MrXMoves.put(MrXMove, score);
				bestChildScore = Math.max(bestChildScore, score);
			}	
		}else{
			System.out.println("ERROR: Game over");
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
		
		return bestMove;
		
	}
	
	private int minMaxCalc(int level, Integer bestPreComputedSibling, boolean afterMrX){
		
		if(model.isGameOver()){
			if(model.getWinningPlayers().contains(Colour.Black)){
				if(Debug.printOutEndGame)System.out.println("Winning model");
				return score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black)) + winningBonus;
			}
			
			if(Debug.printOutEndGame)System.out.println("Losing model.");
			return Integer.MIN_VALUE; //score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black)) - winningBonus;
		}
			
		if(level == 0){
			return score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black));
		}
		
		
		
		EnumMap<Colour, Integer> saveLocations = new EnumMap<Colour, Integer>(model.getLocations());
		
		EnumMap<Colour, Map<Ticket, Integer>> tmp = model.getTickets();
		EnumMap<Colour, Map<Ticket, Integer>> saveTickets = new EnumMap<Colour, Map<Ticket, Integer>>(Colour.class);
		for(Colour c: tmp.keySet()){
			saveTickets.put(c, new EnumMap<Ticket, Integer>(tmp.get(c)));
		}
		
		Colour savedColour = model.getCurrentPlayer();
		int savedRound = model.getRound();
		
		
		Integer bestChildScore = 0;
		List<Move> set = model.validMoves(model.getCurrentPlayer());
		
		
		if(model.getCurrentPlayer().equals(Colour.Black)){
			
			bestChildScore = Integer.MIN_VALUE;
			
			for(Move currentMove: set){
				
				model.setData(saveTickets, saveLocations, savedColour, savedRound);
				
				model.turn(currentMove);
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				int score = minMaxCalc(level-1, bestChildScore, true);
				if(Debug.printOutGeneral)System.out.println(printLevel(level));

				bestChildScore = Math.max(bestChildScore, score);
				
				if(score>bestPreComputedSibling){
					if(Debug.printOutOptimise)System.out.println("Pruned");
					break;
				}
			}
		}else if(afterMrX){
			
			bestChildScore = Integer.MAX_VALUE;
			
			for(Move currentMove: set){
				
				model.setData(saveTickets, saveLocations, savedColour, savedRound);
				
				model.turn(currentMove);
				
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				int score = minMaxCalc(level-1, bestChildScore, false);
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
				bestChildScore = Math.min(minMaxCalc(level-1, bestChildScore, false), bestChildScore);
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				
			}
		}
		
		return bestChildScore;
	}
	
	
	
	Move iterativeMinMaxHelper(){
			
		System.out.println("New");
		
		HashMap<Move, Integer> MrXMoves = new HashMap<Move, Integer>();
		
		int savedRound = model.getRound();
		
		List<Move> singlemoves = model.validMoves(Colour.Black);

		if(model.isReady() && !model.isGameOver()){
			
			Integer bestChildScore = Integer.MIN_VALUE;
			
			List<List<Move>> detectiveMoves = Arrays.asList(
					model.validMoves(Colour.Blue),
					model.validMoves(Colour.Green),
					model.validMoves(Colour.Red),
					model.validMoves(Colour.White),
					model.validMoves(Colour.Yellow)
					);
			
			
			for(Move MrXMove: singlemoves){
				
				if(Debug.printOutEndGame)System.out.println("Analysing "+MrXMove);
				
				model.setData(Tickets, Locations, Colour.Black, savedRound);
				
				model.turn(MrXMove);
				
				//get detective moves?;
				
				int score = min(5, bestChildScore, true, detectiveMoves);
				
				MrXMoves.put(MrXMove, score);
				bestChildScore = Math.max(bestChildScore, score);
			}	
		}else{
			System.out.println("ERROR: Game over");
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
		
		return bestMove;
		
		
		
		
	}
	
	int min(int level, Integer bestPreComputedSibling, boolean afterMrX, List<List<Move>> detectiveMoves){
		
		if(model.isGameOver()){
			if(model.getWinningPlayers().contains(Colour.Black)){
				if(Debug.printOutEndGame)System.out.println("Winning model");
				return score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black)) + winningBonus;
			}
			
			if(Debug.printOutEndGame)System.out.println("Losing model.");
			return Integer.MIN_VALUE;
					//score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black)) - winningBonus;
		}
			
		if(level == 0){
			return score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black));
		}
		
		
		
		EnumMap<Colour, Integer> saveLocations = new EnumMap<Colour, Integer>(model.getLocations());
		
		EnumMap<Colour, Map<Ticket, Integer>> tmp = model.getTickets();
		EnumMap<Colour, Map<Ticket, Integer>> saveTickets = new EnumMap<Colour, Map<Ticket, Integer>>(Colour.class);
		for(Colour c: tmp.keySet()){
			saveTickets.put(c, new EnumMap<Ticket, Integer>(tmp.get(c)));
		}
		
		Colour savedColour = model.getCurrentPlayer();
		int savedRound = model.getRound();
		
		
		Integer bestChildScore = 0;
		int currentPlayer = model.getPlayers().indexOf(model.getCurrentPlayer());
		List<Move> set = detectiveMoves.get(currentPlayer-1);
		
		
		
		if(afterMrX){
			
			bestChildScore = Integer.MAX_VALUE;
			
			for(Move currentMove: set){
				System.out.println(currentMove);
				model.setData(saveTickets, saveLocations, savedColour, savedRound);
				
				model.turn(currentMove);
				
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				int score;
				if(currentPlayer == model.numberOfDetectives-1){
					 score = max(level-1, bestChildScore, false, detectiveMoves);
				}else{
					 score = min(level-1, bestChildScore, false, detectiveMoves);
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
					 score = max(level-1, bestChildScore, false, detectiveMoves);
				}else{
					 score = min(level-1, bestChildScore, false, detectiveMoves);
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
	
	int max(int level, Integer bestPreComputedSibling, boolean b, List<List<Move>> detectiveMoves){

		
		
		if(model.isGameOver()){
			if(model.getWinningPlayers().contains(Colour.Black)){
				if(Debug.printOutEndGame)System.out.println("Winning model");
				return score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black)) + winningBonus;
			}
			
			if(Debug.printOutEndGame)System.out.println("Losing model.");
			return Integer.MIN_VALUE;
					//score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black)) - winningBonus;
		}
			
		if(level == 0){
			return score(model.getLocations(), nodes, edges, model.validMoves(Colour.Black));
		}
		
		detectiveMoves = Arrays.asList(
				model.validMoves(Colour.Blue),
				model.validMoves(Colour.Green),
				model.validMoves(Colour.Red),
				model.validMoves(Colour.White),
				model.validMoves(Colour.Yellow)
				);
		
		EnumMap<Colour, Integer> saveLocations = new EnumMap<Colour, Integer>(model.getLocations());
		
		EnumMap<Colour, Map<Ticket, Integer>> tmp = model.getTickets();
		EnumMap<Colour, Map<Ticket, Integer>> saveTickets = new EnumMap<Colour, Map<Ticket, Integer>>(Colour.class);
		for(Colour c: tmp.keySet()){
			saveTickets.put(c, new EnumMap<Ticket, Integer>(tmp.get(c)));
		}
		
		Colour savedColour = model.getCurrentPlayer();
		int savedRound = model.getRound();
		
		
		Integer bestChildScore = 0;
		List<Move> set = model.validMoves(model.getCurrentPlayer());
		
		
		if(model.getCurrentPlayer().equals(Colour.Black)){
			
			bestChildScore = Integer.MIN_VALUE;
			
			for(Move currentMove: set){
				
				model.setData(saveTickets, saveLocations, savedColour, savedRound);
				
				model.turn(currentMove);
				if(Debug.printOutGeneral)System.out.println(printLevel(level));
				int score = min(level-1, bestChildScore, true, detectiveMoves);
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
	

	
	
	private String printLevel(int l){
		String s = "";
		for(int x = 0; x<l;x++){
			s = s + "\t";
		}
		s = s + "Level "+Integer.toString(l);
		return s;
	}

	@Override
    public Move notify(int location, Set<Move> moves) {

		try {
			scoreInit(location);
			Locations.put(Colour.Black, location);
		
			Move bestMove = iterativeMinMaxHelper();
			//Move bestMove = MinMaxTree();

			
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("Someting has gone wrong");
		return moves.iterator().next();
		
    }
	

}

package player;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
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
	HashMap<Colour, Map<Ticket, Integer>> Tickets;
	HashMap<Colour, Integer> Locations;
	GraphDisplay graphDisplay;
	ScotlandYardModelX model;
	boolean loaded = false;
	
	public MyAIPlayer(ScotlandYardView view, String graphFilename) {
		this.view = view;
		this.graphFilename = graphFilename;
	}
	
	void scoreInit(int mrX) throws IOException{
			
		model = new ScotlandYardModelX(view.getPlayers().size(), view.getRounds(), graphFilename);
		
		//getting detective locations
		detectives = new HashSet<Integer>();
		Locations = new HashMap<Colour, Integer>();
		players = new HashSet<Colour>();
		Tickets = new HashMap<Colour, Map<Ticket, Integer>>();
		
		for(Colour c: view.getPlayers()){
			HashMap<Ticket, Integer> playerTickets = new HashMap<Ticket, Integer>();
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
	private int score(HashMap<Colour, Integer> locations, Set<Node<Integer>> nodes, Set<Edge<Integer, Route>> edges, Set<Move> validMoves){
		
		//getting location
		Integer mrX = locations.get(Colour.Black);
		
		
		//getting distance to detectives
		int totalDistanceToDetectives = 0;
		Hashtable<Integer, Integer> detectiveDistances = breathfirstNodeSearch(mrX, detectives, nodes, edges);
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
		
		
		//Scaling factors
		int distanceFromDetectivesScale = 75;
		int currentOptionsScale = 2;
		int minDistanceScale = 500;
		int positionScale = 1;
		
		/*System.out.println(String.format("MOVE(%d) totdist: %d, mindist: %d, numMoves: %d, pos: %d",
				mrX,
				distanceFromDetectivesScale*totalDistanceToDetectives, 
				minDistanceScale*minDistanceToDetectives, 
				currentOptionsScale*MrXcurrentOptions, 
				positionScale*positionOnBoard));
		*/
		
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

	private Move MinMaxTree(int location, Set<Move> moves){
		
		HashMap<Move, Integer> MrXList = new HashMap<Move, Integer>();
		
		int savedRound = model.getRound();
		
		Set<Move> singlemoves = model.validMoves(Colour.Black);

		if(model.isReady() && !model.isGameOver()){
			for(Move MrXMove: singlemoves){
				
				model.setData(Tickets, Locations, Colour.Black, savedRound);
				
				model.turn(MrXMove);
				
				MrXList.put(MrXMove, minMaxCalc(2));
				
			}	
		}else{
			System.out.println("ERROR: Game over");
		}
		
		int bestScore = 0;
		Move bestMove = null;
		
		for(Move m: MrXList.keySet()){
			int score = MrXList.get(m);
			if(score>bestScore){
				bestScore = score;
				bestMove = m;
			}
		}
		
		return bestMove;
		
	}
	
	private int minMaxCalc(int level){
		
		if(model.isGameOver()){
			return 0;
		}else{
			
			if(level == 0){
				return score(model.getLocations(), model.getNodes(), model.getEdges(), model.validMoves(Colour.Black));
			}
			
			
			
			HashMap<Colour, Integer> saveLocations = new HashMap<Colour, Integer>(model.getLocations());
			
			HashMap<Colour, Map<Ticket, Integer>> tmp = model.getTickets();
			HashMap<Colour, Map<Ticket, Integer>> saveTickets = new HashMap<Colour, Map<Ticket, Integer>>();
			for(Colour c: tmp.keySet()){
				saveTickets.put(c, new HashMap<Ticket, Integer>(tmp.get(c)));
			}
			
			Colour savedColour = model.getCurrentPlayer();
			int savedRound = model.getRound();
			
			Set<Integer> childScores = new HashSet<Integer>();
			Set<Move> set = model.validMoves(model.getCurrentPlayer());
			//System.out.println(set.size());
			for(Move currentMove: set){
				
				model.setData(saveTickets, saveLocations, savedColour, savedRound);
				
				model.turn(currentMove);
				
				//String s = printLevel(level);
				//System.out.println(s);
				
				childScores.add(minMaxCalc(level-1));
				
				//System.out.println(s);
			}
			
			if(model.getCurrentPlayer().equals(Colour.Black))
				return max(childScores);
			return min(childScores);
			
		}
		
	}
	
	
	private String printLevel(int l){
		String s = "";
		for(int x = 0; x<l;x++){
			s = s + "\t";
		}
		s = s + "Level"+Integer.toString(l);
		return s;
	}
	
	private int max(Set<Integer> set) {
		
		int max = 0;
		
		for(Integer i: set){
			if(i > max){
				max = i;
			}
		}
		
		return max;
	}

	private int min(Set<Integer> set) {
		
		int min = Integer.MAX_VALUE;
		
		for(Integer i: set){
			if(i < min){
				min = i;
			}
		}
		
		return min;
	}

	@Override
    public Move notify(int location, Set<Move> moves) {

		try {
			scoreInit(location);
			Locations.put(Colour.Black, location);
			/* **This is a working wersion of 1 move look a head.
			 * int bestScore = 0;
			Move bestMove = null;
					
			for(Move move: moves){
				int newLocation;
				if(move instanceof MoveTicket){
					newLocation = ((MoveTicket) move).target;
				}else if(move instanceof MoveDouble){
					newLocation = ((MoveDouble) move).move2.target;
				}else if(move instanceof MovePass){
					newLocation = location;
				}else{
					throw new Error("Move isn't real");
				}
				
				int score = score(newLocation, detectives, graph.getNodes(), graph.getEdges(), Tickets);
				if(score>bestScore){
					bestScore = score;
					bestMove = move;
				}
			
			}
			*/
			
			Move bestMove = MinMaxTree(location, moves);
			
			
			
			System.out.println(bestMove);
			System.out.println("");
			
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
					return MoveTicket.instance(Colour.Black, Ticket.Secret, ((MoveTicket) bestMove).target);
				}else if(bestMove instanceof MoveDouble){
					return MoveDouble.instance(Colour.Black, MoveTicket.instance(Colour.Black, Ticket.Secret, ((MoveDouble) bestMove).move1.target), ((MoveDouble) bestMove).move2);
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

package player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import scotlandyard.ScotlandYard;
import scotlandyard.ScotlandYardGraphReader;
import scotlandyard.ScotlandYardView;
import scotlandyard.Ticket;


public class MyAIPlayer implements Player{

	ScotlandYardView view;
	String graphFilename;
	Graph graph;
	Set<Integer> detectives;
	HashMap<Colour, HashMap<Ticket, Integer>> Tickets;
	HashMap<Colour, Integer> Locations;
	boolean DEBUG = false;
	GraphDisplay graphDisplay;
	
	
	public MyAIPlayer(ScotlandYardView view, String graphFilename) {
		this.view = view;
		this.graphFilename = graphFilename;
	}
	
	void scoreInit() throws IOException{
		
		//getting detective locations
		detectives = new HashSet<Integer>();
		
		for(Colour player: this.view.getPlayers()){
			if(player != Colour.Black){
				detectives.add(this.view.getPlayerLocation(player));
				Locations.put(player, view.getPlayerLocation(player));
			}
		}
		
		//getting all tickets
		for(Colour c: view.getPlayers()){
			HashMap<Ticket, Integer> playerTickets = new HashMap<Ticket, Integer>();
			for(Ticket t: Ticket.values()){
				playerTickets.put(t, view.getPlayerTickets(c, t));
			}
			Tickets.put(c, playerTickets);
		}
		
		//reading in graph
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();		
		graph = reader.readGraph(graphFilename);
		
		graphDisplay = new GraphDisplay();
				
	}
	
	/**
	 * @param Mr X's location 
	 * @param His valid moves 
	 * @return Integer score of board, from distance to detectives and number of possible moves.
	 */
	private int score(int location, Set<Integer> detectives, Set<Node> nodes, Set<Edge> edges, HashMap<Colour, HashMap<Ticket, Integer>> tickets){
		
		//getting location
		Integer mrX = location;
		
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
		
		
		int positionOnBoard = 509 - Math.abs(graphDisplay.getX(location) - 509) + 404 - Math.abs(graphDisplay.getY(location) - 404);
		
		
		
		
		//getting number of valid moves
		int MrXcurrentOptions = validMoves(Locations, Colour.Black, nodes, edges, tickets).size();
		
		
		
		
		//Scaling factors
		int distanceFromDetectivesScale = 75;
		int currentOptionsScale = 2;
		int minDistanceScale = 300;
		int positionScale = 1;
		
		System.out.println(String.format("MOVE(%d) totdist: %d, mindist: %d, numMoves: %d, pos: %d",
				location,
				distanceFromDetectivesScale*totalDistanceToDetectives, 
				minDistanceScale*minDistanceToDetectives, 
				currentOptionsScale*MrXcurrentOptions, 
				positionScale*positionOnBoard));
		
		
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
	private Hashtable<Integer, Integer>  breathfirstNodeSearch(Integer mrX, Set<Integer> d, Set<Node> nodesO, Set<Edge> edgesO) {
			
			Set<Edge> edges = new HashSet<Edge>(edgesO);
			Set<Node> nodes = new HashSet<Node>(nodesO);
			Set<Integer> detectives = new HashSet<Integer>(d);
			
			
			int currentDistance = 0;
			
			//hash table of detective location against distance.
			Hashtable<Integer, Integer> detectiveDistances = new Hashtable<Integer, Integer>();
			
			//Initialise distance to maximum.
			for(Integer i: detectives){
				detectiveDistances.put(i, Integer.MAX_VALUE);
			}
			
			//Start at Mr X location.
			Set<Node> currentNodes =  new HashSet<Node>();
			Node mrXNode = findNode(mrX, nodes);
			if(mrXNode == null){
				System.err.println("Mr X not on valid location");
			}
			currentNodes.add(mrXNode);
			//Remove visited Nodes.
			nodes.remove(mrXNode);
			//while there are detective still to reach.
			while(!detectives.isEmpty()){
				
				//Get nodes one step away.
				Set<Node> neighbours = getNeighbours(currentNodes, nodes, edges);
				currentDistance++;
				//Remove seen nodes.
				nodes.remove(neighbours);
				
				//If they are detective locations update the shortest distance.
				for(Node n: neighbours){
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
	private Set<Node> getNeighbours(Set<Node> currentNodes, Set<Node> nodes, Set<Edge> edges) {
		Set<Node> neighbours = new HashSet<Node>();
		for(Edge e: edges){
			for(Node currentNode: currentNodes){
				//check if current edge is connected to current node.
				if(e.source().equals(currentNode.data()) || e.target().equals(currentNode.data()) ){
					//If node is still to be reached (Ie. still in "nodes") add to neighbour set.
					Node n = findNode((Integer) e.other(currentNode.data()), nodes);
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
	private Node findNode(Integer i, Set<Node> nodes) {
		for(Node node: nodes){
			if(node.data().equals(i)){
				return node;
			}
		}
		return null;
	}

	private Move MinMaxTree(int location, Set<Move> moves){
		
		HashMap<Colour, Integer> mrxlocations = new HashMap<Colour, Integer>(Locations);
		HashMap<Colour, HashMap<Ticket, Integer>> mrxtickets = new HashMap<Colour, HashMap<Ticket, Integer>>(Tickets);
		HashMap<Move, Integer> MrXList = new HashMap<Move, Integer>();
		
		Set<Node> nodes = graph.getNodes();
		Set<Edge> edges = graph.getEdges();
		
		for(Move MrXMove: validMoves(mrxlocations, Colour.Black, nodes, edges, mrxtickets)){
			int target = 0;
			if(MrXMove instanceof MoveDouble){
				target = ((MoveDouble) MrXMove).move2.target;
				HashMap<Ticket, Integer> tmptickets = mrxtickets.get(Colour.Black);
				tmptickets.put(((MoveDouble) MrXMove).move2.ticket, tmptickets.get(((MoveDouble) MrXMove).move2.target)-1);
				tmptickets.put(((MoveDouble) MrXMove).move1.ticket, tmptickets.get(((MoveDouble) MrXMove).move1.target)-1);
			}else{
				target = ((MoveTicket) MrXMove).target;
				HashMap<Ticket, Integer> tmptickets = mrxtickets.get(Colour.Black);
				tmptickets.put(((MoveTicket) MrXMove).ticket, tmptickets.get(((MoveTicket) MrXMove).target)-1);
			}
			mrxlocations.put(Colour.Black, target);
			
			MrXList.put(MrXMove, minMaxCalc(1, mrxlocations, mrxtickets, nodes, edges));
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
	
	private int minMaxCalc(int level, HashMap<Colour, Integer> locations, HashMap<Colour, HashMap<Ticket, Integer>> tickets, Set<Node> nodes, Set<Edge> edges){
		if(level == 0){
			int mrX = 0;
			Set<Integer> d = new HashSet<Integer>();
			for(Colour c: locations.keySet()){
				if(c.equals(Colour.Black)){
					mrX = locations.get(c);
				}else{
					d.add(locations.get(c));
				}	
			}
			return score(mrX, d, nodes, edges, tickets);
		}
		
			
			Set d1 = new HashSet<Integer>();
			for(Move d1Move: validMoves(locations, Colour.Blue, nodes, edges, tickets)){
				HashMap<Ticket, Integer> tmptickets = tickets.get(Colour.Blue);
				tmptickets.put(((MoveTicket) d1Move).ticket, tmptickets.get(((MoveTicket) d1Move).target)-1);
				locations.put(Colour.Blue, ((MoveTicket) d1Move).target);
				Set d2 = new HashSet<Integer>();
				for(Move d2Move: validMoves(locations, Colour.Green, nodes, edges, tickets)){
					tmptickets = tickets.get(Colour.Green);
					tmptickets.put(((MoveTicket) d2Move).ticket, tmptickets.get(((MoveTicket) d2Move).target)-1);
					locations.put(Colour.Green, ((MoveTicket) d2Move).target);
					Set d3 = new HashSet<Integer>();
					for(Move d3Move: validMoves(locations, Colour.Red, nodes, edges, tickets)){
						tmptickets = tickets.get(Colour.Red);
						tmptickets.put(((MoveTicket) d3Move).ticket, tmptickets.get(((MoveTicket) d3Move).target)-1);
						locations.put(Colour.Red, ((MoveTicket) d3Move).target);
						Set d4 = new HashSet<Integer>();
						for(Move d4Move: validMoves(locations, Colour.White, nodes, edges, tickets)){
							tmptickets = tickets.get(Colour.White);
							tmptickets.put(((MoveTicket) d4Move).ticket, tmptickets.get(((MoveTicket) d4Move).target)-1);
							locations.put(Colour.White, ((MoveTicket) d4Move).target);
							Set d5 = new HashSet<Integer>();
							for(Move d5Move: validMoves(locations, Colour.Yellow, nodes, edges, tickets)){
								tmptickets = tickets.get(Colour.Yellow);
								tmptickets.put(((MoveTicket) d5Move).ticket, tmptickets.get(((MoveTicket) d5Move).target)-1);
								locations.put(Colour.Yellow, ((MoveTicket) d5Move).target);
								
								Set Mrx = new HashSet<Integer>();
								for(Move MrXMove: validMoves(locations, Colour.Black, nodes, edges, tickets)){
									
									int target = 0;
									if(MrXMove instanceof MoveDouble){
										target = ((MoveDouble) MrXMove).move2.target;
										tmptickets = tickets.get(Colour.Black);
										tmptickets.put(((MoveDouble) MrXMove).move2.ticket, tmptickets.get(((MoveDouble) MrXMove).move2.target)-1);
										tmptickets.put(((MoveDouble) MrXMove).move1.ticket, tmptickets.get(((MoveDouble) MrXMove).move1.target)-1);
									}else{
										target = ((MoveTicket) MrXMove).target;
										tmptickets = tickets.get(Colour.Black);
										tmptickets.put(((MoveTicket) MrXMove).ticket, tmptickets.get(((MoveTicket) MrXMove).target)-1);
									}
									locations.put(Colour.Black, target);
									
									Mrx.add(minMaxCalc(level-1,locations,tickets,nodes, edges));
								}
								d5.add(max(Mrx));
							}
							d4.add(min(d5));
						}
						d3.add(min(d4));
					}
					d2.add(min(d3));
				}
				d1.add(min(d2));
			}
			
		return min(d1);
		
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
        //TODO: Some clever AI here ...

		try {
			scoreInit();
			
			int bestScore = 0;
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
		String s = null;
		s.length();
		System.err.println("Someting has gone wrong");
		return moves.iterator().next();
		
    }
	
    protected List<Move> validMoves(HashMap<Colour, Integer> locations, Colour currentColour, Set<Node> nodes, Set<Edge> edges, HashMap<Colour, HashMap<Ticket, Integer>> Tickets) {
    	HashMap<Ticket, Integer> currentTickets = Tickets.get(currentColour);
    	//Adds all the moves around a players current location.
        List<MoveTicket> firstMoves = singleMoves(locations, currentColour, nodes, edges, Tickets);
        List<Move> allMoves = new ArrayList<Move>(firstMoves);
        //Adds double-moves to Mr.X's valid moves.
        for(MoveTicket firstMove: firstMoves){
        		List<MoveTicket> secondMoves = singleMoves(locations, currentColour, nodes, edges, Tickets);
        		for(MoveTicket secondMove: secondMoves){
        			if(secondMove.ticket == firstMove.ticket){
        				if(currentTickets.get(firstMove.ticket)>1){
        					allMoves.add(MoveDouble.instance(currentColour, firstMove, secondMove));
        				}
        			}else if(currentTickets.get(secondMove.ticket)>0){
        				allMoves.add(MoveDouble.instance(currentColour, firstMove, secondMove));
        			}
        		}
        	}
        
        return allMoves;
    }
	
    private List<MoveTicket> singleMoves(HashMap<Colour, Integer> locations, Colour currentColour, Set<Node> nodes, Set<Edge> edges, HashMap<Colour, HashMap<Ticket, Integer>> Tickets) {
    	
    	List<MoveTicket> moves = new ArrayList<MoveTicket>();
    	int location = locations.get(currentColour);
    	for(Edge<Integer, Route> e: edges){
    		if(e.source()==location||e.target()==location){
    			MoveTicket m = MoveTicket.instance(currentColour, Ticket.fromRoute(e.data()), e.other(location));
    			if(!detectives.contains(e.other(location)) && Tickets.get(currentColour).get(m.ticket)>0){ 
        			moves.add(m);
        		}
    		}
    	}	
    	
    	return moves;
    }

}

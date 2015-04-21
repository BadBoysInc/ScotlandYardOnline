package player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
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
	HashMap<Ticket, Integer> MrXTickets;
	boolean DEBUG = false;
	
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
			}
		}
		
		//getting MrX's tickets
		MrXTickets = new HashMap<Ticket, Integer>();
		
		for(Ticket t: Ticket.values()){
			MrXTickets.put(t, view.getPlayerTickets(Colour.Black, t));
		}
		
		//reading in graph
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();		
		graph = reader.readGraph(graphFilename);
				
	}
	
	
	/**
	 * @param Mr X's location 
	 * @param His valid moves 
	 * @return Integer score of board, from distance to detectives and number of possible moves.
	 */
	private int score(int location){
		
		//getting location
		Integer mrX = location;
		
		//getting distance to detectives
		int totalDistanceToDetectives;
		if(mrX != 0){
			totalDistanceToDetectives = breathfirstNodeSearch(mrX, detectives, graph);
		}else{
			totalDistanceToDetectives = 0;
		}
		
		//getting number of valid moves
		int MrXcurrentOptions = validMoves(mrX, detectives, graph.getNodes(), graph.getEdges(), MrXTickets).size();
		
		//Scaling factors
		int a = 1;
		int b = 1;
		
		return (a*totalDistanceToDetectives + b*MrXcurrentOptions);
		
	}

	/**
	 * @param mrX location
	 * @param detectives locations
	 * @param graph 
	 * @return total distance from Mr X to detectives.
	 */
	private int breathfirstNodeSearch(Integer mrX, Set<Integer> detectives,	Graph graph) {
			
			Set<Edge> edges = new HashSet<Edge>(graph.getEdges());
			Set<Node> nodes = new HashSet<Node>(graph.getNodes());
			
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
			int sum = 0;
			for(Integer i: detectiveDistances.keySet()){
				sum += detectiveDistances.get(i);
			}
			return sum;
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

	@Override
    public Move notify(int location, Set<Move> moves) {
        //TODO: Some clever AI here ...

		try {
			scoreInit();
			
			int bestScore = Integer.MAX_VALUE;
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
				
				int score = score(newLocation);
				if(score<bestScore){
					bestScore = score;
					bestMove = move;
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
	
    protected List<Move> validMoves(int location, Set<Integer> detectives, Set<Node> nodes, Set<Edge> edges, HashMap<Ticket, Integer> MrXTickets) {
    	//Adds all the moves around a players current location.
        List<MoveTicket> firstMoves = singleMoves(location, detectives, nodes, edges, MrXTickets);
        List<Move> allMoves = new ArrayList<Move>(firstMoves);
        //Adds double-moves to Mr.X's valid moves.
        for(MoveTicket firstMove: firstMoves){
        		List<MoveTicket> secondMoves = singleMoves(firstMove.target, detectives, nodes, edges, MrXTickets);
        		for(MoveTicket secondMove: secondMoves){
        			if(secondMove.ticket == firstMove.ticket){
        				if(MrXTickets.get(firstMove.ticket)>1){
        					allMoves.add(MoveDouble.instance(Colour.Black, firstMove, secondMove));
        				}
        			}else if(MrXTickets.get(secondMove.ticket)>0){
        				allMoves.add(MoveDouble.instance(Colour.Black, firstMove, secondMove));
        			}
        		}
        	}
        
        return allMoves;
    }
	
    private List<MoveTicket> singleMoves(int location, Set<Integer> detectives, Set<Node> nodes, Set<Edge> edges, HashMap<Ticket, Integer> MrXTickets) {
    	
    	List<MoveTicket> moves = new ArrayList<MoveTicket>();
    	
    	for(Edge<Integer, Route> e: edges){
    		if(e.source()==location||e.target()==location){
    			MoveTicket m = MoveTicket.instance(Colour.Black, Ticket.fromRoute(e.data()), e.other(location));
    			if(!detectives.contains(e.other(location)) && MrXTickets.get(m.ticket)>0){ 
        			moves.add(m);
        		}
    		}
    	}	
    	
    	return moves;
    }

	
	

}

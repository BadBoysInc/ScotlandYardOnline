package player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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


public class MySimpleAIPlayer implements Player{

	ScotlandYardView view;
	String graphFilename;
	Graph<Integer, Route> graph;
	boolean DEBUG = false;
	GraphDisplay graphDisplay;
	HashMap<Ticket, Integer> tickets;
	
	
	public MySimpleAIPlayer(ScotlandYardView view, String graphFilename) {
		this.view = view;
		this.graphFilename = graphFilename;
	}
	
	void scoreInit() throws IOException{
		
		tickets = new HashMap<Ticket, Integer>();
		
		for(Ticket t: Ticket.values()){
			tickets.put(t, view.getPlayerTickets(view.getCurrentPlayer(), t));
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
	private int score(int location){
		
		//getting location
		Integer mrX = view.getPlayerLocation(Colour.Black);
		
		if(mrX.equals(0)){
			return 0;
		}
		
		//getting distance to detectives
		int totalDistanceToDetectives  = breathfirstNodeSearch(mrX, location, graph);
				
		int positionOnBoard = Math.abs(graphDisplay.getX(location) - 509) + Math.abs(graphDisplay.getY(location) - 404);
		
		
		
		//Scaling factors
		int distanceFromDetectivesScale = 100;
		double positionScale = 0.2;
				
		
		return (distanceFromDetectivesScale*totalDistanceToDetectives + 
				((int)positionScale*positionOnBoard));
		
	}

	/**
	 * @param mrX location
	 * @param detectives locations
	 * @param graph 
	 * @return total distance from Mr X to detectives.
	 */
	private int  breathfirstNodeSearch(Integer mrX, Integer detect,	Graph<Integer, Route> graph) {
			
			if(mrX.equals(detect)){
				return 0;
			}
		
			Set<Edge> edges = new HashSet<Edge>(graph.getEdges());
			Set<Node> nodes = new HashSet<Node>(graph.getNodes());
			
			for(Edge e: graph.getEdges()){
				if(e.data().equals(Route.Boat)){
					edges.remove(e);
				}
			}
			
			int currentDistance = 0;
			
			//Initialise distance to maximum.
			int distance = Integer.MAX_VALUE;
			
			//Start at Mr X location.
			Set<Node> currentNodes =  new HashSet<Node>();
			Node detectNode = findNode(detect, nodes);
			if(detectNode == null){
				System.err.println("Mr X not on valid location");
			}
			currentNodes.add(detectNode);
			//Remove visited Nodes.
			nodes.remove(detectNode);
			//while there are detective still to reach.
			while( distance == Integer.MAX_VALUE){
				
				//Get nodes one step away.
				Set<Node> neighbours = getNeighbours(currentNodes, nodes, edges);
				currentDistance++;
				//Remove seen nodes.
				nodes.remove(neighbours);
				
				//If they are detective locations update the shortest distance.
				for(Node n: neighbours){
					if(mrX.equals(n.data())){
						return currentDistance;
					}				
				}
				
				currentNodes = neighbours;
			}
			
			//Add the distances to give a score
			
			return 0;
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
				
				Ticket t = ((MoveTicket) move).ticket;
				int score = score(newLocation);
				
				int scale = 0;
				if(t.equals(Ticket.Underground)){
					scale = 300;
				}else if(t.equals(Ticket.Bus)){
					scale = 150;
				}else if(t.equals(Ticket.Taxi)){
					scale = 100;
				}
					
				
				
				score = score - (tickets.get(t)*scale);
				
				
				if(score<bestScore){
					bestScore = score;
					bestMove = move;
				}
			}
			
			System.out.println(bestMove);
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

}

package player;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
	/**
	 * Initialises tickets Map from view, and graph and GraphDisplay from file.
	 * @throws IOException
	 */
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
	 * @return Integer score of board, from distance to detectives.
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
			
			if(mrX.equals(detect))
				return 0;
		
			Set<Edge<Integer, Route>> edges = new HashSet<Edge<Integer, Route>>(graph.getEdges());
			Set<Node<Integer>> nodes = new HashSet<Node<Integer>>(graph.getNodes());
			
			for(Edge<Integer, Route> e: graph.getEdges()){
				if(e.data().equals(Route.Boat))
					edges.remove(e);
			}
			
			int currentDistance = 0;
			
			//Initialise distance to maximum.
			int distance = Integer.MAX_VALUE;
			
			//Start at Mr X location.
			Set<Node<Integer>> currentNodes =  new HashSet<Node<Integer>>();
			Node<Integer> detectNode = findNode(detect, nodes);
			if(detectNode == null)
				System.err.println("Mr X not on valid location");
			
			currentNodes.add(detectNode);
			//Remove visited Nodes.
			nodes.remove(detectNode);
			//while there are detective still to reach.
			while( distance == Integer.MAX_VALUE){
				
				//Get nodes one step away.
				Set<Node<Integer>> neighbours = getNeighbours(currentNodes, nodes, edges);
				currentDistance++;
				//Remove seen nodes.
				nodes.remove(neighbours);
				
				//If they are detective locations update the shortest distance.
				for(Node<Integer> n: neighbours){
					if(mrX.equals(n.data()))
						return currentDistance;		
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
	private Set<Node<Integer>> getNeighbours(Set<Node<Integer>> currentNodes, Set<Node<Integer>> nodes, Set<Edge<Integer, Route>> edges) {
		Set<Node<Integer>> neighbours = new HashSet<Node<Integer>>();
		for(Edge<Integer, Route> e: edges){
			for(Node<Integer> currentNode: currentNodes){
				//check if current edge is connected to current node.
				if(e.source().equals(currentNode.data()) || e.target().equals(currentNode.data()) ){
					//If node is still to be reached (Ie. still in "nodes") add to neighbour set.
					Node<Integer> n = findNode((Integer) e.other(currentNode.data()), nodes);
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

	
	/**
	 * Called to ask out of the given moves, which to take.
	 * @param int location
	 * @param Set of valid moves
	 */
    public Move notify(int location, Set<Move> moves) {
        //TODO: Some clever AI here ...

		try {
			scoreInit();
			
			Set<Move> tmp = new HashSet<Move>(moves);
			if(tickets.get(Ticket.Taxi)<2){
				if(tickets.get(Ticket.Bus)<2){
					for(Move m: tmp){
						if(m instanceof MoveTicket){
							if(moveExists(((MoveTicket)m).target, Route.Underground)){
								moves.remove(m);
							}
						}
					}
				}else{
					for(Move m: tmp){
						if(m instanceof MoveTicket){
							if(moveExists(((MoveTicket)m).target, Route.Bus)){
								moves.remove(m);
							}
						}
					}
				}
			}
			
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
					System.out.println("Move Pass!!!");
					return move;
				}else{
					throw new Error("Move isn't real");
				}
				
				score = score(newLocation);

				if(move instanceof MoveTicket)
					score = score - (tickets.get(t)*ticketScale);

				if(score<=bestScore){
					bestScore = score;
					bestMove = move;
				}
				
			}
			
			if(bestMove== null){
				if(moves.isEmpty()){
					System.out.println(tmp);
				}else{
					bestMove = tmp.iterator().next();
				}
			}
			
			if(Debug.printOutDetect)System.out.println(bestMove);
			
			return bestMove;
			
		} catch (IOException e) {e.printStackTrace();}
		System.err.println("Someting has gone wrong");
		return moves.iterator().next();
		
    }

    /**
     * Checks if there is a edge connected to 'node' with data 'route'
     * @param node
     * @param underground
     * @return True if edge found, False if not.
     */
	private boolean moveExists(int node, Route route) {
		for(Edge<Integer, Route> e: graph.getEdges()){
			if((e.target().equals(node) || e.source().equals(node)) && e.data().equals(route)){
				return true;
			}
		}
		return false;
	}

}

package player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.Node;
import scotlandyard.Route;
import scotlandyard.ScotlandYardGraphReader;
import scotlandyard.ScotlandYardView;

public class ScoringRandomPlayer extends RandomPlayer {

	ScotlandYardView view;
	String graphFilename;
	boolean DEBUG = false;
	
	public ScoringRandomPlayer(ScotlandYardView view, String graphFilename) {
		super(view, graphFilename);
		this.view = view;
		this.graphFilename = graphFilename;
	}
	
	/**
	 * @param Mr X's location 
	 * @param His valid moves 
	 * @return Integer score of board, from distance to detectives and number of possible moves.
	 */
	private int score(int location, Set<Move> moves){
		
		//getting locations of players
		ArrayList<Integer> detectives = new ArrayList<Integer>();
		Integer mrX = -1;
		
		for(Colour player: this.view.getPlayers()){
			if(player != Colour.Black){
				detectives.add(this.view.getPlayerLocation(player));
			}else{
				mrX = this.view.getPlayerLocation(player);
			}
		}
		
		if(view.getCurrentPlayer() == Colour.Black){
			mrX = location;
		}
		
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();

		try {
			Graph<Integer, Route> graph = reader.readGraph(graphFilename);
			
			int totalDistanceToDetectives;
			if(mrX != 0){
				totalDistanceToDetectives = doSelectiveNOTDijkstra(mrX, detectives, graph);
			}else{
				totalDistanceToDetectives = 0;
			}
			
			int MrXcurrentOptions = moves.size();
			
			//Scaling factors
			int a = 1;
			int b = 1;
			
			return (a*totalDistanceToDetectives + b*MrXcurrentOptions);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
		
	}
	
	

	/**
	 * @param mrX location
	 * @param detectives locations
	 * @param graph 
	 * @return total distance from Mr X to detectives.
	 */
	private int doSelectiveNOTDijkstra(Integer mrX, ArrayList<Integer> detectives,	Graph<Integer, Route> graph) {
				
			Set<Edge<Integer, Route>> edges = graph.getEdges();
			Set<Node<Integer>> nodes = graph.getNodes();
			
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

	@Override
    public Move notify(int location, Set<Move> moves) {
        //TODO: Some clever AI here ...
		System.out.println("--------------Score: " + score(location, moves));
		return super.notify(location, moves);
    }
	
	

}

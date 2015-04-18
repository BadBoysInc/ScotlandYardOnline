package player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Graph;
import scotlandyard.GraphReader;
import scotlandyard.Move;
import scotlandyard.Node;
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
	 * @return int of total distance from Mr X to detectives.
	 */
	private int score(){
		
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
		
		//if don't know where Mr X is distance is 0
		int totalDistanceToDetectives;
		if(mrX != 0){
			totalDistanceToDetectives = doSelectiveDijkstra(mrX, detectives, this.graphFilename);
		}else{
			totalDistanceToDetectives = 0;
		}
		return totalDistanceToDetectives;
		
		
	}
	
	/**
	 * @param mrX location
	 * @param detectives locations
	 * @param graphFilename 
	 * @return total distance from Mr X to detectives.
	 */
	private int doSelectiveDijkstra(Integer mrX, ArrayList<Integer> detectives,	String graphFilename) {
		
		//read in graph from file
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
		try {
			Graph graph = reader.readGraph(graphFilename);
			Set<Edge> edges = graph.getEdges();
			Set<Node> nodes = graph.getNodes();
			
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

	/**
	 * 
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
	 * 
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
		System.out.println("Total Distance of Detectives: " + score());
		return super.notify(location, moves);
    }
	
	

}

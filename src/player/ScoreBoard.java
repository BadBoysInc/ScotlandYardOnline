package player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Node;
import scotlandyard.Route;

public class ScoreBoard {

	static int distanceFromDetectivesScale = 2;
	static int currentOptionsScale = 30;
	static int minDistanceScale = 6;
	static int positionScale = 3;
	
	static GraphDisplay graphDisplay = new GraphDisplay();

	/**
	 * @param Map of all colour players to their locations,
	 * @param A Set holding the nodes of the graph,
	 * @param A list holding the edges of the graph,
	 * @param The number of valid moves at this location
	 * @return Integer score of board, using distance to detectives, position on board and number of possible moves.
	 */

	public static int score(EnumMap<Colour, Integer> locations, Set<Node<Integer>> nodes, List<Edge<Integer, Route>> edges, int validMoves){
		
		//getting location
		Integer mrX = locations.get(Colour.Black);
		
		Set<Integer> detectivesPos = getDetectivePositions(locations);
		
		
		
		//getting distance to detectives
		int totalDistanceToDetectives = 0;
		Map<Integer, Integer> detectiveDistances = breathfirstNodeSearch(mrX, detectivesPos, nodes, edges);
		if(detectiveDistances != null)
			for(Integer i: detectiveDistances.keySet())
				totalDistanceToDetectives += detectiveDistances.get(i);
		
		int minDistanceToDetectives = Integer.MAX_VALUE;
		for(Integer i: detectiveDistances.keySet())
			minDistanceToDetectives = Math.min(detectiveDistances.get(i), minDistanceToDetectives);
		
		int positionOnBoard = 509 - Math.abs(graphDisplay.getX(mrX) - 509) + 404 - Math.abs(graphDisplay.getY(mrX) - 404);
		
		//getting number of valid moves
		int MrXcurrentOptions = validMoves;
		
		
		return (distanceFromDetectivesScale*totalDistanceToDetectives + 
				currentOptionsScale*MrXcurrentOptions + 
				minDistanceScale*minDistanceToDetectives + 
				positionScale*positionOnBoard);
	}

	/**
	 * @param Map of all colour players to their locations,
	 * @return A set holding the detective positions
	 */
	static Set<Integer> getDetectivePositions(EnumMap<Colour, Integer> locations) {
		Set<Integer> detectivesPos = new HashSet<Integer>();
		for(Colour c: locations.keySet()){
			if(!c.equals(Colour.Black)){
				detectivesPos.add(locations.get(c));
			}
		}
		return detectivesPos;
	}

	/**
	 * @param mrX location
	 * @param detectives locations
	 * @param A Set holding the nodes of the graph,
	 * @param A list holding the edges of the graph,
	 * @return A Map from detective location to distance from Mr X
	 */
	static Map<Integer, Integer>  breathfirstNodeSearch(Integer mrX, Set<Integer> d, Set<Node<Integer>> nodes2, List<Edge<Integer, Route>> edges2) {
			
			List<Edge<Integer, Route>> edges = new ArrayList<Edge<Integer, Route>>(edges2);
			Set<Node<Integer>> nodes = new HashSet<Node<Integer>>(nodes2);
			Set<Integer> detectives = new HashSet<Integer>(d);
			
			int currentDistance = 0;
			
			//hash table of detective location against distance.
			Map<Integer, Integer> detectiveDistances = new HashMap<Integer, Integer>();
			
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
							
							int eq = (int) (Math.log(currentDistance)*500);
							
							detectiveDistances.put((Integer) n.data(), eq);
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
	 * @param List of all edges
	 * @return Set of neighbouring nodes to currentNodes
	 */
	static Set<Node<Integer>> getNeighbours(Set<Node<Integer>> currentNodes, Set<Node<Integer>> nodes, List<Edge<Integer, Route>> edges) {
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
	static Node<Integer> findNode(Integer i, Set<Node<Integer>> nodes) {
		for(Node<Integer> node: nodes){
			if(node.data().equals(i)){
				return node;
			}
		}
		return null;
	}
	
	/**
	 * @param mrX location
	 * @param detectives location
	 * @param A Set holding the nodes of the graph,
	 * @param A list holding the edges of the graph,
	 * @return Integer of distance from Mr X to detective.
	 */
	static int pairBreathfirstNodeSearch(Integer mrX, Integer detect,	Set<Node<Integer>> nodes, List<Edge<Integer, Route>> edges) {
		
		if(mrX.equals(detect)){
			return 0;
		}
	
		List<Edge<Integer, Route>> edges1 = new ArrayList<Edge<Integer, Route>>(edges);
		Set<Node<Integer>> nodes1 = new HashSet<Node<Integer>>(nodes);
		
		for(Edge<Integer, Route> e: edges){
			if(e.data().equals(Route.Boat)){
				edges1.remove(e);
			}
		}
		
		int currentDistance = 0;
		
		//Initialise distance to maximum.
		int distance = Integer.MAX_VALUE;
		
		//Start at Mr X location.
		Set<Node<Integer>> currentNodes =  new HashSet<Node<Integer>>();
		Node<Integer> detectNode = findNode(detect, nodes1);
		if(detectNode == null){
			System.err.println("Mr X not on valid location");
		}
		currentNodes.add(detectNode);
		//Remove visited Nodes.
		nodes1.remove(detectNode);
		//while there are detective still to reach.
		while( distance == Integer.MAX_VALUE){
			
			//Get nodes one step away.
			Set<Node<Integer>> neighbours = getNeighbours(currentNodes, nodes1, edges1);
			currentDistance++;
			//Remove seen nodes.
			nodes1.remove(neighbours);
			
			//If they are detective locations update the shortest distance.
			for(Node<Integer> n: neighbours){
				if(mrX.equals(n.data())){
					return currentDistance;
				}				
			}
			
			currentNodes = neighbours;
		}
		return 0;
	}
	
}

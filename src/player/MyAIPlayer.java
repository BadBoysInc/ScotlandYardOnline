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
		//\\//\\//\\//\\//NEED TO LOOK AT MIN DISTANCE SO IT DOESNT WALK TOWARDS ONE DETECTIVE//\\//\\//\\//\\
		
		//getting number of valid moves
		int MrXcurrentOptions = validMoves(mrX, detectives, graph.getNodes(), graph.getEdges(), MrXTickets).size();
		
		//number of modes of transport available
		//is in a corner? mabye? possibly?
		//possible positions
		
		/*
		 * Taken from a ScotlandYard Strategy Guide
		 * 
The strategy here is all about concealment. Your goal is to make the detectives believe that you could be in a large number of possible locations, too many for them to guard adequately.

1. Surface in highly connected locations
If Mr. X surfaces on a well-connected location, then there are many places where he could be located on the following turn. As the detectives attempt to narrow in on your position, they will find that it is difficult to close in the perimeter tightly without offering you an escape route.

A location with all three transportation methods is also a great place to use a black ticket, greatly increasing the number of locations where you may be hiding.

2. Avoid undergrounds
Undergrounds should be used only when they offer Mr. X an opportunity to move a significant distance from the detectives. In any other situation, the use of an underground ticket only serves to limit the number of possible locations where Mr. X could lie.

3. Taxis are good
A taxi can be as good as a black ticket in many situations. Taxi routes go everywhere, so using many taxi tickets can greatly increase the number of locations where you could be hiding.

On the other hand, using too many taxis will restrict you to a relatively small area of the board. So don't rely on them completely.

4. Plausible alternate routes
Bluff whenever possible. Make the detectives believe you are taking the "obvious" route, when in reality you are going by the back door. In practice, this means selecting your tickets carefully. While your "back door" route may let you move with a bus, the "obvious" route may not... so move using tickets that are consistent with the "obvious" route.

5. Double move across surfacing locations
Every time your location is revealed, your set of possible locations reduces to a single point, and the detectives are given an opportunity to narrow their containment circle. You can mitigate this effect by double-moving across the surfacing locations: take the first half of the move, surface, then take the second half of the move. If it would serve to further confuse the detectives, consider adding a black ticket for the second leg of the journey.

6. Keep your distance
You are safe when you move to locations that are at least two jumps away from each of the detectives. Stick to the safe locations unless you have a good reason to do otherwise.

7. Take a risk
If the detectives play well, they will usually close in tightly on Mr. X about three times during the course of a game. You have two double move tickets that can help you to escape twice. How do you escape the third time? Take a risk. In my experience, a successful Mr. X makes exactly one very risky move through the course of a game. Look for an opportunity to slip within one move of a detective if it will set you up to escape to open territory. If you get lucky and are not captured, the detectives will be clustered together in a poor position to regroup.

8. Count tickets
In the last eight or so turns of the game, the detectives may begin running low on some tickets. Use this to your advantage. A detective who runs out of taxi tickets can only move on bus stops, and that may leave a big fat hole for you to escape through.

9. Psychology is important
Be unpredictable. Use a double move to double back on your original location. Use a black ticket to conceal something as innocuous as a taxi trip. Bluff an escape down the river, and instead head into more dangerous territory. Even if you get caught, you earn a reputation as a gutsy criminal... and might have an easier game next time.

10. Learn the map
There are some spots on the map that are simply bad news. Most notorious must be the area around the Regent's Park in the top left, which has some really poorly connected locations. The lower left can be troublesome as well, because the Kensington Gardens/Hyde Park area can make it difficult for Mr. X to escape the area. It's okay to pass through these regions, but try to avoid getting cornered there or you could have a tough game.

		 */
		
		
		//Scaling factors
		int distanceFromDetectivesScale = 1;
		int currentOptionsScale = 1;
		
		return (distanceFromDetectivesScale*totalDistanceToDetectives + currentOptionsScale*MrXcurrentOptions);
		
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

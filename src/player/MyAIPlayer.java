package player;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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


public class MyAIPlayer implements Player{

	ScotlandYardView view;
	String graphFilename;
	Graph<Integer, Route> graph;
	Set<Integer> detectives;
	Set<Colour> players;
	HashMap<Colour, HashMap<Ticket, Integer>> Tickets;
	HashMap<Colour, Integer> Locations;
	boolean playerPrint = false;
	boolean levelPrint = true;
	GraphDisplay graphDisplay;
	
	
	public MyAIPlayer(ScotlandYardView view, String graphFilename) {
		this.view = view;
		this.graphFilename = graphFilename;
	}
	
	void scoreInit() throws IOException{
		
		//getting detective locations
		detectives = new HashSet<Integer>();
		Locations = new HashMap<Colour, Integer>();
		players = new HashSet<Colour>();
		
		for(Colour player: this.view.getPlayers()){
			if(player != Colour.Black){
				detectives.add(this.view.getPlayerLocation(player));
			}
			Locations.put(player, view.getPlayerLocation(player));
			players.add(player);	
			
		}
		
		//getting all tickets
		
		Tickets = new HashMap<Colour, HashMap<Ticket, Integer>>();
		
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
	private int score(HashMap<Colour, Integer> locations, Set<Node<Integer>> nodes, Set<Edge<Integer, Route>> edges, HashMap<Colour, HashMap<Ticket, Integer>> tickets){
		
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
		int MrXcurrentOptions = validMoves(locations, Colour.Black, nodes, edges, tickets).size();
		
		
		
		
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
		
		Set<Node<Integer>> nodes = graph.getNodes();
		Set<Edge<Integer, Route>> edges = graph.getEdges();
		Set<Move> singlemoves = validMoves(Locations, Colour.Black, nodes, edges, Tickets);
		System.out.println(singlemoves);
		for(Move MrXMove: singlemoves){
			HashMap<Colour, Integer> mrxlocations = new HashMap<Colour, Integer>(Locations);
			HashMap<Colour, HashMap<Ticket, Integer>> mrxtickets = new HashMap<Colour, HashMap<Ticket, Integer>>(Tickets);
			int target = 0;
			if(MrXMove instanceof MoveDouble){
				target = ((MoveDouble) MrXMove).move2.target;
				HashMap<Ticket, Integer> tmptickets = mrxtickets.get(Colour.Black);
				tmptickets.put(((MoveDouble) MrXMove).move2.ticket, tmptickets.get(((MoveDouble) MrXMove).move2.target)-1);
				tmptickets.put(((MoveDouble) MrXMove).move1.ticket, tmptickets.get(((MoveDouble) MrXMove).move1.target)-1);
			}else{
				target = ((MoveTicket) MrXMove).target;
				HashMap<Ticket, Integer> tmptickets = mrxtickets.get(Colour.Black);
				tmptickets.put(((MoveTicket) MrXMove).ticket, tmptickets.get(((MoveTicket) MrXMove).ticket)-1);
			}
			mrxlocations.put(Colour.Black, target);
			
			Iterator<Colour> playersGet = players.iterator();
			while(!playersGet.next().equals(Colour.Black)){	}
			
			
			MrXList.put(MrXMove, minMaxCalcNAI(playersGet, 2, mrxlocations, mrxtickets, nodes, edges));
			
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
	
	private int minMaxCalcNAI(Iterator<Colour> playersGet, int level, HashMap<Colour, Integer> locations, HashMap<Colour, HashMap<Ticket, Integer>> tickets, Set<Node<Integer>> nodes, Set<Edge<Integer, Route>> edges){
		
		Colour currentPlayer;
		if(playersGet.hasNext()){
			currentPlayer = playersGet.next();
		}else{
			playersGet = players.iterator();
			currentPlayer = playersGet.next();
		}
		
		if(level == 0){
			return score(locations, nodes, edges, tickets);
		}
		
		Set<Integer> childScores = new HashSet<Integer>();
		boolean noMoves = false;
		Set<Move> validMoves = validMoves(locations, currentPlayer, nodes, edges, tickets);
		if(validMoves.isEmpty()){
			noMoves = true;
			validMoves.add(MovePass.instance(currentPlayer));
		}
		
		for(Move currentMove: validMoves){
			
			HashMap<Colour, HashMap<Ticket, Integer>> newTickets = new HashMap<Colour, HashMap<Ticket, Integer>>();
			for(Colour c: tickets.keySet()){
				newTickets.put(c, new HashMap<Ticket, Integer>(tickets.get(c)));
			}
			HashMap<Colour, Integer> newLocations = new HashMap<Colour, Integer>(locations);
			if(!noMoves){
				HashMap<Ticket, Integer> tmptickets = newTickets.get(currentPlayer);
				tmptickets.put(((MoveTicket) currentMove).ticket, tmptickets.get(((MoveTicket) currentMove).ticket)-1);
				if(!currentPlayer.equals(Colour.Black))
					newTickets.get(Colour.Black).put(((MoveTicket) currentMove).ticket, newTickets.get(Colour.Black).get(((MoveTicket) currentMove).ticket)+1);
				newLocations.put(currentPlayer, ((MoveTicket) currentMove).target);
			}
			childScores.add(minMaxCalcNAI(playersGet, level-1,newLocations, newTickets, nodes, edges));
		}
		if(currentPlayer.equals(Colour.Black))
			return max(childScores);
		return min(childScores);
	}
	
	/*
	private int minMaxCalc(int level, HashMap<Colour, Integer> locations, HashMap<Colour, HashMap<Ticket, Integer>> tickets, Set<Node<Integer>> nodes, Set<Edge<Integer, Route>> edges){
		
		
		
		
		if(level == 0){
			return score(locations, nodes, edges, tickets);
		}
		
		HashMap<Ticket, Integer> tmptickets;
		
			Set d1 = new HashSet<Integer>();
			boolean d1Pass = false;
			Set<Move> d1Moves = validMoves(locations, Colour.Blue, nodes, edges, tickets);
			if(d1Moves.isEmpty()){
				d1Pass = true;
			}
			for(Move d1Move: d1Moves){
				
				HashMap<Colour, HashMap<Ticket, Integer>> d1tickets = new HashMap<Colour, HashMap<Ticket, Integer>>();
				for(Colour c: tickets.keySet()){
					d1tickets.put(c, new HashMap<Ticket, Integer>(tickets.get(c)));
				}
				HashMap<Colour, Integer> d1locations = new HashMap<Colour, Integer>(locations);
				if(!d1Pass){
					tmptickets = d1tickets.get(Colour.Blue);
					tmptickets.put(((MoveTicket) d1Move).ticket, tmptickets.get(((MoveTicket) d1Move).ticket)-1);
					d1tickets.get(Colour.Black).put(((MoveTicket) d1Move).ticket, d1tickets.get(Colour.Black).get(((MoveTicket) d1Move).ticket)+1);
					d1locations.put(Colour.Blue, ((MoveTicket) d1Move).target);
				}
				Set d2 = new HashSet<Integer>();
				boolean d2Pass = false;
				Set<Move> d2Moves = validMoves(d1locations, Colour.Green, nodes, edges, tickets);
				if(d2Moves.isEmpty()){
					d2Pass = true;
				}
				for(Move d2Move: d2Moves){
					
					HashMap<Colour, HashMap<Ticket, Integer>> d2tickets = new HashMap<Colour, HashMap<Ticket, Integer>>();
					for(Colour c: d1tickets.keySet()){
						d2tickets.put(c, new HashMap<Ticket, Integer>(d1tickets.get(c)));
					}
					
					HashMap<Colour, Integer> d2locations = new HashMap<Colour, Integer>(d1locations);
					if(!d2Pass){
						tmptickets = d2tickets.get(Colour.Green);
						tmptickets.put(((MoveTicket) d2Move).ticket, tmptickets.get(((MoveTicket) d2Move).ticket)-1);
						d2tickets.get(Colour.Black).put(((MoveTicket) d2Move).ticket, d2tickets.get(Colour.Black).get(((MoveTicket) d2Move).ticket)+1);
						d2locations.put(Colour.Green, ((MoveTicket) d2Move).target);
					}
					boolean d3Pass = false;
					Set<Move> d3Moves = validMoves(d2locations, Colour.Red, nodes, edges, tickets);
					if(d3Moves.isEmpty()){
						d3Pass = true;
					}
					Set d3 = new HashSet<Integer>();
					for(Move d3Move: d3Moves){
						HashMap<Colour, HashMap<Ticket, Integer>> d3tickets = new HashMap<Colour, HashMap<Ticket, Integer>>();
						for(Colour c: d2tickets.keySet()){
							d3tickets.put(c, new HashMap<Ticket, Integer>(d2tickets.get(c)));
						}
						HashMap<Colour, Integer> d3locations = new HashMap<Colour, Integer>(d2locations);
						if(!d2Pass){
							tmptickets = d3tickets.get(Colour.Red);
							tmptickets.put(((MoveTicket) d3Move).ticket, tmptickets.get(((MoveTicket) d3Move).ticket)-1);
							d3tickets.get(Colour.Black).put(((MoveTicket) d3Move).ticket, d3tickets.get(Colour.Black).get(((MoveTicket) d3Move).ticket)+1);
							d3locations.put(Colour.Red, ((MoveTicket) d3Move).target);
						}
						boolean d4Pass = false;
						Set<Move> d4Moves = validMoves(d3locations, Colour.White, nodes, edges, tickets);
						if(d4Moves.isEmpty()){
							d4Pass = true;
						}
						Set d4 = new HashSet<Integer>();
						for(Move d4Move: d4Moves){
							HashMap<Colour, HashMap<Ticket, Integer>> d4tickets = new HashMap<Colour, HashMap<Ticket, Integer>>();
							for(Colour c: d3tickets.keySet()){
								d4tickets.put(c, new HashMap<Ticket, Integer>(d3tickets.get(c)));
							}
							HashMap<Colour, Integer> d4locations = new HashMap<Colour, Integer>(d3locations);
							if(d4Pass){
								tmptickets = d4tickets.get(Colour.White);
								tmptickets.put(((MoveTicket) d4Move).ticket, tmptickets.get(((MoveTicket) d4Move).ticket)-1);
								d4tickets.get(Colour.Black).put(((MoveTicket) d4Move).ticket, d4tickets.get(Colour.Black).get(((MoveTicket) d4Move).ticket)+1);
								d4locations.put(Colour.White, ((MoveTicket) d4Move).target);
							}
							boolean d5Pass = false;
							Set<Move> d5Moves = validMoves(d4locations, Colour.Yellow, nodes, edges, tickets);
							if(d5Moves.isEmpty()){
								d5Pass = true;
							}
							Set d5 = new HashSet<Integer>();
							for(Move d5Move: d5Moves){
								HashMap<Colour, HashMap<Ticket, Integer>> d5tickets = new HashMap<Colour, HashMap<Ticket, Integer>>();
								for(Colour c: d4tickets.keySet()){
									d5tickets.put(c, new HashMap<Ticket, Integer>(d4tickets.get(c)));
								}
								HashMap<Colour, Integer> d5locations = new HashMap<Colour, Integer>(d4locations);
								if(d5Pass){
									tmptickets = d5tickets.get(Colour.Yellow);
									tmptickets.put(((MoveTicket) d5Move).ticket, tmptickets.get(((MoveTicket) d5Move).ticket)-1);
									d5tickets.get(Colour.Black).put(((MoveTicket) d5Move).ticket, d5tickets.get(Colour.Black).get(((MoveTicket) d5Move).ticket)+1);
									d5locations.put(Colour.Yellow, ((MoveTicket) d5Move).target);
								}
								Set Mrx = new HashSet<Integer>();
								for(Move MrXMove: validMoves(d5locations, Colour.Black, nodes, edges, tickets)){
									HashMap<Colour, HashMap<Ticket, Integer>> xtickets = new HashMap<Colour, HashMap<Ticket, Integer>>(d5tickets);
									for(Colour c: d5tickets.keySet()){
										xtickets.put(c, new HashMap<Ticket, Integer>(d5tickets.get(c)));
									}
									HashMap<Colour, Integer> xlocations = new HashMap<Colour, Integer>(d5locations);
									int target = 0;
									if(MrXMove instanceof MoveDouble){
										target = ((MoveDouble) MrXMove).move2.target;
										tmptickets = xtickets.get(Colour.Black);
										tmptickets.put(((MoveDouble) MrXMove).move2.ticket, tmptickets.get(((MoveDouble) MrXMove).move2.ticket)-1);
										tmptickets.put(((MoveDouble) MrXMove).move1.ticket, tmptickets.get(((MoveDouble) MrXMove).move1.ticket)-1);
									}else{
										target = ((MoveTicket) MrXMove).target;
										tmptickets = xtickets.get(Colour.Black);
										tmptickets.put(((MoveTicket) MrXMove).ticket, tmptickets.get(((MoveTicket) MrXMove).ticket)-1);
									}
									xlocations.put(Colour.Black, target);
									if(levelPrint){
										String s = "";
										for(int x = 0; x<level; x++){
											s = s + "\t";
										}
										s = s + "Level: " + Integer.toString(level);
										System.out.println(s);
									}
									
									Mrx.add(minMaxCalc(level-1,xlocations,xtickets,nodes, edges));
								}
								d5.add(max(Mrx));
								if(playerPrint)System.out.println("\t1 Finished");
							}
							d4.add(min(d5));
							if(playerPrint)System.out.println("\t\t2 Finished");
						}
						d3.add(min(d4));
						if(playerPrint)System.out.println("\t\t\t3 Finished");
					}
					d2.add(min(d3));
					if(playerPrint)System.out.println("\t\t\t\t4 Finished");
				}
				d1.add(min(d2));
				if(playerPrint)System.out.println("\t\t\t\t\t5 Finished");
			}
			if(playerPrint)System.out.println("Move Analysed");
		return min(d1);
		
	}
	*/
	
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
			scoreInit();
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
	/*
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
    */
	
    private Set<Move> validMoves(HashMap<Colour, Integer> locations, Colour currentColour, Set<Node<Integer>> nodes, Set<Edge<Integer, Route>> edges, HashMap<Colour, HashMap<Ticket, Integer>> Tickets) {
    	Set<Move> moves = new HashSet<Move>();
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

import net.PlayerFactory;
import player.AIMrXPlayerFactory;
import player.AIMrXPlayerFactory.PlayerType;
import scotlandyard.Colour;
import scotlandyard.ScotlandYard;
import scotlandyard.Spectator;
import scotlandyard.Ticket;
import solution.ScotlandYardModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The GuiGame is an application that allows you to play a game on
 * the Gui on a local machine without the need for a server or judge.
 */
public class GuiGame {
	
	static Set<Integer> locations;
	
    public static void main(String[] args) {
    	
    	locations = new HashSet<Integer>();
    	
        List<Boolean> rounds = Arrays.asList(
                false,
                false, false,
                true,
                false, false, false, false,
                true,
                false, false, false, false,
                true,
                false, false, false, false,
                true,
                false, false, false, false, false,
                true);
        String graphFilename     = "resources/graph.txt";
        String positionsFilename = "resources/pos.txt";
        String imageFilename     = "resources/map.jpg";

        Map<Colour, PlayerType> typeMap = new HashMap<Colour, PlayerType>();
        typeMap.put(Colour.Black,  AIMrXPlayerFactory.PlayerType.XAI);
        typeMap.put(Colour.Blue,   AIMrXPlayerFactory.PlayerType.Random);
        typeMap.put(Colour.Green,  AIMrXPlayerFactory.PlayerType.Random);
        typeMap.put(Colour.Red,    AIMrXPlayerFactory.PlayerType.Random);
        typeMap.put(Colour.White,  AIMrXPlayerFactory.PlayerType.Random);
        typeMap.put(Colour.Yellow, AIMrXPlayerFactory.PlayerType.Random);


        Map<Ticket, Integer> mrXTickets = new HashMap<Ticket, Integer>();
        mrXTickets.put(Ticket.Taxi,        4);
        mrXTickets.put(Ticket.Bus,         4);
        mrXTickets.put(Ticket.Underground, 4);
        mrXTickets.put(Ticket.Secret,      5);
        mrXTickets.put(Ticket.Double,      2);


        Map<Ticket, Integer> detectiveXTickets = new HashMap<Ticket, Integer>();
        detectiveXTickets.put(Ticket.Bus,         8);
        detectiveXTickets.put(Ticket.Underground, 4);
        detectiveXTickets.put(Ticket.Taxi,        11);


        PlayerFactory factory = new AIMrXPlayerFactory(typeMap, imageFilename, positionsFilename);
        ScotlandYard game = new ScotlandYardModel(5, rounds, graphFilename);
        game.join(factory.player(Colour.Black,  game, graphFilename), Colour.Black, getRandLocation(), mrXTickets);
        game.join(factory.player(Colour.Blue,   game, graphFilename), Colour.Blue, getRandLocation(), new HashMap<Ticket, Integer>(detectiveXTickets));
        game.join(factory.player(Colour.Green,  game, graphFilename), Colour.Green, getRandLocation(), new HashMap<Ticket, Integer>(detectiveXTickets));
        game.join(factory.player(Colour.Red,    game, graphFilename), Colour.Red, getRandLocation(), new HashMap<Ticket, Integer>(detectiveXTickets));
        game.join(factory.player(Colour.Yellow, game, graphFilename), Colour.Yellow, getRandLocation(), new HashMap<Ticket, Integer>(detectiveXTickets));
        game.join(factory.player(Colour.White,  game, graphFilename), Colour.White, getRandLocation(), new HashMap<Ticket, Integer>(detectiveXTickets));


        for (Spectator spec : factory.getSpectators(game))
            game.spectate(spec);

        if (game.isReady()) factory.ready();
        game.start();

    }
    
    
    static int getRandLocation(){
    	int r = (int) (Math.random()*198+1);
    	while(locations.contains(r)){
    		 r = (int) (Math.random()*198+1);
    	}
    	return r;
    }
    
    
}

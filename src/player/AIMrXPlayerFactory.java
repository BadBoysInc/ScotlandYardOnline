package player;

import gui.Gui;
import net.PlayerFactory;
import scotlandyard.Colour;
import scotlandyard.Player;
import scotlandyard.ScotlandYardView;
import scotlandyard.Spectator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The RandomPlayerFactory is an example of a PlayerFactory that
 * creates a series of random players. By default it assigns
 * a random AI to each of the colours except Blue, which
 * is controlled by the GUI.
 */
public class AIMrXPlayerFactory implements PlayerFactory {
    protected Map<Colour, PlayerType> typeMap;

    public enum PlayerType {XAI, SimpAI, Random, GUI}

    String imageFilename;
    String positionsFilename;

    protected List<Spectator> spectators;
    AIAssistedGUI gui;


    public AIMrXPlayerFactory() {
        typeMap = new HashMap<Colour, PlayerType>();
        typeMap.put(Colour.Black, AIMrXPlayerFactory.PlayerType.XAI);
        typeMap.put(Colour.Blue, AIMrXPlayerFactory.PlayerType.GUI);
        typeMap.put(Colour.Green, AIMrXPlayerFactory.PlayerType.SimpAI);
        typeMap.put(Colour.Red, AIMrXPlayerFactory.PlayerType.SimpAI);
        typeMap.put(Colour.White, AIMrXPlayerFactory.PlayerType.SimpAI);
        typeMap.put(Colour.Yellow, AIMrXPlayerFactory.PlayerType.SimpAI);

        positionsFilename = "resources/pos.txt";
        imageFilename     = "resources/map.jpg";

        spectators = new ArrayList<Spectator>();
    }

    public AIMrXPlayerFactory(Map<Colour, PlayerType> typeMap, String imageFilename, String positionsFilename) {
        //this.typeMap = typeMap;
    	this();
        this.imageFilename = imageFilename;
        this.positionsFilename = positionsFilename;
        spectators = new ArrayList<Spectator>();
    }


	@Override
    public Player player(Colour colour, ScotlandYardView view, String mapFilename) {
        switch (typeMap.get(colour)) {
            case XAI:
                return new MyAIPlayer(view, mapFilename);
            case SimpAI:
            	return new MySimpleAIPlayer(view, mapFilename);
            case Random:
                return new RandomPlayer(view, mapFilename);
            case GUI:
                return gui(view);
            default:
                return new RandomPlayer(view, mapFilename);
        }
    }

    @Override
    public void ready() {
        if (gui != null) gui.run();
    }

    @Override
    public List<Spectator> getSpectators(ScotlandYardView view) {
        List<Spectator> specs = new ArrayList<Spectator>();
        specs.add(gui(view));
        return specs;
    }

    @Override
    public void finish() {
        if (gui != null) gui.update();
    }



    private AIAssistedGUI gui(ScotlandYardView view) {
        System.out.println("GUI");
        if (gui == null) {
			try {
				gui = new AIAssistedGUI(view, imageFilename, positionsFilename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            spectators.add(gui);
		}
        return gui;
    }
}

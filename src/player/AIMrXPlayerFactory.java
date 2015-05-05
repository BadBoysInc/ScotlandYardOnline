package player;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import net.PlayerFactory;
import newgui.AIAssistedGUI;
import newgui.PossibleMovesOverLay;
import scotlandyard.Colour;
import scotlandyard.Player;
import scotlandyard.ScotlandYardView;
import scotlandyard.Spectator;

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
	/**
	 * Sets default players and resources.
	 */
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
    	this();
    	//this.typeMap = typeMap;
        this.imageFilename = imageFilename;
        this.positionsFilename = positionsFilename;
        spectators = new ArrayList<Spectator>();
    }

    /**
     * Chooses player type according to global player map and colour.
     * @param colour,
     * @param Shared view
     * @param map file name
     */
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

	/**
	 * Starts the Gui.
	 */
    @Override
    public void ready() {
        if (gui != null) gui.run();
    }
    
    /**
     * Adds the gui to the spectator list so it knows what moves have been made.
     */
    @Override
    public List<Spectator> getSpectators(ScotlandYardView view) {
        List<Spectator> specs = new ArrayList<Spectator>();
        specs.add(gui(view));
        return specs;
    }
    
    /**
     * Finishes the Gui.
     */
    @Override
    public void finish() {
        if (gui != null) gui.update();
    }

    /**
     * Creates the AI assisted Gui.
     * @param View with data in, common to all players.
     * @return AIAssistedGUI to play moves with
     */
    private AIAssistedGUI gui(ScotlandYardView view) {
        System.out.println("GUI");
        if (gui == null) {
			try {
				JFrame.setDefaultLookAndFeelDecorated(true);
				gui = new AIAssistedGUI(view, imageFilename, positionsFilename, makeOverlay());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            spectators.add(gui);
            
            
		}
        return gui;
    }

    public PossibleMovesOverLay overlay;
    /**
     * Initialises the PossibleMovesOverLay and returns a reference to it. 
     * @return PossibleMovesOverLay
     */
	private PossibleMovesOverLay makeOverlay() {
			
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();

        //If translucent windows aren't supported print our failed.
        if (!graphicsDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
            System.out.println("failed");
        }else{
        
	        // Create the screen on a new thread
	        Thread over = new Thread() {
	            @Override
	            public void run() {
	            	overlay = new PossibleMovesOverLay();

	            	// Set the window to 55% opaque (45% translucent).
	                overlay.setOpacity(0.50f);
	
	                // Display the window.
	                overlay.setVisible(true);
	            }
	        };
	        
	        over.start();
	        try {
	        	//wait until thread have finished
				over.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
        }
        return overlay;

	}
	
}











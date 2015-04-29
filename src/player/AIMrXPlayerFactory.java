package player;

import gui.Gui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.PlayerFactory;
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

    PossibleMovesOverLay overlay;

    AIAssistedGUI gui;

    public AIMrXPlayerFactory() {
        typeMap = new HashMap<Colour, PlayerType>();
        typeMap.put(Colour.Black, AIMrXPlayerFactory.PlayerType.Random);
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
				JFrame.setDefaultLookAndFeelDecorated(true);
				gui = new AIAssistedGUI(view, imageFilename, positionsFilename);
				makeOverlay();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            spectators.add(gui);
            spectators.add(overlay);
            
		}
        return gui;
    }

	private void makeOverlay() {
			
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	        GraphicsDevice gd = ge.getDefaultScreenDevice();

	        //If translucent windows aren't supported, exit.
	        if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
	            
	        }else{
	        
		        // Create the GUI on the event-dispatching thread
		        SwingUtilities.invokeLater(new Runnable() {
		            @Override
		            public void run() {
		            	overlay = new PossibleMovesOverLay();
		            	
		                // Set the window to 55% opaque (45% translucent).
		                overlay.setOpacity(0.50f);
		
		                // Display the window.
		                overlay.setVisible(true);
		            }
		        });
	        }
	}
}











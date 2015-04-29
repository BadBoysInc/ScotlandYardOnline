package newgui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import player.GraphDisplay;

public class PossiblePositionsPanel extends JPanel {

	Image blackLocation, background, suggested;
	GraphDisplay position;
	
	PossiblePositionsPanel() throws IOException{
		super();
		blackLocation = ImageIO.read(new File("resources/BlackLocation.png"));
		background = ImageIO.read(new File("resources/transparent.png"));
		suggested = ImageIO.read(new File("resources/suggestion.png"));

		position = new GraphDisplay();
	}
	
	
	
	 @Override
     protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawImage(background, 0, 0, null);
        int x = PossibleMovesOverLay.recommendedMove;
        if(x != 0){
        	g.drawImage(suggested, position.getX(x) - 35, position.getY(x) - 35, null);
        }
        
        for(Integer i: PossibleMovesOverLay.pos){
			g.drawImage(blackLocation, position.getX(i) - 18, position.getY(i) - 18, null);
		}
     }
	 
}

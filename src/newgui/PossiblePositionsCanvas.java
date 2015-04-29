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

public class PossiblePositionsCanvas extends JPanel {

	Image blackLocation;
	Image background;
	GraphDisplay position;
	
	PossiblePositionsCanvas() throws IOException{
		super();
		blackLocation =  ImageIO.read(new File("resources/BlackLocation.png"));
		background =  ImageIO.read(new File("resources/transparent.png"));

		position = new GraphDisplay();
	}
	
	
	
	 @Override
     public Dimension getPreferredSize() {
         return new Dimension(1018, 809);
     }
	
	 @Override
     protected void paintComponent(Graphics g) {
        super.paintComponent(g);
         
        Set<Integer> pos = new HashSet<Integer>();
        pos.add(new Integer(1));
         
        
        for(Integer i: pos){
			g.drawImage(blackLocation, position.getX(i) - 18, position.getY(i) - 18, null);
		}
     }
	 
}

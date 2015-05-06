import java.util.ArrayList;
import java.util.List;

import player.ScoreBoard;


public class BadBoyTests extends Object{
	static int y;
	static List<Integer> l;
	public static void main(String[] args) {
		
		l = new ArrayList<Integer>();
		l.add(30);
		l.add(25);
		l.add(35);
		l.add(32);
		l.add(27);
		
		
		for(y = 0; y<5;y++){
			
			int val = l.get(y); 
			Thread t = new Thread(){
				
				@Override
				public void run() {
					System.out.println("Starting "+val);
					GuiGame g = null;
					int total = 0;
					
					//ScoreBoard.distanceFromDetectivesScale = val;
					for(int x = 0; x<500; x++){
						//System.out.println("Test: "+x);
						g = new GuiGame();
						total += g.run(val);
						//System.out.println(x+": "+total);
					}
					
					
					System.out.println("Final "+val+": "+ total);
				}
				
			};
			t.setName(Integer.toString(val));
			t.start();
			
		}
	}

	
	
	
}

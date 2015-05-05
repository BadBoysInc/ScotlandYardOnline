
public class BadBoyTests extends Object{

	public static void main(String[] args) {
		GuiGame g = null;
		int total = 0;
		
		for(int x = 0; x<100; x++){
			g = new GuiGame();
			total += g.run();
			System.out.println(x+": "+total);
		}
		
		
		System.out.println(3+": "+ total);
	}

	
	
	
}

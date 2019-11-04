import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
	
	static LinkedList<Game> activeGames = new LinkedList<Game>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerSocket socket;
		
		try {

			socket = new ServerSocket(5000);
			
			while(true) {
				if (activeGames.size() == 1)
					System.out.println("There is 1 game on");
				else
					System.out.println("There are " + activeGames.size() + " games going on");
				Socket client = socket.accept();
				Game game = new Game(client);
				Socket secondClient = socket.accept();
				game.addPlayer(secondClient);
				activeGames.add(game);
				Thread t = new Thread(game);
				t.start();
				
			}
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
		catch (NumberFormatException e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void removeGames() {
		LinkedList<Game> toRemove = new LinkedList<Game>();
		for (int i = 0; i < activeGames.size(); ++i) {
			Game g = activeGames.get(i);
			if (g.gameStatus == Game.ENDED)
				toRemove.add(g);
		}
		activeGames.removeAll(toRemove);
	}
}

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	static int games = 0;
	static int players = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerSocket socket;
		
		try {

			socket = new ServerSocket(5000);
			
			while(true) {
				if (games == 1)
					System.out.println("There is 1 game on");
				else
					System.out.println("There are " + games + " games going on");
				Socket client = socket.accept();
				Game game = new Game(client);
				Socket secondClient = socket.accept();
				game.addPlayer(secondClient);
				Thread t = new Thread(game);
				t.start();
				++games;
			}
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
		catch (NumberFormatException e){
			System.out.println(e.getMessage());
		}
	}
}

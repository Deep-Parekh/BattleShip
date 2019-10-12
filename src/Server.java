import java.net.ServerSocket;
import java.net.Socket;

public class Server{
	
	static int players = 0;
	static Game[] games = new Game[5];
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerSocket socket;
		try {

			socket = new ServerSocket(5000);
			
			while(true) {
				Socket client = socket.accept();
				Thread thread = new Thread(new Game(client)); 
				thread.start();
				if (players == 1)
					System.out.println("There is 1 Player online.");
				else
					System.out.println("There are " + players + " players online.");
				if (players % 2 == 0) {
					int gameNumber = players/2;
					System.out.println("Starting game " + gameNumber);
					games[gameNumber-1] = new Game();
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
		
	}
}

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

	public Socket clientSocket = null;
	static int players = 0;
	
	public Server(Socket client) {
		clientSocket = client;
		players++;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerSocket socket;
		try {

			socket = new ServerSocket(5000);
			
			while(true) {
				Socket client = socket.accept();
				Thread thread = new Thread(new Server(client)); 
				thread.run();
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	
	public void run() {
		if (players % 2 == 1)
			System.out.println("Waiting for another player to join... ");
		else {
			try {
				DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
				
			}catch(IOException e) {
				System.out.print(e.getMessage());
			}
		}
	}

}

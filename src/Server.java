import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

	public Socket clientSocket = null;
	static int players = 0;
	Message clientMessage = new Message();
	
	public Server(Socket client) {
		clientSocket = client;
		players++;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerSocket socket;
		Thread[] onlinePlayers = new Thread[10];
		try {

			socket = new ServerSocket(5000);
			
			while(true) {
				System.out.println("There are " + players + " players online.");
				Socket client = socket.accept();
				Thread thread = new Thread(new Server(client)); 
				onlinePlayers[players-1] = thread;
				thread.start();
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	
	public void run() {
		System.out.println(Thread.currentThread().getName()+ " is running...");
		try {
			ObjectOutputStream toClient = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream fromClient = new ObjectInputStream(clientSocket.getInputStream());
			toClient.writeObject(new Message(Message.MSG_REQUEST_INIT));
			System.out.println("Received Request from Player " + players);
			Message playerMsg = (Message) fromClient.readObject();
			System.out.println("Client Message: " + playerMsg.getMsgType());
			System.out.println(playerMsg.Ftable);
		}catch(IOException e) {
			System.out.print(e.getMessage());
		}catch (ClassNotFoundException e) {
			System.out.print(e.getMessage());
		}
	}

}

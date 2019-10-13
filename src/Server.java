import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Server implements Runnable{
	
	static int players = 0;
	static Server previous = null;
	
	final int WAITING_FOR_SECOND_PLAYER = 1;
	final int IN_PROGRESS = 2;
	final int ENDED = 3;
	
	int gameStatus;

	Socket player1 = null;
	//ObjectOutputStream toPlayer1 = null;
	//ObjectInputStream fromPlayer1 = null;
	Socket player2 = null;
	//ObjectOutputStream toPlayer2 = null;
	//ObjectInputStream fromPlayer2 = null;
	
	public Server(Socket client) {
		player1 = client;
		setUpPlayer(player1);
		gameStatus = WAITING_FOR_SECOND_PLAYER;
		players++;
		previous = this;
	}
	
	public void addPlayer(Socket client) {
		player2 = client;
		setUpPlayer(player2);
		gameStatus = IN_PROGRESS;
	}
	
	public void setUpPlayer(Socket player) {
		try {
			ObjectOutputStream toPlayer = new ObjectOutputStream(player1.getOutputStream());
			ObjectInputStream fromPlayer = new ObjectInputStream(player1.getInputStream());
			sendRequest(toPlayer, Message.MSG_REQUEST_INIT);
			receiveResponse(fromPlayer);
		}catch(IOException e) {
			System.out.print(e.getMessage());
		}catch (ClassNotFoundException e) {
			System.out.print(e.getMessage());
		}
	}

	public void sendRequest(ObjectOutputStream toPlayer, int Request) throws IOException {
		toPlayer.writeObject(new Message(Request));
	}
	
	public void receiveResponse(ObjectInputStream fromPlayer) throws IOException, ClassNotFoundException{
		Message playerMsg = (Message) fromPlayer.readObject();
		if (playerMsg.getMsgType() == Message.MSG_RESPONSE_INIT)
			System.out.println("Received Response" + Message.MSG_RESPONSE_INIT + " from Player "); 		
	}
	
	public void playGame() {
		
	}
	
	public void run() {
		System.out.println(Thread.currentThread().getName()+ " is running...");
		while (player2 == null) {
			
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerSocket socket;
		try {

			socket = new ServerSocket(5000);
			
			while(true) {
				System.out.println("There are " + players + " players online.");
				Socket client = socket.accept();
				if (players%2 == 1)
					previous.setUpPlayer(client);
				else {
					Thread thread = new Thread(new Server(client)); 
					thread.start();
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
}

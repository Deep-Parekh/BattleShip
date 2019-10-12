import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * Represents a game being played between two clients (players)
 */

public class Game implements Runnable{

	/*
	 * Status can be set to:
	 * 		0 - started
	 * 		1 - waiting for second player
	 * 		2 - in progress
	 * 		3 - ended
	 */
	final int STARTED = 0;
	final int WAITING_FOR_SECOND_PLAYER = 1;
	final int IN_PROGRESS = 2;
	final int ENDED = 3;
	
	int status;
	int players = 0;
	Socket player1;		// The first server game thread is set as player 1
	ObjectOutputStream toPlayer1 = null;
	ObjectInputStream fromPlayer1 = null;
	Socket player2;		// The second server game thread is set as player 2
	ObjectOutputStream toPlayer2 = null;
	ObjectInputStream fromPlayer2 = null;
	
	public Game() {
		status  = 0;
	}
	public Game(Socket player1) throws IOException {
		this.player1 = player1;
		toPlayer1 = new ObjectOutputStream(player1.getOutputStream());
		fromPlayer1 = new ObjectInputStream(player1.getInputStream());
		setUpPlayer(toPlayer1, fromPlayer1);
		players = 1;
		status = WAITING_FOR_SECOND_PLAYER;
	}
	
	public Game(Socket player1, Socket player2) throws IOException {
		this.player1 = player1;
		toPlayer1 = new ObjectOutputStream(player1.getOutputStream());
		fromPlayer1 = new ObjectInputStream(player1.getInputStream());
		setUpPlayer(toPlayer1, fromPlayer1);
		toPlayer2 = new ObjectOutputStream(player2.getOutputStream());
		fromPlayer2 = new ObjectInputStream(player2.getInputStream());
		setUpPlayer(toPlayer2, fromPlayer2);
		this.player2 = player2;
		players = 2;
		status = IN_PROGRESS;
	}
	public void addPlayer(Socket player2) {
		this.player2 = player2;
	}
	
	public void run() {
		
	}
	public void setUpPlayer(ObjectOutputStream to, ObjectInputStream from){
		try {
			sendRequest(to, Message.MSG_REQUEST_INIT);
			receiveResponse(from);
			if (players % 2 == 1)
				toPlayer1.writeUTF("Waiting for another player to join the game...");
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
			System.out.println("Received Request from Player "); 		// Which player (specify)
	}

}

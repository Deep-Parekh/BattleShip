import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Game implements Runnable {
		
	final int WAITING_FOR_SECOND_PLAYER = 1;
	final int IN_PROGRESS = 2;
	final int ENDED = 3;
	
	int gameStatus;

	Player player1 = null;
	BattleShipTable player1Board = null;

	Player player2 = null;
	BattleShipTable player2Board = null;
	
	boolean turn = true;		// If true that means its player 1's turn else payer 2's turn
	
	public Game(Socket client) {
		player1 = new Player(client);
	}
	
	public void addPlayer(Socket client) {
		player2 = new Player(client);
	}
	
	public void setUpPlayer(Player player) {
		try {
			player.sendMessage(new Message(Message.MSG_REQUEST_INIT));
			Message playerMsg = (Message) player.receiveMessage();
			if (playerMsg.getMsgType() == Message.MSG_RESPONSE_INIT)
				System.out.println("Received Response" + Message.MSG_RESPONSE_INIT + " from Player "); 
			if (player2 == null)
				player.sendMessage(new Message("Waiting for player 2 to join..."));
		}catch(IOException e) {
			System.out.print(e.getMessage());
		}
	}
	
	public void playGame() {
		try {
			while(gameStatus != ENDED) {
				if (turn == true) {
					turn(player1, player2Board);
				}else {
					turn(player2, player1Board);	
				}
			}
		} catch (Exception e){
			
		}
	}
	
	public void turn(Player player, BattleShipTable board) throws IOException{
		player.sendMessage(new Message(Message.MSG_REQUEST_PLAY));
		player.receiveMessage();					
		Message hit = (Message) player.receiveMessage();
		int[] bomb = hit.blockBomb;
		if (player.getBoard().table[bomb[0]][bomb[1]] == "Z")
			board.table[bomb[0]][bomb[1]] = BattleShipTable.MISS_SYMBOL;
		else
			board.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
	}
	
	public void run() {
		try {
			setUpPlayer(player1);
			player1Board = new BattleShipTable();		// Used for displaying to player2
			gameStatus = WAITING_FOR_SECOND_PLAYER;
			while (player2 == null) {
				// this needs to be changed so that this thread will 
				// get alerted when player 2 joins the game 
			}
			setUpPlayer(player2);
			player2Board = new BattleShipTable();		// Used for displaying to player1
			while (gameStatus != ENDED) {
				playGame();
			}
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}


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
	BattleShipTable player2GuessBoard = null;		

	Player player2 = null;
	BattleShipTable player1GuessBoard = null;
	
	boolean turn = true;		// If true that means its player 1's turn else payer 2's turn
	
	public Game(Socket client) {
		player1 = new Player(client);
	}
	
	public Game(Socket player1, Socket player2) {
		this.player1 = new Player(player1);
		this.player2 = new Player(player1);
	}
	
	public void addPlayer(Socket client) {
		player2 = new Player(client);
		gameStatus = IN_PROGRESS;
	}
	
	public void setUpPlayer(Player player) {
		try {
			player.sendMessage(new Message(Message.MSG_REQUEST_INIT));
			Message playerMsg = (Message) player.receiveMessage();
			if (playerMsg.getMsgType() == Message.MSG_RESPONSE_INIT) {
				System.out.println("Received Response" + Message.MSG_RESPONSE_INIT + " from Player "); 
				player.board = playerMsg.Ftable;
			}
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
					player2.sendMessage(new Message("Waiting for opponent to play..."));
					turn(player1, player1GuessBoard, player2GuessBoard);
				}else {
					player1.sendMessage(new Message("Waiting for opponent to play..."));
					turn(player2, player2GuessBoard, player1GuessBoard);	
				}
				turn = (!turn);
			}
		} catch (Exception e){
			
		}
	}
	
	public void turn(Player player, BattleShipTable board, BattleShipTable hits) throws IOException{
		player.sendMessage(new Message(Message.MSG_REQUEST_PLAY));
		player.receiveMessage();					
		Message hit = (Message) player.receiveMessage();
		int[] bomb = hit.blockBomb;
		if (player.getBoard().table[bomb[0]][bomb[1]] == "Z") {
			board.table[bomb[0]][bomb[1]] = BattleShipTable.MISS_SYMBOL;
			player.sendMessage(new Message("You missed", board, hideShips(player.board, hits)));
		}
		else {
			board.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
			player.sendMessage(new Message("You hit opponent's ship!", board, hideShips(player.board, hits)));
		}
	}
	
	public BattleShipTable hideShips(BattleShipTable withShips, BattleShipTable withHits) {
		String[][] returnTable = new String[10][10];
		for(int i=0;i<10;++i){
			for(int j=0;j<10;++j){
				String element = withHits.table[i][j];
				String shipElement = withShips.table[i][j];
				if (element == "Z" && shipElement != "Z") {
					returnTable[i][j] = shipElement;
				}
				else 
					returnTable[i][j] = element;
			}		
		}
		return new BattleShipTable(returnTable);
	}
	
	public void run() {
		try {
			System.out.println(Thread.currentThread().getName() + " has started");
			setUpPlayer(player1);
			player2GuessBoard = new BattleShipTable();		// Used for displaying to player2
			gameStatus = WAITING_FOR_SECOND_PLAYER;
			while (gameStatus == WAITING_FOR_SECOND_PLAYER) {
				
			}
			setUpPlayer(player2);
			player1GuessBoard = new BattleShipTable();		// Used for displaying to player1
			while (gameStatus != ENDED) {
				playGame();
			}
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}


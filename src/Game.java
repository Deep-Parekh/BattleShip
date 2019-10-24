import java.io.IOException;
import java.net.Socket;

public class Game implements Runnable {
		
	final int WAITING_FOR_SECOND_PLAYER = 1;
	final int IN_PROGRESS = 2;
	final int ENDED = 3;
	
	int gameStatus;

	Player player1 = null;

	Player player2 = null;
	
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
				player.setBoard(playerMsg.Ftable);
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
					turn(player1, player2);
				}else {
					player1.sendMessage(new Message("Waiting for opponent to play..."));
					turn(player2, player1);	
				}
				turn = (!turn);
			}
		} catch (Exception e){
			
		}
	}
	
	public void turn(Player turnPlayer, Player waitingPlayer) throws IOException{
		turnPlayer.sendMessage(new Message(Message.MSG_REQUEST_PLAY));		
		Message hit = (Message) turnPlayer.receiveMessage();
		int[] bomb = hit.blockBomb;
		BattleShipTable board = waitingPlayer.getBoard();
		if (board.table[bomb[0]][bomb[1]].equals("Z")) {
			board.table[bomb[0]][bomb[1]] = BattleShipTable.MISS_SYMBOL;
			turnPlayer.sendMessage(new Message("You missed", turnPlayer.getBoard(), board.encrypt()));
			waitingPlayer.sendMessage(new Message("Your opponent missed", board));
		}
		else {
			board.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
			turnPlayer.sendMessage(new Message("You hit opponent's ship!", turnPlayer.getBoard(), board.encrypt()));
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
			player2.sendMessage(new Message("Player1 is setting up their board "));
			setUpPlayer(player1);
			player1.sendMessage(new Message("Waiting for second player to set up their board "));
			setUpPlayer(player2);
			while (gameStatus != ENDED) {
				playGame();
			}
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}


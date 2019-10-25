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
		String turnPlayerMsg = "";
		String waitingPlayerMsg = "";
		BattleShipTable board = waitingPlayer.getBoard();
		if (board.table[bomb[0]][bomb[1]].equals("Z")) {
			board.table[bomb[0]][bomb[1]] = BattleShipTable.MISS_SYMBOL;
			turnPlayerMsg += "You missed!\n" + waitingPlayer.getRemainingShips();
			waitingPlayerMsg += "Your opponent missed!\n" + waitingPlayer.getRemainingShips();
		}else if(board.table[bomb[0]][bomb[1]].equals("S")){
			board.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
			if (board.submarineCoordinates1.contains(bomb)) 
				board.submarineCoordinates1.remove(bomb);
			else 
				board.submarineCoordinates2.remove(bomb);
			turnPlayerMsg += "You destroyed opponent's submarine!\n" + waitingPlayer.getRemainingShips();
			waitingPlayerMsg += "Opponent destroyed your submarine!\n" + waitingPlayer.getRemainingShips();
		}else if(board.table[bomb[0]][bomb[1]].equals("D")){
			board.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
			if (board.destroyerCoordinates1.contains(bomb)) {
				board.destroyerCoordinates1.remove(bomb);
				if (board.destroyerCoordinates1.size() == 0) {
					turnPlayerMsg += "You destroyed opponent's destroyer!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg += "Opponent destroyed your destroyer!\n" + waitingPlayer.getRemainingShips();
				}
				else
					turnPlayerMsg += "You hit opponent's ship!\n" + turnPlayer.getRemainingShips();
			}
			else {
				board.destroyerCoordinates2.remove(bomb);
				if (board.destroyerCoordinates2.size() == 0) {
					turnPlayerMsg +="You destroyed opponent's destroyer!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg += "Opponent destroyed your destroyer!\n" + waitingPlayer.getRemainingShips();
				}
				else
					turnPlayerMsg += "You hit opponent's ship!\n" + waitingPlayer.getRemainingShips();
			}
		}else if(board.table[bomb[0]][bomb[1]].equals("A")) {
			board.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
			if (board.aircraftCoordinates1.contains(bomb)) {
				board.aircraftCoordinates1.remove(bomb);
				if (board.aircraftCoordinates1.size() == 0) {
					turnPlayerMsg += "You destroyed opponent's aircraft carrier!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg += "Opponent destroyed your aircraft carrier!\n" + waitingPlayer.getRemainingShips();
				}
				else
					turnPlayerMsg += "You hit opponent's ship!\n" + turnPlayer.getRemainingShips();
			}
			else {
				board.aircraftCoordinates2.remove(bomb);
				if (board.aircraftCoordinates2.size() == 0) {
					turnPlayerMsg +="You destroyed opponent's aircraft carrier!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg += "Opponent destroyed your aircraft carrier!\n" + waitingPlayer.getRemainingShips();
				}
				else
					turnPlayerMsg += "You hit opponent's ship!\n" + waitingPlayer.getRemainingShips();
			}
		}
		if (waitingPlayer.remainingShips == 0) {
			turnPlayerMsg += "You destroyed all your opponent's ships,\nCongratulations, you win!";
			waitingPlayerMsg += "Your opponent destroyed all of your ships,\nYou lose";
		}
		turnPlayer.sendMessage(new Message(turnPlayerMsg, turnPlayer.getBoard(), board.encrypt()));
		waitingPlayer.sendMessage(new Message(waitingPlayerMsg));
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


import java.io.IOException;
import java.net.Socket;

/**
 * @author dparekh
 *
 */
public class Game implements Runnable {
		
	final int WAITING_FOR_SECOND_PLAYER = 1;
	final int IN_PROGRESS = 2;
	final int ENDED = 3;
	
	int gameStatus;

	Player player1 = null;
	volatile BattleShipTable player1Board = null;

	Player player2 = null;
	volatile BattleShipTable player2Board = null;
	
	boolean turn = true;		// If true that means its player 1's turn else payer 2's turn
	
	public Game(Socket client) {
		player1 = new Player(client);
	}
	
	public Game(Socket player1, Socket player2) {
		this.player1 = new Player(player1);
		this.player2 = new Player(player2);
	}
	
	public void addPlayer(Socket client) {
		player2 = new Player(client);
		gameStatus = IN_PROGRESS;
	}
	
	public BattleShipTable setUpPlayer(Player player) {
		Message playerMsg = null;
		try {
			player.sendMessage(new Message(Message.MSG_REQUEST_INIT));
			playerMsg = player.receiveMessage();
			if (playerMsg.getMsgType() == Message.MSG_RESPONSE_INIT) {
				System.out.println("Received Response" + Message.MSG_RESPONSE_INIT + " from Player "); 
			}
		}catch(IOException e) {
			System.out.print(e.getMessage());
		}
		return playerMsg.Ptable;
	}
	
	public void playGame() throws IOException {
		if (turn == true) {
			turn(player1, player1Board, player2, player2Board);
		}else {
			turn(player2, player2Board, player1, player1Board);
		}
		turn = (!turn);
	}
	
	public void turn(Player turnPlayer, BattleShipTable turnBoard, Player waitingPlayer, BattleShipTable waitingBoard) throws IOException{	
		turnPlayer.sendMessage(new Message(Message.MSG_YOUR_TURN, "Your turn", new BattleShipTable(turnBoard.table), waitingBoard.encrypt()));		
		waitingPlayer.sendMessage(new Message(Message.MSG_OPPONENT_TURN, "Opponents turn\n", new BattleShipTable(waitingBoard.table), turnBoard.encrypt()));	
		//turnPlayer.sendMessage(new Message(Message.MSG_YOUR_TURN));
		//waitingPlayer.sendMessage(new Message(Message.MSG_OPPONENT_TURN));
		Message hit = (Message) turnPlayer.receiveMessage();
		int[] bomb = hit.blockBomb;
		String turnPlayerMsg = new String();
		String waitingPlayerMsg = new String();
		switch (waitingBoard.table[bomb[0]][bomb[1]]){
			case BattleShipTable.DEFAULT_SYMBOL:
				waitingBoard.table[bomb[0]][bomb[1]] = BattleShipTable.MISS_SYMBOL;
				turnPlayerMsg = "You missed!\n" + waitingPlayer.getRemainingShips(waitingBoard);
				waitingPlayerMsg = "Your opponent missed!\n" + waitingPlayer.getRemainingShips(waitingBoard);
				break;
		
			case BattleShipTable.AIRCRAFT_CARRIER_SYMBOL:
				waitingBoard.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
				waitingBoard.removeShip(waitingBoard.aircraftCoordinates1,bomb);
				waitingBoard.removeShip(waitingBoard.aircraftCoordinates2,bomb);
				if (waitingBoard.aircraftCoordinates1.size() == 0 || waitingBoard.aircraftCoordinates2.size() == 0) {
					turnPlayerMsg = "You destroyed opponent's aircraft carrier!\n" + waitingPlayer.getRemainingShips(waitingBoard);
					waitingPlayerMsg = "Opponent destroyed your aircraft carrier!\n" + waitingPlayer.getRemainingShips(waitingBoard);
				}else{
					turnPlayerMsg = "You hit opponent's ship!\n" + waitingPlayer.getRemainingShips(waitingBoard);
					waitingPlayerMsg = "Opponent hit your ship!\n" + waitingPlayer.getRemainingShips(waitingBoard);
				}
				break;
				
			case BattleShipTable.DESTROYER_SYMBOL:
				waitingBoard.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
				waitingBoard.removeShip(waitingBoard.destroyerCoordinates1,bomb);
				waitingBoard.removeShip(waitingBoard.destroyerCoordinates2,bomb);
				if (waitingBoard.destroyerCoordinates1.size() == 0 || waitingBoard.destroyerCoordinates2.size() == 0) {
					turnPlayerMsg = "You destroyed opponent's destroyer!\n" + waitingPlayer.getRemainingShips(waitingBoard);
					waitingPlayerMsg = "Opponent destroyed your destroyer!\n" + waitingPlayer.getRemainingShips(waitingBoard);
				}else {
					turnPlayerMsg = "You hit opponent's ship!\n" + waitingPlayer.getRemainingShips(waitingBoard);
					waitingPlayerMsg = "Opponent hit your ship!\n" + waitingPlayer.getRemainingShips(waitingBoard);
				}
				break;
				
			case BattleShipTable.SUBMARINE_SYMBOL:
				waitingBoard.table[bomb[0]][bomb[1]] = BattleShipTable.HIT_SYMBOL;
				waitingBoard.removeShip(waitingBoard.submarineCoordinates1,bomb);
				waitingBoard.removeShip(waitingBoard.submarineCoordinates2,bomb);
				turnPlayerMsg = "You destroyed opponent's submarine!\n" + waitingPlayer.getRemainingShips(waitingBoard);
				waitingPlayerMsg = "Opponent destroyed your submarine!\n" + waitingPlayer.getRemainingShips(waitingBoard);
				break;
		}
		if (waitingPlayer.remainingShips == 0) {
			gameStatus = ENDED;
			turnPlayerMsg = "You destroyed all of Opponents' ships. You win!\n"+ waitingPlayer.getRemainingShips(waitingBoard);
			waitingPlayerMsg = "Opponent destroyed all your ships. You lose!\n" + waitingPlayer.getRemainingShips(waitingBoard);
			turnPlayer.sendMessage(new Message(Message.MSG_REQUEST_GAME_OVER, turnPlayerMsg, turnBoard, waitingBoard.encrypt()));
			waitingPlayer.sendMessage(new Message(Message.MSG_REQUEST_GAME_OVER, waitingPlayerMsg, waitingBoard, turnBoard.encrypt()));
			return;
		}
		turnPlayer.sendMessage(new Message(turnPlayerMsg, new BattleShipTable(turnBoard.table), waitingBoard.encrypt())); // Encrypt needs to be added before submitting
		waitingPlayer.sendMessage(new Message(waitingPlayerMsg, new BattleShipTable(waitingBoard.table), turnBoard.encrypt()));
	}
	
	
	@Override
	public void run() {
		try {
			System.out.println(Thread.currentThread().getName() + " has started");
			//player2.sendMessage(new Message("Player1 is setting up their board "));
			player1Board = setUpPlayer(player1);
			player1.sendMessage(new Message("Waiting for other player to set up their board "));
			player2Board = setUpPlayer(player2);
			player2.sendMessage(new Message("Player 1 is waiting"));
			this.gameStatus = IN_PROGRESS;
			System.out.println("Playing game now");
			while(this.gameStatus != ENDED) {
				playGame();
			}
			System.out.println("Game on " + Thread.currentThread().getName() + " is over");
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}

/*
 * Not working Properly
 * 
 * 
	public void turn(Player turnPlayer, Player waitingPlayer) throws IOException{
		turnPlayer.sendMessage(new Message(Message.MSG_YOUR_TURN, "Your turn\n", turnPlayer.getBoard(), waitingPlayer.getBoard().encrypt()));		
		waitingPlayer.sendMessage(new Message(Message.MSG_OPPONENT_TURN, "Opponents turn\n", waitingPlayer.getBoard(), turnPlayer.getBoard().encrypt()));		
		Message hit = turnPlayer.receiveMessage();
		int[] bomb = hit.blockBomb;
		System.out.println("Received hit");
		BattleShipTable board = waitingPlayer.getBoard();
		String turnPlayerMsg = null;
		String waitingPlayerMsg = null;
		switch (board.table[bomb[0]][bomb[1]]){
			case BattleShipTable.DEFAULT_SYMBOL:
				board.insert(bomb, BattleShipTable.MISS_SYMBOL);
				turnPlayerMsg = "You missed!\n" + waitingPlayer.getRemainingShips();
				waitingPlayerMsg = "Your opponent missed!\n" + waitingPlayer.getRemainingShips();
				break;
		
			case BattleShipTable.AIRCRAFT_CARRIER_SYMBOL:
				board.insert(bomb, BattleShipTable.HIT_SYMBOL);
				board.removeShip(board.aircraftCoordinates1,bomb);
				board.removeShip(board.aircraftCoordinates2,bomb);
				if (board.aircraftCoordinates1.size() == 0 || board.aircraftCoordinates2.size() == 0) {
					turnPlayerMsg = "You destroyed opponent's aircraft carrier!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg = "Opponent destroyed your aircraft carrier!\n" + waitingPlayer.getRemainingShips();
				}else{
					turnPlayerMsg = "You hit opponent's ship!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg = "Opponent hit your ship!\n" + waitingPlayer.getRemainingShips();
				}
				break;
				
			case BattleShipTable.DESTROYER_SYMBOL:
				board.insert(bomb, BattleShipTable.HIT_SYMBOL);
				board.removeShip(board.destroyerCoordinates1,bomb);
				board.removeShip(board.destroyerCoordinates2,bomb);
				if (board.destroyerCoordinates1.size() == 0 || board.destroyerCoordinates2.size() == 0) {
					turnPlayerMsg = "You destroyed opponent's destroyer!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg = "Opponent destroyed your destroyer!\n" + waitingPlayer.getRemainingShips();
				}else {
					turnPlayerMsg = "You hit opponent's ship!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg = "Opponent hit your ship!\n" + waitingPlayer.getRemainingShips();
				}
				break;
				
			case BattleShipTable.SUBMARINE_SYMBOL:
				board.insert(bomb, BattleShipTable.HIT_SYMBOL);
				board.removeShip(board.submarineCoordinates1,bomb);
				board.removeShip(board.submarineCoordinates2,bomb);
				turnPlayerMsg = "You destroyed opponent's submarine!\n" + waitingPlayer.getRemainingShips();
				waitingPlayerMsg = "Opponent destroyed your submarine!\n" + waitingPlayer.getRemainingShips();
				break;
		}
		waitingPlayer.setBoard(board);
		if (waitingPlayer.remainingShips == 0) {
			gameStatus = ENDED;
			turnPlayerMsg = "You destroyed all of Opponents' ships. You win!\n"+ waitingPlayer.getRemainingShips();
			waitingPlayerMsg = "Opponent destroyed all your ships. You lose!\n" + waitingPlayer.getRemainingShips();
			turnPlayer.sendMessage(new Message(Message.MSG_REQUEST_GAME_OVER, turnPlayerMsg, turnPlayer.getBoard(), waitingPlayer.getBoard().encrypt()));
			waitingPlayer.sendMessage(new Message(Message.MSG_REQUEST_GAME_OVER, waitingPlayerMsg, waitingPlayer.getBoard(), turnPlayer.getBoard().encrypt()));
		}
		else { 
			turnPlayer.sendMessage(new Message(turnPlayerMsg, turnPlayer.getBoard(), waitingPlayer.getBoard())); // Encrypt needs to be added before submitting
			waitingPlayer.sendMessage(new Message(waitingPlayerMsg, waitingPlayer.getBoard(), turnPlayer.getBoard()));
		}
	}
	
	public void turn(Player turnPlayer, BattleShipTable turnBoard, Player waitingPlayer, BattleShipTable waitingBoard) throws IOException{
		turnPlayer.sendMessage(new Message(Message.MSG_YOUR_TURN, "Your turn", turnPlayer.getBoard(), waitingPlayer.getBoard().encrypt()));		
		waitingPlayer.sendMessage(new Message(Message.MSG_OPPONENT_TURN, "Opponents turn\n", waitingPlayer.getBoard(), turnPlayer.getBoard().encrypt()));		
		Message hit = (Message) turnPlayer.receiveMessage();
		int[] bomb = hit.blockBomb;
		System.out.println("Received hit");
		String turnPlayerMsg = null;
		String waitingPlayerMsg = null;
		switch (waitingBoard.table[bomb[0]][bomb[1]]){
			case BattleShipTable.DEFAULT_SYMBOL:
				waitingBoard.insert(bomb, BattleShipTable.MISS_SYMBOL);
				turnPlayerMsg = "You missed!\n" + waitingPlayer.getRemainingShips();
				waitingPlayerMsg = "Your opponent missed!\n" + waitingPlayer.getRemainingShips();
				break;
		
			case BattleShipTable.AIRCRAFT_CARRIER_SYMBOL:
				waitingBoard.insert(bomb, BattleShipTable.HIT_SYMBOL);
				waitingBoard.removeShip(waitingBoard.aircraftCoordinates1,bomb);
				waitingBoard.removeShip(waitingBoard.aircraftCoordinates2,bomb);
				if (waitingBoard.aircraftCoordinates1.size() == 0 || waitingBoard.aircraftCoordinates2.size() == 0) {
					turnPlayerMsg = "You destroyed opponent's aircraft carrier!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg = "Opponent destroyed your aircraft carrier!\n" + waitingPlayer.getRemainingShips();
				}else{
					turnPlayerMsg = "You hit opponent's ship!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg = "Opponent hit your ship!\n" + waitingPlayer.getRemainingShips();
				}
				break;
				
			case BattleShipTable.DESTROYER_SYMBOL:
				waitingBoard.insert(bomb, BattleShipTable.HIT_SYMBOL);
				waitingBoard.removeShip(waitingBoard.destroyerCoordinates1,bomb);
				waitingBoard.removeShip(waitingBoard.destroyerCoordinates2,bomb);
				if (waitingBoard.destroyerCoordinates1.size() == 0 || waitingBoard.destroyerCoordinates2.size() == 0) {
					turnPlayerMsg = "You destroyed opponent's destroyer!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg = "Opponent destroyed your destroyer!\n" + waitingPlayer.getRemainingShips();
				}else {
					turnPlayerMsg = "You hit opponent's ship!\n" + waitingPlayer.getRemainingShips();
					waitingPlayerMsg = "Opponent hit your ship!\n" + waitingPlayer.getRemainingShips();
				}
				break;
				
			case BattleShipTable.SUBMARINE_SYMBOL:
				waitingBoard.insert(bomb, BattleShipTable.HIT_SYMBOL);
				waitingBoard.removeShip(waitingBoard.submarineCoordinates1,bomb);
				waitingBoard.removeShip(waitingBoard.submarineCoordinates2,bomb);
				turnPlayerMsg = "You destroyed opponent's submarine!\n" + waitingPlayer.getRemainingShips();
				waitingPlayerMsg = "Opponent destroyed your submarine!\n" + waitingPlayer.getRemainingShips();
				break;
		}
		if (waitingPlayer.remainingShips == 0) {
			gameStatus = ENDED;
			turnPlayerMsg = "You destroyed all of Opponents' ships. You win!\n"+ waitingPlayer.getRemainingShips();
			waitingPlayerMsg = "Opponent destroyed all your ships. You lose!\n" + waitingPlayer.getRemainingShips();
			turnPlayer.sendMessage(new Message(Message.MSG_REQUEST_GAME_OVER, turnPlayerMsg, turnBoard, waitingBoard));
			waitingPlayer.sendMessage(new Message(Message.MSG_REQUEST_GAME_OVER, waitingPlayerMsg, waitingBoard, turnBoard));
		}
		else { 
			turnPlayer.sendMessage(new Message(turnPlayerMsg, turnBoard, waitingBoard)); // Encrypt needs to be added before submitting
			waitingPlayer.sendMessage(new Message(waitingPlayerMsg, waitingBoard, turnBoard));
		}
	}
	
*/
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Player {
	
	Socket player;
	ObjectOutputStream toPlayer;
	ObjectInputStream fromPlayer;
	BattleShipTable board;
	
	int remainingShips;
	
	public Player(Socket player) {
		this.player = player;
		try {
			this.toPlayer = new ObjectOutputStream(player.getOutputStream());
			this.fromPlayer = new ObjectInputStream(player.getInputStream());
			this.remainingShips = 6;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Sets the current players game board
	public void setBoard(BattleShipTable board) {
		this.board = board;
	}
	
	public BattleShipTable getBoard() {
		return this.board;
	}
	
	public void sendMessage(Message msg) throws IOException{
		toPlayer.writeObject(msg);
	}
	
	public String getRemainingShips(BattleShipTable board) {
		String returnStr = "";
		int aircraft = 0;
		if (board.aircraftCoordinates1.size() > 0) ++aircraft;
		if (board.aircraftCoordinates2.size() > 0) ++aircraft;
		int destroyer = 0;
		if (board.destroyerCoordinates1.size() > 0) ++destroyer;
		if (board.destroyerCoordinates2.size() > 0) ++destroyer;
		int submarine = 0;
		if (board.submarineCoordinates1.size() > 0) ++submarine;
		if (board.submarineCoordinates2.size() > 0) ++submarine;
		remainingShips = destroyer + submarine + aircraft;
		if (remainingShips == 1)
			returnStr += "\nRemaining Ship: 1\n";
		else
			returnStr += "\nRemaining Ships: " + remainingShips + "\n";
		returnStr += ("\tAircraft Carrier: " + aircraft + "\n");
		returnStr += ("\tDestroyer: " + destroyer + "\n");
		returnStr += ("\tSubmarine: " + submarine + "\n");
		return returnStr;
	}
	
	public Message receiveMessage() {
		try {
			Message from = (Message) fromPlayer.readObject();
			return from;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Message();
	}
}

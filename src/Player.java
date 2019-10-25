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
			toPlayer = new ObjectOutputStream(player.getOutputStream());
			fromPlayer = new ObjectInputStream(player.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Sets the current players game board
	public void setBoard(BattleShipTable board) {
		this.board = board;
		this.remainingShips = 6;
	}
	
	public BattleShipTable getBoard() {
		return this.board;
	}
	
	public void sendMessage(Message msg) throws IOException{
		toPlayer.writeObject(msg);
	}
	
	public String getRemainingShips() {
		String returnStr = "";
		int aircraft = 0;
		if (this.board.aircraftCoordinates1.size() > 0) ++aircraft;
		if (this.board.aircraftCoordinates2.size() > 0) ++aircraft;
		int destroyer = 0;
		if (this.board.destroyerCoordinates1.size() > 0) ++destroyer;
		if (this.board.destroyerCoordinates2.size() > 0) ++destroyer;
		int submarine = 0;
		if (this.board.submarineCoordinates1.size() > 0) ++submarine;
		if (this.board.submarineCoordinates2.size() > 0) ++submarine;
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
			return (Message) fromPlayer.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Message();
	}
}

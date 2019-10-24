import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Player {
	
	Socket player;
	ObjectOutputStream toPlayer;
	ObjectInputStream fromPlayer;
	BattleShipTable board;
	
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
	}
	
	public BattleShipTable getBoard() {
		return this.board;
	}
	
	public void sendMessage(Message msg) throws IOException{
		toPlayer.writeObject(msg);
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

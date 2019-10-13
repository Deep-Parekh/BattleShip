import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Socket client;
		BufferedReader userInput;
		BattleShipTable playerBoard;
		ObjectOutputStream outToServer;
		ObjectInputStream inFromServer ;
		
		try {
			client = new Socket("localhost", 5000);
			userInput = new BufferedReader(new InputStreamReader(System.in));
			outToServer = new ObjectOutputStream(client.getOutputStream());
			inFromServer = new ObjectInputStream(client.getInputStream());
			Message srvMsg = (Message) inFromServer.readObject();
			if (srvMsg.getMsgType() == Message.MSG_REQUEST_INIT);{
				System.out.println("Received message from Server");
				playerBoard = new BattleShipTable();
				System.out.println(playerBoard);
				System.out.println("Set up your board");
				setUpBoard(userInput, playerBoard);
				outToServer.writeObject(new Message(Message.MSG_RESPONSE_INIT, playerBoard));
				System.out.println(inFromServer.read());
			}
				
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}catch (ClassNotFoundException e) {
			System.out.print(e.getMessage());
		}
	}

	public static void promptForAircraft() {
		System.out.println("Enter first two coordinates for Aircraft Carrier (Example: A1 B1): ");
	}
	
	public static void promptForDestroyer() {
		System.out.println("Enter first two coordinates for Destroyer (Example: A1 B1): ");
	}
	
	public static void promptForSubmarine() {
		System.out.println("Enter one coordinate for Submarine (Example: A1): ");
	}
	
	public static void setUpBoard(BufferedReader userInput, BattleShipTable board) throws IOException{
		String[] input;
		for ( int i = 0; i< 2; ++i) {
			promptForAircraft();
			input = userInput.readLine().split(" ");
			board.insertAirCarrier(input[0], input[1]);
			promptForDestroyer();
			input = userInput.readLine().split(" ");
			board.insertDestroyer(input[0], input[1]);
			promptForSubmarine();
			board.insertSubmarine(userInput.readLine());
		}
	}
}

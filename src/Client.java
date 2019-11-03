import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author dparekh
 *
 */
public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Socket server;
		BufferedReader userInput;
		ObjectOutputStream outToServer;
		ObjectInputStream inFromServer;
		
		try {
			server = new Socket("localhost", 5000);
			inFromServer = new ObjectInputStream(server.getInputStream());		// Order should be opposite of
			outToServer = new ObjectOutputStream(server.getOutputStream());		// that on the server
			Message srvMsg = (Message) inFromServer.readObject();
			if (srvMsg.getMsgType() == Message.MSG_REQUEST_INIT);{
				BattleShipTable playerBoard = new BattleShipTable();
				System.out.println(playerBoard);
				System.out.println("Set up your board");
				userInput = new BufferedReader(new InputStreamReader(System.in));
				setUpBoard(userInput, playerBoard);
				System.out.println(playerBoard);
				outToServer.writeObject(new Message(Message.MSG_RESPONSE_INIT, playerBoard));
			}
			srvMsg = (Message) inFromServer.readObject();
			while(srvMsg.getMsgType() != Message.MSG_REQUEST_GAME_OVER) {
				System.out.println(srvMsg.getMsg());
				if (srvMsg.getMsgType() == Message.MSG_OPPONENT_TURN) {
					Thread.sleep(2000);
					srvMsg = (Message) inFromServer.readObject();
					BattleShipTable updatedTable = updateTable(srvMsg);
					System.out.println(srvMsg.getMsg());
					System.out.println("Your updated board: ");
					System.out.println(updatedTable);
					if (srvMsg.getMsgType() == Message.MSG_REQUEST_GAME_OVER)
						break;
				}
				else if (srvMsg.getMsgType() == Message.MSG_YOUR_TURN) {
					promptForHit();
					String input = userInput.readLine();
					BattleShipTable board = new BattleShipTable();
					int[] bomb = board.AlphaNumerictoXY(input);
					Message hit = new Message(bomb);
					outToServer.writeObject(hit);
					srvMsg = (Message) inFromServer.readObject();
					System.out.println("Your board: ");
					System.out.println(srvMsg.Ptable);
					System.out.println("Your guess board: ");
					System.out.println(srvMsg.Ftable);
					System.out.println(srvMsg.getMsg());
					if (srvMsg.getMsgType() == Message.MSG_REQUEST_GAME_OVER)
						break;
				}
				srvMsg = (Message) inFromServer.readObject();
			}
			
			// Game over
			System.out.println("Your board: ");
			System.out.println(srvMsg.Ftable);
			System.out.println("Your guess board: ");
			System.out.println(srvMsg.Ptable);
			System.out.println(srvMsg.getMsg());
		}catch (IOException e) {
			System.out.println(e.getMessage());
		}catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}catch (NumberFormatException e){
			System.out.println(e.getMessage());
		}catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void promptForHit() {
		System.out.println("Enter coordinates of your bomb (Example: A0): ");
	}

	public static void promptForAircraft() {
		System.out.println("Enter first two coordinates for Aircraft Carrier (Example: A0 B0): ");
	}
	
	public static void promptForDestroyer() {
		System.out.println("Enter first two coordinates for Destroyer (Example: A0 B0): ");
	}
	
	public static void promptForSubmarine() {
		System.out.println("Enter one coordinate for Submarine (Example: A0): ");
	}
	
	public static BattleShipTable updateTable(Message serverMsg) {
		BattleShipTable rtnTable = serverMsg.Ptable;
		int[] hit = serverMsg.blockBomb;
		String coord = rtnTable.table[hit[0]][hit[1]];
		if (!coord.equals(BattleShipTable.DEFAULT_SYMBOL))
			rtnTable.table[hit[0]][hit[1]] = BattleShipTable.HIT_SYMBOL;
		else
			rtnTable.table[hit[0]][hit[1]] = BattleShipTable.MISS_SYMBOL;
		return rtnTable;
	}
	
	public static void setUpBoard(BufferedReader userInput, BattleShipTable board) throws IOException{
		String[] input;
		// Add error detection and resending prompt if user enters wrong coords
		for ( int i = 0; i< 2; ++i) {
			promptForAircraft();
			input = userInput.readLine().split(" ");
			while (!board.insertAirCarrier(input[0], input[1])) {
				System.out.println("There was an error, please try again");
				promptForAircraft();
				input = userInput.readLine().split(" ");
			}
			promptForDestroyer();
			input = userInput.readLine().split(" ");
			while (!board.insertDestroyer(input[0], input[1])) {
				System.out.println("There was an error, please try again");
				promptForDestroyer();
				input = userInput.readLine().split(" ");
			}
			promptForSubmarine();
			while (!board.insertSubmarine(userInput.readLine())) {
				System.out.println("There was an error, please try again");
				promptForSubmarine();
			}
		}
	}
}
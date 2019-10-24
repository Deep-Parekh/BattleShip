import java.io.Serializable;

public class Message implements Serializable
{ 
	
	//message types
	static final int MSG_REQUEST_INIT = 1;//sent from server to client
	static final int MSG_RESPONSE_INIT = 2; //sent from client to server
	static final int MSG_REQUEST_PLAY = 3; //sent from server to client
	static final int MSG_RESPONSE_PLAY = 4;//sent from client to server
	static final int MSG_REQUEST_GAME_OVER = 5; //sent from server to client
	
	private int msgType=-1;
	private String msg = null;
	int [] blockBomb = new int[2]; //x, y coordinates of the block on the opponent's board to be bombed; this is for the MSG_RESPONSE_PLAY message
	
	
	public BattleShipTable Ftable = null;//the player's own board (F-board)
	public BattleShipTable Ptable = null;//the player hits and misses on the opponent board (P-board)
	
	// constructor
	public Message() {
		
	}
	public Message(int msgType) 
	{ 
		this.msgType = msgType;
	} 
	
	public Message(String msg) {
		this.msg = msg;
	}
	
	public Message(String msg, int msgType) {
		this.msg = msg;
		this.msgType = msgType;
	}
	
	public Message(BattleShipTable opTable) {
		Ptable = opTable;
	}
	
	public Message(int msgType, BattleShipTable FTable) {
		this.Ftable = FTable;
		this.msgType = msgType;
	}
	
	public Message(String msg, BattleShipTable FTable) {
		this.msg = msg;
		this.Ftable = FTable;
	}
	
	public Message(String msg, BattleShipTable FTable, BattleShipTable PTable) {
		this.msg = msg;
		this.Ftable = FTable;
		this.Ptable = PTable;
	}
	
	public Message(int[] bomb) {
		this.blockBomb = bomb;
	}
	
	//getters
	public String getMsg(){
		return this.msg;
	}
	
	public int getMsgType(){
		return this.msgType;
	}
	//setters
	public void setMsg(String m){
		this.msg = m;
	}
	
	public void setMsgType(int type){
		this.msgType = type;
	}
} 

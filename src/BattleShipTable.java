import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;

public class BattleShipTable implements Serializable
{ 
	/* Constants*/
	//Size of each type of ship
	static final int AIRCRAFT_CARRIER_SIZE = 5;
	static final int DESTROYER_SIZE = 3;
	static final int SUBMARINE_SIZE = 1;
	
	//symbols use on the board
	/*
	   "A": Aircraft
	   "D": Destroyer
	   "S": Submarine
	   
	   "X": Hit
	   "O": Miss
	   "Z": default value
	*/
	
	static final String AIRCRAFT_CARRIER_SYMBOL = "A";
	static final String DESTROYER_SYMBOL = "D";
	static final String SUBMARINE_SYMBOL = "S";
	static final String HIT_SYMBOL = "X";
	static final String MISS_SYMBOL = "O";
	static final String DEFAULT_SYMBOL = "Z";
	
	public String[][] table = null;

	LinkedList<int[]> aircraftCoordinates1 = new LinkedList<int[]>();
	LinkedList<int[]> aircraftCoordinates2 = new LinkedList<int[]>();
	LinkedList<int[]> destroyerCoordinates1 = new LinkedList<int[]>();
	LinkedList<int[]> destroyerCoordinates2 = new LinkedList<int[]>();
	LinkedList<int[]> submarineCoordinates1 = new LinkedList<int[]>();
	LinkedList<int[]> submarineCoordinates2 = new LinkedList<int[]>();

	// constructor 
	public BattleShipTable() 
	{ 
		this.table = new String[10][10];
		//set default values
		for(int i=0;i<10;++i){
			for(int j=0;j<10;++j){
				this.table[i][j] = "Z";
			}		
		}		
	} 
	
	public BattleShipTable(String[][] table) {
		this.table = table;
	}
	
	public boolean removeShip(LinkedList<int[]> ship, int[] bomb){
		if (ship.size() == 0)
			return false;
		int removeIndex = -1;
		for ( int i = 0; i < ship.size(); ++i) {
			if (Arrays.equals(ship.get(i), bomb)) {
				removeIndex = i;
				break;
			}
		}
		if (removeIndex < 0)
			return false;
		ship.remove(removeIndex);
		return true;
	}
	
	/*convert alpha_numeric to the X and Y coordinates*/
	public int[] AlphaNumerictoXY(String alpha_coordinates) throws NumberFormatException{
		//get the alpha part
		int []ret = new int[2];
		ret[0] = this.helperAlphaToX(alpha_coordinates.charAt(0));
		//get the numeric part
		ret[1] = Integer.parseInt(alpha_coordinates.substring(1));
		return ret;
	}
	private int helperAlphaToX(char alpha){
		return alpha - 'A';
	}
	
	private String XYToAlphaNumeric(int []xy){
		return "" + ((char)(xy[0] + 'A')) + "" + xy[1];
	}
	//print out the table
	@Override
	public String toString(){
		String ret = new String();
		System.out.println("    0   1   2   3   4   5   6   7   8   9  ");
		for(int i=0;i<10;++i){
			ret = ret + "" + (char)('A' + i) + " | ";
			for(int j=0;j<10;++j){
				ret = ret + this.table[i][j] + " | ";
			}
			ret = ret + "\n";
		}
		return ret;
	}
	
	public void insert(int[] bomb, String s) {
		this.table[bomb[0]][bomb[1]] = s;
	}
	
	public void insertHit(String x1, String s){
		this.insertSinglePoint(this.AlphaNumerictoXY(x1), s);
	}
	public boolean insertSubmarine(String x1){
		//check if it can be inserted
		if(this.insertSinglePoint(this.AlphaNumerictoXY(x1), "S")) {
			if (this.submarineCoordinates1.size() == 0)
				this.submarineCoordinates1.add(this.AlphaNumerictoXY(x1));
			else
				this.submarineCoordinates2.add(this.AlphaNumerictoXY(x1));
			return true;
		}
		else
			return false;
	}	
	
	public boolean insertAirCarrier(String x1, String x2){
		//check if it can be inserted
		if(this.insertShip(x1, x2, BattleShipTable.AIRCRAFT_CARRIER_SIZE, "A"))
			return true;
		else
			return false;
	}
	
	public boolean insertDestroyer(String x1, String x2){
		//check if it can be inserted	
		if(this.insertShip(x1, x2, BattleShipTable.DESTROYER_SIZE, "D"))
			return true;
		else
			return false;
	}

	private boolean insertShip(String x1, String x2, int len, String s){
		int []xy1 = this.AlphaNumerictoXY(x1);
		int []xy2 = this.AlphaNumerictoXY(x2);
		if(!(xy1[0]>=0 && xy1[0]<=9 && xy1[1]>=0 && xy1[1]<=9)) return false;
		if(!(xy2[0]>=0 && xy2[0]<=9 && xy2[1]>=0 && xy2[1]<=9)) return false;
		
		if(xy1[0] == xy2[0] && (xy1[1]+1) == xy2[1]){// along the x axis
			if(checkAlongXAxis(this.AlphaNumerictoXY(x1),len)){//insert the battleship
				this.insertAlongXAxis(this.AlphaNumerictoXY(x1), len, s);
				return true;
			}else{//prompt the user again
				return false;
			}
		}else if(xy1[1] == xy2[1] && (xy1[0]+1) == xy2[0]){// along the y axis
			if(checkAlongYAxis(this.AlphaNumerictoXY(x1), len)){//insert the battleship
				this.insertAlongYAxis(this.AlphaNumerictoXY(x1), len, s);
				return true;
			}else{//prompt the user again
				return false;
			}
		}else
			return false;
	}
	
	private boolean insertSinglePoint(int[] xy, String s){
		if(this.table[xy[0]][xy[1]].equals("Z")){
			this.table[xy[0]][xy[1]] = s;
			return true;
		}else
			return false;
	}
	
	private boolean checkAlongXAxis(int[] xy, int len){
		if(xy[1]+len > 10) return false;
		for(int j=xy[1];j<xy[1]+len;++j){
			if(!this.table[xy[0]][j].equals("Z"))
				return false;
		}
		return true;
	}
	
	private void insertAlongXAxis(int[] xy, int len, String s){
		LinkedList<int[]> toAdd;
		if (len == BattleShipTable.AIRCRAFT_CARRIER_SIZE) {
			if (this.aircraftCoordinates1.size() == 0)
				toAdd = this.aircraftCoordinates1;
			else
				toAdd = this.aircraftCoordinates2;
		}else{
			if (this.destroyerCoordinates1.size() == 0)
				toAdd = this.destroyerCoordinates1;
			else
				toAdd = this.destroyerCoordinates2;
		}
		for(int j=xy[1];j<xy[1]+len;++j){
			int[] coords = new int[]{xy[0], j};
			toAdd.add(coords);
			this.table[xy[0]][j] = s;
		}
	}
	
	private boolean checkAlongYAxis(int[] xy, int len){
		if(xy[0]+len > 10) return false;
		for(int i=xy[0];i<xy[0]+len;++i){
			if(!this.table[i][xy[1]].equals("Z"))
				return false;
		}
		return true;
	}
	
	private void insertAlongYAxis(int[] xy, int len, String s){
		LinkedList<int[]> toAdd;
		if (len == BattleShipTable.AIRCRAFT_CARRIER_SIZE) {
			if (this.aircraftCoordinates1.size() == 0)
				toAdd = this.aircraftCoordinates1;
			else
				toAdd = this.aircraftCoordinates2;
		}else{
			if (this.destroyerCoordinates1.size() == 0)
				toAdd = this.destroyerCoordinates1;
			else
				toAdd = this.destroyerCoordinates2;
		}
		for(int i=xy[0];i<xy[0]+len;++i){
			int[] coords = new int[]{i, xy[1]};
			toAdd.add(coords);
			this.table[i][xy[1]] = s;				
		}		
	}	
	
	public BattleShipTable encrypt() {
		String[][] encryptedBoard = new String[10][10];
		for(int i=0;i<10;++i){
			for(int j=0;j<10;++j){
				if (this.table[i][j].equals(BattleShipTable.AIRCRAFT_CARRIER_SYMBOL) || 
						this.table[i][j].equals(BattleShipTable.DESTROYER_SYMBOL)|| 
						this.table[i][j].equals(BattleShipTable.SUBMARINE_SYMBOL))
					encryptedBoard[i][j] = BattleShipTable.DEFAULT_SYMBOL;
				else
					encryptedBoard[i][j] = this.table[i][j];
			}		
		}
		return new BattleShipTable(encryptedBoard);
	}
	
	public static void main(String args[]) 
	{ 
		BattleShipTable t = new BattleShipTable();		
		t.insertAirCarrier("C5","C6");
		System.out.println(t.toString());
		if(!t.insertDestroyer("H9", "I9")){
			System.out.println("not able to insert");
		}
		System.out.println(t.toString());
		if(!t.insertDestroyer("H9", "I9")){
			System.out.println("not able to insert");
		}
		System.out.println(t.toString());
		
	} 
} 

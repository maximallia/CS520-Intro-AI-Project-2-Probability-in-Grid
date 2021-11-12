import java.util.ArrayList;
import java.awt.Point;

/**
 * 
 * @author Zachary Tarman
 * Represents the maze used by Agent 6 
 * and all corresponding information
 * that it needs to store.
 *
 */
public class Maze {
	
	/**
	 * The big object that will store all the cell information at the specified coordinates
	 */
	private ArrayList<ArrayList<CellInfo>> maze_structure;
	/**
	 * The number of rows
	 */
    private int rows;
    /**
     * The number of columns
     */
    private int cols;
    /**
     * The number of confirmed unblocked + unconfirmed cells (assumed unblocked under the freespace assumption)
     */
    public int assumedUnblocked;
    /**
     * The position of the agent at the start of the program
     */
    public Point agentStart;
    /**
     * The position of the target
     */
    private Point target;

    
    
    
    /**
     * Retrieves a cell's information given the coordinate
     * @param x		The x-coordinate (corresponding to a given column)
     * @param y		The y-coordinate (corresponding to a given row)
     * @return		The CellInfo object for the cell in question
     * @see			CellInfo
     */
    public CellInfo getCell(int x, int y) {
    	return maze_structure.get(x).get(y);
    }
    
    
    /**
     * Checks if the target is here
     * @param p		The coordinates of the cell the agent is checking
     * @return		True if the target is here, false if not
     */
    public boolean isTarget(Point p) {
    	if (p.getX() == target.getX() && p.getY() == target.getY()) {
    		return true;
    	}
    	return false;
    }
    

    /**
     * Randomly assign a terrain status.
     * First, we consider if the cell is blocked.
     * Next, if the cell is not blocked, randomly assign a terrain:
     * either flat, hilly, or forest.
     * @return 		The int value corresponding to the terrain type
     */
    private int gen_status() {
        double rand = Math.random();
        if (rand <= 0.3) { return 3; } // CELL IS BLOCKED
        
        // CELL IS NOT BLOCKED
        else {
        	double rand2 = Math.random();
        	if (rand2 < 0.333) { // CELL IS NOW FLAT
        		return 0;
        	} else if (rand2 < 0.667) { // CELL IS NOW HILLY
        		return 1;
        	}
        }
        return 2; // OTHERWISE, THE CELL IS A FOREST SPOT
    }
    
    
    
    /**
     * Checks if a neighboring cell's coordinates are in bounds
     * @param coor		The coordinates of the neighbor
     * @return			True if in bounds, false if not
     */
    public boolean inBounds(Point coor) {
		if (coor.getX() < 0 || coor.getX() >= cols || coor.getY() < 0 || coor.getY() >= rows) {
			return false;
		}
		return true;
	}

    
    
    
    /**
     * The constructor of the data structure that actually stores the cells.
     * Setting up all the individual cells for all rows and columns.
     * @param rows		The number of rows in the maze
     * @param cols		The number of columns in the maze
     * @return			The ArrayList of ArrayLists of CellInfo objects
     * @see				CellInfo
     */
    public ArrayList<ArrayList<CellInfo>> maze_create(int rows, int cols) {
        
        maze_structure = new ArrayList<ArrayList<CellInfo>>();
        // System.out.println("The initial number of unknown cells is " + assumedUnblocked);
        double initProb = 1.0 / (double)(assumedUnblocked); // THE INITIAL PROBABILIIY THAT EACH CELL IS THE TARGET (UNDER THE FREESPACE ASSUMPTION)
        // System.out.println("The initial probability for each cell is " + initProb);
        
        for (int col = 0; col < cols; col++){
            
            ArrayList<CellInfo> temp = new ArrayList<CellInfo>();
            
            for (int row = 0; row < rows; row++) {
                
                // STORE THE CELL'S POSITION WITHIN THE MAZE
            	Point pos = new Point(col, row);

                // NEW CELL BEING INSERTED INTO THE MAZE WITH THE CORRECT NUMBER OF NEIGHBORS, THE RIGHT HEURISTIC ESTIMATE, AND RANDOMIZED "BLOCKED" STATUS
                CellInfo temp2 = new CellInfo(pos, gen_status(), initProb);
                temp.add(temp2);
            }

            maze_structure.add(temp);
        }

        // RANDOMLY CHOOSING A STARTING POINT FOR THE AGENT AND A POINT FOR THE TARGET
        // System.out.println("Dimensions of maze are " + cols + "x" + rows);
        boolean targetAssigned = false;
        boolean agentAssigned = false;
        do {
        	int targetX = (int) (Math.random() * cols);
        	int targetY = (int) (Math.random() * rows);
        	// System.out.println("Proposed target is at coordinate " + targetX + ", " + targetY);
        	if (getCell(targetX, targetY).getTerrain().value != 3) {
        		Point temp = new Point(targetX, targetY);
        		target = temp;
        		targetAssigned = true;
        		// System.out.println("Target accepted!");
        	}
        } while (!targetAssigned);
        
        do {
        	int agentX = (int) (Math.random() * cols);
        	int agentY = (int) (Math.random() * rows);
        	// System.out.println("Proposed agent start point is at coordinate " + agentX + ", " + agentY);
        	if (getCell(agentX, agentY).getTerrain().value != 3) {
        		Point temp = new Point (agentX, agentY);
        		agentStart = temp;
        		agentAssigned = true;
        		// System.out.println("Agent start point accepted!");
        	}
        } while (!agentAssigned);
        
        return maze_structure;
    }

    
    
    
    
    /**
     * Constructor.
     * @param rows		The number of rows (provided by the user)
     * @param cols		The number of columns (provided by the user)
     */
    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.assumedUnblocked = this.rows * this.cols;
        this.maze_structure = maze_create(rows, cols);
    }

    
    
    
    
    /**
     * Textually represents the maze and returns the representation as a String
     * @return		String of maze, to be printed to standard output
     */
    @Override
    public String toString() {
        
        StringBuilder builder = new StringBuilder("-----------Maze-----------\n");

        for (int i = 0; i < rows; i++) {
        	
        	for (int j = 0; j < cols; j++) {

        		if (i == agentStart.getY() && j == agentStart.getX()) {
        			builder.append("S"); // MARK START CELL
        			continue;
        		} else if (i == target.getY() && j == target.getX()) {
        			builder.append("T"); // MARK TARGET CELL
        			continue;
        		}

        		if (getCell(j, i).getTerrain().value == 3) {
        			builder.append("x");
        		} else if (getCell(j, i).isVisited()) {
        			if (getCell(j, i).getTerrain().value == 0) {
        				builder.append("_");
        			} else if (getCell(j, i).getTerrain().value == 1) {
        				builder.append("~");
        			} else {
        				builder.append("#");
        			}
        		} else {
        			builder.append(":");
        		}

        	}

        	builder.append("\n");
        }

        return builder.toString();
    }
    
    
    
    // ALL OF THE FOLLOWING IS ESSENTIALLY CODE REUSE FROM THE AGENT SOURCE FILE
    // BUT FOR THE SPECIAL PURPOSE OF DETERIMINING IF THE TARGET IS
    // REACHABLE BEFORE WE EVEN START.
    // IF IT IS, THEN WE PROCEED.
    // OTHERWISE, WE MAKE A DIFFERENT MAZE AND NEW POSITIONS FOR THE TARGET AND AGENT START
    
    /**
     * Checks if the target is reachable before the agent starts its search.
     * Nearly identical to agent's plan method.
     * Only difference is this version has knowledge of all blocks in maze.
     * @return		True if target is reachable, false if it's not
     * @see			Agent6#plan(CellInfo, CellInfo)
     */
    public boolean targetIsReachable() {
    	
    	updateHeur(getCell((int) target.getX(),(int) target.getY()));
    	
		ArrayList<CellInfo> toExplore = new ArrayList<CellInfo>(); // TO STORE THE CELLS TO BE EXPLORED
		ArrayList<CellInfo> doneWith = new ArrayList<CellInfo>();
		
		CellInfo curr = getCell((int) agentStart.getX(), (int) agentStart.getY()); // PTR TO THE CURRENT CELL WE'RE EVALUATING TO MOVE ON FROM IN OUR PLAN
		curr.setG(0);
		
		Point curr_position; // THE COORDINATE OF THE CURRENT CELL THAT WE'RE LOOKING AT
		int x, y; // THE X AND Y VALUES OF THE COORDINATE FOR THE CURRENT CELL THAT WE'RE LOOKING AT (FOR FINDING NEIGHBORING COORDINATES)
		boolean addUp, addDown, addLeft, addRight; // TO INDICATE IF WE CAN PLAN TO GO IN THAT DIRECTION FROM CURRENT CELL

		Point up = new Point(); // COORDINATE OF NORTH NEIGHBOR
		Point down = new Point(); // COORDINATE OF SOUTH NEIGHBOR
		Point left = new Point(); // COORDINATE OF WEST NEIGHBOR
		Point right = new Point(); // COORDINATE OF EAST NEIGHBOR
		
		// DEBUGGING STATEMENT
		// System.out.println("We're in a new planning phase.");
		
		CellInfo first = curr; // THIS IS TO MARK WHERE THE REST OF PLANNING WILL CONTINUE FROM
		toExplore.add(first);

		// BEGIN LOOP UNTIL PLAN REACHES GOAL
		while (toExplore.size() > 0) {

			curr = toExplore.remove(0); // CURRENT CELL THAT WE'RE LOOKING AT
			
			if (contains(curr, doneWith)) { // WE DON'T WANT TO EXPAND THE SAME CELL AGAIN (THIS IS HERE JUST IN CASE)
				// System.out.println("We've already seen this cell and its directions: " + curr.getPos().toString());
				continue;
			}

			// DEBUGGING STATEMENT
			// System.out.println("We're currently figuring out where to plan to go to next from " + curr.getPos().toString());
			
			curr_position = curr.getPos(); // COORDINATE OF THE CELL WE'RE CURRENTLY EXPLORING
			x = (int) curr_position.getX(); // X COORDINATE
			y = (int) curr_position.getY(); // Y COORDINATE
			doneWith.add(curr); // WE DON'T WANT TO EXPAND / LOOK AT THIS CELL AGAIN IN THIS PLANNING PHASE
						
			// IS THIS CELL THE GOAL??
			// IF SO, LET'S TRACE BACK TO OUR STARTING POSITION
			if (x == target.getX() && y == target.getY()) {
				return true;
			}

			// DETERMINE POSSIBLE PLACES TO MOVE FROM CURRENT POSITION
			up.setLocation(x, y - 1); // NORTH
			down.setLocation(x, y + 1); // SOUTH
			left.setLocation(x - 1, y); // WEST
			right.setLocation(x + 1, y); // EAST
			addUp = true;
			addDown = true; 
			addLeft = true;
			addRight = true;

			// CHECK FOR CELLS WE CAN'T / SHOULDN'T EXPLORE OR MOVE INTO ON OUR WAY TO THE GOAL
			// CHECKS FOR NORTHBOUND NEIGHBOR
			if (!inBounds(up)) { 
				addUp = false; 
			} else if (getCell((int) up.getX(), (int) up.getY()).getTerrain().value == 3) {
				addUp = false;
			} else if (contains(getCell((int) up.getX(), (int) up.getY()), doneWith)) {
				addUp = false;
			}
			
			// CHECKS FOR SOUTHBOUND NEIGHBOR
			if (!inBounds(down)) { 
				addDown = false; 
			} else if (getCell((int) down.getX(), (int) down.getY()).getTerrain().value == 3) {
				addDown = false;
			} else if (contains(getCell((int) down.getX(), (int) down.getY()), doneWith)) {
				addDown = false;
			}
			
			// CHECKS FOR WESTBOUND NEIGHBOR
			if (!inBounds(left)) { 
				addLeft = false; 
			} else if (getCell((int) left.getX(), (int) left.getY()).getTerrain().value == 3) {
				addLeft = false;
			} else if (contains(getCell((int) left.getX(), (int) left.getY()), doneWith)) {
				addLeft = false;
			}
			
			// CHECKS FOR EASTBOUND NEIGHBOR
			if (!inBounds(right)) { 
				addRight = false; 
			} else if (getCell((int) right.getX(), (int) right.getY()).getTerrain().value == 3) {
				addRight = false;
			} else if (contains(getCell((int) right.getX(), (int) right.getY()), doneWith)) {
				addRight = false;
			}

			// ADD ALL UNVISITED, UNBLOCKED AND NOT-LOOKED-AT-ALREADY CELLS TO PRIORITY QUEUE + SET PARENTS AND G-VALUES
			CellInfo temp;
			double curr_g = curr.getG(); // THE G_VALUE OF THE CURRENT CELL IN THE PLANNING PROCESS
			if (addUp) { // THE CELL TO OUR NORTH IS A CELL WE CAN EXPLORE
				temp = getCell((int) up.getX(), (int) up.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the north cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			if (addLeft) { // THE CELL TO OUR WEST IS A CELL WE CAN EXPLORE
				temp = getCell((int) left.getX(), (int) left.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the west cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			if (addDown) { // THE CELL TO OUR SOUTH IS A CELL WE CAN EXPLORE
				temp = getCell((int) down.getX(), (int) down.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the south cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
				
			}
			if (addRight) { // THE CELL TO OUR EAST IS A CELL WE CAN EXPLORE
				temp = getCell((int) right.getX(), (int) right.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the east cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			

		}

		// System.out.println("We've reached here for some reason.");
		// MAZE IS UNSOLVABLE ):
    	return false;
    }
    
    
    /**
     * Updates heuristics based on where the target is.
     * @param dest		Where the target is
     * @see				Agent6#updateHeur(CellInfo)
     */
    public void updateHeur(CellInfo dest) {
		
		int x = (int) dest.getPos().getX();
		int y = (int) dest.getPos().getY();
		
		for (int i = 0; i < this.cols; i++) {
			for (int j = 0; j < this.rows; j++) {
				double one = Math.abs(j - (y));
                double two = Math.abs(i - (x));
				getCell(i, j).setH(one + two);
			}
		}
		
		return;
	}
    
    
    /**
     * To use for implementing a priority queue in the planning phase
     * @param newCell		The new cell to insert
     * @param toExplore		The list of cells left to explore
     * @return				The updated list
     * @see					Agent6#insertCell(CellInfo, ArrayList)
     */
    private ArrayList<CellInfo> insertCell(CellInfo newCell, ArrayList<CellInfo> toExplore) {
		
		// FIRST CELL TO BE ADDED TO AN EMPTY LIST
		if (toExplore.isEmpty()) {
			toExplore.add(newCell);
			return toExplore;
		}
		
		double cell_f = newCell.getF();
		for (int i = 0; i < toExplore.size(); i++) { // WE WANT TO CHECK IF IT'S ALREADY IN THE LIST
			if (newCell.getPos().getX() == toExplore.get(i).getPos().getX() && newCell.getPos().getY() == toExplore.get(i).getPos().getY()) {
				if (cell_f <= toExplore.get(i).getF()) { // IF THE EXISTING F-VALUE IS HIGHER THAN WE JUST FOUND, WE WANT TO UPDATE THAT
					toExplore.remove(i);
				} else { // OTHERWISE, WE JUST RETURN THE LIST AS IS
					return toExplore;
				}
			}
		}
		
		if (toExplore.isEmpty()) {
			toExplore.add(newCell);
			return toExplore;
		}
		
		// IF OUR CELL HAS A BETTER F-VALUE OR THE CELL ISN'T IN THE LIST ALREADY, THEN WE ADD IT HERE
		for (int i = 0; i < toExplore.size(); i++) {
			if (cell_f < toExplore.get(i).getF()) {
				toExplore.add(i, newCell);
				return toExplore;
			} else if (cell_f == toExplore.get(i).getF()) {
				double cell_g = newCell.getG();
				if (cell_g <= toExplore.get(i).getG()) {
					toExplore.add(i, newCell);
					return toExplore;
				}
			}
		}
		
		// THE CELL WE FOUND HAS THE HIGHEST F-VALUE OF ANY WE FOUND SO FAR
		toExplore.add(toExplore.size() - 1, newCell); // ADDING IT TO THE END OF THE LIST
		return toExplore;
		
	}
    
    
    
    
    /**
     * Indicates whether a cell is already in the list of cells that have been expanded
     * @param newCell		The cell in question
     * @param doneWith		The list of cells that have already been expanded
     * @return				True if the list already contains the cell, false if not
     */
	private boolean contains(CellInfo newCell, ArrayList<CellInfo> doneWith) {
		for (int i = 0; i < doneWith.size(); i++) {
			if (newCell.getPos().getX() == doneWith.get(i).getPos().getX() 
					&& newCell.getPos().getY() == doneWith.get(i).getPos().getY()) {
				return true;
			}
		}
		return false;
	}
    
}
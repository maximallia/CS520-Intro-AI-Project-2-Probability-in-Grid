import java.util.ArrayList;
import java.util.LinkedList;
import java.awt.Point;

/**
 * 
 * @author Zachary Tarman
 * Handles Agent 6 responsibilities in accordance with the descriptions
 * associated with Project 3 of CS520 Fall 2021.
 *
 */
public class Agent6 {
	
	/**
	 * The actual maze object
	 */
	public Maze maze;
	/**
	 * The row dimension of the maze
	 */
	public int rows;
	/**
	 * The column dimension of the maze
	 */
	public int cols;
	/**
	 * The probability held by the cell with the highest probability in the maze
	 */
	public double highestProb;
	/**
	 * The cell in which the highest probability is held
	 */
	public CellInfo cellOfHighestProb;

	/**
	 * The number of blocks the agent physically hits
	 */
	public int collisions = 0;
	/**
	 * The number of cells that we examine (assessing if we can find the target in the given cell)
	 */
    public int examinations = 0;
    /**
     * The trajectory length of the agent (includes collisions within this metric)
     */
	public int trajectoryLength = 0;
	/**
	 * The runtime of the program to find a path to the goal
	 */
    public long runtime = 0; // THE RUNTIME OF THE PROGRAM TO FIND A PATH TO THE GOAL
        
    
    /**
     * Prints the stats that might be useful in data collection for Project 3.
     * Cost is total effort exercised by the agent.
     * @see			Agent6#trajectoryLength
     * @see			Agent6#examinations
     */
    public void printStats() {
    	System.out.println("Statistics for Maze Solution");
    	System.out.println("Trajectory Length: " + trajectoryLength);
    	System.out.println("Collisions: " + collisions);
    	System.out.println("Examinations: " + examinations);
    	int cost = trajectoryLength + examinations;
    	System.out.println("Total agent cost: " + cost);
    	System.out.println("Runtime: " + runtime);
    	System.out.println();
    	return;
    }




	/**
	 * This method plans a route from the agent's current position to the given destination.
	 * Think of it as a single iteration of A* without the agent physically moving.
	 * It uses the knowledge it has of the explored gridworld and otherwise uses the freespace assumption.
	 * @param start			The start cell
	 * @param dest			The destination cell
	 * @return				The planned path from start to finish
	 */
	public LinkedList<CellInfo> plan(CellInfo start, CellInfo dest) {

		LinkedList<CellInfo> plannedPath = new LinkedList<CellInfo>(); // TO STORE THE NEW PLANNED PATH
		ArrayList<CellInfo> toExplore = new ArrayList<CellInfo>(); // TO STORE THE CELLS TO BE EXPLORED
		ArrayList<CellInfo> doneWith = new ArrayList<CellInfo>(); // CELLS THAT HAVE ALREADY BEEN "EXPANDED"
		
		// System.out.println("Starting at " + start.getPos().getX() + "," + start.getPos().getY());
		// System.out.println("Destination at " + dest.getPos().getX() + "," + dest.getPos().getY());
		
		CellInfo curr = start; // PTR TO THE CURRENT CELL WE'RE EVALUATING TO MOVE ON FROM IN OUR PLAN
		curr.setG(0); // SINCE THIS IS THE NEW STARTING POINT, WE SET THE G-VALUE TO 0
		
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
		toExplore.add(first); // AND THIS IS THE FIRST CELL WE'RE GOING TO "EXPAND"

		// BEGIN LOOP UNTIL PLANNING REACHES DESTINATION CELL
		while (toExplore.size() > 0) {

			curr = toExplore.remove(0); // CURRENT CELL THAT WE'RE LOOKING AT
			
			if (contains(curr, doneWith)) { // WE DON'T WANT TO EXPAND THE SAME CELL AGAIN (THIS IS PROBABLY REDUNDANT, BUT HERE JUST IN CASE)
				// System.out.println("We've already seen this cell and its directions: " + curr.getPos().toString());
				continue;
			}

			// DEBUGGING STATEMENT
			// System.out.println("We're currently figuring out where to plan to go to next from " + curr.getPos().toString());
			
			curr_position = curr.getPos(); // COORDINATE OF THE CELL WE'RE CURRENTLY EXPLORING
			x = (int) curr_position.getX(); // X COORDINATE
			y = (int) curr_position.getY(); // Y COORDINATE
			doneWith.add(curr); // WE DON'T WANT TO EXPAND / LOOK AT THIS CELL AGAIN IN THIS PLANNING PHASE
						
			// IS THIS CELL THE DESTINATION??
			// IF SO, LET'S TRACE BACK TO OUR STARTING POSITION
			if (x == (int)dest.getPos().getX() && y == (int)dest.getPos().getY()) {
				CellInfo goal = maze.getCell(x, y);
				CellInfo ptr = goal;
				
				// LOOP BACK THROUGH, FOLLOWING THE PARENT CHAIN BACK TO THE START
				while (ptr.getPos().getX() != first.getPos().getX() || ptr.getPos().getY() != first.getPos().getY()) {
					// DEBUGGING STATEMENT
					// System.out.print("(" + ptr.getPos().getX() + "," + ptr.getPos().getY() + "), ");
					plannedPath.addFirst(ptr);
					ptr = ptr.getParent();
				}
				plannedPath.addFirst(ptr); // ADDING START CELL TO THE PATH
				return plannedPath;
			}

			// IF WE DIDN'T REACH THE DESTINATION, WE HAVE TO CHECK FOR VIABLE NEIGHBORS TO CONSIDER
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
			// THE CHECKS ESSENTIALLY CONSIST OF THE FOLLOWING
				// IS THE CELL OUT OF BOUNDS? IF SO, DON'T ADD
				// IF NOT, HAVE WE VISITED THE CELL, AND IF WE HAVE, IS IT BLOCKED? IF BOTH ARE TRUE, DON'T ADD
					// (THE AGENT ONLY KNOWS IT'S BLOCKED IF IT'S VISITED IT ALREADY)
				// IF WE'VE ALREADY ASSESSED THIS CELL WITHIN OUR PLANNING, THEN DON'T ADD IT TO THE LIST OF THINGS TO EXPLORE
			
			// CHECKS FOR NORTHBOUND NEIGHBOR
			if (!inBounds(up)) { 
				addUp = false;
				// System.out.println("North is not in bounds.");
			} else if (maze.getCell((int) up.getX(), (int) up.getY()).isVisited() &&
					maze.getCell((int)(up.getX()),(int) up.getY()).getTerrain().value == 3) { // WE'VE DETERMINED THAT THIS CELL IS BLOCKED
				addUp = false;
				// System.out.println("North is blocked.");
			} else if (contains(maze.getCell((int) up.getX(), (int) up.getY()), doneWith)) {
				addUp = false;
			}
			
			// CHECKS FOR SOUTHBOUND NEIGHBOR
			if (!inBounds(down)) { 
				addDown = false;
				// System.out.println("South is not in bounds.");
			} else if (maze.getCell((int) down.getX(), (int) down.getY()).isVisited() &&
					maze.getCell((int)(down.getX()),(int) down.getY()).getTerrain().value == 3) { // WE'VE DETERMINED THAT THIS CELL IS BLOCKED
				addDown = false;
				// System.out.println("South is blocked.");
			} else if (contains(maze.getCell((int) down.getX(), (int) down.getY()), doneWith)) {
				addDown = false;
			}
			
			// CHECKS FOR WESTBOUND NEIGHBOR
			if (!inBounds(left)) { 
				addLeft = false;
				// System.out.println("West is not in bounds.");
			} else if (maze.getCell((int) left.getX(), (int) left.getY()).isVisited() &&
					maze.getCell((int)(left.getX()),(int) left.getY()).getTerrain().value == 3) { // WE'VE DETERMINED THAT THIS CELL IS BLOCKED
				addLeft = false;
				// System.out.println("West is blocked.");
			} else if (contains(maze.getCell((int) left.getX(), (int) left.getY()), doneWith)) {
				addLeft = false;
			}
			
			// CHECKS FOR EASTBOUND NEIGHBOR
			if (!inBounds(right)) { 
				addRight = false;
				// System.out.println("East is not in bounds.");
			} else if (maze.getCell((int) right.getX(), (int) right.getY()).isVisited() &&
					maze.getCell((int)(right.getX()),(int) right.getY()).getTerrain().value == 3) { // WE'VE DETERMINED THAT THIS CELL IS BLOCKED
				addRight = false;
				// System.out.println("East is blocked.");
			} else if (contains(maze.getCell((int) right.getX(), (int) right.getY()), doneWith)) {
				addRight = false;
			}

			
			// ADD ALL UNVISITED, UNBLOCKED AND NOT-LOOKED-AT-ALREADY CELLS TO PRIORITY QUEUE + SET PARENTS AND G-VALUES
			CellInfo temp;
			double curr_g = curr.getG(); // THE G_VALUE OF THE CURRENT CELL IN THE PLANNING PROCESS
			if (addUp) { // THE CELL TO OUR NORTH IS A CELL WE CAN EXPLORE
				temp = maze.getCell((int) up.getX(), (int) up.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the north cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			if (addLeft) { // THE CELL TO OUR WEST IS A CELL WE CAN EXPLORE
				temp = maze.getCell((int) left.getX(), (int) left.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the west cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			if (addDown) { // THE CELL TO OUR SOUTH IS A CELL WE CAN EXPLORE
				temp = maze.getCell((int) down.getX(), (int) down.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the south cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
				
			}
			if (addRight) { // THE CELL TO OUR EAST IS A CELL WE CAN EXPLORE
				temp = maze.getCell((int) right.getX(), (int) right.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the east cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			
			/* System.out.print("Cells to be explored: ");
			for (CellInfo ptr: toExplore) {
				System.out.print(ptr.getPos().toString() + "; ");
			}
			System.out.println(); */

		}

		return null; // TARGET IS UNREACHABLE ):
		// LOOK TO RUN METHOD TO SEE WHAT IS DONE WHEN THE PLANNED DESTINATION IS UNREACHABLE
	}

	
	/**
	 * Used to examine a given cell to see if we can find the target. Updates belief state appropriately.
	 * Based on the cell's terrain type, we may not actually see the target even if the agent is standing on it.
	 * @param pos			The cell to be examined
	 * @return				Whether the target has been found
	 * @see					Maze#isTarget(Point)
	 * @see					Agent6#updateRemainingBeliefState(CellInfo, CellInfo, double, double, boolean)
	 */
	public boolean examine(CellInfo pos) {
		
		// System.out.println("Agent currently examining " + pos.getPos().getX() + ", " + pos.getPos().getY());
		
		this.examinations++;
		
		int terrainType = pos.getTerrain().value; // THIS WILL SIGNAL TO US WHAT TERRAIN THIS CELL IS
			// TERRAIN DETERMINES HOW LIKELY WE WILL BE TO SENSE THE TARGET
		
		double oldProb = pos.getProb(); // CONTAINS THE OLD PROBABILITY OF THE CELL WE'RE EXAMINING
		double newProb; // WILL CONTAIN THE NEW PROBABILITY OF FINDING THE TARGET IN THIS CELL (IF NEEDED)
		double rand = Math.random(); // A RANDOMLY GENERATED VALUE THAT WILL DETERMINE IF WE FOUND THE TARGET OR NOT
		// System.out.println("This cell's current probability of containing the target is " + oldProb);		
		
		/*
		 * ALL OF THE EQUATIONS IN THE INNER ELSE CLAUSES WERE DERIVED USING
		 * BAYES' THEOREM FOR THE PROBABILITY OF FINDING THE TARGET IN THE CURRENT CELL
		 * GIVEN A FAILED EXAMINATION IN SOME TYPE OF TERRAIN (THE IF STATEMENTS TAKE
		 * CARE OF THE DIFFERENT SCENARIOS WE MIGHT ENCOUNTER)
		 * 			SEE THE REPORT WRITE-UP TO SEE HOW WE DERIVED THESE UPDATES TO THE LIKELIHOODS
		 */
		if (terrainType == 0) { // TERRAIN IS FLAT
			
			// System.out.println("We're on flat terrain.");
			if (maze.isTarget(pos.getPos()) && rand <= 0.8) {
				// WE'VE FOUND THE TARGET
				return true;
			} else {
				// CALCULATING THE NEW LIKELIHOOD THAT WE FIND THE TARGET HERE AFTER FAILED EXAMINATION
				newProb = (0.2 * oldProb) / (1 - (0.8 * oldProb));
			}
		
		} else if (terrainType == 1) { // TERRAIN IS HILLY
			
			// System.out.println("We're on hilly terrain.");
			if (maze.isTarget(pos.getPos()) && rand <= 0.5) {
				// WE'VE FOUND THE TARGET
				return true;
			} else {
				// CALCULATING THE NEW LIKELIHOOD THAT WE FIND THE TARGET HERE AFTER FAILED EXAMINATION
				newProb = (0.5 * oldProb) / (1 - (0.5 * oldProb));
			}
		
		} else { // TERRAIN IS FOREST-Y
			
			// System.out.println("We're on forest terrain.");
			if (maze.isTarget(pos.getPos()) && rand <= 0.2) {
				// WE'VE FOUND THE TARGET
				return true;
			} else {
				// CALCULATING THE NEW LIKELIHOOD THAT WE FIND THE TARGET HERE AFTER FAILED EXAMINATION
				newProb = (0.8 * oldProb) / (1 - (0.2 * oldProb));
			}
		}
		
		// UPDATE THE BELIEF SYSTEM
		pos.updateProb(newProb);
		highestProb = newProb;
		// System.out.println("The updated probability of this cell is " + newProb);
		// System.out.println("The currX and currY variables equal " + currX + " & " + currY);
		updateRemainingBeliefState(pos, null, oldProb, 0, true); // SEE BELOW HELPER METHOD
		
		return false; // WE DIDN'T FIND THE TARGET
	}



	
	/**
	 * Used to update the remainder of the belief state given some event.
	 * An examination, a collision, or an unreachable cell can trigger this.
	 * A collision means that the blocked cell is unreachable, so the collision cell would be entered into the unreachable parameter.
	 * 
	 * @param pos					The cell of the current position of the agent
	 * @param unreachable			The unreachable cell, if this is not an examination
	 * @param oldProb				The belief in the examined / unreachable cell containing the target at time t (the non-updated belief)
	 * @param currManhattan			The current manhattan distance from the current cell to the (now former) cell with the highest probability
	 * @param examination			Signals whether we're looking at an examination or an unreachable situation
	 */
	public void updateRemainingBeliefState(CellInfo pos, CellInfo unreachable, double oldProb, double currManhattan, boolean examination) {
		
		int terrainType; // CONTAINS THE TERRAIN TYPE OF THE CELL WITH THE UPDATED PROBABILITY
		Point updatePosition; // THE POSITION OF THE CELL WITH THE UPDATED PROBABILITY
		if (examination) {
			terrainType = pos.getTerrain().value; // THE TERRAIN OF THE CURRENT CELL (WHICH WE JUST EXAMINED)
			updatePosition = pos.getPos();
		} else {
			terrainType = 3; // NOT NECESSARILY APPLICABLE, BUT THE CELL IS "VIRUTALLY" BLOCKED SINCE WE CAN'T OCCUPY IT
			updatePosition = unreachable.getPos();
		}
		
		// LOOP THROUGH ALL CELLS TO UPDATE ALL OF THEIR PROBABILITIES
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				
				// System.out.println("i and j equal " + i + " & " + j);
				if (i == (int) updatePosition.getX() && j == (int) updatePosition.getY()) { // WE'VE ALREADY UPDATED THIS CELL ABOVE
					continue;
				}
				
				CellInfo temp = maze.getCell(i, j);
				double multiplier; // WILL CONTAIN THE APPLICABLE MULTIPLIER BASED ON THE SITUATION
				
				if (examination && terrainType == 0) { // THE TERRAIN OF THE FAILED EXAMINATION WAS FLAT
					multiplier = 0.8;
				} else if (examination && terrainType == 1) { // THE TERRAIN OF THE FAILED EXAMINATION WAS HILLY
					multiplier = 0.5;
				} else if (examination && terrainType == 2) { // THE TERRAIN OF THE FAILED EXAMINATION WAS FOREST-Y
					multiplier = 0.2;
				} else { // THE TERRAIN OF THE CELL IS EITHER BLOCKED OR THE CELL WAS UNREACHABLE
					multiplier = 1;
				}
				
				/* DERIVED FROM BAYES' THEOREM LOOKING AT PROBABILITY OF FINDING THE TARGET IN A CELL
				 * GIVEN NEW INFORMATION (WHICH IS A FAILED EXAMINATION IN CELL OF SOME TERRAIN TYPE --
				 * TERRAIN TYPE IS REFLECTED IN THE MULTIPLIER), AND THE PRIOR IS UPDATED TO THE 
				 * POSTERIOR ACCORDINGLY
				 * 				SEE REPORT WRITE-UP FOR DETAILS ON DERIVATION
				 */
				temp.updateProb(temp.getProb() / (1 - (multiplier *(oldProb))));
				// System.out.println("Cell " + temp.getPos().getX() + "," + temp.getPos().getY() + " has new prob of " + temp.getProb());
				
				if (temp.getProb() >= highestProb) {					
					// POTENTIALLY UPDATING THE CLOSEST CELL WITH THE HIGHEST PROBABILITY
						// ESSENTIALLY, THE FOLLOWING IS HANDLING TIE-BREAKERS 
						// BASED ON MANHATTAN DISTANCE FROM CURRENT POSITION
					
					// System.out.println("We've found a cell with a higher (or equivalent) probability.");
					int tempx = (int) temp.getPos().getX();
					int tempy = (int) temp.getPos().getY();
					
					double tempOne = Math.abs(pos.getPos().getX() - tempx);
					double tempTwo = Math.abs(pos.getPos().getY() - tempy);
					double tempManhattan = tempOne + tempTwo;
					
					if (temp.getProb() > highestProb || (temp.getProb() == highestProb && tempManhattan < currManhattan)) { // NEW OPTIMAL CELL TO SHOOT FOR
						// System.out.println("The current lowest manhattan distance is " + currManhattan);
						cellOfHighestProb = temp;
						currManhattan = tempManhattan;
						/* System.out.println("The new cell of highest probability is " + 
								cellOfHighestProb.getPos().getX() + "," +
								cellOfHighestProb.getPos().getY() + 
								" with probability of " + temp.getProb());
						System.out.println("The new lowest manhattan distance is " + currManhattan); */
					}
					
					highestProb = temp.getProb();
				}
			}
		}
		
		return;
		
	}
	
	
	
	/**
	 * Updates heuristics of all cells based on the new destination.
	 * Heuristic value is computed based on manhattan distance from given destination.
	 * @param dest			The new destination cell based on probability assessments
	 */
	public void updateHeur(CellInfo dest) {
		
		int x = (int) dest.getPos().getX();
		int y = (int) dest.getPos().getY();
		
		// LOOP THROUGH ALL CELLS
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				double one = Math.abs(j - (y));
                double two = Math.abs(i - (x));
				maze.getCell(i, j).setH(one + two);
				// System.out.println("The new H-value for " + i + "," + j + " is " + (one + two));
			}
		}
		
		return;
	}
	
	/**
	 * Assesses if the agent can move into the next cell in the planned path.
	 * If it's blocked, obviously the agent cannot.
	 * @param pos			The current position
	 * @param path			The planned path from the current position
	 * @return				Whether we can move into the next cell in the planned path, false if unable
	 */
	public boolean canMove(CellInfo pos, LinkedList<CellInfo> path) { // CHECK IF A CELL CAN ACTUALLY MOVE TO THE NEXT CELL OR NOT
				
		if (path.peekFirst().getTerrain().value == 3) { 
			// System.out.println("Cell " + path.peekFirst().getPos().getX() + ", " + path.peekFirst().getPos().getY() + " is blocked. We cannot move here.");
			return false; 
		}
		return true;
	}

	
	/**
	 * Assesses whether a neighbor of a cell is actually within the bounds of the maze.
	 * If either x or y is less than 0, 
	 * or if x or y are greater than or equal to the number of columns or the number of rows, respectively,
	 * then the neighbor is out of bounds.
	 * Saves the user from out-of-bounds exceptions.
	 * @param coor			The neighbor of the cell
	 * @return				Whether the cell is in bounds, false if not
	 */
	public boolean inBounds(Point coor) {
		if (coor.getX() < 0 || coor.getX() >= cols || coor.getY() < 0 || coor.getY() >= rows) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Checks if the cell of interest is already contained 
	 * within the list of cells that have been expanded already.
	 * @param newCell			The cell of interest
	 * @param doneWith			The list of already expanded cells
	 * @return					True if found, false if not
	 */
	public boolean contains(CellInfo newCell, ArrayList<CellInfo> doneWith) {
		for (int i = 0; i < doneWith.size(); i++) {
			if (newCell.getPos().getX() == doneWith.get(i).getPos().getX() 
					&& newCell.getPos().getY() == doneWith.get(i).getPos().getY()) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * Inserts a cell within the list of cells to be explored by the planning phase.
	 * The method prioritizes cells with the lowest f-values, based on the A* algorithm.
	 * @param newCell			The cell to be inserted
	 * @param toExplore			The current list of cells to be explored by the algorithm
	 * @return					The list with the cell inserted
	 */
	public ArrayList<CellInfo> insertCell(CellInfo newCell, ArrayList<CellInfo> toExplore) {
				
		/* System.out.println("Inserting cell into the priority queue: " 
		+ newCell.getPos().getX() + "," + newCell.getPos().getY() +
				"; f-value: " + newCell.getF()); */
		
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
		
		// IF WE JUST REMOVED THE ONLY CELL WITHIN THE LIST, 
		// THEN THIS MAKES SURE WE DON'T ACTUALLY CREATE AN OUT-OF-BOUNDS EXCEPTION
		if (toExplore.isEmpty()) {
			toExplore.add(newCell);
			return toExplore;
		}
		
		// IF OUR CELL HAS A BETTER F-VALUE OR THE CELL ISN'T IN THE LIST ALREADY, THEN WE ADD IT HERE
		for (int i = 0; i < toExplore.size(); i++) {
			if (cell_f < toExplore.get(i).getF()) {
				toExplore.add(i, newCell);
				return toExplore;
			} else if (cell_f == toExplore.get(i).getF()) { // TIE-BREAKER WITH G-VALUE
				double cell_g = newCell.getG();
				if (cell_g <= toExplore.get(i).getG()) {
					toExplore.add(i, newCell);
					return toExplore;
				}
			}
		}
		
		// THE CELL WE FOUND HAS THE HIGHEST F-VALUE OF ANY WE FOUND SO FAR
		toExplore.add(newCell); // ADDING IT TO THE END OF THE LIST
		return toExplore;
		
	}
	
	
	


	/**
	 * Essentially the method where it all happens. This is the driver for the agent.
	 * Main behavior involves looping through the planned path from A* towards the cell with
	 * the highest probability of containing the target.
	 * If the agent arrives at the destination of the planned path, it examines the cell for the target.
	 * If the target is not found, or we run into a block, or the replanned path is impossible,
	 * we replan again based on the updated probabilities of each cell in the maze.
	 * If the target is found, we simply return success.
	 * @param rowNum			The number of rows in the yet-to-be-built maze (provided by user)
	 * @param colNum			The number of columns in the yet-to-be-built maze (provided by user)
	 * @return					'S' for a successful trial, 'F' for a failed trial
	 * @see						Agent6#examine(CellInfo)
	 * @see						Agent6#plan(CellInfo, CellInfo)
	 * @see						Maze.java
	 * @see						CellInfo.java
	 */
	public static char run(int rowNum, int colNum) {

		Agent6 mazeRunner = new Agent6(); // INSTANCE KEEPS TRACK OF ALL OF OUR DATA AND STRUCTURES
		
		// READING FROM INPUT
		mazeRunner.rows = rowNum; // THE NUMBER OF ROWS THAT WE WANT IN THE CONSTRUCTED MAZE
		mazeRunner.cols = colNum; // THE NUMBER OF COLUMNS THAT WE WANT IN THE CONSTRUCTED MAZE

		// SET UP MAZE
		mazeRunner.maze = new Maze(mazeRunner.rows, mazeRunner.cols);
		// System.out.println(mazeRunner.maze.toString());
		if (!mazeRunner.maze.targetIsReachable()) {
			System.out.println("Initial check: Maze is unsolvable.\n");
			return 'F';
		}
		
		// System.out.println("We've successfully made a maze that is solvable.");
				
		long begin = System.nanoTime();
		CellInfo start = mazeRunner.maze.getCell((int)mazeRunner.maze.agentStart.getX(), (int)mazeRunner.maze.agentStart.getY());
		
		// WE KNOW THAT INITIALLY THE HIGHEST PROBABILITY IS SHARED BY ALL CELLS
		// WE ALSO KNOW THAT THE CLOSEST CELL TO US IS THE CELL WE'RE STARTING IN
		// TO KEEP THE IMPLEMENTATION CONSISTENT, WE'LL JUST PLAN A PATH TO WHERE WE'RE ALREADY AT
		mazeRunner.highestProb = start.getProb();
		mazeRunner.cellOfHighestProb = start;
		LinkedList<CellInfo> plannedPath = mazeRunner.plan(start, mazeRunner.cellOfHighestProb); // STORES OUR BEST PATH THROUGH THE MAZE
		// System.out.println("We've gotten through the first plan.");
		
		// MAIN LOOP FOR AGENT TO FOLLOW AFTER FIRST PLANNING PHASE
		while (true) {

			// EXTRACT THE NEXT CELL IN THE PLANNED PATH
			CellInfo currCell = plannedPath.poll();
			currCell.setVisited();
			
			double currOne;
			double currTwo;
			double currManhattan;
			
			// DEBUGGING STATEMENT
			// System.out.println("Agent is currently in " + currCell.getPos().getX() + ", " + currCell.getPos().getY());
			
			// ARE WE STANDING ON THE TARGET? IF WE'RE AT OUR DESTINATION (CELL WITH HIGH PROBABILITY), LET'S EXAMINE TO TRY TO FIND OUT
			if (currCell.getPos().getX() == mazeRunner.cellOfHighestProb.getPos().getX() 
					&& currCell.getPos().getY() == mazeRunner.cellOfHighestProb.getPos().getY()) { // WE'VE HIT THE GOAL CELL
				
				if (mazeRunner.examine(currCell)) { // IF WE RETURN TRUE, THEN WE'VE FOUND THE TARGET!
					break;
				}
				
				// OTHERWISE, WE HAVE TO REPLAN FOR WHERE TO GO NEXT AND THEN WE CONTINUE ON FROM THERE
				do {
					/* System.out.println("Our next destination after examination is " + 
							mazeRunner.cellOfHighestProb.getPos().getX() + "," +  mazeRunner.cellOfHighestProb.getPos().getY()); */
					mazeRunner.updateHeur(mazeRunner.cellOfHighestProb);
					plannedPath = mazeRunner.plan(currCell, mazeRunner.cellOfHighestProb);
					
					if (plannedPath == null) { // WE WEREN'T ABLE TO REACH THE CELL WITH THE HIGHEST PROBABILITY
						double unreachableOldProb = mazeRunner.cellOfHighestProb.getProb();
						mazeRunner.cellOfHighestProb.updateProb(0);
						mazeRunner.highestProb = 0;
						
						currOne = Math.abs(currCell.getPos().getX() - mazeRunner.cellOfHighestProb.getPos().getX());
						currTwo = Math.abs(currCell.getPos().getY() - mazeRunner.cellOfHighestProb.getPos().getY());
						currManhattan = currOne + currTwo;
						
						mazeRunner.updateRemainingBeliefState(currCell, mazeRunner.cellOfHighestProb, unreachableOldProb, currManhattan, false);
					}
				} while (plannedPath == null);
				
				/* System.out.println("The new planned path is as follows: ");
				for (CellInfo step: plannedPath) {
					System.out.print(step.getPos().getX() + "," + step.getPos().getY() + "; ");
				}
				System.out.println(); */
			}
			
			mazeRunner.trajectoryLength++; // WE'RE COUNTING COLLISIONS AS PART OF THE TRAJECTORY LENGTH NOW

			// OUR PLANNED PATH IS STILL OKAY AS FAR AS WE KNOW IF WE'RE HERE
			// ATTEMPT TO EXECUTE EXACTLY ONE CELL MOVEMENT
			if (!mazeRunner.canMove(currCell, plannedPath)) {
				// WE'VE FOUND / HIT A BLOCK
				CellInfo obstruction = plannedPath.peekFirst();
				obstruction.setVisited();
				mazeRunner.collisions++;
				
				currOne = Math.abs(currCell.getPos().getX() - mazeRunner.cellOfHighestProb.getPos().getX());
				currTwo = Math.abs(currCell.getPos().getY() - mazeRunner.cellOfHighestProb.getPos().getY());
				currManhattan = currOne + currTwo;
				
				// UPDATE KNOWLEDGE BASE NOW THAT WE'VE FOUND A BLOCKED CELL
				double obsOldProb = obstruction.getProb();
				obstruction.updateProb(0);
				
				if (obstruction.getPos().getX() == mazeRunner.cellOfHighestProb.getPos().getX() &&
						obstruction.getPos().getY() == mazeRunner.cellOfHighestProb.getPos().getY()) {
					mazeRunner.highestProb = 0;
				}
				
				mazeRunner.updateRemainingBeliefState(currCell, obstruction, obsOldProb, currManhattan, false);
				
				// DEBUGGING STATEMENT
				// System.out.println("We've hit a block at coordinate " + obstruction.getPos().toString());
								
				// AND WE NEED TO REPLAN AS WELL
				do {
					/* System.out.println("Our next planned destination is " + 
							mazeRunner.cellOfHighestProb.getPos().getX() + "," +  mazeRunner.cellOfHighestProb.getPos().getY()); */
					mazeRunner.updateHeur(mazeRunner.cellOfHighestProb);
					plannedPath = mazeRunner.plan(currCell, mazeRunner.cellOfHighestProb);
					
					if (plannedPath == null) {
						double unreachableOldProb = mazeRunner.cellOfHighestProb.getProb();
						mazeRunner.cellOfHighestProb.updateProb(0);
						mazeRunner.highestProb = 0;
						
						currOne = Math.abs(currCell.getPos().getX() - mazeRunner.cellOfHighestProb.getPos().getX());
						currTwo = Math.abs(currCell.getPos().getY() - mazeRunner.cellOfHighestProb.getPos().getY());
						currManhattan = currOne + currTwo;
						
						mazeRunner.updateRemainingBeliefState(currCell, mazeRunner.cellOfHighestProb, unreachableOldProb, currManhattan, false);
					}
				} while (plannedPath == null);
				
				/* System.out.println("The new planned path is as follows: ");
				for (CellInfo step: plannedPath) {
					System.out.print(step.getPos().getX() + "," + step.getPos().getY() + "; ");
				}
				System.out.println(); */
				
				continue;
			}			

		}
		
		// IF WE BREAK FROM THE LOOP (AKA WE'RE HERE AND HAVEN'T RETURNED YET), WE KNOW WE FOUND THE GOAL.
		long end = System.nanoTime();
		mazeRunner.runtime = end - begin;

		System.out.println("Target Found!");
		System.out.println(mazeRunner.maze.toString());
		mazeRunner.printStats();

		return 'S';
	}
	
	/**
	 * Main method. Program takes number of rows, number of columns and number of successful trials wanted as arguments.
	 * Density of blocks within the maze is fixed at 0.3
	 * @param args			Command line arguments (refer to method description)
	 * @see					Maze.java
	 */
	public static void main(String args[]) {
		
		// ROWS, COLUMNS, AND THE NUMBER OF SUCCESSFUL PATHS FOUND
			// ALL READ IN AS COMMAND LINE ARGUMENTS
		int rowNum = Integer.parseInt(args[0]);
		int colNum = Integer.parseInt(args[1]);
		int successfulTrials = Integer.parseInt(args[2]);
		
		while (successfulTrials > 0) {
			char result = run(rowNum, colNum);
			if (result == 'S') {
				successfulTrials--;
			}
		}
		
		return;
		
	}
}
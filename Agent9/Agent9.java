import java.util.ArrayList;
import java.util.LinkedList;
import java.awt.Point;

/**
 * 
 * @author Zachary Tarman
 * Handles Agent 9 responsibilities in accordance with the descriptions
 * associated with Project 3 of CS520 Fall 2021.
 *
 */
public class Agent9 {
	
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
    public long runtime = 0;
        
    
    /**
     * Prints the stats that might be useful in data collection for Project 3.
     * Cost is total effort exercised by the agent.
     * @see			Agent9#trajectoryLength
     * @see			Agent9#examinations
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
				plannedPath.addFirst(ptr);
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
	 * Handles a situation where the agent discovers a cell that is "unreachable",
	 * either by collision or through planning.
	 * @param pos				The current position of the agent
	 * @param obstruction		The position of the unreachable cell
	 * @param collision			Indicates whether this was a legitimate collision or not
	 */
    public void collision(CellInfo pos, CellInfo obstruction, boolean collision) {
    	
    	obstruction.setVisited();
    	obstruction.unreachable = true;
		if (collision) {
			collisions++;
			trajectoryLength++;
		}
    	
		double blockProb = obstruction.getCurrentProb();
		obstruction.updateCurrentProb(0);
		obstruction.updateImminentProb(0);
		
		// System.out.println("Current beliefs based on collision update:");
		
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				
				// System.out.println("i and j equal " + i + " & " + j);
				if (i == (int) obstruction.getPos().getX() && j == (int) obstruction.getPos().getY()) { // WE'VE ALREADY UPDATED THIS CELL ABOVE
					continue;
				}
				
				CellInfo temp = maze.getCell(i, j);
				
				/* DERIVED FROM BAYES' THEOREM
				 * 				SEE REPORT WRITE-UP FOR DETAILS ON DERIVATION
				 */
				temp.updateCurrentProb(temp.getCurrentProb() / (1 - blockProb));
				// System.out.println("Cell " + temp.getPos().getX() + "," + temp.getPos().getY() + " has new current prob of " + temp.getCurrentProb());
				
			}
		}
		
		updateImminentBeliefState(pos); // SEE BELOW METHOD
		
    	return;
    }
    
    
    /**
     * Examines a cell to see if the target is there (no false negatives)
     * @param pos		The current position of the agent
     * @return			Whether or not the target is there
     */
    public boolean examine(CellInfo pos) {
    	this.examinations++;		
		if (maze.isTarget(pos.getPos())) { // ASSUMING NO FALSE NEGATIVES PER ARAVIND'S NOTE IN THE DISCORD CHANNEL
			return true;
		}
		return false;
    }
    
	
    /**
     * Provides partial sensing of whether one of the 8 neighbors
     * of the agent currently contains the target.
     * @param pos		The current position
     * @return			Whether or not the target was found in the current cell
     * @see				Agent9#examine(CellInfo)
     */
	public boolean sense(CellInfo pos) {
		
		// System.out.println("Agent currently sensing " + pos.getPos().getX() + ", " + pos.getPos().getY());
		
		double posOriginalProb = 0;
		double collectiveProbability = 0; // STORES THE TOTAL PROBABILITY OF CURRENT AND NEIGHBORING CELLS
		
		if (examine(pos)) {
			return true;
		}
		posOriginalProb = pos.getCurrentProb();
		collectiveProbability = posOriginalProb;
		pos.updateCurrentProb(0);
		
		// THE TARGET ISN'T IN THE CURRENT CELL, SO LET'S LOOK AROUND US (PARTIAL SENSING)
		int x = (int) pos.getPos().getX();
		int y = (int) pos.getPos().getY();
		boolean targetSensed = false; // INDICATES WHETHER THE TARGET HAS BEEN SENSED AROUND THE AGENT OR NOT
		
		Point n = new Point(x, y - 1);
		Point nw = new Point(x - 1, y - 1);
		Point w = new Point(x - 1, y);
		Point sw = new Point(x - 1, y + 1);
		Point s = new Point(x, y + 1);
		Point se = new Point(x + 1, y + 1);
		Point e = new Point(x + 1, y);
		Point ne = new Point(x + 1, y - 1);

		ArrayList<Point> neighbors = new ArrayList<Point>();
		neighbors.add(n);
		neighbors.add(nw);
		neighbors.add(w);
		neighbors.add(sw);
		neighbors.add(s);
		neighbors.add(se);
		neighbors.add(e);
		neighbors.add(ne);
		
		for (int i = 0; i < neighbors.size(); i++) {
			if (inBounds(neighbors.get(i))) { // ONLY CHECK NEIGHBORING CELLS THAT ARE ACTUALLY IN BOUNDS (I.E. A LEGITIMATE CELL)
				if (maze.isTarget(neighbors.get(i))) {
					targetSensed = true;
					// System.out.println("Target sensed in the surrounding neighbors.");
				}
				collectiveProbability += maze.getCell((int) neighbors.get(i).getX(), (int) neighbors.get(i).getY()).getCurrentProb();
			} else {
				neighbors.set(i, null);
				// System.out.println("This neighbor wasn't in the bounds of the maze.");
			}
		}
		
		// System.out.println("Current beliefs based on examination and sensing:");
		
		// UPDATING THE BELIEF SYSTEM FOR THE CURRENT STATE OF THE GRID-WORLD
			// STARTING WITH THE NEIGHBORING CELLS
		if (targetSensed) { // WE KNOW THAT THE TARGET IS ONE OF THE NEIGHBORS, AND THEY SHOULD BE WEIGHTED ACCORDINGLY
				collectiveProbability -= posOriginalProb;
			for (Point neighbor: neighbors) {
				if (neighbor != null) {
					
					CellInfo temp = maze.getCell((int) neighbor.getX(), (int) neighbor.getY());
					/* DERIVED FROM BAYES' THEOREM
					 * 				SEE REPORT WRITE-UP FOR DETAILS ON DERIVATION
					 */
					temp.updateCurrentProb(temp.getCurrentProb() / (collectiveProbability));
					// System.out.println("Cell " + temp.getPos().getX() + "," + temp.getPos().getY() + " has new current prob of " + temp.getCurrentProb());
				}
			}
		} else { // THE TARGET IS DEFINITIELY NOT IN ONE OF THE NEIGHBORS AND SO THEY SHOULD GO TO 0
			for (Point neighbor: neighbors) {
				if (neighbor != null) {
					CellInfo temp = maze.getCell((int) neighbor.getX(), (int) neighbor.getY());
					temp.updateCurrentProb(0);
					// System.out.println("Cell " + temp.getPos().getX() + "," + temp.getPos().getY() + " has new current prob of " + temp.getCurrentProb());
				}
			}
		}
		
			// NOW FOR THE REST OF THE CELLS IN THE GRIDWORLD
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				
				// System.out.println("i and j equal " + i + " & " + j);
				if (i == (int) pos.getPos().getX() && j == (int) pos.getPos().getY()) { // WE'VE ALREADY UPDATED THIS CELL ABOVE
					continue;
				}
				
				boolean alreadyUpdated = false;
				
				for (Point neighbor: neighbors) {
					if (neighbor != null) {
						if (i == (int) neighbor.getX() && j == (int) neighbor.getY()) { // WE'VE ALREADY UPDATED THIS CELL AS WELL
							alreadyUpdated = true;
							break;
						}
					}
				}
				
				if (alreadyUpdated) {
					continue;
				}
				
				CellInfo temp = maze.getCell(i, j);
				if (targetSensed) { // WE KNOW THAT THIS CELL DEFINITIELY DOESN'T CONTAIN THE TARGET
					temp.updateCurrentProb(0);
				} else { // WE KNOW THAT THE NEIGHBORS OF THE CURRENT CELL DIDN'T CONTAIN THE TARGET, SO THIS CHANGES ACCORDINGLY
					/* DERIVED FROM BAYES' THEOREM
					 * 				SEE REPORT WRITE-UP FOR DETAILS ON DERIVATION
					 */
					temp.updateCurrentProb(temp.getCurrentProb() / (1 - collectiveProbability));
				}
				
				// System.out.println("Cell " + temp.getPos().getX() + "," + temp.getPos().getY() + " has new current prob of " + temp.getCurrentProb());
				
			}
		}
		
		updateImminentBeliefState(pos); // SEE BELOW HELPER METHOD
		
		return false; // WE DIDN'T FIND THE TARGET YET
	}



	
	/**
	 * Updates the beliefs of where the target will be in the next time unit.
	 * Used for deciding where to plan to go next.
	 * @param pos		The current position of the agent
	 */
	public void updateImminentBeliefState(CellInfo pos) {
		
		// System.out.println("Imminent beliefs:");
		highestProb = 0;
		
		// LOOP THROUGH ALL CELLS TO UPDATE ALL OF THEIR PROBABILITIES
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				
				CellInfo temp = maze.getCell(i, j);
				if (temp.unreachable) { // THIS CELL IS KNOWN TO BE BLOCKED, SO THE TARGET CANNOT GO HERE
					continue;
				}
				
				// THIS WILL STORE THE NEXT "IMMINENT" PROBABILITY
					// TAKES THE SUM OF EACH OF THE NEIGHBORS' "CURRENT" PROBABILITIES
					// WITH EACH TERM DIVIDED BY THE NUMBER OF VALID NEIGHBORS IT HAS
					// THIS IS BECAUSE IF THE TARGET WERE IN THAT CELL CURRENTLY,
					// IT HAS AN EQUAL PROBABILITY OF MOVING INTO ANY VALID NEIGHBOR
					// SO IT MUST BE DIVIDED UP APPROPRIATELY
				double cumulativeProb = 0;
				
				// THE AGENT AND THE TARGET CAN ONLY MOVE IN THE FOUR CARDINAL DIRECTIONS
				Point n = new Point(i, j - 1);
				Point w = new Point(i - 1, j);
				Point s = new Point(i, j + 1);
				Point e = new Point(i + 1, j);

				ArrayList<Point> neighbors = new ArrayList<Point>();
				neighbors.add(n);
				neighbors.add(w);
				neighbors.add(s);
				neighbors.add(e);
				
				for (Point point: neighbors) {
					
					if (!inBounds(point)) { // A NEIGHBOR NEEDS TO BE IN BOUNDS
						continue;
					}
					
					CellInfo temp2 = maze.getCell((int) point.getX(), (int) point.getY());
					
					int validNeighbors = 0; // COUNTS THE NUMBER OF VALID NEIGHBORS THAT THIS NEIGHBOR HAS ITSELF
					int x = (int) point.getX();
					int y = (int) point.getY();
					
					Point nneighbor = new Point(x, y - 1);
					Point wneighbor = new Point(x - 1, y);
					Point sneighbor = new Point(x, y + 1);
					Point eneighbor = new Point(x + 1, y);

					ArrayList<Point> neighborsOfNeighbor = new ArrayList<Point>();
					neighborsOfNeighbor.add(nneighbor);
					neighborsOfNeighbor.add(wneighbor);
					neighborsOfNeighbor.add(sneighbor);
					neighborsOfNeighbor.add(eneighbor);
					
					for (Point point2: neighborsOfNeighbor) {
						if (inBounds(point2)) {
							if ((!maze.getCell((int) point2.getX(), (int) point2.getY()).isVisited()) ||
									maze.getCell((int) point2.getX(), (int) point2.getY()).getTerrain().value != 3) {
								validNeighbors++;
							}
						}
					}
					
					cumulativeProb = cumulativeProb + ((temp2.getCurrentProb()) / validNeighbors);
					// System.out.println("Cell " + temp2.getPos().getX() + "," + temp2.getPos().getY() + " has " +
							// validNeighbors + " valid neighbors.");
					
				}
				
				temp.updateImminentProb(cumulativeProb);
				if (temp.getPos().getX() != pos.getPos().getX() || temp.getPos().getY() != pos.getPos().getY()) {
					if (temp.getImminentProb() > highestProb) {
						highestProb = temp.getImminentProb();
						cellOfHighestProb = temp;
						// System.out.println("The new cell of highest probability is " + temp.getPos().getX() + "," + temp.getPos().getY());
					}
				}
				// System.out.println("Cell " + temp.getPos().getX() + "," + temp.getPos().getY() + " has new imminent prob of " + temp.getImminentProb());
			}
		}
		
		return;
		
	}
	
	
	/**
	 * Refreshes the current probabilities from the given imminent probabilities.
	 * Simulates the agent's knowledge transferring to the next time unit.
	 * @param pos		The current position of the agent
	 */
	public void nextTimeUnit(CellInfo pos) {
		
		highestProb = 0;
		
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				
				CellInfo temp = maze.getCell(i, j);
				temp.updateCurrentProb(temp.getImminentProb());
				// System.out.println("Cell " + temp.getPos().getX() + "," + temp.getPos().getY() + " has new prob of " + temp.getProb());
			}
		}
		
		// System.out.println();
		// System.out.println("---Next time unit---");
		
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
	 * Used to make sure that our total probability is still summing to 1
	 * @return		Whether or not something has gone wrong with our probability calculations
	 */
	public boolean errorCheck() {
		
		double cumulativeProb = 0;
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				cumulativeProb += maze.getCell(i, j).getCurrentProb();
			}
		}
		
		System.out.println("Total probability = " + cumulativeProb);
		
		if (cumulativeProb < 0.999 || cumulativeProb > 1.001) {
			return true;
		}
		
		return false;
	}
	


	/**
	 * Essentially the method where it all happens. This is the driver for the agent.
	 * Main behavior involves looping through the planned path from A* towards the cell with
	 * the highest probability of containing the target.
	 * At each step, the agent examines and senses for the target.
	 * If the target is not found, or we run into a block, or the replanned path is impossible,
	 * we replan again based on the updated probabilities of each cell in the maze.
	 * If the agent arrives at the destination of the planned path, it replans.
	 * If the target is found, we simply return success.
	 * @param rowNum			The number of rows in the yet-to-be-built maze (provided by user)
	 * @param colNum			The number of columns in the yet-to-be-built maze (provided by user)
	 * @return					'S' for a successful trial, 'F' for a failed trial
	 * @see						Agent9#examine(CellInfo)
	 * @see						Agent9#plan(CellInfo, CellInfo)
	 * @see						Maze.java
	 * @see						CellInfo.java
	 */
	public static char run(int rowNum, int colNum) {

		Agent9 mazeRunner = new Agent9(); // INSTANCE KEEPS TRACK OF ALL OF OUR DATA AND STRUCTURES
		
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
		CellInfo start = mazeRunner.maze.getCell((int) mazeRunner.maze.agentStart.getX(), (int) mazeRunner.maze.agentStart.getY());
		CellInfo currCell = start;
		
		// WE KNOW THAT INITIALLY THE HIGHEST PROBABILITY IS SHARED BY ALL CELLS
		// WE ALSO KNOW THAT THE CLOSEST CELL TO US IS THE CELL WE'RE STARTING IN
		// TO KEEP THE IMPLEMENTATION CONSISTENT, WE'LL JUST PLAN A PATH TO WHERE WE'RE ALREADY AT
		mazeRunner.highestProb = start.getCurrentProb();
		mazeRunner.cellOfHighestProb = start;
		LinkedList<CellInfo> plannedPath = mazeRunner.plan(start, mazeRunner.cellOfHighestProb); // STORES OUR BEST PATH THROUGH THE MAZE
		
		// MAIN LOOP FOR AGENT TO FOLLOW AFTER FIRST PLANNING PHASE
		while (true) {
			
			currCell = plannedPath.poll();
			
			// DEBUGGING STATEMENT
			// System.out.println("Agent is currently in " + currCell.getPos().getX() + ", " + currCell.getPos().getY());
			
			// IF THE CELL HASN'T BEEN VISITED YET, IT COULD POTENTIALLY AFFECT THE BELIEF STATE
			if (!currCell.isVisited()) {
				currCell.setVisited();
			}
			
			if (mazeRunner.sense(currCell)) { // IF WE RETURN TRUE, THEN WE'VE FOUND THE TARGET!
				break;
			}
			
			// DO WE NEED TO REPLAN?
			if (plannedPath.isEmpty() || 
					(plannedPath.peekLast().getPos().getX() != mazeRunner.cellOfHighestProb.getPos().getX() ||
							plannedPath.peekLast().getPos().getY() != mazeRunner.cellOfHighestProb.getPos().getY())) {
				
				int stuck = 0;
				
				do {
					
					if (stuck > 10) {
						System.out.println("The agent has gotten stuck somehow. Try again.");
						return 'F';
					}
					/*System.out.println("Our next destination after examination is " + 
							mazeRunner.cellOfHighestProb.getPos().getX() + "," +  mazeRunner.cellOfHighestProb.getPos().getY() +
							" which has imminent probability of " + mazeRunner.highestProb + 
							" (Check: this is the associated probability: " + mazeRunner.cellOfHighestProb.getImminentProb());*/
					mazeRunner.updateHeur(mazeRunner.cellOfHighestProb);
					plannedPath = mazeRunner.plan(currCell, mazeRunner.cellOfHighestProb);
					
					if (plannedPath == null) { // WE WEREN'T ABLE TO REACH THE CELL WITH THE HIGHEST PROBABILITY
						mazeRunner.collision(currCell, mazeRunner.cellOfHighestProb, false);
					} else if (plannedPath.size() > 1) { // WE KNOW WE'RE NOT PLANNING TO ARRIVE WHERE WE ALREADY ARE
						plannedPath.poll();
					}
					
					stuck++;
					
					/* try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} */
					
				} while (plannedPath == null);
				
				/*System.out.println("The new planned path is as follows: ");
				for (CellInfo step: plannedPath) {
					System.out.print(step.getPos().getX() + "," + step.getPos().getY() + "; ");
				}
				System.out.println();*/
				
			}
						
			// OUR PLANNED PATH IS STILL OKAY AS FAR AS WE KNOW IF WE'RE HERE
			// ATTEMPT TO EXECUTE EXACTLY ONE CELL MOVEMENT
			while (!mazeRunner.canMove(currCell, plannedPath)) {
				// WE'VE FOUND / HIT A BLOCK
				mazeRunner.collision(currCell, plannedPath.peekFirst(), true);
							
				// DEBUGGING STATEMENT
				// System.out.println("We've hit a block at coordinate " + plannedPath.peekFirst().getPos().toString());
				int stuck = 0;					
				// AND WE NEED TO REPLAN AS WELL
				do {
					
					if (stuck > 10) {
						System.out.println("The agent has gotten stuck somehow. Try again.");
						return 'F';
					}
					/*System.out.println("Our next planned destination is " + 
							mazeRunner.cellOfHighestProb.getPos().getX() + "," +  mazeRunner.cellOfHighestProb.getPos().getY());*/
					mazeRunner.updateHeur(mazeRunner.cellOfHighestProb);
					plannedPath = mazeRunner.plan(currCell, mazeRunner.cellOfHighestProb);
								
					if (plannedPath == null) { // WE WEREN'T ABLE TO REACH THE CELL WITH THE HIGHEST PROBABILITY
						mazeRunner.collision(currCell, mazeRunner.cellOfHighestProb, false);
					} else if (plannedPath.size() > 1) { // WE KNOW WE'RE NOT PLANNING TO ARRIVE WHERE WE ALREADY ARE
						plannedPath.poll();
					}
					
					/* try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} */
					stuck++;
					
				} while (plannedPath == null);
							
				/*System.out.println("The new planned path is as follows: ");
				for (CellInfo step: plannedPath) {
					System.out.print(step.getPos().getX() + "," + step.getPos().getY() + "; ");
				}
				System.out.println();*/
				
				mazeRunner.nextTimeUnit(currCell);
				mazeRunner.maze.moveTarget();
				
			}
			
			// REFRESH FOR THE NEW ITERATION
			mazeRunner.trajectoryLength++;
			mazeRunner.nextTimeUnit(currCell);
			mazeRunner.maze.moveTarget();
			/* try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
		}
		
		mazeRunner.errorCheck();
		
		// IF WE BREAK FROM THE LOOP (AKA WE'RE HERE AND HAVEN'T RETURNED YET), WE KNOW WE FOUND THE GOAL.
		long end = System.nanoTime();
		mazeRunner.runtime = end - begin;

		System.out.println("Target Found!");
		// System.out.println(mazeRunner.maze.toString());
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
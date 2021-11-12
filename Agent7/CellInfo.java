import java.awt.Point;

/**
 * 
 * @author Zachary Tarman
 * Represents all of the information associated with a cell
 * of a maze (at least as far as needed for Agent 7).
 *
 */
public class CellInfo {

	enum Terrain {
		FLAT(0), HILLY(1), FOREST(2), BLOCKED(3);
		public int value;
		private Terrain (int value) {
			this.value = value;
		}
	}
	
	/**
	 * What's the terrain type of this particular cell?
	 * @see			Terrain
	 */
	private Terrain terr;
	/**
	 * Has this cell been visited by the agent yet? (Hitting a blocked cell counts)
	 */
	private boolean visited;
	
	/**
	 * Coordinate of the current cell within the maze
	 */
	private Point position;
	/**
	 * Parent of the current cell on a planned path
	 */
	private CellInfo parent;
	/**
	 * The number of steps it's taken to get to this cell in a given planning phase
	 */
	private double g_value;
	/**
	 * The heuristic estimate, based on the manhattan distance from a given destination
	 * @see			Agent7#updateHeur(CellInfo)
	 */
	private double h_estimate;
	/**
	 * The probability that this cell contains the target
	 */
	private double probContain;
	/**
	 * The probability of finding the target in this cell
	 */
	private double probFind;

	
	
	/**
	 * Constructor
	 * @param position			The position of the cell within the maze
	 * @param terrain			The terrain of the cell (flat, hilly, forest, or blocked); given as an int
	 * @param prob				The starting probability that this cell contains the target
	 * @see						Maze#maze_create(int, int)
	 */
	public CellInfo(Point position, int terrain, double prob) {		
		this.visited = false;
		
		if (terrain == 0) {
			this.terr = Terrain.FLAT;
		} else if (terrain == 1) {
			this.terr = Terrain.HILLY;
		} else if (terrain == 2) {
			this.terr = Terrain.FOREST;
		} else {
			this.terr = Terrain.BLOCKED;
		}
		
		this.position = position; // GIVES THE COORDINATE OF THE CELL IN THE MAZE
		this.parent = null; // WILL BE SET ONCE WE ESTABLISH A PLANNED PATH TO SOME DESTINATION
		this.g_value = 0; // DEFAULT VALUE, WILL BE CHANGED WHEN CELLS ARE SEARCHED
		this.h_estimate = 0; // WILL BE SET WITH EACH NEW DESTINATION
		
		this.probContain = prob; // SETS THE INITIAL PROBABILITY THAT THIS CELL IS THE TARGET
	}




	// GET METHODS
	/**
	 * Gives the position of the cell
	 * @return		The position of the cell
	 */
	public Point getPos() {
		return this.position;
	}
	/**
	 * Tells the caller if the cell has already been visited
	 * @return		True if visited, false if not
	 */
	public boolean isVisited() {
		return this.visited;
	}
	/**
	 * Provides the terrain of this cell (only to be used for visited cells)
	 * @return		The terrain type in the format of the Terrain enum
	 */
	public Terrain getTerrain() {
		return this.terr;
	}
	/**
	 * Gives the f-value to find which cell to explore next in the planning phase
	 * @return		The f-value of this cell
	 * @see			CellInfo#getG()
	 * @see			CellInfo#getH()
	 */
	public double getF() {
		return this.g_value + this.h_estimate;
	}
	/**
	 * Gives the g-value currently assigned to this cell
	 * @return		The g-value of this cell
	 */
	public double getG() {
		return this.g_value;
	}
	/**
	 * Gives the heuristic estimate attributed to the cell
	 * @return		The h-value of this cell
	 */
	public double getH() {
		return this.h_estimate;
	}
	/**
	 * Retrieves the parent of this cell in a planned path
	 * @return		The parent of this cell
	 */
	public CellInfo getParent() {
		return this.parent;
	}
	/**
	 * Retrieves the current belief that the cell contains the target
	 * @return		The probability that the cell contains the target
	 */
	public double getProbContain() {
		return this.probContain;
	}
	/**
	 * Retrieves the current belief of finding the target in this cell
	 * @return		The probability of finding the target in this cell
	 */
	public double getProbFind() {
		return this.probFind;
	}



	// SET METHODS
	/**
	 * Sets the cell to have been visited 
	 * (done during execution and also applies to blocked cells that the agent runs into)
	 */
	public void setVisited() { // SET THE CELL TO HAVE BEEN VISITED (DONE DURING EXECUTION PHASE)
		this.visited = true;
		return;
	}
	/**
	 * Sets the distance from the start node during the current planning of the path
	 * @param g		The new g-value for this cell during this iteration of planning
	 */
	public void setG(double g) {
		this.g_value = g;
		return;
	}
	/**
	 * Sets the heuristic estimate to the new destination
	 * @param h		The new h-value based on the new destination cell to shoot for
	 */
	public void setH(double h) {
		this.h_estimate = h;
		return;
	}
	/**
	 * Sets the parent of the cell based on the current planning of a path
	 * @param c		The parent of this cell in the planned path
	 */
	public void setParent(CellInfo c) {
		this.parent = c;
		return;
	}
	/**
	 * Updates the probability that the target is in this cell 
	 * as well as the probability of finding the target in the cell.
	 * @param p		The new probability that the target is in this cell
	 */
	public void updateProb(double p) {
		
		this.probContain = p;
		double multiplier = 0.5; // DEFAULT VALUE FOR UNVISITED CELL
		
		if (this.visited) { // THIS CELL HAS BEEN VISITED
			if (this.terr.value == 0) { // THIS CELL HAS BEEN OBSERVED TO BE FLAT
				multiplier = 0.8;
			} else if (this.terr.value == 1) { // THIS CELL HAS BEEN OBSERVED TO BE HILLY
				multiplier = 0.5;
			} else if (this.terr.value == 2) { // THIS CELL HAS BEEN OBSERVED TO BE FOREST-Y
				multiplier = 0.2;
			} else { // THIS CELL HAS BEEN OBSERVED TO BE BLOCKED (KIND OF ARBITRARY SINCE THE CONTAIN PROBABILITY WILL BE 0 ANYWAY)
				this.probFind = 0;
			}
		}
		
		this.probFind = (this.probContain * multiplier);
		
		return;
	}

}
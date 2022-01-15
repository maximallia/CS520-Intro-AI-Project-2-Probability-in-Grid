# CS520-Intro-AI-Project-3-Probability-in-GridCS520-Probability-in-Grid


We return to Gridworld, but not the Gridworld that you are used to.

As usual, the world is a dim by dim grid, where cells are blocked with probability p = 0.3. Among the unblocked cells, however, there are three kinds of terrain. Flat terrain, hilly terrain, and thick forests. An unblocked cell has an equal likelihood of being each of these three terrains.

A robot is located in the gridworld, and wants to find a hidden target. The robot can move between unblocked cells (up/down/left/right), and can examine the cell it is currently in to try to determine if the target is there. However, examination can potentially yield false negatives. If the target is in the cell being examined, the examination fails with different probabilities based on the terrain. If the terrain is flat, the false negative rate is 0.2 (these cells are relatively easy to search). If the terrain is hilly, the false negative rate is 0.5 (easy to hide in the hills, potentially). If the terrain is a thick forest, the false negative rate is 0.8 (it is very easy to miss the target). Hence if you examine a cell and fail to find the target there, it does not mean that the target isn’t there - but it does reduce the likelihood that the target is there. If however you examine the cell and find the target, you are done - there are no false positives.

The robot initially does not know what cells are blocked, or the terrain type of each cell. The robot is ‘blindfolded’ as before, so it can only tell a cell is blocked if it attempts to move into that cell and fails. When it successfully enters (or starts in) a cell, however, the robot can sense the terrain type of that cell. At every point in time, the agents you build will need to track a belief state, describing the probability of the target being in each cell - this will need to be updated as you learn more about the environment.

As in the previous projects, the robot starts knowing nothing about the cell contents or terrains, then learns about
its environment by traveling through it and taking measurements (moving and sensing/examining), updating its
probabilities as it goes.

For this project, you will need to code three agents:
•Agent 6: At any given time, the agent identifies the cell with the highest probability of containing
the target, given what the agent has observed. If there are multiple cells that have equal (maximum)
probability, ties should be broken by distance; if there are equidistant cells with equal (maximum) probability,
ties should be broken uniformly at random. The agent then plans a route to that square (Repeated A*). As
the agent moves along the planned route, it is both sensing the terrain of the cells it moves into and whether
or not the cell it is trying to move into is blocked. If a blocked cell is encountered, or the cell with the highest
probability of containing the target changes based on information collected while traveling, the agent must
re-plan a new route. Once it arrives at the intended cell, it will examine the cell, and then repeat the process
as necessary.

•Agent 7: At any time, the agent identifies the cell with the highest probability of successfully finding
the target, given what the agent has observed. If there are multiple cells that have equal (maximum)
probability, ties should be broken by distance; if there are equidistant cells with equal (maximum) probability,
ties should be broken uniformly at random. The agent then plans a route to that square (Repeated A*). As
the agent moves along the planned route, it is both sensing the terrain of the cells it moves into and whether
or not the cell it is trying to move into is blocked. If a blocked cell is encountered, or the cell with the highest
probability of finding the target changes based on information collected while traveling, the agent must re-plan
a new route. Once it arrives at the intended cell, it will examine the cell then repeat the process as necessary.

You will need to repeatedly run each agent on a variety of randomly generated boards (at constant dimension) to
estimate the number of actions (movement + examinations) each agent needs to find the target on average.

As in the previous projects, the robot starts knowing nothing about the cell contents or terrains, then learns about its environment by traveling through it and taking measurements (moving and sensing/examining), updating its probabilities as it goes.

You will need to repeatedly run each agent on a variety of randomly generated boards (at constant dimension) to estimate the number of actions (movement + examinations) each agent needs to find the target on average.

The third agent, Agent 8 needs to be an agent of your own design. It has the exact same information as Agents 6 and 7, and can move and sense in the exact same way. You may only modify how it decides where to go / what to do next. Agent 8 needs to have a better performance than Agents 6 and 7, in terms of reducing the number of needed actions.


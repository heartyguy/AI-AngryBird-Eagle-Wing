/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2015, Team DataLab Birds: 
 ** Karel Rymes, Radim Spetlik, Tomas Borovicka
 ** Team HeartyTian: Tian Jian Wang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package hearty.heuristics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ClientActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABType;
import hearty.utils.HeartyTrajectory;
import hearty.utils.LogWriter;

/**
 * This is the framework for all the heuristics from which the agent will later
 * choose. The right sequence of function calls (most of these functions have to
 * be implemented separately in every heuristic) for a given heuristic is: 1)
 * constructor 2) estimate utility 3) selectBestTrajectoryWRTAvailableBirds
 * (3.5) getUtility - decision purposes) 4) getShot 5) getTapInterval
 */
public class AbstractHeuristic {
	protected final SceneState _currentState;

	protected final Random _randomGenerator;

	protected final TrajectoryPlannerHeartyTian _tp;

	protected final ClientActionRobot _actionRobot;

	protected ArrayList<HeartyTrajectory> _possibleDLTrajectories = null;

	protected HeartyTrajectory _selectedDLTrajectory = null;

	// commented out for the competition
	protected LogWriter log;

	protected int _utility = -1;

	/**
	 * Basic constructor. It has to have all the information about the game
	 * scene, i.e. blocks, hills,pigs, birds, actionRobot, etc.
	 */

	public AbstractHeuristic(SceneState currentState, ClientActionRobot actionRobot, TrajectoryPlannerHeartyTian tp,
			LogWriter lg) {
		_currentState = currentState;

		_actionRobot = actionRobot;
		_tp = tp;

		_randomGenerator = new Random();

		log = lg;
	}

	/**
	 * Does not have to be implemented in child classes.
	 * 
	 * @return the utility of a given heuristic
	 */

	public int getUtility() {
		return _utility;
	}

	/**
	 * Does not have to be implemented in child classes.
	 * 
	 * @return the trajectory of a given target point
	 */
	public HeartyTrajectory getSelectedDLTrajectory() {
		return _selectedDLTrajectory;
	}

	/**
	 * Does not have to be implemented in child classes! Performs the choice of
	 * the best trajectory with respect to available birds and the bird on the
	 * sling. The cornerstone of this method is the first object type in
	 * trajectory. So for example, blue bird is much better for ice than yellow
	 * or red bird. So in that case, this method would try to give the blue bird
	 * an object in the way that he is the best at compared to other bird.
	 * Returns null if there are no possible targets for the trajectory.
	 * 
	 * @return the selected trajectory
	 */
	protected HeartyTrajectory selectBestTrajectoryWRTAvailableBirds() {
		if (_possibleDLTrajectories.size() == 0)
			return null;

		HeartyTrajectory selectedTrajectory = _possibleDLTrajectories.get(0);

		/*** Can't tell you everything... ;) ***/

		return selectedTrajectory;
	}

	protected List<HeartyTrajectory> selectBestNTrajectoryWRTAvailableBirds(int numTraj) {
		if (_possibleDLTrajectories.size() == 0)
			return null;
		List<HeartyTrajectory> retTrajs = new ArrayList<HeartyTrajectory>();
		for (int i = 0; i < Math.min(numTraj, _possibleDLTrajectories.size()); i++) {
			retTrajs.add(_possibleDLTrajectories.get(i));
		}

		/*** Can't tell you everything... ;) ***/

		return retTrajs;
	}

	/**
	 * Has to be implemented in child classes! Performs the calculation of the
	 * trajectory utility.
	 */
	protected int estimateUtility() {
		return 42;
	}

	/**
	 * Does not have to be implemented in child classes.
	 * 
	 * @return shot that will be passed to the server.
	 */
	public Shot getShot() {
		if (_selectedDLTrajectory == null)
			return null;

		Point refPoint = _tp.getReferencePoint(_currentState._sling);

		int dx, dy;

		Point releasePoint = _selectedDLTrajectory.releasePoint;
		Point targetPoint = _selectedDLTrajectory.targetPoint;
		ABObject targetObject = _selectedDLTrajectory.targetObject;

		log.append(getHeuristicId());

		Shot shot = null;

		// Calculate the tapping time according the bird type
		if (releasePoint != null) {
			// Calculate tapInterval in a measure of percent trajectory
			// travelled
			int tapTime = getTapInterval();

			dx = (int) releasePoint.getX() - refPoint.x;
			dy = (int) releasePoint.getY() - refPoint.y;

			shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);

			return shot;
		}

		return null;
	}

	/**
	 * Does not have to be implemented in child classes but if the heuristic
	 * works only for a few birds then it should be implemented. This function
	 * calculates when to tap for a given bird and given target object. This
	 * information is later passed to the server.
	 * 
	 * @return the time in milliseconds.
	 */
	protected int getTapInterval() {
		// some of these values need to be tuned
		int tapInterval = 0;
		int collision = 100;

		switch (_currentState._birdOnSling) {
		case RedBird:
			tapInterval = 0;
			break; // start of trajectory  Can't tell you everything
		case YellowBird:
			tapInterval = collision - 30;
			break; // 65-90% of the way  Can't tell you everything
		case WhiteBird:
			tapInterval = collision - 30;
			break; // 70-90% of the way  Can't tell you everything
		case BlackBird:
			tapInterval = 0;
			break;
		case BlueBird:
			tapInterval = collision - 30;
			break; // 65-85% of the way  Can't tell you everything
		default:
			tapInterval = 60;
		}
		if (_currentState._birdOnSling == ABType.WhiteBird) {
			if (Math.toDegrees(_selectedDLTrajectory.releaseAngle) > 45)
				tapInterval = 100;   // Can't tell you everything
			else
				tapInterval = 100;   // Can't tell you everything

			return _tp.getTapTime(_currentState._sling, _selectedDLTrajectory.releasePoint,
					new Point(_selectedDLTrajectory.targetPoint.x - 10, _selectedDLTrajectory.targetPoint.y),
					tapInterval);
		}

		int ret = _tp.getTapTime(_currentState._sling, _selectedDLTrajectory.releasePoint,
				_selectedDLTrajectory.targetPoint, tapInterval);
		if (_currentState._birdOnSling == ABType.BlackBird) {
			ret = 6000;
			// we do not tap for blackbird
		}

		return ret;
	}
	
	/**
	 * Has to be implemented in child classes. Writes to a log file information
	 * about the particular heuristic.
	 */
	public void writeToLog() {

	}

	/**
	 * Has to be implemented in child classes.
	 * 
	 * @return ID of the heuristic for log purposes.
	 */
	public int getHeuristicId() {
		return -1;
	}

	/**
	 * Does not have to be implemented in child classes. Calculates the distance
	 * between two objects on the screenshot.
	 */
	protected double distance(Point p1, Point p2) {
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
	}
}
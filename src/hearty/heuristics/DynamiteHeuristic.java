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
import java.util.Collections;

import ab.demo.other.ClientActionRobot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABType;
import hearty.utils.HeartyTrajectory;
import hearty.utils.LogWriter;
import hearty.utils.pigCountComparator;

/**
 * Dynamite strategy tries to aim at the TNT only if there is a pig nearby. We
 * count that the dynamite is even more useful when there are a lot of stone
 * objects and other TNTs within a given pixel range.
 */
public class DynamiteHeuristic extends AbstractHeuristic {
	private boolean noHighShotFound = false;
	
	// some of these values need to be tuned
	private static final int[] dynamiteUtility = {
			// Pig
			// Ice
			// Wood
			// Stone
			3700, 50, 100, 400 };

	/**
	 * Basic constructor. It has to have all the information about the game
	 * scene, i.e. blocks, hills,pigs, birds, actionRobot, etc.
	 */

	public DynamiteHeuristic(SceneState currentState, ClientActionRobot actionRobot, TrajectoryPlannerHeartyTian tp,
			LogWriter lg) {
		super(currentState, actionRobot, tp, lg);

		_utility = estimateUtility();

		if (_possibleDLTrajectories.size() == 0) {
			noHighShotFound = true;
			_utility = estimateUtility();
		}
	}

	/**
	 * Performs the calculation of the trajectory utility.
	 */

	@Override
	protected int estimateUtility() {
		_possibleDLTrajectories = new ArrayList<HeartyTrajectory>();

		// find all reachable targets and save them to _possibleDLTrajectories
		// array
		for (ABObject tmpTargetObject : _currentState._TNTs) {

			boolean flag = false;

			for (ABObject pig : _currentState._pigs) {
				if (isInTheRightDistance(pig, tmpTargetObject)) {
					flag = true;
					break;
				}
			}

			if (flag == false) {
				continue;
			}

			// get target point
			Point tmpTargetCenterPoint = tmpTargetObject.getCenter();

			estimateTrajectories(tmpTargetCenterPoint, tmpTargetObject);

			int whiteBirdCorrection = 0;

			if (_currentState._birdOnSling == ABType.WhiteBird) {
				whiteBirdCorrection = 3;
			}

			// search around the target
			for (int i = -1 - whiteBirdCorrection; i < 2 + whiteBirdCorrection; i += 2) {
				searchAroundTheTarget(i, tmpTargetCenterPoint, tmpTargetObject);
			}
		}

		if (_possibleDLTrajectories.size() == 0)
			return 0xffff0000;

		// sort available HeartyTrajectory possibilities by number of pigs in
		// the way
		Collections.sort(_possibleDLTrajectories, new pigCountComparator());

		_selectedDLTrajectory = selectBestTrajectoryWRTAvailableBirds();

		return _selectedDLTrajectory.heuristicUtility;
	}

	/**
	 ** searches around the target for a better trajectory
	 **/
	private void searchAroundTheTarget(int i, Point tmpTargetCenterPoint, ABObject tmpTargetObject) {
		int radius = _currentState._birdOnSling.getBirdRadius();

		if (_currentState._birdOnSling == ABType.WhiteBird) {
			if (i == 0) {
				return;
			}
			radius = (int) (radius * 0.9);
		}

		Point tmpTargetPoint = new Point(tmpTargetCenterPoint.x + i * radius, tmpTargetCenterPoint.y);

		estimateTrajectories(tmpTargetPoint, tmpTargetObject);

		if (_currentState._birdOnSling != ABType.WhiteBird) {
			tmpTargetPoint = new Point(tmpTargetCenterPoint.x, tmpTargetCenterPoint.y + i * radius);

			estimateTrajectories(tmpTargetPoint, tmpTargetObject);
		}

	}

	/**
	 ** decides if there is a pig in the right distance from the TNT
	 **/
	private boolean isInTheRightDistance(ABObject pig, ABObject tmpTargetObject) {
		if (tmpTargetObject.getCenterX() - 90 < pig.x && tmpTargetObject.getCenterX() + 90 > pig.x
				&& tmpTargetObject.getCenterY() - 90 < pig.y && tmpTargetObject.getCenterY() + 90 > pig.y) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Finds and calculates a trajectory and its utilities for a given target
	 * object. (dynamite)
	 */
	protected void estimateTrajectories(Point tmpTargetPoint, ABObject tmpTargetObject) {
		ArrayList<Point> pts = null;

		// estimate launch point
		if (noHighShotFound == false) {
			pts = _tp.estimateLaunchPoint(_currentState._sling, tmpTargetPoint, _currentState._hills,
					_currentState._blocks, tmpTargetObject, _currentState._birdOnSling);
		} else {
			pts = _tp.estimateLaunchPoint(_currentState._sling, tmpTargetPoint);
		}

		for (Point tmpReleasePoint : pts) {
			estimateSingleTrajectory(tmpReleasePoint, tmpTargetPoint, tmpTargetObject);
		}
	}

	/**
	 ** estimates a single trajectory for the target
	 **/
	private void estimateSingleTrajectory(Point tmpReleasePoint, Point tmpTargetPoint, ABObject tmpTargetObject) {
		// create new instance of HeartyTrajectory
		HeartyTrajectory tmpDLTrajectory = new HeartyTrajectory(_actionRobot, _tp, _currentState._sling,
				_currentState._birdOnSling, tmpReleasePoint, tmpTargetPoint, tmpTargetObject, _currentState._hills,
				_currentState._blocks, _currentState._pigs);
		int trajUtility = 0;
		// some of these values need to be tuned
		for (ABObject tmp : _currentState._blocks) {
			double dist = distance(tmpTargetPoint, new Point((int) tmp.getCenterX(), (int) tmp.getCenterY()));

			if (dist < 60 && tmp.type.id > 8) {
				trajUtility += ((60 - dist) / 100.0) * dynamiteUtility[tmp.type.id - 9];
			}
		}

		for (ABObject tmp : _currentState._pigs) {
			double dist = distance(tmpTargetPoint, new Point((int) tmp.getCenterX(), (int) tmp.getCenterY()));

			if (dist < 110 && tmp.type.id > 8) {
				trajUtility += ((110 - dist) / 100.0) * dynamiteUtility[tmp.type.id - 9];
			}
		}

		for (ABObject tmpTNT : _currentState._TNTs) {
			double dist = distance(tmpTargetPoint, new Point((int) tmpTNT.getCenterX(), (int) tmpTNT.getCenterY()));
			
			if (dist < 50) {
				trajUtility += ((50 - dist) / 100.0) * 2500;
			}
		}

		tmpDLTrajectory.heuristicUtility = trajUtility + (int) (1.2 * tmpDLTrajectory.trajectoryUtility)
				+ tmpDLTrajectory.pigsInTheWay.size() * 5000;

		// add trajectory to possible trajectories
		_possibleDLTrajectories.add(tmpDLTrajectory);
	}

	/**
	 * This function calculates when to tap for a given bird and given target
	 * object. This information is later passed to the server.
	 * 
	 * @return the time in milliseconds.
	 */
	@Override
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
	 * @return ID of the heuristic for log purposes.
	 */
	@Override
	public int getHeuristicId() {
		return 3;
	}
}
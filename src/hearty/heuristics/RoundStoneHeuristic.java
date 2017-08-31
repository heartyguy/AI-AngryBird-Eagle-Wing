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
import java.util.Comparator;
import java.util.List;

import ab.demo.other.ClientActionRobot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABShape;
import ab.vision.ABType;
import hearty.utils.HeartyTrajectory;
import hearty.utils.LogWriter;

/**
 * Round blocks strategy tries to hit a round block so that the moving block
 * kills a pig. There is also an alternative of releasing the stone from a
 * shelter which subsequently sets the killer round object in motion.
 */
public class RoundStoneHeuristic extends AbstractHeuristic {
	/**
	 * This enum stores the information of what strategy will be used in setting
	 * the stone in motion.
	 */
	enum microHeuristicType {
		None(-1), DestroySupport(0), HitCircle(1);

		public int id;

		private microHeuristicType(int id) {
			this.id = id;
		}
	}

	/**
	 * This class stores the information about a single stone and strategy to
	 * use on it.
	 */
	class microHeuristic {
		public ABObject _circle = null;
		public int _nOfPigs = 0;
		public List<ABObject> _pigsIncluded = null;
		public microHeuristicType _microHeuristicType = microHeuristicType.None;

		public microHeuristic(ABObject circle, int nOfPigs, List<ABObject> pigsIncluded, microHeuristicType mH) {
			_circle = circle;
			_nOfPigs = nOfPigs;
			_pigsIncluded = pigsIncluded;
			_microHeuristicType = mH;
		}
	}

	private ArrayList<microHeuristic> circles = null;

	/**
	 * Basic constructor. It has to have all the information about the game
	 * scene, i.e. blocks, hills,pigs, birds, actionRobot, etc.
	 */
	public RoundStoneHeuristic(SceneState currentState, ClientActionRobot actionRobot, TrajectoryPlannerHeartyTian tp,
			LogWriter lg) {
		super(currentState, actionRobot, tp, lg);

		_utility = estimateUtility();
	}

	/**
	 * Performs the calculation of the trajectory utility.
	 */
	@Override
	protected int estimateUtility() {
		// some of these values need to be tuned
		String mHType = "";

		// create list with microheuristics
		circles = new ArrayList<microHeuristic>();

		_possibleDLTrajectories = new ArrayList<HeartyTrajectory>();

		// skip for white bird
		if (_currentState._birdOnSling == ABType.WhiteBird)
			return 0xffff0000;

		// find circles and assign microHeuristics by the number of pigs
		// probably affected
		for (ABObject tmpObj : _currentState._blocks) {
			findCircles(tmpObj);
		}

		// try to find a release point
		for (int i = 0; i < circles.size(); ++i) {
			final microHeuristic mH = circles.get(i);

			// set primary target object
			ABObject tmpTargetObject = mH._circle;

			// get primary target point
			Point tmpTargetPoint = tmpTargetObject.getCenter();

			// choose target point for microheuristic destroy support
			if (mH._microHeuristicType == microHeuristicType.DestroySupport) {
				mHType = "DestroySupport";

				tmpTargetObject = tmpTargetObject.findLowestLeftWhichRecursivelyTouches(_currentState._blocks);

				tmpTargetPoint = new Point(tmpTargetObject.x, tmpTargetObject.getCenter().y);
				if (destroySupportCondition(tmpTargetObject, mH)) {
					continue;
				}
			}
			// tell the heuristic not to work when there is a sto
			else {
				mHType = "HitCircle";

				List<ABObject> barriersOnTheRight = tmpTargetObject
						.findAllDirectlyRightWhichRecursivelyTouches(_currentState._blocks);

				mHType += String.format(", barriersOnTheRight: %d", barriersOnTheRight.size());

				if (hitCircleCondition(tmpTargetObject, barriersOnTheRight)) {
					continue;
				}
			}

			// estimate launch point
			ArrayList<Point> pts = _tp.estimateLaunchPoint(_currentState._sling, tmpTargetPoint, _currentState._hills,
					_currentState._blocks, tmpTargetObject, _currentState._birdOnSling);

			for (Point tmpReleasePoint : pts) {
				// add trajectory to possible trajectories
				_possibleDLTrajectories
						.add(prepareRoundStoneTrajectory(mH, tmpTargetObject, tmpTargetPoint, tmpReleasePoint));
			}
		}

		// returns null when no circle is found
		if (_possibleDLTrajectories.size() == 0)
			return 0xffff0000;

		// sort circles by number of pigs
		Collections.sort(_possibleDLTrajectories, new Comparator<HeartyTrajectory>() {
			@Override
			public int compare(HeartyTrajectory a, HeartyTrajectory b) {
				if (b.numberOfPigsInTheWay == a.numberOfPigsInTheWay)
					return (int) a.targetObject.getCenterX() - (int) b.targetObject.getCenterX();

				return b.numberOfPigsInTheWay - a.numberOfPigsInTheWay;
			}
		});

		_selectedDLTrajectory = selectBestTrajectoryWRTAvailableBirds();

		// skip for blue bird
		if (_currentState._birdOnSling == ABType.BlueBird && _selectedDLTrajectory.targetObject.type == ABType.Stone)
			return 0xffff0000;

		return _selectedDLTrajectory.heuristicUtility;
	}

	// if tmpTargetObject is made of wood or ice, tmpTargetObject is made a
	// regular target
	private boolean destroySupportCondition(ABObject tmpTargetObject, microHeuristic mH) {
		if ((tmpTargetObject.type != ABType.Ice && tmpTargetObject.type != ABType.Wood)
				|| tmpTargetObject.equals(mH._circle)
				|| tmpTargetObject.getCenter().distance(mH._circle.getCenter()) > 80.0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean hitCircleCondition(ABObject tmpTargetObject, List<ABObject> barriersOnTheRight) {
		if (barriersOnTheRight.size() > 1
				|| (barriersOnTheRight.size() == 1 && (barriersOnTheRight.get(0).width >= tmpTargetObject.width
						|| barriersOnTheRight.get(0).height > tmpTargetObject.height))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 ** find circular blocks that are around pigs
	 **/
	private void findCircles(ABObject tmpObj) {
		// work just with circle objects
		System.out.println("in find circle object \n" + tmpObj.toString() + "  shape is " + tmpObj.shape);
		if (tmpObj.shape == ABShape.Circle) {
			List<ABObject> pigsDirectlyBelowAndBelowLeft = new ArrayList<ABObject>();
			List<ABObject> pigsBelowRight = new ArrayList<ABObject>();

			// sort stones by pigs positions
			for (ABObject pig : _currentState._pigs) {
				// pigs directly bellow circle and below left to the circle
				if (tmpObj.isDirectlyBelow(pig) || (tmpObj.isLeft(pig) && tmpObj.isBelow(pig))) {
					pigsDirectlyBelowAndBelowLeft.add(pig);
				}

				// pigs bellow right to the circle
				if (tmpObj.isRight(pig) && tmpObj.isBelow(pig)) {
					pigsBelowRight.add(pig);
				}
			}

			// add circle to the possible targets with hitCircle micro heuristic
			if (pigsBelowRight.size() > 0) {
				circles.add(new microHeuristic(tmpObj, pigsBelowRight.size(), pigsBelowRight,
						microHeuristicType.HitCircle));
			}

			// add circle to the possible targets with destroySupport micro
			// heuristic
			if (pigsDirectlyBelowAndBelowLeft.size() > 0) {
				circles.add(new microHeuristic(tmpObj, pigsDirectlyBelowAndBelowLeft.size(),
						pigsDirectlyBelowAndBelowLeft, microHeuristicType.DestroySupport));
			}
		}

	}

	/**
	 ** prepares trajectories for different possible targets
	 **/
	private HeartyTrajectory prepareRoundStoneTrajectory(microHeuristic mH, ABObject tmpTargetObject,
			Point tmpTargetPoint, Point tmpReleasePoint) {
		// create new instance of HeartyTrajectory
		HeartyTrajectory tmpDLTrajectory = new HeartyTrajectory(_actionRobot, _tp, _currentState._sling,
				_currentState._birdOnSling, tmpReleasePoint, tmpTargetPoint, tmpTargetObject, _currentState._hills,
				_currentState._blocks, _currentState._pigs);

		tmpDLTrajectory.numberOfPigsInTheWay = mH._nOfPigs;

		// compute heuristic utility
		for (ABObject tmpPig : mH._pigsIncluded) {
			/*** Can't tell you everything... ;) ***/

		}

		// if there is anything in the way, this trajectory must be forgotten!
		if (tmpDLTrajectory.blocksInTheWay.size() != 0 || tmpDLTrajectory.hillsInTheWay.size() != 0) {
			tmpDLTrajectory.heuristicUtility = 0xffff0000;

			if (tmpDLTrajectory.hillsInTheWay.size() == 0) {
				tmpDLTrajectory.hillsInTheWay.add(tmpDLTrajectory.targetObject);
			}
		}

		return tmpDLTrajectory;
	}

	/**
	 * check if there is any object from objects param somewhere the width of
	 * tmpObj from the center of tmpObj to the left and a little lower...
	 */
	public boolean objectsLiesInTheWay(ABObject tmpObj, List<ABObject> objects) {

		Point objCenterTranslatedLeft = new Point(tmpObj.getCenter());
		objCenterTranslatedLeft.translate((int) (-1.5 * tmpObj.width), 0);

		Point objCenterTranslatedLeftDown = new Point(objCenterTranslatedLeft);
		objCenterTranslatedLeftDown.translate(0, 7 * (tmpObj.width / 12));
		;

		for (ABObject obj : objects) {
			if (obj.contains(objCenterTranslatedLeft))
				return true;

			if (obj.contains(objCenterTranslatedLeftDown))
				return true;
		}

		return false;
	}

	/**
	 * Logs information about one possible round stone object.
	 * RoundCircleArea,RoundNoOfPigsBeneathCircle,RoundMicroHeuristicId
	 */
	public void logRoundObject(microHeuristic circle) {
		log.append(circle._circle.width * circle._circle.width);
		log.append(circle._nOfPigs);
		log.append(circle._microHeuristicType.id);
	}

	/**
	 * Writes to a log file information about the particular heuristic.
	 */

	@Override
	public void writeToLog() {
		// RoundObjectsCount,RoundCircleArea,RoundNoOfPigsBeneathCircle,RoundMicroHeuristicId

		log.append(circles.size());

		int i;
		for (i = 0; i < circles.size() && i < 6; ++i) {
			logRoundObject(circles.get(i));
		}
		log.fillWithBlanks(6 - i, 3);

	}

	/**
	 * @return ID of the heuristic for log purposes.
	 */
	@Override
	public int getHeuristicId() {
		return 1;
	}
}

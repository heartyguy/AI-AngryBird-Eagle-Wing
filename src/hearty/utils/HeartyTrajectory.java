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

package hearty.utils;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ab.demo.other.ClientActionRobot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.utils.ABUtilHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABType;

/**
 * HeartyTrajectory encapsulates the trajectory planning. it stores the
 * trajectory points, its utility and the objects that are in the way.
 */

public class HeartyTrajectory {
	public static final int NA = -1;
	public static final int ALOT = -1;
	
	// some of these values need to be tuned
	public static final int _birdsBlocksDamage[][] = {
			// stone_normal
			// stone_boost
			// wood_normal
			// wood_boost
			// ice_normal
			// ice_boost
			// pig
			// ice_default
			// wood_default
			// stone_default
			{ 500, NA, 1400, NA, 1200, NA, 0, 0, 0, 5500, 1200, 1400, 500 }, // RED_BIRD
			{ 450, 600, 3100, 4200, 1300, 1400, 0, 0, 0, 5400, 1400, 4200, 600 }, // YELLOW_B
			{ 100, 300, 300, 1200, 3300, ALOT, 0, 0, 0, 5400, 3300, 300, 100 }, // BLUE_B
			{ 3600, ALOT, 1450, ALOT, 2000, ALOT, 0, 0, 0, 5300, 2000, 1450, 3600 }, // BLACK_B
			{ 1500, NA, 2600, NA, 4000, NA, 0, 0, 0, 5300, 4000, 2600, 1500 } // WHITE_B
	};

	public static final int _birdsBlocksDamageTrajectory[][] = {
			{ 500, NA, 1400, NA, 1200, NA, 0, 0, 0, 5500, 1200, 500, 1400 }, // RED_BIRD
			{ 450, 600, 3100, 4200, 1300, 1400, 0, 0, 0, 5400, 1400, 600, 4200 }, // YELLOW_B
			{ 100, 300, 300, 1200, 3300, ALOT, 0, 0, 0, 5400, 100, 300, 3300 }, // BLUE_B
			{ 3600, ALOT, 1450, ALOT, 2000, ALOT, 0, 0, 0, 5300, 2000, 3600, 1450 }, // BLACK_B
			{ 1500, NA, 2600, NA, 4000, NA, 0, 0, 0, 5300, 1500, 2600, 4000 } // WHITE
			// { 500, NA,1400, NA,1200, NA,0,0,0,5500, 700, 500,1400},//
			// RED_BIRD
			// { 450, 600,3100,4200,1300,1400,0,0,0,5400,3400, 600,4200},//
			// YELLOW_B
			// { 100, 300, 300,1200,3300,ALOT,0,0,0,5400, 100,3100,3300},//
			// BLUE_B
			// {3600,ALOT,1450,ALOT,2000,ALOT,0,0,0,5300,3050,3600,1450},//
			// BLACK_B
			// {1500, NA,2600, NA,4000, NA,0,0,0,5300,1500,2900,4000} // WHITE_B
	};

	public ClientActionRobot actionRobot = null;

	public TrajectoryPlannerHeartyTian tp = null;

	public Rectangle sling = null;

	public ABType birdOnSling = null;

	public Point releasePoint = null;
	public double releaseAngle = 0.0;

	public Point targetPoint = null;
	public ABObject targetObject = null;

	public List<Point> trajectoryPoints = null;
	public int trajectoryUtility = 0;

	public List<ABObject> hillsAll = null;
	public List<ABObject> blocksAll = null;

	public List<ABObject> hillsInTheWay = null;
	public List<ABObject> blocksInTheWay = null;
	public List<Double> blocksInTheWayDistanceFromTheTarget = null;
	public List<ABObject> pigsInTheWay = null;

	public int numberOfPigsInTheWay = 0;

	public int heuristicUtility = 0;

	public boolean buildingFlag = false;

	public BufferedImage plannedScreenshot = null;
	
	public int numDestroyableBlocksOnTraj = 0;

	/**
	 * Constructor needs all the information about the scene, also the target
	 * point and target object it then calculates the trajectory and uses the
	 * trajectory points to compute the objects in the way.
	 */
	public HeartyTrajectory(ClientActionRobot tmpActionRobot, TrajectoryPlannerHeartyTian tmpTp, Rectangle tmpSling,
			ABType tmpBirdOnSling, Point tmpReleasePoint, Point tmpTargetPoint, ABObject tmpTargetObject,
			List<ABObject> tmpHills, List<ABObject> tmpBlocks, List<ABObject> tmpPigs) {
		actionRobot = tmpActionRobot;
		tp = tmpTp;

		sling = tmpSling;
		birdOnSling = tmpBirdOnSling;

		releasePoint = tmpReleasePoint;
		releaseAngle = tp.getReleaseAngle(sling, releasePoint);

		targetPoint = tmpTargetPoint;
		targetObject = tmpTargetObject;

		hillsAll = tmpHills;
		blocksAll = tmpBlocks;

		computeBlocksDistanceFromTheTarget(tmpBlocks);

		computeTrajectoryUtility(tmpHills, tmpBlocks);
		countThePigsInTheTrajectory(tmpPigs);

		if (trajectoryPoints.size() == 0) {
			trajectoryUtility = 0xfffff000;
		}
	}

	/**
	 * this function computes the trajectory utility based on the objects that
	 * are in the way.
	 */
	public void computeTrajectoryUtility(List<ABObject> tmpHills, List<ABObject> tmpBlocks) {
		if (hillsInTheWay == null)
			hillsInTheWay = estimateObjectsInTheWay(tmpHills);

		if (blocksInTheWay == null)
			blocksInTheWay = estimateObjectsInTheWay(tmpBlocks);

		if (trajectoryPoints == null)
			trajectoryPoints = computeTrajectoryPoints();

		// compute hills in the way and blocks in the way for a white bird
		if (birdOnSling == ABType.WhiteBird) {
			estimateHillsAndBlocksInTheWayForWhiteBird();
		}

		// the way is clear
		if (blocksInTheWay.size() == 0 && hillsInTheWay.size() == 0) {
			trajectoryUtility = 0;
			return;
		}

		// there is a hill in the way
		if (hillsInTheWay.size() > 0) {
			trajectoryUtility = 0xffff0000;
			return;
		}

		/*** Can't tell you everything... ;) ***/

		trajectoryUtility *= -1;

		// choosing the lower release angle
		if (Math.toDegrees(releaseAngle) > 50) {
			/*** Can't tell you everything... ;) ***/
		}
	}

	/**
	 * estimates the objects in the way specifically for the white bird.
	 */
	public void estimateHillsAndBlocksInTheWayForWhiteBird() {
		if (trajectoryPoints.size() == 0) {
			return;
		}

		List<Point> eggPoints = new ArrayList<Point>();

		Point tmpPoint = trajectoryPoints.get(trajectoryPoints.size() - 1);

		double oppositeSide = Math.abs(tmpPoint.y - targetObject.getCenter().y);
		double adjacentSide = Math.abs(tmpPoint.x - targetObject.getCenter().x);

		double alpha = Math.atan(oppositeSide / adjacentSide);

		double distanceRemaining = tmpPoint.distance(targetObject.getCenter());

		// xDirection
		int xDirection = -1;
		// tmpPoint lies under the targetObject
		if (targetObject.getCenter().x - tmpPoint.x < 0) {
			xDirection = 1;
		}

		// yDirection
		int yDirection = -1;
		// tmpPoint lies under the targetObject
		if (targetObject.getCenter().y - tmpPoint.y < 0) {
			yDirection = 1;
		}

		// draws a way to the target
		while (!ABUtilHeartyTian.pointHitsObjectWithinARange(targetObject, tmpPoint, birdOnSling.getBirdRadius())) {
			oppositeSide = Math.sin(alpha) * distanceRemaining;

			adjacentSide = Math.cos(alpha) * distanceRemaining;

			distanceRemaining -= 1.0;

			tmpPoint = new Point(targetObject.getCenter().x + (int) (Math.round(adjacentSide * xDirection)),
					targetObject.getCenter().y + (int) (Math.round(oppositeSide * yDirection)));

			eggPoints.add(tmpPoint);
		}

		// estimate objects in the way
		blocksInTheWay = ABUtilHeartyTian.estimateObjectsInTheWay(eggPoints, blocksAll, targetPoint, targetObject, 1);

		computeBlocksDistanceFromTheTarget(blocksInTheWay);

		// estimate hills in the way
		hillsInTheWay = ABUtilHeartyTian.estimateObjectsInTheWay(eggPoints, hillsAll, targetPoint, targetObject, 1);
	}

	/**
	 * @return true if there are no objects in the way, false otherwise.
	 */
	public boolean isReachableBoolean() {
		if (hillsInTheWay == null || blocksInTheWay == null || pigsInTheWay == null) {
			throw new RuntimeException("Problem with reachable boolean");

		}
		if (hillsInTheWay.size() == 0 && blocksInTheWay.size() == 0)
			return true;
		else
			return false;

	}

	/**
	 * counts the pigs that are in the trajectory excluding the target object
	 */
	public void countThePigsInTheTrajectory(List<ABObject> tmpPigs) {
		if (pigsInTheWay == null)
			pigsInTheWay = estimateObjectsInTheWay(tmpPigs);

		numberOfPigsInTheWay = pigsInTheWay.size();
	}

	/**
	 * computes the distance from the target object of the particular blocks
	 * that are in the way
	 */
	public void computeBlocksDistanceFromTheTarget(List<ABObject> tmpBlocks) {
		blocksInTheWayDistanceFromTheTarget = new ArrayList<Double>();

		if (trajectoryPoints == null)
			trajectoryPoints = computeTrajectoryPoints();

		if (blocksInTheWay == null)
			blocksInTheWay = estimateObjectsInTheWay(tmpBlocks);

		if (blocksInTheWay.size() == 0 || trajectoryPoints.size() == 0)
			return;

		for (ABObject block : blocksInTheWay) {
			// compute distance of the block from the targetPoint minus bird
			// radius, minus targetObject width
			blocksInTheWayDistanceFromTheTarget
					.add((block.trajectoryHitPoint.distance(targetObject.getCenter()) - birdOnSling.getBirdRadius()));// -
																														// Math.abs(targetObject.width)));
		}
	}

	/**
	 * returns percentage of the trajectory when the first object is it
	 */
	public int getPercentageOfTheTrajectoryWhenTheFirstObjectIsHit(List<ABObject> tmpBlocks) {
		if (trajectoryPoints == null)
			trajectoryPoints = computeTrajectoryPoints();

		if (blocksInTheWay == null)
			blocksInTheWay = estimateObjectsInTheWay(tmpBlocks);

		if (blocksInTheWay.size() == 0)
			return 100;

		return ABUtilHeartyTian.reachablePercentage(sling, blocksInTheWay, targetPoint, targetObject, releasePoint,
				birdOnSling.getBirdRadius(), trajectoryPoints);
	}

	/**
	 * estimates objects that are in the way.
	 */
	private List<ABObject> estimateObjectsInTheWay(List<ABObject> objects) {
		if (trajectoryPoints == null)
			trajectoryPoints = computeTrajectoryPoints();

		return ABUtilHeartyTian.estimateObjectsInTheWay(trajectoryPoints, objects, targetPoint, targetObject,
				birdOnSling.getBirdRadius());
	}
	
	/**
	 * estimates objects that are in the trajectory.
	 */
	public List<ABObject> estimateObjectsInTheTraj(List<ABObject> objects) {
		if (trajectoryPoints == null)
			trajectoryPoints = computeTrajectoryPoints();

		return ABUtilHeartyTian.estimateObjectsInTheTraj(trajectoryPoints, objects, targetPoint, targetObject,
				birdOnSling.getBirdRadius());
	}

	/**
	 * computes the trajectory points for a given target point
	 */
	private List<Point> computeTrajectoryPoints() {
		List<Point> tmpTrajectory = tp.predictTrajectory(sling, releasePoint, birdOnSling, targetPoint, targetObject,
				hillsAll, blocksAll);

		return tmpTrajectory;
	}

	/**
	 * saves the trajectory onto an image and writes it to disk
	 */
	public void saveTrajectory(String path) {
		HeartyGraphics dlg = new HeartyGraphics(actionRobot.doScreenShot());
		List<ABObject> allObjects = new ArrayList<ABObject>(hillsInTheWay);
		allObjects.addAll(pigsInTheWay);
		allObjects.addAll(blocksInTheWay);
		dlg.plotTrajectory(trajectoryPoints, birdOnSling, allObjects);
		dlg.writeToDisk(path);
	}

	/**
	 * logs information about the shot that will be performed.
	 */
	public void logShot(LogWriter log) throws Exception {
		// targetX,targetY,releaseAngle,targetObjectType,pigsInTheWayCount,blocksInTheWayCount,IceInTheWay,WoodInTheWay,StoneInTheWay,FirstInTheWayType,TrajectoryUtility

		log.append(targetPoint.x);
		log.append(targetPoint.y);
		log.append(releaseAngle);
		log.append(targetObject.type.id);
		if (pigsInTheWay == null || hillsInTheWay == null || blocksInTheWay == null || trajectoryUtility == 0) {
			throw new Exception("Blocks in the way have to be first computed!");
		}

		log.append(pigsInTheWay.size());
		log.append(blocksInTheWay.size());
		log.processBlocks(blocksInTheWay, 10, 13);

		if (blocksInTheWay.size() >= 1) {
			log.append(blocksInTheWay.get(0).type.id);
		} else {
			log.append(-1);
		}

		log.append(trajectoryUtility);
	}

	/**
	 * finds the closest path from an object to a target object. this is used in
	 * the round stone strategy
	 */
	public List<ABObject> findAPathWithAStar(List<ABObject> objects, ABObject startABObject, ABObject endABObject) {
		HeartyPriorityQueue<ABObject> open = new HeartyPriorityQueue<ABObject>();
		HashSet<ABObject> closed = new HashSet<ABObject>();
		HashMap<ABObject, ABObject> prev = new HashMap<ABObject, ABObject>();
		ABObject current;

		Map<ABObject, Double> dist = new HashMap<ABObject, Double>();

		open.enqueue(startABObject, 0);
		dist.put(startABObject, 0.0);

		while (!open.isEmpty()) {
			current = open.dequeue();

			// System.out.println(current);

			if (current.equals(endABObject)) {
				return buildPath(prev, startABObject, current);
			}

			for (ABObject y : current.findAllWhichTouches(objects)) {
				double newdistance = dist.get(current) + y.getCenter().distance(current.getCenter());
				double yToEndDistance = y.getCenter().distance(endABObject.getCenter());

				// if y (expanded apex) is not in closed
				if (!closed.contains(y)) {
					// distance either does not include actual key or the new
					// distance is smaller than the original...
					if (!open.contains(y) || (dist.get(y) > newdistance)) {
						dist.put(y, newdistance);
						prev.put(y, current);

						if (open.contains(y)) {
							open.updateKey(y, (newdistance + yToEndDistance));
						} else {
							open.enqueue(y, (newdistance + yToEndDistance));
						}
					}
				}
			}

			closed.add(current);

		}

		return new ArrayList<ABObject>();
	}

	/**
	 * reconstructs the path from A*
	 */
	private List<ABObject> buildPath(HashMap<ABObject, ABObject> prev, ABObject start, ABObject target) {
		List<ABObject> path = new ArrayList<ABObject>();
		path.add(target);
		ABObject current = target;

		while (current != start) {
			current = prev.get(current);
			path.add(current);
		}

		path.add(start);

		return path;
	}
}

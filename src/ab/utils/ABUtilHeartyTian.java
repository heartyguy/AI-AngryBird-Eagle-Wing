/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2015,  XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 ** Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** Team DataLab Birds: Karel Rymes, Radim Spetlik, Tomas Borovicka
 ** Team HeartyTian: Tian Jian Wang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.utils;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import ab.demo.other.Shot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.ABObject;

public class ABUtilHeartyTian {

	private static TrajectoryPlannerHeartyTian tp = new TrajectoryPlannerHeartyTian();

	/**
	 * @return the percentage of the trajectory when the first object is hit
	 */
	public static int reachablePercentage(Rectangle sling, List<ABObject> objects, Point targetPoint,
			ABObject targetObject, Point releasePoint, int range) {

		List<Point> trajectoryPoints = tp.predictTrajectory(sling, releasePoint);

		return reachablePercentage(sling, objects, targetPoint, targetObject, releasePoint, range, trajectoryPoints);
	}

	/**
	 * @return the percentage of the trajectory when the first object is hit
	 */
	public static int reachablePercentage(Rectangle sling, List<ABObject> objects, Point targetPoint,
			ABObject targetObject, Point releasePoint, int range, List<Point> trajectoryPoints) {
		int traY = tp.getYCoordinate(sling, releasePoint, targetPoint.x);

		if (Math.abs(traY - targetPoint.y) > 100)
			return 100;

		Point collision = isReachable(trajectoryPoints, sling, objects, targetPoint, targetObject, releasePoint, range);

		if (collision == null)
			return 100;

		int distance = collision.x - sling.x;
		int totDistance = targetPoint.x - sling.x;

		return (int) ((distance * 100.0) / totDistance);
	}

	/**
	 * estimates the objects that are in the trajectory
	 */
	public static List<ABObject> estimateObjectsInTheWay(Rectangle sling, List<ABObject> objects, Point targetPoint,
			ABObject targetObject, Point releasePoint, int range) {
		List<Point> trajectoryPoints = tp.predictTrajectory(sling, releasePoint);

		return estimateObjectsInTheWay(trajectoryPoints, objects, targetPoint, targetObject, range);
	}

	/**
	 * estimates the objects that are in the trajectory that are before the target
	 */
	public static List<ABObject> estimateObjectsInTheWay(List<Point> trajectoryPoints, List<ABObject> objects,
			Point targetPoint, ABObject targetObject, int range) {
		List<ABObject> foundObjects = new ArrayList<ABObject>();

		for (Point point : trajectoryPoints) {
			boolean reachedTarget = false;

			if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 200) {
				for (ABObject ab : objects) {
					if (pointHitsObjectWithinARange(ab, point, range)
							&& !pointHitsObjectWithinARange(targetObject, point, range)) {
						if (!foundObjects.contains(ab)) {
							ab.trajectoryHitPoint = point;
							foundObjects.add(ab);
						}
					} else if (pointHitsObjectWithinARange(targetObject, point, range)) {
						reachedTarget = true;
						break;
					}
				}
			}

			if (reachedTarget)
				break;
		}

		return foundObjects;
	}
	
	
	/**
	 * estimates the objects that are in the trajectory
	 */
	public static List<ABObject> estimateObjectsInTheTraj(List<Point> trajectoryPoints, List<ABObject> objects,
			Point targetPoint, ABObject targetObject, int range) {
		List<ABObject> foundObjects = new ArrayList<ABObject>();

		for (Point point : trajectoryPoints) {
			boolean reachedTarget = false;

			if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 200) {
				for (ABObject ab : objects) {
					if (pointHitsObjectWithinARange(ab, point, range)
							&& !pointHitsObjectWithinARange(targetObject, point, range)) {
						if (!foundObjects.contains(ab)) {
							ab.trajectoryHitPoint = point;
							foundObjects.add(ab);
						}
					}
				}
			}
		}

		return foundObjects;
	}

	/**
	 * @return the first point it hits, null if the trajectory is clear
	 */
	public static Point isReachable(List<Point> points, Rectangle sling, List<ABObject> objects, Point targetPoint,
			ABObject targetObject, Point releasePoint, int range) {
		for (Point point : points) {
			boolean reachedTarget = false;

			if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 200) {
				for (ABObject ab : objects) {
					if (pointHitsObjectWithinARange(ab, point, range)
							&& !pointHitsObjectWithinARange(targetObject, point, range)) {
						return point;
					} else if (pointHitsObjectWithinARange(targetObject, point, range)) {
						reachedTarget = true;
						break;
					}
				}
			}

			if (reachedTarget)
				break;
		}

		return null;
	}

	/**
	 * @return true if there is no object in the trajectory, false otherwise
	 */
	public static boolean isReachableBool(Rectangle sling, List<ABObject> objects, Point targetPoint,
			ABObject targetObject, Point releasePoint, int range) {

		int traY = tp.getYCoordinate(sling, releasePoint, targetPoint.x);

		if (Math.abs(traY - targetPoint.y) > 100)
			return false;

		List<Point> points = tp.predictTrajectory(sling, releasePoint);

		return isReachable(points, sling, objects, targetPoint, targetObject, releasePoint, range) == null ? true
				: false;

	}

	/**
	 * @return true if the target can be hit by a clear shot releasing the bird
	 *         at the specified release point, false otherwise
	 */
	public static boolean isReachableBoolByAClearShot(Rectangle sling, List<ABObject> blocks, List<ABObject> hills,
			Point targetPoint, ABObject targetObject, Point releasePoint, int range) {
		ArrayList<ABObject> allObjects = new ArrayList<>(blocks);
		allObjects.addAll(hills);

		return isReachableBool(sling, allObjects, targetPoint, targetObject, releasePoint, range);
	}

	/**
	 * @return true if a point hits object within a range (point included) - the
	 *         range is usually the bird's radius
	 */
	public static boolean pointHitsObjectWithinARange(ABObject object, Point point, int range) {
		if (object.contains(point))
			return true;

		int pX = point.x;
		int pY = point.y;

		if (range > 0 && (object.contains(new Point(pX - range, pY)) || object.contains(new Point(pX + range, pY))
				|| object.contains(new Point(pX, pY + range)) || object.contains(new Point(pX, pY - range))))
			return true;

		return false;
	}

	/**
	 * @return number of pigs that are in the trajectory
	 */
	public static int countThePigsInTheTrajectory(Rectangle sling, List<ABObject> pigs, Point targetPoint,
			ABObject targetObject, Shot shot, int range) {
		Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy());

		return countThePigsInTheTrajectory(sling, pigs, targetPoint, targetObject, releasePoint, range);
	}

	/**
	 * @return number of pigs that are in the trajectory
	 */
	public static int countThePigsInTheTrajectory(Rectangle sling, List<ABObject> pigs, Point targetPoint,
			ABObject targetObject, Point releasePoint, int range) {
		List<ABObject> pigsInTheTrajectory = estimateObjectsInTheWay(sling, pigs, targetPoint, targetObject,
				releasePoint, range);

		return pigsInTheTrajectory.size();
	}
}

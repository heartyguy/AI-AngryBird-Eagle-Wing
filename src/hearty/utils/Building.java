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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ab.demo.other.ClientActionRobot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.Rect;

/**
 * This class encorporates the building structure. All the connected components
 * are in the blocks variable.
 */
public class Building {
	public List<ABObject> blocks;
	public int height;
	public int x;
	public int y;
	public List<ABObject> pigs = new LinkedList<ABObject>();
	public List<ABObject> leftPigs = new LinkedList<ABObject>();
	public List<ABObject> rightPigs = new LinkedList<ABObject>();

	public List<Integer> distances = new LinkedList<Integer>();

	private Rectangle bounding;
	private int[] offsets = { 1, 2, 4, 6 };
	private int[] types = null;
	// some of these values need to be tuned
	public static final int PIG_VALUE = 5000;

	public static final double pyramidUtility[][] = { { 0, 0.5 }, { 0.05, 0.6 }, { 0.1, 0.6 }, { 0.2, 0.6 },
			{ 0.3, 0.60 }, { 0.4, 0.65 }, { 0.5, 0.7 }, { 0.6, 0.75 }, { 0.7, 0.8 }, { 0.8, 0.9 }, { 0.9, .95 },
			{ 1, 0.5 } };

	public static final double rectangleUtility[][] = { { 0, 0.8 }, { 0.05, 1 }, { 0.1, 0.9 }, { 0.2, 0.8 },
			{ 0.3, 0.7 }, { 0.4, 0.6 }, { 0.5, 0.5 }, { 0.6, 0.6 }, { 0.7, 0.7 }, { 0.8, 0.9 }, { 0.9, 0.9 },
			{ 1, 0.5 } };

	public static final double highUtility[][] = { { 0, 0.6 }, { 0.05, 0.95 }, { 0.1, 0.9 }, { 0.2, 0.85 },
			{ 0.3, 0.8 }, { 0.4, 0.75 }, { 0.5, 0.7 }, { 0.6, 0.6 }, { 0.7, 0.75 }, { 0.8, 0.8 }, { 0.9, 0.85 },
			{ 1, 0.5 } };

	public static final int destroyUtility[][] = {
			// ICE
			// WOOD
			// STONE
			{ 1000, 1400, 500 }, // RED_BIRD
			{ 700, 1000, 300 }, // YELLOW_BIRD
			{ 550, 200, 100 }, // BLUE_BIRD
			{ 1100, 1000, 700 }, // BLACK_BIRD
			{ 0, 0, 0 } // WHITE_BIRD
	};

	public static final int getDownUtility[][] = { { 900, 1500, 850 }, // RED_BIRD
			{ 300, 750, 1050 }, // YELLOW_BIRD
			{ 100, 500, 300 }, // BLUE_BIRD
			{ 500, 700, 900 }, // BLACK_BIRD
			{ 0, 0, 0 } // WHITE_BIRD
	};

	public static final double blocksInTheWayUtility[][] = { { .9, .8, .7 }, // RED_BIRD
			{ .95, .9, .8 }, // YELLOW_BIRD
			{ .95, .8, .6 }, // BLUE_BIRD
			{ .8, .85, .7 }, // BLACK_BIRD
			{ 0, 0, 0 } // WHITE_BIRD
	};

	/**
	 * Basic constructor for the building.
	 * 
	 * @param objs
	 *            the objects are the objects that are found in the HeartyUtils
	 *            method findBuilding!!
	 */
	public Building(List<ABObject> objs) {
		blocks = objs;
		Point left = findLeftCorner();
		x = left.x;
		y = left.y;
		height = findHeight();
		bounding = null;
	}

	/**
	 * Returns the height of the building.
	 */
	private int findHeight() {
		int max = 0;

		for (ABObject obj : blocks) {
			if (obj.y + obj.height > max) {
				max = obj.y + obj.height;
			}
		}

		return max - y;

	}

	/**
	 * Finds the top left corner of the building which is later stored in x and
	 * y
	 */
	private Point findLeftCorner() {
		Point tmp = new Point(1000, 1000);

		for (ABObject tmpObj : blocks) {
			if (tmpObj.y < tmp.y) {
				tmp.y = tmpObj.y;
				tmp.x = tmpObj.x;
			}
		}

		return tmp;
	}

	/**
	 * @return creates the bounding rectangle of the building
	 */
	public Rectangle getBoundingRect() {
		if (bounding != null) {
			return bounding;
		} else {

			int mostleft = 1000;
			int mostright = 0;

			for (ABObject obj : blocks) {
				if (obj.x < mostleft)
					mostleft = obj.x;
				if ((obj.x + obj.width) > mostright)
					mostright = obj.x + obj.width;
			}

			bounding = new Rectangle(mostleft, y, mostright - mostleft, height);

			return bounding;
		}
	}

	/**
	 * Finds the x coordinate of corresponding y coordinate on the left side of
	 * the building and returns the corresponding object
	 */
	public ABObject findXCoor(int y) {
		int leftmost = 1000;
		ABObject ret = null;

		for (ABObject tmp : blocks) {
			if (tmp.findXPosition(y) != -1 && tmp.x < leftmost) {
				ret = tmp;
				leftmost = tmp.x;
			}
		}

		if (ret == null) {
			for (ABObject tmp : blocks) {
				if (tmp.findXPosition(y, 2) != -1 && tmp.x < leftmost) {
					ret = tmp;
					leftmost = tmp.x;
				}
			}
			return ret;
		} else {
			return ret;
		}
	}

	/**
	 * This method performs the aiming at the object. All the necessary
	 * information has to be passed to it.
	 * 
	 * @return null if the target is not reachable, a new trajectory instance
	 *         otherwise
	 */
	private HeartyTrajectory isReachable(ClientActionRobot _actionRobot, TrajectoryPlannerHeartyTian _tp,
			Rectangle _sling, ABType _birdOnSling, List<ABObject> _hills, List<ABObject> _blocks, List<ABObject> _pigs,
			Point targetPoint, ABObject targetObject) {
		ArrayList<Point> pts = null;
		pts = _tp.estimateLaunchPoint(_sling, targetPoint, _hills, _blocks, targetObject, _birdOnSling);
		if (pts.size() > 0) {
			Point releasePoint = pts.get(0);
			HeartyTrajectory tmpDLTrajectory = new HeartyTrajectory(_actionRobot, _tp, _sling, _birdOnSling,
					releasePoint, targetPoint, targetObject, _hills, _blocks, _pigs);

			if (tmpDLTrajectory.isReachableBoolean()) {
				tmpDLTrajectory.buildingFlag = true;

				return tmpDLTrajectory;
			} else if (tmpDLTrajectory.blocksInTheWay.size() < 3) {
				tmpDLTrajectory.buildingFlag = false;

				return tmpDLTrajectory;
			}
		}

		return null;

	}

	/**
	 * @return all the objects on the left side of the building
	 */
	private List<ABObject> getLeftBlocks(List<ABObject> erasedBlocks, List<ABObject> allBlocks) {

		for (int i = 0; i < erasedBlocks.size(); ++i) {

			ABObject bl = erasedBlocks.get(i);

			if (bl.toString().equals("Rect")
					&& Math.abs(((Rect) bl).getpWidth() - bl.width + ((Rect) bl).getpLength() - bl.height) > 5) {
				erasedBlocks.remove(i);
				--i;
				continue;
			}

			if ((bl.width + bl.height < 25) || (bl.height > 3.8 * bl.width) || (bl.width <= 13 && bl.height <= 23)
					|| (bl.width <= 23 && !bl.isSomethingBigAbove(allBlocks))) {
				erasedBlocks.remove(i);
				--i;
				continue;
			}

			List<ABObject> supps = bl.getSupporters(blocks);

			if (supps.size() >= 2)
				continue;

			if (supps.size() == 1 && bl.width <= 53 && bl.height <= 10) {
				List<ABObject> left = bl.findAllDirectlyLeft(allBlocks);

				boolean flag = false;

				for (ABObject tmp : left) {
					if (tmp.width <= 53 && tmp.height <= 10) {
						flag = true;
						break;
					}
				}

				if (flag == true) {
					continue;
				}
			}

			erasedBlocks.remove(i);
			--i;
		}

		return erasedBlocks;
	}

	/**
	 * Finds rectangular blocks that are not straight, i.e. are slightly turned
	 * to the side.
	 */
	private List<ABObject> findNonStraightBlocks(List<ABObject> blocks) {
		List<ABObject> ret = new ArrayList<ABObject>();
		for (ABObject bl : blocks) {
			if (bl.toString().equals("Rect")
					&& Math.abs(((Rect) bl).getpWidth() - bl.width + ((Rect) bl).getpLength() - bl.height) > 5) {
				ret.add(bl);
			}

		}

		return ret;
	}

	/**
	 * Finds the blocks that straight and touch with a non straight block. These
	 * blocks are then used in the second round of block selection.
	 */
	private List<ABObject> getSpareLeftBlocks(List<ABObject> erasedBlocks) {
		List<ABObject> nonStraight = findNonStraightBlocks(erasedBlocks);

		for (int i = 0; i < erasedBlocks.size(); ++i) {
			ABObject bl = erasedBlocks.get(i);

			if (bl.toString().equals("Rect")
					&& Math.abs(((Rect) bl).getpWidth() - bl.width + ((Rect) bl).getpLength() - bl.height) > 5) {
				erasedBlocks.remove(i);
				--i;
				continue;
			}

			boolean flag = false;

			if (bl.height > bl.width * 3) {
				for (ABObject tmp : nonStraight) {
					if (bl.touches(tmp)) {
						flag = true;
						break;
					}
				}
			}

			if (flag == false) {
				erasedBlocks.remove(i);
				--i;
			}

		}

		return erasedBlocks;
	}

	/**
	 * Finds the right value in the utility arrays for a given percentage.
	 * 
	 * @return the right utility value
	 */
	private double findPercentage(double[][] points, double target) {
		for (int i = 0; i < points.length; ++i) {
			if (points[i][0] > target) {
				double total = points[i][0] - points[i - 1][0];
				double dif = points[i][1] - points[i - 1][1];
				double mine = target - points[i - 1][0];
				return points[i - 1][1] + (mine / total) * dif;
			}
		}
		return points[points.length][1];
	}

	/**
	 * Counts the utility of a given trajectory for building. It takes into
	 * consideration the following things: target object type, building type,
	 * bird type, relative position in the building.
	 * 
	 * @return the calculated utility
	 */
	private int getUtility(ABObject target, ABType onSling, boolean isDestroy, int bunkerType, HeartyTrajectory traj) {

		double position = (target.y - y) / (double) height;
		double perc = 0;
		if (bunkerType == 2) {
			/*** Can't tell you everything... ;) ***/
		} else if (bunkerType == 3) {
			/*** Can't tell you everything... ;) ***/
		} else {
			/*** Can't tell you everything... ;) ***/
		}

		if (target.height > 10 && target.height < 15
				&& ((onSling == ABType.YellowBird && target.type == ABType.Stone)
						|| (onSling == ABType.BlueBird && target.type != ABType.Ice) || (onSling == ABType.RedBird)
						|| (onSling == ABType.BlackBird))) {
			/*** Can't tell you everything... ;) ***/

		}

		int ret = 0;
		if (isDestroy == true && target.type.id > 9 && target.type.id < 13) {
			/*** Can't tell you everything... ;) ***/
		} else if (target.type.id > 9 && target.type.id < 13) {
			/*** Can't tell you everything... ;) ***/
		} else {
			/*** Can't tell you everything... ;) ***/
		}

		if (traj.buildingFlag == false) {
			for (ABObject obj : traj.blocksInTheWay) {
				if (obj.type.id > 9 && obj.type.id < 13) {

					/*** Can't tell you everything... ;) ***/
				}
			}
		}

		return ret;
	}

	/**
	 * Calculates the utility for pigs that are either in the building or that
	 * are next to the building. The utility depends on the building type, the
	 * position of the pigs and relative position of the target block.
	 */
	private int pigsInTheWay(double upPercentage, boolean directly, double downPercentage, int downPixels,
			double leftPercentage, double rightPercentage, ABObject target) {
		int ret = 0;
		ABObject targetObject = new ABObject(new Rectangle(target.x - 15, target.y, target.width + 30, target.height),
				ABType.Unknown);
		for (ABObject pig : pigs) {
			if (directly == true && targetObject.isDirectlyAbove(pig)) {
				ret += PIG_VALUE * upPercentage;
			} else if (directly == false && pig.y < targetObject.y) {
				ret += PIG_VALUE * upPercentage;
			} else if (pig.y - downPixels < targetObject.y) {
				ret += PIG_VALUE * downPercentage;
			}
		}

		for (ABObject pig : leftPigs) {
			ret += PIG_VALUE * leftPercentage;
		}

		for (ABObject pig : rightPigs) {
			ret += PIG_VALUE * rightPercentage;
		}

		return ret;

	}

	/**
	 * Picks randomly one reachable shot (from a pool of 10) for a given block.
	 * 
	 * @return the picked trajectory
	 */
	private HeartyTrajectory getShotForJoint(ClientActionRobot _actionRobot, TrajectoryPlannerHeartyTian _tp,
			Rectangle _sling, ABType _birdOnSling, List<ABObject> _hills, List<ABObject> _blocks, List<ABObject> _pigs,
			ABObject bl) {
		List<HeartyTrajectory> oneJoint = new ArrayList<HeartyTrajectory>();
		List<HeartyTrajectory> unreachableJoint = new ArrayList<HeartyTrajectory>();

		for (int offset : offsets) {
			HeartyTrajectory tmp = isReachable(_actionRobot, _tp, _sling, _birdOnSling, _hills, _blocks, _pigs,
					new Point(bl.x, bl.y + offset), bl);

			if (tmp != null && tmp.buildingFlag == true) {

				oneJoint.add(tmp);

			} else if (tmp != null) {
				unreachableJoint.add(tmp);
			}
			tmp = isReachable(_actionRobot, _tp, _sling, _birdOnSling, _hills, _blocks, _pigs,
					new Point(bl.x, bl.y + bl.height - offset), bl);

			if (tmp != null && tmp.buildingFlag == true) {
				oneJoint.add(tmp);
			} else if (tmp != null) {
				unreachableJoint.add(tmp);
			}

		}

		HeartyTrajectory tmp = pickTrajectory(oneJoint);
		if (tmp != null) {
			return tmp;
		} else if (tmp == null) {
			HeartyTrajectory unreach = pickTrajectory(unreachableJoint);

			if (unreach != null) {
				return tmp;
			}
		}

		return null;
	}

	/**
	 * Wrapper function that uses the right utility array for the right building
	 * type.
	 */
	private void utilizePoints(List<HeartyTrajectory> targets, ABType onSling) {
		for (HeartyTrajectory target : targets) {
			if (isPyramid(targets) == true) {
				/*** Can't tell you everything... ;) ***/
			} else if (getProportions() == true) {
				/*** Can't tell you everything... ;) ***/
			} else {
				/*** Can't tell you everything... ;) ***/
			}
		}
	}

	/**
	 * @return a list of trajectories for the blocks on the left side - one
	 *         trajectory per block.
	 */
	public List<HeartyTrajectory> findJoints(ClientActionRobot _actionRobot, TrajectoryPlannerHeartyTian _tp,
			Rectangle _sling, ABType _birdOnSling, List<ABObject> _hills, List<ABObject> _blocks, List<ABObject> _pigs,
			boolean spare) {
		List<HeartyTrajectory> ret = new ArrayList<HeartyTrajectory>();
		List<ABObject> erasedBlocks = new LinkedList<ABObject>(blocks);

		if (spare == false) {
			erasedBlocks = getLeftBlocks(erasedBlocks, blocks);

		} else
			erasedBlocks = getSpareLeftBlocks(erasedBlocks);

		for (ABObject bl : erasedBlocks) {
			HeartyTrajectory tmp = getShotForJoint(_actionRobot, _tp, _sling, _birdOnSling, _hills, _blocks, _pigs, bl);

			if (tmp != null) {
				ret.add(tmp);
			}

		}

		List<ABObject> leftSpareBlocks = new LinkedList<ABObject>(blocks);
		leftSpareBlocks = getSpareLeftBlocks(erasedBlocks);
		leftSpareBlocks.removeAll(erasedBlocks);

		for (ABObject bl : leftSpareBlocks) {

			HeartyTrajectory tmp = getShotForJoint(_actionRobot, _tp, _sling, _birdOnSling, _hills, _blocks, _pigs, bl);

			if (tmp != null && tmp.buildingFlag == true) {
				ret.add(tmp);
			}

		}

		utilizePoints(ret, _birdOnSling);

		Collections.sort(ret, new Comparator<HeartyTrajectory>() {
			@Override
			public int compare(HeartyTrajectory a, HeartyTrajectory b) {
				return b.trajectoryUtility - a.trajectoryUtility;
			}
		});

		return ret;

	}

	/**
	 * Randomly chooses one of a few block trajectories.
	 */

	private HeartyTrajectory pickTrajectory(List<HeartyTrajectory> joints) {
		Collections.sort(joints, new Comparator<HeartyTrajectory>() {
			@Override
			public int compare(HeartyTrajectory a, HeartyTrajectory b) {
				if (a.targetPoint.y == b.targetPoint.y) {
					return (int) (a.releaseAngle * 100 - b.releaseAngle * 100);
				} else {
					return a.targetPoint.y - b.targetPoint.y;
				}
			}
		});

		for (int i = 1; i < joints.size(); ++i) {
			Point prev = joints.get(i - 1).targetPoint;
			Point cur = joints.get(i).targetPoint;

			if ((prev.y == cur.y && cur.x == prev.x) || cur.y - 7 < y || cur.y + 6 >= y + height) {
				joints.remove(i);
				--i;
			}

		}

		if (joints.size() == 0)
			return null;

		return joints.get((int) (Math.random() * joints.size()));
	}

	/**
	 * @return true if the building is higher than wider, false if wider than
	 *         higher
	 */
	public boolean getProportions() {

		if (getBoundingRect().height - 9 > bounding.width)
			return true;
		else
			return false;

	}

	/**
	 * @return an array representing different blocks that are contained in the
	 *         building.
	 */
	public int[] getTypes() {
		if (types != null)
			return types;

		final int total = ABType.values().length;
		types = new int[total];

		for (int i = 0; i < total; ++i) {
			types[i] = 0;
		}

		for (ABObject obj : blocks) {
			types[obj.type.id]++;
		}
		return types;
	}

	/**
	 * @return true if the structure resembles pyramid, false otherwise
	 */
	public boolean isPyramid(List<HeartyTrajectory> trajs) {
		int min = 1000, max = 0;

		boolean containsFlag = false;

		for (HeartyTrajectory dlt : trajs) {
			if (dlt.targetObject.height * 4 < dlt.targetObject.width && dlt.buildingFlag == true) {
				if (containsFlag == false) {
					containsFlag = containsPigInside(dlt);
				}

				if (dlt.targetObject.x > max) {
					max = dlt.targetObject.x;
				}

				if (dlt.targetObject.x < min)
					min = dlt.targetObject.x;

			}

		}

		if (max - min >= 15 && containsFlag == true)
			return true;
		return false;

	}

	/**
	 * goes through the building and finds the dominant ABtype contained in the
	 * building and returns its id
	 * 
	 * @return the id of the dominant type
	 */
	public int getDominantType() {
		getTypes();
		int max = 0;
		int index = 0;

		for (int i = 0; i < types.length; ++i) {
			if (types[i] > max) {
				max = types[i];
				index = i;
			}

		}

		return index;
	}

	/**
	 * @return distance to the nearest object below the target object
	 */
	public int getDistanceToNearestObjectBelow(ABObject target, int position) {
		ABObject tmp = getNearestObjectBelow(target, position);
		if (tmp == null)
			return -1;
		else
			return tmp.y - target.y - target.height;

	}

	/**
	 * @return nearest object below the target object
	 */
	public ABObject getNearestObjectBelow(ABObject target, int position) {
		List<ABObject> below = target.findAllDirectlyBelow(blocks);
		if (below.size() < position)
			return null;
		ABObjectComp cmp = new ABObjectComp();
		cmp.sortBelow();
		cmp.setPosition((int) target.getCenterY());
		Collections.sort(below, cmp);

		ABObject tmp = below.get(position - 1);

		return tmp;

	}

	/**
	 * sets a contained pig
	 */
	public void setPig(ABObject pig) {
		pigs.add(pig);
		distances.add(pig.getDistanceBelowPig(blocks));
	}

	/**
	 * sets a pig on the left side
	 */
	public void setLeftPig(ABObject pig) {

		leftPigs.add(pig);
	}

	/**
	 * sets a pig on the right side
	 */
	public void setRightPig(ABObject pig) {

		rightPigs.add(pig);
	}

	/**
	 * finds the pigs that are inside the building
	 * 
	 * @return true if the building is big enough and false if not
	 **/
	public boolean findPigsInsideBuilding(List<ABObject> piggies) {
		ABObject rect = new ABObject(getBoundingRect(), ABType.Unknown);

		if (blocks.size() <= 2) {
			return false;
		}

		for (int j = 0; j < piggies.size(); ++j) {
			ABObject pig = piggies.get(j);

			if (pig.touches(rect)) {
				setPig(pig);
				piggies.remove(j);
				--j;

			}
		}

		return true;
	}

	/**
	 * decide if there are pigs inside the building this time, the search is
	 * performed only on the pigs that are really useful to the building
	 **/
	private boolean containsPigInside(HeartyTrajectory dlt) {
		for (ABObject pig : pigs) {
			if (pig.x > dlt.targetObject.x - 60 && pig.x < dlt.targetObject.x + 60 && pig.y < dlt.targetObject.y + 5) {
				return true;
			}
		}
		return false;
	}

	/**
	 ** finds pigs that are nearby a building and adds them to the building
	 * structure
	 **/
	public void findPigsNearby(List<ABObject> piggies) {
		ABObject rect = new ABObject(getBoundingRect(), ABType.Unknown);

		for (ABObject pig : piggies) {
			if (rect.getCenterX() + (0.65 * rect.height) > pig.x && rect.x < pig.x && rect.getCenterY() < pig.y) {
				setRightPig(pig);
				continue;
			}

			if (rect.getCenterX() - (0.55 * rect.height) < pig.x && rect.x > pig.x && rect.getCenterY() < pig.y) {
				setLeftPig(pig);

			}
		}

	}
}
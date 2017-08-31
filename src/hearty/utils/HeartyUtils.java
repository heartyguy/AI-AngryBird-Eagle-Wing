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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import ab.demo.other.Shot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABType;
import hearty.heuristics.AbstractHeuristic;

/* HeartyUtils ------------------------------------------------------ 
**      methods that are not used are also "stored" in here.
*/

public class HeartyUtils {
	private static int nOfScreens = 1;

	// create a trajectory planner object
	public HeartyUtils() {
	}

	/**
	 * this function clears all the blocks from helmets NOT USED IN ACTUAL
	 * RUNTIME
	 */
	public static List<ABObject> clearBlocksFromHelmets(List<ABObject> blocks, List<ABObject> pigs) {

		for (int i = 0; i < blocks.size(); ++i) {
			ABObject block = blocks.get(i);

			if (block.toString().equals("Poly") && block.type == ABType.Stone && block.width <= 24
					&& block.height <= 24) {
				for (ABObject pig : pigs) {
					if (pig.touches(block)) {
						blocks.remove(i);
						--i;
						break;
					}

				}

			}

		}

		return blocks;

	}

	/**
	 * Adds 1 px to polygon realshape for the sake of touch method
	 */
	public static Area resize(Polygon p) {
		int shift = 2;

		Polygon p1 = new Polygon(p.xpoints, p.ypoints, p.npoints);
		Polygon p2 = new Polygon(p.xpoints, p.ypoints, p.npoints);
		Polygon p3 = new Polygon(p.xpoints, p.ypoints, p.npoints);
		Polygon p4 = new Polygon(p.xpoints, p.ypoints, p.npoints);

		p1.translate(shift, 0);
		p2.translate(-shift, 0);
		p3.translate(0, shift);
		p4.translate(0, -shift);

		Area ret = new Area(p1);
		ret.add(new Area(p2));
		ret.add(new Area(p3));
		ret.add(new Area(p4));

		return ret;
	}

	/**
	 * finds a building structure from the given objects a building consists of
	 * recursively touching stones.
	 */
	public static Building findBuilding(List<ABObject> tobevisited) {
		Queue<ABObject> fronta = new ArrayDeque<ABObject>();
		List<ABObject> total = new ArrayList<ABObject>();

		fronta.add(tobevisited.get(0));
		tobevisited.remove(0);

		while (fronta.size() != 0) {
			ABObject tmp = fronta.poll();
			total.add(tmp);

			for (int i = 0; i < tobevisited.size(); ++i) {
				if (tmp.touches(tobevisited.get(i))) {
					fronta.add(tobevisited.get(i));
					tobevisited.remove(i);
					--i;
				}
			}
		}
		Building bld = new Building(total);
		return bld;

	}

	/**
	 * finds a lonely ranger that does not have any object below or above him in
	 * a given x coordinate NOT USED IN RUNTIME!
	 */
	public static int findLonelyBlock(Building bld) {
		int width = bld.getBoundingRect().width;

		Collections.sort(bld.blocks, new Comparator<ABObject>() {
			@Override
			public int compare(ABObject a, ABObject b) {

				return a.x - b.x;

			}
		});

		for (int i = bld.x + 3; i < bld.x + width; i += 10) {

			int count = 0;
			int lonelyRanger = -1;
			for (int j = 0; j < bld.blocks.size(); ++j) {
				ABObject block = bld.blocks.get(j);
				if (block.x <= i && block.x + block.width >= i && block.y > bld.y + bld.height - 20) {
					++count;
					lonelyRanger = j;
				}
			}

			if (count == 1) {
				return lonelyRanger;
			}
		}

		return -1;
	}

	/**
	 * finds the biggest building from the building given NOT USED IN RUNTIME!
	 */
	public static Building findBiggestBuilding(List<Building> blds) {
		if (blds.size() == 0)
			return null;

		Building max = blds.get(0);

		for (int i = 1; i < blds.size(); ++i) {
			Rectangle tmp = blds.get(i).getBoundingRect();
			if (tmp.width * tmp.height > max.getBoundingRect().width * max.getBoundingRect().height) {
				max = blds.get(i);
			}
		}

		return max;
	}

	/**
	 * finds multiple building structures from the given objects
	 */
	public static List<Building> findBuildings(List<ABObject> objs) {
		List<ABObject> tobevisited = new ArrayList<ABObject>(objs);
		List<Building> boundingboxes = new ArrayList<Building>();

		while (tobevisited.size() != 0)
			boundingboxes.add(findBuilding(tobevisited));

		return boundingboxes;
	}

	/**
	 * finds only the building structures from the list of objects that contain
	 * pigs NOT USED IN RUNTIME!
	 */
	public static List<Building> findBuildingsWithPigs(List<ABObject> objs, List<ABObject> pigs) {
		List<Building> blds = findBuildings(objs);
		List<Building> ret = new LinkedList<Building>();

		for (Building tmp : blds) {
			Rectangle bld = tmp.getBoundingRect();
			for (ABObject pig : pigs) {
				if (pig.x >= bld.x && pig.x <= bld.x + bld.width && pig.y >= bld.y && pig.y <= bld.y + bld.height) {
					ret.add(tmp);
					break;
				}
			}

		}

		return ret;
	}

	/**
	 * @return true if the released bird is left to all possible objects, false
	 *         otherwise
	 */
	public static boolean isBirdLeft(List<ABObject> blocks, List<ABObject> pigs, List<ABObject> birds) {
		if (birds.size() == 0)
			return true;

		int left = 1000;

		for (ABObject block : blocks) {
			if (block.x < left)
				left = block.x;
		}

		for (ABObject block : pigs) {
			if (block.x < left)
				left = block.x;
		}

		for (ABObject pig : pigs) {
			if (pig.x + pig.width - 10 > left)
				return false;
		}

		return true;

	}

	/**
	 * @return true if the released bird is right to all possible objects, false
	 *         otherwise
	 */
	public static boolean isBirdRight(List<ABObject> blocks, List<ABObject> pigs, List<ABObject> birds) {
		if (birds.size() == 0)
			return true;

		int right = 0;

		for (ABObject block : blocks) {
			if (block.x + block.width > right)
				right = block.x + block.width;
		}

		for (ABObject block : pigs) {
			if (block.x + block.width > right)
				right = block.x + block.width;
		}

		for (ABObject pig : pigs) {
			if (pig.x + 10 < right)
				return false;
		}

		return true;

	}

	/**
	 * @return a random shot, not targeting anythin.
	 */
	public static Shot findRandomShot(TrajectoryPlannerHeartyTian tp, Rectangle sling, ABType birdOnSling) {
		System.out.println("No release point found for the target");
		System.out.println("Try a random shot");
		double start = (Math.random() * .5) + Math.PI / 6;

		Point releasePoint = tp.findReleasePoint(sling, start);

		Point refPoint = tp.getReferencePoint(sling);
		double releaseAngle = tp.getReleaseAngle(sling, releasePoint);

		int tapTime = (int) (Math.random() * 1001) + 2000;

		if (birdOnSling == ABType.BlackBird) {
			tapTime = 0;
		}

		int dx = (int) releasePoint.getX() - refPoint.x;
		int dy = (int) releasePoint.getY() - refPoint.y;

		return new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
	}

	/**
	 * performs roullete selection for the metaagent based on the utilities is
	 * not used in RUNTIME
	 */
	public static AbstractHeuristic rouletteHeuristicSelection(List<AbstractHeuristic> heuristics) {
		if (heuristics == null || heuristics.size() == 0)
			return null;

		int rangeMin = 0;
		int rangeMax = 0;

		ArrayList<Integer> rouletteData = new ArrayList<Integer>();

		for (AbstractHeuristic heuristic : heuristics) {
			int currentUtility = heuristic.getUtility();

			if (currentUtility > 0) {
				rangeMax += currentUtility;

				// add utility of the heuristic
				rouletteData.add(rangeMax);
			}
		}

		Random r = new Random();

		int randomValue = rangeMin + (rangeMax - rangeMin) * r.nextInt();

		int heuristicId = -1;

		for (int cumulativeHeuristicUtility : rouletteData) {
			heuristicId++;

			if (cumulativeHeuristicUtility >= randomValue)
				break;
		}

		return heuristics.get(heuristicId);
	}

}

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
package ab.vision;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import dl.utils.ABObjectComp;

public class ABObject extends Rectangle {
	private static final long serialVersionUID = 1L;
	private final int deviation = 2;
	private static int counter = 0;
	public int id;

	// object type
	public ABType type;

	public int area = 0;
	// For all MBRs, the shape is Rect by default.
	public ABShape shape = ABShape.Rect;

	// For all MBRs, the angle is 0 by default.
	public double angle = 0;

	// is Hollow or not
	public boolean hollow = false;

	// Area Object for intersection
	public Area _ar = null;

	// Shifted Area Object for intersection
	protected Area _shiftar = null;

	// protected AffineTransform scale = null;
	protected int shift = 2;

	public Point trajectoryHitPoint = null;

	public ABObject(Rectangle mbr, ABType type) {
		super(mbr);
		this.type = type;
		this.id = counter++;
		_ar = new Area(mbr);
		mbr.grow(shift, shift);
		_shiftar = new Area(mbr);
	}

	public ABObject(Rectangle mbr, ABType type, int id) {
		super(mbr);
		this.type = type;
		this.id = id;
		_ar = new Area(mbr);
		mbr.grow(shift, shift);
		_shiftar = new Area(mbr);
	}

	public ABObject(ABObject ab) {
		super(ab.getBounds());
		this.type = ab.type;
		this.id = ab.id;
		_ar = new Area(ab._ar);
		Rectangle mbr = (Rectangle) ab.getBounds().clone();
		mbr.grow(shift, shift);
		_shiftar = new Area(mbr);
	}

	public ABObject() {
		this.id = counter++;
		this.type = ABType.Unknown;
	}

	/**
	 * @return the type of the object
	 */
	public ABType getType() {
		return type;
	}

	/**
	 * @return the value of the block for the steadiness check
	 */
	public int getTotal() {
		return (int) getCenterX() + (int) getCenterY() + y + x + width + height;
	}

	/**
	 * @return the center point of the object
	 */
	public Point getCenter() {
		return new Point((int) getCenterX(), (int) getCenterY());
	}

	/**
	 * @return the enlarged area of the object
	 */
	public Area getBigger() {
		return _shiftar;
	}

	/**
	 * @return true if this touches with obj, false otherwise; the enlarged area
	 *         is used
	 */
	public boolean touches(ABObject obj) {
		if (x == obj.x && y == obj.y && width == obj.width && height == obj.height)
			return false;

		Area tmp = (Area) _shiftar.clone();
		tmp.intersect(obj._shiftar);
		boolean ret = !tmp.isEmpty();
		return ret;
	}

	/**
	 * @return true if the object is on the ground
	 */
	public boolean isOnGround(int gnd, List<ABObject> hills) {
		if (getY() - gnd < deviation)
			return true;

		for (ABObject hill : hills)
			if (touches(hill))
				return true;

		return false;
	}

	/**
	 * resets the counter for objects
	 */
	public static void resetCounter() {
		counter = 0;
	}

	/**
	 * If o1 supports o2, return true - this time using the touch function
	 */
	public boolean isSupport(ABObject o1) {

		if (x == o1.x && y == o1.y && width == o1.width && height == o1.height)
			return false;

		int o2down = y + height;
		int o2right = x + width;

		int o1right = o1.x + o1.width;

		if (!touches(o1) || o2down - deviation > o1.y || (o1right - deviation < x) || (o1.x + deviation > o2right))
			return false;

		return true;
	}

	/**
	 * |------this-------| |-------------O1---------| |O1|
	 * |----------O1--------| |not O1|
	 */
	public boolean isDirectlyBelow(ABObject o1) {
		if (x == o1.x && y == o1.y && width == o1.width && height == o1.height)
			return false;

		int o2down = y + height;
		int o2right = x + width;

		int o1right = o1.x + o1.width;

		if (getCenterY() - deviation > o1.y || (o1right - deviation < x) || (o1.x + deviation > o2right))
			return false;

		return true;
	}

	/**
	 * @return the x coordinate if the object lies also on the given y
	 *         coordinate
	 */
	public int findXPosition(int target) {
		return findXPosition(target, 0);
	}

	/**
	 * @return the x coordinate if the object lies also on the given y
	 *         coordinate
	 */
	public int findXPosition(int target, int range) {
		if (y - range <= target && y + height + range >= target)
			return x;

		return -1;
	}

	/**
	 * isDirectlyAbove - along the lines of isDirectlyAbove
	 */
	public boolean isDirectlyAbove(ABObject o1) {
		return o1.isDirectlyBelow(this);
	}

	/**
	 * |------this-------| |-------------O1---------| |O1|
	 * |----------O1--------| |is O1|
	 */
	public boolean isBelow(ABObject o1) {
		if (x == o1.x && y == o1.y && width == o1.width && height == o1.height)
			return false;

		int o2down = y + height;

		if (o2down - deviation > o1.y)
			return false;

		return true;
	}

	/**
	 * isAbove - along the lines of isBelow
	 */
	public boolean isAbove(ABObject o1) {
		return o1.isBelow(this);
	}

	/**
	 * |-OBJ-----------| |Directlyleft||-OBJ-----------|
	 * |isLeftTouches||-OBJ-----------| |support| |left| |directlybelow| |below|
	 * |below a not left!|
	 */

	public boolean isDirectlyLeft(ABObject o1) {
		if (x == o1.x && y == o1.y && width == o1.width && height == o1.height)
			return false;
		int o2down = y + height;

		int o1right = o1.x + o1.width;
		int o1down = o1.y + o1.height;

		if (o1right - deviation > getCenterX() || (o1down - deviation < y) || (o1.y + deviation > o2down))
			return false;

		return true;
	}

	/**
	 * along the lines of isDirectlyLeft
	 */
	public boolean isDirectlyRight(ABObject o1) {
		return o1.isDirectlyLeft(this);
	}

	/**
	 * |-OBJ-----------| |left| |-OBJ-----------| |Left||-OBJ-----------|
	 * |support| |left| |directlybelow| |below| |below a not left!|
	 */
	public boolean isLeft(ABObject o1) {
		if (x == o1.x && y == o1.y && width == o1.width && height == o1.height)
			return false;

		int o1right = o1.x + o1.width;

		if (o1right - deviation > getCenterX())
			return false;

		return true;
	}

	/**
	 * along the lines of isLeft
	 */
	public boolean isRight(ABObject o1) {
		return o1.isLeft(this);
	}

	/**
	 * Return a link list of ABObjects that support o1 (test by isSupport
	 * function). objs refers to a list of potential supporters. Empty list will
	 * be returned if no such supporters.
	 */
	public List<ABObject> getSupporters(List<ABObject> objs) {
		List<ABObject> result = new LinkedList<ABObject>();

		// Loop through the potential supporters
		for (ABObject o1 : objs)
			if (isSupport(o1))
				result.add(o1);

		return result;
	}

	/**
	 * finds all objects that are directly below this
	 */
	public List<ABObject> findAllDirectlyBelow(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyBelow(obj) == true)
				sorted.add(obj);

		return sorted;
	}

	/**
	 * finds all objects that are directly below this supportBeam
	 */
	public List<ABObject> findAllDirectlyBelowSupportBeam(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyBelow(obj) == true)
				sorted.add(obj);

		return sorted;
	}

	/**
	 * finds all objects that are directly above this
	 */
	public List<ABObject> findAllDirectlyAbove(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyAbove(obj) == true)
				sorted.add(obj);

		return sorted;
	}

	/**
	 * finds all objects that are above this
	 */
	public List<ABObject> findAllAbove(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isAbove(obj) == true)
				sorted.add(obj);

		return sorted;
	}

	/**
	 * finds all objects that are directly left to this
	 */
	public List<ABObject> findAllDirectlyLeft(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyLeft(obj) == true)
				sorted.add(obj);

		return sorted;
	}

	/**
	 * finds all objects that are directly right to this
	 */
	public List<ABObject> findAllDirectlyRight(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyRight(obj) == true)
				sorted.add(obj);

		return sorted;
	}

	/**
	 * finds the object that is nearest to this object on the left side
	 */
	public ABObject findNearestLeft(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyLeft(obj) == true)
				sorted.add(obj);

		ABObjectComp cmp = new ABObjectComp();
		cmp.sortLeft();
		cmp.setPosition((int) getCenterX());
		Collections.sort(sorted, cmp);

		if (sorted.size() == 0)
			return null;
		else
			return sorted.get(0);
	}

	/**
	 * finds the object that is nearest to this object on the right side
	 */
	public ABObject findNearestRight(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyRight(obj) == true)
				sorted.add(obj);

		ABObjectComp cmp = new ABObjectComp();
		cmp.sortRight();
		cmp.setPosition((int) getCenterX());
		Collections.sort(sorted, cmp);

		if (sorted.size() == 0)
			return null;
		else
			return sorted.get(0);
	}

	/**
	 * finds the object that is nearest to this object above
	 */
	public ABObject findNearestAbove(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyAbove(obj) == true)
				sorted.add(obj);

		ABObjectComp cmp = new ABObjectComp();
		cmp.sortAbove();
		cmp.setPosition((int) getCenterY());
		Collections.sort(sorted, cmp);

		if (sorted.size() == 0)
			return null;
		else
			return sorted.get(0);
	}

	/**
	 * finds the object that is furthest from this object above
	 */
	public int findFurthestAbove(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyAbove(obj) == true)
				sorted.add(obj);
		if (sorted.size() == 0) {
			return y - 20;
		}
		int min = 1000;
		for (ABObject tmp : sorted) {
			if (min > tmp.y)
				min = tmp.y;
		}
		return min - 5;

	}

	/**
	 * finds the object that is nearest to this object below
	 */
	public ABObject findNearestBelow(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isDirectlyBelow(obj) == true)
				sorted.add(obj);

		ABObjectComp cmp = new ABObjectComp();
		cmp.sortBelow();
		cmp.setPosition((int) getCenterY());
		Collections.sort(sorted, cmp);

		if (sorted.size() == 0)
			return null;
		else
			return sorted.get(0);
	}

	/**
	 * finds the object that is a supporter and is nearest
	 */
	public ABObject findNearestSupporter(List<ABObject> objs) {
		List<ABObject> sorted = new LinkedList<ABObject>();

		for (ABObject obj : objs)
			if (isSupport(obj) == true)
				sorted.add(obj);
		ABObjectComp cmp = new ABObjectComp();
		cmp.sortBelow();
		cmp.setPosition((int) getCenterY());
		Collections.sort(sorted, cmp);

		if (sorted.size() == 0)
			return null;
		else
			return sorted.get(0);
	}

	/**
	 * finds all objects, which recursively touches | A || B || C || D | - a
	 * touches b touches c touches d = a touches d
	 */
	public List<ABObject> findAllWhichTouches(List<ABObject> objs) {
		List<ABObject> fresh = new ArrayList<ABObject>(objs);

		List<ABObject> touches = new ArrayList<ABObject>();

		fresh.remove(this);

		while (fresh.size() != 0) {
			ABObject tmpObject = fresh.remove(0);

			if (this.touches(tmpObject)) {
				touches.add(tmpObject);
			}
		}

		return touches;
	}

	/**
	 * finds all objects, which recursively touches | A || B || C || D | - a
	 * touches b touches c touches d = a touches d
	 */
	public List<ABObject> findAllWhichRecursivelyTouches(List<ABObject> objs) {
		List<ABObject> fresh = new ArrayList<ABObject>(objs);

		List<ABObject> opened = new ArrayList<ABObject>();

		List<ABObject> touches = new ArrayList<ABObject>();

		fresh.remove(this);

		opened.add(this);

		while (opened.size() != 0) {
			ABObject tmpObject = opened.remove(0);

			touches.add(tmpObject);

			for (int i = 0; i < fresh.size(); i++) {
				if (tmpObject.touches(fresh.get(i))) {
					opened.add(fresh.remove(i));
					--i;
				}
			}
		}

		touches.remove(this);

		return touches;
	}

	public List<ABObject> findAllDirectlyRightWhichRecursivelyTouches(List<ABObject> objs) {
		List<ABObject> result = new ArrayList<ABObject>();

		List<ABObject> allDirectlyRight = this.findAllDirectlyRight(objs);

		// sort all objects by x
		Collections.sort(allDirectlyRight, new Comparator<ABObject>() {
			@Override
			public int compare(ABObject a, ABObject b) {
				return a.x - b.x;
			}
		});

		ABObject tmpObj = this;
		for (int i = 0; i < allDirectlyRight.size(); i++) {
			if (!tmpObj.touches(allDirectlyRight.get(i)))
				break;

			tmpObj = allDirectlyRight.get(i);
			result.add(tmpObj);
		}

		result.remove(this);

		return result;

	}

	/**
	 * find lowest left object which recursively touches | A | | B || C | | D |
	 *
	 * method called on A, returns D, if there is no lower left, returns this
	 * instance
	 */
	public ABObject findSameAbove(List<ABObject> objs) {
		List<ABObject> directlyAbove = findAllDirectlyAbove(objs);

		ABObject above = null;
		for (ABObject tmp : directlyAbove) {
			if (touches(tmp) && Math.abs(tmp.width - width + tmp.height - height) < 3) {
				above = tmp;
			}

		}

		if (above == null) {
			return this;
		}

		return above.findSameAbove(objs);

	}

	public ABObject findLowestLeftWhichRecursivelyTouches(List<ABObject> objs) {
		List<ABObject> allWhichTouches = findAllWhichRecursivelyTouches(objs);

		if (allWhichTouches.size() == 0)
			return this;

		// sort all objects which touches by y
		Collections.sort(allWhichTouches, new Comparator<ABObject>() {
			@Override
			public int compare(ABObject a, ABObject b) {
				if (a.x == b.x)
					return b.y + b.height - a.y + a.height;

				return a.x - b.x;
			}
		});

		return allWhichTouches.remove(0);
	}

	/**
	 * @return the free distance below pig
	 */
	public int getDistanceBelowPig(List<ABObject> blocks) {
		List<ABObject> below = findAllDirectlyBelowSupportBeam(blocks);
		below.removeAll(getSupporters(blocks));
		if (below.size() > 0) {
			return below.get(0).y - y;
		} else {
			return -1;
		}
	}

	/**
	 * @return the nearest object below this
	 */
	public ABObject getNearestObjectBelow(List<ABObject> blocks, int position) {
		List<ABObject> below = findAllDirectlyBelow(blocks);
		if (below.size() < position)
			return null;
		ABObjectComp cmp = new ABObjectComp();
		cmp.sortBelow();
		cmp.setPosition((int) getCenterY());
		Collections.sort(below, cmp);

		ABObject tmp = below.get(position - 1);

		return tmp;

	}

	/**
	 * @return true if there is something big satisfying the following
	 *         condition: width > 13 || height > 13
	 */
	public boolean isSomethingBigAbove(List<ABObject> blocks) {
		List<ABObject> above = findAllDirectlyAbove(blocks);

		for (ABObject tmp : above) {
			if (tmp.width > 13 || tmp.height > 13) {
				return true;
			}

		}

		return false;
	}

	/**
	 * x,y,width and height has to be the same
	 */
	@Override
	public boolean equals(Object tmp) {
		ABObject obj = (ABObject) tmp;
		if (x == obj.x && y == obj.y && width == obj.width && height == obj.height)
			return true;
		else
			return false;
	}
}

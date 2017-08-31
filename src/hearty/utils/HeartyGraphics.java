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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import ab.planner.TrajectoryPlannerHeartyTian;
import ab.utils.ABUtilHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.Body;

/**
 * This class is used to store screenshot from the disk, plot trajectories and
 * objects on them and later store them on the disk.
 */

public class HeartyGraphics {
	private static int _nOfScreens = 1;

	public String _filename = "img";

	public String _filenameAppend = "";

	public BufferedImage _canvas = null;

	BufferedImage _canvasOrig = null;

	Graphics2D _g2d = null;

	Color _color = Color.WHITE;

	/**
	 * Blank constructor that creates a white canvas without anything on it.
	 */
	public HeartyGraphics() {
		this("img", new BufferedImage(840, 480, BufferedImage.TYPE_INT_RGB));
	}

	/**
	 * Creates a canvas from the screenshot that is provided.
	 */
	public HeartyGraphics(BufferedImage canvas) {
		this("img", canvas);
	}

	/**
	 * Creates a canvas from the screenshot that is provided with the given
	 * filename.
	 */
	public HeartyGraphics(String filename, BufferedImage canvas) {
		_canvas = deepCopy(canvas);
		_canvasOrig = deepCopy(canvas);
		_g2d = _canvas.createGraphics();
	}

	/**
	 * writes screenshot to disk
	 *
	 * @param screenshot
	 *            - screenshot to save
	 * @param path
	 *            - path, where the image is going to be saved
	 */
	public void writeToDisk(String path) {
		final String imgFilename = String.format(path + File.separator + "%06d_" + _filename + _filenameAppend + ".png",
				_nOfScreens++);
		System.out.println("Saving image to " + imgFilename);

		try {
			ImageIO.write(_canvas, "png", new File(imgFilename));
		} catch (Exception e) {
			System.err.println("failed to save image " + imgFilename);
			e.printStackTrace();
		}
	}

	/**
	 * resets the canvas to the starting one
	 */
	public void resetCanvas() {
		_canvas = deepCopy(_canvasOrig);
		_g2d = _canvas.createGraphics();
	}

	/**
	 * clears the canvas
	 */
	public void clearCanvas() {
		clearCanvas(Color.white);
	}

	/**
	 * clears the canvas and sets its color to the color provided
	 */
	public void clearCanvas(Color color) {
		_g2d.setBackground(Color.WHITE);
		_g2d.clearRect(0, 0, 840, 480);
	}

	/**
	 * @return the current canvas
	 */
	public BufferedImage getCanvas() {
		return deepCopy(_canvas);
	}

	/**
	 * replaces the canvas with a new one
	 */
	public void replaceCanvas(BufferedImage canvas) {
		_canvas = deepCopy(canvas);
	}

	/**
	 * Sets drawing color
	 */
	public void setColor(Color color) {
		_color = color;
	}

	/**
	 * Sets RGB color
	 */
	public void setColor(int r, int g, int b) {
		_color = new Color(r, g, b);
	}

	/**
	 * Draws a string into active Graphics2D
	 */
	public void drawString(String str) {
		drawString(str, 200, 30, _color);
	}

	/**
	 * Draws a string into active Graphics2D
	 */
	public void drawString(String str, Color color) {
		drawString(str, 200, 30, color);
	}

	/**
	 * Draws a string into active Graphics2D
	 */
	public void drawString(String str, int x, int y) {
		drawString(str, x, y, _color);
	}

	/**
	 * Draws a string into active Graphics2D
	 */
	public void drawString(String str, int x, int y, Color color) {
		_g2d.setColor(color);
		_g2d.drawString(str, x, y);
		_g2d.setColor(_color);
	}

	/**
	 * Draws a point into active Graphics2D
	 */
	public void drawPoint(Point p) {
		drawPoint(p, _color);
	}

	/**
	 * Draws a point into active Graphics2D
	 */
	public void drawPoint(Point p, Color color) {
		_g2d.setColor(color);
		_g2d.drawRect(p.x, p.y, 0, 0);

		_g2d.setColor(_color);
	}

	/**
	 * Draws ABObjects into active Graphics2D
	 */
	public void drawABObjects(List<ABObject> objects) {
		drawABObjects(objects, _color);
	}

	/**
	 * Draws ABObjects into active Graphics2D with a predefined color
	 */
	public void drawABObjects(List<ABObject> objects, Color color) {
		if (objects == null) {
			return;
		}

		for (ABObject object : objects) {
			Body b = (Body) object;
			b.draw(_g2d, false, color);
		}

	}

	/**
	 * Draws ABObject into active Graphics2D
	 */
	public void drawABObject(ABObject object) {
		drawABObject(object, _color);
	}

	/**
	 * Draws ABObject into active Graphics2D with a predefined color
	 */
	public void drawABObject(ABObject object, Color color) {
		Body b = (Body) object;
		b.draw(_g2d, false, color);
	}

	/**
	 * draws all ABObjects from objects below tmpObj
	 */
	public void drawAllABObjectsDirectlyBelow(ABObject tmpObj, List<ABObject> objects) {
		// draw base object black
		((Body) tmpObj).draw(_g2d, false, Color.black);

		// find nearest object
		ABObject nearest = tmpObj.findNearestBelow(objects);

		// draw nearest object red
		if (nearest != null)
			((Body) nearest).draw(_g2d, false, Color.red);

		// find and draw all objects directly below
		for (ABObject res : tmpObj.findAllDirectlyBelow(objects))
			((Body) res).draw(_g2d, false, Color.white);
	}

	/**
	 * draws all ABObjects from objects above tmpObj
	 */
	public void drawAllABObjectsDirectlyAbove(ABObject tmpObj, List<ABObject> objects) {
		// draw base object black
		((Body) tmpObj).draw(_g2d, false, Color.black);

		// find nearest object
		ABObject nearest = tmpObj.findNearestAbove(objects);

		// draw nearest object red
		if (nearest != null)
			((Body) nearest).draw(_g2d, false, Color.red);

		// find and draw all objects directly above
		for (ABObject res : tmpObj.findAllDirectlyAbove(objects))
			((Body) res).draw(_g2d, false, Color.white);
	}

	/**
	 * draws all ABObjects from objects to the right of tmpObj
	 */
	public void drawAllABObjectsDirectlyRight(ABObject tmpObj, List<ABObject> objects) {
		// draw base object black
		((Body) tmpObj).draw(_g2d, false, Color.black);

		// find nearest object
		ABObject nearest = tmpObj.findNearestRight(objects);

		// draw nearest object red
		if (nearest != null)
			((Body) nearest).draw(_g2d, false, Color.red);

		// find and draw all objects directly right
		for (ABObject res : tmpObj.findAllDirectlyRight(objects))
			((Body) res).draw(_g2d, false, Color.white);
	}

	/**
	 * draws all ABObjects from objects to the left of tmpObj
	 */
	public void drawAllABObjectsDirectlyLeft(ABObject tmpObj, List<ABObject> objects) {
		// draw base object black
		((Body) tmpObj).draw(_g2d, false, Color.black);

		// find nearest object
		ABObject nearest = tmpObj.findNearestLeft(objects);

		// draw nearest object red
		if (nearest != null)
			((Body) nearest).draw(_g2d, false, Color.red);

		// find and draw all objects directly left
		for (ABObject res : tmpObj.findAllDirectlyLeft(objects))
			((Body) res).draw(_g2d, false, Color.white);
	}

	/**
	 * draws a slingshot
	 */
	public void drawSlingshot(Rectangle sling) {
		_g2d.draw(sling);
		_g2d.draw(new Rectangle(new Point((int) (sling.getCenterX() + 0.5), (int) (sling.getCenterY() + 0.5))));
	}

	/**
	 * Plot a trajectory
	 *
	 * @param tp
	 *            - trajectory planner instance slingshot - bounding rectangle
	 *            of the slingshot releasePoint - point where the mouse click
	 *            was released from objects - if specified, the trajectory is
	 *            drawed red where collides with objects
	 * @return the canvas with trajectory drawn
	 */

	public void plotTrajectory(TrajectoryPlannerHeartyTian tp, Rectangle slingshot, Point releasePoint,
			ABType birdOnSling) {
		plotTrajectory(tp, slingshot, releasePoint, birdOnSling, null);
	}

	/**
	 * Plot a trajectory
	 *
	 * @param tp
	 *            - trajectory planner instance slingshot - bounding rectangle
	 *            of the slingshot releasePoint - point where the mouse click
	 *            was released from objects - if specified, the trajectory is
	 *            drawed red where collides with objects
	 * @return the canvas with trajectory drawn
	 */

	public void plotTrajectory(TrajectoryPlannerHeartyTian tp, Rectangle slingshot, Point releasePoint,
			ABType birdOnSling, List<ABObject> objects) {
		List<Point> trajectoryPoints = tp.predictTrajectory(slingshot, releasePoint);

		plotTrajectory(trajectoryPoints, birdOnSling, objects);
	}

	/**
	 * Plot a trajectory
	 *
	 * @param tp
	 *            - trajectory planner instance slingshot - bounding rectangle
	 *            of the slingshot releasePoint - point where the mouse click
	 *            was released from objects - if specified, the trajectory is
	 *            drawed red where collides with objects
	 * @return the canvas with trajectory drawn
	 */

	public void plotTrajectory(List<Point> trajectoryPoints, ABType birdOnSling, List<ABObject> objects) {
		// draw estimated trajectory
		for (Point p : trajectoryPoints) {
			if ((p.y > 0) && (p.y < _canvas.getHeight(null))) {
				_g2d.setColor(Color.green);

				if (objects != null)
					for (ABObject ob : objects)
						if (ABUtilHeartyTian.pointHitsObjectWithinARange(ob, p, birdOnSling.getBirdRadius()))
							_g2d.setColor(Color.red);

				_g2d.drawRect(p.x, p.y, 1, 1);
			}
		}
	}

	/**
	 * writes the data about a given heuristic onto the canvas
	 */
	public void writeHeuristicData(int trajectoryUtility, int heuristicUtility) {
		drawString(String.format("Trajectory utility: %d, heuristic utility: %d", trajectoryUtility, heuristicUtility),
				200, 45, Color.red);
	}

	/**
	 * Buffered Image deep copy
	 */
	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
}

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
package ab.vision.real.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;

import ab.vision.ABShape;
import ab.vision.ABType;
import ab.vision.real.ImageSegmenter;
import ab.vision.real.LineSegment;
import hearty.utils.HeartyUtils;

public class Poly extends Body {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Polygon polygon = null;

	public Poly(ArrayList<LineSegment> lines, int left, int top, ABType type, double xs, double ys) {
		polygon = new Polygon();
		shape = ABShape.Poly;
		if (lines != null) {
			for (LineSegment l : lines) {
				Point start = l._start;
				polygon.addPoint(start.x + left, start.y + top);
			}
		}
		centerX = xs;
		centerY = ys;
		angle = 0;
		area = getBounds().height * getBounds().width;
		this.type = type;
		super.setBounds(polygon.getBounds());
		_ar = new Area(polygon);
		_shiftar = HeartyUtils.resize(polygon);
	}

	@Override
	public Rectangle getBounds() {
		return polygon.getBounds();
	}

	@Override
	public void draw(Graphics2D g, boolean fill, Color boxColor) {
		if (fill) {
			g.setColor(ImageSegmenter._colors[type.id]);
			g.fillPolygon(polygon);
		} else {
			g.setColor(boxColor);
			g.drawPolygon(polygon);
		}

	}

	@Override
	public boolean contains(Point pt) {
		return polygon.contains(pt);
	}

	@Override
	public String toString() {
		return "Poly";
		// return String.format("Poly: id:%d type:%s hollow:%b %dpts at x:%3.1f
		// y:%3.1f", id, type, hollow, polygon.npoints, centerX, centerY);
	}
}

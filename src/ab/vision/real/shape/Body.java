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

import ab.vision.ABObject;

public abstract class Body extends ABObject

{
	private static final long serialVersionUID = 1L;

	public Body() {
		super();
	}

	// position (x, y) as center of the object
	public double centerX = 0;
	public double centerY = 0;

	public static int round(double i) {
		return (int) (i + 0.5);
	}

	@Override
	public Point getCenter() {
		Point point = new Point();
		point.setLocation(centerX, centerY);
		return point;
	}

	@Override
	public double getCenterX() {
		return centerX;
	}

	@Override
	public double getCenterY() {
		return centerY;
	}

	public abstract void draw(Graphics2D g, boolean fill, Color boxColor);
}

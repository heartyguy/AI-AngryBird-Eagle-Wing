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
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.Vision;

public class ShootingAgent {

	public static void shoot(String[] args, boolean cshoot) {
		ActionRobot ar = new ActionRobot();
		TrajectoryPlannerHeartyTian tp = new TrajectoryPlannerHeartyTian();
		ActionRobot.fullyZoomOut();
		Vision vision = new Vision(ActionRobot.doScreenShot());
		Rectangle slingshot = vision.findSlingshotMBR();
		while (slingshot == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("no slingshot detected. Please remove pop up or zoom out");
			vision = new Vision(ActionRobot.doScreenShot());
			slingshot = vision.findSlingshotMBR();
		}
		Point refPoint = tp.getReferencePoint(slingshot);
		int x = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int tap = 0;
		if (args.length > 3)
			tap = Integer.parseInt(args[3]);

		Shot shot = null;
		if (cshoot)
			shot = new Shot(refPoint.x, refPoint.y, -x, y, 0, tap);
		else {
			int r = x;
			double theta = y / 100;
			int dx = -(int) (r * Math.cos(Math.toRadians(theta)));
			int dy = (int) (r * Math.sin(Math.toRadians(theta)));
			shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tap);
		}
		vision = new Vision(ActionRobot.doScreenShot());
		Rectangle _slingshot = vision.findSlingshotMBR();
		if (!slingshot.equals(_slingshot))
			System.out.println("the scale is changed, the shot might not be executed properly.");
		// ar.cshoot(shot);
		System.exit(0);
	}

}

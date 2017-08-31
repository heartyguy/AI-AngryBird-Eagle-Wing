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

import java.awt.image.BufferedImage;

import ab.demo.other.ActionRobot;
import ab.vision.Vision;

public class VisionPerformanceTest {

	private static void log(String message) {
		System.out.println(message);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new ActionRobot();
		while (true) {
			long time = System.nanoTime();
			BufferedImage image = ActionRobot.doScreenShot();
			Vision vision = new Vision(image);
			vision.findBlocksMBR();
			log((System.nanoTime() - time) + "");
		}
	}

}

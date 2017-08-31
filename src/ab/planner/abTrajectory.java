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
package ab.planner;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.imageio.ImageIO;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.utils.ImageSegFrame;
import ab.vision.GameStateExtractor;
import ab.vision.VisionMBR;
import ab.vision.VisionUtils;

// User Interface of the trajectory module
public class abTrajectory {
	private static Proxy server;

	public abTrajectory() {
		if (server == null) {
			try {
				server = new Proxy(9000) {
					@Override
					public void onOpen() {
						System.out.println("Client connected");
					}

					@Override
					public void onClose() {
						System.out.println("Client disconnected");
					}
				};
				server.start();

				System.out.println("abTrajectory is running which should not be the case!");

				System.out.println("Server started on port: " + server.getPort());

				System.out.println("Waiting for client to connect");
				server.waitForClients(1);

			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	public BufferedImage doScreenShot() {
		byte[] imageBytes = server.send(new ProxyScreenshotMessage());
		BufferedImage image = null;
		try {
			image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (IOException e) {
			// do something
		}

		return image;
	}

	public static void main(String args[]) {
		abTrajectory ar = new abTrajectory();

		ImageSegFrame frame = null;
		GameStateExtractor gameStateExtractor = new GameStateExtractor();
		TrajectoryPlannerHeartyTian trajectory = new TrajectoryPlannerHeartyTian();

		while (true) {
			// capture image
			BufferedImage screenshot = ar.doScreenShot();
			final int nHeight = screenshot.getHeight();
			final int nWidth = screenshot.getWidth();

			System.out.println("captured image of size " + nWidth + "-by-" + nHeight);

			// extract game state
			GameStateExtractor.GameState state = gameStateExtractor.getGameState(screenshot);
			if (state != GameStateExtractor.GameState.PLAYING) {
				continue;
			}

			// process image
			VisionMBR vision = new VisionMBR(screenshot);
			List<Rectangle> pigs = vision.findPigsMBR();
			List<Rectangle> redBirds = vision.findRedBirdsMBRs();

			Rectangle sling = vision.findSlingshotMBR();
			if (sling == null) {
				System.out.println("...could not find the slingshot");
				continue;
			}
			// System.out.println("...found " + pigs.size() + " pigs and " +
			// redBirds.size() + " birds");
			System.out.println("...found slingshot at " + sling.toString());

			// convert screenshot to grey scale and draw bounding boxes
			screenshot = VisionUtils.convert2grey(screenshot);
			VisionUtils.drawBoundingBoxes(screenshot, pigs, Color.GREEN);
			VisionUtils.drawBoundingBoxes(screenshot, redBirds, Color.PINK);
			VisionUtils.drawBoundingBox(screenshot, sling, Color.ORANGE);

			// find active bird
			Rectangle activeBird = trajectory.findActiveBird(redBirds);
			if (activeBird == null) {
				System.out.println("...could not find active bird");
				continue;
			}

			trajectory.plotTrajectory(screenshot, sling, activeBird);

			// show image
			if (frame == null) {
				frame = new ImageSegFrame("trajectory", screenshot);
			} else {
				frame.refresh(screenshot);
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}

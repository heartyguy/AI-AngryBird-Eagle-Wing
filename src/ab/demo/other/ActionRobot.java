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
package ab.demo.other;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import ab.planner.TrajectoryPlannerHeartyTian;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyDragMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import dl.utils.DLUtils;

/**
 * Util class for basic functions
 * 
 */
public class ActionRobot {
	public static Proxy proxy;
	public String level_status = "UNKNOWN";
	public int current_score = 0;
	private LoadingLevelSchema lls;
	private RestartLevelSchema rls;
	static {
		if (proxy == null) {
			try {
				proxy = new Proxy(9000) {
					@Override
					public void onOpen() {
						System.out.println("Client connected");
					}

					@Override
					public void onClose() {
						System.out.println("Client disconnected");
					}
				};
				proxy.start();

				System.out.println("Server started on port: " + proxy.getPort());

				System.out.println("Waiting for client to connect");
				proxy.waitForClients(1);

			} catch (UnknownHostException e) {

				e.printStackTrace();
			}
		}
	}

	// A java util class for the standalone version. It provides common
	// functions an agent would use. E.g. get the screenshot
	public ActionRobot() {
		lls = new LoadingLevelSchema(proxy);
		rls = new RestartLevelSchema(proxy);
	}

	public void restartLevel() {
		rls.restartLevel();
	}

	public static void GoFromMainMenuToLevelSelection() {
		// --- go from the main menu to the episode menu
		GameState state = StateUtil.getGameState(proxy);
		while (state == GameState.MAIN_MENU) {

			System.out.println("Go to the Episode Menu");
			proxy.send(new ProxyClickMessage(305, 277));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			state = StateUtil.getGameState(proxy);
		}
		// --- go from the episode menu to the level selection menu
		while (state == GameState.EPISODE_MENU) {
			System.out.println("Select the Poached Eggs Episode");
			proxy.send(new ProxyClickMessage(150, 300));
			state = StateUtil.getGameState(proxy);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			state = StateUtil.getGameState(proxy);
		}

	}

	public GameState shootWithStateInfoReturned(List<Shot> csc) {
		ShootingSchema ss = new ShootingSchema();
		ss.shoot(proxy, csc);
		System.out.println("Shooting Completed");
		GameState state = StateUtil.getGameState(proxy);
		return state;

	}

	public synchronized GameState getState() {
		GameState state = StateUtil.getGameState(proxy);
		return state;
	}

	public void shoot(List<Shot> csc) {
		ShootingSchema ss = new ShootingSchema();

		ss.shoot(proxy, csc);
		System.out.println("Shooting Completed");
		System.out.println("wait 15 seconds to ensure all objects in the scene static");
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/* Checks and waits until no objects are moving in the screenshot */
	public void waitingForSceneToBeSteady(List<ABObject> birdsOld) {
		int last = -1;
		while (getState() == GameState.PLAYING) {
			BufferedImage screen = doScreenShot();

			// erase birds
			Graphics2D g2d = screen.createGraphics();

			for (ABObject bird : birdsOld) {
				g2d.setColor(new Color(148, 206, 222));
				g2d.fillRect(bird.getCenter().x - 40, bird.getCenter().y - 40, 80, 80);

				g2d.setColor(Color.magenta);
				g2d.drawString(bird.type.toString(), bird.getCenter().x, bird.getCenter().y);
			}

			Vision vision = new Vision(screen);
			int total = 0;

			List<ABObject> birds = vision.findBirdsMBR();
			for (ABObject bird : birds) {
				total += bird.getTotal();
			}

			List<ABObject> pigs = vision.findPigsRealShape();
			for (ABObject pig : pigs) {
				total += pig.getTotal();
			}

			List<ABObject> blocks = vision.findBlocksRealShape();
			for (ABObject block : blocks) {
				total += block.getTotal();
			}

			if ((DLUtils.isBirdLeft(blocks, pigs, birds) || DLUtils.isBirdRight(blocks, pigs, birds))
					&& Math.abs(last - total) <= 1)
				break;

			last = total;

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void cshoot(Shot shot, TrajectoryPlannerHeartyTian tp, Rectangle sling, ABType birdOnSling,
			List<ABObject> blocks, List<ABObject> birds) {
		ShootingSchema ss = new ShootingSchema();
		LinkedList<Shot> shots = new LinkedList<Shot>();

		shots.add(shot);
		ss.shoot(proxy, shots);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		waitingForSceneToBeSteady(birds);
	}

	public void cFastshoot(Shot shot) {
		ShootingSchema ss = new ShootingSchema();
		LinkedList<Shot> shots = new LinkedList<Shot>();
		shots.add(shot);
		ss.shoot(proxy, shots);
	}

	public void fshoot(Shot shot) {
		ShootingSchema ss = new ShootingSchema();
		LinkedList<Shot> shots = new LinkedList<Shot>();
		shots.add(shot);
		ss.shoot(proxy, shots);
		// System.out.println(" tap time : " + shot.getT_tap());
		System.out.println("Shooting Completed");

	}

	public void click() {
		proxy.send(new ProxyClickMessage(100, 100));
	}

	public void drag() {
		proxy.send(new ProxyDragMessage(0, 0, 0, 0));
	}

	public void loadLevel(int... i) {
		int level = 1;
		if (i.length > 0) {
			level = i[0];
		}

		lls.loadLevel(level);
	}

	public static void fullyZoomOut() {
		for (int k = 0; k < 15; k++) {

			proxy.send(new ProxyMouseWheelMessage(-1));
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void fullyZoomIn() {
		for (int k = 0; k < 15; k++) {

			proxy.send(new ProxyMouseWheelMessage(1));
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void solveStart() {
		click();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		click();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage doScreenShot() {
		byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
		BufferedImage image = null;
		try {
			image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (IOException e) {

		}

		return image;
	}

	/*
	 * @return the type of the bird on the sling.
	 * 
	 **/
	public ABType getBirdTypeOnSling() throws IOException {
		throw new IOException("This should not be called!");
	}

	public static void main(String args[]) {

		long time = System.currentTimeMillis();
		ActionRobot.doScreenShot();
		time = System.currentTimeMillis() - time;
		System.out.println(" cost: " + time);
		time = System.currentTimeMillis();
		int count = 0;
		while (count < 40) {
			ActionRobot.doScreenShot();
			count++;
		}

		System.out.println(" time to take 40 screenshots" + (System.currentTimeMillis() - time));
		System.exit(0);

	}

	public int getScore() {
		return StateUtil.getScore(ActionRobot.proxy);
	}
}

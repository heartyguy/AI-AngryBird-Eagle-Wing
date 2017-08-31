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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ClientActionRobot;
import ab.demo.other.ClientActionRobotJava;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import hearty.heuristics.AbstractHeuristic;
import hearty.heuristics.BuildingHeuristic;
import hearty.heuristics.DestroyAsManyPigsAtOnceAsPossibleHeuristic;
import hearty.heuristics.DynamiteHeuristic;
import hearty.heuristics.PenetrationHeuristic;
import hearty.heuristics.RoundStoneHeuristic;
import hearty.heuristics.SceneState;
import hearty.utils.HeartyLevelSelection;
import hearty.utils.HeartyUtils;
import hearty.utils.LogWriter;

public class HeartyTian2017Main implements Runnable {
	// Wrapper of the communicating messages
	private ClientActionRobotJava actionRobot;
	private Random randomGenerator;

	private int id = 23349;
	// public int chosen_level = -1;

	private int heuristicId = 0;
	TrajectoryPlannerHeartyTian tp;

	private HeartyLevelSelection levelSchemer;

	private boolean firstShot;
	private Point prevTarget;

	/**
	 * Constructor using the default IP
	 */
	public HeartyTian2017Main() {
		// the default ip is the localhost
		actionRobot = new ClientActionRobotJava("127.0.0.1");
		tp = new TrajectoryPlannerHeartyTian();
		randomGenerator = new Random();
		prevTarget = null;
		firstShot = true;
	}

	public HeartyTian2017Main(int level) {
		// the default ip is the localhost
		actionRobot = new ClientActionRobotJava("127.0.0.1");
		tp = new TrajectoryPlannerHeartyTian();
		randomGenerator = new Random();
		prevTarget = null;
		HeartyLevelSelection.currentLevel = (byte) (level);
		System.out.println("what the f is HeartyLevelSelection.currentLevel " + HeartyLevelSelection.currentLevel);
		// chosen_level = level;
		firstShot = true;
	}

	/**
	 * Constructor with a specified IP
	 */
	public HeartyTian2017Main(String ip) {
		actionRobot = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlannerHeartyTian();
		randomGenerator = new Random();
		prevTarget = null;
		firstShot = true;

	}

	public HeartyTian2017Main(String ip, int id) {
		actionRobot = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlannerHeartyTian();
		randomGenerator = new Random();
		prevTarget = null;
		firstShot = true;
		this.id = id;
	}

	public HeartyTian2017Main(String ip, int id, int level) {
		actionRobot = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlannerHeartyTian();
		randomGenerator = new Random();
		prevTarget = null;
		firstShot = true;
		// chosen_level = level;
		HeartyLevelSelection.currentLevel = (byte) (level);
		this.id = id;
	}

	/*
	 * Run the Client (Naive Agent) this is the loop that keeps the agent
	 * running for the whole time
	 */
	@Override
	public void run() {
		byte[] info = actionRobot.configure(ClientActionRobot.intToByteArray(id));
		System.out.println("after configuring actionrobot");
		levelSchemer = new HeartyLevelSelection(info, actionRobot);
		System.out.println("after creating HeartyLevelSelection");
		System.out.println("HeartyLevelSelection.currentLevel is " + HeartyLevelSelection.currentLevel);
		actionRobot.loadLevel(HeartyLevelSelection.currentLevel);

		GameState state;

		while (true) {
			System.out.println("HeartyTian2017Main before solve");
			state = solve();

			if (state != GameState.PLAYING) {
				LogWriter.lastScore = 0;
			}

			// If the level is solved , go to the next level
			if (state == GameState.WON) {

				levelSchemer.updateStats(actionRobot, true);

				actionRobot.loadLevel(HeartyLevelSelection.currentLevel);

				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlannerHeartyTian();

				// first shot on this level, try high shot first
				firstShot = true;

			}
			// If lost, then restart the level
			else if (state == GameState.LOST) {

				levelSchemer.updateStats(actionRobot, false);

				actionRobot.loadLevel(HeartyLevelSelection.currentLevel);

			} else if (state == GameState.LEVEL_SELECTION) {
				System.out.println("unexpected level selection page, go to the last current level : "
						+ HeartyLevelSelection.currentLevel);
				actionRobot.loadLevel(HeartyLevelSelection.currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, reload the level : " + HeartyLevelSelection.currentLevel);
				actionRobot.loadLevel(HeartyLevelSelection.currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out.println(
						"unexpected episode menu page, reload the level: " + HeartyLevelSelection.currentLevel);
				actionRobot.loadLevel(HeartyLevelSelection.currentLevel);
			}

		}

	}

	/**
	 * Solve a particular level by shooting birds directly to pigs only intended
	 * for one level
	 * 
	 * @return GameState: the game state after shots.
	 */
	public GameState solve() {
		// capture Image
		BufferedImage screenshot = actionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		Rectangle sling = vision.findSlingshotRealShape();

		// Get bird type on sling.
		ABType birdOnSling = vision.getBirdTypeOnSling();
		System.out.println("in solve() before actionRobot.checkState()");
		GameState startState = actionRobot.checkState();
		System.out.println("start state is playing? " + (startState != GameState.PLAYING));
		if (startState != GameState.PLAYING) {
			return startState;
		}

		// If the level is loaded (in PLAYING state) but no slingshot detected
		// or no bird on sling is detected, then the agent will try to do
		// something with it.
		while ((sling == null || birdOnSling == ABType.Unknown) && actionRobot.checkState() == GameState.PLAYING) {
			visionInfo retValues = new visionInfo(sling, vision, screenshot, birdOnSling);
			waitTillSlingshotIsFound(retValues);
			sling = retValues.sling;
			vision = retValues.vision;
			screenshot = retValues.screenshot;
			birdOnSling = retValues.birdOnSling;
		}

		startState = actionRobot.checkState();
		if (startState != GameState.PLAYING) {
			return startState;
		}

		final List<ABObject> pigs = vision.findPigsRealShape();
		final List<ABObject> birds = vision.findBirdsRealShape();
		final List<ABObject> hills = vision.findHills();
		final List<ABObject> blocks = vision.findBlocksRealShape();
		int gnd = vision.getGroundLevel();
		tp.ground = gnd;

		// Get game state.
		GameState state = actionRobot.checkState();

		// creates the logwriter that will be used to store the information
		// about turns
		final LogWriter log = new LogWriter("output.csv");
		log.appendStartLevel(HeartyLevelSelection.currentLevel, pigs, birds, blocks, hills, birdOnSling);
		log.saveStart(actionRobot.doScreenShot());

		// accumulates information about the scene that we are currently playing
		SceneState currentState = new SceneState(pigs, hills, blocks, sling, vision.findTNTs(), prevTarget, firstShot,
				birds, birdOnSling);
		// Prepare shot.
		Shot shot = null;

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {

				shot = findHeuristicAndShoot(currentState, log);
			} else {
				System.err.println("No Release Point Found, will try to zoom out...");

				// try to zoom out
				actionRobot.fullyZoomOut();

				return state;
			}

			// check whether the slingshot is changed. the change of the
			// slingshot indicates a change in the scale.
			state = performTheActualShooting(log, currentState, shot);
		}

		return state;
	}

	/**
	 ** performs the waiting for the slingshot to be found by zooming out and in
	 * and out
	 **/
	private void waitTillSlingshotIsFound(visionInfo inf) {
		if (inf.sling == null) {
			System.out.println("No slingshot detected. Please remove pop up or zoom out");
		} else if (inf.birdOnSling == ABType.Unknown) {
			System.out.println("No bird on sling detected!!");
		}

		actionRobot.fullyZoomOut();
		inf.screenshot = actionRobot.doScreenShot();
		inf.vision = new Vision(inf.screenshot);
		inf.sling = inf.vision.findSlingshotRealShape();
		inf.birdOnSling = inf.vision.getBirdTypeOnSling();

		if (inf.birdOnSling == ABType.Unknown) {
			actionRobot.fullyZoomIn();
			inf.screenshot = actionRobot.doScreenShot();
			inf.vision = new Vision(inf.screenshot);
			inf.birdOnSling = inf.vision.getBirdTypeOnSling();
			actionRobot.fullyZoomOut();

			inf.screenshot = actionRobot.doScreenShot();
			inf.vision = new Vision(inf.screenshot);
			inf.sling = inf.vision.findSlingshotRealShape();
		}
	}

	/**
	 ** this is the actual meta-agent that decides which strategy will be played
	 ** 
	 * @return Shot that should be fired
	 **/
	private Shot findHeuristicAndShoot(SceneState currentState, LogWriter log) {
		Random rand = new Random();
		AbstractHeuristic possibleHeuristics[] = new AbstractHeuristic[5];
		possibleHeuristics[0] = new BuildingHeuristic(currentState, actionRobot, tp, log);
		possibleHeuristics[1] = new DestroyAsManyPigsAtOnceAsPossibleHeuristic(currentState, actionRobot, tp, log);
		possibleHeuristics[2] = new RoundStoneHeuristic(currentState, actionRobot, tp, log);
		possibleHeuristics[3] = new DynamiteHeuristic(currentState, actionRobot, tp, log);
		possibleHeuristics[4] = new PenetrationHeuristic(currentState, actionRobot, tp, log);
		int heuristicId = 0;
		int max = 0xffff0000;

		for (int i = 0; i < possibleHeuristics.length; ++i) {
			AbstractHeuristic tmp = possibleHeuristics[i];

			System.out.println("Utility " + tmp.toString() + ": " + tmp.getUtility());
			tmp.writeToLog();
			if (max < tmp.getUtility() && !(i == 0 && tmp.getUtility() < 1000)) {
				max = tmp.getUtility();
				heuristicId = i;
			}

		}

		Shot shot = possibleHeuristics[heuristicId].getShot();
		if (shot != null) {
			System.out.println("Heuristic ID: " + heuristicId);
		}

		// if there are hills in the way, choose a random set of blocks and make
		// them targets
		if (possibleHeuristics[heuristicId].getSelectedDLTrajectory() != null
				&& possibleHeuristics[heuristicId].getSelectedDLTrajectory().hillsInTheWay.size() != 0) {

			Shot lastBreathShot = getLastBreathShot(currentState, log);

			if (lastBreathShot != null) {
				shot = lastBreathShot;
			}
		}

		if (shot == null) {
			shot = HeartyUtils.findRandomShot(tp, currentState._sling, currentState._birdOnSling);
		}

		return shot;
	}

	/**
	 ** In case the strategies do not find any possible shooting, i.e. blocks in
	 * the way, too far away or too close, than this "random" shot is fired.
	 **/
	private Shot getLastBreathShot(SceneState currentState, LogWriter log) {
		List<ABObject> randomBlocks = new ArrayList<ABObject>();

		int rndNumber = 0;
		int nOfRandomBlockToChoose = (currentState._blocks.size() > 5 ? 5 : currentState._blocks.size());

		for (int i = 0; i < nOfRandomBlockToChoose; i++) {
			rndNumber = randomGenerator.nextInt(currentState._blocks.size());

			if (!randomBlocks.contains(currentState._blocks.get(rndNumber))) {
				randomBlocks.add(currentState._blocks.get(rndNumber));
			} else {
				i--;
			}
		}

		AbstractHeuristic lastBreathHeuristic = new DestroyAsManyPigsAtOnceAsPossibleHeuristic(currentState,
				actionRobot, tp, log);

		return lastBreathHeuristic.getShot();

	}

	/**
	 ** the actual shot is passed to the server
	 ** 
	 * @return the state of the scene after the performed shot
	 **/
	private GameState performTheActualShooting(LogWriter log, SceneState currentState, Shot shot) {
		actionRobot.fullyZoomOut();
		BufferedImage screenshot = actionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		Rectangle _sling = vision.findSlingshotRealShape();

		GameState state = null;
		if (_sling != null) {
			double scale_diff = Math.pow((currentState._sling.width - _sling.width), 2)
					+ Math.pow((currentState._sling.height - _sling.height), 2);

			if (scale_diff < 25) {
				if (shot.getDx() < 0) {
					actionRobot.shoot(shot.getX(), shot.getY(), shot.getDx(), shot.getDy(), 0, shot.getT_tap(), false,
							tp, currentState._sling, currentState._birdOnSling, currentState._blocks,
							currentState._birds, 1);
				}

				try {

					state = actionRobot.checkState();
					log.appendScore(actionRobot.getCurrentScore(), state);
					log.flush(actionRobot.doScreenShot());
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (state == GameState.PLAYING) {
					vision = new Vision(actionRobot.doScreenShot());
					List<Point> traj = vision.findTrajPoints();
					Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy());

					// adjusts trajectory planner
					Rectangle sling = vision.findSlingshotRealShape();
					if (sling != null) {
						tp.adjustTrajectory(traj, vision.findSlingshotRealShape(), releasePoint);
					}

					firstShot = false;
				}
			} else
				System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
		} else
			System.out.println("no sling detected, can not execute the shot, will re-segement the image");

		return state;
	}

	/**
	 ** I/O class for some methods it encapsulates info about the scene regarding
	 * vision
	 **/
	private class visionInfo {
		public Rectangle sling;
		public Vision vision;
		public BufferedImage screenshot;
		public ABType birdOnSling;

		public visionInfo(Rectangle sl, Vision vis, BufferedImage sc, ABType birdie) {
			sling = sl;
			vision = vis;
			screenshot = sc;
			birdOnSling = birdie;
		}
	}

	public static void main(String args[]) {

		HeartyTian2017Main na;
		if (args.length > 0)
			na = new HeartyTian2017Main(args[0]);
		else
			na = new HeartyTian2017Main();
		// na = new HeartyTian2017Main(6);
		na.run();

	}
}

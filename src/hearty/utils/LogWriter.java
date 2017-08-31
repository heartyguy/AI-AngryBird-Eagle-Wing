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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.imageio.ImageIO;

import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;

/**
 * log writer logs information about the game to a file. It stores the
 * information in a csv file.
 */

public class LogWriter {
	private File f = null;
	private PrintWriter out = null;
	private StringBuffer str = null;

	public static int lastScore = 0;
	private String startString = null;
	private int birdCount;

	private BufferedImage startImg;
	private int imgCnt = 1;

	private static final boolean __DEBUG = true;

	public LogWriter(String name) {
		if (__DEBUG) {
			try {

				f = new File(name);
				out = new PrintWriter(new FileWriter(f, true));
				str = new StringBuffer();

				File file = new File("imgCnt");
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String text = null;
				text = reader.readLine();

				imgCnt = Integer.parseInt(text);
				reader.close();

				PrintWriter tmpOut = new PrintWriter(new FileWriter(file));
				tmpOut.print(imgCnt + 1);
				tmpOut.close();

			} catch (Exception ex) {
				System.out.println("LogWriter failed.");
			}
		}

	}

	/**
	 * clears the string and replaces it with the starting string
	 */
	public void clear() {
		if (isAbleToWrite()) {
			str.delete(0, str.length());
			str.append(startString);
		}
	}

	/**
	 * resets the whole string
	 */
	public void reset() {
		if (isAbleToWrite()) {
			str.delete(0, str.length());
		}
	}

	/**
	 * appends the csv file with the given information in a new cell
	 */
	public void plainAppend(String tmp) {
		if (isAbleToWrite()) {
			str.append(tmp);
		}
	}

	/**
	 * appends the csv file with the given information in a new cell
	 */
	public void append(String tmp) {
		if (isAbleToWrite()) {
			if (str.length() != 0)
				str.append(",");
			str.append(tmp);
		}
	}

	/**
	 * appends the csv file with the given information in a new cell
	 */
	public void append(int tmp) {
		if (isAbleToWrite()) {
			append(Integer.toString(tmp));
		}
	}

	/**
	 * appends the csv file with the given information in a new cell
	 */
	public void append(double tmp) {
		if (isAbleToWrite()) {
			append(Double.toString(tmp));
		}
	}

	/**
	 * appends the csv file with the given information in a new cell
	 */
	public void append(boolean tmp) {
		if (isAbleToWrite()) {
			if (tmp == true)
				append(Integer.toString(1));
			else
				append(Integer.toString(0));
		}
	}

	/**
	 * tries to write the given string information to th provided file
	 * 
	 * @param img
	 *            will be saved alongside the file
	 */
	public void flush(BufferedImage img) {

		if (isAbleToWrite()) {
			// ImgCount
			append(imgCnt);

			writeScreenshotToDiskWithNumber(startImg,
					String.format(System.getProperty("user.dir") + "/img/LogWriter/img%06d_before.png", imgCnt));
			writeScreenshotToDiskWithNumber(img,
					String.format(System.getProperty("user.dir") + "/img/LogWriter/img%06d_after.png", imgCnt));

			try {
				out.println(str);
				out.flush();
			} catch (Exception ex) {
				System.out.println("Log saving failed!");
			}
		}
	}

	/**
	 * tries to write the given string information to th provided file
	 */
	public void flush() {
		if (isAbleToWrite()) {
			try {
				out.println(str);
				out.flush();
			} catch (Exception ex) {
				System.out.println("Log saving failed!");
			}
		}
	}

	/**
	 * appends the score of the turn there are three types of scores: score
	 * increase,score increase without pigs and the TotalScore
	 */
	public void appendScore(int score, GameState state) {
		if (isAbleToWrite()) {
			// Change,Change W/O pigs,TotalScore
			int change = score - lastScore;
			if (score == -1)
				change = -1;
			append(Integer.toString(change));
			if (state == GameState.WON) {
				change -= getDeduct();
				while (change < 5000) {
					change += 10000;
				}
			}

			append(Integer.toString(change));
			append(Integer.toString(score));
			lastScore = score;
		}
	}

	public int getDeduct() {
		return (birdCount - 1) * 10000;
	}

	/**
	 * appends information about the start of the level.
	 */
	public void appendStartLevel(List<ABObject> pigs, List<ABObject> birds, ABType birdOnSling) {
		if (isAbleToWrite()) {
			birdCount = birds.size();
			append(pigs.size());
			append(birdCount);
			append(birdOnSling.toString());
			startString = str.substring(0);
		}
	}

	/**
	 * @return an array that represents the type counts
	 */
	private int[] getTypes(List<ABObject> blocks) {
		final int total = ABType.values().length;
		int[] types = new int[total];
		for (int i = 0; i < total; ++i) {
			types[i] = 0;
		}

		for (ABObject obj : blocks) {
			types[obj.type.id]++;
		}

		return types;
	}

	/**
	 * append the counts of different objects
	 */
	public void processBlocks(List<ABObject> blocks, int start, int endExclusive) {
		if (isAbleToWrite()) {
			int[] types = getTypes(blocks);
			/*
			 * Ice(10), Wood(11), Stone(12), RedBird(4), YellowBird(5),
			 * BlueBird(6), BlackBird(7), WhiteBird(8),
			 */
			for (int tmp = start; tmp < endExclusive; ++tmp)
				append(types[tmp]);
		}
	}

	/**
	 * appends information about the start of the level
	 */
	public void appendStartLevel(int currentLevel, List<ABObject> pigs, List<ABObject> birds, List<ABObject> blocks,
			List<ABObject> hills, ABType birdOnSling) {
		if (isAbleToWrite()) {
			// CurrentLevel,StartLevelPigCount,StartLevelBirdCount,birdOnSling,StartLevelRedCount,StartLevelYellowCount,StartLevelBlueCount,StartLevelBlackCount,StartLevelWhiteCount,StartLevelBlockCount,StartLevelIceCount,StartLevelWoodCount,StartLevelStoneCount
			birdCount = birds.size();
			append(currentLevel);
			append(pigs.size());
			append(birdCount);

			append(birdOnSling.id);
			processBlocks(birds, 4, 9);
			append(blocks.size());
			processBlocks(blocks, 10, 13);

			startString = str.substring(0);
		}
	}

	/**
	 * fills the given number of cells with blanks
	 */
	public void fillWithBlanks(int noItems, int oneItem) {
		if (isAbleToWrite()) {
			for (int i = 0; i < noItems * oneItem; ++i) {
				append("");
			}
		}
	}

	/**
	 * saves the starting img
	 */
	public void saveStart(BufferedImage img) {
		startImg = img;
	}

	/*
	 * writes screenshot to disk
	 *
	 * @param screenshot - screenshot to save path - path, where the image is
	 * going to be saved
	 */
	public static void writeScreenshotToDiskWithNumber(BufferedImage screenshot, String imgFilename) {

		if (__DEBUG) {
			System.out.println("saving image to " + imgFilename);

			try {
				ImageIO.write(screenshot, "png", new File(imgFilename));
			} catch (Exception e) {
				System.err.println("failed to save image " + imgFilename);
				e.printStackTrace();
			}
		}
	}

	private boolean isAbleToWrite() {
		if (__DEBUG && f != null && out != null && str != null)
			return true;
		else
			return false;
	}
}
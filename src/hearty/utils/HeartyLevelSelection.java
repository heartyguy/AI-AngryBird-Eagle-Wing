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

import ab.demo.other.ClientActionRobotJava;

/**
 ** this class selects the competition levels based on how well the opponents are
 * playing
 **/
public class HeartyLevelSelection {
	public int[] solved;
	public int[] _myScores;
	public int[] _globalScores;
	public boolean allLevelsPlayed = false;
	public boolean allLevelsSolved = false;
	private int[] _blacklist;
	private int _levelsCount;
	public static byte currentLevel = 1;

	public HeartyLevelSelection(byte[] info, ClientActionRobotJava actionRobot) {
		solved = new int[info[2]];
		_blacklist = new int[info[2]];
		_levelsCount = info[2];

		if (info[0] == 2) {
			allLevelsPlayed = true;
		}

		// Check my score
		_myScores = actionRobot.checkMyScore();
		_globalScores = actionRobot.checkScore();

		int count = 0;
		for (int i = 0; i < _levelsCount; ++i) {
			if (_myScores[i] > 0) {
				solved[i] = 1;
				++count;
			}

		}

		if (count == info[2]) {
			allLevelsPlayed = true;
			allLevelsSolved = true;
		}

		if (allLevelsPlayed == true) {
			currentLevel = (byte) getNextLevel();
		}

	}

	/**
	 ** Statistics must be updated after each shot!
	 **/
	public void updateStats(ClientActionRobotJava actionRobot, boolean WONState) {
		_myScores = actionRobot.checkMyScore();
		_globalScores = actionRobot.checkScore();

		if (WONState) {
			_myScores[currentLevel - 1] = actionRobot.getCurrentScore();
		}

		currentLevel = (byte) getNextLevel();
	}

	/**
	 ** chooses the actual level to be played
	 **/
	private int getLevelFromArrayOnCondition(boolean onlyLevelsWithouBan) {
		int max = 0xffff0000;
		int pickedLevel = -1;

		for (int i = 0; i < _levelsCount; i++) {
			if ((allLevelsSolved || solved[i] == 0) && (!onlyLevelsWithouBan || _blacklist[i] == 0)
					&& _globalScores[i] - _myScores[i] > max) {
				max = _globalScores[i] - _myScores[i];
				pickedLevel = i;
			}
		}

		if (pickedLevel == -1)
			return -1;

		return pickedLevel + 1;
	}

	/**
	 ** updates the blacklist, levels played, etc. after each level
	 **/
	private boolean getStateFromAllScores() {
		// updates solved and unsolved levels
		int count = 0;
		int blackCount = 0;

		if (allLevelsPlayed == true) {
			_blacklist[currentLevel - 1] = 1;
		}

		for (int i = 0; i < _levelsCount; i++) {

			if (_myScores[i] > 0) {
				solved[i] = 1;
				++count;
			}

			if ((_globalScores[i] - _myScores[i] == 0 && _globalScores[i] != 0) || (_myScores[i] > _globalScores[i])) {
				_blacklist[i] = 1;
			}

			if (_blacklist[i] == 1) {
				blackCount++;
			}

		}

		if (blackCount == _levelsCount) {

			_blacklist = new int[_levelsCount];

			for (int i = 0; i < _levelsCount; ++i) {
				if ((_globalScores[i] - _myScores[i] == 0 && _myScores[i] != 0) || (_myScores[i] > _globalScores[i])) {
					_blacklist[i] = 1;
				}
			}

			allLevelsSolved = true;
			allLevelsPlayed = true;

		} else if (count == _levelsCount) {
			allLevelsSolved = true;
			allLevelsPlayed = true;
		} else if (currentLevel == _levelsCount) {

			allLevelsPlayed = true;
		}

		return true;
	}

	/**
	 ** tries to first choose played, non-finished levels, and if all levels are
	 * finished, it chooses the finished levels.
	 **/
	private int getLevelForEitherSolvedOrPlayed() {
		int retValue = -1;

		int tmp = getLevelFromArrayOnCondition(true);

		if (tmp != -1)
			retValue = tmp;
		else
			retValue = getLevelFromArrayOnCondition(false);

		return retValue;

	}

	/**
	 ** wrapper method that is called when the meta-agent wants to play the next
	 * level
	 **/
	public int getNextLevel() {
		int level = 0;
		int retValue = -1;

		if (currentLevel < 1 || currentLevel > _levelsCount) {
			return 1;
		}

		if (getStateFromAllScores() == false) {
			return (int) (Math.random() * _myScores.length + 1);

		}

		// not all Levels were played, get the next level
		if (allLevelsPlayed == false) {
			retValue = currentLevel + 1;
		} else {
			retValue = getLevelForEitherSolvedOrPlayed();

		}

		if (retValue < 1 || retValue > _levelsCount) {
			return (int) (Math.random() * _myScores.length + 1);
		}

		return retValue;
	}
}
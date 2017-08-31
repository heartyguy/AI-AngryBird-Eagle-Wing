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
package hearty.heuristics;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import ab.vision.ABObject;
import ab.vision.ABType;

/**
 ** this class encapsulates the info about the scene that is used later in the
 * different strategies - position of birds, hills, pigs, blocks, etc.
 **/
public class SceneState {
	public final List<ABObject> _birds;

	public final List<ABObject> _pigs;

	public final List<ABObject> _hills;

	public final List<ABObject> _blocks;

	public final Rectangle _sling;

	public final List<ABObject> _TNTs;

	public final ABType _birdOnSling;

	public Point _prevTarget;

	public boolean _firstShot;

	public SceneState(List<ABObject> pigs, List<ABObject> hills, List<ABObject> blocks, Rectangle sling,
			List<ABObject> TNTs, Point prevTarget, boolean firstShot, List<ABObject> birds, ABType birdOnSling) {
		_birds = birds;
		_birdOnSling = birdOnSling;

		_pigs = pigs;
		_hills = hills;
		_blocks = blocks;
		_sling = sling;
		_TNTs = TNTs;

		_prevTarget = prevTarget;
		_firstShot = firstShot;
	}
}
package hearty.heuristics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ab.demo.other.ClientActionRobot;
import ab.planner.TrajectoryPlannerHeartyTian;
import ab.vision.ABObject;
import ab.vision.ABType;
import hearty.heuristics.RoundStoneHeuristic.microHeuristic;
import hearty.heuristics.RoundStoneHeuristic.microHeuristicType;
import hearty.utils.HeartyTrajectory;
import hearty.utils.LogWriter;

/**
 * Destroy as many block as possible/penetrate strategy.
 * tries to find a trajectory that penetrates as much of the structure as possible.
 * Opposite to destroy as many pigs at once, it tries to score high points by destroy many blocks
 * and may in the process collapse large buildings
 * Skip red and white bird as they do not destroy the blocks according to trajectory
 */

public class PenetrationHeuristic extends AbstractHeuristic {
	
	private ArrayList<ABObject> destroyableBlocks = null;
	public final static Map<ABType, List<ABType>> BirdDestroyableBlockMap;
	
    static {
    	Map<ABType, List<ABType>> BirdDestroyableBlockMapTemp = new EnumMap<ABType, List<ABType>>(ABType.class);
    	List<ABType> blueList = new ArrayList<ABType>(Arrays.asList(ABType.Pig, ABType.TNT, ABType.Ice));
    	List<ABType> yellowList = new ArrayList<ABType>(Arrays.asList(ABType.Pig, ABType.TNT, ABType.Wood));
    	List<ABType> blackList = new ArrayList<ABType>(Arrays.asList(ABType.Pig, ABType.TNT, ABType.Wood, ABType.Ice, ABType.Stone));
    	BirdDestroyableBlockMapTemp.put(ABType.BlueBird, blueList);
    	BirdDestroyableBlockMapTemp.put(ABType.YellowBird, yellowList);
    	BirdDestroyableBlockMapTemp.put(ABType.BlackBird, blackList);
        BirdDestroyableBlockMap = Collections.unmodifiableMap(BirdDestroyableBlockMapTemp);
    }
    
    

	public PenetrationHeuristic(SceneState currentState, ClientActionRobot actionRobot, TrajectoryPlannerHeartyTian tp,
			LogWriter lg) {
		super(currentState, actionRobot, tp, lg);
		_utility = estimateUtility();
	}
	
	/**
	 * Performs the calculation of the trajectory utility.
	 */
	@Override
	protected int estimateUtility() {
		// some of these values need to be tuned
		_possibleDLTrajectories = new ArrayList<HeartyTrajectory>();
		
		// create list with destroyable blocks
		destroyableBlocks = new ArrayList<ABObject>();

		// skip for white bird and red bird
		if (_currentState._birdOnSling == ABType.WhiteBird || _currentState._birdOnSling == ABType.RedBird)
			return 0xffff0000;

		// find circles and assign microHeuristics by the number of pigs
		// probably affected
		for (ABObject tmpObj : _currentState._blocks) {
			findDestroyableBlocks(_currentState._birdOnSling, tmpObj);
		}

		// try to find a release point
		for ( ABObject db : destroyableBlocks) {
			// get primary target point
			Point tmpTargetPoint = db.getCenter();
			

			tmpTargetPoint = new Point(tmpTargetPoint.x, tmpTargetPoint.y);

			// estimate launch point
			ArrayList<Point> pts = _tp.estimateLaunchPoint(_currentState._sling, tmpTargetPoint, _currentState._hills,
					_currentState._blocks, db, _currentState._birdOnSling);

			for (Point tmpReleasePoint : pts) {
				// add trajectory to possible trajectories
				_possibleDLTrajectories
						.add(preparePenetrationTrajectory(db, tmpTargetPoint, tmpReleasePoint,_currentState._birdOnSling ));
			}
		}

		// returns int min when no traj or no destroyable block is found
		if (_possibleDLTrajectories.size() == 0 || destroyableBlocks.size() == 0)
			return 0xffff0000;

		// sort circles by number of pigs
		Collections.sort(_possibleDLTrajectories, new Comparator<HeartyTrajectory>() {
			@Override
			public int compare(HeartyTrajectory a, HeartyTrajectory b) {
				if (b.numDestroyableBlocksOnTraj == a.numDestroyableBlocksOnTraj)
					return (int) a.targetObject.getCenterX() - (int) b.targetObject.getCenterX();

				return b.numDestroyableBlocksOnTraj - a.numDestroyableBlocksOnTraj;
			}
		});

		_selectedDLTrajectory = selectBestTrajectoryWRTAvailableBirds();

		return _selectedDLTrajectory.heuristicUtility;
	}
	
	/**
	 ** prepares trajectories for different possible targets
	 **/
	private HeartyTrajectory preparePenetrationTrajectory(ABObject tmpTargetObject,
			Point tmpTargetPoint, Point tmpReleasePoint, ABType birdOnSling) {
		// create new instance of HeartyTrajectory
		HeartyTrajectory tmpDLTrajectory = new HeartyTrajectory(_actionRobot, _tp, _currentState._sling,
				_currentState._birdOnSling, tmpReleasePoint, tmpTargetPoint, tmpTargetObject, _currentState._hills,
				_currentState._blocks, _currentState._pigs);
		
		boolean undestructableBlocksInTheWay = false;
		for(ABObject block : tmpDLTrajectory.blocksInTheWay ){
			if(!isDestroyable(birdOnSling,block)){
				undestructableBlocksInTheWay = true;
				break;
			}
		}
		
		List<ABObject> blocksOnTraj = tmpDLTrajectory.estimateObjectsInTheTraj(_currentState._blocks);
		tmpDLTrajectory.numDestroyableBlocksOnTraj = 0;
		for(ABObject blockOnTraj : blocksOnTraj){
			if(isDestroyable(birdOnSling, blockOnTraj)) {
				tmpDLTrajectory.numDestroyableBlocksOnTraj++;
				if(blockOnTraj.type == ABType.Pig || blockOnTraj.type == ABType.TNT) tmpDLTrajectory.numDestroyableBlocksOnTraj++;
			}
			else break;
		}
		
		double destroyedRatio = (double)tmpDLTrajectory.numDestroyableBlocksOnTraj / _currentState._blocks.size();
		
		if(destroyedRatio > 0.25 || (destroyedRatio > 0.15 && _currentState._firstShot))
			tmpDLTrajectory.heuristicUtility = tmpDLTrajectory.numDestroyableBlocksOnTraj * 20;

		// if there is anything that can not be destroyed in the way, this trajectory must be forgotten!
		if (undestructableBlocksInTheWay || tmpDLTrajectory.hillsInTheWay.size() != 0) {
			tmpDLTrajectory.heuristicUtility = 0xffff0000;

			if (tmpDLTrajectory.hillsInTheWay.size() == 0) {
				tmpDLTrajectory.hillsInTheWay.add(tmpDLTrajectory.targetObject);
			}
		}

		return tmpDLTrajectory;
	}


	private void findDestroyableBlocks(ABType birdOnSling, ABObject block) {
		if(isDestroyable( birdOnSling,  block)){
			destroyableBlocks.add(block);
		}
	}
	
	public static boolean isDestroyable(ABType birdOnSling, ABObject block){
		return PenetrationHeuristic.BirdDestroyableBlockMap.get(birdOnSling).contains(block.type);
		
	}
	
	

	/**
	 * Writes to a log file information about the particular heuristic.
	 */

	@Override
	public void writeToLog() {
		// RoundObjectsCount,RoundCircleArea,RoundNoOfPigsBeneathCircle,RoundMicroHeuristicId

		log.append("choosable targets : " + destroyableBlocks.size());
		if(_selectedDLTrajectory != null){
			log.append("  num destroyable blocks on selected traj : " + _selectedDLTrajectory.numDestroyableBlocksOnTraj);
		}
		
	}

	/**
	 * @return ID of the heuristic for log purposes.
	 */
	@Override
	public int getHeuristicId() {
		return 5;
	}
	
}

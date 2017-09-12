## Description: 

a **multiple strategy affordance based structural analysis** that gives multiple decisions and a manually tuned utility to decide between them. Particular attention is paid to buildings, with unique strategies for different types and shapes of buildings. The utility function is learned through the machine learning method xgboost at first using thousands of shots in the first 42 levels in Poached Eggs, and then rewritten in normal programming for much simpler (i.e., tunable) and faster calculation. 

The agent is based on the open sourced version of the agent from Team Datalab in the 2014 competition.  However, Team Datalab literally left out most of important parts in each of their strategies, only leaving behind a general structure to follow. I went on to re-imagine the strategies, made many additions and upgrades, added another strategy and rewrote the utility function entirely.

## 5 strategies:

1. **Pigshooter**, the bread and butter strategy. It will try to find the trajectory that either targets an unprotected pig or one that includes mutliple pigs on the trajectory. It can make great shots in the
beginning and make neat finishing touches at the end.

2. **Destroy TNT**. Sometimes there are TNT on the map that can devastate a large region. Aim for them.

3. **Destroy as many blocks as possible**. For bluebirds, blackbirds, whitebirds, and yellow birds, they can destroy many ice, stone, wood and wood blocks respectively in their paths.

4.Destroy objects close to **high round objects**(preferably large stones) to release them.

5. **Building strategy** All the blocks are then sorted based on their type and relative position in the building. The best block for a given bird on sling is then selected. We also differentiate between three types of buildings:

• Pyramid

• Rectangle

• Skyscraper

## Agent building process:

I wrote a separate program to fire 200 shots aimed at different objects in each of the first 42 levels and collect 200+ current game state and scene features in each before shots scene.  I used the features to train a gradient boosting tree xgboost model with target variable being the score difference between shots.  As I looked at the feature importance graph, I begin to understand the important features and was able to formulate and fill in the details for the 5 strategies. 

As I have the 5 strategies written, I first tuned the tap time as it is very important. I tried over more than 1000+ shots for each bird for each strategy on the 42 levels and used the average of the tap time of the best shots for each birds for each strategy.

Then, I tried to tune the utility using xgboost by firing thousands of shots for each strategy. The resulting model is relatively straight forward. As it turns out that the situation is almost always super clear which strategy to use in a given scenario. Looking at the decision trees, I simply coded up a very simple utility function for each strategy: if some particular conditions meet, give INT_MAX(choose for sure). If some other conditions meet, give INT_MIN(never choose). Else give a default score or a score that takes into account some features in the current scene. I imagine that my utility scores are very very different from DataLab agent’s utility score. 

Finally, I noticed that a small portion of the shots just misses the pivotal points(weak points in a structure, a small pigs, a small round object) by a tiny margin but adjusting the trajectory planning code for their sake would make other shots significantly worse. Thus, I implemented a logic where for levels that have been tried once, add a small random factor to some trajectories. This is a crude way of adding some adaptation and some non determinism into the agent for improving score on retries.

## Location of important files:

**/src/ab/demo/HeartyTian2017MainEntry.java**   

the entry point for the agent.  It parse inputs and start the agent.

**/src/ab/demo/HeartyTian2017Main.java**     

The logics of the agent. It chooses levels, call different heuristics, choose heurstics.

**/src/ab/demo/other/ClientActionRobotJava.java**   

the interface that the agent use to communicate with the server to perform actions on the game and to receive scene snapshots and information

**/src/ab/planner/TrajectoryPlannerHeartyTian.java**  

improved trajectory planner

**/src/ab/utils/ABUtilHeartyTian.java**   

Many common utility functions to facilitate the operation of the heuristics

**/src/ab/vision/ABObject.java**          

the class that describes each object in the game.  Heavily modified to fit our heuristics

**/src/ab/vision/Vision.java**           

Vision module that recognizes the object and type.  Somewhat modified.

**/src/hearty/heuristics/AbstractHeuristic.java**    

the base class of heuristic, contains lots of logics that are common to all heuristics

**/src/hearty/heuristics/BuildingHeuristic.java**   

heuristic for toppling 3 types of buildings

**/src/hearty/heuristics/DestroyAsManyPigsAtOnceAsPossibleHeuristic.java**  

default heuristic.  aims to destroy as many pigs as possible in one shot

**/src/hearty/heuristics/DynamiteHeuristic.java**      

heuristic for triggering TNTs

**/src/hearty/heuristics/PenetrationHeuristic.java**     

heuristic for destroying as many blocks as possible in one shot

**/src/hearty/heuristics/RoundStoneHeuristic.java**      

heuristic for triggering roundstones to fall/roll onto pigs

**/src/hearty/heuristics/SceneState.java**           

container for many information on the current scene, used by all heuristics

**/src/hearty/utils**    

this package contains many utility classes that help with different heuristics

**/src/hearty/utils/HeartyLevelSelection.java**     

logic for level selection before playing each level

## Area for improvements:

Watch for **Can't tell you everything... ;)**  This indicates that team DataLab omits certain important code logics at the location. I already filled in most of them. I did not fill in the remaing ones as they are not neccessary for my agent.

Watch for **Can't tell you everything**     This indicates I omit some parts that were actually in the Eagle's Wings 2017. These are in only a couple of places and have logic that work reasonably well.  You can tune these yourself. 

Add new strategies or tune the utility or tune the level selection scheme or tune the trajectory planner or tune the building construction process or change the logic in each heurstics

![alt text](/docs/eagle.png)

# AI-AngryBird-Eagle-Wings
*Eagle's Wings* is an artificial player for the popular video game Angry Birds; it participated and represented [University of Waterloo](https://uwaterloo.ca/) and [Zazzle Inc.](https://www.zazzle.com/) in the 2016 and the 2017 [Angry Birds AI Competitions](http://aibirds.org).  

*Eagle's Wings* is the [reigning champion of the competition](http://aibirds.org/angry-birds-ai-competition/competition-results.html)!  

It employs a simple multi-strategy affordance based structural analysis with a manually tuned utility to decide between the strategies. It was develped on top of a fork of the agent from Team DataLab, the champion in 2014. Both the *tactic gameplay* and the *strategic gameplay* are coded in java. Major efforts were made to employ machine learning method [xgboost](https://github.com/dmlc/xgboost) and deep reinforcement learning. Insights learned from xgboost models were used to formulate the strategies. Key values and utility are learned from 40k shots. Read the description at [Eagle's Wing pdf](/docs/eaglewing2017.pdf).

## Running the agent (if you have not changed anything)

 1. Open Chrome on the webpage http://chrome.angrybirds.com and make sure you are using the **SD version** of the game 
 1. Currently, http://chrome.angrybirds.com is taken down, please refer to http://aibirds.org/basic-game-playing-software/chrome-issues.html  (require team registration and login)
 1. `java -jar ABServer.jar`
 1. Open another instance of the terminal
 1. `java -jar ABSoftware.jar`

---

## Running the agent while you are developing


 1. `ant jar` (make sure you have installed [Apache Ant](http://ant.apache.org/manual/install.html))
 2. Follow the steps of the above section (**Running the agent**)
 
ABSoftware.jar is all you need for running

## Developer guide

Refer to [Developer Guide](/docs/developerguide.md) for tuning and strategy outline


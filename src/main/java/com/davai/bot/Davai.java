package com.davai.bot;

import robocode.Robot;

//import java.awt.Color;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Davaibot - a robot by (your name here)
 */
public class Davai extends Robot {

    private int battleFieldScale = 36;
    // Rewards get added to action 10 ticks ago
    private int rewardDelay = 10;

    private int learningTickInterval = 5;

    private int penalty;

    private double[][] stateHistory;
    private int[] actionHistory;
    private int[] rewardHistory;

    /**
     * run: Davaibot's default behavior
     */
    public void run() {
        // set up the neural network
        // Initialization of the robot should be put here
        // After trying out your robot, try uncommenting the import at the top,
        // and the next line:

        while (true) {
            turnRight(360);
        }
    }
}

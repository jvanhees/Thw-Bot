package com.thw.bot;

import com.thw.bot.Neural.Layer;
import robocode.*;

import java.util.ArrayList;

//import java.awt.Color;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * TeamHoekscheWaardBlaffer - a robot by (your name here)
 */
public class TeamHoekscheWaardBlaffer extends AdvancedRobot {

    // Rewards get added to action 10 ticks ago
    private int rewardDelay = 0;

    private NetworkInterface blaffernet;

    // Add history only on certain ticks
    private int learningTickInterval = 1;

    private int inputLength = 5;
    private int reconcileDelay = 20;

    private boolean initiatedNet = false;

    private int penalty;

    private ArrayList<double[]> stateHistory = new ArrayList<>();
    private ArrayList<Integer> actionHistory = new ArrayList<>();
    private ArrayList<Integer> rewardHistory = new ArrayList<>();

    double enemyHeading;
    double enemyDistance;

    /**
     * run: TeamHoekscheWaardBlaffer's default behavior
     */
    public void run() {
        // set up the neural network

        if (!initiatedNet) InitNet();

        // Initialization of the robot should be put here
        // After trying out your robot, try uncommenting the import at the top,
        // and the next line:

        while (true) {
            // Gameloop
            double[] ownPosition = getInputData();

            // Get action from net
            int action = blaffernet.GetAction(ownPosition);

            // Only add learning data when we are in the tick interval
            if (getTime() % learningTickInterval == 0) {
                // Get reward that happened in this ticks
                int reward = calcuteReward();

                stateHistory.add(ownPosition);
                actionHistory.add(action);
                rewardHistory.add(reward);

                // Process this ticks reward for the action x ticks ago
                penalty = 0;
            }

            doAction(action);
        }
    }

    public double[] getInputData() {
        double ownX = getX() / getBattleFieldWidth();
        double ownY = getY() / getBattleFieldHeight();
        double ownHeading = getHeading() / 360;
        double normEnemyDistance = enemyDistance / Math.sqrt(getBattleFieldHeight() * getBattleFieldWidth());
        double normEnemyHeading = enemyHeading / 360;

        return new double[]{ownX, ownY, ownHeading, normEnemyDistance, normEnemyHeading};
    }

    public void doAction(int action) {
        switch (action) {
            case 0:
                turnRadarRight(360);
                break;
            case 1:
                ahead(10);
                break;
            case 2:
                back(10);
                break;
            case 3:
                turnRight(10);
                break;
            case 4:
                turnLeft(10);
        }
    }

    public int calcuteReward() {
        int reward = 3;
        reward -= penalty;
        return reward;
    }


    void InitNet() {
        initiatedNet = true;
        // + 4 for own posX , posY , rotation
        // NOT NOW + 4 for enemy posX , posY , rotation
        int[] HiddenLayers = new int[]{inputLength, 10, 5};

        int[] ActivationFunctions = new int[]{Layer.ACTIVATION_SIGMOID, Layer.ACTIVATION_SIGMOID, Layer.ACTIVATION_SIGMOID};

        try {
            blaffernet = new NetworkInterface(.99f, 1d, inputLength, HiddenLayers, ActivationFunctions);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // Replace the next line with any behavior you would like
        // fire(1);
        enemyHeading = e.getHeading();
        enemyDistance = e.getDistance();
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // Replace the next line with any behavior you would like
        // back(10);
        penalty += 2;
    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    public void onHitWall(HitWallEvent e) {
        // Replace the next line with any behavior you would like
        // back(20);
        penalty += 1;
    }

    public void onRoundEnded(RoundEndedEvent e) {
        // Train the neural network.

        double[][] states = new double[stateHistory.size()][inputLength];
        for (int i = 0; i < states.length; i++) {
            states[i] = stateHistory.get(i);
        }

        int[] actions = actionHistory.stream().mapToInt(d -> d).toArray();
        int[] rewards = rewardHistory.stream().mapToInt(d -> d).toArray();

        blaffernet.train(states, actions , rewards, rewardDelay);

        // Clear all history
        actionHistory.clear();
        rewardHistory.clear();
        stateHistory.clear();

        if (getRoundNum() % reconcileDelay == 0) {
            blaffernet.ReconcileNetworks(getRoundNum());
        }
    }

    public void onBattleEnded(BattleEndedEvent e) {
        blaffernet.SaveNetwork("layers.cfg");
    }
}

package com.thw.bot;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import robocode.*;

//import java.awt.Color;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * TeamHoekscheWaardBlaffer - a robot by (your name here)
 */
public class TeamHoekscheWaardBlaffer extends AdvancedRobot {

    private int battleFieldScale = 36;
    // Rewards get added to action 10 ticks ago
    private int rewardDelay = 10;

    private NetworkInterface blaffernet;

    private int learningTickInterval = 5;

    private int penalty;

    private double[][] stateHistory;
    private int[] actionHistory;
    private int[] rewardHistory;



    /**
     * run: TeamHoekscheWaardBlaffer's default behavior
     */
    public void run() {
        // set up the neural network
        InitNet();

        // Initialization of the robot should be put here
        // After trying out your robot, try uncommenting the import at the top,
        // and the next line:

        while (true) {
            // Gameloop
            double[] ownPosition = getOwnPosition();

            INDArray inputArray = Nd4j.create(ownPosition );

            // Get action from net
            int action = blaffernet.GetAction(inputArray);
            doAction(action);

            // Only add learning data when we are in the tick interval
            if (getTime() % learningTickInterval == 0) {
                // Get reward that happened in this ticks
                int reward = calcuteReward();

                stateHistory[stateHistory.length] = ownPosition;
                actionHistory[actionHistory.length] = action;
                rewardHistory[rewardHistory.length] = reward;

                // Process this ticks reward for the action x ticks ago
                penalty = 0;
            }
        }
    }

    public double[] getOwnPosition() {
        double ownX = getX() % battleFieldScale;
        double ownY = getY() % battleFieldScale;
        double ownHeading = getHeading();
        double ownVelocity = getVelocity();

        return new double[]{ownX, ownY, ownHeading, ownVelocity};
    }

    public void doAction(int action) {
        switch (action) {
            case 0:
                ahead(100);
                break;
            case 1:
                back(100);
                break;
            case 2:
                turnRight(360);
                break;
            case 3:
                turnLeft(360);
        }
    }

    public int calcuteReward() {
        int reward = 3;
        reward -= penalty;
        return reward;
    }


//    // enemy position as input
//    int enemyX = ((int) getX() % battleFieldScale);
//    int enemyY = ((int) getY() % battleFieldScale);
//    double enemyHeading = getHeading();
//    double enemyVelocity = getVelocity();

    float FrameBuffer[][];

    void InitNet() {
        // + 4 for own posX , posY , rotation
        // NOT NOW + 4 for enemy posX , posY , rotation
        int InputLength = 4;
        int HiddenLayerCount = 150;
        MultiLayerConfiguration conf1 = new NeuralNetConfiguration.Builder()
                .seed(123)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(0.0025)
                .updater(Updater.NESTEROVS).momentum(0.95)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(InputLength).nOut(HiddenLayerCount)
                        .weightInit(WeightInit.XAVIER)
                        .activation("relu")
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation("identity").weightInit(WeightInit.XAVIER)
                        .nIn(HiddenLayerCount).nOut(4).build())
                .pretrain(false).backprop(true).build();


        blaffernet = new NetworkInterface(conf1, 100000, .99f, 1d, 1024, 500, 1024, InputLength, 4);
    }


    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // Replace the next line with any behavior you would like
        // fire(1);
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
        blaffernet.train(stateHistory, actionHistory, rewardHistory, rewardDelay);

        if (e.getRound() % 10 == 0) {
            blaffernet.ReconcileNetworks();
        }
    }

    public void onBattleEnded(BattleEndedEvent e) {
        blaffernet.SaveNetwork("config.json", "layers.cfg");
    }
}

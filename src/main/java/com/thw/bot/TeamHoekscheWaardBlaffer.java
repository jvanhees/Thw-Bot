package com.thw.bot;
import com.dap.dl4j.DeepQNetwork;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import robocode.*;

//import java.awt.Color;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * TeamHoekscheWaardBlaffer - a robot by (your name here)
 */
public class TeamHoekscheWaardBlaffer extends AdvancedRobot
{
    /**
     * run: TeamHoekscheWaardBlaffer's default behavior
     */
    public void run() {
        // Initialization of the robot should be put here
        // After trying out your robot, try uncommenting the import at the top,
        // and the next line:


        // set up the neural network
        InitNet();


        while(true) {

            ahead(100);
            turnGunRight(360);
            back(100);
            turnGunRight(360);



        }
    }










    DeepQNetwork RLNet;
    int size = 4;
    int scale = 3;

    float FrameBuffer[][];

    void InitNet(){

        int InputLength = size*size*2+1 ;
        int HiddenLayerCount = 150 ;
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


        RLNet = new DeepQNetwork(conf1 ,  100000 , .99f , 1d , 1024 , 500 , 1024 , InputLength , 4);
    }










    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // Replace the next line with any behavior you would like
        fire(1);
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // Replace the next line with any behavior you would like
        back(10);
    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    public void onHitWall(HitWallEvent e) {
        // Replace the next line with any behavior you would like
        back(20);
    }
}

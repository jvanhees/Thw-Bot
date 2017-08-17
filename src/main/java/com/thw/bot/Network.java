package com.thw.bot;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Random;

public class Network {

    // The network itself
    private MultiLayerNetwork DeepQ;
    // Double that defines the chance of a random action
    private double Epsilon;
    private Random r;
    private int numActions;

    public Network(MultiLayerConfiguration conf , int replayMemoryCapacity , float discount ,
        double epsilon , int batchSize , int updateFreq , int replayStartSize , int inputLength , int numActions) {

        DeepQ = new MultiLayerNetwork(conf);
        DeepQ.init();

        Epsilon = epsilon;
        r = new Random();
        numActions = numActions;
    }

    int GetAction(INDArray Inputs) {
        INDArray outputs = DeepQ.output(Inputs);
        System.out.print(outputs + " ");

        if(Epsilon > r.nextDouble()) {
            // Return random action
            return r.nextInt(outputs.size(1));
        }

        // Return best action
        return GetActionMax(outputs);
    }


    // Returns the index of the highest value (recommended action)
    int GetActionMax(INDArray outputs) {
        float max = outputs.getFloat(0);
        int action = 0;

        for (int i = 1; i < outputs.length(); i++) {
            if (outputs.getFloat(i) > max) {
                max = outputs.getFloat(i);
                action = i;
            }
        }

        return action;
    }

}

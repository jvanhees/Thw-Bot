package com.thw.bot;

import com.thw.bot.Neural.Network;
import robocode.RobocodeFileOutputStream;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class NetworkInterface {

    private Network DeepQ;
    private Network TargetDeepQ;

    // Double that defines the chance of a random action
    private float discount;
    private double epsilon;
    private Random r;
    private int[] actionLayer;
    private int[] activationFunctions;
    private int numActions;

    public NetworkInterface(float discount , double epsilon , int inputLength , int[] layers , int[] activationFunctions) throws Exception {

        // The network itself
        this.DeepQ = new Network(
            inputLength,
            layers,
            activationFunctions);

        this.TargetDeepQ = new Network(
            inputLength,
            layers,
            activationFunctions);

        this.actionLayer = actionLayer;
        this.activationFunctions = activationFunctions;
        this.discount = discount;
        this.epsilon = epsilon;
        this.numActions = actionLayer.length;
        r = new Random();
    }

    int GetAction(double[] Inputs) {
        try {
            double[] outputs = DeepQ.calculate(Inputs);

            if(epsilon > r.nextDouble()) {
                // Return random action
                return r.nextInt(numActions);
            }

            // Return best action
            return GetActionMax(outputs);

        } catch (Exception e) {
            return r.nextInt(numActions);
        }
    }


    // Returns the index of the highest value (recommended action)
    int GetActionMax(double[] outputs) {
        double max = outputs[0];
        int action = 0;

        for (int i = 1; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
                action = i;
            }
        }

        return action;
    }

    double FindMaxCertainty(double[] outputs){
        double max = outputs[0];

        for (int i = 1; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
            }
        }

        return max;
    }


    double[][] getOutputs(double[][] inputs) throws Exception {
        double[][] outputs = new double[inputs.length][numActions];
        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = DeepQ.calculate(inputs[i]);
        }

        return outputs;
    }



    public void train(double[][] stateHistory, int[] actionHistory, int[] rewardHistory, int delay) {

        if (delay > stateHistory.length - 1) {
            return;
        }

        double[][] inputs = Arrays.copyOf(stateHistory, stateHistory.length - 1);
        double[][] targetInputs = stateHistory;

        double[][] currOutputs;
        double[][] targetOutputs;
        try {
            currOutputs = getOutputs(inputs);
            targetOutputs = getOutputs(targetInputs);
        } catch (Exception e) {
            System.out.println("Couldn't get outputs for training.");
            System.out.println(e.toString());
            return;
        }

        double targetValues[][] = new double[inputs.length][numActions];

        float TotalError = 0;

        for (int i = 0; i < inputs.length; i++) {
            double[] state = stateHistory[i];
            int action = actionHistory[i];

            int reward = rewardHistory[i];

            int ind[] = { i , action };

            double futureCertainty = 0;
            futureCertainty = FindMaxCertainty(targetOutputs[i]);

            double TargetReward = reward + discount * futureCertainty;

            TotalError += (TargetReward - currOutputs[i][action] * (TargetReward - currOutputs[i][action]));

            targetValues[i][action] = TargetReward;
        }
        System.out.println("Average Error: " + (TotalError / actionHistory.length) );

        try {
            DeepQ.train(1000, epsilon, 0.5, 0, 1, inputs, targetValues);
        } catch (Exception e) {
            System.out.println("Couldn't train network.");
            System.out.println(e.toString());
        }
    }

    void ReconcileNetworks(int roundNumber){
        try {
            TargetDeepQ.setWeights(DeepQ.getWeights());
            this.SaveNetwork("tempLayers_" + roundNumber + ".cfg");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public boolean SaveNetwork(String LayersFileName){
        //Write the network parameters:
        try {
            double[][][] weights = DeepQ.getWeights();
            RobocodeFileOutputStream fileOut = new RobocodeFileOutputStream(LayersFileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(weights);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

//    public boolean LoadNetwork(String LayersFileName){
//        //Load network configuration from disk:
//        try {
//            FileInputStream fileIn = new FileInputStream(LayersFileName);
//            ObjectInputStream in = new ObjectInputStream(fileIn);
//
//        } catch (IOException e1) {
//            System.out.println("Failed to load config");
//            return false;
//        }
//
//        //Load parameters from disk:
//        INDArray newParams;
//        try{
//            DataInputStream dis = new DataInputStream(new FileInputStream(LayersFileName));
//            newParams = Nd4j.read(dis);
//        } catch (FileNotFoundException e) {
//            System.out.println("Failed to load layers");
//            return false;
//        } catch (IOException e) {
//            System.out.println("Failed to load layers");
//            return false;
//        }
//        //Create a MultiLayerNetwork from the saved configuration and parameters
//        DeepQ = new MultiLayerNetwork(confFromJson);
//        DeepQ.init();
//        DeepQ.setParameters(newParams);
//        ReconcileNetworks();
//        return true;
//
//    }

}

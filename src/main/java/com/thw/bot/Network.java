package com.thw.bot;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

public class Network {

    // The network itself
    private MultiLayerNetwork DeepQ;
    private MultiLayerNetwork TargetDeepQ;
    // Double that defines the chance of a random action
    private float discount;
    private double epsilon;
    private Random r;
    private int numActions;
    private int inputLength;

    public Network(MultiLayerConfiguration conf , int replayMemoryCapacity , float discount ,
        double epsilon , int batchSize , int updateFreq , int replayStartSize , int inputLength , int numActions) {

        DeepQ = new MultiLayerNetwork(conf);
        DeepQ.init();
        TargetDeepQ = new MultiLayerNetwork(conf);
        TargetDeepQ.init();
        TargetDeepQ.setParams(DeepQ.params());

        this.discount = discount;
        this.epsilon = epsilon;
        r = new Random();
        inputLength = inputLength;
        numActions = numActions;
    }

    int GetAction(INDArray Inputs) {
        INDArray outputs = DeepQ.output(Inputs);
        System.out.print(outputs + " ");

        if(epsilon > r.nextDouble()) {
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

    float FindMaxCertainty(INDArray outputs){
        float max = outputs.getFloat(0);

        for (int i = 1; i < outputs.length(); i++) {
            if (outputs.getFloat(i) > max) {
                max = outputs.getFloat(i);
            }
        }

        return max;
    }

    INDArray CombineInputs(double[][] states) {
        INDArray retVal = Nd4j.create(states.length , inputLength);
        for(int i = 0; i < states.length ; i++){
            INDArray state = Nd4j.create(states[i]);
            retVal.putRow(i, state);
        }
        return retVal;
    }

    INDArray CombineNextInputs(double[][] states) {
        INDArray retVal = Nd4j.create(states.length - 1, inputLength);
        for(int i = 0; i < states.length ; i++){
            if (i + 1 > states.length - 1) {
                INDArray state = Nd4j.create(states[i + 1]);
                retVal.putRow(i, state);
            }
        }
        return retVal;
    }

    public void train(double[][] stateHistory, int[] actionHistory, int[] rewardHistory, int delay) {
        double[] state;
        int action;
        int reward;

        // Remove last input from input array since we don't have a target
        INDArray inputs = CombineInputs(Arrays.copyOf(stateHistory, stateHistory.length - 1));
        INDArray targetInputs = CombineNextInputs(stateHistory);

        INDArray currOutputs = DeepQ.output(inputs);
        INDArray targetOutputs = TargetDeepQ.output(targetInputs);

        float TotalError = 0;

        for (int i = 0; i < actionHistory.length; i++) {
            state = stateHistory[i];
            action = actionHistory[i];
            reward = rewardHistory[i + delay];

            int ind[] = { i , action };

            float futureCertainty = 0;
            futureCertainty = FindMaxCertainty(targetOutputs.getRow(i));

            float TargetReward = reward + discount * futureCertainty;

            TotalError += (TargetReward - currOutputs.getFloat(ind)) * (TargetReward - currOutputs.getFloat(ind));

            currOutputs.putScalar(ind , TargetReward );
        }
        //System.out.println("Avgerage Error: " + (TotalError / y.length) );

        DeepQ.fit(inputs, currOutputs);
    }

    void ReconcileNetworks(){
        TargetDeepQ.setParams(DeepQ.params());
    }

    public boolean SaveNetwork(String ConfFileName , String LayersFileName){
        //Write the network parameters:
        try{
            DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get(ConfFileName)));
            Nd4j.write(DeepQ.params(),dos);
        } catch (IOException e) {
            System.out.println("Failed to write config");
            return false;
        }

        //Write the network configuration:
        try {
            FileUtils.write(new File(LayersFileName), DeepQ.getLayerWiseConfigurations().toJson());
        } catch (IOException e) {
            System.out.println("Failed to write layers");
            return false;
        }
        return true;
    }

    public boolean LoadNetwork(String ConfFileName, String LayersFileName){
        //Load network configuration from disk:
        MultiLayerConfiguration confFromJson;
        try {
            confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(ConfFileName)));
        } catch (IOException e1) {
            System.out.println("Failed to load config");
            return false;
        }

        //Load parameters from disk:
        INDArray newParams;
        try{
            DataInputStream dis = new DataInputStream(new FileInputStream(LayersFileName));
            newParams = Nd4j.read(dis);
        } catch (FileNotFoundException e) {
            System.out.println("Failed to load layers");
            return false;
        } catch (IOException e) {
            System.out.println("Failed to load layers");
            return false;
        }
        //Create a MultiLayerNetwork from the saved configuration and parameters
        DeepQ = new MultiLayerNetwork(confFromJson);
        DeepQ.init();
        DeepQ.setParameters(newParams);
        ReconcileNetworks();
        return true;

    }

}

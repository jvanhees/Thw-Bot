package com.thw.bot;

import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;

/**
 * Hello world!
 *
 */
public class App
{
    public static QLearning.QLConfiguration THWBOT_QL =
        new QLearning.QLConfiguration(
            123,    //Random seed
            200,    //Max step By epoch
            150000, //Max step
            150000, //Max size of experience replay
            32,     //size of batches
            500,    //target update (hard)
            10,     //num step noop warmup
            0.01,   //reward scaling
            0.99,   //gamma
            1.0,    //td-error clipping
            0.1f,   //min epsilon
            1000,   //num step for eps greedy anneal
            true    //double DQN
        );

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}

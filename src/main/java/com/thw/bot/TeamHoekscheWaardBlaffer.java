package com.thw.bot;
import robocode.*;

//import java.awt.Color;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * TeamHoekscheWaardBlaffer - a robot by (your name here)
 */
public class TeamHoekscheWaardBlaffer extends Robot
{
    /**
     * run: TeamHoekscheWaardBlaffer's default behavior
     */
    public void run() {
        // Initialization of the robot should be put here
        // After trying out your robot, try uncommenting the import at the top,
        // and the next line:

        while(true) {





        }
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

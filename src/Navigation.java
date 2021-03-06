/* Navigation.java
 * This class is used to allow the robot to navigate to specified coordinates using the odometer.
 * 
 * This navigation class was used from lab 3 and modified for the purposes of lab4 to work with the provided odometer.
 * 
 * Written by:
 * Hadi Sayar, Student ID: 260531679 
 * Antonio D'Aversa, Student ID: 260234498
 */

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
//import lejos.nxt.LCD;

import java.util.Stack;

public class Navigation {
	private TwoWheeledRobot robot;

	private Odometer odo;
	NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;
	private boolean isNav = false;
	private double currentX = 0, currentY = 0, currentTheta = 0;
	private double deltaX = 0, deltaY = 0, deltaTheta = 0, heading;
	private static final double PI = Math.PI;
	private double wheelRadii, width;
	private ObjectDetection oDetect;
	UltraDisplay ultra;
	private double[] pos = new double[3];
	private boolean yesItHasBlock = false;

	// Navigation constructor
	public Navigation(Odometer odometer, ObjectDetection oDetect,
			UltraDisplay ultra) {
		this.odo = odometer;
		this.robot = odo.getTwoWheeledRobot();
		this.oDetect = oDetect;
		this.ultra = ultra;
		wheelRadii = 1.978; // wheel radius

		width = 18.62; // width between wheels

		// set the acceleration to prevent slipping.
	}

	// Travel to method which determines the distance that robot needs to travel
	public Stack<Point> travelTo(double x, double y, int sensorThreshold,
			Stack<Point> currentPath) {

		// Travel to method which determines the distance that robot needs to
		// travel

		// Sets Navigation to true
		isNav = true;
		// set the motor speeds
		this.robot.setForwardSpeed(250);
		// clear and write to the LCD
		odo.getPosition(pos);
		// Calculate the heading of the robot and turn towards it
		currentX = this.odo.getX();
		currentY = this.odo.getY();
		deltaX = x - currentX;
		deltaY = y - currentY;
		heading = Math.atan2(deltaX, deltaY);
		turnTo(heading);
		this.odo.getPosition(pos);

		// Until the robot is at its destination within 1 cm, it will move
		// forward in the heading's direction
		while (Math.abs(x - this.odo.getX()) >= 1.0
				|| Math.abs(y - this.odo.getY()) >= 1.0) {

			// if theta is within the threshold go straight
			//if (Math.abs(this.odo.getAng() - heading) < 4) {
				this.robot.getLeftMotor().setSpeed(100);
				this.robot.getRightMotor().setSpeed(100);
				this.robot.getLeftMotor().forward();
				this.robot.getRightMotor().forward();
			//}
			// if not adjust theta by simply using turnto.
			//else {
				//turnTo(heading);
			//}
			if (this.ultra.getDist() < sensorThreshold) {
				double firstHeading = this.odo.getAng();
				double finalHeading = 0;
				int objectI = this.oDetect.identify();
				turnTo(Math.toRadians((this.odo.getAng() - 20.0)));
				finalHeading = this.odo.getAng();
				objectI = this.oDetect.identify();
				turnTo(Math.toRadians(firstHeading));
				if (objectI == 1) {
					double turnToHeading = this.odo.getAng() + 35.0;
					// turnTo(Math.toRadians((this.odo.getAng() + 35.0)));
					currentPath.push(new Point(this.odo.getX() + 30
							* Math.cos(Math.toRadians(turnToHeading)), this.odo.getY() + 30
							* Math.sin(Math.toRadians(turnToHeading)), false));
					yesItHasBlock = true;
					break;
				} else if (objectI == 2) {
					currentPath = avoidObstacle(currentPath);
					break;
				}
			}

			// if not adjust theta by simply using turnto.
			//else {
				//turnTo(heading);
			//}
		}

		// When it exits the loop, STOP
		this.robot.stop(0);
		// 1 second cat-nap
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected
			// that the odometer will be interrupted by another thread
		}

		// sets navigation to false when it gets to destination
		isNav = false;
		return currentPath;
		// UPDATE LCD
		// this.odo.getPosition(pos);
	}

	public boolean hasBlock() {
		return this.yesItHasBlock;
	}

	public Stack<Point> avoidObstacle(Stack<Point> currentPath) {
		currentPath.push(new Point(this.odo.getX(), this.odo.getY() + 30.46,
				false));
		currentPath.push(new Point(this.odo.getX()
				+ Math.cos(Math.toRadians(odo.getAng())) * 60.98, this.odo
				.getY(), false));
		currentPath.push(new Point(this.odo.getX(), this.odo.getY() - 30.46,
				false));
		return currentPath;
	}

	public void turnTo(double theta) {
		isNav = true;

		// Finds current heading according to odometer
		currentTheta = pos[2];
		deltaTheta = theta - Math.toRadians(currentTheta);

		// computes acute angle (Shortest turn)
		if (deltaTheta < -PI) {
			deltaTheta += 2 * PI;
		} else if (deltaTheta > PI) {
			deltaTheta -= 2 * PI;
		}

		// Performs rotation
		deltaTheta = Math.toDegrees(deltaTheta);

		this.robot.getLeftMotor().rotate(
				convertAngle(wheelRadii, width, deltaTheta), true);
		this.robot.getRightMotor().rotate(
				-convertAngle(wheelRadii, width, deltaTheta), false);
	}

	boolean isNavigating() {
		return isNav;
	}

	// Method "borrowed" from SquareDriver.java
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// Method "borrowed" from SquareDriver.java
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}

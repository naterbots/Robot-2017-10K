// RobotBuilder Version: 2.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.


package org.usfirst.frc.team2509.robot.subsystems;

import java.util.ArrayList;
import java.util.Iterator;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team2509.robot.RobotMap;
import org.usfirst.frc.team2509.robot.commands.FilterBoilerTarget;
import org.usfirst.frc.team2509.robot.commands.FilterGearTarget;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


/**
 *
 */
public class Vision extends Subsystem {
	public Command filterGear = new FilterGearTarget();
	public Command filterBoiler = new FilterBoilerTarget();
	public Rect GEAR_TARGET, BOILER_TARGET;
	private ArrayList<MatOfPoint>
		contours = new ArrayList<MatOfPoint>();
	private CvSink 
		SHOOT_SINK = CameraServer.getInstance().getVideo("SHOOTER"),
		GEAR_SINK = CameraServer.getInstance().getVideo("GEAR");
	private final CvSource 
		OUTPUT_STREAM = CameraServer.getInstance().putVideo("ALT-SHOOTER", 640, 480);
	private double
		TARGETSPEED,
		DISTANCE;
	private final Mat
		BINARY = new Mat(),
		CLUSTERS = new Mat(),
		HEIRARCHY = new Mat(),
		HSV = new Mat(),
		SOURCE = new Mat(),
		THRESH = new Mat();
	protected final Scalar 
	//COLOR VALUES
		BLACK = new Scalar(0,0,0),
		BLUE = new Scalar(255, 0, 0),
		GREEN = new Scalar(0, 255, 0),
		RED = new Scalar(0, 0, 255),
		YELLOW = new Scalar(0, 255, 255),
	//Thresholds values
		LOWER_BOUNDS = new Scalar(180,190,40),
		UPPER_BOUNDS = new Scalar(200,210,60);
	private final RobotDrive DT = RobotMap.DRIVETRAIN;
	private final ADXRS450_Gyro GYRO = RobotMap.DT_GYRO;
	private final DigitalInput SWITCH = RobotMap.GEAR_SWITCH;
	
    public void initDefaultCommand() {
    	filterGear.start();
    	filterBoiler.start();
    }
    public Thread FilterBoiler = new Thread(()->{
 	   while(true){
 			contours.clear();
 			RobotMap.SHOOT_CAM.setBrightness(0);
 			SHOOT_SINK.grabFrame(SOURCE);
 			Imgproc.cvtColor(SOURCE, HSV, Imgproc.COLOR_BGR2RGB);
 			Imgproc.threshold(HSV, BINARY, 180, 190, Imgproc.THRESH_BINARY_INV);	
 			Imgproc.cvtColor(BINARY, THRESH, Imgproc.COLOR_HSV2BGR);
 			Imgproc.cvtColor(THRESH, CLUSTERS, Imgproc.COLOR_BGR2GRAY);
 			Mat GRAY = CLUSTERS;
 			Imgproc.Canny(GRAY, HEIRARCHY, 2, 4);
 			Imgproc.findContours(HEIRARCHY, contours, new Mat(),Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
 	        for(MatOfPoint mop :contours){
 				Rect rec = Imgproc.boundingRect(mop);
 				Imgproc.rectangle(SOURCE, rec.br(), rec.tl(), RED);
 			}
 			for(Iterator<MatOfPoint> iterator = contours.iterator();iterator.hasNext();){
 				MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
 				Rect rec = Imgproc.boundingRect(matOfPoint);
 				//float aspect = (float)rec.width/(float)rec.height;
 				//if( rec.height < 10 || rec.width < 5||rec.y<75/*||aspect<=1*/){
 				//	iterator.remove();
 				//continue;
 				//}
 				BOILER_TARGET = rec;
 				DISTANCE = ((rec.width*(-1))+15);
 				TARGETSPEED = Math.sqrt((DISTANCE+2.5)*(0.3048)*(9.8)/(0.85)*(751.9113586737));
 				SmartDashboard.putNumber("S_Contours", contours.size());
 				SmartDashboard.putNumber("S_X", rec.x);
 				SmartDashboard.putNumber("S_Width", rec.width);
 				SmartDashboard.putNumber("S_Height", rec.height);
 				SmartDashboard.putNumber("S_Distance", DISTANCE);
 				SmartDashboard.putNumber("Target Speed", TARGETSPEED);
 			}			
 			OUTPUT_STREAM.putFrame(SOURCE);
 		}
    	});
    public Thread FilterGear = new Thread(()->{
 	   while(true){
   			contours.clear();
   			RobotMap.SHOOT_CAM.setBrightness(0);
   			GEAR_SINK.grabFrame(SOURCE);
   		//	CVSINK.grabFrameNoTimeout(SOURCE);
   			Imgproc.cvtColor(SOURCE, HSV, Imgproc.COLOR_BGR2RGB);
   			Imgproc.threshold(HSV, BINARY, 180, 190, Imgproc.THRESH_BINARY_INV);	
   			Imgproc.cvtColor(BINARY, THRESH, Imgproc.COLOR_HSV2BGR);
   			Imgproc.cvtColor(THRESH, CLUSTERS, Imgproc.COLOR_BGR2GRAY);
   			Mat GRAY = CLUSTERS;
   			//Core.inRange(THRESH	, LOWER_BOUNDS, UPPER_BOUNDS, CLUSTERS);	
   			Imgproc.Canny(GRAY, HEIRARCHY, 2, 4);
   			Imgproc.findContours(HEIRARCHY, contours, new Mat(),Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
   	        for(MatOfPoint mop :contours){
   				Rect rec = Imgproc.boundingRect(mop);
   				Imgproc.rectangle(SOURCE, rec.br(), rec.tl(), RED);
   			}
   			for(Iterator<MatOfPoint> iterator = contours.iterator();iterator.hasNext();){
   				MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
   				Rect rec = Imgproc.boundingRect(matOfPoint);
   				/*if( rec.height < 25 || rec.width < 10){
   					iterator.remove();
   				continue;
   				}*/
   				BOILER_TARGET = rec;
   				SmartDashboard.putInt("Contours", contours.size());
   				SmartDashboard.putInt("X", rec.x);
   				SmartDashboard.putInt("Width", rec.width);
   			}			
   			OUTPUT_STREAM.putFrame(SOURCE);
   			}
    });
}

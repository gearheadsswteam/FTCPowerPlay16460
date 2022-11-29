package org.firstinspires.ftc.teamcode.autonomous;
import static java.lang.Math.*;
import static org.firstinspires.ftc.teamcode.classes.ValueStorage.*;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
@Autonomous(name = "AutonomousBlueRightParkShared", group = "BlueRight")
public class AutonomousBlueRightParkShared extends AbstractAutonomous {
    Pose2d dropPose = new Pose2d(-32, 18, -1);
    Pose2d[] parkPose = new Pose2d[] {new Pose2d(-12, 36, -PI / 2), new Pose2d(-35, 36, -PI / 2), new Pose2d(-57, 36, -PI / 2)};
    TrajectorySequence traj1;
    TrajectorySequence[] traj2;
    double traj1Time;
    boolean startLift = false;
    boolean endTraj1 = false;
    boolean traj1Done = false;
    boolean endTraj2 = false;
    boolean done = false;
    ElapsedTime clock = new ElapsedTime();
    double time;
    @Override
    public void initialize() {
        traj1 = robot.drive.trajectorySequenceBuilder(initPose())
                .setVelConstraint(SampleMecanumDrive.getVelocityConstraint(25, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH))
                .lineTo(new Vector2d(-35, 30))
                .setAccelConstraint(SampleMecanumDrive.getAccelerationConstraint(25))
                .splineTo(dropPose.vec(), dropPose.getHeading())
                .resetConstraints()
                .addTemporalMarker(1, -1.4, () -> {
                    startLift = true;
                })
                .addTemporalMarker(1, 0, () -> {
                    endTraj1 = true;
                    traj1Done = true;
                    traj1Time = clock.seconds();
                })
                .build();
        traj2 = new TrajectorySequence[]{
                robot.drive.trajectorySequenceBuilder(dropPose)
                        .setReversed(true)
                        .splineTo(parkPose[1].vec(), parkPose[1].getHeading() + PI)
                        .setReversed(false)
                        .lineTo(parkPose[0].vec())
                        .build(),
                robot.drive.trajectorySequenceBuilder(dropPose)
                        .setReversed(true)
                        .splineTo(parkPose[1].vec(), parkPose[1].getHeading() + PI)
                        .build(),
                robot.drive.trajectorySequenceBuilder(dropPose)
                        .setReversed(true)
                        .splineTo(parkPose[1].vec(), parkPose[1].getHeading() + PI)
                        .setReversed(false)
                        .lineTo(parkPose[2].vec())
                        .build(),
        };
    }
    @Override
    public void run() {
        clock.reset();
        robot.armProfile = autonomousArmProfile(0);
        robot.wristProfile = autonomousWristProfile(0);
        robot.drive.followTrajectorySequenceAsync(traj1);
        while(opModeIsActive()) {
            time = clock.seconds();
            if (startLift) {
                robot.extendLiftProfile(time, liftHighClose[0], 0);
                robot.extendArmProfile(time, liftHighClose[1], 0);
                robot.extendWristProfile(time, liftHighClose[2], 0);
                startLift = false;
            }
            if (endTraj1) {
                robot.gripper.setPosition(gripperRelease);
                endTraj1 = false;
            }
            if (traj1Done && time - traj1Time > 1) {
                robot.drive.followTrajectorySequenceAsync(traj2[0]);
                robot.extendLiftProfile(time, 0, 0);
                robot.extendArmProfile(time, armIn, 0);
                robot.extendWristProfile(time, wristIn, 0);
                traj1Done = false;
            }
            robot.drive.update();
            robot.update(time);
        }
    }
    @Override
    public int side() {
        return sides.BLUE;
    }
    @Override
    public Pose2d initPose() {
        return new Pose2d(-35, 60, -PI / 2);
    }
}

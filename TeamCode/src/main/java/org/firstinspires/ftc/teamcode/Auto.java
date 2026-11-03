package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx; // Use DcMotorEx for velocity methods
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@Autonomous(name = "Auto_Encoder_Drive", preselectTeleOp = "Dev1")
public class Auto extends LinearOpMode {

    private IMU imu;
    private DcMotorEx motor_right;
    private DcMotorEx motor_left;

    static final double TICKS_PER_REV = 537.7;
    static final double WHEEL_DIAMETER_IN = 3.77953;
    static final double TICKS_PER_INCH = TICKS_PER_REV / (WHEEL_DIAMETER_IN * Math.PI);

    @Override
    public void runOpMode() {
        motor_right = hardwareMap.get(DcMotorEx.class, "motor_right");
        motor_left = hardwareMap.get(DcMotorEx.class, "motor_left");
        imu = hardwareMap.get(IMU.class, "imu");

        motor_left.setDirection(DcMotorSimple.Direction.REVERSE);
        motor_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        motor_left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor_right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motor_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.UP);
        imu.initialize(new IMU.Parameters(orientationOnRobot));
        sleep(500);


        telemetry.addData("Status", "Initialized. Ticks/Inch: %.2f", TICKS_PER_INCH);
        telemetry.update();

        waitForStart();
        imu.resetYaw();

        // auto sequence

        driveInches(10, 0.5);

        generalRotate(87);
        sleep(500);
        driveInches(33.1, 0.5);



//        driveInches(32.5, 0.5);
//
//        generalRotate(92);
//
//        sleep(500);
//
//
//
//        driveInches(20.0, 0.5);
    }


    public void driveInches(double inches, double power) {
        int moveTicks = (int)(inches * TICKS_PER_INCH);
        int leftTarget = motor_left.getCurrentPosition() + moveTicks;
        int rightTarget = motor_right.getCurrentPosition() + moveTicks;

        motor_left.setTargetPosition(leftTarget);
        motor_right.setTargetPosition(rightTarget);

        motor_left.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motor_right.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        motor_left.setPower(power);
        motor_right.setPower(power);

        while (opModeIsActive() && (motor_left.isBusy() && motor_right.isBusy())) {
            telemetry.addData("Speed (in/s)", getSpeedInchesPerSecond());
            telemetry.addData("Left Pos", motor_left.getCurrentPosition());
            telemetry.addData("Target", leftTarget);
            telemetry.update();
        }

        motor_left.setPower(0);
        motor_right.setPower(0);
        motor_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private double getSpeedInchesPerSecond() {
        double avgVelocityTicks = (motor_left.getVelocity() + motor_right.getVelocity()) / 2.0;
        return avgVelocityTicks / TICKS_PER_INCH;
    }

    private double getHeading() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private void rotateCCW(int targetAngle) {
        motor_right.setPower(0.3);
        motor_left.setPower(-0.3);

        while (opModeIsActive() && Math.abs(getHeading()) < targetAngle) {
            telemetry.addData("Target", targetAngle);
            telemetry.addData("Current", getHeading());
            telemetry.addData("Raw Value", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            telemetry.update();
        }

        motor_left.setPower(0);
        motor_right.setPower(0);

    }


    private void rotateCW(int targetAngle) {
        motor_right.setPower(-0.3);
        motor_left.setPower(0.3);

        while (opModeIsActive() && getHeading() > targetAngle) {
            telemetry.addData("Heading", getHeading());
            telemetry.update();
        }

        motor_left.setPower(0);
        motor_right.setPower(0);
    }

    private void generalRotate(double degrees) {
        double startHeading = getHeading();
        double targetHeading = normalizeAngle(startHeading + degrees);

        double turnPower = (degrees > 0) ? 0.3 : -0.3;
        motor_right.setPower(turnPower);
        motor_left.setPower(-turnPower);

        while (opModeIsActive()) {
            double currentHeading = getHeading();
            double error = normalizeAngle(targetHeading - currentHeading);

            telemetry.addData("Target", targetHeading);
            telemetry.addData("Current", currentHeading);
            telemetry.addData("Error", error);
            telemetry.update();

            if (Math.abs(error) < 2.0) {
                break;
            }
        }

        motor_left.setPower(0);
        motor_right.setPower(0);
    }

    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle <= -180) angle += 360;
        return angle;
    }








}
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Robot TeleOp")
public class RobotTeleOP extends LinearOpMode {
    // Motor and servo variables
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftWheel;
    private DcMotor rightWheel;
    private DcMotor intakeMotor;
    private DcMotor flywheelMotor;

    private Servo feederServo;
    private Servo turretServo;

    // Configurable positions for the feeder servo - Change if needbe
    private static final double FEEDER_SERVO_CLOSED_POSITION = 0;
    private static final double FEEDER_SERVO_OPEN_POSITION = 1;

    // Configurable speed for turret - Change if needbe
    private static final double TURRET_SPEED = 0.25;

    // Configurable power for the flywheel and intake - Change if needbe
    private static final double FLYWHEEL_POWER = 1;
    private static final double INTAKE_POWER = 1;

    @Override
    public void runOpMode() {
        // Fetch motors and servos and assign them to their respective hardware as set on the driver station
        leftWheel = hardwareMap.get(DcMotor.class, "leftWheel");
        rightWheel = hardwareMap.get(DcMotor.class, "rightWheel");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        flywheelMotor = hardwareMap.get(DcMotor.class, "flywheelMotor");
        feederServo = hardwareMap.get(Servo.class, "feederServo");
        turretServo = hardwareMap.get(Servo.class, "turretServo");

        // Sets behavior of drive wheels if at 0 power at any time
        leftWheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightWheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initialize motors at zero power and close the feeder gate
        leftWheel.setPower(0);
        rightWheel.setPower(0);
        intakeMotor.setPower(0);
        flywheelMotor.setPower(0);
        feederServo.setPosition(FEEDER_SERVO_CLOSED_POSITION);

        // Prints to driver station
        telemetry.addData("Init", "Robot initialized");
        telemetry.update();

        // Stops program until start button is pressed
        waitForStart();

        // Ends program if it is stopped on the driver station
        if (isStopRequested()) {
            return;
        }

        // Set intake motor and flywheel motor to run at max power continuously - Adjust values as needed
        intakeMotor.setPower(INTAKE_POWER);
        flywheelMotor.setPower(FLYWHEEL_POWER);

        // Declare variable for emergency purposes
        boolean intakeEnabled = true;
        boolean previousX = false;

        // Declare starting turret servo position
        double turretPosition = 0.5;

        // Reset runtime so calculations are accurate
        runtime.reset();

        // Continuously runs when start button is pressed
        while (opModeIsActive()) {
            // Initialize movement variables to joystick input
            // Forward is negative because pushing up on the joystick will give a negative value, and we want up to be positive
            double forward = -(gamepad1.left_stick_y);
            double turn = gamepad1.left_stick_x;

            // This is for dead zone on the joystick - Adjust values if needed
            if (Math.abs(forward) < 0.05) {
                forward = 0;
            }
            if (Math.abs(turn) < 0.05) {
                turn = 0;
            }

            // Slow mode - Adjust values if needed
            if (gamepad1.left_bumper) {
                forward *= 0.4;
                turn *= 0.4;
            }

            // Toggles intake motor power - FOR EMERGENCY PURPOSES ONLY
            if (gamepad1.x && !previousX) {
                intakeEnabled = !intakeEnabled;
            }

            // Variable used to check if x is held
            // We don't want the motor to rapidly turn on and off. We only want it to be toggled
            previousX = gamepad1.x;

            telemetry.addData("Intake is running?", intakeEnabled);

            // Turns off intake motor - FOR EMERGENCY PURPOSES ONLY
            if (!intakeEnabled) {
                intakeMotor.setPower(0);
            } else {
                intakeMotor.setPower(INTAKE_POWER);
            }

            // Shooting mechanism - Activates when right trigger is pressed more than 25%
            if (gamepad1.right_trigger >= 0.25) {
                feederServo.setPosition(FEEDER_SERVO_OPEN_POSITION);
            } else {
                feederServo.setPosition(FEEDER_SERVO_CLOSED_POSITION);
            }

            telemetry.addData("Feeder servo open?", gamepad1.right_trigger >= 0.25);

            // Turret input calculations
            double turretInput = gamepad1.right_stick_x;
            if (Math.abs(turretInput) < 0.05) {
                turretInput = 0;
            }

            double deltaTime = runtime.seconds();
            runtime.reset();

            turretPosition += turretInput * TURRET_SPEED * deltaTime;
            turretPosition = Math.max(0, Math.min(1, turretPosition));

            turretServo.setPosition(turretPosition);

            telemetry.addData("Current turret input", turretInput);
            telemetry.addData("Current turret position", turretPosition);

            // Initialize power and scale variables
            // This section calculates and normalizes power in order to move
            double leftPower = forward + turn;
            double rightPower = forward - turn;
            double scale = Math.max(1, Math.max(Math.abs(leftPower), Math.abs(rightPower)));
            leftPower = leftPower / scale;
            rightPower = rightPower / scale;

            // Set motor power to calculated values
            leftWheel.setPower(leftPower);
            rightWheel.setPower(rightPower);

            // Display current power to driver station
            telemetry.addData("Left wheel power", leftWheel.getPower());
            telemetry.addData("Right wheel power", rightWheel.getPower());
            telemetry.update();
        }

        // Stop motors and return feeder to its closed position when the active loop exits - Remove or comment this code if it causes problems or makes us lose points
        leftWheel.setPower(0);
        rightWheel.setPower(0);
        intakeMotor.setPower(0);
        flywheelMotor.setPower(0);
        feederServo.setPosition(FEEDER_SERVO_CLOSED_POSITION);
    }
}

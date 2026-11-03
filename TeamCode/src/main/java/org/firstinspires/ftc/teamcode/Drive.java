package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp
public class Drive extends LinearOpMode {

    private DcMotor left_wheel;
    private DcMotor right_wheel;

    float turn;
    double forwardSpeed;
    double leftPower = 0;
    double rightPower = 0;


    @Override
    public void runOpMode() {
        left_wheel = hardwareMap.get(DcMotor.class, "left_wheel");
        right_wheel = hardwareMap.get(DcMotor.class, "right_wheel");

        waitForStart();

        left_wheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        right_wheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        while (opModeIsActive()) {
            forwardSpeed = gamepad1.left_stick_x;
            turn = gamepad1.left_stick_y;
        }
    }
}
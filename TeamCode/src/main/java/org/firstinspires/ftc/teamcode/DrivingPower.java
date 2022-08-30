package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * This is a simple teleop routine for testing localization. Drive the robot around like a normal
 * teleop routine and make sure the robot's estimated pose matches the robot's actual pose (slight
 * errors are not out of the ordinary, especially with sudden drive motions). The goal of this
 * exercise is to ascertain whether the localizer has been configured properly (note: the pure
 * encoder localizer heading may be significantly off if the track width has not been tuned).
 */
@TeleOp(group = "drive")
public class DrivingPower extends OpMode {

    ElapsedTime runtime = new ElapsedTime();

    DcMotorEx left = null;
    DcMotorEx right = null;
    DcMotorEx plate = null;
    DcMotorEx lift = null;
    DcMotorEx hook = null;
    Servo front = null;

    @Override
    public void init() {
        left = hardwareMap.get(DcMotorEx.class, "left");
        right = hardwareMap.get(DcMotorEx.class, "right");
        plate = hardwareMap.get(DcMotorEx.class, "plate");
        lift = hardwareMap.get(DcMotorEx.class, "lift");
        hook = hardwareMap.get(DcMotorEx.class, "hook");
        front = hardwareMap.get(Servo.class, "servo");

        left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        plate.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        hook.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        plate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hook.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        left.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        right.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hook.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        right.setDirection(DcMotorSimple.Direction.REVERSE);
        front.setDirection(Servo.Direction.REVERSE);
        hook.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void start() {
        front.setPosition(0);

        runtime.reset();
    }

    boolean climbing = false;

    enum DriveMode {
        AUTO,
        MANUAL
    }

    DriveMode driveMode = DriveMode.MANUAL;

    @Override
    public void loop() {
        float sp = gamepad1.right_bumper ? 0.4f : 1f;
        float ms = gamepad1.left_bumper ? 1f : 0.8f;

        float leftPower = (-gamepad1.left_trigger + gamepad1.right_trigger - gamepad1.left_stick_y) * sp * ms;
        float rightPower = (gamepad1.left_trigger - gamepad1.right_trigger - gamepad1.left_stick_y) * sp * ms;

        //float leftPower = (gamepad1.left_stick_x - gamepad1.left_stick_y) * sp * ms;
        //float rightPower = (-gamepad1.left_stick_x - gamepad1.left_stick_y) * sp * ms;

        debugTelemetry();

        if (gamepad1.a)
            upPlate();
        else
            downPlate();

        left.setPower(leftPower);
        right.setPower(rightPower);

        //carlig sus 33005 25009
        //lift sus 20162 20733

        //switch lift 18479 carlig 21823

        if (gamepad2.y) {
            climbing = true;
            driveMode = DriveMode.AUTO;
        } else if (gamepad2.x) {
            climbing = false;
            driveMode = DriveMode.AUTO;
        }

        if (gamepad2.left_stick_y != 0 || gamepad2.right_stick_y != 0)
            driveMode = DriveMode.MANUAL;


        if (driveMode == DriveMode.AUTO) {
            if (climbing) {
                if (hook.getCurrentPosition() < 25009) //max
                    hook.setPower(1);
                else
                    hook.setPower(0);
                if (lift.getCurrentPosition() < 20100) //max
                    lift.setPower((lift.getCurrentPosition() < 10000) ? .95f : .75f); //nivel schimbare
                else
                    lift.setPower(0);
            } else if (gamepad2.x) {
                lift.setPower((lift.getCurrentPosition() < 10000) ? -.95f : -.75f); //nivel schimbare
                hook.setPower(-1f);
            } else {
                lift.setPower(0);
                hook.setPower(0);
            }
        } else { // manual
            lift.setPower(-gamepad2.left_stick_y);
            hook.setPower(-gamepad2.right_stick_y);
        }

        //reset pozitie 0
        if (gamepad2.share) {
            lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            hook.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            hook.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

    }

    boolean waitingLift = false, lifted = false;

    private void upPlate() {
        front.setPosition(0.15);
        if (!waitingLift && !lifted) {
            waitingLift = true;
            runtime.reset();
        }
        if (waitingLift && runtime.milliseconds() >= 300 && !lifted)
            waitingLift = false;
        if (!waitingLift) {
            lifted = true;
            plate.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            if (plate.getCurrentPosition() < 70)
                plate.setPower(0.5);
            else plate.setPower(0);
        }
    }

    private void downPlate() {
        lifted = false;
        front.setPosition(0);

        plate.setTargetPosition(0);
        plate.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        plate.setPower(1);
    }

    private void debugTelemetry() {
        telemetry.addData("driveMode", driveMode);
        telemetry.addData("placa", plate.getCurrentPosition());
        telemetry.addData("front", front.getPosition());
        telemetry.addData("lift", lift.getCurrentPosition());
        telemetry.addData("carlig", hook.getCurrentPosition());
        telemetry.update();
    }

}

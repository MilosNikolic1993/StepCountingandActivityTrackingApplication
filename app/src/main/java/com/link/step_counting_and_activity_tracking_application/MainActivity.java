package com.link.step_counting_and_activity_tracking_application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "StepPrefs";
    private static final String STEP_COUNT_KEY = "stepCount";
    private static final String GOAL_KEY = "goal";

    private TextView stepCountTextView;
    private TextView caloriesTextView;
    private TextView activityTextView;
    private TextView waterIntakeTextView;
    private TextView goalTextView;
    private TextView statusTextView;
    private EditText goalEditText;
    private Button addWaterButton;
    private Button setGoalButton;
    private Button resetButton;
    private Button shareButton;
    private Button startStepCountingButton;
    private Button exitButton;
    private int stepCount = 0;
    private int runningCount = 0;
    private int cyclingCount = 0;
    private int waterIntake = 0;
    private int goal = 10000;
    private double calories = 0;

    private BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stepCount = intent.getIntExtra("stepCount", 0);
            saveStepCount();
            updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepCountTextView = findViewById(R.id.stepCountTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        activityTextView = findViewById(R.id.activityTextView);
        waterIntakeTextView = findViewById(R.id.waterIntakeTextView);
        goalTextView = findViewById(R.id.goalTextView);
        statusTextView = findViewById(R.id.statusTextView);
        goalEditText = findViewById(R.id.goalEditText);
        addWaterButton = findViewById(R.id.addWaterButton);
        setGoalButton = findViewById(R.id.setGoalButton);
        resetButton = findViewById(R.id.resetButton);
        shareButton = findViewById(R.id.shareButton);
        startStepCountingButton = findViewById(R.id.startStepCountingButton);
        exitButton = findViewById(R.id.exitButton);

        addWaterButton.setOnClickListener(v -> addWater());
        setGoalButton.setOnClickListener(v -> setGoal());
        resetButton.setOnClickListener(v -> resetData());
        shareButton.setOnClickListener(v -> shareProgress());
        startStepCountingButton.setOnClickListener(v -> startStepCounting());
        exitButton.setOnClickListener(v -> exitApp());

        registerReceiver(stepUpdateReceiver, new IntentFilter("StepUpdate"));

        loadPreferences();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stepUpdateReceiver);
    }

    private void addWater() {
        waterIntake += 250; // Each press adds 250 ml
        updateUI();
    }

    private void setGoal() {
        String goalInput = goalEditText.getText().toString();
        if (!goalInput.isEmpty()) {
            goal = Integer.parseInt(goalInput);
            saveGoal();
            updateUI();
        }
    }

    private void resetData() {
        stepCount = 0;
        runningCount = 0;
        cyclingCount = 0;
        waterIntake = 0;
        calories = 0;
        saveStepCount();
        updateUI();
    }

    private void startStepCounting() {
        Intent serviceIntent = new Intent(this, StepService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        statusTextView.setText("Status: Step Counting Started");
    }

    private void updateUI() {
        stepCountTextView.setText("Steps: " + stepCount);
        caloriesTextView.setText("Calories: " + String.format("%.2f", calories));
        activityTextView.setText("Running: " + runningCount + ", Cycling: " + cyclingCount);
        waterIntakeTextView.setText("Water Intake: " + waterIntake + " ml");
        goalTextView.setText("Goal: " + goal + " steps");
        if (stepCount == 0) {
            statusTextView.setText("Status: Ready");
        }
    }

    private void shareProgress() {
        String message = "I have walked " + stepCount + " steps, ran " + runningCount + " times, and cycled " + cyclingCount + " times today, burning " + String.format("%.2f", calories) + " calories and drank " + waterIntake + " ml of water!";
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void exitApp() {
        stopService(new Intent(this, StepService.class));
        finish();
    }

    private void saveStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, stepCount);
        editor.apply();
    }

    private void saveGoal() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(GOAL_KEY, goal);
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0);
        goal = sharedPreferences.getInt(GOAL_KEY, 10000);
    }
}
package com.bajracharya.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addTaskButton = findViewById(R.id.button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToAddTaskIntent = new Intent(MainActivity.this, addTask.class);
                MainActivity.this.startActivity(goToAddTaskIntent);
            }
        });

        Button allTaskButton = findViewById(R.id.button2);
        allTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToAllTaskIntent = new Intent(MainActivity.this, allTasks.class);
                MainActivity.this.startActivity(goToAllTaskIntent);
            }
        });

//        task buttons :::::::::::::::::::
        final Button task1 = findViewById(R.id.button5);
        task1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToTaskDetail = new Intent(MainActivity.this, TaskDetail.class);
                goToTaskDetail.putExtra("task1", task1.getText().toString());
                MainActivity.this.startActivity(goToTaskDetail);
            }
        });

        final Button task2 = findViewById(R.id.button6);
        task1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToTaskDetail = new Intent(MainActivity.this, TaskDetail.class);
                goToTaskDetail.putExtra("task2", task2.getText().toString());
                MainActivity.this.startActivity(goToTaskDetail);
            }
        });

        final Button task3 = findViewById(R.id.button7);
        task1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToTaskDetail = new Intent(MainActivity.this, TaskDetail.class);
                goToTaskDetail.putExtra("task3", task3.getText().toString());
                MainActivity.this.startActivity(goToTaskDetail);
            }
        });
    }

    public void onSettingButtonPress(View view) {
        Intent goToSettingsPage = new Intent(this, SettingsActivity.class);
        startActivity(goToSettingsPage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "resumed");

        TextView displayUsername = findViewById(R.id.textView12);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = sharedPreferences.getString("username", displayUsername.getText().toString());
        displayUsername.setText(username + "'s, Task");

    }


}

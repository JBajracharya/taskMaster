package com.bajracharya.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TaskDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        TextView title = findViewById(R.id.textView8);
        String displayTitle = getIntent().getStringExtra("task");
        title.setText(displayTitle);
    }
}

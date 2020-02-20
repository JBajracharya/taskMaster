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
        TextView body = findViewById(R.id.textView9);
        TextView state = findViewById(R.id.textView13);

        if(getIntent().getStringExtra("task") != null) {
            String displayTitleFromTaskButton = getIntent().getStringExtra("task");
            title.setText(displayTitleFromTaskButton);
        }else if(getIntent().getStringExtra("mTitleView") != null) {
            String displayTitleFromRecyclerView = getIntent().getStringExtra("mTitleView");
            String displayDescriptionFromRecyclerView = getIntent().getStringExtra("mBodyView");
            String displayStatus = getIntent().getStringExtra("mStateView");
            title.setText(displayTitleFromRecyclerView);
            body.setText(displayDescriptionFromRecyclerView);
            state.setText(displayStatus);


        }

    }
}

package com.bajracharya.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class addTask extends AppCompatActivity {

    AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);



        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "taskToDo").allowMainThreadQueries().build();

        Button addTaskButton = findViewById(R.id.button3);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText taskTitleInput = findViewById(R.id.editText);
                String taskTitleInputText = taskTitleInput.getText().toString();

                EditText taskDescriptionInput = findViewById(R.id.editText2);
                String taskDescriptionInputText = taskDescriptionInput.getText().toString();

                Task newTask = new Task(taskTitleInputText, taskDescriptionInputText, null);
                MainActivity.listOfTasks.add(0, newTask);

                appDatabase.tasksDao().save(newTask);






                TextView showSubmitMessage = addTask.this.findViewById(R.id.textView7);
                showSubmitMessage.setText("submitted!");
                showSubmitMessage.setVisibility(View.VISIBLE);
            }
        });
    }
}

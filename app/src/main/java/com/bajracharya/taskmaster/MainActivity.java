package com.bajracharya.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateTodoTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bajracharya.taskmaster.dummy.DummyContent;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTodoTaskInput;

public class MainActivity extends AppCompatActivity implements TaskListFragment.OnListFragmentInteractionListener {
    static String TAG = "MainActivity";

    static List<Task> listOfTasks;
    AppDatabase appDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "taskToDo").allowMainThreadQueries().build();

        this.listOfTasks = appDatabase.tasksDao().getAll();
//        for (Task i :
//                listOfTasks) {
//            Log.i(TAG, i.title);
//        }

//        Task a = new Task("Grocery", "Buy bunch of meat", "new");
//        Task b = new Task("Exercise", "Go to gym", "new");
//        Task c = new Task("Eat", "Buy bunch of meat", "new");
//
//        appDatabase.tasksDao().save(a);
//        appDatabase.tasksDao().save(b);
//        appDatabase.tasksDao().save(c);

        RecyclerView recyclerView = findViewById(R.id.fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyTaskListRecyclerViewAdapter(this.listOfTasks, this));


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
                goToTaskDetail.putExtra("task", task1.getText().toString());
                MainActivity.this.startActivity(goToTaskDetail);
            }
        });

        final Button task2 = findViewById(R.id.button6);
        task2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToTaskDetail = new Intent(MainActivity.this, TaskDetail.class);
                goToTaskDetail.putExtra("task", task2.getText().toString());
                MainActivity.this.startActivity(goToTaskDetail);
            }
        });

        final Button task3 = findViewById(R.id.button7);
        task3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToTaskDetail = new Intent(MainActivity.this, TaskDetail.class);
                goToTaskDetail.putExtra("task", task3.getText().toString());
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

        RecyclerView recyclerView = findViewById(R.id.fragment);
        recyclerView.getAdapter().notifyItemInserted(0);
        recyclerView.getLayoutManager().scrollToPosition(0);

        TextView displayUsername = findViewById(R.id.textView12);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = sharedPreferences.getString("username", displayUsername.getText().toString());
        displayUsername.setText(username + "'s, Task");

    }


    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        Log.i(TAG, "clikced");
    }

}

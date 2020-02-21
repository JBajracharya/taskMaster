package com.bajracharya.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.amazonaws.amplify.generated.graphql.CreateTodoTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import type.CreateTodoTaskInput;

public class addTask extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static String TAG = "jitendra";

    AppDatabase appDatabase;
    private AWSAppSyncClient mAWSAppSyncClient;
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        parent.getItemAtPosition(pos);
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        //        pulls in application context from aws
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "taskToDo").allowMainThreadQueries().build();

// adding dropdown list for task status ::::::::::::::::::::::::
        Spinner spinner = findViewById(R.id.spinner2);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        Button addTaskButton = findViewById(R.id.button3);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText taskTitleInput = findViewById(R.id.editText);
                String taskTitleInputText = taskTitleInput.getText().toString();

                EditText taskDescriptionInput = findViewById(R.id.editText2);
                String taskDescriptionInputText = taskDescriptionInput.getText().toString();

                Spinner statusInput = (Spinner) findViewById(R.id.spinner2);
                String statusInputText = statusInput.getSelectedItem().toString();
                Log.i(TAG, statusInputText);

                runTaskCreateMutation(taskTitleInputText, taskDescriptionInputText, statusInputText);

//                Task newTask = new Task(taskTitleInputText, taskDescriptionInputText, statusInputText);
//                MainActivity.listOfTasks.add(0, newTask);
//
//                appDatabase.tasksDao().save(newTask);
//
//                TextView showSubmitMessage = addTask.this.findViewById(R.id.textView7);
//                showSubmitMessage.setText("submitted!");
//                showSubmitMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    //    add data to database with mutation
    public void runTaskCreateMutation(String title, String body, String status){
        CreateTodoTaskInput createTodoInput = CreateTodoTaskInput.builder()
                .title(title)
                .body(body)
                .state(status)
                .build();


        mAWSAppSyncClient.mutate(CreateTodoTaskMutation.builder().input(createTodoInput).build())
                .enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<CreateTodoTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTodoTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTodoTaskMutation.Data> response) {
            Log.i("Results", "Added Todo");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };
}

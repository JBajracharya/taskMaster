package com.bajracharya.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateTodoTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTodoTasksQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bajracharya.taskmaster.dummy.DummyContent;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTodoTaskInput;

public class MainActivity extends AppCompatActivity implements TaskListFragment.OnListFragmentInteractionListener {
    static String TAG = "MainActivity";

    private AWSAppSyncClient mAWSAppSyncClient;


      List<Task> listOfTasks;
//    AppDatabase appDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        get application context for S3 bucket
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));


//        initialize the app to use the aws log in feature:::::::::::::::
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("INIT", "onResult: " + userStateDetails.getUserState().name());
                        if(userStateDetails.getUserState().equals(UserState.SIGNED_OUT)) {
                            //        Drop-in pre build auth
                            // 'this' refers the the current active activity
                            AWSMobileClient.getInstance().showSignIn(MainActivity.this, SignInUIOptions.builder()
                                            .nextActivity(MainActivity.class)
                                            .build(),
                                    new Callback<UserStateDetails>() {
                                        @Override
                                        public void onResult(UserStateDetails result) {
                                            Log.d(TAG, "onResult: " + result.getUserState());
                                            switch (result.getUserState()){
                                                case SIGNED_IN:
                                                    Log.i("INIT", "logged in!");
                                                    break;
                                                case SIGNED_OUT:
                                                    Log.i(TAG, "onResult: User did not choose to sign-in");
                                                    break;
                                                default:
                                                    AWSMobileClient.getInstance().signOut();
                                                    break;
                                            }

                                            if(result.getUserState().equals((UserState.SIGNED_IN))) {
                                                uploadWithTransferUtility();

                                            }
                                        }

                                @Override
                                public void onError(Exception e) {
                                    Log.e(TAG, "onError: ", e);
                                }
                            });
                        }



//                        if(userStateDetails.getUserState().equals((UserState.SIGNED_IN))) {
//                            AWSMobileClient.getInstance().signOut();
//                        }

                    }


                    @Override
                    public void onError(Exception e) {
                        Log.e("INIT", "Initialization error.", e);
                    }
                }
        );




//        pulls in context from aws
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        this.listOfTasks = new ArrayList<Task>();


        RecyclerView recyclerView = findViewById(R.id.fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyTaskListRecyclerViewAdapter(this.listOfTasks, this));

//        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
//                "taskToDo").allowMainThreadQueries().build();
//
//        this.listOfTasks = appDatabase.tasksDao().getAll();




//        for (Task i :
//                listOfTasks) {
//            Log.i(TAG, i.title);
//        }

//        Task a = new Task("Grocery", "Buy bunch of meat", "new");
//        Task b = new Task("Exercise", "Go to gym", "new");
//        listOfTasks.add(a);
//        listOfTasks.add(b);
//        Log.i(TAG, listOfTasks.toString());

//        Task c = new Task("Eat", "Buy bunch of meat", "new");
//
//        appDatabase.tasksDao().save(a);
//        appDatabase.tasksDao().save(b);
//        appDatabase.tasksDao().save(c);

//        RecyclerView recyclerView = findViewById(R.id.fragment);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(new MyTaskListRecyclerViewAdapter(this.listOfTasks, this));


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




//    method to log out user and send to log in page :::::::::::::::::::
    public void signoutUser(View view) {
        AWSMobileClient.getInstance().signOut();
        Intent goToMainPage = new Intent(MainActivity.this, MainActivity.class);
        startActivity(goToMainPage);
    }

//    get task data from dynamo db and show to the the recycler View :::::::::::::::::::::
    public void runQuery(){
        mAWSAppSyncClient.query(ListTodoTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(todoTaskCallback);
    }

    private GraphQLCall.Callback<ListTodoTasksQuery.Data> todoTaskCallback = new GraphQLCall.Callback<ListTodoTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<ListTodoTasksQuery.Data> response) {
            Log.i(TAG, response.data().listTodoTasks().items().toString());

            listOfTasks.clear();

            for( ListTodoTasksQuery.Item item : response.data().listTodoTasks().items()) {
                Log.i(TAG, item.title());
                listOfTasks.add(new Task(item.title(),item.body(), item.state()));
            }


//            this is necessary any time you modify content in the view
//            looper lets us send an action to the main ui thread (getMainLooper)
            Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {


                    RecyclerView recyclerView = findViewById(R.id.fragment);
                    recyclerView.getAdapter().notifyItemInserted(0);
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.getLayoutManager().scrollToPosition(0);
                }
            };
            handlerForMainThread.obtainMessage().sendToTarget();

        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };

    public void onSettingButtonPress(View view) {
        Intent goToSettingsPage = new Intent(this, SettingsActivity.class);
        startActivity(goToSettingsPage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "resumed");

//        get the data from aws and show in recycler view on resume ::::::::::::::::::::::::
        runQuery();

//        AWSMobileClient.getInstance()
        TextView displayUserNameFromAWS = findViewById(R.id.username);
        String usernameFromAWS = AWSMobileClient.getInstance().getUsername();
        displayUserNameFromAWS.setText("Welcome " + usernameFromAWS);


//      Display username from settings
        TextView displayUsername = findViewById(R.id.textView12);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = sharedPreferences.getString("username", displayUsername.getText().toString());
        displayUsername.setText(username + "'s, Task");

    }

//    uploading files to s3 bucket :::::::::::::::::::::::::;;;;;;;;;:::::::::
    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        File file = new File(getApplicationContext().getFilesDir(), "sample.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append("Howdy World!");
            writer.close();
        }
        catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/sample.txt",
                        new File(getApplicationContext().getFilesDir(),"sample.txt"));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the st;
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d(TAG, "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d(TAG, "Bytes Total: " + uploadObserver.getBytesTotal());
    }


    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        Log.i(TAG, "clikced");
    }

}

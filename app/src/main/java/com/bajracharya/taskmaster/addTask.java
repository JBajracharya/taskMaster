package com.bajracharya.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.amazonaws.amplify.generated.graphql.CreateTodoTaskMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateTodoTaskInput;

public class addTask extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static String TAG = "jitendra";

    AppDatabase appDatabase;
    private AWSAppSyncClient mAWSAppSyncClient;
    Context context;

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

        //        get application context for S3 bucket
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

        // Initialize the AWSMobileClient if not initialized
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
//                uploadWithTransferUtility();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Initialization error.", e);
            }
        });

        //        pulls in application context from aws
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "taskToDo").allowMainThreadQueries().build();

        context = getApplicationContext();





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

    //    add data to aws dynamo database with mutation
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
            Intent goToMain = new Intent(context, MainActivity.class);
            context.startActivity(goToMain);

        }


        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };

    ImageView imageView;
    Uri uri;

//    source: https://stackoverflow.com/questions/18220152/opening-an-image-using-intent-action-pick
//    grab and open image folder on the phone
    public void getImagefolder(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 50);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Log.i("image", Boolean.toString(data == null));
//        Log.i("image", uri.getPath());
        Log.i("requestcode", resultCode + "");

        if(requestCode==50 && resultCode == RESULT_OK) {
            uri = data.getData();
            Log.i("uri", uri + " " );
            uploadWithTransferUtility();
//            imageView.setImageURI(uri);
        }

    }


    //    uploading files to s3 bucket :::::::::::::::::::::::::;;;;;;;;;:::::::::
    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();
//
//        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
//        UPLOADING_IMAGE=new File(Environment.getExternalStorageDirectory().getPath()+"/Screenshot.png");
//        TransferObserver observer = transferUtility.upload(MY_BUCKET,OBJECT_KEY,UPLOADING_IMAGE);
//        observer.setTransferListener(new TransferListener()

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        File file = new File(picturePath);

        Log.i(TAG, "Uri: "+uri.toString());
        Log.i(TAG, "Path: "+picturePath);
        Log.i(TAG, "File? "+file.toString());

//        File file = new File(getApplicationContext().getFilesDir(), uri + "");
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//            writer.append("hihihih!");
//            writer.close();
//        }
//        catch(Exception e) {
//            Log.e(TAG, e.getMessage());
//        }

//        TransferObserver uploadObserver =
//                transferUtility.upload(
//                        "public/" + file,
//                        new File(getApplicationContext().getFilesDir(),uri+""));

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/" + UUID.randomUUID().toString(),
                        file);

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.i(TAG, "Upload works!@ :D");
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
                Log.e(TAG, "error on upload");
                ex.printStackTrace();
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
}

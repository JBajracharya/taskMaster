package com.bajracharya.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateTodoTaskInput;

public class addTask extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static String TAG = "jitendra";
    static String CHANNEL_ID = "100";

//  location service
    private FusedLocationProviderClient fusedLocationClient;

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

//        Create location services client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Add intent filter
        // Get the intent that started this activity
        Intent goToAddTask = getIntent();

        String type = goToAddTask.getType();

        // Figure out what to do based on the intent type
        if (type != null && type.contains("image/")) {
            // Handle intents with image data ...
            Uri imageUri = (Uri) goToAddTask.getParcelableExtra(goToAddTask.EXTRA_STREAM);
            if(imageUri != null) {
                ImageView imageView = findViewById(R.id.imageUpload);
                imageView.setImageURI(imageUri);
            }
        }

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

        //        pulls in application context from aws for database connectivity
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "taskToDo").allowMainThreadQueries().build();

        context = getApplicationContext();

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        //Set the notification's tap action
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, addTask.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //setting notfication contents
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("task title")
                .setContentText("Task description")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(((int)Math.random() *100), builder.build());





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


//        Button addTaskButton = findViewById(R.id.button3);
//        addTaskButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditText taskTitleInput = findViewById(R.id.editText);
//                String taskTitleInputText = taskTitleInput.getText().toString();
//
//                EditText taskDescriptionInput = findViewById(R.id.editText2);
//                String taskDescriptionInputText = taskDescriptionInput.getText().toString();
//
//                Spinner statusInput = (Spinner) findViewById(R.id.spinner2);
//                String statusInputText = statusInput.getSelectedItem().toString();
//                Log.i(TAG, statusInputText);
//
//                runTaskCreateMutation(taskTitleInputText, taskDescriptionInputText, statusInputText);
//
//
////                Task newTask = new Task(taskTitleInputText, taskDescriptionInputText, statusInputText);
////                MainActivity.listOfTasks.add(0, newTask);
////
////                appDatabase.tasksDao().save(newTask);
////
////                TextView showSubmitMessage = addTask.this.findViewById(R.id.textView7);
////                showSubmitMessage.setText("submitted!");
////                showSubmitMessage.setVisibility(View.VISIBLE);
//
//
//            }
//        });

    }

    public void getLocationAndSave(View view) {
        //        Get the last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.d(TAG, "Coords: " + location.getLatitude() + " " + location.getLongitude());
                            EditText taskTitleInput = findViewById(R.id.editText);
                            String taskTitleInputText = taskTitleInput.getText().toString();


                            EditText taskDescriptionInput = findViewById(R.id.editText2);
                            String taskDescriptionInputText = taskDescriptionInput.getText().toString();

                            Spinner statusInput = (Spinner) findViewById(R.id.spinner2);
                            String statusInputText = statusInput.getSelectedItem().toString();
                            Log.i(TAG, statusInputText);

                            runTaskCreateMutation(taskTitleInputText,taskDescriptionInputText,statusInputText,
                                    (float)location.getLongitude(), (float)location.getLatitude());
                        }
                    }
                });
    }





    //    add data to aws dynamo database with mutation
    public void runTaskCreateMutation(String title, String body, String status, Float longitude, Float latitude){
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

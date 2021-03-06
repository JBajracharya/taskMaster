package com.bajracharya.taskmaster;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.amplify.generated.graphql.ListTodoTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bajracharya.taskmaster.dummy.DummyContent;
import com.bajracharya.taskmaster.dummy.DummyContent.DummyItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TaskListFragment extends Fragment {

    String TAG = "FragmentActivity";
    private AWSAppSyncClient mAWSAppSyncClient;


        public static AppDatabase appDB;
    public List<Task> taskLists;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TaskListFragment newInstance(int columnCount) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasklist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            mAWSAppSyncClient = AWSAppSyncClient.builder()
                    .context(getContext())
                    .awsConfiguration(new AWSConfiguration(getContext()))
                    .build();



            this.taskLists = new ArrayList<Task>();
            runQuery();

            recyclerView.setAdapter(new MyTaskListRecyclerViewAdapter(taskLists, mListener));

//            using room to save and retrieve data
//            appDB = Room.databaseBuilder(getContext(), AppDatabase.class,

//                    "taskToDo").allowMainThreadQueries().build();
//
//            this.taskLists = appDB.tasksDao().getAll();

//            List<Task> listOfTasks = new ArrayList<>();
//            listOfTasks.add(new Task("Grocery", "Buy bunch of meat", "new"));
//            listOfTasks.add(new Task("Exercise", "Go to gym", "new"));
//            listOfTasks.add(new Task("Eat", "Buy bunch of meat", "new"));
//            listOfTasks.add(new Task("Sleep", "Go to bed at 10pm", "new"));
//            listOfTasks.add(new Task("Code", "Code everyday", "new"));
//            listOfTasks.add(new Task("Run", "Go for run every weekend", "new"));
//            recyclerView.setAdapter(new MyTaskListRecyclerViewAdapter(listOfTasks, null));
        }
        return view;
    }

    public void runQuery(){
        mAWSAppSyncClient.query(ListTodoTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(todoTaskCallback);
    }

    private GraphQLCall.Callback<ListTodoTasksQuery.Data> todoTaskCallback = new GraphQLCall.Callback<ListTodoTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTodoTasksQuery.Data> response) {
            Log.i(TAG, response.data().listTodoTasks().items().toString());

            taskLists.clear();

            for( ListTodoTasksQuery.Item item : response.data().listTodoTasks().items()) {
                taskLists.add(new Task(item.title(),item.body(), item.state()));
            }
            Log.i("taskList", taskLists.toString());

        };

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}

## Task Master application

### Lab 26: Beginning TaskMaster

<img src="assets/lab26.png" width="500" height="500">

Home page: Home page displays the heading at the top of the page, and image that mocks the my task view
and buttons at the bottom of the page to go to add task and all tasks page.

Add a task page: In add a task page user can type the title of the task and the description of the task.
When the user hits the submit button, displays submitted label on the page.

All task page: Displays images with a back button.

### Lab 27: Adding Data to TaskMaster
<img src="assets/settingPage.png" width="400" height="800">
<img src="assets/taskDetailPage.png" width="400" height="800">

Task Detail Page: Displays the detail of the task and the title of the task in this page. When the
user selects the specific task title, this page opens up with the details of the tasks user pick.

Task list: On the home page, three different task titles are displayed as a button. When the user
clicks on it, it goes to task detail page which shows the same title user chose on the task detail
page.

Settings page: Added setting button on the home page. When the user clicks on the setting button, it
takes to the setting page in which user can add their username. When the user clicks on save, it saves
the username and displays as "{username}'s task" above the three task button on the home page.


### Lab 28: RecyclerViews for Displaying Lists
<img src="assets/lab28.png" width="400" height="800">

Recycler View: Home page displays the recycler view for the list of different task saved by the user.
When the user taps on any of the task, it take to the Task detail page which display the title and the
description of the task that the user picks.

### Lab 33: Recycler View to All Task page and Toast
<img src="assets/lab33ToastDisplay.png" width="400" height="800">

All task displays recycler view showing all the task added by the user. When the user taps on
any of the task row, the toast view pops up showing the description of the task selected by the user.

### Lab 34: Amplify and Dynamo DB

<img src="assets/lab34dynamodb.png">
<img src="assets/lab34recyclerviewDynamoDBData.png" width="400" height="800">

Using the amplify add api command, created a Task resource that replicates the existing Task schema.
Updated all references to the Task data to instead use AWS Amplify to access the data in DynamoDB
instead of in Room.

Modified Add Task form to save the data entered in as a Task to DynamoDB.

Refactored homepage’s RecyclerView to display all Task entities in DynamoDB.

### Lab 37: S3
[location](assets/s3.png)

Uploads

On the “Add a Task” activity, allow users to optionally select a file to attach to that task. If a 
user attaches a file to a task, that file should be uploaded to S3, and associated with that task.

Displaying Files
On the Task detail activity, if there is a file that is an image associated with a particular Task, 
that image should be displayed within that activity. (If the file is any other type, you should 
display a link to it.)


### Lab 40: Notification
<img src="assets/notification.png" width="400" height="800">

Adding the notification feature which adds the new task added to the notification. When clicked on notification
it starts the application.

### Lab 41: Intent Filters
<img src="assets/intentFilter.png" width="370" height="750">

Adding an intent filter to the application such that a user can hit the “share” button on an image in
another application, choose TaskMaster as the app to share that image with, and be taken directly
to the Add a Task activity with that image pre-selected.


### Lab 42: Location
![location](assets/savingLocation.png)

Location
When the user adds a task, their location should be retrieved and included as part of the saved Task.

Displaying Location
On the Task Detail activity, the location of a Task should be displayed if it exists.






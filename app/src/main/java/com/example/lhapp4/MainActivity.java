package com.example.lhapp4;

import eneter.messaging.diagnostic.EneterTrace;
import eneter.messaging.endpoints.typedmessages.*;
import eneter.messaging.messagingsystems.messagingsystembase.*;
import eneter.messaging.messagingsystems.tcpmessagingsystem.TcpMessagingSystemFactory;
import eneter.net.system.EventHandler;
import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class MainActivity extends Activity implements SensorEventListener{


    // Request message type
    // The message must have the same name as declared in the service.
    // Also, if the message is the inner class, then it must be static.
    public static class MyRequest
    {
        public String Text;
    }

    // Response message type
    // The message must have the same name as declared in the service.
    // Also, if the message is the inner class, then it must be static.
    public static class MyResponse
    {
        public int Length;
    }

    private QuestionsLibrary mQuestionsLibrary = new QuestionsLibrary();
    private int mQuestionNumber = 0;

    // UI controls
    private Handler myRefresh = new Handler();
    private EditText myMessageTextEditText;
    private EditText myResponseEditText;
    private Button mySendRequestBtn, myAccRequestBtn;
    private TextView myQuestionView;
    private TextView xText,yText, zText;

    private SensorManager SM;
    Sensor myAccelerometer;

    // Sender sending MyRequest and as a response receiving MyResponse.
    private IDuplexTypedMessageSender<MyResponse, MyRequest> mySender;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer sensor
        myAccelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register sensor listener
        SM.registerListener(MainActivity.this, myAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);

        // Get UI widgets.
        myMessageTextEditText = (EditText) findViewById(R.id.messageTextEditText);
        myResponseEditText = (EditText) findViewById(R.id.messageLengthEditText);
        mySendRequestBtn = (Button) findViewById(R.id.sendRequestBtn);
        myAccRequestBtn = (Button) findViewById(R.id.sendAccBtn);
        myQuestionView = (TextView) findViewById(R.id.questionText1);

        updateQuestion();

        // Subscribe to handle the button click.
        mySendRequestBtn.setOnClickListener(myOnSendRequestClickHandler);
        myAccRequestBtn.setOnClickListener(myOnAccRequestClickHandler);

        // Subscribe to handle the focus click
        myMessageTextEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    myMessageTextEditText.setHint("Your answer here");
                }
                else {
                    myMessageTextEditText.setHint("");
                    //hideKeyboard(v);
                }
            }
        });

        // Open the connection in another thread.
        // Note: From Android 3.1 (Honeycomb) or higher
        //       it is not possible to open TCP connection
        //       from the main thread.
        Thread anOpenConnectionThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    openConnection();
                }
                catch (Exception err)
                {
                    EneterTrace.error("Open connection failed.", err);
                }
            }
        });
        anOpenConnectionThread.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*Not implemented*/
    }

    boolean DataRequested = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        xText.setText("X:" + event.values[0]);
        yText.setText("Y:" + event.values[1]);
        zText.setText("Z:" + event.values[2]);

        if(DataRequested){
            onAccRequest(event);
        }
    }

    @Override
    public void onDestroy()
    {
        // Stop listening to response messages.
        mySender.detachDuplexOutputChannel();

        super.onDestroy();
    }

    private void openConnection() throws Exception
    {
        // Create sender sending MyRequest and as a response receiving MyResponse
        IDuplexTypedMessagesFactory aSenderFactory =
                new DuplexTypedMessagesFactory();
        mySender = aSenderFactory.createDuplexTypedMessageSender(MyResponse.class, MyRequest.class);

        // Subscribe to receive response messages.
        mySender.responseReceived().subscribe(myOnResponseHandler);

        // Create TCP messaging for the communication.
        // Note: 192.168.0.100/192.168.0.101 is the IP address
        //      from the wireless router (no internet)
        //      and 8800 is the socket.
        IMessagingSystemFactory aMessaging = new TcpMessagingSystemFactory();
        IDuplexOutputChannel anOutputChannel =
                aMessaging.createDuplexOutputChannel("tcp://192.168.0.100:8800/");

        // Attach the output channel to the sender and be able to send
        // messages and receive responses.
        mySender.attachDuplexOutputChannel(anOutputChannel);
    }

    // Request for the message
    private void onSendRequest(View v)
    {
        // Create the request message.
        MyRequest aRequestMsg = new MyRequest();

        aRequestMsg.Text = "Q:" + myMessageTextEditText.getText().toString();


        // Send the request message.
        try
        {
            mySender.sendRequestMessage(aRequestMsg);

        }
        catch (Exception err)
        {
            EneterTrace.error("Sending the message failed.", err);
        }
    }

    // Request for the accelerometer data
    private void onAccRequest(SensorEvent e)
    {
        // Create the request data
        MyRequest xMsg = new MyRequest();
        MyRequest yMsg = new MyRequest();
        MyRequest zMsg = new MyRequest();

        xMsg.Text = "X:" + e.values[0];
        yMsg.Text = "Y:" + e.values[1];
        zMsg.Text = "Z:" + e.values[2];

        //Send the request data
        try
        {
            mySender.sendRequestMessage(xMsg);
            mySender.sendRequestMessage(yMsg);
            mySender.sendRequestMessage(zMsg);
        }
        catch (Exception err)
        {
            EneterTrace.error("Sending the data failed.", err);
        }
    }

    private void onResponseReceived(Object sender, final TypedResponseReceivedEventArgs<MyResponse> e)
    {
        // Display the result - returned number of characters.
        // Note: Marshal displaying to the correct UI thread.
        myRefresh.post(new Runnable()
        {
            @Override
            public void run()
            {
                myResponseEditText.setText(Integer.toString(e.getResponseMessage().Length));
            }
        });
    }

    private EventHandler<TypedResponseReceivedEventArgs<MyResponse>> myOnResponseHandler

            = new EventHandler<TypedResponseReceivedEventArgs<MyResponse>>()
    {
        @Override
        public void onEvent(Object sender,
                            TypedResponseReceivedEventArgs<MyResponse> e)
        {
            onResponseReceived(sender, e);
        }
    };

    // Button that sends the message when clicked
    private OnClickListener myOnSendRequestClickHandler = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Log.d("textevent","Text send button clicked:");
            onSendRequest(v);
            myMessageTextEditText.getText().clear();
            updateQuestion();
        }
    };


    // Button that sends the accelerometer data when clicked
    private OnClickListener myOnAccRequestClickHandler = new OnClickListener() {
        @Override
        public void onClick(View acc) {
            Log.d("Debug","ACC button Pressed");

            if(DataRequested == true){
                DataRequested = false;
            } else if(DataRequested == false){
                DataRequested = true;
            }
            }

        };


    // Questions being updated
    private void updateQuestion() {
        myQuestionView.setText(mQuestionsLibrary.getQuestion(mQuestionNumber));
        mQuestionNumber++;

        if(mQuestionNumber > 4) {
            mySendRequestBtn.setVisibility(View.INVISIBLE);
            myMessageTextEditText.setVisibility(View.INVISIBLE);
        }

    }

    // Remove keyboard when click outside
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class MainActivity extends Activity implements SensorEventListener{

    private QuestionsLibrary mQuestionsLibrary = new QuestionsLibrary();
    private int mQuestionNumber = 0;

    // UI controls

    private EditText myMessageTextEditText;
    private EditText myResponseEditText;
    private Button mySendRequestBtn, myAccRequestBtn, myConnectBtn;
    private TextView myQuestionView;
    private TextView xText,yText, zText, GyroXText, GyroYText, GyroZText;
    private CheckBox myCheckBoxAcc, myCheckBoxGyro;

    private SensorManager SM;
    Sensor myAccelerometer;
    Sensor myGyroscope;
    TCPManager myTCPManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        myTCPManager = new TCPManager();
        final String ipAddress = getIntent().getStringExtra("IP");
        Thread anOpenConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    myTCPManager.openConnection(ipAddress);
                    //generatePopUp("Connection Established");
                    //myTCPManager.onDestroy();
                } catch (Exception err) {
                    //generatePopUp("Open connection failed.");
                }
            }
        });
        anOpenConnectionThread.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer sensor
        myAccelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Gyroscope sensor
        myGyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Register sensor listener
        SM.registerListener(MainActivity.this, myAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(MainActivity.this, myGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        // Get UI widgets.
        myMessageTextEditText = (EditText) findViewById(R.id.messageTextEditText);
        myResponseEditText = (EditText) findViewById(R.id.messageLengthEditText);
        mySendRequestBtn = (Button) findViewById(R.id.sendRequestBtn);
        myAccRequestBtn = (Button) findViewById(R.id.sendAccBtn);
        myQuestionView = (TextView) findViewById(R.id.questionText1);
        myCheckBoxAcc = (CheckBox) findViewById(R.id.checkBoxAcc);
        myCheckBoxGyro = (CheckBox) findViewById(R.id.checkBoxGyro);

        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);

        GyroXText = (TextView) findViewById(R.id.GyroXText);
        GyroYText = (TextView) findViewById(R.id.GyroYText);
        GyroZText = (TextView) findViewById(R.id.GyroZText);

        updateQuestion();

        // Subscribe to handle the button click.
        mySendRequestBtn.setOnClickListener(myOnSendRequestClickHandler);
        myAccRequestBtn.setOnClickListener(myOnAccRequestClickHandler);

        xText.setVisibility(View.INVISIBLE);
        yText.setVisibility(View.INVISIBLE);
        zText.setVisibility(View.INVISIBLE);

        GyroXText.setVisibility(View.INVISIBLE);
        GyroYText.setVisibility(View.INVISIBLE);
        GyroZText.setVisibility(View.INVISIBLE);

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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    // Get the sensor data
    boolean DataRequested = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            xText.setText("AccX:" + event.values[0]);
            yText.setText("AccY:" + event.values[1]);
            zText.setText("AccZ:" + event.values[2]);

            if(DataRequested){
                onAccRequest(event);
            }
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            GyroXText.setText("GyroX:" + event.values[0]);
            GyroYText.setText("GyroY:" + event.values[1]);
            GyroZText.setText("GyroZ:" + event.values[2]);

            if(DataRequested){
                onGyroRequest(event);
            }
        }
    }



    // Request for the message
    private void onSendRequest(View v)
    {
        String s = "QQ:" + myMessageTextEditText.getText().toString();
        generatePopUp(s);
        myTCPManager.sendTCPMessage(s);
    }




    // Request for the accelerometer data
    private void onAccRequest(SensorEvent e)
    {

        String x = "AX:" + e.values[0];
        String y = "AY:" + e.values[1];
        String z = "AZ:" + e.values[2];

        Log.d("acc ", x);
        myTCPManager.sendTCPMessage(x);
        myTCPManager.sendTCPMessage(y);
        myTCPManager.sendTCPMessage(z);
        Log.d("acc ", "data sent");

    }

    //Request for the gyroscope data
    private void onGyroRequest(SensorEvent g)
    {
        String gx = "GX:" + g.values[0];
        String gy = "GY:" + g.values[1];
        String gz = "GZ:" + g.values[2];

        myTCPManager.sendTCPMessage(gx);
        myTCPManager.sendTCPMessage(gy);
        myTCPManager.sendTCPMessage(gz);

    }



    // Button that sends the message when clicked
    private OnClickListener myOnSendRequestClickHandler = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Log.d("Message","Text send button clicked:");
            onSendRequest(v);
            myMessageTextEditText.getText().clear();
            updateQuestion();
        }
    };


    // Checkbox for the sensors and button to send the data
    private OnClickListener myOnAccRequestClickHandler = new OnClickListener() {
        @Override
        public void onClick(View acc) {
            Log.d("Sensor","ACC button Pressed");

            // Checkbox for Accelerometer
            if(myCheckBoxAcc.isChecked()) {

                if(DataRequested == true) {
                    DataRequested = false;

                    generatePopUp("Sensor data recording stopped");

                    xText.setVisibility(View.INVISIBLE);
                    yText.setVisibility(View.INVISIBLE);
                    zText.setVisibility(View.INVISIBLE);
                }
                else if(DataRequested == false) {
                    DataRequested = true;

                    generatePopUp("Sensor data recording started");

                    xText.setVisibility(View.VISIBLE);
                    yText.setVisibility(View.VISIBLE);
                    zText.setVisibility(View.VISIBLE);
                }
            }

            // Checkbox for Gyroscope
            if(myCheckBoxGyro.isChecked()) {

                if(DataRequested == true){
                    DataRequested = false;

                    generatePopUp("Sensor data recording stopped");

                    GyroXText.setVisibility(View.INVISIBLE);
                    GyroYText.setVisibility(View.INVISIBLE);
                    GyroZText.setVisibility(View.INVISIBLE);
                }
                else if(DataRequested == false){
                    DataRequested = true;

                    generatePopUp("Sensor data recording started");

                    GyroXText.setVisibility(View.VISIBLE);
                    GyroYText.setVisibility(View.VISIBLE);
                    GyroZText.setVisibility(View.VISIBLE);
                }
            }

            // Checkbox for Accelerometer and Gyroscope
            if(myCheckBoxAcc.isChecked() && myCheckBoxGyro.isChecked()) {

                if(DataRequested == true) {
                    DataRequested = false;

                    generatePopUp("Sensor data recording stopped");

                    xText.setVisibility(View.INVISIBLE);
                    yText.setVisibility(View.INVISIBLE);
                    zText.setVisibility(View.INVISIBLE);

                    GyroXText.setVisibility(View.INVISIBLE);
                    GyroYText.setVisibility(View.INVISIBLE);
                    GyroZText.setVisibility(View.INVISIBLE);
                }
                else if(DataRequested == false) {
                    DataRequested = true;

                    generatePopUp("Sensor data recording started");

                    xText.setVisibility(View.VISIBLE);
                    yText.setVisibility(View.VISIBLE);
                    zText.setVisibility(View.VISIBLE);

                    GyroXText.setVisibility(View.VISIBLE);
                    GyroYText.setVisibility(View.VISIBLE);
                    GyroZText.setVisibility(View.VISIBLE);
                }
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
            // TODO Slider shows up here
        }

    }

    // Remove keyboard when click outside
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void generatePopUp(String s){
        final Toast myToastConnected = Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT);
        myToastConnected.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        myToastConnected.show();
    };


}
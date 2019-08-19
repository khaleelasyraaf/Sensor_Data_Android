package com.example.lhapp4;

import android.app.Activity;

import android.os.Bundle;
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
import android.content.Context;
public class MainActivity extends Activity implements SensorEventListener{

    private QuestionsLibrary mQuestionsLibrary = new QuestionsLibrary();
    private int mQuestionNumber = 0;

    // UI controls
    private EditText myMessageTextEditText;
    private EditText myResponseEditText;
    private Button mySendRequestBtn, myAccRequestBtn;
    private TextView myQuestionView;
    private TextView xText,yText, zText, GyroXText, GyroYText, GyroZText, lightView;
    private CheckBox myCheckBoxAcc, myCheckBoxGyro, myCheckBoxLight;

    private SensorManager SM;
    Sensor myAccelerometer, myGyroscope, myLight;
    TCPManager myTCPManager;
    Context context;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        myTCPManager = new TCPManager(context);
        final String ipAddress = getIntent().getStringExtra("IP");
        Thread anOpenConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    myTCPManager.openConnection(ipAddress);
                }
                catch (Exception err) {

                }
            }
        });
        anOpenConnectionThread.start();

        // Create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer sensor
        myAccelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Gyroscope sensor
        myGyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Light sensor
        myLight = SM.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Register sensor listener
        SM.registerListener(MainActivity.this, myAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(MainActivity.this, myGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(MainActivity.this, myLight, SensorManager.SENSOR_DELAY_NORMAL);

        //region Widgets

        // Get UI widgets.
        myMessageTextEditText = (EditText) findViewById(R.id.messageTextEditText);
        myResponseEditText = (EditText) findViewById(R.id.messageLengthEditText);
        mySendRequestBtn = (Button) findViewById(R.id.sendRequestBtn);
        myAccRequestBtn = (Button) findViewById(R.id.sendAccBtn);
        myQuestionView = (TextView) findViewById(R.id.questionText1);
        myCheckBoxAcc = (CheckBox) findViewById(R.id.checkBoxAcc);
        myCheckBoxGyro = (CheckBox) findViewById(R.id.checkBoxGyro);
        myCheckBoxLight = (CheckBox) findViewById(R.id.checkBoxLight);

        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);

        GyroXText = (TextView) findViewById(R.id.GyroXText);
        GyroYText = (TextView) findViewById(R.id.GyroYText);
        GyroZText = (TextView) findViewById(R.id.GyroZText);

        lightView = (TextView) findViewById(R.id.lightView);

        //endregion

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

        lightView.setVisibility(View.INVISIBLE);

        // Subscribe to handle the focus click
        myMessageTextEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    myMessageTextEditText.setHint("Your answer here");
                }
                else {
                    myMessageTextEditText.setHint("");
                }
            }
        });
    }

    // Stop listening to the sensors when the app is closed
    @Override
    protected void onPause() {
        super.onPause();
        SM.unregisterListener(MainActivity.this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    // Get the sensors data from the phone
    boolean IsDataRequested = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Log.d("Accelerometer", "X:" + event.values[0] + "Y:" + event.values[1] + "Z:" + event.values[2]);

            if (IsAccDataRequested == true) {
                xText.setText("AccX:" + event.values[0]);
                yText.setText("AccY:" + event.values[1]);
                zText.setText("AccZ:" + event.values[2]);

                if (IsDataRequested == true) {
                    onAccRequest(event);
                }
            }
        }

        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //Log.d("Gyroscope", "X:" + event.values[0] + "Y:" + event.values[1] + "Z:" + event.values[2]);

            if (IsGyroDataRequested == true) {
                GyroXText.setText("GyroX:" + event.values[0]);
                GyroYText.setText("GyroY:" + event.values[1]);
                GyroZText.setText("GyroZ:" + event.values[2]);

                if (IsDataRequested == true) {
                     onGyroRequest(event);
                }
            }
        }

        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            //Log.d("Light", "Li:" + event.values[0]);

            if(IsLightDataRequested == true) {
                lightView.setText("Light:" + event.values[0]);

                if(IsDataRequested == true) {
                    onLightRequest(event);
                }
            }
        }
    }

    //region Request data

    // Request for the text message
    private void onSendRequest(View v)
    {
        String s = "QQ:" + myMessageTextEditText.getText().toString();
        myTCPManager.sendTCPMessage(s);
    }


    // Request for the accelerometer data
    private void onAccRequest(SensorEvent e)
    {

        String x = "AX:" + e.values[0];
        String y = "AY:" + e.values[1];
        String z = "AZ:" + e.values[2];

        myTCPManager.sendTCPMessage(x);
        myTCPManager.sendTCPMessage(y);
        myTCPManager.sendTCPMessage(z);

    }

    // Request for the gyroscope data
    private void onGyroRequest(SensorEvent g)
    {
        String gx = "GX:" + g.values[0];
        String gy = "GY:" + g.values[1];
        String gz = "GZ:" + g.values[2];

        myTCPManager.sendTCPMessage(gx);
        myTCPManager.sendTCPMessage(gy);
        myTCPManager.sendTCPMessage(gz);

    }

    // Request for the light data
    private void onLightRequest(SensorEvent light)
    {
        String li = "LI:" + light.values[0];

        myTCPManager.sendTCPMessage(li);
    }

    //endregion

    //region Checkbox

    boolean IsAccDataRequested = false;
    boolean IsGyroDataRequested = false;
    boolean IsLightDataRequested = false;

    // When the accelerometer checkbox has been clicked
    public void onAccCheckboxClicked (View view) {
        IsAccDataRequested = myCheckBoxAcc.isChecked();
        generatePopUp("IsAccDataRequested: " + IsAccDataRequested);
    }

    // When the gyroscope checkbox has been clicked
    public void onGyroCheckboxClicked (View view) {
        IsGyroDataRequested = myCheckBoxGyro.isChecked();
        generatePopUp("IsGyroDataRequested: " + IsGyroDataRequested);
    }

    // When the light checkbox has been clicked
    public void onLightCheckboxClicked (View view) {
        IsLightDataRequested = myCheckBoxLight.isChecked();
        generatePopUp("IsLightDataRequested: " + IsLightDataRequested);
    }

    //endregion

    //region Send data

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

    // Button that sends the sensor data when clicked
    private OnClickListener myOnAccRequestClickHandler = new OnClickListener() {
        @Override
        public void onClick(View acc) {
            Log.d("Sensor","ACC button Pressed");

            if(IsAccDataRequested == true) {

                xText.setVisibility(View.VISIBLE);
                yText.setVisibility(View.VISIBLE);
                zText.setVisibility(View.VISIBLE);
            }
            else if(IsAccDataRequested == false) {

                xText.setVisibility(View.INVISIBLE);
                yText.setVisibility(View.INVISIBLE);
                zText.setVisibility(View.INVISIBLE);
            }

            if(IsGyroDataRequested == true){

                GyroXText.setVisibility(View.VISIBLE);
                GyroYText.setVisibility(View.VISIBLE);
                GyroZText.setVisibility(View.VISIBLE);
            }
            else if(IsGyroDataRequested == false){

                GyroXText.setVisibility(View.INVISIBLE);
                GyroYText.setVisibility(View.INVISIBLE);
                GyroZText.setVisibility(View.INVISIBLE);
            }

            if(IsLightDataRequested == true) {

                lightView.setVisibility(View.VISIBLE);
            }
            else if(IsLightDataRequested == true) {

                lightView.setVisibility(View.INVISIBLE);
            }

            IsDataRequested = !IsDataRequested;
            generatePopUp(String.valueOf(IsDataRequested));
        }

    };

    //endregion


    // Questions being updated
    public void updateQuestion() {
        myQuestionView.setText(mQuestionsLibrary.getQuestion(mQuestionNumber));
        mQuestionNumber++;

        if(mQuestionNumber > 4) {
            mySendRequestBtn.setVisibility(View.INVISIBLE);
            myMessageTextEditText.setVisibility(View.INVISIBLE);
            // TODO Slider shows up here
        }

    }

    // Remove keyboard on screen touch (anywhere else besides keyboard)
    public void hideKeyboard(View kb) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(kb.getWindowToken(), 0);
    }

    // Toast message
    public void generatePopUp(String s){
        final Toast myToastConnected = Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT);
        myToastConnected.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        myToastConnected.show();
    };

}
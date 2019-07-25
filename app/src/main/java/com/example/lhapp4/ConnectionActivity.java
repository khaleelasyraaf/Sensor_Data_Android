package com.example.lhapp4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

import eneter.messaging.diagnostic.EneterTrace;
import eneter.messaging.endpoints.typedmessages.IDuplexTypedMessageSender;

public class ConnectionActivity extends AppCompatActivity implements Serializable {


    private TextView myIPaddressText, myPortText;
    private EditText myIPaddressInput, myPortInput;
    private Button myConnectBtn;
    TCPManager myTCPManager;
    private IDuplexTypedMessageSender<String, String> mySender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        myIPaddressText = (TextView) findViewById(R.id.myIPaddressText);
        myIPaddressInput = (EditText) findViewById(R.id.myIPaddressInput);
        myPortText = (TextView) findViewById(R.id.myPortText);
        myPortInput = (EditText) findViewById(R.id.myPortInput);
        myConnectBtn = (Button) findViewById(R.id.myConnectBtn);

        myConnectBtn.setOnClickListener(myConnectRequestClickHandler);

        myTCPManager = new TCPManager();
    }



    private String  ipAddress = "";
    private View.OnClickListener myConnectRequestClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View cnn) {
            ipAddress = "tcp://192.168.0." + myIPaddressInput.getText() + ":" + myPortInput.getText();
            final Intent intent = new Intent(ConnectionActivity.this, MainActivity.class);
            generatePopUp("clicked" + ipAddress);
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
                    if(myTCPManager.SuccessfullyConnected == true) {
                        intent.putExtra("IP", ipAddress);
                        startActivity(intent);
                    }
                }
            });
            anOpenConnectionThread.start();

            //Boolean success = false;

        }
    };

        public void generatePopUp(String s){
            final Toast myToast = Toast.makeText(ConnectionActivity.this, s, Toast.LENGTH_SHORT);
            myToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            myToast.show();
        };


}
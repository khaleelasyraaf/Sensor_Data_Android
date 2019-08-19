package com.example.lhapp4;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import java.io.Serializable;

/**Class for handling the connection page**/
public class ConnectionActivity extends AppCompatActivity implements Serializable {


    private TextView myIPaddressText, myPortText;
    private EditText myIPaddressInput, myPortInput;
    private Button myConnectBtn;
    TCPManager myTCPManager;
    Context context;

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

        myTCPManager = new TCPManager(context);
    }


    // Button that opens the MainActivity page once the connection has been established,
    // based on IP Address and Port Number.
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
                        //myTCPManager.onDestroy();
                    } catch (Exception err) {

                    }

                    if(myTCPManager.SuccessfullyConnected == true) {
                        intent.putExtra("IP", ipAddress);
                        startActivity(intent);
                    }
                }
            });
            anOpenConnectionThread.start();


        }
    };

    // Remove keyboard on screen touch (anywhere else besides keyboard)
    public void hideKeyboard(View kb) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(kb.getWindowToken(), 0);
    }

    // Toast message
    public void generatePopUp(String s){
        final Toast myToast = Toast.makeText(ConnectionActivity.this, s, Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        myToast.show();
    };

}

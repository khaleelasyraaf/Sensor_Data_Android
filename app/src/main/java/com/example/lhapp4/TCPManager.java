package com.example.lhapp4;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import eneter.messaging.diagnostic.EneterTrace;
import eneter.messaging.endpoints.typedmessages.DuplexTypedMessagesFactory;
import eneter.messaging.endpoints.typedmessages.IDuplexTypedMessageSender;
import eneter.messaging.endpoints.typedmessages.IDuplexTypedMessagesFactory;
import eneter.messaging.endpoints.typedmessages.TypedResponseReceivedEventArgs;
import eneter.messaging.messagingsystems.messagingsystembase.IDuplexOutputChannel;
import eneter.messaging.messagingsystems.messagingsystembase.IMessagingSystemFactory;
import eneter.messaging.messagingsystems.tcpmessagingsystem.TcpMessagingSystemFactory;
import eneter.net.system.EventHandler;

/**Class for handling all tcp related process**/
public class TCPManager {

    // Sender sending MyRequest and as a response receiving MyResponse.
    private IDuplexTypedMessageSender<String, String> mySender;
    private Handler myRefresh = new Handler();
    public Boolean SuccessfullyConnected = false;

    /**Constructor for TCP Manager **/
    public TCPManager(){


    }

    public void sendTCPMessage(String request){
        try
        {
            mySender.sendRequestMessage(request);
        }
        catch (Exception err)
        {
            EneterTrace.error("Sending the data failed.", err);
        }
    }

    public void onDestroy()
    {
        // Stop listening to response messages.
        mySender.detachDuplexOutputChannel();

    }

    public void openConnection(String ip) throws Exception
    {
        // Create sender sending MyRequest and as a response receiving MyResponse
        IDuplexTypedMessagesFactory aSenderFactory =  new DuplexTypedMessagesFactory();
        Log.d("OC:","aSenderCreated");
        mySender = aSenderFactory.createDuplexTypedMessageSender(String.class, String.class);
        Log.d("OC:","mySenderInstantiated");
        // Subscribe to receive response messages.
        mySender.responseReceived().subscribe(myOnResponseHandler);
        Log.d("OC:","Subscribed");
        // Create TCP messaging for the communication.
        IMessagingSystemFactory aMessaging = new TcpMessagingSystemFactory();
        IDuplexOutputChannel anOutputChannel = aMessaging.createDuplexOutputChannel(ip);
        Log.d("OC:","OutPutChannelCreated");
        // Attach the output channel to the sender and be able to send
        // messages and receive responses.
        mySender.attachDuplexOutputChannel(anOutputChannel);
        Log.d("OC:","Connection Successful");
        SuccessfullyConnected = true;

    }

    private void onResponseReceived(Object sender, final TypedResponseReceivedEventArgs<String> e)
    {
        // Display the result - returned number of characters.
        myRefresh.post(new Runnable()
        {
            @Override
            public void run()
            {
                //myResponseEditText.setText(Integer.toString(e.getResponseMessage().Length));
            }
        });
    }

    private EventHandler<TypedResponseReceivedEventArgs<String>> myOnResponseHandler
            = new EventHandler<TypedResponseReceivedEventArgs<String>>()
    {
        @Override
        public void onEvent(Object sender,
                            TypedResponseReceivedEventArgs<String> e)
        {
            onResponseReceived(sender, e);
        }
    };

}

package com.netcore.pnserver.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


class FCMSender extends Sender {

    public FCMSender(String key) {
        super(key);
    }

    @Override
    protected HttpURLConnection getConnection(String url) throws IOException {
        String fcmUrl = "https://fcm.googleapis.com/fcm/send";
        return (HttpURLConnection) new URL(fcmUrl).openConnection();
    }
}

public class TestWebPush {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String apiKey = "AIzaSyDs-VOurqb8LOpEti6rOapv5kqnjEX47r8";
        //apiKey = "AIzaSyB2v0tlGaOHpjM6MV_Z-R17KLsWCybo3GU";
        String token = "dNUcI58T52Q:APA91bG9ccaA09QbNVS-gi7k9uCgDjIQR1qEyttNvax0NQGs_eifW6ejcf-fIbvH5UGWBJJMfrM7T-AUU2ByCHTaVwIUIzDSRCJ7MGZs2vlBYNEN4bAeE4g0W-WfWrYNhyBYyirXBIM2";
        token = "f8dK8tBHlCw:APA91bGy6sG9vi_y2RKPbdg4lkCTUf9SHxl8Eog37MII64t5ey5uKqviUSE1x8V1PKgQ71P-MiF0zPZbMAwxPJfLQQsnCWeMKBAs6xiSLs6Y3OMsQlxk2dziqWDJsGgyhL-TAH_dleT9";
        
        apiKey = "AIzaSyB2v0tlGaOHpjM6MV_Z-R17KLsWCybo3GU";
        token = "eIPbQBPlSBc:APA91bHQneyWmUn8c_QnLn4XKVA6TAcKX11rM-E17ISvQ3EdwwxeLVrA1XH3xIPtCmNwJhpnMpKSYJBqnlufQM0XkKYl-L61Ojo4qFJEJPMUGcZrY9f3AqrAHUZmASYyuQ0kgP81LXYM";
        int RETRIES = 2;
        FCMSender sender = new FCMSender(apiKey);
       
        String title = "HI VINIT", message = "This is PROJECT-K";
        int ttl = 1000;
       
          			
    	String trid = "14150-164-10-0-160707150357";
    	String imageUrl = "https://www.google.co.in/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";
    	StringBuilder action = new StringBuilder("{\"sound\":\""+true+"\"");
    	action.append(",\"icon\":\""+imageUrl+"\"");
        action.append(",\"deeplink\":\""+imageUrl+"\"");
        action.append("}");
    	
        
        
        Message msg = new Message.Builder().addData("action", action.toString()).addData("title", title).addData("body", message).addData("time_to_live", String.valueOf(ttl)).addData("trid", trid).build();
        try {
            Result result = sender.send(msg, token, RETRIES);
            System.out.print("done" + msg);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
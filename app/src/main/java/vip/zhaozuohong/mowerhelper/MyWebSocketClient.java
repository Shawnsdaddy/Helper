package vip.zhaozuohong.mowerhelper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MyWebSocketClient extends WebSocketClient {
    Handler handler;
    TextView mowerLog;
    List<String> last30 = new ArrayList<>();
    String next_time;
    Context context;

    public MyWebSocketClient(Handler handler, TextView mowerLog, Context context, URI serverUri) {
        super(serverUri);
        this.handler = handler;
        this.mowerLog = mowerLog;
        this.context = context;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed. Code: " + code + ", Reason: " + reason);
    }

    @Override
    public void onMessage(String message) {
        String[] msg_lines = message.split("\n");
        String regex = ".*到(\\d{1,2}:\\d{2}:\\d{2})开始工作$";
        Pattern pattern = Pattern.compile(regex);
        next_time = "";
        for (String string : msg_lines) {
            Matcher matcher = pattern.matcher(string);
            if (matcher.matches()) {
                next_time = matcher.group(1);
            }
        }
        Collections.addAll(last30, msg_lines);
        while (last30.size() > 30) {
            last30.remove(0);
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                mowerLog.setText("");
                mowerLog.append(String.join("\n", last30));
            }
        });
        if (!Objects.equals(next_time, "")) {
            Intent intent = new Intent(context, MyService.class);
            intent.putExtra("next_time", next_time);
            context.stopService(intent);
            context.startService(intent);
        }
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error: " + ex.getMessage());
    }
}

package vip.zhaozuohong.mowerhelper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Log.d("WebSocket", "Connection opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed. Code: " + code + ", Reason: " + reason);
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String logData = json.optString("data", "无日志内容");

            // 更新日志内容
            Collections.addAll(last30, logData.split("\n"));
            while (last30.size() > 30) {
                last30.remove(0);
            }

            // 检查是否有时间信息
            String regex = ".*到(\\d{1,2}:\\d{2}:\\d{2})开始工作$";
            Pattern pattern = Pattern.compile(regex);
            next_time = "";
            for (String string : logData.split("\n")) {
                Matcher matcher = pattern.matcher(string);
                if (matcher.matches()) {
                    next_time = matcher.group(1);
                }
            }

            // 更新 UI
            handler.post(() -> {
                mowerLog.setText("");
                mowerLog.append(String.join("\n", last30));
            });

            // 如果有时间信息，启动服务
            if (!Objects.equals(next_time, "")) {
                Intent intent = new Intent(context, MyService.class);
                intent.putExtra("next_time", next_time);
                context.stopService(intent);
                context.startService(intent);
            }
        } catch (JSONException e) {
            handler.post(() -> mowerLog.append("日志解析失败: " + e.getMessage() + "\n"));
        }
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error: " + ex.getMessage());
    }
}

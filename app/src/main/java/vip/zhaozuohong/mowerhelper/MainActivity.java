package vip.zhaozuohong.mowerhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private TextView mowerLog;
    private EditText addressText;
    private Button connectButton;

    private MyWebSocketClient webSocketClient;

    private Handler handler;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mowerLog = findViewById(R.id.mowerLog);
        addressText = findViewById(R.id.addressText);
        connectButton = findViewById(R.id.connectButton);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String inputText = sharedPreferences.getString("addressText", "");
        addressText.setText(inputText);

        handler = new Handler(Looper.getMainLooper());

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectButton.setEnabled(false);
                connectButton.setText("已连接");
                String inputText = addressText.getText().toString();
                String ws_address = "ws://" + inputText + "/log";
                try {
                    URI serverUri = new URI(ws_address);
                    webSocketClient = new MyWebSocketClient(handler, mowerLog, MainActivity.this, serverUri);
                    webSocketClient.connect();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String inputText = addressText.getText().toString();
        editor.putString("addressText", inputText);
        editor.apply();
    }
}
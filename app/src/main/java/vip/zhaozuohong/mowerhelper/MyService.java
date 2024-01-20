package vip.zhaozuohong.mowerhelper;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MyService extends Service {
    private final IBinder binder = new LocalBinder();
    private final Handler handler = new Handler(Looper.getMainLooper());

    String next_time = "";

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int minutes = remainingMinutes();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MyService.this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(MyService.this, MyWidgetProvider.class));
            if (minutes >= 0) {
                String show_text = "下次任务时间：" + next_time + "\n剩余时间：" + remainingMinutes() + "分钟";
                showToast(show_text);
                if (minutes >= 10) {
                    handler.postDelayed(this, 60000);
                } else {
                    handler.postDelayed(this, 10000);
                }
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
                    views.setTextViewText(R.id.infoText, show_text);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            } else {
                showToast("Mower正在运行中……");
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
                    views.setTextViewText(R.id.infoText, "Mower正在工作中……");
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
                stopSelf();
            }
        }
    };

    private int remainingMinutes() {
        Date targetTime;
        try {
            targetTime = sdf.parse(next_time);
        } catch (ParseException e) {
            return -1;
        }
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(targetTime);

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(new Date());

        int target_hour = targetCal.get(Calendar.HOUR);
        int now_hour = nowCal.get(Calendar.HOUR);
        if (target_hour < 6 && now_hour > 6) {
            target_hour += 12;
        }
        int hours = target_hour - now_hour;
        int minutes = targetCal.get(Calendar.MINUTE) - nowCal.get(Calendar.MINUTE);

        int diff = hours * 60 + minutes;

        if (diff == 0) {
            if (targetCal.get(Calendar.SECOND) - nowCal.get(Calendar.SECOND) > 0) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return diff;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void showToast(String info) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyService.this, info, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            next_time = intent.getStringExtra("next_time");
            if (!Objects.equals(next_time, "")) {
                handler.post(runnable);
            } else {
                stopSelf();
            }
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }
}

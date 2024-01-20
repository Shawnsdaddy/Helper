package vip.zhaozuohong.mowerhelper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

//            // 设置小组件的点击事件
//            Intent intent = new Intent(context, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
//
//            // 更新小组件的显示内容
//            views.setTextViewText(R.id.widget_text, "Widget Updated!");

            // 更新小组件
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
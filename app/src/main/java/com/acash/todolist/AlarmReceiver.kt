package com.acash.todolist

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class

AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val todoData=intent.getStringArrayExtra("todo")!!
        val db by lazy{
            TodoDatabase.getDatabase(context)
        }

        val list = GlobalScope.async(Dispatchers.IO) {
            db.todoDao().getTodoByReqCode(todoData[0].toInt())
        }

        runBlocking{
            if (!list.await().isNullOrEmpty()) {
                val nm: NotificationManager? =
                    context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
                val pi = PendingIntent.getActivity(
                    context,
                    121,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    nm?.createNotificationChannel(
                        NotificationChannel(
                            "TodoListAlarm",
                            "TodoList",
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            enableLights(true)
                            enableVibration(true)
                        }
                    )
                }

                val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder(context,"TodoListAlarm")
                } else {
                    Notification.Builder(context)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                }

                val todoNotification = builder
                        .setContentTitle(todoData[1])
                        .setContentText(todoData[2])
                        .setSmallIcon(R.drawable.ic_stat_todo_list_icon)
                        .addAction(R.drawable.ic_stat_todo_list_icon, "OPEN APP", pi)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .build()

                if (nm != null) {
                    nm.notify(System.currentTimeMillis().toInt(), todoNotification)
                }
            }
        }
    }
}

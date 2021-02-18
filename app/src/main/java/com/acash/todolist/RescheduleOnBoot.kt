package com.acash.todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RescheduleOnBoot : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_REBOOT
            || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {

            val db by lazy {
                TodoDatabase.getDatabase(context)
            }

            GlobalScope.launch(Dispatchers.Main) {
                val list = withContext(Dispatchers.IO) {
                    db.todoDao().getAllTodosAfterReboot()
                }

                if (!list.isNullOrEmpty()) {
                    for (todo in list) {
                        if (todo.reqCodeForNotification != null && todo.dateAndTime!! > System.currentTimeMillis()) {

                            val alarmManager =
                                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

                            val i = Intent(context, AlarmReceiver::class.java)

                            i.putExtra(
                                "todo",
                                arrayOf(
                                    todo.reqCodeForNotification.toString(),
                                    todo.title,
                                    todo.description
                                )
                            )

                            val pi =
                                PendingIntent.getBroadcast(
                                    context,
                                    todo.reqCodeForNotification!!,
                                    i,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )

                            when {
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                                    //Wakes up the device in Doze Mode
                                    alarmManager.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        todo.dateAndTime!!,
                                        pi
                                    )
                                }
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                                    //Wakes up the device in Idle Mode
                                    alarmManager.setExact(
                                        AlarmManager.RTC_WAKEUP,
                                        todo.dateAndTime!!,
                                        pi
                                    )
                                }
                                else -> {
                                    //Old APIs
                                    alarmManager.set(
                                        AlarmManager.RTC_WAKEUP,
                                        todo.dateAndTime!!,
                                        pi
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
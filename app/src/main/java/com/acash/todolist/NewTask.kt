package com.acash.todolist

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_new_task.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class NewTask : AppCompatActivity(), View.OnClickListener {
    private var myCalendar:Calendar= Calendar.getInstance()
    private lateinit var dateSetListener:DatePickerDialog.OnDateSetListener
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    private val categories = arrayListOf("Education","Business","Banking","Personal","Insurance","Shopping")
    private lateinit var alarmManager:AlarmManager
    var id:Long=-1L
    private lateinit var todoToBeEdited: TodoModel


    private val db by lazy {
        TodoDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)
        noteSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                tvSwitch.text=getString(R.string.note)
                tvDate.visibility=View.GONE
                tvTime.visibility=View.GONE
                tilDate.visibility=View.GONE
                tilTime.visibility=View.GONE
                etDescription.setLines(15)
            }else{
                tvSwitch.text=getString(R.string.todo)
                tvDate.visibility=View.VISIBLE
                tvTime.visibility=View.VISIBLE
                tilDate.visibility=View.VISIBLE
                tilTime.visibility=View.VISIBLE
                etDescription.setLines(2)
            }
        }

        id=intent.getLongExtra("ID",-1L)
        if(id!=-1L){
            toolbarNewTask.title = "Edit Task"
            GlobalScope.launch(Dispatchers.Main) {
                todoToBeEdited = withContext(Dispatchers.IO) { db.todoDao().getTodoById(id) }
                setTodoToBeEdited()
            }
        }

        etDate.setOnClickListener(this)
        etTime.setOnClickListener(this)
        saveBtn.setOnClickListener(this)
        setSpinner()
        alarmManager=getSystemService(ALARM_SERVICE) as AlarmManager
    }

    private fun setTodoToBeEdited() {
        if(todoToBeEdited.dateAndTime!=null) {
            myCalendar.time = Date(todoToBeEdited.dateAndTime!!)
            updateDate()
            updateTime()
        }else noteSwitch.isChecked=true

        (etTitle as TextView).text=todoToBeEdited.title
        (etDescription as TextView).text=todoToBeEdited.description

        var spId=0
        for(i in 0..categories.size){
            if(categories[i]==todoToBeEdited.category) {
                spId = i
                break
            }
        }
        spinnerCategory.setSelection(spId)
    }

    private fun setSpinner() {
        val spinnerAdapter = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,categories)
        categories.sort()
        spinnerCategory.adapter = spinnerAdapter
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.etDate->{
                setListenerDate()
            }
            R.id.etTime->{
                setListenerTime()
            }
            R.id.saveBtn->{
                if(id==-1L)
                    saveTask()
                else editTask()
            }
        }
    }

    private fun editTask() {
        val todo=createTodoObject()
        todo.id=todoToBeEdited.id
        GlobalScope.launch(Dispatchers.IO) {
            db.todoDao().editTodo(todo)
        }
        finish()
    }

    private fun saveTask() {
        val todo=createTodoObject()
        GlobalScope.launch(Dispatchers.IO){
            db.todoDao().insertTodo(todo)
        }
        finish()
    }

    private fun createTodoObject():TodoModel{
        val title=etTitle.text.toString()
        val description=etDescription.text.toString()
        lateinit var todo:TodoModel
        var reqCode: Int?=null

        if(!noteSwitch.isChecked) {
            if(id!=-1L) {
                    if(myCalendar.time.time==todoToBeEdited.dateAndTime)
                        reqCode = todoToBeEdited.reqCodeForNotification!!
                    else if(todoToBeEdited.dateAndTime!=null){
                        cancelAlarm()
                    }
            }

            if(reqCode==null) {
                //Setting Alarm
                val intent = Intent(this, AlarmReceiver::class.java)
                reqCode = System.currentTimeMillis().toInt()
                intent.putExtra("todo", arrayOf(reqCode.toString(), title, description))
                val pi =
                    PendingIntent.getBroadcast(
                        this,
                        reqCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                alarmManager.set(AlarmManager.RTC_WAKEUP, myCalendar.time.time, pi)
            }

            todo = TodoModel(
                title,
                description,
                spinnerCategory.selectedItem.toString(),
                myCalendar.time.time,
                reqCode
            )
        }else if(noteSwitch.isChecked){
            if(id!=-1L && todoToBeEdited.dateAndTime!=null){
                cancelAlarm()
            }
            todo = TodoModel(
                title,
                description,
                spinnerCategory.selectedItem.toString(),
            )
        }
        return todo
    }

    private fun cancelAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("todo", arrayOf(todoToBeEdited.reqCodeForNotification.toString(), todoToBeEdited.title, todoToBeEdited.description))
        val pi =
            PendingIntent.getBroadcast(
                this,
                todoToBeEdited.reqCodeForNotification!!,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        alarmManager.cancel(pi)
    }

    private fun setListenerDate() {
        dateSetListener=DatePickerDialog.OnDateSetListener{ _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            myCalendar.set(Calendar.YEAR,year)
            myCalendar.set(Calendar.MONTH,month)
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDate()
        }

        val datePickerDialog= DatePickerDialog(this,
            dateSetListener,
            myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate=System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun setListenerTime() {
        timeSetListener=TimePickerDialog.OnTimeSetListener{ _: TimePicker, hourOfDay: Int, min: Int ->
            myCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay)
            myCalendar.set(Calendar.MINUTE,min)
            updateTime()
        }

        val timePickerDialog= TimePickerDialog(this,
            timeSetListener,
            myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE),
            false
        )

        timePickerDialog.show()
    }

    private fun updateDate() {
        val myFormat="EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myFormat)
        etDate.setText(sdf.format(myCalendar.time))
    }

    private fun updateTime() {
        val myFormat="h:mm a"
        val sdf = SimpleDateFormat(myFormat)
        etTime.setText(sdf.format(myCalendar.time))
    }

}
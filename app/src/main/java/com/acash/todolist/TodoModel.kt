package com.acash.todolist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoModel(
    var title: String,
    var description: String,
    var category: String,
    var dateAndTime: Long? = null,
    var reqCodeForNotification: Int? = null,
    var isFinished: Int = 0,

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
)
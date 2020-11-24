package com.acash.todolist

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao{
    @Insert
    suspend fun insertTodo(todo: TodoModel)

    @Query("Select * from TodoModel where isFinished=0")
    fun getAllTodos():LiveData<List<TodoModel>>

    @Query("Select * from TodoModel where id=:uid")
    fun getTodoById(uid:Long):TodoModel

    @Query("Select * from TodoModel where isFinished=1")
    fun getFinishedTasks():LiveData<List<TodoModel>>

    @Query("Update TodoModel Set isFinished=1 where id=:uid")
    fun finishTask(uid:Long)

    @Query("Delete from TodoModel where id=:uid")
    fun deleteTask(uid:Long)

    @Query("Select * from TodoModel where reqCodeForNotification=:code and isFinished=0")
    fun getTodoByReqCode(code: Int):List<TodoModel>

    @Update
    fun editTodo(todo: TodoModel)
}
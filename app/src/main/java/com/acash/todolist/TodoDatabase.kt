package com.acash.todolist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TodoModel::class], version = 8)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var Instance: TodoDatabase? = null
        fun getDatabase(context: Context): TodoDatabase {
            if (Instance != null) {
                return Instance!!
            }

            synchronized(this) {
                Instance = Room.databaseBuilder(
                    context,
                    TodoDatabase::class.java, "Todo.db"
                )
                    .fallbackToDestructiveMigration().build()
                return Instance!!
            }

        }
    }
}

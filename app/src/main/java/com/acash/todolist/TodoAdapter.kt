package com.acash.todolist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_todo.view.*
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(private var list: List<TodoModel>, private val context: Context) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    var onClick: ((id: Long) -> Unit)? = null

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(todomodel: TodoModel) {
            itemView.apply {
                viewTitle.text = todomodel.title
                viewDescription.text = todomodel.description
                viewCategory.text = todomodel.category
                val colors = resources.getIntArray(R.array.random_colors)
                cardTodo.setCardBackgroundColor(colors[todomodel.id.toInt() % 4])
                editButton.setOnClickListener {
                    onClick?.invoke(todomodel.id)
                }
            }

            if (todomodel.dateAndTime != null) {
                updateDate(todomodel.dateAndTime!!)
                updateTime(todomodel.dateAndTime!!)
            } else {
                itemView.viewDate.visibility = View.GONE
                itemView.viewTime.visibility = View.GONE
            }
        }

        private fun updateDate(date: Long) {
            val myFormat = "EEE, d MMM yyyy"
            val sdf = SimpleDateFormat(myFormat)
            itemView.viewDate.text = sdf.format(Date(date))
            itemView.viewDate.visibility = View.VISIBLE
        }

        private fun updateTime(time: Long) {
            val myFormat = "h:mm a"
            val sdf = SimpleDateFormat(myFormat)
            itemView.viewTime.text = sdf.format(Date(time))
            itemView.viewTime.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_todo, parent, false)
        if (context is History)
            itemView.editButton.visibility = View.GONE
        return TodoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return list[position].id
    }
}
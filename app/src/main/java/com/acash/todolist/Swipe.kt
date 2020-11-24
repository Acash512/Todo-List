package com.acash.todolist

import android.content.Context
import android.graphics.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Swipe{
    fun initSwipe(todoAdapter: TodoAdapter,
                  context: Context,
                  db: TodoDatabase,
                  rView: RecyclerView) {

        val itemTouchCallBack = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val paint = Paint()
                    val icon: Bitmap

                    if (dX > 0 && context is MainActivity){
                        icon = BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.ic_check_white_png
                        )
                        paint.color = Color.parseColor("#388E3C")

                        c.drawRect(
                            viewHolder.itemView.left.toFloat(),
                            viewHolder.itemView.top.toFloat(),
                            viewHolder.itemView.left.toFloat() + dX,
                            viewHolder.itemView.bottom.toFloat(), paint
                        )

                        c.drawBitmap(
                            icon, viewHolder.itemView.left.toFloat(),
                            viewHolder.itemView.top.toFloat() + (viewHolder.itemView.bottom.toFloat() - viewHolder.itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                        viewHolder.itemView.translationX = dX
                    } else if (dX < 0) {
                        icon = BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.ic_delete_white_png
                        )
                        paint.color = Color.parseColor("#D32F2F")

                        c.drawRect(
                            viewHolder.itemView.right.toFloat() + dX,
                            viewHolder.itemView.top.toFloat(),
                            viewHolder.itemView.right.toFloat(),
                            viewHolder.itemView.bottom.toFloat(), paint
                        )

                        c.drawBitmap(
                            icon, viewHolder.itemView.right.toFloat() - icon.width.toFloat(),
                            viewHolder.itemView.top.toFloat() + (viewHolder.itemView.bottom.toFloat() - viewHolder.itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                        viewHolder.itemView.translationX = dX
                    }
                } else {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }

            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    GlobalScope.launch(Dispatchers.IO) {
                        db.todoDao().deleteTask(todoAdapter.getItemId(position))
                    }
                } else if (direction == ItemTouchHelper.RIGHT && context is MainActivity) {
                    GlobalScope.launch(Dispatchers.IO) {
                        db.todoDao().finishTask(todoAdapter.getItemId(position))
                    }
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallBack)
        itemTouchHelper.attachToRecyclerView(rView)
    }
}
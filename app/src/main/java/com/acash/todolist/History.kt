package com.acash.todolist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_history.adView
import kotlinx.android.synthetic.main.activity_history.rView
import kotlinx.android.synthetic.main.activity_history.toolbar

class History : AppCompatActivity() {
    private val list = ArrayList<TodoModel>()
    private val todoAdapter = TodoAdapter(list, this)

    private val db by lazy {
        TodoDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        toolbar.title = "History"

        setSupportActionBar(toolbar)

        rView.apply {
            layoutManager = LinearLayoutManager(this@History, LinearLayoutManager.VERTICAL, true)
            adapter = todoAdapter
        }

        Swipe.initSwipe(todoAdapter, this, db, rView)

        db.todoDao().getFinishedTasks().observe(this, {
            if (it.isNotEmpty()) {
                list.clear()
                list.addAll(it)
                todoAdapter.notifyDataSetChanged()
                rView.scrollToPosition(todoAdapter.itemCount - 1)
            } else {
                list.clear()
                todoAdapter.notifyDataSetChanged()
                Toast.makeText(this, "No Todos to display", Toast.LENGTH_SHORT).show()
            }
        })

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        val searchItem = menu?.findItem(R.id.Search)
        val searchView = searchItem?.actionView as SearchView

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                displaySearchedTodo()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                displaySearchedTodo()
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    displaySearchedTodo(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    displaySearchedTodo(newText)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun displaySearchedTodo(newText: String = "") {
        db.todoDao().getFinishedTasks().observe(this, {
            list.clear()
            if (it.isNotEmpty()) {
                val filteredList = it.filter { todo ->
                    todo.title.contains(newText, true)
                }
                list.addAll(filteredList)
                todoAdapter.notifyDataSetChanged()
                if (filteredList.isEmpty()) {
                    Toast.makeText(this, "No search results found", Toast.LENGTH_SHORT).show()
                }
            } else {
                list.clear()
                todoAdapter.notifyDataSetChanged()
                Toast.makeText(this, "No Todos to display", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
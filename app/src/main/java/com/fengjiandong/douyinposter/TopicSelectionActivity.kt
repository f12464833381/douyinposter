package com.fengjiandong.douyinposter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fengjiandong.douyinposter.adapter.SelectionAdapter
import com.fengjiandong.douyinposter.data.MockData

class TopicSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.title_select_topic)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView: RecyclerView = findViewById(R.id.selection_recycler_view)
        val topics = MockData.getTopics()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SelectionAdapter(topics) { selectedTopic ->
            val resultIntent = Intent()
            resultIntent.putExtra("selected_text", selectedTopic)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}

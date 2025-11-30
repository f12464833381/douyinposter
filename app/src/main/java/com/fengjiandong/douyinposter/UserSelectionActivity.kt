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

class UserSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.title_select_user)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView: RecyclerView = findViewById(R.id.selection_recycler_view)

        // 获取完整的用户列表，而不仅仅是名字
        val users = MockData.getUsers()

        recyclerView.layoutManager = LinearLayoutManager(this)
        // Adapter 显示的是昵称，但在点击时我们需要查找对应的 username
        recyclerView.adapter = SelectionAdapter(users.map { it.nickname }) { selectedNickname ->
            // 1. 根据点击的昵称，找到完整的 User 对象
            val selectedUser = users.find { it.nickname == selectedNickname }

            // 2. 构造返回的文本：@username
            val resultText = "@${selectedUser?.username ?: selectedNickname}"

            // 3. 将结果返回给 PostActivity
            val resultIntent = Intent()
            resultIntent.putExtra("selected_text", resultText)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}

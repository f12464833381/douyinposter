package com.fengjiandong.douyinposter.data

import com.fengjiandong.douyinposter.data.User

// 一个单例对象，用于提供模拟数据
object MockData {
    fun getTopics(): List<String> {
        return listOf(
            "#旅游",
            "#美食",
            "#萌宠",
            "#OOTD",
            "#Vlog",
            "#电影",
            "#健身"
        )
    }

    fun getUsers(): List<User> {
        return listOf(
            User("uid1", "zhang_san", "张三"),
            User("uid2", "li_si", "李四"),
            User("uid3", "wang_wu", "王五"),
            User("uid4", "zhao_liu", "赵六"),
            User("uid5", "sun_qi", "孙七")
        )
    }
}

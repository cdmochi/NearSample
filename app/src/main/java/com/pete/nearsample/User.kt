package com.pete.nearsample

import com.google.gson.Gson

data class User(
    var votedName: String = "",
    var totalPoint: Int = 0,
    var previousHash: String = "",
    var timestamp: String = ""
)

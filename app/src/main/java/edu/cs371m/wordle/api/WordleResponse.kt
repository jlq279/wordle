package edu.cs371m.wordle.api

import com.google.gson.annotations.SerializedName

data class NewWordle (
    @SerializedName("word")
    val word: String,
    @SerializedName("letter-count")
    val letterCount: Int
)

data class CheckWordle (
    @SerializedName("word")
    val word: String,
    @SerializedName("result")
    val result: Boolean
)
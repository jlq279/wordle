package edu.cs371m.wordle.api

import retrofit2.http.Query

class Repository(private val api: WordleApi) {
    // XXX Write me.
    suspend fun newWord(): NewWordle {
        return api.newWord()
    }

    suspend fun checkWord(word: String) : CheckWordle {
        return api.checkWord(word)
    }
}
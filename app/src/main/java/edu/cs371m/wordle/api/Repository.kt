package edu.cs371m.wordle.api

class Repository(private val api: WordleApi) {
    suspend fun newWord(length: Int): NewWordle {
        return api.newWord(length)
    }

    suspend fun checkWord(word: String) : CheckWordle {
        return api.checkWord(word)
    }
}
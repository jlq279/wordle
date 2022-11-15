package edu.cs371m.wordle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.wordle.api.CheckWordle
import edu.cs371m.wordle.api.Repository
import edu.cs371m.wordle.api.WordleApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var dictionary = "Default"
    private var guessIndex = MutableLiveData(0)
    private var guess = MutableLiveData("")
    private var guesses = MutableLiveData<List<String>>(emptyList())
    private val api = WordleApi.create()
    private val repository = Repository(api)
    private var result = MutableLiveData<CheckWordle?>()
    private var target = MutableLiveData<String>()
    private var playing = MutableLiveData<Boolean>()
    private var success = MutableLiveData<Boolean>()

    // XXX You need some important member variables
    init {
        // XXX one-liner to kick off the app
        startGame()
    }

    private fun startGame() {
        viewModelScope.launch (
            context = viewModelScope.coroutineContext +
                    Dispatchers.IO) {
            target.postValue(repository.newWord().word)
//            target.postValue("empty")
        }
        playing.value = true
    }

    fun endGame() {
        if (playing.value!!) {
            success.value = guesses.value!![guesses.value!!.size - 1] == target.value
            playing.value = false
        }
    }

    fun isPlaying() : LiveData<Boolean> {
        return playing
    }

    fun isSuccess() : LiveData<Boolean> {
        return success
    }

    fun getTarget(): LiveData<String> {
        return target
    }

    fun checkGuess() {
        viewModelScope.launch (
            context = viewModelScope.coroutineContext +
                    Dispatchers.IO) {
            result.postValue(repository.checkWord(guess.value!!))
        }
    }

    fun submitGuess() {
        guessIndex.value = guessIndex.value!!.plus(1)
        guesses.value = guesses.value?.plus(guess.value!!)
        guess.value = ""
        result.value = null
    }

    fun updateGuess(newGuess: String) {
        guess.value = newGuess
    }

    fun getGuess(): LiveData<String> {
        return guess
    }

    fun observeGuessIndex(): LiveData<Int> {
        return guessIndex
    }

    fun observeResult(): MutableLiveData<CheckWordle?> {
        return result
    }

    fun setDictionary(level: String) {
//        val needsRefresh = dictionary != level.lowercase(Locale.getDefault())
//        dictionary = when(level.lowercase(Locale.getDefault())) {
//            // Sanitize input
//            "easy" -> "easy"
//            "medium" -> "medium"
//            "hard" -> "hard"
//            else -> "medium"
//        }
//        if (needsRefresh) {
//            netRefresh()
//        }
//
//        Log.d(javaClass.simpleName, "level $level END difficulty $dictionary")
    }

//    fun netRefresh() {
//        // XXX Write me.  This is where the network request is initiated.
//        viewModelScope.launch (
//            context = viewModelScope.coroutineContext +
//                    Dispatchers.IO) {
//            questions.postValue(repository.getThree(dictionary))
//        }
//    }
    // XXX Another function is necessary
    fun observeGuesses(): LiveData<List<String>> {
        return guesses
    }
}

package edu.cs371m.wordle

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.wordle.api.CheckWordle
import edu.cs371m.wordle.api.Repository
import edu.cs371m.wordle.api.WordleApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel : ViewModel() {
    private var guessIndex = MutableLiveData(0)
    private var guess = MutableLiveData("")
    private var guesses = MutableLiveData<List<String>>(emptyList())
    private val api = WordleApi.create()
    private val repository = Repository(api)
    private var result = MutableLiveData<CheckWordle?>()
    private var target = MediatorLiveData<String>()
    private var playing = MutableLiveData<Boolean>()
    private var success = MutableLiveData<Boolean>()
    private val storage = Storage()
    private val dbHelp = ViewModelDBHelper()
    private var words = MutableLiveData<List<String>>()
    private var dictionary = "default"
    private var guessLength = MutableLiveData(5)
    private var numGuesses = MutableLiveData(6)
    private var dictionaryMetaList = MutableLiveData<List<DictionaryMeta>>()
    private var dictionaryIdx = 0
    private var lengthIdx = 2
    private var guessesIdx = 2
    private fun noFile() {
        Log.d(javaClass.simpleName, "Function must be initialized to something that can start the file intent")
    }
    private var chooseFileIntent: () -> Unit = ::noFile
    private fun defaultDictionary(@Suppress("UNUSED_PARAMETER") sizeBytes : Long) {
    }
    private var uploadSuccess: (sizeBytes : Long) -> Unit = ::defaultDictionary

    fun startGame(dictionaryUuid : String, wordLength: Int, guessNum: Int) {
        dictionary = dictionaryUuid
        guessLength.value = wordLength
        numGuesses.value = guessNum
        guessIndex.value = 0
        guess.value = ""
        guesses.value = emptyList()
        result.value = null
        target.value = ""
        target.removeSource(words)
        words.value = emptyList()
        if (dictionary == "default") {
            viewModelScope.launch (
                context = viewModelScope.coroutineContext +
                        Dispatchers.IO) {
                target.postValue(repository.newWord(guessLength.value!!).word)
            }
        }
        else {
            storage.getWords(dictionary, guessLength.value!!, words)
            target.addSource(words) {
                if (words.value != null && words.value!!.isNotEmpty()) {
                    val word = words.value!![Random().nextInt(words.value!!.size)]
                    println("target word $word")
                    target.value = word
                }
                else {
                    println("words null or empty")
                }
            }

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
            val checkWordle : CheckWordle = if (dictionary == "default") {
                repository.checkWord(guess.value!!)
            } else {
                CheckWordle(guess.value!!, words.value!!.contains(guess.value!!))
            }
            println("check word: $checkWordle")
            result.postValue(checkWordle)
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

    fun fetchDictionaryMeta() {
        dbHelp.fetchDictionaryMeta(dictionaryMetaList)
    }

    fun observeDictionaryMeta(): LiveData<List<DictionaryMeta>> {
        return dictionaryMetaList
    }

    fun createDictionaryMeta(uuid : String, name: String) {
        val dictionaryMeta = DictionaryMeta(
            uuid = uuid,
            name = name,
        )
        dbHelp.createDictionaryMeta(dictionaryMeta, dictionaryMetaList)
    }

    fun setFileIntent(_chooseFileIntent: () -> Unit) {
        chooseFileIntent = _chooseFileIntent
    }

    fun chooseFile(uuid: String, _uploadSuccess: (Long) -> Unit) {
        uploadSuccess = _uploadSuccess
        chooseFileIntent()
        dictionary = uuid
    }

    fun uploadSuccess(uri: Uri) {
        storage.uploadDictionary(uri, dictionary, uploadSuccess)
        uploadSuccess = ::defaultDictionary
    }

    fun getLength(): MutableLiveData<Int> {
        return guessLength
    }

    fun getGuesses(): MutableLiveData<Int> {
        return numGuesses
    }

    fun observeGuesses(): LiveData<List<String>> {
        return guesses
    }

}

package edu.cs371m.wordle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import edu.cs371m.wordle.databinding.ActivityMainBinding
import edu.cs371m.wordle.databinding.ContentMainBinding
import edu.cs371m.wordle.ui.main.GuessFragment

class MainActivity :
    AppCompatActivity()
{
    private lateinit var contentMainBinding: ContentMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var word = ""
    private var playing = false
    private var dictionary = "default"
    private var targetWord = ""
    private var wordLength = 5
    private var numGuesses = 6
    private var selectedDictionaryPosition = 0
    private var selectedLengthPosition = 2
    private var selectedGuessesPosition = 2

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setKeyboardListeners() {
        viewModel.isPlaying().observe(this){ isPlaying ->
            playing = isPlaying
        }
        viewModel.isSuccess().observe(this){ success ->
            if (success) {
                Toast.makeText(this, "Good work!", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, targetWord.uppercase(), Toast.LENGTH_LONG).show()
            }
        }
        viewModel.getGuess().observe(this){ guess ->
            word = guess
        }
        val letters = arrayOf(contentMainBinding.keyboard.a,contentMainBinding.keyboard.b,contentMainBinding.keyboard.c,contentMainBinding.keyboard.d,contentMainBinding.keyboard.e,contentMainBinding.keyboard.f,contentMainBinding.keyboard.g,contentMainBinding.keyboard.h,contentMainBinding.keyboard.i,contentMainBinding.keyboard.j,contentMainBinding.keyboard.k,contentMainBinding.keyboard.l,contentMainBinding.keyboard.m,contentMainBinding.keyboard.n,contentMainBinding.keyboard.o,contentMainBinding.keyboard.p,contentMainBinding.keyboard.q,contentMainBinding.keyboard.r,contentMainBinding.keyboard.s,contentMainBinding.keyboard.t,contentMainBinding.keyboard.u,contentMainBinding.keyboard.v,contentMainBinding.keyboard.w,contentMainBinding.keyboard.x,contentMainBinding.keyboard.y,contentMainBinding.keyboard.z)
        for (i in letters.indices) {
            letters[i].setOnClickListener {
                if (playing) {
                    if (word.length < wordLength) {
                        word = word.plus(('a'.plus(i)))
                        viewModel.updateGuess(word)
                    }
                }
            }
        }
        var correctLetters = emptyList<Char>()
        var presentLetters = emptyList<Char>()
        viewModel.observeGuesses().observe(this){ guesses ->
            if (guesses.isNotEmpty()) {
                val word = guesses.last()
                for (i in word.indices) {
                    val letter = letters[word[i].minus('a')]
                    val correct = resources.getDrawable(R.drawable.rounded_corners_correct)
                    val present = resources.getDrawable(R.drawable.rounded_corners_present)
                    val incorrect = resources.getDrawable(R.drawable.rounded_corners_incorrect)
                    if (word[i] == targetWord[i]) {
                        letter.background = correct
                        correctLetters = correctLetters.plus(word[i])
                    }
                    else if (targetWord.contains(word[i]) && !correctLetters.contains(word[i])) {
                        letter.background = present
                        presentLetters = presentLetters.plus(word[i])
                    }
                    else if (!correctLetters.contains(word[i]) && !presentLetters.contains(word[i])){
                        letter.background = incorrect
                    }
                }
            }
            else {
                for (i in letters.indices) {
                    letters[i].background = resources.getDrawable(R.drawable.rounded_corners)
                }
            }
        }
        contentMainBinding.keyboard.enter.setOnClickListener{
            if (playing) {
                if (word.length < wordLength) {
                    // Toast
                    Toast.makeText(this, "Not enough letters", Toast.LENGTH_SHORT).show()
                } else {
                    // submit
                    viewModel.checkGuess()
                }
            }
        }
        contentMainBinding.keyboard.delete.setOnClickListener{
            if (playing) {
                if (word.isNotEmpty()) {
                    word = word.dropLast(1)
                    viewModel.updateGuess(word)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBiding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBiding.root)
        activityMainBiding.toolbar.title = "Wordle"
        setSupportActionBar(activityMainBiding.toolbar)
        contentMainBinding = activityMainBiding.contentMain
        val numGuesses = viewModel.getGuesses().value!!
        if (savedInstanceState == null) {
            val ids = listOf(R.id.g1, R.id.g2, R.id.g3, R.id.g4, R.id.g5, R.id.g6, R.id.g7, R.id.g8)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                for (i in 0 until numGuesses) {
                    add(ids[i], GuessFragment.newInstance(i))
                }
            }
            viewModel.getGuesses().observe(this){ guesses ->
                for (fragment in supportFragmentManager.fragments) {
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
                }
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    for (i in 0 until guesses) {
                        add(ids[i], GuessFragment.newInstance(i))
                    }
                }
            }
        }
        viewModel.getTarget().observe(this) { target ->
            targetWord = target
        }
        setKeyboardListeners()
        viewModel.startGame(dictionary, wordLength, numGuesses)
    }

    private var resultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.getTarget().observe(this) { target ->
                    targetWord = target
                }
                dictionary = result.data?.getStringExtra(SettingsManager.dictionaryId) ?: "default"
                wordLength = result.data?.getIntExtra(SettingsManager.wordLength, 5) ?: 5
                numGuesses = result.data?.getIntExtra(SettingsManager.numGuesses, 6) ?: 6
                selectedDictionaryPosition = result.data?.getIntExtra(SettingsManager.dictionaryIdx, 0) ?: 0
                selectedLengthPosition = result.data?.getIntExtra(SettingsManager.lengthIdx, 2) ?: 2
                selectedGuessesPosition = result.data?.getIntExtra(SettingsManager.guessesIdx, 2) ?: 2
                if (result.data?.getBooleanExtra(SettingsManager.changedSetting, false) == true) {
                    viewModel.startGame(dictionary, wordLength, numGuesses)
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_settings) {
            settingsButton(item)
            true
        } else super.onOptionsItemSelected(item)

    }

    private fun settingsButton(@Suppress("UNUSED_PARAMETER") item: MenuItem) {
        item.setOnMenuItemClickListener {
            val intent = Intent(this, SettingsManager::class.java)
            intent.putExtra(SettingsManager.dictionaryIdx, selectedDictionaryPosition)
            intent.putExtra(SettingsManager.lengthIdx, selectedLengthPosition)
            intent.putExtra(SettingsManager.guessesIdx, selectedGuessesPosition)
            resultLauncher.launch(intent)
            true
        }
    }
}

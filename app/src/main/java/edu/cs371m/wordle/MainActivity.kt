package edu.cs371m.wordle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import edu.cs371m.wordle.databinding.ActivityMainBinding
import edu.cs371m.wordle.databinding.ContentMainBinding
import edu.cs371m.wordle.ui.main.GuessFragment

// https://opentdb.com/api_config.php
class MainActivity :
    AppCompatActivity()
{
    companion object {
        val TAG = this::class.java.simpleName
    }
    private val frags = listOf(
        GuessFragment.newInstance(0),
        GuessFragment.newInstance(1),
        GuessFragment.newInstance(2),
        GuessFragment.newInstance(3),
        GuessFragment.newInstance(4),
        GuessFragment.newInstance(5)
    )
    val dictionaryList = listOf("Default")
    private lateinit var contentMainBinding: ContentMainBinding
    private val viewModel: MainViewModel by viewModels() // XXX need to initialize the viewmodel (from an activity)
    private var word = ""
    private var playing = false
    var targetWord = ""

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setKeyboardListeners() {
        viewModel.getTarget().observe(this) { target ->
            targetWord = target
        }
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
            word = guess;
        }
        val letters = arrayOf(contentMainBinding.keyboard.a,contentMainBinding.keyboard.b,contentMainBinding.keyboard.c,contentMainBinding.keyboard.d,contentMainBinding.keyboard.e,contentMainBinding.keyboard.f,contentMainBinding.keyboard.g,contentMainBinding.keyboard.h,contentMainBinding.keyboard.i,contentMainBinding.keyboard.j,contentMainBinding.keyboard.k,contentMainBinding.keyboard.l,contentMainBinding.keyboard.m,contentMainBinding.keyboard.n,contentMainBinding.keyboard.o,contentMainBinding.keyboard.p,contentMainBinding.keyboard.q,contentMainBinding.keyboard.r,contentMainBinding.keyboard.s,contentMainBinding.keyboard.t,contentMainBinding.keyboard.u,contentMainBinding.keyboard.v,contentMainBinding.keyboard.w,contentMainBinding.keyboard.x,contentMainBinding.keyboard.y,contentMainBinding.keyboard.z)
        for (i in letters.indices) {
            letters[i].setOnClickListener {
                if (playing) {
                    if (word.length < 5) {
                        word = word.plus(('a'.plus(i)))
                        viewModel.updateGuess(word)
                    }
                }
            }
        }
        viewModel.observeResult().observe(this){ result ->
            if (result != null) {
                if (result.result) {
                    for (i in result.word.indices) {
                        val letterBackground = letters[result.word[i].minus('a')].background
                        val correct = resources.getDrawable(R.drawable.rounded_corners_correct)
                        val present = resources.getDrawable(R.drawable.rounded_corners_present)
                        val incorrect = resources.getDrawable(R.drawable.rounded_corners_incorrect)
                        if (result.word[i] == targetWord[i]) {
                            letters[result.word[i].minus('a')].background = correct
                        }
                        else if (targetWord.contains(result.word[i]) && letterBackground != correct) {
                            letters[result.word[i].minus('a')].background = present
                        }
                        else if (letterBackground != correct && letterBackground != present){
                            letters[result.word[i].minus('a')].background = incorrect
                        }
                    }
                }
            }
        }
        contentMainBinding.keyboard.enter.setOnClickListener{
            if (playing) {
                if (word.length < 5) {
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
        if (savedInstanceState == null) {
            // XXX Write me: add fragments to layout, swipeRefresh
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.g1, frags[0])
                add(R.id.g2, frags[1])
                add(R.id.g3, frags[2])
                add(R.id.g4, frags[3])
                add(R.id.g5, frags[4])
                add(R.id.g6, frags[5])
            }
        }
        setKeyboardListeners()
//        // Please enjoy this code that manages the spinner
//        // Create an ArrayAdapter using a simple spinner layout and languages array
//        val aa = ArrayAdapter(this, R.layout.spinner_list, dictionaryList)
//        // Set layout to use when the list of choices appear
////        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        aa.setDropDownViewResource(R.layout.spinner_list)
//        // Create the object as we are assigning it
//        contentMainBinding.spinner.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>,
//                                        view: View, position: Int, id: Long) {
//                Log.d(TAG, "pos $position")
////                viewModel.setDictionary(dictionaryList[position])
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                Log.d(TAG, "onNothingSelected")
//            }
//        }
//        // Set Adapter to Spinner
//        contentMainBinding.spinner.adapter = aa
//        // Set initial value of spinner to medium
//        val initialSpinner = 0
//        contentMainBinding.spinner.setSelection(initialSpinner)
////        viewModel.setDictionary(dictionaryList[initialSpinner])
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        return if (id == R.id.action_settings) {
            settingsButton(item)
            true
        } else super.onOptionsItemSelected(item)

    }

    private fun settingsButton(@Suppress("UNUSED_PARAMETER") item: MenuItem) {
        // XXX Write me
//        item.setOnMenuItemClickListener {
//            val intent = Intent(this, SettingsManager::class.java)
//            intent.putExtra(SettingsManager.doLoopKey, looping)
//            intent.putExtra(SettingsManager.songsPlayedKey, songsPlayed)
//            resultLauncher.launch(intent)
//            true
//        }
    }
}

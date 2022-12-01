package edu.cs371m.wordle

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.cs371m.wordle.databinding.ActivitySettingsBinding
import edu.cs371m.wordle.databinding.ContentSettingsBinding
import java.util.*


class SettingsManager : AppCompatActivity() {
    companion object {
        const val dictionaryId = "dictionaryId"
        const val wordLength = "wordLength"
        const val numGuesses = "numGuesses"
        const val changedSetting = "changed"
        const val dictionaryIdx = "dictionaryIdx"
        const val lengthIdx = "lengthIdx"
        const val guessesIdx = "guessesIdx"
    }
    private lateinit var binding : ContentSettingsBinding
    private val viewModel: MainViewModel by viewModels()
    private var dictionary = "default"
    private var length = 5
    private var guesses = 6
    private var dictionaryMetaList = emptyList<DictionaryMeta>()
    private var dictionaryList = listOf("Default")
    private var selectedDictionaryPosition = 0
    val lengthList = listOf(3, 4, 5, 6, 7)
    private var selectedLengthPosition = 2
    val guessesList = listOf(4, 5, 6, 7, 8)
    private var selectedGuessesPosition = 2
    private var changedDictionary = false
    private var changedLength = false
    private var changedGuesses = false

    override fun onCreate(savedInstanceState: Bundle?) {
        selectedDictionaryPosition = intent.getIntExtra(dictionaryIdx, 0)
        selectedLengthPosition = intent.getIntExtra(lengthIdx, 2)
        selectedGuessesPosition = intent.getIntExtra(guessesIdx, 2)
        super.onCreate(savedInstanceState)
        val activitySettingsBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(activitySettingsBinding.root)
        setSupportActionBar(activitySettingsBinding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding = activitySettingsBinding.contentSettings
        // dictionary spinner
        var dictionaryAdapter = ArrayAdapter(this, R.layout.spinner_list, dictionaryList)
        dictionaryAdapter.setDropDownViewResource(R.layout.spinner_list)
        binding.dictionary.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                changedDictionary = selectedDictionaryPosition != position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.dictionary.adapter = dictionaryAdapter
        binding.dictionary.setSelection(0)

        // length spinner
        val lengthAdapter = ArrayAdapter(this, R.layout.spinner_list, lengthList)
        lengthAdapter.setDropDownViewResource(R.layout.spinner_list)
        binding.length.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                changedLength = selectedLengthPosition != position
                length = lengthList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.length.adapter = lengthAdapter
        binding.length.setSelection(selectedLengthPosition)

        // guesses spinner
        val guessesAdapter = ArrayAdapter(this, R.layout.spinner_list, guessesList)
        guessesAdapter.setDropDownViewResource(R.layout.spinner_list)
        binding.guesses.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                changedGuesses = selectedGuessesPosition != position
                guesses = guessesList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.guesses.adapter = guessesAdapter
        binding.guesses.setSelection(selectedGuessesPosition)

        binding.importButton.setOnClickListener {
            val text = binding.dictionaryName.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, "You must name dictionary", Toast.LENGTH_SHORT).show()
            }
            else {
                val uuid = UUID.randomUUID().toString()
                viewModel.chooseFile(uuid) {
                    viewModel.createDictionaryMeta(
                        uuid,
                        text,
                    )
                }
            }
        }

        binding.cancelButton.setOnClickListener{
            cancelButton()
        }
        binding.okButton.setOnClickListener{
            okButton()
        }

        viewModel.setFileIntent(::chooseFileIntent)
        viewModel.observeDictionaryMeta().observe(this) { dictionaryMetas ->
            dictionaryMetaList = dictionaryMetas
            dictionaryList = listOf("Default")
            dictionaryMetas.forEach { dictionaryMeta -> dictionaryList = dictionaryList.plus(dictionaryMeta.name) }
            dictionaryAdapter = ArrayAdapter(this, R.layout.spinner_list, dictionaryList)
            dictionaryAdapter.setDropDownViewResource(R.layout.spinner_list)
            binding.dictionary.adapter = dictionaryAdapter
            binding.dictionary.setSelection(selectedDictionaryPosition)

        }
        viewModel.fetchDictionaryMeta()

    }

    private fun chooseFileIntent() {
        val fileIntent = Intent(Intent.ACTION_GET_CONTENT)
        fileIntent.type = "text/*"
        filePickerLauncher.launch(Intent.createChooser(fileIntent, "Select a file"))
    }

    private var filePickerLauncher = registerForActivityResult( ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedFileUri = result.data!!.data
            viewModel.uploadSuccess(selectedFileUri!!)
        }
    }

    private fun cancelButton() {
        doFinish(false, "", 0, 0, 0, 0, 0, false)
    }

    private fun okButton() {
        selectedDictionaryPosition = binding.dictionary.selectedItemPosition
        dictionary = if (selectedDictionaryPosition == 0) "default" else dictionaryMetaList[selectedDictionaryPosition - 1].uuid
        selectedLengthPosition = binding.length.selectedItemPosition
        length = lengthList[selectedLengthPosition]
        selectedGuessesPosition = binding.guesses.selectedItemPosition
        guesses = guessesList[selectedGuessesPosition]
        doFinish(true, dictionary, length, guesses, selectedDictionaryPosition, selectedLengthPosition, selectedGuessesPosition, changedDictionary || changedLength || changedGuesses)
    }

    private fun doFinish(save: Boolean, dict: String, len: Int, num: Int, dictIdx: Int, lenIdx: Int, numIdx: Int, changed: Boolean) {
        val intent = Intent()
        if (save) {
            intent.putExtra(dictionaryId, dict)
            intent.putExtra(wordLength, len)
            intent.putExtra(numGuesses, num)
            intent.putExtra(changedSetting, changed)
            intent.putExtra(dictionaryIdx, dictIdx)
            intent.putExtra(lengthIdx, lenIdx)
            intent.putExtra(guessesIdx, numIdx)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == android.R.id.home) {
            okButton()
            true
        } else super.onOptionsItemSelected(item)
    }
}

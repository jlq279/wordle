package edu.cs371m.wordle.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import edu.cs371m.wordle.MainViewModel
import edu.cs371m.wordle.R
import edu.cs371m.wordle.databinding.GuessBinding


class GuessFragment : Fragment() {
    companion object {
        const val idKey = "idKey"
        fun newInstance(id: Int): GuessFragment {
            val b = Bundle()
            b.putInt(idKey, id)
            val frag = GuessFragment()
            frag.arguments = b
            return frag
        }
    }
    private var _binding : GuessBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GuessBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var numLetters = viewModel.getLength().value!!
        val numGuesses = viewModel.getGuesses().value!!
        val guess = binding.guess
        guess.columnCount = numLetters
        guess.rowCount = 1
        var chars = emptyList<View>()
        for (i in 0 until numLetters) {
            val letter = layoutInflater.inflate(R.layout.letter, null)
            val param = GridLayout.LayoutParams()
            param.height = TableLayout.LayoutParams.MATCH_PARENT
            param.width = TableLayout.LayoutParams.WRAP_CONTENT
            letter.layoutParams = param
            chars = chars.plus(letter)
            guess.addView(letter)
        }

        viewModel.getLength().observe(viewLifecycleOwner){ length ->
            guess.removeAllViews()
            numLetters = length
            guess.columnCount = numLetters
            guess.rowCount = 1
            chars = emptyList()
            for (i in 0 until numLetters) {
                val letter = layoutInflater.inflate(R.layout.letter, null)
                val param = GridLayout.LayoutParams()
                param.height = TableLayout.LayoutParams.MATCH_PARENT
                param.width = TableLayout.LayoutParams.WRAP_CONTENT
                letter.layoutParams = param
                chars = chars.plus(letter)
                guess.addView(letter)
            }
        }

        val arguments = requireArguments()
        var targetWord = ""
        val targetMap = HashMap<Char, Int>()
        viewModel.getTarget().observe(viewLifecycleOwner){ target ->
            if (target.isNotBlank()) {
                targetWord = target
                targetMap.clear()
                for (i in chars.indices) {
                    targetMap[targetWord[i]] = (targetMap[targetWord[i]] ?: 0).plus(1)
                }
            }
        }
        var guessIndex = 0
        viewModel.observeGuessIndex().observe(viewLifecycleOwner){ guessIdx ->
            guessIndex = guessIdx
        }
        viewModel.getGuess().observe(viewLifecycleOwner){ guessStr ->
            if (guessIndex == arguments.getInt(idKey)) {
                for (i in chars.indices) {
                    chars[i].findViewById<TextView>(R.id.front).text =
                        if (guessStr.length <= i) "" else guessStr[i].uppercaseChar().toString()
                }
            }
        }
        viewModel.observeResult().observe(viewLifecycleOwner){ result ->
            if (guessIndex == arguments.getInt(idKey)) {
                if (result != null) {
                    if (result.result) {
                        val targetMapCopy = HashMap<Char,Int>(targetMap)
                        var indices = emptyList<Int>()
                        for (i in chars.indices) {
                            if (result.word[i] == targetWord[i]) {
                                targetMapCopy[targetWord[i]] = (targetMapCopy[targetWord[i]] ?: 0).minus(1)
                                indices = indices.plus(i)
                                chars[i].setBackgroundColor(Color.parseColor("#538d4e"))
                            }
                        }
                        for (i in chars.indices) {
                            if (result.word[i] != targetWord[i]) {
                                if (targetMapCopy[result.word[i]] != null && targetMapCopy[result.word[i]]!! > 0) {
                                    targetMapCopy[result.word[i]] =
                                        (targetMapCopy[result.word[i]] ?: 0).minus(1)
                                    chars[i].setBackgroundColor(Color.parseColor("#b59f3b"))
                                } else {
                                    chars[i].setBackgroundColor(Color.parseColor("#3a3a3c"))
                                }
                            }
                        }
                        viewModel.submitGuess()
                    } else {
                        Toast.makeText(activity, "Not in word list", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        viewModel.observeGuesses().observe(viewLifecycleOwner){ guesses ->
            if (guesses.size == numGuesses || (guesses.isNotEmpty() && guesses[guesses.size - 1] == targetWord)) {
                viewModel.endGame()
            }
            if (arguments.getInt(idKey) < guesses.size) {
                val guessStr = guesses[arguments.getInt(idKey)]
                for (i in guessStr.indices) {
                    chars[i].findViewById<TextView>(R.id.front).text = guessStr[i].uppercaseChar().toString()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

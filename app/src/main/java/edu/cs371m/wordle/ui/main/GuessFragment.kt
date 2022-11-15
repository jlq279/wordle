package edu.cs371m.wordle.ui.main

import android.animation.AnimatorSet
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import edu.cs371m.wordle.MainViewModel
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
    lateinit var front_anim:AnimatorSet
    lateinit var back_anim: AnimatorSet
    // XXX initialize the viewModel
//    val chars = arrayOf(binding.c1, binding.c2, binding.c3, binding.c4, binding.c5)
//    private fun setClickListeners(question: WordleResponse, tv: TextView, tb: Button, fb: Button) {
//        // XXX Write me Color.GREEN for correct, Color.RED for incorrect
//        tb.setOnClickListener{
//            if (question.correctAnswer) {
//                tv.setBackgroundColor(Color.GREEN)
//            }
//            else {
//                tv.setBackgroundColor(Color.RED)
//            }
//        }
//        fb.setOnClickListener{
//            if (question.correctAnswer) {
//                tv.setBackgroundColor(Color.RED)
//            }
//            else {
//                tv.setBackgroundColor(Color.GREEN)
//            }
//        }
//    }
    // Corrects some ugly HTML encodings
//    private fun fromHtml(source: String): String {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString()
//        } else {
//            @Suppress("DEPRECATION")
//            return Html.fromHtml(source).toString()
//        }
//    }

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
//        val letter = layoutInflater.inflate(edu.cs371m.wordle.R.layout.letter, null)
//        val param = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            1.0f
//        )
//        letter.layoutParams = param
//        val main = findViewById(edu.cs371m.wordle.R.id.guess) as ViewGroup
//        main.addView(view, 0)
        // XXX Write me.  viewModel should observe something
        // When it gets what it is observing, it should index into it
        // You might find the requireArguments() function useful
        // You should let MainActivity know to "turn off" the swipe
        // refresh spinner.
        val arguments = requireArguments()
        val chars = arrayOf(binding.c1, binding.c2, binding.c3, binding.c4, binding.c5)
        var targetWord = ""
        val targetMap = HashMap<Char, Int>()
        viewModel.getTarget().observe(viewLifecycleOwner){ target ->
            targetWord = target
            for (i in chars.indices) {
                targetMap[targetWord[i]] = (targetMap[targetWord[i]] ?: 0).plus(1)
            }
        }
        var guessIndex = 0
        viewModel.observeGuessIndex().observe(viewLifecycleOwner){ guessIdx ->
            guessIndex = guessIdx
        }
        viewModel.getGuess().observe(viewLifecycleOwner){ guess ->
            if (guessIndex == arguments.getInt(idKey)) {
                for (i in chars.indices) {
                    chars[i].text =
                        if (guess.length <= i) "" else guess[i].uppercaseChar().toString()
                }
            }
        }
        viewModel.observeResult().observe(viewLifecycleOwner){ result ->
            if (guessIndex == arguments.getInt(idKey)) {
                if (result != null) {
                    if (result.result) {
                        var indices = emptyList<Int>()
                        for (i in chars.indices) {
                            if (result.word[i] == targetWord[i]) {
                                targetMap[targetWord[i]] = (targetMap[targetWord[i]] ?: 0).minus(1)
                                indices = indices.plus(i)
                                chars[i].setBackgroundColor(Color.parseColor("#538d4e"))
                            }
                        }
                        for (i in chars.indices) {
                            if (!indices.contains(i)) {
                                if (targetMap[result.word[i]]!! > 0) {
                                    targetMap[result.word[i]] =
                                        (targetMap[result.word[i]] ?: 0).minus(1)
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
            if (guesses.size == 6 || (guesses.isNotEmpty() && guesses[guesses.size - 1] == targetWord)) {
                viewModel.endGame()
            }
            if (arguments.getInt(idKey) < guesses.size) {
                val guess = guesses[arguments.getInt(idKey)]
                for (i in guess.indices) {
                    chars[i].text = guess[i].uppercaseChar().toString()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

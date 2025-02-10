package com.serwylo.lexica

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.serwylo.lexica.databinding.NewGameModeBinding
import com.serwylo.lexica.db.Database
import com.serwylo.lexica.db.GameMode

class NewGameModeActivity : AppCompatActivity() {

    private lateinit var binding: NewGameModeBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        // Not sure why, but this has to come before super.onCreate(), or else the light theme
        // will have a dark background, making the text very hard to read. This seems to fix it,
        // though I have not investigated why. Source: https://stackoverflow.com/a/15657428.
        ThemeManager.getInstance().applyTheme(this)

        super.onCreate(savedInstanceState)

        binding = NewGameModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.new_game_mode_menu, binding.toolbar.menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save && binding.time.isValid()) {
            createNewGameMode()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun TextInputEditText.isValid() : Boolean = with(this.text.toString()) {
        return@with when {
            isBlank() -> {
                binding.root.showSnackbar(context.getString(R.string.time_limit_must_be_set))
                false
            }
            toLong() * 60 > MAX_TIME_LIMIT_IN_MINUTES -> {
                binding.root.showSnackbar(context.getString(R.string.max_time_limit))
                false
            }
            Integer.parseInt(this) <= 0 -> {
                binding.root.showSnackbar(context.getString(R.string.time_limit_too_short))
                false
            }
            else -> true
        }
    }

    private fun View.showSnackbar(text: String) {
        Snackbar.make(this, text, Snackbar.LENGTH_LONG).show()
    }

    private fun createNewGameMode() {
        val boardSize = selectedBoardSize()
        if (boardSize == -1) {
            showError()
            return
        }

        val minWordLength = selectedMinWordLength()
        if (minWordLength == -1) {
            showError()
            return
        }

        val scoreType = selectedScoreType()
        if (scoreType == null) {
            showError()
            return
        }

        val label = selectedLabel()
        if (label.isEmpty()) {
            showError()
            return
        }

        val gameMode = GameMode(
            gameModeId = 0,
            type = GameMode.Type.CUSTOM,
            customLabel = selectedLabel(),
            boardSize = boardSize,
            timeLimitSeconds = selectedTimeLimitInSeconds(),
            minWordLength = minWordLength,
            scoreType = scoreType,
            hintMode = selectedHintMode(),
        )

        Database.writeExecutor.execute {
            val dao = Database.get(this).gameModeDao()
            dao.insert(gameMode)
            onSaveComplete()
        }
    }

    private fun showError() {
        Toast.makeText(this, "All fields required.", Toast.LENGTH_SHORT).show()
    }

    private fun onSaveComplete() {
        finish()
    }

    private fun selectedLabel(): String {
        return binding.label.text?.toString() ?: ""
    }

    private fun selectedTimeLimitInSeconds(): Int = binding.time.text.toString().toInt() * 60

    private fun selectedBoardSize(): Int {
        return when {
            binding.boardSize4x4.isChecked -> 4 * 4
            binding.boardSize5x5.isChecked -> 5 * 5
            binding.boardSize6x6.isChecked -> 6 * 6
            else -> -1
        }
    }

    private fun selectedMinWordLength(): Int {
        return when {
            binding.minWordLength3.isChecked -> 3
            binding.minWordLength4.isChecked -> 4
            binding.minWordLength5.isChecked -> 5
            binding.minWordLength6.isChecked -> 6
            else -> -1
        }
    }

    private fun selectedScoreType(): String? {
        return when {
            binding.scoreTypeLetterPoints.isChecked -> GameMode.SCORE_LETTERS
            binding.scoreTypeWordLength.isChecked -> GameMode.SCORE_WORDS
            else -> null
        }
    }

    private fun selectedHintMode(): String {

        val count = binding.hintTileCount.isChecked
        val colour = binding.hintColour.isChecked

        if (count && colour) {
            return "hint_both"
        } else if (colour) {
            return "hint_colour"
        } else if (count) {
            return "tile_count"
        }

        return "hint_none"

    }

    private companion object {
        const val MAX_TIME_LIMIT_IN_MINUTES = 2592000
    }
}
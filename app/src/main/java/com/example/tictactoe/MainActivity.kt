package com.example.tictactoe

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.CycleInterpolator
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat

class MainActivity : AppCompatActivity() {

    private var boardState = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )

    private var activePlayer = 1
    private var isGameActive = true
    private lateinit var statusText: TextView
    private lateinit var buttons: Array<Array<Button>>
    private lateinit var resetButton: Button
    private lateinit var settingsButton: Button
    private lateinit var rootLayout: View
    private lateinit var gameBoard: GridLayout
    private lateinit var resultCard: View
    private lateinit var resultScrim: View
    private lateinit var resultTitle: TextView
    private lateinit var resultSubtitle: TextView
    private lateinit var playAgainButton: Button
    private lateinit var closePopupButton: Button
    private lateinit var prefs: PreferenceHelper
    private var palette: ThemePalette? = null
    private var defaultCellBackground: Drawable? = null
    private var lastWinCells: List<Pair<Int, Int>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceHelper(this)
        bindViews()
        initializeButtons()
        setupActions()
        applyTheme()
        updateStatusText()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        updateStatusText()
    }

    private fun bindViews() {
        statusText = findViewById(R.id.text_status)
        resetButton = findViewById(R.id.button_reset)
        settingsButton = findViewById(R.id.button_settings)
        rootLayout = findViewById(R.id.root_layout)
        gameBoard = findViewById(R.id.game_board)
        resultCard = findViewById(R.id.result_card)
        resultScrim = findViewById(R.id.result_scrim)
        resultTitle = findViewById(R.id.result_title)
        resultSubtitle = findViewById(R.id.result_subtitle)
        playAgainButton = findViewById(R.id.button_play_again)
        closePopupButton = findViewById(R.id.button_close_popup)
    }

    private fun initializeButtons() {
        buttons = Array(3) { r ->
            Array(3) { c ->
                findViewById<Button>(resources.getIdentifier("button_${r}${c}", "id", packageName))
            }
        }
        defaultCellBackground = tintedCellDrawable()
        buttons.forEach { row ->
            row.forEach { button ->
                button.background = defaultCellBackground?.constantState?.newDrawable()?.mutate()
            }
        }
    }

    private fun setupActions() {
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        playAgainButton.setOnClickListener {
            hideResultCard()
            resetGame(it)
        }
        closePopupButton.setOnClickListener { hideResultCard() }
    }

    fun onBoardClick(view: View) {
        if (!isGameActive) return

        val button = view as Button
        val buttonId = resources.getResourceEntryName(button.id)
        val row = buttonId[7].toString().toInt()
        val col = buttonId[8].toString().toInt()

        if (boardState[row][col] != 0) return

        animateTap(button)

        if (activePlayer == 1) {
            button.text = "X"
            boardState[row][col] = 1
        } else {
            button.text = "O"
            boardState[row][col] = 2
        }
        fadeIn(button)

        val winCells = checkForWin()
        if (winCells != null) {
            isGameActive = false
            lastWinCells = winCells
            highlightWin(winCells)
            showResult("Player $activePlayer Wins!", true)
            return
        }

        if (isBoardFull()) {
            isGameActive = false
            showResult("It's a Draw!", false)
            shakeBoard()
            return
        }

        activePlayer = if (activePlayer == 1) 2 else 1
        updateStatusText()
    }

    private fun checkForWin(): List<Pair<Int, Int>>? {
        val p = activePlayer
        for (i in 0..2) {
            if (boardState[i][0] == p && boardState[i][1] == p && boardState[i][2] == p) return listOf(i to 0, i to 1, i to 2)
        }
        for (j in 0..2) {
            if (boardState[0][j] == p && boardState[1][j] == p && boardState[2][j] == p) return listOf(0 to j, 1 to j, 2 to j)
        }
        if (boardState[0][0] == p && boardState[1][1] == p && boardState[2][2] == p) return listOf(0 to 0, 1 to 1, 2 to 2)
        if (boardState[0][2] == p && boardState[1][1] == p && boardState[2][0] == p) return listOf(0 to 2, 1 to 1, 2 to 0)
        return null
    }

    private fun isBoardFull(): Boolean = boardState.all { row -> row.none { it == 0 } }

    private fun updateStatusText() {
        if (isGameActive) statusText.text = "Player $activePlayer Turn" else statusText.text = "Game Over"
    }

    fun resetGame(view: View) {
        activePlayer = 1
        isGameActive = true
        boardState = arrayOf(
            intArrayOf(0, 0, 0),
            intArrayOf(0, 0, 0),
            intArrayOf(0, 0, 0)
        )
        buttons.forEach { row ->
            row.forEach { button ->
                button.text = ""
                button.alpha = 1f
                button.scaleX = 1f
                button.scaleY = 1f
                button.background = defaultCellBackground?.constantState?.newDrawable()?.mutate()
            }
        }
        lastWinCells = null
        hideResultCard()
        applyTheme()
        updateStatusText()
    }

    private fun highlightWin(cells: List<Pair<Int, Int>>) {
        val highlight = ContextCompat.getDrawable(this, R.drawable.win_highlight_background)?.mutate()
        val strokeColor = palette?.winStroke ?: ContextCompat.getColor(this, R.color.teal_200)
        highlight?.let { DrawableCompat.setTint(it, strokeColor) }
        cells.forEach { (r, c) ->
            buttons[r][c].background = highlight?.constantState?.newDrawable()?.mutate()
            buttons[r][c].animate().scaleX(1.08f).scaleY(1.08f).setDuration(180).start()
        }
    }

    private fun showResult(title: String, isWin: Boolean) {
        resultTitle.text = title
        resultSubtitle.text = if (isWin) "Great finish!" else "So close - play again!"
        resultScrim.visibility = View.VISIBLE
        resultCard.visibility = View.VISIBLE
        resultCard.alpha = 0f
        resultCard.scaleX = 0.85f
        resultCard.scaleY = 0.85f
        resultCard.animate()
            .alpha(1f)
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(260)
            .withEndAction {
                resultCard.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }
            .start()
        playTone(if (isWin) ToneGenerator.TONE_CDMA_SIGNAL_OFF else ToneGenerator.TONE_SUP_PIP, 250)
        vibrate(90)
    }

    private fun hideResultCard() {
        resultScrim.visibility = View.GONE
        resultCard.visibility = View.GONE
    }

    private fun animateTap(button: Button) {
        button.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(80)
            .withEndAction {
                button.animate().scaleX(1f).scaleY(1f).setDuration(110).start()
            }
            .start()
    }

    private fun fadeIn(button: Button) {
        button.alpha = 0f
        button.animate().alpha(1f).setDuration(150).start()
    }

    private fun shakeBoard() {
        gameBoard.animate()
            .translationXBy(12f)
            .setDuration(400)
            .setInterpolator(CycleInterpolator(4f))
            .withEndAction { gameBoard.translationX = 0f }
            .start()
    }

    private fun playTone(tone: Int, durationMs: Int) {
        if (!prefs.isSoundEnabled()) return
        ToneGenerator(AudioManager.STREAM_MUSIC, 80).apply {
            startTone(tone, durationMs)
            release()
        }
    }

    private fun vibrate(durationMs: Long) {
        if (!prefs.isVibrationEnabled()) return
        val vibrator = ContextCompat.getSystemService(this, Vibrator::class.java) ?: return
        val effect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }

    private fun applyTheme() {
        palette = ThemeManager.palette(this, prefs.getTheme())
        val p = palette ?: return
        rootLayout.setBackgroundColor(p.background)
        statusText.setTextColor(p.textPrimary)
        resultTitle.setTextColor(p.textPrimary)
        resultSubtitle.setTextColor(ColorUtils.setAlphaComponent(p.textPrimary, 210))
        resetButton.setBackgroundColor(p.accent)
        resetButton.setTextColor(p.buttonText)
        settingsButton.setBackgroundColor(ColorUtils.setAlphaComponent(p.accent, 200))
        settingsButton.setTextColor(p.textPrimary)
        resultCard.background = ContextCompat.getDrawable(this, R.drawable.popup_background)?.mutate()?.also {
            DrawableCompat.setTint(it, ColorUtils.setAlphaComponent(p.buttonBackground, 245))
        }
        applyCellStyles(p)
        lastWinCells?.let { highlightWin(it) }
    }

    private fun applyCellStyles(palette: ThemePalette) {
        defaultCellBackground = tintedCellDrawable()
        val baseDrawable = defaultCellBackground ?: return
        buttons.forEach { row ->
            row.forEach { button ->
                button.setTextColor(palette.textPrimary)
                button.background = baseDrawable.constantState?.newDrawable()?.mutate()
            }
        }
    }

    private fun tintedCellDrawable() =
        ContextCompat.getDrawable(this, R.drawable.board_cell_background)?.mutate()?.also { drawable ->
            val tintColor = palette?.accent ?: ContextCompat.getColor(this, R.color.light_accent)
            DrawableCompat.setTint(drawable, tintColor)
    }
}
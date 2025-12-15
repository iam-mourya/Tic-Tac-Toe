package com.example.tictactoe

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = PreferenceHelper(this)

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group_theme)
        val soundSwitch = findViewById<SwitchMaterial>(R.id.switch_sound)
        val vibrationSwitch = findViewById<SwitchMaterial>(R.id.switch_vibration)
        val root = findViewById<ConstraintLayout>(R.id.settings_root)
        val radioLight = findViewById<RadioButton>(R.id.radio_light)
        val radioDark = findViewById<RadioButton>(R.id.radio_dark)
        val radioNeon = findViewById<RadioButton>(R.id.radio_neon)
        val title = findViewById<TextView>(R.id.text_title)
        val themeLabel = findViewById<TextView>(R.id.text_theme_label)

        when (prefs.getTheme()) {
            ThemeOption.LIGHT -> radioGroup.check(R.id.radio_light)
            ThemeOption.DARK -> radioGroup.check(R.id.radio_dark)
            ThemeOption.NEON -> radioGroup.check(R.id.radio_neon)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selection = when (checkedId) {
                R.id.radio_dark -> ThemeOption.DARK
                R.id.radio_neon -> ThemeOption.NEON
                else -> ThemeOption.LIGHT
            }
            prefs.setTheme(selection)
            applyPreview(selection, root, title, themeLabel, radioLight, radioDark, radioNeon, soundSwitch, vibrationSwitch)
        }

        soundSwitch.isChecked = prefs.isSoundEnabled()
        vibrationSwitch.isChecked = prefs.isVibrationEnabled()

        soundSwitch.setOnCheckedChangeListener { _, isChecked -> prefs.setSoundEnabled(isChecked) }
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked -> prefs.setVibrationEnabled(isChecked) }

        findViewById<Button>(R.id.button_done).setOnClickListener { finish() }

        applyPreview(prefs.getTheme(), root, title, themeLabel, radioLight, radioDark, radioNeon, soundSwitch, vibrationSwitch)
    }

    private fun applyPreview(
        option: ThemeOption,
        root: ConstraintLayout,
        title: TextView,
        themeLabel: TextView,
        radioLight: RadioButton,
        radioDark: RadioButton,
        radioNeon: RadioButton,
        sound: SwitchMaterial,
        vibration: SwitchMaterial
    ) {
        val palette = ThemeManager.palette(this, option)
        root.setBackgroundColor(palette.background)
        val done = root.findViewById<Button>(R.id.button_done)
        done.setBackgroundColor(palette.accent)
        done.setTextColor(palette.buttonText)
        val primary = palette.textPrimary
        title.setTextColor(primary)
        themeLabel.setTextColor(primary)
        radioLight.setTextColor(primary)
        radioDark.setTextColor(primary)
        radioNeon.setTextColor(primary)
        sound.setTextColor(primary)
        vibration.setTextColor(primary)
    }
}


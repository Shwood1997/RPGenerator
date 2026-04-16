package com.example.rpgenerator

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// This is the main "brain" of the app. It connects the screen to the logic.
class MainActivity : AppCompatActivity() {

    // The ViewModel holds our data (like stats) so they don't disappear when the screen rotates.
    private val viewModel: MainViewModel by viewModels()

    // These variables represent the different things you see on the screen.
    private lateinit var characterImage: ImageView
    private lateinit var genderGroup: RadioGroup
    private lateinit var classSpinner: Spinner
    private lateinit var raceSpinner: Spinner
    private lateinit var nameInput: EditText
    
    private lateinit var allocatedPointsText: TextView
    private lateinit var remainingPointsText: TextView
    
    private lateinit var resetButton: Button
    private lateinit var generateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Makes the app use the full screen
        setContentView(R.layout.activity_main)
        
        // This ensures the app doesn't hide behind system bars like the notch
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Connecting our variables to the actual items in the layout file
        characterImage = findViewById(R.id.characterImage)
        genderGroup = findViewById(R.id.genderGroup)
        classSpinner = findViewById(R.id.classSpinner)
        raceSpinner = findViewById(R.id.raceSpinner)
        nameInput = findViewById(R.id.nameInput)
        allocatedPointsText = findViewById(R.id.allocatedPoints)
        remainingPointsText = findViewById(R.id.remainingPoints)
        resetButton = findViewById(R.id.resetButton)
        generateButton = findViewById(R.id.generateButton)

        // Run the setup functions
        setupTitle()
        setupSpinners()
        setupAttributeRows()
        observeViewModel()

        // Set what happens when you click the buttons
        resetButton.setOnClickListener { resetFields() }
        generateButton.setOnClickListener { updateCharacterImage() }
    }

    // Adds the fancy gold underline to the title
    private fun setupTitle() {
        val titleText = "RPGenerator"
        val target = "Genera"
        val start = titleText.indexOf(target)
        val end = start + target.length
        val span = SpannableString(titleText)
        val gold = ContextCompat.getColor(this, R.color.gold)
        val underlineSpan = TitleUnderline(8f, gold, 24f)
        span.setSpan(underlineSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        findViewById<TextView>(R.id.appTitle).text = span
    }

    // Sets up the dropdown menus for Class and Race
    private fun setupSpinners() {
        setupSpinner(classSpinner, R.array.class_options)
        setupSpinner(raceSpinner, R.array.race_options)
    }

    // Connects the stat sliders (Strength, Intelligence, etc.) to the logic
    private fun setupAttributeRows() {
        setupAttributeRow(R.id.strengthRow, "Strength") { viewModel.updateStrength(it) }
        setupAttributeRow(R.id.intRow, "Intelligence") { viewModel.updateIntelligence(it) }
        setupAttributeRow(R.id.dexRow, "Dexterity") { viewModel.updateDexterity(it) }
        setupAttributeRow(R.id.wisRow, "Wisdom") { viewModel.updateWisdom(it) }
    }

    // This "watches" the ViewModel. Whenever a value changes, the screen updates automatically.
    private fun observeViewModel() {
        viewModel.strength.observe(this) { updateSeekBar(R.id.strengthRow, it) }
        viewModel.intelligence.observe(this) { updateSeekBar(R.id.intRow, it) }
        viewModel.dexterity.observe(this) { updateSeekBar(R.id.dexRow, it) }
        viewModel.wisdom.observe(this) { updateSeekBar(R.id.wisRow, it) }

        viewModel.allocatedPoints.observe(this) { 
            allocatedPointsText.text = it.toString() 
        }
        viewModel.remainingPoints.observe(this) { 
            remainingPointsText.text = it.toString() 
        }

        viewModel.characterImageResId.observe(this) { resId ->
            characterImage.setImageResource(resId)
        }
    }

    // Updates a specific slider and its number display
    private fun updateSeekBar(rowId: Int, value: Int) {
        val row = findViewById<View>(rowId)
        val seekBar = row.findViewById<SeekBar>(R.id.attributeSeek)
        if (seekBar.progress != value) {
            seekBar.progress = value
        }
        row.findViewById<TextView>(R.id.attributeValue).text = value.toString()
    }

    // Clears everything back to the starting state
    private fun resetFields() {
        nameInput.text.clear()
        genderGroup.clearCheck()
        classSpinner.setSelection(0)
        raceSpinner.setSelection(0)
        viewModel.reset()
        Toast.makeText(this, "Fields Reset", Toast.LENGTH_SHORT).show()
    }

    // Configures a slider to update the ViewModel when moved
    private fun setupAttributeRow(rowId: Int, label: String, onUpdate: (Int) -> Boolean) {
        val row = findViewById<View>(rowId)
        row.findViewById<TextView>(R.id.attributeLabel).text = label
        val seekBar = row.findViewById<SeekBar>(R.id.attributeSeek)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return // Ignore changes that aren't from the user sliding
                
                // If you try to use more than 100 points, snap the bar to the maximum allowed
                if (!onUpdate(progress)) {
                    val currentVal = getCurrentVal(rowId)
                    val remaining = viewModel.remainingPoints.value ?: 0
                    val maxAllowed = currentVal + remaining
                    
                    seekBar.progress = maxAllowed
                    onUpdate(maxAllowed)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    // Helper to get the current value of a stat from the ViewModel
    private fun getCurrentVal(rowId: Int): Int {
        return when(rowId) {
            R.id.strengthRow -> viewModel.strength.value
            R.id.intRow -> viewModel.intelligence.value
            R.id.dexRow -> viewModel.dexterity.value
            R.id.wisRow -> viewModel.wisdom.value
            else -> 0
        } ?: 0
    }

    // Helper to setup the dropdown menus with custom colors
    private fun setupSpinner(spinner: Spinner, optionsArrayId: Int) {
        val options = resources.getStringArray(optionsArrayId)
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_item, options) {
            override fun isEnabled(position: Int) = position != 0 // Disable the "Select a..." item
            override fun getDropDownView(pos: Int, conv: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(pos, conv, parent) as TextView
                // Make the prompt gray and the options white
                v.setTextColor(if (pos == 0) ContextCompat.getColor(context, android.R.color.darker_gray) else ContextCompat.getColor(context, R.color.white))
                return v
            }
        }
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
    }

    // Finds the character image based on the selected class
    private fun updateCharacterImage() {
        if (classSpinner.selectedItemPosition > 0) {
            val className = classSpinner.selectedItem.toString().lowercase()
            val resId = resources.getIdentifier(className, "drawable", packageName)
            
            val finalResId = if (resId != 0) resId else R.drawable.empty_character
            viewModel.setCharacterImage(finalResId)
            
            if (resId == 0) Toast.makeText(this, "Image not found for: $className", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setCharacterImage(R.drawable.empty_character)
            Toast.makeText(this, "Please select a Class first", Toast.LENGTH_SHORT).show()
        }
    }
}

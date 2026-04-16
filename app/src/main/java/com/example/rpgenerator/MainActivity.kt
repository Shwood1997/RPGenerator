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
        // Makes the app take up the full screen of the phone
        enableEdgeToEdge() 
        // Tells the activity which layout design file to use
        setContentView(R.layout.activity_main)
        
        // This ensures the app doesn't hide behind system bars like the notch
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the image where the character portrait will be shown
        characterImage = findViewById(R.id.characterImage)
        // Find the gender selection buttons
        genderGroup = findViewById(R.id.genderGroup)
        // Find the dropdown list for picking a character class
        classSpinner = findViewById(R.id.classSpinner)
        // Find the dropdown list for picking a character race
        raceSpinner = findViewById(R.id.raceSpinner)
        // Find the box where the user types the character name
        nameInput = findViewById(R.id.nameInput)
        // Find the text label that shows total points spent
        allocatedPointsText = findViewById(R.id.allocatedPoints)
        // Find the text label that shows points left to spend
        remainingPointsText = findViewById(R.id.remainingPoints)
        // Find the button used to clear all inputs
        resetButton = findViewById(R.id.resetButton)
        // Find the button used to create the character image
        generateButton = findViewById(R.id.generateButton)

        // Set up the fancy title with an underline
        setupTitle()
        // Fill the dropdown lists with options
        setupSpinners()
        // Link the attribute sliders to the point calculation logic
        setupAttributeRows()
        // Start watching for data changes in the background
        observeViewModel()

        // When the reset button is clicked, clear all the fields
        resetButton.setOnClickListener { resetFields() }
        // When the generate button is clicked, update the character's portrait
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
        // Apply the special underline to the specific word "Genera"
        span.setSpan(underlineSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        findViewById<TextView>(R.id.appTitle).text = span
    }

    // Sets up the dropdown menus for Class and Race
    private fun setupSpinners() {
        // Load class options into the class dropdown
        setupSpinner(classSpinner, R.array.class_options)
        // Load race options into the race dropdown
        setupSpinner(raceSpinner, R.array.race_options)
    }

    // Connects the attribute sliders to the logic
    private fun setupAttributeRows() {
        // Set up the Strength slider
        setupAttributeRow(R.id.strengthRow, "Strength") { viewModel.updateStrength(it) }
        // Set up the Intelligence slider
        setupAttributeRow(R.id.intRow, "Intelligence") { viewModel.updateIntelligence(it) }
        // Set up the Dexterity slider
        setupAttributeRow(R.id.dexRow, "Dexterity") { viewModel.updateDexterity(it) }
        // Set up the Wisdom slider
        setupAttributeRow(R.id.wisRow, "Wisdom") { viewModel.updateWisdom(it) }
    }

    // This watches the ViewModel. Whenever a value changes, the screen updates automatically.
    private fun observeViewModel() {
        // Watch for changes in Strength and update its slider
        viewModel.strength.observe(this) { updateSeekBar(R.id.strengthRow, it) }
        // Watch for changes in Intelligence and update its slider
        viewModel.intelligence.observe(this) { updateSeekBar(R.id.intRow, it) }
        // Watch for changes in Dexterity and update its slider
        viewModel.dexterity.observe(this) { updateSeekBar(R.id.dexRow, it) }
        // Watch for changes in Wisdom and update its slider
        viewModel.wisdom.observe(this) { updateSeekBar(R.id.wisRow, it) }

        // Update the points spent label when it changes in the background
        viewModel.allocatedPoints.observe(this) { 
            allocatedPointsText.text = it.toString() 
        }
        // Update the points left label when it changes in the background
        viewModel.remainingPoints.observe(this) { 
            remainingPointsText.text = it.toString() 
        }

        // Change the character image on screen when it updates in the background
        viewModel.characterImageResId.observe(this) { resId ->
            characterImage.setImageResource(resId)
        }
    }

    // Updates a specific slider and its number display
    private fun updateSeekBar(rowId: Int, value: Int) {
        val row = findViewById<View>(rowId)
        val seekBar = row.findViewById<SeekBar>(R.id.attributeSeek)
        // Only update the slider position if it's different from the new value
        if (seekBar.progress != value) {
            seekBar.progress = value
        }
        // Update the number text next to the slider
        row.findViewById<TextView>(R.id.attributeValue).text = value.toString()
    }

    // Clears everything back to the starting state
    private fun resetFields() {
        // Clear the character name box
        nameInput.text.clear()
        // Unselect any selected gender
        genderGroup.clearCheck()
        // Set dropdowns back to the "Select..." option
        classSpinner.setSelection(0)
        raceSpinner.setSelection(0)
        // Tell the background logic to reset all points
        viewModel.reset()
        // Show a small popup message saying everything was reset
        Toast.makeText(this, "Fields Reset", Toast.LENGTH_SHORT).show()
    }

    // Configures a slider to update the ViewModel when moved
    private fun setupAttributeRow(rowId: Int, label: String, onUpdate: (Int) -> Boolean) {
        val row = findViewById<View>(rowId)
        // Set the text for the slider's label (e.g. Strength)
        row.findViewById<TextView>(R.id.attributeLabel).text = label
        val seekBar = row.findViewById<SeekBar>(R.id.attributeSeek)

        // Listen for when the user moves the slider
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                // Ignore changes that aren't from the user sliding
                if (!fromUser) return 
                
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
        // Create an adapter to bridge the data with the UI
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_item, options) {
            // Disable the default "Select a ..." option on spinners
            override fun isEnabled(position: Int) = position != 0
            override fun getDropDownView(pos: Int, conv: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(pos, conv, parent) as TextView
                // Make the prompt gray and the options white
                v.setTextColor(if (pos == 0) ContextCompat.getColor(context, android.R.color.darker_gray) else ContextCompat.getColor(context, R.color.white))
                return v
            }
        }
        // Tell the dropdown which design to use for its items
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
    }

    // Finds the character image based on the selected class
    private fun updateCharacterImage() {
        // Check if a class has been picked
        if (classSpinner.selectedItemPosition > 0) {
            val className = classSpinner.selectedItem.toString().lowercase()
            // Look for an image file in the project that matches the class name
            val resId = resources.getIdentifier(className, "drawable", packageName)
            
            // Use the found image, or a placeholder if it doesn't exist
            val finalResId = if (resId != 0) resId else R.drawable.empty_character
            viewModel.setCharacterImage(finalResId)
            
            // If image is missing, show a warning message
            if (resId == 0) Toast.makeText(this, "Image not found for: $className", Toast.LENGTH_SHORT).show()
        } else {
            // Remind the user to pick a class if they haven't yet
            viewModel.setCharacterImage(R.drawable.empty_character)
            Toast.makeText(this, "Please select a Class first", Toast.LENGTH_SHORT).show()
        }
    }
}

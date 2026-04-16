package com.example.rpgenerator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MediatorLiveData

// This class stores the character data. It stays alive even if you rotate your phone.
class MainViewModel : ViewModel() {

    private val maxPoints = 100 // The total points you are allowed to spend

    // These hold the values for the 4 attributes. They start at 25 each.
    private val _strength = MutableLiveData(25)
    val strength: LiveData<Int> = _strength

    private val _intelligence = MutableLiveData(25)
    val intelligence: LiveData<Int> = _intelligence

    private val _dexterity = MutableLiveData(25)
    val dexterity: LiveData<Int> = _dexterity

    private val _wisdom = MutableLiveData(25)
    val wisdom: LiveData<Int> = _wisdom

    // This holds the ID for the character image (e.g., rogue.png)
    private val _characterImageResId = MutableLiveData<Int>(R.drawable.empty_character)
    val characterImageResId: LiveData<Int> = _characterImageResId

    // This automatically adds up all the spent points whenever a stat changes
    val allocatedPoints = MediatorLiveData<Int>().apply {
        value = 100 // Start at 100 (25 * 4)
        addSource(strength) { value = calculateTotal() }
        addSource(intelligence) { value = calculateTotal() }
        addSource(dexterity) { value = calculateTotal() }
        addSource(wisdom) { value = calculateTotal() }
    }

    // This automatically calculates how many points are left (100 minus what you used)
    val remainingPoints = MediatorLiveData<Int>().apply {
        value = 0
        addSource(allocatedPoints) { value = maxPoints - (it ?: 0) }
    }

    // Helper to add up all current stats
    private fun calculateTotal(): Int {
        val s = _strength.value ?: 0
        val i = _intelligence.value ?: 0
        val d = _dexterity.value ?: 0
        val w = _wisdom.value ?: 0
        return s + i + d + w
    }

    // Saves the character image so it persists
    fun setCharacterImage(resId: Int) {
        _characterImageResId.value = resId
    }

    // Functions to update each stat. They return 'true' if the change was allowed.
    fun updateStrength(newValue: Int): Boolean = validateAndUpdate(_strength, newValue)
    fun updateIntelligence(newValue: Int): Boolean = validateAndUpdate(_intelligence, newValue)
    fun updateDexterity(newValue: Int): Boolean = validateAndUpdate(_dexterity, newValue)
    fun updateWisdom(newValue: Int): Boolean = validateAndUpdate(_wisdom, newValue)

    // This logic ensures you never spend more than 100 points
    private fun validateAndUpdate(stat: MutableLiveData<Int>, newValue: Int): Boolean {
        val currentTotal = calculateTotal()
        val currentStatValue = stat.value ?: 0
        val projectedTotal = currentTotal - currentStatValue + newValue
        
        return if (projectedTotal <= maxPoints) {
            stat.value = newValue
            true
        } else {
            false
        }
    }

    // Resets everything back to the default values
    fun reset() {
        _strength.value = 25
        _intelligence.value = 25
        _dexterity.value = 25
        _wisdom.value = 25
        _characterImageResId.value = R.drawable.empty_character
    }
}

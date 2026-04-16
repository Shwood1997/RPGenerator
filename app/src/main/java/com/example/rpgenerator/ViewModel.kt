package com.example.rpgenerator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MediatorLiveData

// This class stores the character data. It stays alive even if you rotate your phone.
class MainViewModel : ViewModel() {

    // The maximum number of points you can spend on all your stats combined.
    private val maxPoints = 100 

    // The following blocks define the character attributes
    // The "private" variable stores the value MutableLiveData
    // The "val" variable lets the screen see it LiveData without changing it directly
    
    private val _strength = MutableLiveData(25)
    val strength: LiveData<Int> = _strength

    private val _intelligence = MutableLiveData(25)
    val intelligence: LiveData<Int> = _intelligence

    private val _dexterity = MutableLiveData(25)
    val dexterity: LiveData<Int> = _dexterity

    private val _wisdom = MutableLiveData(25)
    val wisdom: LiveData<Int> = _wisdom

    private val _characterImageResId = MutableLiveData<Int>(R.drawable.empty_character)
    val characterImageResId: LiveData<Int> = _characterImageResId

    // This section handles the calculation of points.
    // AllocatedPoints adds up everything you've spent.
    val allocatedPoints = MediatorLiveData<Int>().apply {
        value = 100 
        addSource(strength) { value = calculateTotal() }
        addSource(intelligence) { value = calculateTotal() }
        addSource(dexterity) { value = calculateTotal() }
        addSource(wisdom) { value = calculateTotal() }
    }

    // RemainingPoints tells you how many points are left in your 100-point limitation
    val remainingPoints = MediatorLiveData<Int>().apply {
        value = 0
        addSource(allocatedPoints) { value = maxPoints - (it ?: 0) }
    }

    // This function adds up all 4 attributes to get the current total points used.
    private fun calculateTotal(): Int {
        val s = _strength.value ?: 0
        val i = _intelligence.value ?: 0
        val d = _dexterity.value ?: 0
        val w = _wisdom.value ?: 0
        return s + i + d + w
    }

    // This updates the ID of the image we want to show for the character.
    fun setCharacterImage(resId: Int) {
        _characterImageResId.value = resId
    }

    // The following functions are called by the UI to try and change a stat
    // They return 'true' if there were enough points but 'false' if the limit was reached
    fun updateStrength(newValue: Int): Boolean = validateAndUpdate(_strength, newValue)
    fun updateIntelligence(newValue: Int): Boolean = validateAndUpdate(_intelligence, newValue)
    fun updateDexterity(newValue: Int): Boolean = validateAndUpdate(_dexterity, newValue)
    fun updateWisdom(newValue: Int): Boolean = validateAndUpdate(_wisdom, newValue)

    // Prevents you from spending more than 100 points
    private fun validateAndUpdate(stat: MutableLiveData<Int>, newValue: Int): Boolean {
        // Get the total points spent right now
        val currentTotal = calculateTotal()
        // Get the current value of the stat we want to change
        val currentStatValue = stat.value ?: 0
        // Calculate what the total would be if we allowed the change
        val projectedTotal = currentTotal - currentStatValue + newValue
        
        // If the new total is 100 or less, the change is valid
        return if (projectedTotal <= maxPoints) {
            // Update the stat with the new value.
            stat.value = newValue
            // Return true to indicate the update happened.
            true
        } else {
            // Return false to indicate the update was blocked.
            false
        }
    }

    // This resets the character's stats to 25 and sets the image back to the placeholder.
    fun reset() {
        _strength.value = 25
        _intelligence.value = 25
        _dexterity.value = 25
        _wisdom.value = 25
        _characterImageResId.value = R.drawable.empty_character
    }
}

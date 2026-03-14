package edu.nd.pmcburne.hwapp.one

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import android.util.Log

data class MainState(
    val date: LocalDate = LocalDate.now(),
    val gender: Gender = Gender.MEN,
    val isLoading: Boolean = false,
    val games: List<GameUi> = emptyList(),
    val errorHint: String? = null
)

data class GameUi(
    val titleLine: String,
    val statusLine: String,
    val scoreLine: String?,
    val winnerHint: String?
)

class MainViewModel(private val repo: Repo) : ViewModel() {

    private val date = MutableStateFlow(LocalDate.now())
    private val gender = MutableStateFlow(Gender.MEN)
    private val isLoading = MutableStateFlow(false)
    private val errorHint = MutableStateFlow<String?>(null)

    private val games: StateFlow<List<GameUi>> =
        combine(date, gender) { d, g -> d to g }
            .flatMapLatest { (d, g) -> repo.observe(d, g) }
            .map { list -> list.map { it.toUi() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<MainState> =
        combine(date, gender, isLoading, games, errorHint) { d, g, loading, items, err ->
            MainState(date = d, gender = g, isLoading = loading, games = items, errorHint = err)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, MainState())

    //    fun changeDate(date: LocalDate) {
//        _selectedDate.value = date
//        refreshGames()
//    }
    fun setDate(newDate: LocalDate) {
        date.value = newDate
        refresh()
    }

    //    fun toggleGender(gender: String) {
//        _selectedGender.value = gender
//        refreshGames()
//    }
    fun setGender(newGender: Gender) {
        gender.value = newGender
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            errorHint.value = null
            runCatching { repo.refresh(date.value, gender.value) }
                .onFailure { errorHint.value = "Refresh failed (showing cached scores)." }
            isLoading.value = false
        }
    }

    init {
        refresh()
    }
}

private fun GameEntity.toUi(): GameUi {
    Log.d("GameEntity", "Mapping to UI: $this") //testing
    val title = "${awayName} @ ${homeName}"

    val normalized = gameState.lowercase()
    val isFinal = normalized == "final" ||
            currentPeriod.uppercase() == "FINAL" ||
            finalMessage.uppercase().contains("FINAL")

    val isLive = normalized == "live"

//val score = if (isLive || isFinal) "${awayScore ?: "-"} - ${homeScore ?: "-"}" else null

    val score = if (isLive || isFinal) {
        val a = awayScore?.toString() ?: "-"
        val h = homeScore?.toString() ?: "-"
        "$a - $h"
    } else null

//    val status = when {
//        isFinal -> "Final"
//        isLive -> (currentPeriod.ifBlank { "Live" } + if (contestClock.isBlank()) "" else " • $contestClock")
//        else -> "Starts ${startTime.ifBlank { "TBD" }}"
//    }

    val status = when {
        isFinal -> "Final"
        isLive -> {
            val p = currentPeriod.ifBlank { "Live" }
            val c = contestClock.ifBlank { "" }
            if (c.isBlank()) p else "$p • $c"
        }
        else -> "Starts ${startTime.ifBlank { "TBD" }}"
    }

    val winner = if (isFinal) {
        when {
            isHomeWinner -> "Winner: $homeName"
            isAwayWinner -> "Winner: $awayName"
            else -> null
        }
    } else null

    return GameUi(titleLine = title, statusLine = status, scoreLine = score, winnerHint = winner)
}
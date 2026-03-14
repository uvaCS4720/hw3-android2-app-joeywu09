package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.nd.pmcburne.hwapp.one.Gender
import edu.nd.pmcburne.hwapp.one.GameUi
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels { MainVmFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        state = vm.state.collectAsState().value,
                        onPickDate = vm::setDate,
                        onPickGender = vm::setGender,
                        onRefresh = vm::refresh,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private class MainVmFactory(
    private val appContext: android.content.Context
) : ViewModelProvider.Factory {

    private val db by lazy { DbModule.create(appContext) }
    private val repo by lazy { Repo(appContext, NetworkModule.api, db.gameDao()) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repo) as T
        }
        error("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    state: MainState,
    onPickDate: (LocalDate) -> Unit,
    onPickGender: (Gender) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val utc = ZoneId.of("UTC")

        val initialMillis = state.date
            .atStartOfDay(utc)
            .toInstant()
            .toEpochMilli()

        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val ld = Instant.ofEpochMilli(millis)
                            .atZone(utc)
                            .toLocalDate()
                        onPickDate(ld)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = pickerState) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "College Basketball Games (NCAA)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DATE PICKER BUTTON
            Button(onClick = { showDatePicker = true }) {
                Text(state.date.toString())
            }

            Spacer(Modifier.width(12.dp))

            // GENDER BUTTONS
            Button(
                onClick = { onPickGender(Gender.MEN) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.gender == Gender.MEN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Men")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = { onPickGender(Gender.WOMEN) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.gender == Gender.WOMEN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Women")
            }

            Spacer(Modifier.weight(1f))

            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }

        state.errorHint?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        if (state.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            Spacer(Modifier.height(12.dp))
        }

        if (state.games.isEmpty() && !state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No games cached for this date.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.games) { g -> GameCard(g) }
            }
        }
    }
}

//@Composable
//fun GameItemUi(game: GameUi) {
//    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
//        Text(game.titleLine, style = MaterialTheme.typography.titleMedium)
//        Text(game.statusLine, style = MaterialTheme.typography.bodyMedium)
//        game.scoreLine?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
//        game.winnerHint?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
//        Divider()
//    }
//}
@Composable
private fun GameCard(game: GameUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(game.titleLine, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(game.statusLine)

            game.scoreLine?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.headlineSmall)
            }

            game.winnerHint?.let {
                Spacer(Modifier.height(6.dp))
                Text(it)
            }
        }
    }
}
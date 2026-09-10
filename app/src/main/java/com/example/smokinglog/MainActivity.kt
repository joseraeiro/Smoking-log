@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.smokinglog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smokinglog.data.*
import java.time.*
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GuideTheme { SmokingLogApp() } }
    }
}

private val Space = Color(0xFF071A2B)
private val GuideBlue = Color(0xFF0A3454)
private val TowelTeal = Color(0xFF57DFC2)
private val FriendlyYellow = Color(0xFFFFE066)

@Composable
private fun GuideTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = FriendlyYellow, onPrimary = Space, secondary = TowelTeal,
        background = Space, surface = GuideBlue, onBackground = Color(0xFFF4F6EF),
        onSurface = Color(0xFFF4F6EF), error = Color(0xFFFF8A80)
    )
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}

private enum class Screen(val label: String, val icon: ImageVector, val topic: HumorTopic) {
    TODAY("Today", Icons.Default.RocketLaunch, HumorTopic.TODAY),
    HISTORY("Log", Icons.Default.MenuBook, HumorTopic.HISTORY),
    INSIGHTS("Guide", Icons.Default.QueryStats, HumorTopic.INSIGHTS),
    SETTINGS("Settings", Icons.Default.Settings, HumorTopic.SETTINGS)
}

@Composable
private fun SmokingLogApp(vm: MainViewModel = viewModel()) {
    val entries by vm.entries.collectAsState()
    val target by vm.dailyTarget.collectAsState()
    var screen by remember { mutableStateOf(Screen.TODAY) }
    var bulletin by remember { mutableStateOf(Humor.next(HumorTopic.TODAY)) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("DON'T PANIC", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("A mostly harmless smoking log", fontSize = 12.sp, color = TowelTeal) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Space)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = GuideBlue) {
                Screen.entries.forEach { item -> NavigationBarItem(
                    selected = screen == item, onClick = {
                        screen = item
                        bulletin = Humor.next(item.topic, bulletin)
                    },
                    icon = { Icon(item.icon, null) }, label = { Text(item.label) }
                ) }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (screen) {
                Screen.TODAY -> TodayScreen(entries, target, bulletin, vm)
                Screen.HISTORY -> HistoryScreen(entries, bulletin, vm)
                Screen.INSIGHTS -> InsightsScreen(entries, bulletin)
                Screen.SETTINGS -> SettingsScreen(entries, target, bulletin, vm)
            }
        }
    }
}

@Composable
private fun TodayScreen(entries: List<LogEntry>, target: Double?, bulletin: String, vm: MainViewModel) {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val todayEntries = entries.filter { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() == today }
    val smoked = todayEntries.filter { it.type == EntryType.SMOKED }
    val amount = smoked.sumOf { it.amount }
    val resisted = todayEntries.count { it.type == EntryType.RESISTED }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var resistDialog by remember { mutableStateOf(false) }
    fun log(value: Double) = vm.log(value) { entry ->
        scope.launch {
            if (snackbar.showSnackbar(Humor.smoked(value),
                    "UNDO", duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) vm.delete(entry)
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Your pocket guide to today", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { CosmicBulletin(bulletin) }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = GuideBlue), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("CIGARETTE EQUIVALENTS", color = TowelTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(formatAmount(amount), fontSize = 52.sp, fontWeight = FontWeight.Black, color = FriendlyYellow)
                        Text("${smoked.size} smoking events  •  $resisted urges resisted")
                        if (target != null) {
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(progress = { (amount / target).toFloat().coerceIn(0f, 1f) }, Modifier.fillMaxWidth())
                            Text(if (amount <= target) "${formatAmount(target - amount)} left before today's target"
                            else "Above target. Keep logging honestly — no Vogon poetry required.", fontSize = 12.sp)
                        }
                    }
                }
            }
            item { LastCigarette(smoked.firstOrNull()) }
            item {
                Button(onClick = { log(1.0) }, Modifier.fillMaxWidth().height(68.dp), shape = RoundedCornerShape(18.dp)) {
                    Icon(Icons.Default.AddCircle, null); Spacer(Modifier.width(10.dp)); Text("LOG A WHOLE CIGARETTE", fontWeight = FontWeight.Black)
                }
            }
            item {
                OutlinedButton(onClick = { log(0.5) }, Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(18.dp)) {
                    Icon(Icons.Default.ExposurePlus1, null); Spacer(Modifier.width(10.dp)); Text("LOG HALF A CIGARETTE", fontWeight = FontWeight.Bold)
                }
            }
            item { HorizontalDivider() }
            item {
                FilledTonalButton(onClick = { resistDialog = true }, Modifier.fillMaxWidth().height(56.dp)) {
                    Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("I RESISTED AN URGE")
                }
            }
            item { Text("Record first, reflect optionally. Accurate data is useful data, even on improbable days.", color = TowelTeal, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(12.dp))
    }
    if (resistDialog) ResistDialog(onDismiss = { resistDialog = false }) { note, strength ->
        vm.resist(note, strength); resistDialog = false
        scope.launch { snackbar.showSnackbar(Humor.resisted()) }
    }
}

@Composable
private fun LastCigarette(entry: LogEntry?) {
    val text = if (entry == null) "No cigarette logged yet today. A splendidly uneventful entry."
    else {
        val minutes = Duration.between(Instant.ofEpochMilli(entry.timestamp), Instant.now()).toMinutes().coerceAtLeast(0)
        "Last cigarette ${if (minutes < 60) "$minutes min" else "${minutes / 60}h ${minutes % 60}m"} ago"
    }
    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Schedule, null, tint = TowelTeal); Spacer(Modifier.width(8.dp)); Text(text) }
}

@Composable
private fun ResistDialog(onDismiss: () -> Unit, onSave: (String, Int?) -> Unit) {
    var note by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf<Int?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("A small but excellent victory") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("How did you feel, and what helped? This is optional — the universe will continue either way.")
            OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Optional note") }, minLines = 3)
            Text("Urge strength (optional)")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (1..5).forEach { value -> FilterChip(selected = strength == value, onClick = { strength = if (strength == value) null else value }, label = { Text("$value") }) }
            }
        } },
        confirmButton = { Button(onClick = { onSave(note, strength) }) { Text("RECORD THE WIN") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } })
}

@Composable
private fun HistoryScreen(entries: List<LogEntry>, bulletin: String, vm: MainViewModel) {
    var filter by remember { mutableStateOf<EntryType?>(null) }
    var editing by remember { mutableStateOf<LogEntry?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("The ship's log", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CosmicBulletin(bulletin)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(filter == null, { filter = null }, label = { Text("All") })
            FilterChip(filter == EntryType.SMOKED, { filter = EntryType.SMOKED }, label = { Text("Smoked") })
            FilterChip(filter == EntryType.RESISTED, { filter = EntryType.RESISTED }, label = { Text("Resisted") })
        }
        val shown = entries.filter { filter == null || it.type == filter }
        if (shown.isEmpty()) EmptyState("The log is emptier than this bit of space.") else
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(shown, key = { it.id }) { entry -> EntryCard(entry, { editing = entry }, { vm.delete(entry) }) }
            }
    }
    editing?.let { entry -> EditDialog(entry, { editing = null }) { vm.update(it); editing = null } }
}

@Composable
private fun EntryCard(entry: LogEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateTime = Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault())
    Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (entry.type == EntryType.SMOKED) Icons.Default.SmokingRooms else Icons.Default.AutoAwesome, null,
            tint = if (entry.type == EntryType.SMOKED) FriendlyYellow else TowelTeal)
        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) {
            Text(if (entry.type == EntryType.SMOKED) "${formatAmount(entry.amount)} cigarette" else "Urge resisted", fontWeight = FontWeight.Bold)
            Text(dateTime.format(DateTimeFormatter.ofPattern("EEE, d MMM • HH:mm")), fontSize = 12.sp, color = TowelTeal)
            AnimatedVisibility(entry.note.isNotBlank()) { Text(entry.note, fontSize = 13.sp) }
            entry.urgeStrength?.let { Text("Urge strength: $it/5", fontSize = 12.sp) }
        }
        IconButton(onEdit) { Icon(Icons.Default.Edit, "Edit") }
        IconButton(onDelete) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
    } }
}

@Composable
private fun EditDialog(entry: LogEntry, onDismiss: () -> Unit, onSave: (LogEntry) -> Unit) {
    var note by remember { mutableStateOf(entry.note) }
    var amount by remember { mutableStateOf(entry.amount) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Correct the log") }, text = { Column {
        if (entry.type == EntryType.SMOKED) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(amount == 1.0, { amount = 1.0 }, label = { Text("Whole") })
            FilterChip(amount == 0.5, { amount = 0.5 }, label = { Text("Half") })
        }
        OutlinedTextField(note, { note = it }, label = { Text("Optional note") }, modifier = Modifier.fillMaxWidth())
        Text("Recorded time is preserved.", fontSize = 12.sp, color = TowelTeal)
    } }, confirmButton = { Button(onClick = { onSave(entry.copy(note = note.trim(), amount = amount)) }) { Text("SAVE") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } })
}

@Composable
private fun InsightsScreen(entries: List<LogEntry>, bulletin: String) {
    var days by remember { mutableIntStateOf(7) }
    val totals = Stats.dayTotals(entries, days, Instant.now(), ZoneId.systemDefault())
    val amount = totals.sumOf { it.amount }
    val average = if (days == 0) 0.0 else amount / days
    val longest = longestGap(entries)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("The Guide", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { CosmicBulletin(bulletin) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7, 30).forEach { FilterChip(days == it, { days = it }, label = { Text("$it days") }) }
        } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("TOTAL", formatAmount(amount), Modifier.weight(1f))
            MetricCard("DAILY AVG", formatAmount(average), Modifier.weight(1f))
            MetricCard("RESISTED", totals.sumOf { it.resisted }.toString(), Modifier.weight(1f))
        } }
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("Cigarette equivalents by day", fontWeight = FontWeight.Bold)
            BarChart(totals, Modifier.fillMaxWidth().height(180.dp))
        } } }
        item { MetricRow("Longest logged interval", longest ?: "Not enough coordinates yet") }
        item { MetricRow("Highest day", totals.maxByOrNull { it.amount }?.let { "${it.date.format(DateTimeFormatter.ofPattern("EEE d MMM"))}: ${formatAmount(it.amount)}" } ?: "—") }
        item { Text("These are observations, not medical conclusions. The Guide is useful, but not infallible.", color = TowelTeal, fontSize = 12.sp) }
    }
}

@Composable private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = FriendlyYellow, fontSize = 24.sp, fontWeight = FontWeight.Black); Text(label, fontSize = 9.sp)
    } }
}
@Composable private fun MetricRow(label: String, value: String) = Card(Modifier.fillMaxWidth()) {
    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(value, color = TowelTeal, fontWeight = FontWeight.Bold) }
}

@Composable
private fun BarChart(totals: List<DayTotal>, modifier: Modifier) {
    val maximum = max(1.0, totals.maxOfOrNull { it.amount } ?: 1.0)
    Canvas(modifier.padding(top = 18.dp, bottom = 16.dp)) {
        val step = size.width / totals.size.coerceAtLeast(1)
        totals.forEachIndexed { index, day ->
            val height = (day.amount / maximum * size.height).toFloat()
            drawLine(FriendlyYellow, Offset(step * index + step / 2, size.height), Offset(step * index + step / 2, size.height - height),
                strokeWidth = (step * .55f).coerceAtLeast(3f), cap = StrokeCap.Round)
            if (day.resisted > 0) drawCircle(TowelTeal, 5.dp.toPx(), Offset(step * index + step / 2, (size.height - height - 12.dp.toPx()).coerceAtLeast(5.dp.toPx())))
        }
    }
}

@Composable
private fun SettingsScreen(entries: List<LogEntry>, target: Double?, bulletin: String, vm: MainViewModel) {
    val context = LocalContext.current
    var targetText by remember(target) { mutableStateOf(target?.let(::formatAmount) ?: "") }
    var message by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            runCatching { context.contentResolver.openOutputStream(it)?.bufferedWriter()?.use { writer ->
                writer.write(Stats.csv(entries, ZoneId.systemDefault()))
            } }.onSuccess { message = "Log exported. Your data has its towel." }.onFailure { message = "Export failed: ${it.localizedMessage}" }
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Guide settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { CosmicBulletin(bulletin) }
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("OPTIONAL DAILY TARGET", color = TowelTeal, fontWeight = FontWeight.Bold)
            Text("A navigational aid, never a verdict.", fontSize = 13.sp)
            OutlinedTextField(targetText, { targetText = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text("Cigarette equivalents") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row { Button(onClick = { targetText.toDoubleOrNull()?.takeIf { it > 0 }?.let(vm::setTarget) }, enabled = targetText.toDoubleOrNull()?.let { it > 0 } == true) { Text("SAVE TARGET") }
                TextButton(onClick = { targetText = ""; vm.setTarget(null) }) { Text("CLEAR") } }
        } } }
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("YOUR DATA", color = TowelTeal, fontWeight = FontWeight.Bold)
            Text("Stored locally on this device. Export a portable copy whenever you like.")
            Spacer(Modifier.height(10.dp)); Button(onClick = { exportLauncher.launch("mostly-harmless-log.csv") }, enabled = entries.isNotEmpty()) {
                Icon(Icons.Default.FileDownload, null); Spacer(Modifier.width(8.dp)); Text("EXPORT CSV")
            }
            message?.let { Text(it, color = TowelTeal, fontSize = 12.sp) }
        } } }
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("PRIVACY & ACCESSIBILITY", color = TowelTeal, fontWeight = FontWeight.Bold)
            Text("No account, advertisements, analytics, location, or internet permission. Large controls, system font scaling, and dark colours are built in.")
        } } }
        item { Text("Mostly Harmless • Version 1.0\nBuilt for honest logging across this unfashionable end of the galaxy.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = TowelTeal) }
    }
}

@Composable private fun EmptyState(text: String) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, color = TowelTeal, textAlign = TextAlign.Center) }
@Composable private fun CosmicBulletin(message: String) = Card(
    colors = CardDefaults.cardColors(containerColor = TowelTeal.copy(alpha = .12f)),
    modifier = Modifier.fillMaxWidth()
) {
    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Campaign, contentDescription = null, tint = TowelTeal)
        Spacer(Modifier.width(10.dp))
        Column {
            Text("MESSAGE FROM THE GUIDE", color = TowelTeal, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(message, fontWeight = FontWeight.Medium)
        }
    }
}
private fun formatAmount(value: Double) = if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
private fun longestGap(entries: List<LogEntry>): String? {
    val times = entries.filter { it.type == EntryType.SMOKED }.map { it.timestamp }.sorted()
    val minutes = times.zipWithNext { a, b -> (b - a) / 60_000 }.maxOrNull() ?: return null
    return if (minutes < 60) "${minutes}m" else "${minutes / 60}h ${minutes % 60}m"
}

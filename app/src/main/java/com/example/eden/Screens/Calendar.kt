package com.example.eden.Screens

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eden.MainActivity
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

@Composable
fun Calendar(navController: NavController) {
    val userId = MainActivity.loggedInUser?.objectId
    var visits by remember(userId) { mutableStateOf<List<CalendarVisit>>(emptyList()) }
    var selectedDay by remember(userId) { mutableStateOf<LocalDate?>(null) }
    var refresh by remember(userId) { mutableIntStateOf(0) }
    var loading by remember(userId, refresh) { mutableStateOf(!userId.isNullOrBlank()) }
    var failed by remember(userId, refresh) { mutableStateOf(false) }

    DisposableEffect(userId, refresh) {
        var active = true
        val main = Handler(Looper.getMainLooper())
        if (!userId.isNullOrBlank()) {
            AppointmentController().loadCalendarVisits(
                userId = userId,
                onSuccess = { loaded ->
                    main.post {
                        if (active) {
                            visits = loaded
                            loading = false
                        }
                    }
                },
                onError = {
                    main.post {
                        if (active) {
                            loading = false
                            failed = true
                        }
                    }
                }
            )
        }
        onDispose { active = false }
    }

    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    var month by remember(userId) { mutableStateOf(YearMonth.from(today)) }
    val visitDates = remember(visits, zone) {
        visits.map { calendarVisitDay(it, zone) }.toSet()
    }
    val now = System.currentTimeMillis()
    val groupedVisits = groupCalendarVisits(visits, selectedDay, zone, now)
    val upcoming = groupedVisits.upcoming
    val past = groupedVisits.past
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController, stringResource(R.string.calendar_visits)) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (userId.isNullOrBlank()) {
                item { Text(stringResource(R.string.calendar_sign_in)) }
            } else {
                item {
                    VisitMonth(
                        month = month,
                        selectedDay = selectedDay,
                        today = today,
                        visitDates = visitDates,
                        onMonthChange = { month = it },
                        onDayClick = { selectedDay = it }
                    )
                }
                item {
                    Row {
                        TextButton(onClick = { selectedDay = null }, enabled = selectedDay != null) {
                            Text(stringResource(R.string.calendar_all_dates))
                        }
                        TextButton(onClick = { refresh++ }, enabled = !loading) {
                            Text(stringResource(if (failed) R.string.visit_retry else R.string.calendar_refresh))
                        }
                    }
                    selectedDay?.let { Text(stringResource(R.string.calendar_selected_day, it.format(dateFormatter))) }
                }

                when {
                    loading -> item { CircularProgressIndicator() }
                    failed -> item { Text(stringResource(R.string.calendar_load_failed)) }
                    upcoming.isEmpty() && past.isEmpty() -> item {
                        Text(stringResource(if (selectedDay == null) R.string.calendar_no_visits else R.string.calendar_no_day_visits))
                    }
                    else -> {
                        if (upcoming.isNotEmpty()) {
                            item { VisitSection(R.string.calendar_upcoming) }
                            items(upcoming) { VisitRow(it) }
                        }
                        if (past.isNotEmpty()) {
                            item { VisitSection(R.string.calendar_past) }
                            items(past) { VisitRow(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitMonth(
    month: YearMonth,
    selectedDay: LocalDate?,
    today: LocalDate,
    visitDates: Set<LocalDate>,
    onMonthChange: (YearMonth) -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val firstWeekday = WeekFields.of(locale).firstDayOfWeek
    val offset = (month.atDay(1).dayOfWeek.value - firstWeekday.value + 7) % 7
    val weeks = (offset + month.lengthOfMonth() + 6) / 7
    val fullDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    val colors = MaterialTheme.colorScheme

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val previous = stringResource(R.string.calendar_previous_month)
            val next = stringResource(R.string.calendar_next_month)
            TextButton(
                onClick = { onMonthChange(month.minusMonths(1)) },
                modifier = Modifier.semantics { contentDescription = previous }
            ) { Text("‹") }
            Text(
                month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(
                onClick = { onMonthChange(month.plusMonths(1)) },
                modifier = Modifier.semantics { contentDescription = next }
            ) { Text("›") }
        }
        Row {
            repeat(7) { column ->
                Box(Modifier.weight(1f).height(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        firstWeekday.plus(column.toLong()).getDisplayName(TextStyle.NARROW, locale),
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
        repeat(weeks) { week ->
            Row {
                repeat(7) { column ->
                    val number = week * 7 + column - offset + 1
                    val cell = Modifier.weight(1f).height(48.dp)
                    if (number !in 1..month.lengthOfMonth()) {
                        Box(cell)
                    } else {
                        val day = month.atDay(number)
                        val selected = day == selectedDay
                        val hasVisit = day in visitDates
                        val dateLabel = day.format(fullDate)
                        val description = if (hasVisit) {
                            stringResource(R.string.calendar_date_has_visit, dateLabel)
                        } else {
                            dateLabel
                        }
                        Box(
                            modifier = cell
                                .selectable(selected = selected, role = Role.Button) { onDayClick(day) }
                                .semantics { contentDescription = description },
                            contentAlignment = Alignment.Center
                        ) {
                            val background = when {
                                selected -> colors.primary
                                day == today -> colors.secondaryContainer
                                else -> androidx.compose.ui.graphics.Color.Transparent
                            }
                            Box(
                                Modifier.size(38.dp).background(background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val foreground = when {
                                    selected -> colors.onPrimary
                                    day == today -> colors.onSecondaryContainer
                                    else -> colors.onSurface
                                }
                                Text(number.toString(), color = foreground)
                                if (hasVisit) {
                                    Box(
                                        Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                                            .size(5.dp).background(
                                                if (selected) colors.onPrimary else colors.primary,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitSection(title: Int) {
    Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun VisitRow(visit: CalendarVisit) {
    val context = LocalContext.current
    val date = Date(visit.timeMillis)
    val day = android.text.format.DateFormat.getDateFormat(context).format(date)
    val time = android.text.format.DateFormat.getTimeFormat(context).format(date)
    Text(
        visit.petName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.unknown_pet),
        style = MaterialTheme.typography.titleSmall
    )
    Text(stringResource(R.string.calendar_visit_date_time, day, time))
    val status = visit.status.ifBlank { stringResource(R.string.unknown_pet) }
    Text(stringResource(R.string.calendar_visit_status, status))
    HorizontalDivider(Modifier.padding(top = 8.dp))
}

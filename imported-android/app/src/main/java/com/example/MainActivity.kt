package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val viewModel: EventViewModel = viewModel()
    val profile by viewModel.profileState.collectAsState(initial = null)
    val sessions by viewModel.sessionsState.collectAsState(initial = emptyList())
    val contacts by viewModel.contactsState.collectAsState(initial = emptyList())
    val exhibitors by viewModel.exhibitorsState.collectAsState(initial = emptyList())

    val isOnboarded = profile?.isOnboarded == true

    if (!isOnboarded) {
        AuthOnboardingScreen(viewModel = viewModel)
    } else {
        var selectedTab by remember { mutableStateOf(0) }
        val haptic = LocalHapticFeedback.current

        // Notification bell state
        var showNotificationsDialog by remember { mutableStateOf(false) }
        var unreadNotificationsCount by remember { mutableStateOf(3) }
        val notificationsList = remember {
            mutableStateListOf(
                "💡 Sourcing Hall A: Keynote panel on cultured proteins starting in 5 minutes!",
                "🎉 Matchmakers found: 3 high-synergy partners match your criteria in cellular agriculture.",
                "📍 Scan bonus active: Scavenger hunt code at EcoPack (Booth B-05) awards double tickets!"
            )
        }

        // Search overlay state
        var isSearchActive by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }

        // Global Scan dialog state
        var showGlobalScanDialog by remember { mutableStateOf(false) }
        var scanSimName by remember { mutableStateOf("Nadia Kovalenko") }
        var scanSimCompany by remember { mutableStateOf("BioSynth Algae") }
        var scanSimEmail by remember { mutableStateOf("nadia@biosynth-algae.co.uk") }
        var scanSimPhone by remember { mutableStateOf("+44 7911 123456") }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Persistent FB5 Inspired Header
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = Color.White
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Logo icon",
                                    tint = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Food Forward Summit",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    navigationIcon = {
                        // Brand Icon/Shield click trigger for Splash simulation
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Splash Simulator",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Notification bell icon with unread badge
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showNotificationsDialog = true
                            unreadNotificationsCount = 0
                        }) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge(containerColor = Color.Red) {
                                            Text(unreadNotificationsCount.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (unreadNotificationsCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "Notifications bell",
                                    tint = Color.White
                                )
                            }
                        }

                        // Dedicated hardware scanning sim button (Lead Retrieval)
                        IconButton(
                            modifier = Modifier.testTag("lead_retrieval_quick_scanner"),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showGlobalScanDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = "Scan badge retrieval",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Persistent search icon
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSearchActive = true
                        }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Standard search grid",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_bar"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = 0
                        },
                        icon = { Icon(Icons.Default.Feed, contentDescription = "Home timeline feed") },
                        label = { Text("Feed", fontSize = 11.sp) },
                        modifier = Modifier.testTag("menu_tab_logistics")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = 1
                        },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Agenda schedules & video") },
                        label = { Text("Agenda", fontSize = 11.sp) },
                        modifier = Modifier.testTag("menu_tab_agenda")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = 2
                        },
                        icon = { Icon(Icons.Default.Diversity3, contentDescription = "AI Matchmaker & Rolodex") },
                        label = { Text("Networking", fontSize = 11.sp) },
                        modifier = Modifier.testTag("menu_tab_matchmaker")
                    )
                    val isAdmin = profile?.role == "Admin"
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = 3
                        },
                        icon = {
                            if (isAdmin) {
                                Icon(Icons.Default.Diversity3, contentDescription = "Attendee Registry")
                            } else {
                                Icon(Icons.Default.Business, contentDescription = "Exhibitor floor plan")
                            }
                        },
                        label = { Text(if (isAdmin) "Attendee" else "Exhibitors", fontSize = 11.sp) },
                        modifier = Modifier.testTag("menu_tab_map")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = 4
                        },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "More & Admin CMS") },
                        label = { Text("More", fontSize = 11.sp) },
                        modifier = Modifier.testTag("menu_tab_networking")
                    )
                }
            }
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            
            // Render Selected View Switcher
            when (selectedTab) {
                0 -> HomeTab(viewModel = viewModel, modifier = modifier)
                1 -> AgendaTab(viewModel = viewModel, modifier = modifier)
                2 -> InteractiveNetworkingTab(viewModel = viewModel, modifier = modifier)
                3 -> {
                    val isUserAdmin = profile?.role == "Admin"
                    if (isUserAdmin) {
                        AttendeeTab(viewModel = viewModel, modifier = modifier)
                    } else {
                        FloorplanTab(viewModel = viewModel, modifier = modifier)
                    }
                }
                4 -> MenuTab(viewModel = viewModel, modifier = modifier)
            }

            // Notification Bell Dialog overlay
            if (showNotificationsDialog) {
                Dialog(onDismissRequest = { showNotificationsDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Broadcast System Notifications", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                IconButton(onClick = { showNotificationsDialog = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close dialog")
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (notificationsList.isEmpty()) {
                                Text("No incoming notifications currently.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.heightIn(max = 250.dp)
                                ) {
                                    items(notificationsList) { notification ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(notification, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TextButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    notificationsList.remove(notification)
                                                }
                                            ) {
                                                Text("Read", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showNotificationsDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color(0xFF0A2A33)
                                )
                            ) {
                                Text("Close announcements panel", color = Color(0xFF0A2A33))
                            }
                        }
                    }
                }
            }

            // Global Scan Dialog (Hardware Lead Retrieval Simulation)
            if (showGlobalScanDialog) {
                Dialog(onDismissRequest = { showGlobalScanDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Exhibitor Lead Retrieval Simulator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Simulate native optical hardware badge scans of other attendees to instantly capture hot/warm lead data.", style = MaterialTheme.typography.bodySmall)

                            OutlinedTextField(
                                value = scanSimName,
                                onValueChange = { scanSimName = it },
                                label = { Text("Attendee Full Name") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = scanSimCompany,
                                onValueChange = { scanSimCompany = it },
                                label = { Text("Attendee Company") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = scanSimEmail,
                                onValueChange = { scanSimEmail = it },
                                label = { Text("Contact Email") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = scanSimPhone,
                                onValueChange = { scanSimPhone = it },
                                label = { Text("Phone Number") },
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showGlobalScanDialog = false }) { Text("Cancel") }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        // Save lead data
                                        viewModel.scanContact(scanSimName, scanSimCompany, scanSimEmail, scanSimPhone, "Hot")
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showGlobalScanDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color(0xFF0A2A33)
                                    )
                                ) {
                                    Text("Simulate Scan (+40 XP)", color = Color(0xFF0A2A33))
                                }
                            }
                        }
                    }
                }
            }

            // Standardized Immersive Search Grid overlay
            if (isSearchActive) {
                Dialog(onDismissRequest = { isSearchActive = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search sessions, speakers, companies...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search results")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Global Enterprise Search Results Grid", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Search matcher
                            val filteredSess = sessions.filter {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                it.speaker.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                            }

                            val filteredExhi = exhibitors.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.focus.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                            }

                            if (searchQuery.trim().isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Type your search query", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                        Text("E.g. seaweed, protein, drone, Marcus", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            } else if (filteredSess.isEmpty() && filteredExhi.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No matching items found.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (filteredSess.isNotEmpty()) {
                                        item {
                                            Text("SESSIONS & SPEAKERS (${filteredSess.size})", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                        }
                                        items(filteredSess) { session ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text(session.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                                        TextButton(onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            viewModel.toggleSessionBookmark(session)
                                                        }) {
                                                            Text(if (session.isBookmarked) "Scheduled" else "Schedule")
                                                        }
                                                    }
                                                    Text("🎤 ${session.speaker} (${session.speakerRole})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("📅 ${session.startTime} - ${session.endTime} • ${session.location}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }

                                    if (filteredExhi.isNotEmpty()) {
                                        item {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("EXHIBITOR PORTFOLIOS (${filteredExhi.size})", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                        }
                                        items(filteredExhi) { exhibitor ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text(exhibitor.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                        Text(exhibitor.boothLocation, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    Text(exhibitor.focus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(exhibitor.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


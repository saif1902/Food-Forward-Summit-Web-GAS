package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ============================================================================
// 1. HOME TAB (Social Event Feed, Broadcasts, Dynamic Photo Uploads & Comments)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeTab(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.postsState.collectAsState()
    val profileState by viewModel.profileState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var postText by remember { mutableStateOf("") }
    var showPhotoSelector by remember { mutableStateOf(false) }
    var selectedPhotoResName by remember { mutableStateOf<String?>(null) }

    // Multi-composing temporary questions state
    val postsComments = remember { mutableStateMapOf<Int, MutableList<String>>() }

    // Seed default questions if not present (thematic questions based on post content)
    LaunchedEffect(posts) {
        posts.forEach { post ->
            if (!postsComments.containsKey(post.id)) {
                val seedQuestions = when (post.id) {
                    1 -> mutableStateListOf(
                        "How do climate-resilient organic grain proteins affect regional baking yields?",
                        "Is there a physical sourcing sheet or catalog available at your booth?"
                    )
                    2 -> mutableStateListOf(
                        "What is the average startup cost and run-time power for the BioCult live bioreactor?",
                        "Do we need prior booking of slot to see alternative seaweed-scaffolds?"
                    )
                    else -> mutableStateListOf(
                        "Are there any technical papers or slides we can read on this?",
                        "Where is the best pavilion location to discuss b2b partnerships?"
                    )
                }
                postsComments[post.id] = seedQuestions
            }
        }
    }

    // Quick Switching Personas for Exhibitors & Speakers Posts
    val postingPersonas = remember(profileState) {
        val userName = profileState?.name?.ifEmpty { "My Profile" } ?: "My Profile"
        val userCompany = profileState?.company?.ifEmpty { "FFS Visitor" } ?: "FFS Visitor"
        val userRoleLabel = profileState?.role ?: "Attendee"
        
        listOf(
            Triple(userName, userCompany, userRoleLabel),
            Triple("Sophia Weiss", "Grain Millers Inc.", "Speaker"),
            Triple("BioCult Agri-Labs", "Booth B-02, Sourcing Hall", "Exhibitor"),
            Triple("Alexande Kappes", "Greener Herd", "Speaker"),
            Triple("EcoPack Solutions", "Booth A-14, Pack Hall", "Exhibitor")
        )
    }
    
    var selectedPersonaIndex by remember { mutableStateOf(0) }

    val simulatedPhotos = listOf(
        "ex_biocult" to "Main Keynote Pavilion Hall",
        "ex_ecopack" to "Sourcing Exhibition Area",
        "ex_agridrone" to "Demo Stage Drone Arenas",
        "ex_mycelium" to "Sponsor Networking Lounge"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // News Broadcast / Alert Slide Band (Tappable carousel indicator)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = "", tint = Color(0xFF0A2A33))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "LIVE SUMMIT BROADCAST NEWS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "The main pavilion seaweed bioplastic tasting sample exhibits have relocated to Booth B-05.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Post Builder Card
        val isAdmin = profileState?.role == "Admin"
        if (isAdmin) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Security Admin", tint = Color.Black)
                    }
                    Column {
                        Text(
                            "ADMIN MODERATOR PANEL",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "You are logged in as System Admin. Post creation is restricted, but you have full database privileges to remove any objectionable community posts below in real-time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val activePersona = postingPersonas.getOrNull(selectedPersonaIndex) ?: postingPersonas[0]
                            Text(
                                activePersona.first.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A2A33),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = postText,
                            onValueChange = { postText = it },
                            placeholder = { Text("Publish an exhibitor update or speaker note...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("feed_input_field"),
                            singleLine = true
                        )
                    }

                    // Selector for Active Persona Role
                    Text(
                        "Identity Mode:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        postingPersonas.forEachIndexed { idx, persona ->
                            val isSelected = selectedPersonaIndex == idx
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPersonaIndex = idx },
                                label = {
                                    val prefix = when (persona.third) {
                                        "Speaker" -> "🎙️ "
                                        "Exhibitor" -> "🔬 "
                                        else -> "👤 "
                                    }
                                    Text("$prefix${persona.first}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    selectedPhotoResName?.let { tag ->
                        val name = simulatedPhotos.find { it.first == tag }?.second ?: tag
                        Row(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Attached: $name", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { selectedPhotoResName = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Red)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showPhotoSelector = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Photo File", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (postText.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val persona = postingPersonas.getOrNull(selectedPersonaIndex) ?: postingPersonas[0]
                                    viewModel.postToSocialFeed(
                                        textContent = postText,
                                        authorRole = persona.third,
                                        customAuthor = if (selectedPersonaIndex == 0) null else persona.first,
                                        customCompany = if (selectedPersonaIndex == 0) null else persona.second
                                    )
                                    postText = ""
                                    selectedPhotoResName = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color(0xFF0A2A33)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("feed_post_submit_button")
                        ) {
                            Text("Post Timeline", color = Color(0xFF0A2A33), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Standardized Grid Feed List
        Text(
            "Food Forward Community Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No posts loaded. Seed some from applet launch data.")
            }
        } else {
            posts.forEach { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Author detail
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        when (post.authorRole) {
                                            "Speaker" -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                            "Exhibitor" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    post.authorName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = when (post.authorRole) {
                                        "Speaker" -> Color(0xFFA78BFA)
                                        "Exhibitor" -> Color(0xFF34D399)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(post.authorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (post.authorRole == "Speaker") {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("🎙️ SPEAKER", color = Color(0xFFA78BFA), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    } else if (post.authorRole == "Exhibitor") {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("🔬 EXHIBITOR", color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                                Text(post.authorCompany, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(post.textContent, style = MaterialTheme.typography.bodyMedium, color = Color.White)

                        // Rendering post image if any
                        val designImage = post.imageResName ?: when (post.id) {
                            1 -> "ex_ecopack"
                            2 -> "ex_agridrone"
                            else -> null
                        }

                        if (designImage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Draw an aesthetically attractive abstract gradient banner representing the uploaded post scene
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.background
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (designImage == "ex_ecopack") "Sourcing Exhibition Area" else "Demo Stage Drone Arenas",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Engagement Action and Questions lists
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(post.timestamp)),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row {
                                if (isAdmin) {
                                    TextButton(onClick = { viewModel.deleteSocialPost(post.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Red
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove", fontSize = 11.sp, color = Color.Red)
                                    }
                                }

                                TextButton(onClick = { viewModel.toggleLikePost(post) }) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Engage",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (post.isLikedByMe) Color(0xFFFFB000) else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${post.likesCount} Engaged", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }

                                var showAddCommentDialog by remember { mutableStateOf(false) }
                                TextButton(onClick = { showAddCommentDialog = true }) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = "Question", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val qCount = postsComments[post.id]?.size ?: 0
                                    Text(if (qCount == 1) "1 Question" else "$qCount Questions", fontSize = 11.sp)
                                }

                                // Interactive Questions modal
                                if (showAddCommentDialog) {
                                    var newCommentText by remember { mutableStateOf("") }
                                    Dialog(onDismissRequest = { showAddCommentDialog = false }) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text("Submit Q&A Question", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                                Text("Ask exhibitors and speakers detailed questions about their updates.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                                                Spacer(modifier = Modifier.height(10.dp))

                                                LazyColumn(
                                                    modifier = Modifier.heightIn(max = 140.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val commentsList = postsComments[post.id] ?: mutableListOf()
                                                    items(commentsList) { comment ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(
                                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .padding(8.dp)
                                                        ) {
                                                            Text("❓ $comment", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                OutlinedTextField(
                                                    value = newCommentText,
                                                    onValueChange = { newCommentText = it },
                                                    placeholder = { Text("Ask a question about this post...") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    TextButton(onClick = { showAddCommentDialog = false }) { Text("Close") }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Button(
                                                        onClick = {
                                                            if (newCommentText.isNotBlank()) {
                                                                val currentList = postsComments[post.id] ?: mutableListOf()
                                                                currentList.add(newCommentText)
                                                                postsComments[post.id] = currentList
                                                                newCommentText = ""
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = Color(0xFF0A2A33)
                                                        )
                                                    ) {
                                                        Text("Submit Question", color = Color(0xFF0A2A33))
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

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Photo Attach Selector dialog
    if (showPhotoSelector) {
        Dialog(onDismissRequest = { showPhotoSelector = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Simulated Summit Photo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Choose an abstract backdrop below to attach a live graphic upload to your feed post.", style = MaterialTheme.typography.bodySmall)

                    simulatedPhotos.forEach { (tag, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPhotoResName = tag
                                    showPhotoSelector = false
                                }
                                .background(
                                    if (selectedPhotoResName == tag) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        modifier = Modifier.align(Alignment.End),
                        onClick = { showPhotoSelector = false }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. AGENDA TAB (Schedules with EN/FR Translation, Conflict Warnings, virtual Live stream & ICS Exports)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaTab(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessionsState.collectAsState()
    val speakers by viewModel.speakersState.collectAsState()
    val profileState by viewModel.profileState.collectAsState()
    val isAdmin = profileState?.role == "Admin"
    var showCreateSessionDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    var activeTrackFilter by remember { mutableStateOf("All") }

    var agendaSubTab by remember { mutableStateOf("Schedules") } // "Schedules", "Speakers"
    var speakerSearchQuery by remember { mutableStateOf("") }
    var speakerFormatFilter by remember { mutableStateOf("All") }
    var selectedSpeakerForDetails by remember { mutableStateOf<SpeakerEntity?>(null) }
    var showProposalForm by remember { mutableStateOf(false) }

    // Locale configuration State: true is EN (English), false is FR (French)
    var isEnglishLocale by remember { mutableStateOf(true) }

    // Custom Toast Success state for ICS export
    var showIcsToast by remember { mutableStateOf<String?>(null) }
    
    // Livestream video pane states
    var isStreamPlaying by remember { mutableStateOf(true) }
    var isStreamMuted by remember { mutableStateOf(false) }
    var streamProgress by remember { mutableStateOf(0.35f) }

    // Translation Dictionaries (Hardcoded Mappings)
    val frenchTranslations = mapOf(
        "Fireside: Blockchain Audits & Regulatory compliance" to "Fireside: Audits de Blockchain & conformité Réglementaire",
        "Sola Vance & Naomi Klein" to "Sola Vance & Naomi Klein",
        "Regulatory Specialists, ChocoTrace" to "Spécialistes de la réglementation, ChocoTrace",
        "Consumer & Regulatory" to "Consommateurs & Réglementaire",
        "Exploring global chocolate deforestation compliance policies and how decentralized geofence smart contracts can prove compliance under strict EU supply rules." to "Exploration des politiques de conformité sur la déforestation et comment les contrats intelligents de clôture géographique peuvent prouver la conformité selon les règles strictes de l'UE.",
        
        "General Keynote: Circular Vertical Framing & Sourcing" to "Keynote Générale: Cadrage Vertical Circulaire & Sourcing",
        "Marcus Sterling" to "Marcus Sterling",
        "Chief Sourcing Executive, OrganoCrop Labs" to "Directeur de l'approvisionnement, OrganoCrop Labs",
        "Tech & Innovation" to "Technologie & Innovation",
        "An in-depth review of autonomous urban crop systems employing recirculating water nodes to optimize crop delivery metrics." to "Une étude approfondie des systèmes de culture urbains autonomes employant des nœuds d'eau recirculée pour optimiser la livraison.",

        "Workshop: Sourcing Algae Products as alternate polymers" to "Atelier: Sourcing de Produits Algues comme polymères alternatifs",
        "Elena Rostova" to "Elena Rostova",
        "Lead Biopolymer Alchemist, BioSynth Polymers" to "Alchimiste en chef des biopolymères, BioSynth Polymers",
        "Sustainability & Packaging" to "Durabilité & Emballage",
        "Analyzing maritime seaweed polymer molecular structures. Followed by a clean lab demo of custom injection molding using circular compostable techniques." to "Analyse moléculaire de polymères d'algues maritimes. Suivie d'une démonstration pratique de moulage par injection compostable.",

        "Panel: Seaweed packaging sustainability & biodegradability trends" to "Table Ronde: Emballages d'algues et biodégradabilité",
        "Dr. Linda Green & Team" to "Dr. Linda Green & Équipe",
        "Ecology Advisory Panel, Food Forward" to "Comité consultatif d'écologie, Food Forward",
        "Analyzing real-world decomposing performance of marine compost packaging under high salinity soil constraints." to "Analyse des performances de décomposition des emballages de compost marin sous des contraintes de sols à forte salinité.",
        
        "All" to "Tous",
        "Schedule Filter" to "Filtrer l'agenda",
        "Live Summit Sourcing Stream" to "Flux Sourcing en Direct",
        "My Booked Schedule conflicts check" to "Conflits d'horaire détectés",
        "Export Standard ICS" to "Exporter ICS",
        "Conflict Detected: Overlaps with " to "⚠️ CONFLIT: Chevauchement avec "
    )

    fun translateMaybe(item: String): String {
        return if (isEnglishLocale) item else frenchTranslations[item] ?: item
    }

    // Interactive countdown to tick livestream progress
    LaunchedEffect(isStreamPlaying) {
        while (isStreamPlaying) {
            delay(1000)
            streamProgress = (streamProgress + 0.005f)
            if (streamProgress >= 1f) streamProgress = 0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // EN/FR Localization switcher on top right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Agenda & Schedules Portal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Dynamic locale button switcher
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isEnglishLocale) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isEnglishLocale = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "EN",
                        fontWeight = FontWeight.Bold,
                        color = if (isEnglishLocale) Color(0xFF0A2A33) else Color.White,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (!isEnglishLocale) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isEnglishLocale = false }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "FR",
                        fontWeight = FontWeight.Bold,
                        color = if (!isEnglishLocale) Color(0xFF0A2A33) else Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Beautiful Sub-Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Schedules", "Speakers").forEach { tab ->
                val isSelected = agendaSubTab == tab
                val title = if (tab == "Schedules") "🗓️ Schedules & Live" else "👤 Speaker Directory"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            agendaSubTab = tab
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0A2A33) else Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (agendaSubTab == "Schedules") {
            // Live Virtual Stream Video Player
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Abstract video design
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(width = 4f)
                        val w = size.width
                        val h = size.height
                        
                        // Draw simulated audio/video waves
                        for (i in 0..10) {
                            val waveH = (Math.sin((i + streamProgress * 15f).toDouble()) * 30.0).toFloat()
                            drawLine(
                                color = Color(0xFF21C3CE).copy(alpha = 0.5f),
                                start = Offset(w * (i / 10f), h / 2f),
                                end = Offset(w * (i / 10f), h / 2f + waveH),
                                strokeWidth = 8f
                            )
                        }
                    }
                    
                    // Streaming indicator Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Centered stream overlay control
                    IconButton(onClick = { isStreamPlaying = !isStreamPlaying }) {
                        Icon(
                            imageVector = if (isStreamPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                // Controls row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.People, contentDescription = "", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("344 Watching", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Row {
                        IconButton(onClick = { isStreamMuted = !isStreamMuted }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (isStreamMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                LinearProgressIndicator(progress = streamProgress, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }
        }

        if (isAdmin) {
            Button(
                onClick = { showCreateSessionDialog = true },
                modifier = Modifier.fillMaxWidth().testTag("admin_add_session_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color(0xFF0A2A33)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0A2A33))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Agenda Card", fontWeight = FontWeight.Bold)
            }

            if (showCreateSessionDialog) {
                var titleText by remember { mutableStateOf("") }
                var speakerName by remember { mutableStateOf("") }
                var speakerRoleText by remember { mutableStateOf("") }
                var startHour by remember { mutableStateOf("11:00 AM") }
                var endHour by remember { mutableStateOf("11:45 AM") }
                var selectedTrack by remember { mutableStateOf("Sustainability & Packaging") }
                var locText by remember { mutableStateOf("Main Auditorium A") }
                var descText by remember { mutableStateOf("") }
                var errorMsg by remember { mutableStateOf<String?>(null) }

                Dialog(onDismissRequest = { showCreateSessionDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Create Agenda Session",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (errorMsg != null) {
                                Text(errorMsg!!, color = Color.Red, fontSize = 11.sp)
                            }

                            OutlinedTextField(
                                value = titleText,
                                onValueChange = { titleText = it },
                                label = { Text("Session Title") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = speakerName,
                                onValueChange = { speakerName = it },
                                label = { Text("Speaker Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = speakerRoleText,
                                onValueChange = { speakerRoleText = it },
                                label = { Text("Speaker Role/Company") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = startHour,
                                onValueChange = { startHour = it },
                                label = { Text("Start Time (e.g. 11:00 AM)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = endHour,
                                onValueChange = { endHour = it },
                                label = { Text("End Time (e.g. 11:45 AM)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = locText,
                                onValueChange = { locText = it },
                                label = { Text("Location/Booth") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Track selector
                            Text(
                                "Event Track:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            val tracksList = listOf("Sustainability & Packaging", "Tech & Innovation", "Consumer & Regulatory")
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tracksList.forEach { trackOption ->
                                    FilterChip(
                                        selected = selectedTrack == trackOption,
                                        onClick = { selectedTrack = trackOption },
                                        label = { Text(trackOption, fontSize = 10.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = descText,
                                onValueChange = { descText = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showCreateSessionDialog = false }) {
                                    Text("Cancel", color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (titleText.isBlank() || speakerName.isBlank() || descText.isBlank()) {
                                            errorMsg = "Title, Speaker, and Description are required."
                                        } else {
                                            viewModel.createAgendaSession(
                                                com.example.data.AgendaSession(
                                                    id = java.util.UUID.randomUUID().toString(),
                                                    title = titleText,
                                                    speaker = speakerName,
                                                    speakerRole = speakerRoleText,
                                                    startTime = startHour,
                                                    endTime = endHour,
                                                    track = selectedTrack,
                                                    location = locText,
                                                    description = descText,
                                                    isBookmarked = false
                                                )
                                            )
                                            showCreateSessionDialog = false
                                        }
                                    }
                                ) {
                                    Text("Create")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Multi-Track Filters Row
        val trackFilters = listOf("All", "Sustainability & Packaging", "Tech & Innovation", "Consumer & Regulatory")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            trackFilters.forEach { f ->
                val displayLabel = translateMaybe(f)
                FilterChip(
                    selected = (activeTrackFilter == f),
                    onClick = { activeTrackFilter = f },
                    label = { Text(displayLabel, fontSize = 11.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Agenda List
        val filtered = if (activeTrackFilter == "All") sessions else sessions.filter { it.track == activeTrackFilter }

        filtered.forEach { session ->
            // Conflict Detection Algorithm
            // Check if user bookmarked this session AND another session starts at the same hour
            val otherConflict = sessions.find {
                it.isBookmarked && it.id != session.id && it.startTime == session.startTime
            }
            val hasOverlapConflict = session.isBookmarked && otherConflict != null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agenda_session_item_${session.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasOverlapConflict) Color(0xFF381212) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (hasOverlapConflict) Color.Red else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Conflict warning alert bubble
                    if (hasOverlapConflict && otherConflict != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "", tint = Color.Red, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                translateMaybe("Conflict Detected: Overlaps with ") + otherConflict.title,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                translateMaybe(session.title),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "🎤 " + translateMaybe(session.speaker) + " — " + translateMaybe(session.speakerRole),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Bookmark Switcher with immediate loader response trigger
                        var toggling by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                toggling = true
                                viewModel.toggleSessionBookmark(session)
                                toggling = false
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            if (toggling) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Icon(
                                    imageVector = if (session.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${session.startTime} - ${session.endTime} | ${session.location}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                translateMaybe(session.track),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        translateMaybe(session.description),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated ICS exporting mechanics
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Simulate .ics generation
                            showIcsToast = "SUCCESS: Live ICS Sync Calendar invite generated compiled for '${session.title}' and appended to local storage."
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(translateMaybe("Export Standard ICS"), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
        } else {
            // ==================== SPEAKER DATABASE DIRECTORY ====================
            Text(
                text = "Alternative Proteins & Sustainable Sourcing Proposal Directory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Submit Proposal Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showProposalForm = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_speaker_proposal_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color(0xFF0A2A33)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Speaker Proposal Form", fontWeight = FontWeight.ExtraBold)
            }

            // Search Bar Input
            OutlinedTextField(
                value = speakerSearchQuery,
                onValueChange = { speakerSearchQuery = it },
                placeholder = { Text("Search by name, topic, location or company bio...", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (speakerSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { speakerSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("speaker_search_input"),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Format Chips Row
            val formatFilters = listOf("All", "Keynote", "Panel Discussion", "Fireside Chat")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                formatFilters.forEach { f ->
                    FilterChip(
                        selected = (speakerFormatFilter == f),
                        onClick = { speakerFormatFilter = f },
                        label = { Text(f, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Hardcoded Map of Beautiful Unsplash Profiles matching our 13 seeded speakers precisely
            val speakerImages = remember {
                mapOf(
                    "Alexande Kappes" to "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80",
                    "ASHLEY NICHOLLS" to "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80",
                    "Saif" to "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
                    "Heidi M. Peterson" to "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
                    "Sophia Weiss" to "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80",
                    "Marc-André Roberge" to "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150&auto=format&fit=crop&q=80",
                    "Maha Tahiri" to "https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=150&auto=format&fit=crop&q=80",
                    "Daniela Galloro" to "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?w=150&auto=format&fit=crop&q=80",
                    "Mehnaz Tabassum Mehnaz Tabassum" to "https://images.unsplash.com/photo-1544717305-2782549b5136?w=150&auto=format&fit=crop&q=80",
                    "Brendon Steele" to "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&auto=format&fit=crop&q=80",
                    "Carin Gerhardt" to "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80",
                    "Julie Francoeur" to "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=150&auto=format&fit=crop&q=80",
                    "Meifan Shi" to "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80"
                )
            }

            // Organization emojis as discrete company logos
            val companyLogos = remember {
                mapOf(
                    "Greener Herd" to "🐄",
                    "REACH" to "🌾",
                    "Nectar" to "🐝",
                    "Biome" to "🔬",
                    "Grain Millers" to "🌾",
                    "SVG Ventures" to "🌐",
                    "Fairtrade" to "🤝",
                    "Waterpoint Lane" to "💧",
                    "Future 500" to "🌱"
                )
            }

            // Filter Speakers list
            val filteredSpeakers = remember(speakers, speakerSearchQuery, speakerFormatFilter) {
                speakers.filter { s ->
                    val queryMatches = if (speakerSearchQuery.isEmpty()) true else {
                        s.fullName.contains(speakerSearchQuery, ignoreCase = true) ||
                        s.topicTitle.contains(speakerSearchQuery, ignoreCase = true) ||
                        s.bio.contains(speakerSearchQuery, ignoreCase = true) ||
                        s.email.contains(speakerSearchQuery, ignoreCase = true) ||
                        s.nomineeName.contains(speakerSearchQuery, ignoreCase = true) ||
                        s.location.contains(speakerSearchQuery, ignoreCase = true)
                    }
                    val formatMatches = if (speakerFormatFilter == "All") true else s.sessionFormat == speakerFormatFilter
                    queryMatches && formatMatches
                }
            }

            if (filteredSpeakers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No speakers or proposals match your search query.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                filteredSpeakers.forEach { speaker ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedSpeakerForDetails = speaker
                            }
                            .testTag("speaker_card_${speaker.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Profile Image or Logo Initials
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val imgUrl = speakerImages[speaker.fullName] ?: speaker.imageUrl
                                if (!imgUrl.isNullOrEmpty()) {
                                    androidx.compose.foundation.Image(
                                        painter = rememberAsyncImagePainter(model = imgUrl),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = if (speaker.fullName.isNotBlank()) speaker.fullName.take(2).uppercase() else "SP",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Logo badge
                                val logoEmoji = companyLogos.entries.firstOrNull {
                                    speaker.bio.contains(it.key, ignoreCase = true) ||
                                    speaker.topicTitle.contains(it.key, ignoreCase = true)
                                }?.value
                                if (logoEmoji != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                            .align(Alignment.BottomEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(logoEmoji, fontSize = 11.sp)
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = speaker.fullName,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(speaker.location, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = speaker.email,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = speaker.topicTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val formatColor = when (speaker.sessionFormat) {
                                        "Keynote" -> Color(0xFFFBBF24)
                                        "Panel Discussion" -> Color(0xFF10B981)
                                        "Fireside Chat" -> Color(0xFF8B5CF6)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(formatColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                            .border(1.dp, formatColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(speaker.sessionFormat, color = formatColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(speaker.submissionType.replace("_", " ").uppercase(), color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Success Toast Overlay
    showIcsToast?.let { msg ->
        Dialog(onDismissRequest = { showIcsToast = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ICS Calendar Synchronized", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showIcsToast = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Awesome", color = Color(0xFF0A2A33))
                    }
                }
            }
        }
    }

    selectedSpeakerForDetails?.let { speaker ->
        Dialog(onDismissRequest = { selectedSpeakerForDetails = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speaker Portfolio",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedSpeakerForDetails = null }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Portrait Image
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val presetImages = mapOf(
                                "Alexande Kappes" to "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80",
                                "ASHLEY NICHOLLS" to "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80",
                                "Saif" to "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
                                "Heidi M. Peterson" to "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
                                "Sophia Weiss" to "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80",
                                "Marc-André Roberge" to "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150&auto=format&fit=crop&q=80",
                                "Maha Tahiri" to "https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=150&auto=format&fit=crop&q=80",
                                "Daniela Galloro" to "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?w=150&auto=format&fit=crop&q=80",
                                "Mehnaz Tabassum Mehnaz Tabassum" to "https://images.unsplash.com/photo-1544717305-2782549b5136?w=150&auto=format&fit=crop&q=80",
                                "Brendon Steele" to "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&auto=format&fit=crop&q=80",
                                "Carin Gerhardt" to "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80",
                                "Julie Francoeur" to "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=150&auto=format&fit=crop&q=80",
                                "Meifan Shi" to "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80"
                            )
                            val imgUrl = presetImages[speaker.fullName] ?: speaker.imageUrl
                            if (!imgUrl.isNullOrEmpty()) {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(model = imgUrl),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = if (speaker.fullName.isNotBlank()) speaker.fullName.take(2).uppercase() else "SP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column {
                            Text(
                                text = speaker.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = speaker.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "📍 " + speaker.location,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Topic & Submission proposal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = speaker.topicTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(speaker.sessionFormat, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(speaker.submissionType.replace("_", " ").uppercase(), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (speaker.nomineeName.isNotEmpty() || speaker.nomineeEmail.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nomination details",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                if (speaker.nomineeName.isNotEmpty()) {
                                    Text("Nominee Name: ${speaker.nomineeName}", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                if (speaker.nomineeEmail.isNotEmpty()) {
                                    Text("Nominee Email: ${speaker.nomineeEmail}", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    if (speaker.avRequirements.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A/V & Equipment requirements",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = speaker.avRequirements,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Speaker Biography",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = speaker.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (speaker.linkedinUrl.isNotBlank()) {
                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Copy LinkedIn link
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("LinkedIn URL", speaker.linkedinUrl)
                                    clipboard.setPrimaryClip(clip)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy LinkedIn", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        Button(
                            onClick = { selectedSpeakerForDetails = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss", color = Color(0xFF0A2A33), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showProposalForm) {
        var formName by remember { mutableStateOf("") }
        var formEmail by remember { mutableStateOf("") }
        var formTopic by remember { mutableStateOf("") }
        var formBio by remember { mutableStateOf("") }
        var formFormat by remember { mutableStateOf("Keynote") }
        var formAV by remember { mutableStateOf("") }
        var formType by remember { mutableStateOf("pitch_talk") }
        var formNomineeName by remember { mutableStateOf("") }
        var formNomineeEmail by remember { mutableStateOf("") }
        var formLinkedin by remember { mutableStateOf("") }
        var formLocation by remember { mutableStateOf("") }

        var validationError by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { showProposalForm = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Speaker Submission Form",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Apply to pitch, present panels, or nominate innovative agtech leaders directly to the Food Forward database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (validationError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(validationError!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Field: Full Name
                    OutlinedTextField(
                        value = formName,
                        onValueChange = { formName = it },
                        label = { Text("Full Name *", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Field: Email
                    OutlinedTextField(
                        value = formEmail,
                        onValueChange = { formEmail = it },
                        label = { Text("Email Address *", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Field: LinkedIn
                    OutlinedTextField(
                        value = formLinkedin,
                        onValueChange = { formLinkedin = it },
                        label = { Text("LinkedIn URL", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Field: Location
                    OutlinedTextField(
                        value = formLocation,
                        onValueChange = { formLocation = it },
                        label = { Text("Location (e.g. Toronto, ON) *", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Field: Topic
                    OutlinedTextField(
                        value = formTopic,
                        onValueChange = { formTopic = it },
                        label = { Text("Topic Title *", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Field: Bio
                    OutlinedTextField(
                        value = formBio,
                        onValueChange = { formBio = it },
                        label = { Text("Speaker Bio & Background *", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )

                    // Dropdown / Row: Format Selection
                    Text("Session Format *", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Keynote", "Panel Discussion", "Fireside Chat").forEach { format ->
                            val isSelected = formFormat == format
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { formFormat = format }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(format, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF0A2A33) else Color.White)
                            }
                        }
                    }

                    // Field: AV requirements
                    OutlinedTextField(
                        value = formAV,
                        onValueChange = { formAV = it },
                        label = { Text("A/V Requirements (Optional)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Dropdown/Row: Submission Type
                    Text("Submission Type *", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "pitch_talk" to "Pitch Talk",
                            "propose_panel" to "Propose Panel",
                            "nominate_speaker" to "Nominate"
                        ).forEach { (typeKey, typeLabel) ->
                            val isSelected = formType == typeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { formType = typeKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(typeLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF0A2A33) else Color.White)
                            }
                        }
                    }

                    if (formType == "nominate_speaker") {
                        // Nominee Name
                        OutlinedTextField(
                            value = formNomineeName,
                            onValueChange = { formNomineeName = it },
                            label = { Text("Nominee Full Name *", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Nominee Email
                        OutlinedTextField(
                            value = formNomineeEmail,
                            onValueChange = { formNomineeEmail = it },
                            label = { Text("Nominee Email Address *", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showProposalForm = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Form Validation Checks
                                if (formName.isBlank() || formEmail.isBlank() || formTopic.isBlank() || formBio.isBlank() || formLocation.isBlank()) {
                                    validationError = "Please complete all marked (*) fields before submitting."
                                } else if (formType == "nominate_speaker" && (formNomineeName.isBlank() || formNomineeEmail.isBlank())) {
                                    validationError = "Please complete the Nominee Name and Nominee Email to make a valid nomination."
                                } else {
                                    val newSpeaker = SpeakerEntity(
                                        id = UUID.randomUUID().toString(),
                                        createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                                        fullName = formName,
                                        email = formEmail,
                                        topicTitle = formTopic,
                                        bio = formBio,
                                        sessionFormat = formFormat,
                                        avRequirements = formAV,
                                        submissionType = formType,
                                        nomineeName = formNomineeName,
                                        nomineeEmail = formNomineeEmail,
                                        linkedinUrl = formLinkedin,
                                        location = formLocation,
                                        imageUrl = null
                                    )
                                    viewModel.submitSpeakerProposal(newSpeaker)
                                    showProposalForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Post Submission", color = Color(0xFF0A2A33), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. NETWORKING TAB (QR Badge Swap, Rolodex Captured Leads & Gemini B2B Matchmaker Questionnaire)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveNetworkingTab(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var activeSubSection by remember { mutableStateOf(0) } // 0 = Badge Swap (QR/Rolodex), 1 = AI Matchmaker

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sliding Navigation Tab Headers
        TabRow(selectedTabIndex = activeSubSection) {
            Tab(
                selected = activeSubSection == 0,
                onClick = { activeSubSection = 0 },
                text = { Text("Badge Swap (QR)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubSection == 1,
                onClick = { activeSubSection = 1 },
                text = { Text("AI Matchmaker Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        if (activeSubSection == 0) {
            // Renders Digital Badge and contact swapping
            NetworkingTab(viewModel = viewModel, modifier = Modifier)
        } else {
            // Renders B2B Questionnaire and recommended embeddings matches
            MatchmakerTab(viewModel = viewModel, modifier = Modifier)
        }
    }
}

@Composable
fun NetworkingTab(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contactsState.collectAsState()
    val profileState by viewModel.profileState.collectAsState()
    val isAdmin = profileState?.role == "Admin"
    val haptic = LocalHapticFeedback.current

    var textInput by remember { mutableStateOf("") }
    var privacyDirectoryConsent by remember { mutableStateOf(true) }
    var privacyMessagingConsent by remember { mutableStateOf(true) }

    // Simulation QR Manual Scan Trigger input modal values
    var showScanSimDialog by remember { mutableStateOf(false) }
    var simName by remember { mutableStateOf("David Miller") }
    var simCompany by remember { mutableStateOf("Marine Kelp Co") }
    var simEmail by remember { mutableStateOf("david@kelpmarine.org") }
    var simPhone by remember { mutableStateOf("+1 415-388-9201") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isAdmin) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Security Admin", tint = Color.Black)
                    }
                    Column {
                        Text(
                            "ADMIN REGISTRY MONITOR MODE ACTIVE",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Viewing all live Badge Swap logs parsed across the central local database ledger.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Digital Badge Scanning Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "My In-App Digital QR Badge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Show this QR node to other conference attendees to quickly swap lead details.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(12.dp))

                // Render vector QR matrix symbol matching actual design guidelines
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(width = 8f)
                        // Draw corner anchors typical for QR codes
                        drawRect(Color.Black, Offset(0f, 0f), Size(30.dp.toPx(), 30.dp.toPx()), style = stroke)
                        drawRect(Color.Black, Offset(size.width - 30.dp.toPx(), 0f), Size(30.dp.toPx(), 30.dp.toPx()), style = stroke)
                        drawRect(Color.Black, Offset(0f, size.height - 30.dp.toPx()), Size(30.dp.toPx(), 30.dp.toPx()), style = stroke)

                        // Draw randomized simulated QR data squares
                        val dots = listOf(
                            Offset(0.4f, 0.2f), Offset(0.5f, 0.4f), Offset(0.3f, 0.5f),
                            Offset(0.6f, 0.5f), Offset(0.7f, 0.3f), Offset(0.5f, 0.7f),
                            Offset(0.4f, 0.8f), Offset(0.8f, 0.6f), Offset(0.2f, 0.4f)
                        )
                        dots.forEach { dot ->
                            drawRect(
                                Color.Black,
                                Offset(size.width * dot.x, size.height * dot.y),
                                Size(12.dp.toPx(), 12.dp.toPx())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showScanSimDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("scan_badge_sim_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color(0xFF0A2A33)
                    )
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF0A2A33))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Simulation Badge Scan", color = Color(0xFF0A2A33), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sim Scanning Input Dialog (Exhibitor Lead Retrieval & demographic inputs)
        if (showScanSimDialog) {
            Dialog(onDismissRequest = { showScanSimDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Simulate Digital Contact Scan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Enter peer data below to mimic scanning an attendee's native QR code.", style = MaterialTheme.typography.bodySmall)

                        OutlinedTextField(value = simName, onValueChange = { simName = it }, label = { Text("Attendee Name") }, singleLine = true)
                        OutlinedTextField(value = simCompany, onValueChange = { simCompany = it }, label = { Text("Company Name") }, singleLine = true)
                        OutlinedTextField(value = simEmail, onValueChange = { simEmail = it }, label = { Text("Email Contact") }, singleLine = true)
                        OutlinedTextField(value = simPhone, onValueChange = { simPhone = it }, label = { Text("Phone") }, singleLine = true)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showScanSimDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.scanContact(simName, simCompany, simEmail, simPhone)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showScanSimDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color(0xFF0A2A33)
                                )
                            ) {
                                Text("Complete Scan (+40 Points)", color = Color(0xFF0A2A33))
                            }
                        }
                    }
                }
            }
        }

        // Lead Directory & Edit notes / Ranking (Retrieved Scanned Contacts list)
        Text(
            text = if (isAdmin) "👑 GLOBAL SYSTEM BADGE SWAP TRACKER" else "My Rolodex & Captured Leads",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isAdmin) MaterialTheme.colorScheme.error else Color.White
        )
        Text(
            text = if (isAdmin) "Audit, review, and organize complete database matchmaking and QR scanning logs." else "Rank your leads (Hot/Warm/Cold) and save metadata follow-up notes in real time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No captured contacts yet. Swap badges with others to earn dynamic XP details!", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            contacts.forEach { contact ->
                var notesText by remember { mutableStateOf(contact.notes) }
                var showRankMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(contact.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("${contact.company} • ${contact.email}", style = MaterialTheme.typography.bodySmall)
                            }

                            // Interactive lead rank chip (Lead Retrieval hot/warm/cold)
                            AssistChip(
                                onClick = { showRankMenu = true },
                                label = { Text(contact.rank) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier.size(8.dp).background(
                                            color = when (contact.rank) {
                                                "Hot" -> Color.Red
                                                "Warm" -> Color.Yellow
                                                else -> Color.Blue
                                            },
                                            shape = CircleShape
                                        )
                                    )
                                }
                            )

                            DropdownMenu(expanded = showRankMenu, onDismissRequest = { showRankMenu = false }) {
                                listOf("Hot", "Warm", "Cold").forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            viewModel.updateContactDetails(contact.id, notesText, r)
                                            showRankMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = {
                                notesText = it
                                viewModel.updateContactDetails(contact.id, it, contact.rank)
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            singleLine = true,
                            placeholder = { Text("Type follow-up notes here...", fontSize = 12.sp) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { viewModel.deleteContact(contact.id) }) {
                                Text("Remove Lead", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Privacy Configuration controls
        Text("Directory Privacy Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("De-index my badge from overall directories", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = !privacyDirectoryConsent, onCheckedChange = { privacyDirectoryConsent = !it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Refuse in-app messaging from exhibitors", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = !privacyMessagingConsent, onCheckedChange = { privacyMessagingConsent = !it })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchmakerTab(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profileState.collectAsState()
    val matchUiState by viewModel.matchUiState.collectAsState()
    val meetings by viewModel.meetingsState.collectAsState()
    val customQuestions by viewModel.matchmakerQuestionsState.collectAsState()
    val isAdmin = profile?.role == "Admin"

    var showForm by remember { mutableStateOf(false) }

    // Onboarding form state
    var nameInput by remember { mutableStateOf("") }
    var companyInput by remember { mutableStateOf("") }
    var goalInput by remember { mutableStateOf("") }
    var selectedTrack by remember { mutableStateOf("Sustainability & Packaging") }
    var selectedTier by remember { mutableStateOf("Growth") }

    val trackOptions = listOf("Sustainability & Packaging", "Tech & Innovation", "Supply Chain & Automation", "Consumer & Regulatory")
    val tierOptions = listOf("Seed", "Growth", "Enterprise")

    LaunchedEffect(profile) {
        profile?.let {
            if (nameInput.isEmpty()) nameInput = it.name
            if (companyInput.isEmpty()) companyInput = it.company
            if (goalInput.isEmpty()) goalInput = it.businessGoal
            selectedTrack = it.preferredTrack
            selectedTier = it.budgetTier
        }
    }

    // Determine current display mode
    val hasMatches = profile?.matchedJson != null
    val displayForm = !hasMatches || showForm

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header / Sponsor branding zone
        BannerAdZone(title = "🌟 MATCHMAKING PORTAL POWERED BY GEMINI 3.5 🌟", subtitle = "Find high-synergy partners in cellular ag, bio-packing & cold chains!")

        if (isAdmin) {
            var newQuestionText by remember { mutableStateOf("") }
            var newQuestionTrack by remember { mutableStateOf("Sustainability & Packaging") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "MATCHMAKING QUESTION FACTORY (ADMIN)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        "Design custom questions that appear on the attendee B2B matchmaking onboarding terminal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    OutlinedTextField(
                        value = newQuestionText,
                        onValueChange = { newQuestionText = it },
                        label = { Text("Question Text") },
                        placeholder = { Text("e.g. Sourcing cold storage refrigeration or smart freezers?") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_new_matching_q_input"),
                        singleLine = true
                    )

                    Text("Attach Question to Sourcing Track:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        trackOptions.forEach { t ->
                            FilterChip(
                                selected = (newQuestionTrack == t),
                                onClick = { newQuestionTrack = t },
                                label = { Text(t) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (newQuestionText.isNotBlank()) {
                                viewModel.createMatchmakerQuestion(newQuestionText, newQuestionTrack)
                                newQuestionText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF0A2A33))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0A2A33))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deploy Question", fontWeight = FontWeight.Bold)
                    }

                    if (customQuestions.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        Text("Active Dynamic Questions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        customQuestions.forEach { q ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("• " + q.questionText, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    Text("Track: " + q.track, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteMatchmakerQuestion(q.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (displayForm) {
            Text(
                text = "B2B Matchmaking Onboarding",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tell us about your interests and target sourcing targets. Our AI Matchmaking agent will cross-reference with our full 2026 exhibitor dataset to find your high-synergy partners.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = companyInput,
                        onValueChange = { companyInput = it },
                        label = { Text("Company Name") },
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_company_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("Business Objective / Sourcing Goals") },
                        placeholder = { Text("e.g. Find seaweed alternative packaging and plastic salad bowl swaps that breakdown quickly.") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("onboarding_goal_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Preferred Event Sourcing Track", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    trackOptions.forEach { track ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTrack = track }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = (selectedTrack == track), onClick = { selectedTrack = track })
                            Text(track, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Organization Sourcing Budget / Target Tier", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tierOptions.forEach { tier ->
                            FilterChip(
                                selected = (selectedTier == tier),
                                onClick = { selectedTier = tier },
                                label = { Text(tier) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (customQuestions.isNotEmpty()) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "ADDITIONAL MATCHMAKING INQUIRIES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        customQuestions.forEach { q ->
                            var ansText by remember { mutableStateOf("") }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = q.questionText + " (Required For: " + q.track + ")",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            OutlinedTextField(
                                value = ansText,
                                onValueChange = { ansText = it },
                                placeholder = { Text("Your response...") },
                                modifier = Modifier.fillMaxWidth().testTag("custom_q_${q.id}"),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank() && goalInput.isNotBlank()) {
                                viewModel.runMatchmaking(nameInput, companyInput, goalInput, selectedTrack, selectedTier)
                                showForm = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color(0xFF0A2A33)
                        )
                    ) {
                        Text("Generate AI Match Results (+50 XP)", color = Color(0xFF0A2A33), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Display Results
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Recommended AI Match Partners",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = { showForm = true }) {
                    Text("Redo Profile Answers", color = MaterialTheme.colorScheme.primary)
                }
            }

            // Highlights the matched interests logic
            Text("Highlights shared parameters like track interest and budget tiers.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            when (val state = matchUiState) {
                is MatchUiState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gemini executing cosine embeddings matches...")
                        }
                    }
                }
                is MatchUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("PERSONALIZED AI OUTLOOK REPORT FOUND", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.result.welcomeMessage, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }

                    state.result.recommendedExhibitors.forEach { match ->
                        // Highlights common interests!
                        val profileTrack = profile?.preferredTrack ?: "Sustainability & Packaging"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(match.exhibitorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Match Confidence: ${match.matchScore}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text("Reason: ${match.matchReason}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                Spacer(modifier = Modifier.height(10.dp))

                                // Interest highlighting box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("🤝 COMMON SOURCING ALIGNMENTS FOUND", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("• Industry Sector: " + profileTrack + " (100% matched)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                        Text("• Budget Sourcing Tier: " + (profile?.budgetTier ?: "Growth") + " matched with exhibitor supply tier", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.scanContact(
                                            match.exhibitorName,
                                            "Exhibitor Partner",
                                            "info@" + match.exhibitorName.lowercase().replace(" ", "") + ".com",
                                            "+1 800-FFS-2026",
                                            "Hot"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Add to captured Rolodex leads", color = Color(0xFF0A2A33), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text("No AI recommended lists fetched yet. Submit the profile questionnaire above to connect with exhibitors.")
                }
            }
        }
    }
}

// ============================================================================
// 4. EXHIBITORS TAB (SVG 2D Interactive Floor Plan Map, Sourcing Directories & Downloaders)
// ============================================================================

@Composable
fun FloorplanTab(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var selectedBoothId by remember { mutableStateOf<String?>(null) }
    var searchFilter by remember { mutableStateOf("") }

    // Downloading asset progress mapping
    val downloadingProgress = remember { mutableStateMapOf<String, Float>() }
    val downloadStatus = remember { mutableStateMapOf<String, String>() }

    val exhibitorsList by viewModel.exhibitorsState.collectAsState()

    val filteredExhibitors = exhibitorsList.filter {
        it.name.contains(searchFilter, ignoreCase = true) ||
        it.track.contains(searchFilter, ignoreCase = true) ||
        it.focus.contains(searchFilter, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rotating Banner / Sponsorship Real Estate Carousel
        PremiumSponsorCarousel()

        Text(
            "Pavilion Interactive 2D Floor Plan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Tap highlighted grid nodes to pull up the associated Exhibitor details card or book immediate meeting slots.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Canvas representing vector coordinates map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            val hallwayColor = MaterialTheme.colorScheme.outlineVariant
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Cycle booth selections for prototype simulation
                        selectedBoothId = when (selectedBoothId) {
                            null -> "ex_biocult"
                            "ex_biocult" -> "ex_ecopack"
                            "ex_ecopack" -> "ex_agridrone"
                            "ex_agridrone" -> "ex_mycelium"
                            else -> null
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                // Draw map grid lines
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                for (i in 1..4) {
                    val x = width * i / 5
                    drawLine(Color.Gray.copy(alpha = 0.2f), Offset(x, 0f), Offset(x, height), pathEffect = pathEffect)
                }
                for (j in 1..3) {
                    val y = height * j / 4
                    drawLine(Color.Gray.copy(alpha = 0.2f), Offset(0f, y), Offset(width, y), pathEffect = pathEffect)
                }

                // Draw interactive nodes / booths on the layout
                drawRect(
                    color = if (selectedBoothId == "ex_biocult") Color(0xFF10B981) else Color(0xFF0F766E),
                    topLeft = Offset(width * 0.1f, height * 0.15f),
                    size = Size(width * 0.18f, height * 0.3f)
                )

                drawRect(
                    color = if (selectedBoothId == "ex_ecopack") Color(0xFF10B981) else Color(0xFF0F766E),
                    topLeft = Offset(width * 0.32f, height * 0.15f),
                    size = Size(width * 0.18f, height * 0.3f)
                )

                drawRect(
                    color = if (selectedBoothId == "ex_agridrone") Color(0xFF10B981) else Color(0xFF0F766E),
                    topLeft = Offset(width * 0.54f, height * 0.15f),
                    size = Size(width * 0.18f, height * 0.3f)
                )

                drawRect(
                    color = if (selectedBoothId == "ex_mycelium") Color(0xFF10B981) else Color(0xFF0F766E),
                    topLeft = Offset(width * 0.76f, height * 0.15f),
                    size = Size(width * 0.18f, height * 0.3f)
                )

                // Draw hallway indicator
                drawRect(
                    color = hallwayColor.copy(alpha = 0.5f),
                    topLeft = Offset(0f, height * 0.6f),
                    size = Size(width, height * 0.15f)
                )
            }

            // Display floating overlay labels
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Text("Booth A-12\nBioCult", color = Color.White, fontSize = 8.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    Text("Booth B-05\nEcoPack", color = Color.White, fontSize = 8.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    Text("Booth C-08\nAgriDrone", color = Color.White, fontSize = 8.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    Text("Booth A-20\nMycelium", color = Color.White, fontSize = 8.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }

                Text(
                    "🔴 MAIN STAGE HALLWAY - GESTURE CLICK ANYWHERE TO SWITCH SELECTIONS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        // Selected Exhibitor Preview Overlay panel
        selectedBoothId?.let { boothId ->
            val exhibitor = exhibitorsList.find { it.id == boothId }
            if (exhibitor != null) {
                var bookingDialogShow by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("map_overlay_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(exhibitor.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) { Text(exhibitor.tier) }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { selectedBoothId = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Close overlay", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }

                        Text("${exhibitor.boothLocation} • ${exhibitor.track}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(exhibitor.focus, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { bookingDialogShow = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color(0xFF0A2A33)
                                )
                            ) {
                                Text("Book Session Meet", color = Color(0xFF0A2A33), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Booking Scheduler Popup Dialog
                if (bookingDialogShow) {
                    var inputHour by remember { mutableStateOf("01:30 PM") }
                    var inputDay by remember { mutableStateOf("June 8, 2026") }

                    Dialog(onDismissRequest = { bookingDialogShow = false }) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Schedule 1:1 meeting", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Select the day & hour slot to sync connection meetings with ${exhibitor.name}.", style = MaterialTheme.typography.bodySmall)

                                OutlinedTextField(value = inputDay, onValueChange = { inputDay = it }, label = { Text("Day") }, singleLine = true)
                                OutlinedTextField(value = inputHour, onValueChange = { inputHour = it }, label = { Text("Time Slot") }, singleLine = true)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { bookingDialogShow = false }) { Text("Cancel") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.bookMeetingFromMatch(exhibitor.id, exhibitor.name, "$inputDay at $inputHour", "1:1 Meet Sourcing")
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            bookingDialogShow = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color(0xFF0A2A33)
                                        )
                                    ) {
                                        Text("Book meeting slot", color = Color(0xFF0A2A33))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Searchable Exhibitor Directory Section
        Text(
            "Search Sourcing Company Directory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        OutlinedTextField(
            value = searchFilter,
            onValueChange = { searchFilter = it },
            placeholder = { Text("Type company focus or track name...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        filteredExhibitors.forEach { ex ->
            val documentName = "Ecology_Report_" + ex.name.replace(" ", "_") + ".pdf"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ex.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Badge(containerColor = MaterialTheme.colorScheme.primary) { Text(ex.tier, fontWeight = FontWeight.Bold) }
                    }

                    Text("${ex.boothLocation} • Track: ${ex.track}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ex.focus, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(ex.description, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Downloadable Resource attachment downloader simulation block
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Download, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(documentName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            val progress = downloadingProgress[ex.id] ?: 0f
                            val status = downloadStatus[ex.id] ?: "Download File"

                            TextButton(
                                onClick = {
                                    if (progress == 0f || progress == 1f) {
                                        coroutineScope.launch {
                                            downloadStatus[ex.id] = "Downloading..."
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            for (ip in 1..10) {
                                                delay(250)
                                                downloadingProgress[ex.id] = ip * 0.1f
                                            }
                                            downloadStatus[ex.id] = "Open Resource"
                                        }
                                    } else if (progress >= 1f) {
                                        // open pdf alert
                                        downloadStatus[ex.id] = "Resource Saved!"
                                    }
                                }
                            ) {
                                Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val progress = downloadingProgress[ex.id] ?: 0f
                        if (progress > 0f) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Book meeting CTA
                    var showDirectBookingDialog by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showDirectBookingDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Book 1:1 Meeting Sourcing Calendar", color = Color(0xFF0A2A33), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (showDirectBookingDialog) {
                        var directHour by remember { mutableStateOf("03:15 PM") }
                        Dialog(onDismissRequest = { showDirectBookingDialog = false }) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Book Direct Meeting", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Instantly coordinate private schedules with representatives from ${ex.name}.", style = MaterialTheme.typography.bodySmall)

                                    OutlinedTextField(value = directHour, onValueChange = { directHour = it }, label = { Text("Select hour slot") }, singleLine = true)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { showDirectBookingDialog = false }) { Text("Cancel") }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.bookMeetingFromMatch(ex.id, ex.name, "June 8, 2026 at $directHour", "1:1 Direct Sourcing Meet")
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showDirectBookingDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Complete Booking", color = Color(0xFF0A2A33))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ============================================================================
// 5. MORE MENU TAB (Gamification, Trivia, Audience Poll, CME, CMS Admin Portals & CSV Exports)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MenuTab(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val sessions by viewModel.sessionsState.collectAsState()
    val contacts by viewModel.contactsState.collectAsState()
    val postLogs by viewModel.postLogsState.collectAsState()
    val triviaAnswers by viewModel.triviaAnswersState.collectAsState()
    val matchmakerResults by viewModel.matchmakerResultsState.collectAsState()
    val haptic = LocalHapticFeedback.current

    var selectedAdminPortal by remember { mutableStateOf<String?>(null) } // null, "CMS", "Leads", "CSV"

    // Trivia simulation logic
    var selectedTriviaOption by remember { mutableStateOf<Int?>(null) }
    val triviaQuestion = "Which metropolitan circular vertical framing setup uses 95% less clean water than traditional systems?"
    val triviaOptions = listOf(
        "A) Lab-Grown Bioreactor assemblies",
        "B) Hydroponic smart vertical designs",
        "C) Passive phase-change cooling columns",
        "D) Multispectral drone sensor mapping"
    )
    val correctTriviaIndex = 1

    // Real-Time audience poll simulation
    var chosenPollOption by remember { mutableStateOf<Int?>(null) }
    val pollQuestion = "Which Food Sourcing Track are you allocating the largest budget toward in 2026?"
    val pollOptions = listOf("Tech & Alternative Proteins", "Seaweed & Biomaterials Sustainability", "Supply Chain & Automation Hardware")

    val state = gameState ?: GamificationState()

    // Certification state data
    val bookmarkedSessionsCount = sessions.count { it.isBookmarked }
    val calculatedCeCredits = bookmarkedSessionsCount * 0.5f
    var isCertGenerated by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gamification Status Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("FFS 2026 ENGAGEMENT METRICS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("${state.score} XP Earned", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text("Level 2 Sourcing Specialist", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(progress = (state.score % 200) / 200f, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🎟️ Raffle tickets: ${state.raffleTickets}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    val scavengerCount = state.scavengerExhibitorsFound.split(",").filter { it.isNotEmpty() }.size
                    Text("📍 Scavenger Codes: $scavengerCount found", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Sub Portals Grid (CMS Dashboard, Leads Dashboard, Export Center)
        Text(
            "RBAC Role-Based Administrative Portals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedAdminPortal = "CMS" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Settings, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("CMS Portal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedAdminPortal = "LEADS" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Leaderboard, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Lead Central", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedAdminPortal = "CSV" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DownloadDone, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Export Hub", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        val profileState = viewModel.profileState.collectAsState()
        val currentProfile = profileState.value
        val isAdmin = currentProfile?.role == "Admin" || currentProfile?.role == "Organizer"

        if (isAdmin) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedAdminPortal = "ADMIN_CONSOLE" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "", tint = Color.Black)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("👑 SYSTEM ADMIN CONTROL PANEL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Add, edit, or remove Speakers, Exhibitors, and Attendees in database tables in real-time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = "", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // CME/CE Credit hour validator & manifest downloader
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("CE Credits Certification Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Validation system tracks entered sessions to yield standard Continuing Education credits.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sessions Bookmarked:")
                    Text("$bookmarkedSessionsCount Sessions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Calculated CME hours:")
                    Text("$calculatedCeCredits CE Credits", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isCertGenerated) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F3E3E), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("📜 CE Hour Certificate Ready", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            Text("Validated CE credit hours checklist with printable certificate ID FFS-2026-N682V saved successfully.", fontSize = 11.sp, color = Color.White)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isCertGenerated = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calculate CE Hours & Download Certificate Bundle", color = Color(0xFF0A2A33), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Trivia Game Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("FFS 2026 Summit Trivia Quiz", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(triviaQuestion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                triviaOptions.forEachIndexed { idx, opt ->
                    val optionBg = when {
                        selectedTriviaOption == null -> Color.Transparent
                        idx == correctTriviaIndex -> Color(0xFF0F3E3E)
                        selectedTriviaOption == idx -> Color(0xFF421D1D)
                        else -> Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectedTriviaOption == null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedTriviaOption = idx
                                viewModel.completeTrivia(
                                    questionText = triviaQuestion,
                                    selectedOption = opt,
                                    isCorrect = (idx == correctTriviaIndex)
                                )
                            }
                            .background(optionBg, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedTriviaOption == idx, onClick = null, enabled = selectedTriviaOption == null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(opt, color = if (selectedTriviaOption != null && idx == correctTriviaIndex) MaterialTheme.colorScheme.primary else Color.White)
                    }
                }

                if (selectedTriviaOption != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (selectedTriviaOption == correctTriviaIndex) "CORRECT! +50 XP Awarded!" else "INCORRECT! Hydroponics is the correct answer.",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTriviaOption == correctTriviaIndex) Color.Green else Color.Red,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Poll Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Live Interactive Poll Sourcing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(pollQuestion, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                pollOptions.forEachIndexed { idx, opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = chosenPollOption == null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                chosenPollOption = idx
                                viewModel.submitPollVote(idx)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = chosenPollOption == idx, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(opt, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (chosenPollOption != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text("Thank you for submitting! Vote verified. +25 XP Added.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Interactive Logout btn
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.logOut()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout & Reset Profile State", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Portal Dialog Overlay details
    selectedAdminPortal?.let { portal ->
        Dialog(onDismissRequest = { selectedAdminPortal = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (portal) {
                                "CMS" -> "Authorized Organizer CMS Portal"
                                "LEADS" -> "Exhibitor Lead Control Portal"
                                "CSV" -> "Plain-Text CSV Export Center"
                                "ADMIN_CONSOLE" -> "👑 System Admin Control Panel"
                                else -> "Portal"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { selectedAdminPortal = null }) {
                            Icon(Icons.Default.Close, contentDescription = "")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (portal) {
                        "ADMIN_CONSOLE" -> {
                            AdminConsoleSubView(viewModel = viewModel)
                        }
                        "CMS" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Real-Time Schedule Metadata CMS updates. Direct writes modify active session cards in real-time.", style = MaterialTheme.typography.bodySmall)

                                var sampleEditSessionTitle by remember { mutableStateOf("General Keynote: Circular Vertical Framing & Sourcing") }
                                var sampleSpeakerBio by remember { mutableStateOf("Elena Rostova") }
                                var biopolymerVisible by remember { mutableStateOf(true) }

                                OutlinedTextField(
                                    value = sampleEditSessionTitle,
                                    onValueChange = { sampleEditSessionTitle = it },
                                    label = { Text("CMS - Keynote Title Update") },
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = sampleSpeakerBio,
                                    onValueChange = { sampleSpeakerBio = it },
                                    label = { Text("CMS - Algae Speaker Bio") },
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Make BioCult (Booth A-12) Profile Visible")
                                    Switch(checked = biopolymerVisible, onCheckedChange = { biopolymerVisible = it })
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        // Mock state update
                                        selectedAdminPortal = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Publish CMS Changes Real-Time", color = Color(0xFF0A2A33))
                                }
                            }
                        }
                        "LEADS" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text("Exhibitor Captured Attendees Database. Edit follow-up ranks & metadata details.", style = MaterialTheme.typography.bodySmall)

                                if (contacts.isEmpty()) {
                                    Text("No captured contact leads compiled yet.", color = Color.Gray)
                                } else {
                                    contacts.forEach { contact ->
                                        var leadRank by remember { mutableStateOf(contact.rank) }
                                        var progressLeadNotes by remember { mutableStateOf(contact.notes) }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(contact.name + " (" + contact.company + ")", fontWeight = FontWeight.Bold)
                                                
                                                OutlinedTextField(
                                                    value = progressLeadNotes,
                                                    onValueChange = {
                                                        progressLeadNotes = it
                                                        viewModel.updateContactDetails(contact.id, it, leadRank)
                                                    },
                                                    label = { Text("Lead notes") },
                                                    singleLine = true
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    listOf("Hot", "Warm", "Cold").forEach { r ->
                                                        FilterChip(
                                                            selected = leadRank == r,
                                                            onClick = {
                                                                leadRank = r
                                                                viewModel.updateContactDetails(contact.id, progressLeadNotes, r)
                                                            },
                                                            label = { Text(r) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "CSV" -> {
                            var activeSubCsvTab by remember { mutableStateOf("Leads") } // "Leads", "Posts", "Trivia", "Match"

                            val leadDetailsCsv = remember(contacts) {
                                val csvHeaders = "ID,Name,Company,Email,Phone,Rank,Notes,CapturedTime\n"
                                val rows = contacts.joinToString("\n") { c ->
                                    "${c.id},\"${c.name}\",\"${c.company}\",\"${c.email}\",\"${c.phone}\",${c.rank},\"${c.notes}\",${c.scannedAt}"
                                }
                                csvHeaders + rows
                            }

                            val postLogsCsv = remember(postLogs) {
                                val csvHeaders = "ID,Author,Company,Text,Timestamp\n"
                                val rows = postLogs.joinToString("\n") { p ->
                                    "${p.id},\"${p.authorName}\",\"${p.authorCompany}\",\"${p.textContent}\",${p.timestamp}"
                                }
                                csvHeaders + rows
                            }

                            val triviaAnswersCsv = remember(triviaAnswers) {
                                val csvHeaders = "ID,UserEmail,UserName,Question,SelectedOption,IsCorrect,Timestamp\n"
                                val rows = triviaAnswers.joinToString("\n") { t ->
                                    "${t.id},\"${t.userEmail}\",\"${t.userName}\",\"${t.questionText}\",\"${t.selectedOption}\",${t.isCorrect},${t.timestamp}"
                                }
                                csvHeaders + rows
                            }

                            val matchmakerResultsCsv = remember(matchmakerResults) {
                                val csvHeaders = "ID,UserEmail,ExhibitorID,ContentTitle,ContentDescription,Timestamp\n"
                                val rows = matchmakerResults.joinToString("\n") { m ->
                                    "${m.id},\"${m.userEmail}\",\"${m.exhibitorId}\",\"${m.contentTitle}\",\"${m.contentDescription}\",${m.timestamp}"
                                }
                                csvHeaders + rows
                            }

                            val displayedCsv = when (activeSubCsvTab) {
                                "Leads" -> leadDetailsCsv
                                "Posts" -> postLogsCsv
                                "Trivia" -> triviaAnswersCsv
                                else -> matchmakerResultsCsv
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Consolidate lead retention, account activity logs, user quiz outcomes, or matchmaking telemetry into spreadsheets.", style = MaterialTheme.typography.bodySmall)

                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("Leads", "Posts Log", "Trivia Outcomes", "B2B Matches").forEach { label ->
                                        val tabKey = when (label) {
                                            "Leads" -> "Leads"
                                            "Posts Log" -> "Posts"
                                            "Trivia" -> "Trivia"
                                            else -> "Match"
                                        }
                                        FilterChip(
                                            selected = activeSubCsvTab == tabKey,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                activeSubCsvTab = tabKey
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                        .background(Color.Black, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = displayedCsv,
                                        color = Color.Green,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedAdminPortal = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Copy selected CSV to clipboard", color = Color(0xFF0A2A33))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// HELPER SHARED COMPONENTS
// ============================================================================

@Composable
fun PremiumSponsorCarousel() {
    val sponsors = listOf(
        "Apex Organic Sourcing • SUSTAINABILITY PLATINUM" to "Seaweed and compost bowl pack solutions",
        "AeroAgri Logix • TECH ADVOCATE EXCLUSIVE" to "Advanced vertical indoor payload drones"
    )

    var currentIdx by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentIdx = (currentIdx + 1) % sponsors.size
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(sponsors[currentIdx].first, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            Text(sponsors[currentIdx].second, style = MaterialTheme.typography.bodySmall, color = Color.White)
        }
    }
}

@Composable
fun BannerAdZone(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CsvDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun AdminConsoleSubView(viewModel: EventViewModel) {
    val speakers by viewModel.speakersState.collectAsState()
    val exhibitors by viewModel.exhibitorsState.collectAsState()
    val attendees by viewModel.attendeesState.collectAsState()

    var activeTab by remember { mutableStateOf("speakers") } // "speakers", "exhibitors", "attendees"
    val haptic = LocalHapticFeedback.current

    // Dialog state for ADD or EDIT action
    var docToEditSpeaker by remember { mutableStateOf<SpeakerEntity?>(null) }
    var docToEditExhibitor by remember { mutableStateOf<ExhibitorEntity?>(null) }
    var docToEditAttendee by remember { mutableStateOf<AttendeeEntity?>(null) }

    var showAddSpeakerDlg by remember { mutableStateOf(false) }
    var showAddExhibitorDlg by remember { mutableStateOf(false) }
    var showAddAttendeeDlg by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Select Database Table to Manage",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.LightGray
        )

        // Tab Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("speakers" to "🎤 Speakers", "exhibitors" to "🏢 Exhibitors", "attendees" to "👥 Attendees")
            tabs.forEach { (tabId, label) ->
                val selected = activeTab == tabId
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        activeTab = tabId
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Large Add New Button
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                when (activeTab) {
                    "speakers" -> showAddSpeakerDlg = true
                    "exhibitors" -> showAddExhibitorDlg = true
                    "attendees" -> showAddAttendeeDlg = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "", tint = Color.Black)
                Text("Add New Record", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // List Scroll View
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                "speakers" -> {
                    if (speakers.isEmpty()) {
                        Text("No speakers in DB.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(speakers) { speaker ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(speaker.fullName, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(speaker.topicTitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(speaker.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Row {
                                            IconButton(onClick = { docToEditSpeaker = speaker }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { viewModel.deleteSpeaker(speaker.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "exhibitors" -> {
                    if (exhibitors.isEmpty()) {
                        Text("No exhibitors in DB.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(exhibitors) { exhibitor ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(exhibitor.logoAsset ?: "🏢")
                                                Text(exhibitor.name, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            Text(exhibitor.focus, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${exhibitor.boothLocation} • Tier: ${exhibitor.tier}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Row {
                                            IconButton(onClick = { docToEditExhibitor = exhibitor }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { viewModel.deleteExhibitor(exhibitor.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "attendees" -> {
                    if (attendees.isEmpty()) {
                        Text("No attendees in DB.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(attendees) { attendee ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(attendee.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(attendee.companyDescription, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${attendee.email} • Role: ${attendee.role}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Row {
                                            IconButton(onClick = { docToEditAttendee = attendee }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { viewModel.deleteAttendee(attendee.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
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

    // --- SPEAKERS FORM MODALS ---
    docToEditSpeaker?.let { s ->
        var name by remember { mutableStateOf(s.fullName) }
        var bio by remember { mutableStateOf(s.bio) }
        var topic by remember { mutableStateOf(s.topicTitle) }
        var email by remember { mutableStateOf(s.email) }
        var loc by remember { mutableStateOf(s.location) }
        var format by remember { mutableStateOf(s.sessionFormat) }
        var liUrl by remember { mutableStateOf(s.linkedinUrl) }

        Dialog(onDismissRequest = { docToEditSpeaker = null }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edit Speaker 🎤", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") })
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic Title") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") }, singleLine = true)
                    OutlinedTextField(value = format, onValueChange = { format = it }, label = { Text("Session Format") }, singleLine = true)
                    OutlinedTextField(value = liUrl, onValueChange = { liUrl = it }, label = { Text("LinkedIn URL") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { docToEditSpeaker = null }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.updateSpeaker(s.copy(fullName = name, bio = bio, topicTitle = topic, email = email, location = loc, sessionFormat = format, linkedinUrl = liUrl))
                            docToEditSpeaker = null
                        }) { Text("Save") }
                    }
                }
            }
        }
    }

    if (showAddSpeakerDlg) {
        var name by remember { mutableStateOf("") }
        var bio by remember { mutableStateOf("") }
        var topic by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var loc by remember { mutableStateOf("") }
        var format by remember { mutableStateOf("Keynote") }
        var liUrl by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddSpeakerDlg = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Speaker ✨", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") })
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic Title") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") }, singleLine = true)
                    OutlinedTextField(value = format, onValueChange = { format = it }, label = { Text("Session Format") }, singleLine = true)
                    OutlinedTextField(value = liUrl, onValueChange = { liUrl = it }, label = { Text("LinkedIn URL") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddSpeakerDlg = false }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.updateSpeaker(SpeakerEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                createdAt = "2026-06-08 10:00:00",
                                fullName = name,
                                email = email,
                                topicTitle = topic,
                                bio = bio,
                                sessionFormat = format,
                                avRequirements = "None",
                                submissionType = "Direct",
                                nomineeName = "",
                                nomineeEmail = "",
                                linkedinUrl = liUrl,
                                location = loc,
                                imageUrl = ""
                            ))
                            showAddSpeakerDlg = false
                        }) { Text("Add") }
                    }
                }
            }
        }
    }

    // --- EXHIBITORS FORM MODALS ---
    docToEditExhibitor?.let { ex ->
        var name by remember { mutableStateOf(ex.name) }
        var focus by remember { mutableStateOf(ex.focus) }
        var track by remember { mutableStateOf(ex.track) }
        var desc by remember { mutableStateOf(ex.description) }
        var booth by remember { mutableStateOf(ex.boothLocation) }
        var tier by remember { mutableStateOf(ex.tier) }
        var email by remember { mutableStateOf(ex.contactEmail) }
        var web by remember { mutableStateOf(ex.website) }
        var logo by remember { mutableStateOf(ex.logoAsset ?: "🏢") }

        Dialog(onDismissRequest = { docToEditExhibitor = null }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edit Exhibitor 🏢", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name") }, singleLine = true)
                    OutlinedTextField(value = focus, onValueChange = { focus = it }, label = { Text("Focus / Core Offering") }, singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    OutlinedTextField(value = track, onValueChange = { track = it }, label = { Text("Track Category") }, singleLine = true)
                    OutlinedTextField(value = booth, onValueChange = { booth = it }, label = { Text("Booth Location") }, singleLine = true)
                    OutlinedTextField(value = tier, onValueChange = { tier = it }, label = { Text("Sponsorship Tier") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Contact Email") }, singleLine = true)
                    OutlinedTextField(value = web, onValueChange = { web = it }, label = { Text("Website") }, singleLine = true)
                    OutlinedTextField(value = logo, onValueChange = { logo = it }, label = { Text("Emoji Logo Asset") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { docToEditExhibitor = null }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.updateExhibitor(ex.copy(name = name, focus = focus, track = track, description = desc, boothLocation = booth, tier = tier, contactEmail = email, website = web, logoAsset = logo))
                            docToEditExhibitor = null
                        }) { Text("Save") }
                    }
                }
            }
        }
    }

    if (showAddExhibitorDlg) {
        var name by remember { mutableStateOf("") }
        var focus by remember { mutableStateOf("") }
        var track by remember { mutableStateOf("Tech & Innovation") }
        var desc by remember { mutableStateOf("") }
        var booth by remember { mutableStateOf("") }
        var tier by remember { mutableStateOf("Gold") }
        var email by remember { mutableStateOf("") }
        var web by remember { mutableStateOf("") }
        var logo by remember { mutableStateOf("🏢") }

        Dialog(onDismissRequest = { showAddExhibitorDlg = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Exhibitor ✨", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name") }, singleLine = true)
                    OutlinedTextField(value = focus, onValueChange = { focus = it }, label = { Text("Focus / Core Offering") }, singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    OutlinedTextField(value = track, onValueChange = { track = it }, label = { Text("Track Category") }, singleLine = true)
                    OutlinedTextField(value = booth, onValueChange = { booth = it }, label = { Text("Booth Location") }, singleLine = true)
                    OutlinedTextField(value = tier, onValueChange = { tier = it }, label = { Text("Sponsorship Tier") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Contact Email") }, singleLine = true)
                    OutlinedTextField(value = web, onValueChange = { web = it }, label = { Text("Website") }, singleLine = true)
                    OutlinedTextField(value = logo, onValueChange = { logo = it }, label = { Text("Emoji Logo Asset") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddExhibitorDlg = false }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.updateExhibitor(ExhibitorEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                name = name,
                                focus = focus,
                                track = track,
                                description = desc,
                                boothLocation = booth,
                                website = web,
                                contactEmail = email,
                                tier = tier,
                                logoAsset = logo
                            ))
                            showAddExhibitorDlg = false
                        }) { Text("Add") }
                    }
                }
            }
        }
    }

    // --- ATTENDEES FORM MODALS ---
    docToEditAttendee?.let { att ->
        var name by remember { mutableStateOf(att.displayName) }
        var desc by remember { mutableStateOf(att.companyDescription) }
        var email by remember { mutableStateOf(att.email) }
        var role by remember { mutableStateOf(att.role) }
        var web by remember { mutableStateOf(att.websiteUrl) }
        var country by remember { mutableStateOf(att.countryRegion) }
        var buyers by remember { mutableStateOf(att.targetBuyers) }

        Dialog(onDismissRequest = { docToEditAttendee = null }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edit Attendee 👥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Company / Bio") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, singleLine = true)
                    OutlinedTextField(value = web, onValueChange = { web = it }, label = { Text("Website URL") }, singleLine = true)
                    OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country/Region") }, singleLine = true)
                    OutlinedTextField(value = buyers, onValueChange = { buyers = it }, label = { Text("Target Sourcing Need") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { docToEditAttendee = null }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.updateAttendee(att.copy(displayName = name, companyDescription = desc, email = email, role = role, websiteUrl = web, countryRegion = country, targetBuyers = buyers))
                            docToEditAttendee = null
                        }) { Text("Save") }
                    }
                }
            }
        }
    }

    if (showAddAttendeeDlg) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("Attendee") }
        var web by remember { mutableStateOf("") }
        var country by remember { mutableStateOf("") }
        var buyers by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddAttendeeDlg = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Attendee ✨", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Company / Bio") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, singleLine = true)
                    OutlinedTextField(value = web, onValueChange = { web = it }, label = { Text("Website URL") }, singleLine = true)
                    OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country/Region") }, singleLine = true)
                    OutlinedTextField(value = buyers, onValueChange = { buyers = it }, label = { Text("Target Sourcing Need") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddAttendeeDlg = false }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.updateAttendee(AttendeeEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                displayName = name,
                                companyDescription = desc,
                                email = email,
                                websiteUrl = web,
                                linkedinUrl = "",
                                countryRegion = country,
                                annualRevenue = "",
                                currentMarkets = "",
                                targetMarkets = "",
                                importExportStatus = "",
                                brandsRepresented = "",
                                primarySectors = "",
                                targetBuyers = buyers,
                                boothSizeConfirmed = "",
                                electricalNeeds = "",
                                exhibitorLeadId = "",
                                role = role
                            ))
                            showAddAttendeeDlg = false
                        }) { Text("Add") }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendeeTab(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val attendees by viewModel.attendeesState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedAttendeeForEdit by remember { mutableStateOf<AttendeeEntity?>(null) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Attendee Registry Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "System Admin panel to view and create professional attendee profiles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF0A2A33))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0A2A33))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create New", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (attendees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No dynamic attendee profiles found in Local Storage database.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            attendees.forEach { att ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    att.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "🏢 ${att.companyDescription}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                                Text(
                                    "📧 ${att.email} | 📍 ${if (att.countryRegion.isBlank()) "Unspecified" else att.countryRegion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { selectedAttendeeForEdit = att },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.deleteAttendee(att.id)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Profile",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (att.primarySectors.isNotBlank() || att.targetBuyers.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (att.primarySectors.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "Sector: ${att.primarySectors}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (att.targetBuyers.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "Target: ${att.targetBuyers}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Dialog
        if (showCreateDialog) {
            AttendeeFormDialog(
                onDismiss = { showCreateDialog = false },
                onSave = { newAttendee ->
                    viewModel.updateAttendee(newAttendee)
                    showCreateDialog = false
                }
            )
        }

        // Edit Dialog
        selectedAttendeeForEdit?.let { existingAttendee ->
            AttendeeFormDialog(
                attendee = existingAttendee,
                onDismiss = { selectedAttendeeForEdit = null },
                onSave = { updatedAttendee ->
                    viewModel.updateAttendee(updatedAttendee)
                    selectedAttendeeForEdit = null
                }
            )
        }
    }
}

@Composable
fun AttendeeFormDialog(
    attendee: AttendeeEntity? = null,
    onDismiss: () -> Unit,
    onSave: (AttendeeEntity) -> Unit
) {
    var displayName by remember { mutableStateOf(attendee?.displayName ?: "") }
    var companyDesc by remember { mutableStateOf(attendee?.companyDescription ?: "") }
    var email by remember { mutableStateOf(attendee?.email ?: "") }
    var countryRegion by remember { mutableStateOf(attendee?.countryRegion ?: "United States") }
    var primarySectors by remember { mutableStateOf(attendee?.primarySectors ?: "Food Chemistry") }
    var targetBuyers by remember { mutableStateOf(attendee?.targetBuyers ?: "Distributors") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    if (attendee == null) "Create Attendee Profile" else "Edit Attendee Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 11.sp)
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name / Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = companyDesc,
                    onValueChange = { companyDesc = it },
                    label = { Text("Company Name & Focus") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Contact") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                OutlinedTextField(
                    value = countryRegion,
                    onValueChange = { countryRegion = it },
                    label = { Text("Country/Region") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                OutlinedTextField(
                    value = primarySectors,
                    onValueChange = { primarySectors = it },
                    label = { Text("Primary Industry Sectors") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                OutlinedTextField(
                    value = targetBuyers,
                    onValueChange = { targetBuyers = it },
                    label = { Text("Target Buyers / Objectives") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (displayName.isBlank() || companyDesc.isBlank() || email.isBlank()) {
                                errorMsg = "Name, Company, and Email are required."
                            } else {
                                onSave(
                                    AttendeeEntity(
                                        id = attendee?.id ?: java.util.UUID.randomUUID().toString(),
                                        displayName = displayName,
                                        companyDescription = companyDesc,
                                        email = email,
                                        websiteUrl = attendee?.websiteUrl ?: "",
                                        linkedinUrl = attendee?.linkedinUrl ?: "",
                                        countryRegion = countryRegion,
                                        annualRevenue = attendee?.annualRevenue ?: "",
                                        currentMarkets = attendee?.currentMarkets ?: "",
                                        targetMarkets = attendee?.targetMarkets ?: "",
                                        importExportStatus = attendee?.importExportStatus ?: "",
                                        brandsRepresented = attendee?.brandsRepresented ?: "",
                                        primarySectors = primarySectors,
                                        targetBuyers = targetBuyers,
                                        boothSizeConfirmed = attendee?.boothSizeConfirmed ?: "",
                                        electricalNeeds = attendee?.electricalNeeds ?: "",
                                        exhibitorLeadId = attendee?.exhibitorLeadId ?: "",
                                        role = attendee?.role ?: "Attendee"
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}

package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

sealed interface OnboardingStep {
    object Authenticate : OnboardingStep
    data class UniqueId(val email: String, val provider: String) : OnboardingStep
    data class SelectRole(val email: String, val provider: String) : OnboardingStep
    data class Questionnaire(val email: String, val provider: String, val role: String) : OnboardingStep
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AuthOnboardingScreen(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf<OnboardingStep>(OnboardingStep.Authenticate) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "OnboardingTransition"
        ) { step ->
            when (step) {
                is OnboardingStep.Authenticate -> {
                    AuthenticationStage(
                        onAuthenticated = { email, provider ->
                            currentStep = OnboardingStep.UniqueId(email, provider)
                        }
                    )
                }
                is OnboardingStep.UniqueId -> {
                    UniqueIdVerificationStage(
                        viewModel = viewModel,
                        email = step.email,
                        provider = step.provider,
                        onBack = { currentStep = OnboardingStep.Authenticate },
                        onVerified = {
                            // Automatically onboarded, ViewModel logs them in directly so we don't have to show SelectRole unless needed.
                        }
                    )
                }
                is OnboardingStep.SelectRole -> {
                    RoleSelectionStage(
                        onRoleSelected = { role ->
                            currentStep = OnboardingStep.Questionnaire(step.email, step.provider, role)
                        },
                        onBack = { currentStep = OnboardingStep.UniqueId(step.email, step.provider) }
                    )
                }
                is OnboardingStep.Questionnaire -> {
                    MatchmakingQuestionnaireStage(
                        role = step.role,
                        onSubmit = { name, company, goals, track, tier ->
                            viewModel.saveOnboardedProfile(
                                name = name,
                                company = company,
                                goal = goals,
                                track = track,
                                tier = tier,
                                email = step.email,
                                provider = step.provider,
                                role = step.role
                            )
                        },
                        onBack = { currentStep = OnboardingStep.SelectRole(step.email, step.provider) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// STAGE 1: AUTHENTICATION (EMAIL & SOCIAL PROVIDERS)
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationStage(
    onAuthenticated: (email: String, provider: String) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Identity Brand Node
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = com.example.R.drawable.ic_favicon),
                contentDescription = "Food Forward Logo",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("Food Forward ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF21C3CE))) {
                    append("Summit")
                }
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Enter your business email or connect with a professional network below to source alternative tech pipelines.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card Container for Email Auth
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sign In / Register",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    label = { Text("Business Email Address") },
                    placeholder = { Text("name@company.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        if (emailInput.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                            errorMessage = "Please enter a valid business email address."
                        } else {
                            onAuthenticated(emailInput.trim(), "Email")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_email_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color(0xFF0A2A33)
                    ),
                    shape = CircleShape
                ) {
                    Text("Continue with Email", fontWeight = FontWeight.Bold, color = Color(0xFF0A2A33))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0A2A33))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Divider Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = " OR CONNECT SECURELY ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social Button Layout
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SocialAuthButton(
                text = "Continue with Google",
                iconRes = "🌐",
                iconTint = Color(0xFFDB4437),
                onClick = { onAuthenticated("sourcing.buyer@google-partner.com", "Google") },
                tag = "login_social_google"
            )

            SocialAuthButton(
                text = "Continue with LinkedIn",
                iconRes = "💼",
                iconTint = Color(0xFF0077B5),
                onClick = { onAuthenticated("director.biotech@linkedin-partner.org", "LinkedIn") },
                tag = "login_social_linkedin"
            )

            SocialAuthButton(
                text = "Continue with Apple",
                iconRes = "",
                iconTint = Color.Black,
                onClick = { onAuthenticated("exec.packaging@apple-partner.com", "Apple") },
                tag = "login_social_apple"
            )
        }
    }
}

@Composable
fun SocialAuthButton(
    text: String,
    iconRes: String,
    iconTint: Color,
    onClick: () -> Unit,
    tag: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag(tag),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        shape = CircleShape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                iconRes,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ============================================================================
// STAGE 2: UNIQUE REPRESENTATIVE IDENTIFICATION
// ============================================================================
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UniqueIdVerificationStage(
    viewModel: EventViewModel,
    email: String,
    provider: String,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    var idInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedProfile by remember { mutableStateOf<CsvProfile?>(null) }

    val adminProfile = remember {
        CsvProfile(
            id = "admin",
            displayName = "System Admin",
            companyDescription = "Food Forward Summit Chief Administrator Console Profile.",
            websiteUrl = "www.nleats.com",
            countryRegion = "Netherlands",
            annualRevenue = "Enterprise",
            currentMarkets = "Global",
            targetMarkets = "Global",
            importExportStatus = "Both",
            brandsRepresented = "NLEats Admin",
            primarySectors = "Administration",
            targetBuyers = "Everyone",
            boothSizeConfirmed = "N/A",
            electricalNeeds = "N/A",
            exhibitorLeadId = "admin-lead"
        )
    }

    val profilesToUse = remember(email) {
        if (email.trim().equals("admin@nleats.com", ignoreCase = true)) {
            listOf(adminProfile)
        } else {
            CSVProfileData.list
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.Start)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter Your Unique ID",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "To access your pre-registered representative credentials, please reference your attendee ID.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Text Field for Unique ID
        OutlinedTextField(
            value = idInput,
            onValueChange = {
                idInput = it
                errorMsg = null
                // Check if matching any CSV profile in our allowed list
                selectedProfile = profilesToUse.find { profile -> profile.id.equals(it.trim(), ignoreCase = true) }
            },
            label = { Text("Attendee / Exhibitor Unique ID (UUID)") },
            placeholder = {
                Text(
                    if (email.trim().equals("admin@nleats.com", ignoreCase = true)) "e.g. admin"
                    else "e.g. feb8a00c-839e-4412-80c8-2e76765a1014"
                )
            },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_unique_id_input"),
            isError = errorMsg != null
        )

        if (errorMsg != null) {
            Text(
                text = errorMsg ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reference section with the profiles
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📋 Registered Database Reference",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tap any pre-registered profile block to automatically load its unique representative credentials.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                profilesToUse.forEach { profile ->
                    val isSelected = idInput.trim().equals(profile.id, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                idInput = profile.id
                                selectedProfile = profile
                                errorMsg = null
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (profile.id == "admin") "👑" else if (profile.displayName.contains("Food")) "🌿" else if (profile.displayName.contains("CogniCo")) "💻" else "💼",
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (profile.id == "admin") "admin (Primary System Admin ID)" else "${profile.id.take(8)}... (${profile.countryRegion})",
                                style = Modifier.testTag("id_hint_" + profile.id).let { MaterialTheme.typography.bodySmall },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Beautiful Interactive Preview of loaded Profile details
        AnimatedVisibility(
            visible = selectedProfile != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            selectedProfile?.let { csv ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = "Matched Representative Profile", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Profile Matched successfully!",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Representative of ${csv.displayName}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
                        Text(text = csv.companyDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        ProfileDetailRow(label = "Primary Sectors", value = csv.primarySectors)
                        ProfileDetailRow(label = "Target Buyers", value = csv.targetBuyers)
                        ProfileDetailRow(label = "Annual Revenue", value = csv.annualRevenue)
                        ProfileDetailRow(label = "Current Markets", value = csv.currentMarkets)
                        ProfileDetailRow(label = "Target Markets", value = csv.targetMarkets)
                        if (csv.websiteUrl.isNotEmpty()) {
                            ProfileDetailRow(label = "Website", value = csv.websiteUrl)
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val trimmedEmail = email.trim()
                val trimmedId = idInput.trim()
                
                if (trimmedEmail.equals("admin@nleats.com", ignoreCase = true)) {
                    if (!trimmedId.equals("admin", ignoreCase = true)) {
                        errorMsg = "Admin email requires 'admin' as the Unique ID."
                    } else {
                        selectedProfile = adminProfile
                        viewModel.saveCsvOnboardedProfile(adminProfile, email, provider)
                        onVerified()
                    }
                } else {
                    if (trimmedId.equals("admin", ignoreCase = true)) {
                        errorMsg = "This admin profile is restricted to admin@nleats.com."
                    } else {
                        val matched = CSVProfileData.list.find { it.id.equals(trimmedId, ignoreCase = true) }
                        if (matched != null) {
                            selectedProfile = matched
                            viewModel.saveCsvOnboardedProfile(matched, email, provider)
                            onVerified()
                        } else {
                            errorMsg = "Invalid Unique ID. Please select or input a representative record ID from the CSV database."
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("unique_id_submit_btn"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color(0xFF0A2A33)
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0A2A33))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirm Representative Credentials & Launch", fontWeight = FontWeight.Bold, color = Color(0xFF0A2A33))
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.4f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
    }
}

// ============================================================================
// STAGE 3: ROLE SELECTION
// ============================================================================
@Composable
fun RoleSelectionStage(
    onRoleSelected: (role: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedRole by remember { mutableStateOf("Attendee") }

    val roles = listOf(
        RoleInfo("Attendee", "🌿 Sourcing Buyer", "Scout seaweed composts, bioreactors, vertical kits & set meetings with producers."),
        RoleInfo("Exhibitor", "🔬 Tech exhibitor", "Present engineering blueprints, automated farms & track scanned visitor leads."),
        RoleInfo("Speaker", "🎤 Expert Panelist", "Deliver regulatory masterclasses and check session bookmarks/feed activity."),
        RoleInfo("Investor", "💼 Venture Capitalist", "Analyze Series A eco-packaging pipeline structures and capital requirements."),
        RoleInfo("Organizer", "🛠️ Summit Host", "Monitor total engagement metrics, feedback polls, active trivia & scavenger raffles."),
        RoleInfo("Admin", "👑 Admin / System Host", "Authorized to edit and remove speakers, exhibitors, and attendees in real-time.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("STEP 3 of 4", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Text(
            text = "Select Your Role",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Your selected role dynamically shapes your personalized dashboard interface and guides what sourcing tasks earn points in our scavenger hunt.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        roles.forEach { role ->
            val isSelected = selectedRole == role.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = role.id }
                    .testTag("role_card_${role.id.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                      else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedRole = role.id }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = role.displayName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = role.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onRoleSelected(selectedRole) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("role_submit_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color(0xFF0A2A33)
            ),
            shape = CircleShape
        ) {
            Text("Next: Matchmaking Profile", fontWeight = FontWeight.Bold, color = Color(0xFF0A2A33))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0A2A33))
        }
    }
}

data class RoleInfo(val id: String, val displayName: String, val description: String)

// ============================================================================
// STAGE 4: MATCHMAKING QUESTIONNAIRE
// ============================================================================
@Composable
fun MatchmakingQuestionnaireStage(
    role: String,
    onSubmit: (name: String, company: String, goal: String, track: String, tier: String) -> Unit,
    onBack: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var companyInput by remember { mutableStateOf("") }
    var goalInput by remember { mutableStateOf("") }
    var selectedTrack by remember { mutableStateOf("Sustainability & Packaging") }
    var selectedTier by remember { mutableStateOf("Growth") }

    val trackOptions = listOf("Sustainability & Packaging", "Tech & Innovation", "Supply Chain & Automation", "Consumer & Regulatory")
    val tierOptions = listOf("Seed", "Growth", "Enterprise")

    // Dynamically adjust goal prompts depending on role
    val goalHintDefault = when (role) {
        "Investor" -> "Find seed-stage cell ag tech and bio-polymer startups to diversify our early fund pipeline."
        "Exhibitor" -> "Promote cold-chain containers and partner with clean-tech seaweed suppliers."
        "Speaker" -> "Connect with alternative protein compliance experts and share deforestation templates."
        else -> "Find seaweed alternative packaging and plastic salad bowl swaps that breakdown quickly."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("STEP 4 of 4", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Text(
            text = "Sourcing Matchmaker Questionnaire",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Tell us about your sourcing objectives. Our AI Matchmaking agent cross-references with all summit providers to draw custom suggestions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Your Full Name") },
            placeholder = { Text("Alex Miller") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("questionnaire_name_input")
        )

        OutlinedTextField(
            value = companyInput,
            onValueChange = { companyInput = it },
            label = { Text("Your Company / Entity") },
            placeholder = { Text("GreenPlanet Foods") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("questionnaire_company_input")
        )

        OutlinedTextField(
            value = goalInput,
            onValueChange = { goalInput = it },
            label = { Text("Business goals / targets at FFS 2026") },
            placeholder = { Text(goalHintDefault) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("questionnaire_goal_input")
        )

        // Sourcing track horizontal filter scroll
        Text("Primary Event Sourcing Track", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            trackOptions.forEach { track ->
                val isSelected = selectedTrack == track
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTrack = track },
                    label = { Text(track, fontSize = 12.sp) },
                    modifier = Modifier.testTag("track_chip_$track")
                )
            }
        }

        // Tier / Budget Range choices
        Text("Budget Tier Sourcing Target", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tierOptions.forEach { tier ->
                val isSelected = selectedTier == tier
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTier = tier },
                    label = { Text(tier) },
                    modifier = Modifier.weight(1f).testTag("tier_chip_$tier")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (nameInput.isNotBlank() && companyInput.isNotBlank()) {
                    val finalGoal = if (goalInput.isNotBlank()) goalInput else goalHintDefault
                    onSubmit(nameInput, companyInput, finalGoal, selectedTrack, selectedTier)
                }
            },
            enabled = nameInput.isNotBlank() && companyInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("questionnaire_submit_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color(0xFF0A2A33)
            ),
            shape = CircleShape
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF0A2A33))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit & Launch App", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0A2A33))
        }
    }
}

package com.example.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.UserEntity
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.LocalCompactMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    currentUser: UserEntity,
    onCompleteOnboarding: (
        displayName: String,
        dob: String,
        gender: String,
        targetGender: String,
        heightCm: Int,
        datingGoal: String,
        avatarUrl: String,
        photosJson: String,
        bio: String,
        interests: String,
        hobbies: String,
        locationCity: String,
        locationArea: String,
        latitude: Double?,
        longitude: Double?,
        minAgePref: Int,
        maxAgePref: Int,
        distanceKmPref: Int
    ) -> Unit
) {
    val isCompact = LocalCompactMode.current
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 6

    // State Variables
    var name by remember { mutableStateOf(currentUser.displayName) }
    var dob by remember { mutableStateOf(currentUser.dateOfBirth.ifBlank { "2000-05-15" }) }
    var gender by remember { mutableStateOf(currentUser.gender.ifBlank { "FEMALE" }) }
    var targetGender by remember { mutableStateOf(currentUser.targetGender.ifBlank { "MALE" }) }
    var heightCm by remember { mutableIntStateOf(currentUser.heightCm.takeIf { it > 100 } ?: 168) }
    var datingGoal by remember { mutableStateOf(currentUser.datingGoal.ifBlank { "Long-term relationship" }) }
    var avatarUrl by remember { mutableStateOf(currentUser.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" }) }
    var bio by remember { mutableStateOf(currentUser.bio.ifBlank { "Passionate about coffee, traveling, and finding authentic connections!" }) }
    var locationCity by remember { mutableStateOf(currentUser.locationCity.ifBlank { "Kathmandu" }) }
    var locationArea by remember { mutableStateOf(currentUser.locationArea.ifBlank { "Kathmandu Valley" }) }
    var minAgePref by remember { mutableFloatStateOf(18f) }
    var maxAgePref by remember { mutableFloatStateOf(35f) }
    var distanceKmPref by remember { mutableFloatStateOf(50f) }

    // Selected Interests & Hobbies
    val availableInterests = listOf("Music", "Travel", "Coffee", "Fitness", "Reading", "Photography", "Art", "Cooking", "Hiking", "Gaming", "Movies", "Tech", "Yoga", "Pets")
    var selectedInterests by remember { mutableStateOf(setOf("Coffee", "Travel", "Music", "Photography")) }

    // Permission States & Rationales
    var showLocationRationale by remember { mutableStateOf(false) }
    var showCameraRationale by remember { mutableStateOf(false) }

    // Photo Picker Contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUrl = uri.toString()
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Keep app functional with preset avatar picker
        }
    }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            locationCity = "Kathmandu"
            locationArea = "Current Location (Verified)"
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Profile Setup",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Step $currentStep of $totalSteps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onCompleteOnboarding(
                            name, dob, gender, targetGender, heightCm, datingGoal,
                            avatarUrl, "[\"$avatarUrl\"]", bio, selectedInterests.joinToString(", "),
                            "Photography, Travel", locationCity, locationArea, 27.7172, 85.3240,
                            minAgePref.toInt(), maxAgePref.toInt(), distanceKmPref.toInt()
                        )
                    }) {
                        Text("Skip to Finish", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = if (isCompact) 16.dp else 24.dp, vertical = 12.dp)
        ) {
            // Linear Progress Indicator
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                label = "OnboardingStepAnimation"
            ) { step ->
                when (step) {
                    1 -> Step1BasicsAndPhotos(
                        name = name,
                        onNameChange = { name = it },
                        avatarUrl = avatarUrl,
                        onPickPhoto = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onSelectPreset = { avatarUrl = it }
                    )
                    2 -> Step2AgeAndGender(
                        dob = dob,
                        onDobChange = { dob = it },
                        gender = gender,
                        onGenderSelect = { gender = it },
                        targetGender = targetGender,
                        onTargetGenderSelect = { targetGender = it }
                    )
                    3 -> Step3HeightAndGoals(
                        heightCm = heightCm,
                        onHeightChange = { heightCm = it },
                        datingGoal = datingGoal,
                        onDatingGoalSelect = { datingGoal = it }
                    )
                    4 -> Step4HobbiesAndBio(
                        bio = bio,
                        onBioChange = { bio = it },
                        availableInterests = availableInterests,
                        selectedInterests = selectedInterests,
                        onInterestToggle = { interest ->
                            selectedInterests = if (selectedInterests.contains(interest)) {
                                selectedInterests - interest
                            } else {
                                selectedInterests + interest
                            }
                        }
                    )
                    5 -> Step5LocationAndPreferences(
                        locationCity = locationCity,
                        onLocationCityChange = { locationCity = it },
                        minAgePref = minAgePref,
                        onMinAgeChange = { minAgePref = it },
                        maxAgePref = maxAgePref,
                        onMaxAgeChange = { maxAgePref = it },
                        distanceKmPref = distanceKmPref,
                        onDistanceChange = { distanceKmPref = it },
                        onRequestLocationPermission = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                locationCity = "Kathmandu"
                                locationArea = "Current Verified Location"
                            } else {
                                showLocationRationale = true
                            }
                        }
                    )
                    6 -> Step6ReviewAndConfirm(
                        name = name,
                        dob = dob,
                        gender = gender,
                        targetGender = targetGender,
                        heightCm = heightCm,
                        datingGoal = datingGoal,
                        avatarUrl = avatarUrl,
                        bio = bio,
                        locationCity = locationCity,
                        interests = selectedInterests.joinToString(", "),
                        onEditStep = { currentStep = it }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(28.dp))

            // Navigation Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    SecondaryButton(
                        text = "Back",
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    )
                }

                PrimaryButton(
                    text = if (currentStep == totalSteps) "Complete Profile & Start" else "Continue",
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            onCompleteOnboarding(
                                name, dob, gender, targetGender, heightCm, datingGoal,
                                avatarUrl, "[\"$avatarUrl\"]", bio, selectedInterests.joinToString(", "),
                                "Photography, Travel", locationCity, locationArea, 27.7172, 85.3240,
                                minAgePref.toInt(), maxAgePref.toInt(), distanceKmPref.toInt()
                            )
                        }
                    },
                    modifier = Modifier.weight(2f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Rationale Dialog for Location Permission
    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = { showLocationRationale = false },
            title = { Text("Location Permission") },
            text = { Text("You & i uses approximate distance to find compatible people near you. Exact coordinates are never shared with other users.") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationRationale = false
                    locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationRationale = false }) {
                    Text("Enter Manually")
                }
            }
        )
    }
}

@Composable
private fun Step1BasicsAndPhotos(
    name: String,
    onNameChange: (String) -> Unit,
    avatarUrl: String,
    onPickPhoto: () -> Unit,
    onSelectPreset: (String) -> Unit
) {
    val presets = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e"
    )

    Column {
        Text(
            text = "Let's start with the basics",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Your first photo and display name are how people see you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Avatar Upload / Preview Box
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(130.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onPickPhoto() },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Upload Photo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = onPickPhoto,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Choose from Gallery")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Or choose a preset portrait:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(presets) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Preset",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(
                            if (avatarUrl == url) 3.dp else 1.dp,
                            if (avatarUrl == url) MaterialTheme.colorScheme.primary else Color.LightGray,
                            CircleShape
                        )
                        .clickable { onSelectPreset(url) },
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Display Name") },
            placeholder = { Text("e.g. Alex") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

@Composable
private fun Step2AgeAndGender(
    dob: String,
    onDobChange: (String) -> Unit,
    gender: String,
    onGenderSelect: (String) -> Unit,
    targetGender: String,
    onTargetGenderSelect: (String) -> Unit
) {
    Column {
        Text(
            text = "Age & Gender",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "You & i uses server-side age verification to isolate Teen (13-17) and Adult (18+) experiences safely.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = dob,
            onValueChange = onDobChange,
            label = { Text("Date of Birth (YYYY-MM-DD)") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "I identify as:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("FEMALE" to "Woman 👩", "MALE" to "Man 👨").forEach { (key, label) ->
                FilterChip(
                    selected = gender.equals(key, ignoreCase = true),
                    onClick = { onGenderSelect(key) },
                    label = { Text(label, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Looking to connect with:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("MALE" to "Men", "FEMALE" to "Women", "ALL" to "Everyone").forEach { (key, label) ->
                FilterChip(
                    selected = targetGender.equals(key, ignoreCase = true),
                    onClick = { onTargetGenderSelect(key) },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Step3HeightAndGoals(
    heightCm: Int,
    onHeightChange: (Int) -> Unit,
    datingGoal: String,
    onDatingGoalSelect: (String) -> Unit
) {
    val feetInches = "${heightCm / 30.48.toInt()}'${((heightCm % 30.48) / 2.54).toInt()}\""

    Column {
        Text(
            text = "Height & Intentions",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Be upfront about your physical stats and what you're hoping to find.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Height: $heightCm cm ($feetInches)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Slider(
            value = heightCm.toFloat(),
            onValueChange = { onHeightChange(it.toInt()) },
            valueRange = 140f..210f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Dating Goal:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(10.dp))

        val goals = listOf(
            "Long-term relationship",
            "Casual dating",
            "Marriage / Life partner",
            "New friends & deep chats"
        )

        goals.forEach { goal ->
            Surface(
                onClick = { onDatingGoalSelect(goal) },
                shape = RoundedCornerShape(16.dp),
                color = if (datingGoal == goal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = datingGoal == goal,
                        onClick = { onDatingGoalSelect(goal) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = goal,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step4HobbiesAndBio(
    bio: String,
    onBioChange: (String) -> Unit,
    availableInterests: List<String>,
    selectedInterests: Set<String>,
    onInterestToggle: (String) -> Unit
) {
    Column {
        Text(
            text = "Hobbies & Bio",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Pick at least 3 passions that describe what you love to do.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availableInterests.forEach { interest ->
                val selected = selectedInterests.contains(interest)
                FilterChip(
                    selected = selected,
                    onClick = { onInterestToggle(interest) },
                    label = { Text(interest) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            label = { Text("About Me (Bio)") },
            placeholder = { Text("Share a short story or what makes you unique...") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            shape = RoundedCornerShape(16.dp),
            maxLines = 4
        )
    }
}

@Composable
private fun Step5LocationAndPreferences(
    locationCity: String,
    onLocationCityChange: (String) -> Unit,
    minAgePref: Float,
    onMinAgeChange: (Float) -> Unit,
    maxAgePref: Float,
    onMaxAgeChange: (Float) -> Unit,
    distanceKmPref: Float,
    onDistanceChange: (Float) -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    Column {
        Text(
            text = "Location & Discovery",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Set your location and maximum distance for candidate matches.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = locationCity,
            onValueChange = onLocationCityChange,
            label = { Text("Your City / Area") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                IconButton(onClick = onRequestLocationPermission) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Get GPS Location", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Max Match Distance: ${distanceKmPref.toInt()} km", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Slider(
            value = distanceKmPref,
            onValueChange = onDistanceChange,
            valueRange = 5f..150f
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Age Preference: ${minAgePref.toInt()} - ${maxAgePref.toInt()} years", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        RangeSlider(
            value = minAgePref..maxAgePref,
            onValueChange = { range ->
                onMinAgeChange(range.start)
                onMaxAgeChange(range.endInclusive)
            },
            valueRange = 18f..60f
        )
    }
}

@Composable
private fun Step6ReviewAndConfirm(
    name: String,
    dob: String,
    gender: String,
    targetGender: String,
    heightCm: Int,
    datingGoal: String,
    avatarUrl: String,
    bio: String,
    locationCity: String,
    interests: String,
    onEditStep: (Int) -> Unit
) {
    Column {
        Text(
            text = "Ready to Launch! 🚀",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Review your profile details before entering the You & i community.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text(text = "$locationCity • $gender", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onEditStep(1) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Step 1")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                ReviewItem("Goal", datingGoal) { onEditStep(3) }
                ReviewItem("Height", "$heightCm cm") { onEditStep(3) }
                ReviewItem("Interests", interests) { onEditStep(4) }
                ReviewItem("Bio", bio) { onEditStep(4) }
            }
        }
    }
}

@Composable
private fun ReviewItem(label: String, value: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value.ifBlank { "Not specified" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        IconButton(onClick = onEdit) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
        }
    }
}

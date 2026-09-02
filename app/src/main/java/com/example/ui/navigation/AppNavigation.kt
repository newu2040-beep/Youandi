package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.data.model.AgeGroup
import com.example.data.model.UserEntity
import com.example.data.repository.*
import com.example.ui.components.GuestActionModal
import com.example.ui.components.ReportSheet
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.chat.*
import com.example.ui.screens.discover.DiscoverScreen
import com.example.ui.screens.home.HomeDashboardScreen
import com.example.ui.screens.profile.MyProfileAndSettingsScreen
import com.example.ui.screens.profile.ProfileDetailScreen
import com.example.ui.screens.safety.SafetyCenterScreen
import com.example.ui.screens.welcome.AuthDialog
import com.example.ui.screens.welcome.WelcomeScreen
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Discover : Screen("discover", "Discover", Icons.Outlined.CompassCalibration, Icons.Filled.CompassCalibration)
    object Matches : Screen("matches", "Matches", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite)
    object Messages : Screen("messages", "Messages", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person)
}

@Composable
fun AppNavigation(
    authRepo: AuthRepository,
    discoveryRepo: DiscoveryRepository,
    socialRepo: SocialRepository,
    chatRepo: ChatRepository,
    safetyRepo: SafetyRepository,
    currentTheme: AppTheme,
    isDarkMode: Boolean,
    onSelectTheme: (AppTheme) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // State flows
    val currentSession by authRepo.currentSession.collectAsState(initial = null)
    val currentUserState = produceState<UserEntity?>(initialValue = null, currentSession) {
        value = authRepo.getCurrentUser()
    }
    val currentUser = currentUserState.value

    var activeTab by remember { mutableStateOf<Screen>(Screen.Home) }

    // Screen Overlays / Navigation Stack States
    var selectedUserForDetail by remember { mutableStateOf<UserEntity?>(null) }
    var activeChatConversation by remember { mutableStateOf<ConversationUiItem?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var showGuestRequiredModal by remember { mutableStateOf(false) }
    var showReportSheetForUser by remember { mutableStateOf<UserEntity?>(null) }
    var matchCelebrationUser by remember { mutableStateOf<UserEntity?>(null) }
    var showSafetyCenter by remember { mutableStateOf(false) }
    var showAdminDashboard by remember { mutableStateOf(false) }

    // Reactive Data Queries
    val currentUserId = currentUser?.id ?: "guest_user"
    val currentAgeGroup = currentUser?.ageGroup ?: AgeGroup.ADULT

    val discoverUsers by discoveryRepo.getDiscoverableUsers(currentUserId, currentAgeGroup)
        .collectAsState(initial = emptyList())

    val matches by socialRepo.getMatches(currentUserId)
        .collectAsState(initial = emptyList())

    val conversations by chatRepo.getConversationsForUser(currentUserId)
        .collectAsState(initial = emptyList())

    val adminReports by safetyRepo.getAllReportsForAdmin()
        .collectAsState(initial = emptyList())

    val adminUsers by safetyRepo.getAllUsersForAdmin()
        .collectAsState(initial = emptyList())

    val adminAuditLogs by safetyRepo.getAllAuditLogs()
        .collectAsState(initial = emptyList())

    val activeMessages by produceState<List<com.example.data.model.MessageEntity>>(
        initialValue = emptyList(),
        activeChatConversation
    ) {
        val convId = activeChatConversation?.conversation?.id
        if (convId != null) {
            chatRepo.getMessages(convId).collect { value = it }
        } else {
            value = emptyList()
        }
    }

    // Handlers
    fun handleProtectedAction(action: () -> Unit) {
        if (currentUser == null || currentUser.isGuest) {
            showGuestRequiredModal = true
        } else {
            action()
        }
    }

    if (currentSession == null) {
        // Welcome Screen
        WelcomeScreen(
            onContinueWithGoogle = {
                showAuthDialog = true
            },
            onGoogleCredentialResult = { idToken, email, displayName ->
                coroutineScope.launch {
                    val result = authRepo.loginWithGoogleToken(idToken, email, displayName)
                    result.onSuccess {
                        showAuthDialog = false
                        authError = null
                    }.onFailure {
                        authError = it.message
                        showAuthDialog = true
                    }
                }
            },
            onContinueWithEmail = {
                authError = null
                showAuthDialog = true
            },
            onContinueAsGuest = {
                coroutineScope.launch {
                    authRepo.startGuestSession()
                }
            },
            onOpenAuthDialogWithError = { errorMsg ->
                authError = errorMsg
                showAuthDialog = true
            }
        )

        if (showAuthDialog) {
            AuthDialog(
                onDismiss = {
                    showAuthDialog = false
                    authError = null
                },
                onLoginSubmit = { email, pass ->
                    coroutineScope.launch {
                        val result = authRepo.loginWithEmail(email, pass)
                        result.onSuccess {
                            showAuthDialog = false
                            authError = null
                        }.onFailure {
                            authError = it.message
                        }
                    }
                },
                onRegisterSubmit = { name, email, pass, dob ->
                    coroutineScope.launch {
                        val result = authRepo.registerEmailUser(email, pass, name, dob)
                        result.onSuccess {
                            showAuthDialog = false
                            authError = null
                        }.onFailure {
                            authError = it.message
                        }
                    }
                },
                onForgotPassword = { email ->
                    coroutineScope.launch {
                        authRepo.sendPasswordReset(email)
                    }
                },
                errorMessage = authError
            )
        }
    } else {
        // Main App Layout
        Scaffold(
            bottomBar = {
                if (selectedUserForDetail == null && activeChatConversation == null && !showSafetyCenter && !showAdminDashboard) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        listOf(Screen.Home, Screen.Discover, Screen.Matches, Screen.Messages, Screen.Profile).forEach { tab ->
                            NavigationBarItem(
                                selected = (activeTab == tab),
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (activeTab == tab) tab.selectedIcon else tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Screen Navigation Stack
                when {
                    showSafetyCenter -> {
                        SafetyCenterScreen(onBackClick = { showSafetyCenter = false })
                    }
                    showAdminDashboard -> {
                        AdminDashboardScreen(
                            reports = adminReports,
                            users = adminUsers,
                            auditLogs = adminAuditLogs,
                            onBackClick = { showAdminDashboard = false },
                            onWarnUser = { userId, reason, repId ->
                                coroutineScope.launch {
                                    safetyRepo.updateUserStatusByAdmin(
                                        currentUserId,
                                        userId,
                                        com.example.data.model.AccountStatus.WARNED,
                                        reason,
                                        repId
                                    )
                                }
                            },
                            onSuspendUser = { userId, reason, repId ->
                                coroutineScope.launch {
                                    safetyRepo.updateUserStatusByAdmin(
                                        currentUserId,
                                        userId,
                                        com.example.data.model.AccountStatus.SUSPENDED,
                                        reason,
                                        repId
                                    )
                                }
                            },
                            onBanUser = { userId, reason, repId ->
                                coroutineScope.launch {
                                    safetyRepo.updateUserStatusByAdmin(
                                        currentUserId,
                                        userId,
                                        com.example.data.model.AccountStatus.BANNED,
                                        reason,
                                        repId
                                    )
                                }
                            },
                            onRestoreUser = { userId ->
                                coroutineScope.launch {
                                    safetyRepo.updateUserStatusByAdmin(
                                        currentUserId,
                                        userId,
                                        com.example.data.model.AccountStatus.ACTIVE,
                                        "Restored by admin"
                                    )
                                }
                            },
                            onDismissReport = { repId ->
                                coroutineScope.launch {
                                    safetyRepo.dismissReport(repId, currentUserId)
                                }
                            }
                        )
                    }
                    selectedUserForDetail != null -> {
                        val targetUser = selectedUserForDetail!!
                        ProfileDetailScreen(
                            user = targetUser,
                            onBackClick = { selectedUserForDetail = null },
                            onLikeClick = {
                                handleProtectedAction {
                                    coroutineScope.launch {
                                        val isMatch = socialRepo.likeUser(currentUserId, targetUser.id)
                                        if (isMatch) {
                                            matchCelebrationUser = targetUser
                                        }
                                    }
                                }
                            },
                            onMessageClick = {
                                handleProtectedAction {
                                    coroutineScope.launch {
                                        val convId = chatRepo.sendMessageToUser(
                                            currentUserId,
                                            targetUser.id,
                                            "Hello! Loved your profile."
                                        )
                                        selectedUserForDetail = null
                                        activeTab = Screen.Messages
                                    }
                                }
                            },
                            onReportClick = {
                                showReportSheetForUser = targetUser
                            },
                            onBlockClick = {
                                handleProtectedAction {
                                    coroutineScope.launch {
                                        safetyRepo.blockUser(currentUserId, targetUser.id)
                                        selectedUserForDetail = null
                                    }
                                }
                            }
                        )
                    }
                    activeChatConversation != null -> {
                        val conv = activeChatConversation!!
                        DirectMessageChatScreen(
                            currentUserId = currentUserId,
                            otherUser = conv.otherUser,
                            messages = activeMessages,
                            onSendMessage = { text ->
                                coroutineScope.launch {
                                    chatRepo.sendMessageToUser(currentUserId, conv.otherUser.id, text)
                                }
                            },
                            onDeleteMessage = { msgId ->
                                coroutineScope.launch {
                                    chatRepo.softDeleteMessage(msgId)
                                }
                            },
                            onBackClick = { activeChatConversation = null },
                            onReportUser = { showReportSheetForUser = conv.otherUser },
                            onBlockUser = {
                                coroutineScope.launch {
                                    safetyRepo.blockUser(currentUserId, conv.otherUser.id)
                                    activeChatConversation = null
                                }
                            }
                        )
                    }
                    else -> {
                        // Main Bottom Tabs
                        when (activeTab) {
                            Screen.Home -> HomeDashboardScreen(
                                currentUser = currentUser,
                                recommendedPeople = discoverUsers.take(4),
                                nearbyPeople = discoverUsers.filter { it.locationArea.contains("Kathmandu", ignoreCase = true) },
                                newTodayPeople = discoverUsers.takeLast(3),
                                matches = matches,
                                onSelectUser = { selectedUserForDetail = it },
                                onNavigateToDiscover = { activeTab = Screen.Discover },
                                onNavigateToChat = { activeTab = Screen.Messages },
                                onNavigateToSafetyCenter = { showSafetyCenter = true }
                            )
                            Screen.Discover -> DiscoverScreen(
                                discoverUsers = discoverUsers,
                                onSelectUser = { selectedUserForDetail = it },
                                onLikeUser = { target ->
                                    handleProtectedAction {
                                        coroutineScope.launch {
                                            val isMatch = socialRepo.likeUser(currentUserId, target.id)
                                            if (isMatch) {
                                                matchCelebrationUser = target
                                            }
                                        }
                                    }
                                },
                                onSaveUser = { target ->
                                    // Saved preference
                                },
                                onPassUser = { target ->
                                    // Pass
                                }
                            )
                            Screen.Matches -> ConversationsListScreen(
                                conversations = conversations,
                                onSelectConversation = { activeChatConversation = it },
                                onNavigateToDiscover = { activeTab = Screen.Discover }
                            )
                            Screen.Messages -> ConversationsListScreen(
                                conversations = conversations,
                                onSelectConversation = { activeChatConversation = it },
                                onNavigateToDiscover = { activeTab = Screen.Discover }
                            )
                            Screen.Profile -> MyProfileAndSettingsScreen(
                                currentUser = currentUser,
                                currentTheme = currentTheme,
                                isDarkMode = isDarkMode,
                                onSelectTheme = onSelectTheme,
                                onToggleDarkMode = onToggleDarkMode,
                                onSaveProfile = { name, bio, location ->
                                    coroutineScope.launch {
                                        val updated = currentUser?.copy(
                                            displayName = name,
                                            bio = bio,
                                            locationArea = location
                                        )
                                        if (updated != null) {
                                            authRepo.db.userDao().updateUser(updated)
                                        }
                                    }
                                },
                                onNavigateToSafetyCenter = { showSafetyCenter = true },
                                onNavigateToAdminDashboard = { showAdminDashboard = true },
                                onLogout = {
                                    coroutineScope.launch {
                                        authRepo.logout()
                                    }
                                },
                                onDeleteAccount = {
                                    coroutineScope.launch {
                                        if (currentUser != null) {
                                            authRepo.deleteAccount(currentUser.id)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Modals & Overlays
                if (showGuestRequiredModal) {
                    GuestActionModal(
                        onDismiss = { showGuestRequiredModal = false },
                        onLoginClick = {
                            showGuestRequiredModal = false
                            coroutineScope.launch {
                                authRepo.logout()
                            }
                        }
                    )
                }

                if (matchCelebrationUser != null) {
                    MatchCelebrationDialog(
                        matchedUser = matchCelebrationUser!!,
                        onSayHello = {
                            val target = matchCelebrationUser!!
                            matchCelebrationUser = null
                            coroutineScope.launch {
                                chatRepo.sendMessageToUser(currentUserId, target.id, "Hi! So glad we matched.")
                                activeTab = Screen.Messages
                            }
                        },
                        onKeepExploring = {
                            matchCelebrationUser = null
                        }
                    )
                }

                if (showReportSheetForUser != null) {
                    val reportedUser = showReportSheetForUser!!
                    ReportSheet(
                        reportedUserName = reportedUser.displayName,
                        onDismiss = { showReportSheetForUser = null },
                        onSubmitReport = { reason, desc ->
                            coroutineScope.launch {
                                safetyRepo.reportUser(currentUserId, reportedUser.id, null, reason, desc)
                            }
                        }
                    )
                }
            }
        }
    }
}

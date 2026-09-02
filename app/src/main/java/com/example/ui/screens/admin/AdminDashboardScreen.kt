package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.UserEntity
import com.example.data.repository.ReportUiItem
import com.example.ui.components.GlassCard
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    reports: List<ReportUiItem>,
    users: List<UserEntity>,
    auditLogs: List<AuditLogEntity>,
    onBackClick: () -> Unit,
    onWarnUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onSuspendUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onBanUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onRestoreUser: (userId: String) -> Unit,
    onDismissReport: (reportId: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Reports, 1: Users, 2: Audit Logs
    val tabs = listOf("Reports (${reports.size})", "User Management (${users.size})", "Audit Logs")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Safety Moderation",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ReportsTabContent(
                    reports = reports,
                    onWarnUser = onWarnUser,
                    onSuspendUser = onSuspendUser,
                    onBanUser = onBanUser,
                    onDismissReport = onDismissReport
                )
                1 -> UsersTabContent(
                    users = users,
                    onWarnUser = onWarnUser,
                    onSuspendUser = onSuspendUser,
                    onBanUser = onBanUser,
                    onRestoreUser = onRestoreUser
                )
                2 -> AuditLogsTabContent(auditLogs = auditLogs)
            }
        }
    }
}

@Composable
fun ReportsTabContent(
    reports: List<ReportUiItem>,
    onWarnUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onSuspendUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onBanUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onDismissReport: (reportId: String) -> Unit
) {
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No pending reports",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "The You & i community is running safely.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(reports) { item ->
                GlassCard(shape = RoundedCornerShape(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = item.report.status,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        val timeStr = remember(item.report.createdAt) {
                            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            sdf.format(Date(item.report.createdAt))
                        }
                        Text(text = timeStr, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Reported: ${item.reportedUserName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Reporter: ${item.reporterName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Reason: ${item.report.reason}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error
                    )
                    if (item.report.description.isNotBlank()) {
                        Text(
                            text = "\"${item.report.description}\"",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onWarnUser(item.report.reportedUserId, item.report.reason, item.report.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Warn", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onBanUser(item.report.reportedUserId, item.report.reason, item.report.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Ban User", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onDismissReport(item.report.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsersTabContent(
    users: List<UserEntity>,
    onWarnUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onSuspendUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onBanUser: (userId: String, reason: String, reportId: String?) -> Unit,
    onRestoreUser: (userId: String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users) { user ->
            GlassCard(shape = RoundedCornerShape(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${user.displayName} (${user.ageGroup.name})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${user.email} • Age ${user.age} • ${user.locationArea}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = when (user.accountStatus) {
                            AccountStatus.ACTIVE -> Color(0xFFDCFCE7)
                            AccountStatus.WARNED -> Color(0xFFFEF9C3)
                            AccountStatus.SUSPENDED -> Color(0xFFFFEDD5)
                            AccountStatus.BANNED -> Color(0xFFFEE2E2)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = user.accountStatus.name,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (user.accountStatus != AccountStatus.ACTIVE) {
                        Button(
                            onClick = { onRestoreUser(user.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore Active")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onWarnUser(user.id, "Admin warning issued", null) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Issue Warn")
                        }
                        Button(
                            onClick = { onBanUser(user.id, "Violated safety policies", null) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ban User")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogsTabContent(auditLogs: List<AuditLogEntity>) {
    if (auditLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No administrative audit logs recorded yet.")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(auditLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Action: ${log.action}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            val timeStr = remember(log.timestamp) {
                                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                                sdf.format(Date(log.timestamp))
                            }
                            Text(text = timeStr, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            text = "Reason: ${log.reason}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

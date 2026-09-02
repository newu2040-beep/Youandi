package com.example.ui.screens.discover

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserEntity
import com.example.ui.components.AppFilterChip
import com.example.ui.components.InterestChip
import com.example.ui.components.VerificationBadge

@Composable
fun DiscoverScreen(
    discoverUsers: List<UserEntity>,
    onSelectUser: (UserEntity) -> Unit,
    onLikeUser: (UserEntity) -> Unit,
    onSaveUser: (UserEntity) -> Unit,
    onPassUser: (UserEntity) -> Unit
) {
    var activeFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Nearby", "New Today", "Verified", "Music", "Fitness", "Books")

    val filteredList = remember(activeFilter, discoverUsers) {
        when (activeFilter) {
            "Verified" -> discoverUsers.filter { it.verificationStatus != com.example.data.model.VerificationStatus.UNVERIFIED }
            "Nearby" -> discoverUsers.filter { it.locationArea.contains("Kathmandu", ignoreCase = true) }
            "Music" -> discoverUsers.filter { it.bio.contains("music", ignoreCase = true) || it.bio.contains("acoustic", ignoreCase = true) }
            else -> discoverUsers
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Discover",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "People • Places • Possibilities",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(filters) { filter ->
                    AppFilterChip(
                        text = filter,
                        selected = (activeFilter == filter),
                        onClick = { activeFilter = filter }
                    )
                }
            }

            // Cards Feed (Vertical Editorial Browsing)
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your people are still out there.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Try widening your filter preferences.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.id }) { user ->
                        DiscoverProfileCard(
                            user = user,
                            onClick = { onSelectUser(user) },
                            onLike = { onLikeUser(user) },
                            onPass = { onPassUser(user) },
                            onSave = { onSaveUser(user) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverProfileCard(
    user: UserEntity,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onPass: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(440.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Profile Image
            if (user.avatarUrl.contains("img_profile_sophie")) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_sophie_1788365274178),
                    contentDescription = user.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (user.avatarUrl.contains("img_profile_aarav")) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_aarav_1788365288136),
                    contentDescription = user.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.take(1),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Gradient Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.85f)
                            ),
                            startY = 200f
                        )
                    )
            )

            // Top Status Badge
            Surface(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ADE80))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Recently",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            // Bottom Profile Info & Actions
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${user.displayName}, ${user.age}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    VerificationBadge()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.locationArea,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row (♡ Like, ✕ Pass, ☆ Save)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Pass
                    IconButton(
                        onClick = onPass,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Pass",
                            tint = Color.White
                        )
                    }

                    // Like (Primary)
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Save
                    IconButton(
                        onClick = onSave,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

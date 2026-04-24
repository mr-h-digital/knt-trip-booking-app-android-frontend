package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.UserDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel

@Composable
fun AdminUsersScreen(
    onBack        : () -> Unit,
    onCreateDriver: () -> Unit,
    onEditUser    : (String) -> Unit,
    viewModel     : AdminViewModel = viewModel(),
) {
    val c    = LocalAppColors.current
    val tabs = listOf("All", "Commuters", "Drivers")
    var selectedTab by remember { mutableIntStateOf(0) }
    val usersState by viewModel.users.collectAsState()

    val roleFilter = when (selectedTab) {
        1 -> "COMMUTER"
        2 -> "DRIVER"
        else -> null
    }

    LaunchedEffect(selectedTab) {
        viewModel.loadUsers(role = roleFilter)
    }

    KntScaffold(
        title   = "Users",
        onBack  = onBack,
        actions = {
            IconButton(onClick = onCreateDriver) {
                Icon(Icons.Rounded.PersonAdd, "Add Driver", tint = KntWhite)
            }
        },
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_8, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Manage your team",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = c.surface1,
                contentColor     = c.blue,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color    = c.yellow,
                    )
                },
                divider = {},
            ) {
                tabs.forEachIndexed { i, label ->
                    Tab(
                        selected = i == selectedTab,
                        onClick  = { selectedTab = i },
                        text     = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        selectedContentColor   = c.yellow,
                        unselectedContentColor = c.textMuted,
                    )
                }
            }

            when (val state = usersState) {
                is ApiResult.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is ApiResult.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = c.textDim, modifier = Modifier.size(44.dp))
                        Text(state.message, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        KntPrimaryButton(text = "Retry", onClick = { viewModel.loadUsers(role = roleFilter) },
                            modifier = Modifier.fillMaxWidth(0.5f))
                    }
                }
                is ApiResult.Success -> {
                    val users = state.data
                    if (users.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.PeopleOutline, null, tint = c.textDim, modifier = Modifier.size(52.dp))
                                Text("No users found", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            users.forEach { user ->
                                AdminUserDtoCard(user = user, onClick = { onEditUser(user.id) })
                            }
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserDtoCard(user: UserDto, onClick: () -> Unit) {
    val c = LocalAppColors.current
    val (tint, roleLabel) = when (user.role.uppercase()) {
        "DRIVER" -> KntYellow to "Driver"
        "ADMIN"  -> KntOrange to "Admin"
        else     -> KntBlue   to "Commuter"
    }
    val initials = user.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")

    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(tint.copy(0.7f), tint.copy(0.4f)))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initials,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.name,  style = MaterialTheme.typography.titleSmall, color = c.textBright)
                Text(user.email, style = MaterialTheme.typography.bodySmall,  color = c.textMuted)
                Text(user.phone, style = MaterialTheme.typography.bodySmall,  color = c.textDim)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = tint.copy(alpha = 0.12f),
                ) {
                    Text(
                        roleLabel,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = tint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = c.textDim, modifier = Modifier.size(18.dp))
            }
        }
    }
}

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
import com.kntransport.app.data.SampleData
import com.kntransport.app.data.User
import com.kntransport.app.data.UserRole
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

val adminSampleUsers = listOf(
    User("u1",  "Tayla Hendricks",  "tayla@email.com",            "072 345 6789",   UserRole.COMMUTER),
    User("u2",  "Nadia Adams",      "nadia@email.com",            "083 456 7890",   UserRole.COMMUTER),
    User("u3",  "Yusuf Daniels",    "yusuf@email.com",            "071 567 8901",   UserRole.COMMUTER),
    User("u4",  "Fatima Jacobs",    "fatima@email.com",           "082 678 9012",   UserRole.COMMUTER),
    User("d1",  "Taswill Heynes",   "taswill@ktransport.co.za",  "+27787784182",   UserRole.DRIVER),
    User("d2",  "Bradley September","bradley@ktransport.co.za",  "+27821234567",   UserRole.DRIVER),
    User("a1",  "Admin User",       "admin@ktransport.co.za",    "+27211234567",   UserRole.ADMIN),
)

@Composable
fun AdminUsersScreen(
    onBack        : () -> Unit,
    onCreateDriver: () -> Unit,
    onEditUser    : (String) -> Unit,
) {
    val c    = LocalAppColors.current
    val tabs = listOf("All", "Commuters", "Drivers")
    var selectedTab by remember { mutableIntStateOf(0) }

    val filtered = when (selectedTab) {
        1 -> adminSampleUsers.filter { it.role == UserRole.COMMUTER }
        2 -> adminSampleUsers.filter { it.role == UserRole.DRIVER }
        else -> adminSampleUsers
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

            if (filtered.isEmpty()) {
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
                    filtered.forEach { user ->
                        AdminUserCard(user = user, onClick = { onEditUser(user.id) })
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminUserCard(user: User, onClick: () -> Unit) {
    val c      = LocalAppColors.current
    val (tint, roleLabel) = when (user.role) {
        UserRole.DRIVER  -> KntYellow to "Driver"
        UserRole.ADMIN   -> KntOrange to "Admin"
        else             -> KntBlue   to "Commuter"
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


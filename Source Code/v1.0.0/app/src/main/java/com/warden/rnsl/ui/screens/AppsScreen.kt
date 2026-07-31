package com.warden.rnsl.ui.screens

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.data.model.AppInfo
import com.warden.rnsl.ui.AppFilter
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.components.*
import com.warden.rnsl.ui.navigation.Screen
import com.warden.rnsl.ui.theme.BadRed
import com.warden.rnsl.ui.theme.GoodGreen
import com.warden.rnsl.ui.theme.WardenBlue

@Composable
fun AppsScreen(viewModel: WardenViewModel, navController: NavController) {
  val filteredApps by viewModel.filteredApps.collectAsState()
  val appFilter by viewModel.appFilter.collectAsState()
  val isLoading by viewModel.isLoadingApps.collectAsState()
  var searchQuery by remember {
    mutableStateOf("")
  }

  val displayed = if (searchQuery.isBlank()) filteredApps
  else filteredApps.filter {
    it.appName.contains(searchQuery, ignoreCase = true) ||
    it.packageName.contains(searchQuery, ignoreCase = true)
  }

  Column(modifier = Modifier.fillMaxSize()) {
    WardenTopBar(title = "Apps")

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = {
        searchQuery = it
      },
      modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
      placeholder = {
        Text("Search apps...")
      },
      leadingIcon = {
        Icon(Icons.Filled.Search, contentDescription = null)
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = WardenBlue,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
      )
    )

    // Filter chips
    FilterChips(
      selected = appFilter.name,
      options = listOf("ALL", "USER", "SYSTEM"),
      onSelect = {
        viewModel.setFilter(AppFilter.valueOf(it))
      }
    )

    if (isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = WardenBlue)
      }
    } else {
      LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
        items(displayed) {
          app ->
          AppCard(app = app, onClick = {
            navController.navigate(Screen.AppDetail.createRoute(app.packageName))
          })
        }
        item {
          Spacer(modifier = Modifier.height(80.dp))
        }
      }
    }
  }
}

@Composable
fun AppCard(app: AppInfo, onClick: () -> Unit) {
  Card(
    modifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 4.dp)
    .clickable {
      onClick()
    },
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(10.dp),
        color = WardenBlue.copy(alpha = 0.1f)
      ) {
        if (app.icon != null) {
          Image(
            bitmap = app.icon.toBitmap(width = 44, height = 44).asImageBitmap(),
            contentDescription = app.appName,
            modifier = Modifier.size(44.dp)
          )
        } else {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
              text = app.appName.take(1).uppercase(),
              fontWeight = FontWeight.Bold,
              color = WardenBlue,
              fontSize = 20.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = app.appName,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          if (app.isSystemApp) {
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "SYSTEM",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            }
          }
        }
        Text(
          text = app.packageName,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (app.permissions.isNotEmpty()) {
          Text(
            text = "🔑 ${app.permissions.size} permissions",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Icon(
        imageVector = Icons.Filled.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
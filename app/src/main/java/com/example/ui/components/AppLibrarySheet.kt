package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.ui.theme.GlassWhiteLow
import com.example.ui.theme.GlassWhiteMedium

@Composable
fun AppLibrarySheet(
  apps: List<AppInfo>,
  recentApps: List<AppInfo> = emptyList(),
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onAppClick: (AppInfo) -> Unit,
  modifier: Modifier = Modifier
) {
  val filtered = remember(apps, searchQuery) {
    val q = searchQuery.trim()
    if (q.isBlank()) apps else apps
      .filter { it.label.contains(q, true) || it.packageName.contains(q, true) }
      .sortedWith(compareByDescending<AppInfo> { it.label.startsWith(q, true) }.thenBy { it.label.lowercase() })
  }
  var selectedLetter by remember { mutableStateOf<Char?>(null) }
  var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }
  val letters = remember(apps) { apps.mapNotNull { it.label.firstOrNull()?.uppercaseChar() }.distinct().sorted() }

  Column(modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("app_library_screen")) {
    GlassSearchBar(
      query = searchQuery,
      onQueryChange = onSearchQueryChange,
      modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    )

    if (searchQuery.isNotBlank()) {
      Text("${filtered.size} results", color = Color.White.copy(.55f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
      ) {
        if (filtered.isEmpty()) item { EmptySearchState(searchQuery) }
        items(filtered, key = { it.packageName + it.activityName }) { app -> SearchResult(app, onAppClick) }
      }
    } else {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
          Row(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
              Text("App Library", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
              Text("${apps.size} apps", color = Color.White.copy(.55f), fontSize = 12.sp)
            }
            GlassPill(text = "A–Z")
          }
        }
        if (recentApps.isNotEmpty()) {
          item {
            SectionTitle("Recently Used")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 18.dp)) {
              recentApps.take(6).forEach { app ->
                AppIconItem(app = app, iconSize = 54.dp, showLabel = true, onClick = { onAppClick(app) })
              }
            }
          }
        }
        item {
          SectionTitle("Suggestions")
          val suggested = apps.filter { recentApps.none { r -> r.packageName == it.packageName } }.sortedBy { it.label.lowercase() }.take(8)
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 18.dp)) {
            suggested.take(6).forEach { app -> AppIconItem(app = app, iconSize = 54.dp, showLabel = true, onClick = { onAppClick(app) }) }
          }
        }
        item {
          if (selectedCategory != null) {
            val categoryApps = apps.filter { it.category == selectedCategory }
            Row(
              Modifier.fillMaxWidth().padding(bottom = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              SectionTitle(selectedCategory!!.title)
              GlassPill(text = "BACK", onClick = { selectedCategory = null })
            }
            LazyVerticalGrid(
              columns = GridCells.Fixed(4),
              modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 900.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(categoryApps, key = { it.packageName + it.activityName }) { app ->
                AppIconItem(app = app, iconSize = 54.dp, showLabel = true, onClick = { onAppClick(app) })
              }
            }
          } else {
            SectionTitle("Categories")
            Row(modifier = Modifier.fillMaxWidth()) {
              LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).heightIn(min = 300.dp, max = 1000.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                items(AppCategory.values().filter { c -> apps.any { it.category == c } }) { category ->
                  CategoryClusterCard(category, apps.filter { it.category == category }, onAppClick, onCategoryClick = { selectedCategory = it })
                }
              }
            if (letters.isNotEmpty()) {
              Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(22.dp).padding(top = 4.dp)) {
                letters.forEach { letter ->
                  Text(letter.toString(), color = if (selectedLetter == letter) Color.White else Color.White.copy(.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { selectedLetter = letter; onSearchQueryChange(letter.toString()) }.padding(vertical = 2.dp))
                }
                if (selectedLetter != null) Text("×", color = Color.White.copy(.7f), fontSize = 12.sp, modifier = Modifier.clickable { selectedLetter = null; onSearchQueryChange("") })
              }
            }
          }
        }
      }
    }
  }
}

@Composable private fun SectionTitle(title: String) {
  Text(title, color = Color.White.copy(.9f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
}

@Composable private fun GlassPill(text: String, onClick: (() -> Unit)? = null) {
  Box(Modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(.12f)).clickable(enabled = onClick != null) { onClick?.invoke() }.padding(horizontal = 10.dp, vertical = 6.dp)) {
    Text(text, color = Color.White.copy(.75f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
  }
}

@Composable private fun SearchResult(app: AppInfo, onAppClick: (AppInfo) -> Unit) {
  GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassWhiteLow, shape = RoundedCornerShape(18.dp), onClick = { onAppClick(app) }) {
    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      AppIconItem(app = app, iconSize = 48.dp, showLabel = false, onClick = { onAppClick(app) })
      Spacer(Modifier.width(16.dp))
      Column { Text(app.label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold); Text(app.category.title, color = Color.White.copy(.6f), fontSize = 12.sp) }
    }
  }
}

@Composable private fun EmptySearchState(query: String) {
  Box(Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
    Text("No apps found for \"$query\"", color = Color.White.copy(.7f), fontSize = 15.sp)
  }
}

@Composable
fun CategoryClusterCard(
  category: AppCategory,
  apps: List<AppInfo>,
  onAppClick: (AppInfo) -> Unit,
  modifier: Modifier = Modifier,
  onCategoryClick: (AppCategory) -> Unit = {}
) {
  GlassCard(
    modifier = modifier.height(178.dp).fillMaxWidth(),
    backgroundColor = GlassWhiteMedium,
    shape = RoundedCornerShape(26.dp),
    onClick = { onCategoryClick(category) }
  ) {
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        apps.take(2).forEach { AppIconItem(app = it, iconSize = 46.dp, showLabel = false, onClick = { onAppClick(it) }) }
      }
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        apps.drop(2).take(2).forEach { AppIconItem(app = it, iconSize = 46.dp, showLabel = false, onClick = { onAppClick(it) }) }
      }
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(category.title, color = Color.White.copy(.9f), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text("${apps.size}", color = Color.White.copy(.5f), fontSize = 10.sp)
      }
    }
  }
}

@Composable
fun GlassSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
  Row(modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(.65f), modifier = Modifier.size(20.dp))
    Spacer(Modifier.width(8.dp))
    androidx.compose.foundation.text.BasicTextField(value = query, onValueChange = onQueryChange, singleLine = true,
      textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp), cursorBrush = SolidColor(Color.White),
      modifier = Modifier.weight(1f), decorationBox = { inner -> if (query.isEmpty()) Text("Search Apps", color = Color.White.copy(.5f), fontSize = 15.sp); inner() })
    if (query.isNotEmpty()) Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(.65f), modifier = Modifier.size(18.dp).clickable { onQueryChange("") })
  }
}

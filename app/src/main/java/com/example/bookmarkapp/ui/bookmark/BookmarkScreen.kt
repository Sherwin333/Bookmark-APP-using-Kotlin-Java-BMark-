package com.example.bookmarkapp.ui.bookmark

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookmarkapp.domain.model.Bookmark
import com.example.bookmarkapp.presentation.BookmarkViewModel
import kotlinx.coroutines.launch

// UI-only categories (no DB change required)
private val categories = listOf("General", "Work", "Study", "Fun", "Read Later")
enum class SortType { DATE, TITLE, EMOJI }

// Emoji priority order: lower = higher priority
private val emojiPriority = mapOf(
    "🔥" to 0, "🚀" to 1, "⭐" to 2, "📌" to 3, "❤️" to 4, "💤" to 5
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookmarkScreen(viewModel: BookmarkViewModel) {
    val all by viewModel.bookmarks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }

    // New‑bookmark category (compact dropdown in form)
    var selectedCategory by remember { mutableStateOf("General") }

    // Global viewing state (top‑bar menu)
    var activeFilter by remember { mutableStateOf("All") }
    var sortType by remember { mutableStateOf(SortType.DATE) }

    val clipboard = LocalClipboardManager.current

    // Fun priority emojis (UI only)
    val emojiChoices = listOf("🔥", "🚀", "⭐", "📌", "❤️", "💤")
    val emojiById: SnapshotStateMap<Long, String> = remember { mutableStateMapOf() }

    // Category state per-id (UI only)
    val categoryById: SnapshotStateMap<Long, String> = remember { mutableStateMapOf() }

    // Track IDs to detect freshly inserted rows
    var lastKnownIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var lastAddedCategory by remember { mutableStateOf<String?>(null) }

    // Sync maps with DB list and assign category to newly added items
    LaunchedEffect(all) {
        val currentIds = all.map { it.id }.toSet()
        val newIds = currentIds - lastKnownIds

        // Defaults + emoji setup
        all.forEach { b ->
            if (!emojiById.containsKey(b.id)) emojiById[b.id] = "⭐"
            if (!categoryById.containsKey(b.id)) categoryById[b.id] = "General"
        }

        // Assign the remembered category to any newly appeared items
        if (newIds.isNotEmpty() && lastAddedCategory != null) {
            newIds.forEach { id ->
                categoryById[id] = lastAddedCategory!!
            }
            lastAddedCategory = null
        }

        // cleanup removed items
        emojiById.keys.retainAll(currentIds)
        categoryById.keys.retainAll(currentIds)

        lastKnownIds = currentIds
    }

    // ----- Filtering (reacts to category changes) -----
    val filtered = remember(all, query, activeFilter, categoryById.toMap()) {
        all.filter { b ->
            val cat = categoryById[b.id] ?: "General"
            (query.isBlank() || b.title.contains(query, ignoreCase = true) || b.url.contains(query, ignoreCase = true)) &&
                    (activeFilter == "All" || cat == activeFilter)
        }
    }

    // ----- Sorting (emoji uses priority order) -----
    val items = remember(filtered, sortType, emojiById.toMap()) {
        when (sortType) {
            SortType.DATE -> filtered.sortedBy { it.id } // assuming id increases with time
            SortType.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortType.EMOJI -> filtered.sortedWith(compareBy(
                { emojiPriority[emojiById[it.id] ?: "⭐"] ?: Int.MAX_VALUE },
                { it.title.lowercase() } // tiebreaker
            ))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF000000), Color(0xFF121212), Color(0xFF1C1C1C))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            // compact header with a single Filter menu
            topBar = {
                var menuOpen by remember { mutableStateOf(false) }

                CenterAlignedTopAppBar(
                    title = { Text("Bookmarks", color = Color.White) },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter & Sort", tint = Color.White)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            // Sort header
                            DropdownMenuItem(
                                text = { Text("Sort by", style = MaterialTheme.typography.labelMedium) },
                                onClick = {},
                                enabled = false
                            )
                            DropdownMenuItem(text = { Text("Date") }, onClick = { sortType = SortType.DATE; menuOpen = false })
                            DropdownMenuItem(text = { Text("Title") }, onClick = { sortType = SortType.TITLE; menuOpen = false })
                            DropdownMenuItem(text = { Text("Emoji (priority)") }, onClick = { sortType = SortType.EMOJI; menuOpen = false })
                            Divider()
                            // Filter header
                            DropdownMenuItem(
                                text = { Text("Category filter", style = MaterialTheme.typography.labelMedium) },
                                onClick = {},
                                enabled = false
                            )
                            (listOf("All") + categories).forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = { activeFilter = cat; menuOpen = false }
                                )
                            }
                        }
                    }
                )
            },
            // watermark in bottomBar so it doesn't overlap content
            bottomBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp, top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Built ❤️ by Sherwin D'Souza",
                        color = Color(0xFF8E8E8E),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        // remember the category for the item that will be inserted next
                        lastAddedCategory = selectedCategory.takeIf { it.isNotBlank() } ?: "General"

                        viewModel.addBookmark(title, url)  // original signature
                        title = ""
                        url = ""
                        selectedCategory = "General"
                    },
                    text = { Text("Add") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                    expanded = true,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.Black
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search bookmarks…", color = Color(0xFFBDBDBD)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                )

                // Compact input card with category dropdown
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E)),
                    elevation = CardDefaults.elevatedCardElevation(6.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("URL") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category dropdown (Exposed)
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                categories.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedCategory = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // List
                if (items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (query.isBlank()) "No bookmarks yet.\nAdd your first one!" else "No results for “$query”.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFBDBDBD),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 96.dp) // room for FAB + bottomBar
                    ) {
                        items(items, key = { it.id }) { item ->
                            BookmarkRow(
                                item = item,
                                emoji = emojiById[item.id] ?: "⭐",
                                category = categoryById[item.id] ?: "General",
                                onChangeEmoji = { new -> emojiById[item.id] = new },
                                onChangeCategory = { new -> categoryById[item.id] = new },
                                onCopy = {
                                    scope.launch {
                                        clipboard.setText(AnnotatedString(item.url))
                                        snackbarHostState.showSnackbar("Copied link")
                                    }
                                },
                                onDelete = { viewModel.deleteBookmark(item) },
                                onShare = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, item.url)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share via"))
                                },
                                emojiChoices = emojiChoices
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkRow(
    item: Bookmark,
    emoji: String,
    category: String,
    onChangeEmoji: (String) -> Unit,
    onChangeCategory: (String) -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    emojiChoices: List<String>
) {
    val uriHandler = LocalUriHandler.current
    var menuOpen by remember { mutableStateOf(false) }
    var emojiMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }

    val host = remember(item.url) {
        val parsed = runCatching { Uri.parse(item.url) }.getOrNull()
        parsed?.host ?: item.url.removePrefix("https://").removePrefix("http://").substringBefore("/")
    }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF2A2A2A)),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji badge (tap to change)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3A))
                    .clickable { emojiMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, style = MaterialTheme.typography.titleMedium)
                DropdownMenu(expanded = emojiMenu, onDismissRequest = { emojiMenu = false }) {
                    emojiChoices.forEach { e ->
                        DropdownMenuItem(text = { Text(e) }, onClick = { emojiMenu = false; onChangeEmoji(e) })
                    }
                }
            }

            // favicon
            AsyncImage(
                model = "https://www.google.com/s2/favicons?domain=$host&sz=64",
                contentDescription = "favicon",
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            // Title + URL + Category chip
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    text = item.url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { uriHandler.openUri(item.url) }
                )

                AssistChip(
                    onClick = { categoryMenu = true },
                    label = { Text(category) },
                    leadingIcon = null,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF3A3A3A),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
                DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { categoryMenu = false; onChangeCategory(cat) })
                    }
                }
            }

            // Row actions
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = Color.White)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Open") }, onClick = {
                        uriHandler.openUri(item.url); menuOpen = false
                    })
                    DropdownMenuItem(text = { Text("Copy") }, onClick = { onCopy(); menuOpen = false })
                    DropdownMenuItem(text = { Text("Share") }, onClick = { onShare(); menuOpen = false })
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Filled.Delete, null) },
                        onClick = { onDelete(); menuOpen = false }
                    )
                }
            }
        }
    }
}

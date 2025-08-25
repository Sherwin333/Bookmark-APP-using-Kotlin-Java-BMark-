package com.example.bookmarkapp.presentation

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookmarkapp.domain.model.Bookmark
import com.example.bookmarkapp.domain.usecase.BookmarkUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val useCases: BookmarkUseCases
) : ViewModel() {

    val bookmarks: StateFlow<List<Bookmark>> =
        useCases.getBookmarks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addBookmark(title: String, url: String) {
        val t = title.trim()
        val u = url.trim()
        if (t.isEmpty() || u.isEmpty()) return

        val clean = sanitizeUrl(u) ?: return // if invalid, ignore add (or surface a toast/snackbar from UI)

        viewModelScope.launch {
            useCases.addBookmark(
                Bookmark(
                    title = t,
                    url = clean
                )
            )
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch { useCases.deleteBookmark(bookmark) }
    }

    fun restoreBookmark(b: Bookmark) {
        viewModelScope.launch { useCases.addBookmark(b) }
    }

    // --- helpers ---

    /**
     * Ensures URL has a scheme and is a valid http/https URI.
     * Returns null if invalid.
     */
    private fun sanitizeUrl(raw: String): String? {
        val trimmed = raw.trim()

        // Add https:// if missing scheme
        val withScheme =
            if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
                trimmed
            } else {
                "https://$trimmed"
            }

        // Parse and validate host
        val uri: Uri = runCatching { withScheme.toUri() }.getOrNull() ?: return null
        val host = uri.host ?: return null
        if (host.isBlank()) return null

        // Normalize: lower-case host, keep path/query as-is
        val normalized = Uri.Builder()
            .scheme(uri.scheme ?: "https")
            .authority(host.lowercase())
            .encodedPath(uri.encodedPath)
            .encodedQuery(uri.encodedQuery)
            .fragment(uri.fragment)
            .build()
            .toString()

        return normalized
    }
}

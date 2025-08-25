package com.example.bookmarkapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.example.bookmarkapp.presentation.BookmarkViewModel
import com.example.bookmarkapp.ui.bookmark.BookmarkScreen
import com.example.bookmarkapp.ui.theme.BookmarkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: BookmarkViewModel by viewModels() // <-- injected by Hilt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookmarkTheme(
                darkTheme = false,     // force light for now
                dynamicColor = false   // <- IMPORTANT: use our palette, not wallpaper colors
            ) {
                BookmarkScreen(viewModel = viewModel)
            }
        }
    }
}

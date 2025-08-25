package com.example.bookmarkapp.domain.usecase

data class BookmarkUseCases(
    val addBookmark: AddBookmark,
    val getBookmarks: GetBookmarks,
    val deleteBookmark: DeleteBookmark
)
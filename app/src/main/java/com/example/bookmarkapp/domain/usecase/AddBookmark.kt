package com.example.bookmarkapp.domain.usecase

import com.example.bookmarkapp.domain.model.Bookmark

class AddBookmark(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(bookmark: Bookmark) {
        repository.insertBookmark(bookmark)
    }
}
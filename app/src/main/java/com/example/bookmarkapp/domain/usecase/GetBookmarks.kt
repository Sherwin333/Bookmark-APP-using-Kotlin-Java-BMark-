package com.example.bookmarkapp.domain.usecase

import com.example.bookmarkapp.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

class GetBookmarks(
    private val repository: BookmarkRepository
) {
    operator fun invoke(): Flow<List<Bookmark>> {
        return repository.getBookmarks()
    }
}

package com.example.bookmarkapp.data

import com.example.bookmarkapp.data.mappers.toDomain
import com.example.bookmarkapp.data.mappers.toEntity
import com.example.bookmarkapp.domain.model.Bookmark
import com.example.bookmarkapp.domain.usecase.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookmarkRepositoryImpl(
    private val dao: BookmarkDao
) : BookmarkRepository {

    override fun getBookmarks(): Flow<List<Bookmark>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insertBookmark(bookmark: Bookmark) {
        dao.insert(bookmark.toEntity())
    }

    override suspend fun deleteBookmark(bookmark: Bookmark) {
        dao.delete(bookmark.toEntity())
    }
}

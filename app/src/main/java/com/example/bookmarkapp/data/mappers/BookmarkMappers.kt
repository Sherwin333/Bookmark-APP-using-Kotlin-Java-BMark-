package com.example.bookmarkapp.data.mappers

import com.example.bookmarkapp.data.BookmarkEntity
import com.example.bookmarkapp.domain.model.Bookmark

fun BookmarkEntity.toDomain() = Bookmark(
    id = id,
    title = title,
    url = url
)

fun Bookmark.toEntity() = BookmarkEntity(
    id = id,
    title = title,
    url = url
)

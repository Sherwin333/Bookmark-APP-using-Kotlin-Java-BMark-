package com.example.bookmarkapp.di

import android.app.Application
import androidx.room.Room
import com.example.bookmarkapp.data.BookmarkDao
import com.example.bookmarkapp.data.BookmarkDatabase
import com.example.bookmarkapp.data.BookmarkRepositoryImpl
import com.example.bookmarkapp.domain.usecase.AddBookmark
import com.example.bookmarkapp.domain.usecase.BookmarkRepository
import com.example.bookmarkapp.domain.usecase.BookmarkUseCases
import com.example.bookmarkapp.domain.usecase.DeleteBookmark
import com.example.bookmarkapp.domain.usecase.GetBookmarks
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): BookmarkDatabase =
        Room.databaseBuilder(
            app,
            BookmarkDatabase::class.java,
            "bookmark_db"
        ).build()

    @Provides
    @Singleton
    fun provideDao(db: BookmarkDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    @Singleton
    fun provideRepository(dao: BookmarkDao): BookmarkRepository =
        BookmarkRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideBookmarkUseCases(repository: BookmarkRepository): BookmarkUseCases =
        BookmarkUseCases(
            addBookmark = AddBookmark(repository),
            getBookmarks = GetBookmarks(repository),
            deleteBookmark = DeleteBookmark(repository)
        )
}

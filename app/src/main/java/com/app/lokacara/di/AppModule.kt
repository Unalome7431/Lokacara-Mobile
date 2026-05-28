package com.app.lokacara.di

import android.content.Context
import com.app.lokacara.data.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager {
        return SettingsManager(context)
    }

    @Provides
    @Singleton
    fun provideUserSessionManager(@ApplicationContext context: Context): UserSessionManager {
        return UserSessionManager(context)
    }

    @Provides
    @Singleton
    fun provideBookmarkManager(@ApplicationContext context: Context): BookmarkManager {
        return BookmarkManager(context)
    }

    @Provides
    @Singleton
    fun provideOnboardingManager(@ApplicationContext context: Context): OnboardingManager {
        return OnboardingManager(context)
    }

    @Provides
    @Singleton
    fun provideFileStorageManager(@ApplicationContext context: Context): FileStorageManager {
        return FileStorageManager(context)
    }
}

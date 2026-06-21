package com.app.lokacara.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
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
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache { MemoryCache.Builder(context).maxSizePercent(0.25).build() }
            .diskCache { DiskCache.Builder().directory(context.cacheDir.resolve("image_cache")).maxSizeBytes(250L * 1024 * 1024).build() }
            .crossfade(false)
            .build()
    }

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
    fun provideDraftManager(@ApplicationContext context: Context): DraftManager {
        return DraftManager(context)
    }

    @Provides
    @Singleton
    fun provideFileStorageManager(@ApplicationContext context: Context): FileStorageManager {
        return FileStorageManager(context)
    }
}

package com.example.dynamicwallpaper.Database

import android.content.Context
import androidx.room.Room
import com.example.dynamicwallpaper.Utils.Constants.FAVOURITE_IMAGE_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DatabaseInstance {
        return Room.databaseBuilder(
            context, DatabaseInstance::class.java, FAVOURITE_IMAGE_DATABASE
        ).build()
    }

    @Provides
    fun provideFavouriteImageDao(database: DatabaseInstance): Dao {
        return database.favouriteImageDao()
    }
}
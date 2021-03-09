package com.gerardbradshaw.fairday.room

import android.content.Context
import androidx.room.*

@Database(
  entities = [LocationEntity::class],
  version = 1,
  exportSchema = false
)
abstract class RoomDb : RoomDatabase() {
  abstract fun getLocationDao(): LocationDao

  companion object {
    private const val TAG = "GGG RoomDb"
    private var INSTANCE: RoomDb? = null

    fun getDatabase(context: Context, isTest: Boolean): RoomDb {
      if (INSTANCE == null || isTest) {
        synchronized(RoomDb::class.java) {
          if (INSTANCE == null || isTest) {
            val dbBuilder = Room
              .databaseBuilder(context.applicationContext, RoomDb::class.java, "ww_db")
              .fallbackToDestructiveMigration() // Wipes and rebuilds instead of migrating

            if (isTest) dbBuilder.allowMainThreadQueries()

            INSTANCE = dbBuilder.build()
          }
        }
      }
      return INSTANCE!!
    }
  }
}
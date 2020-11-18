package com.gerardbradshaw.whetherweather.room

import android.content.Context
import androidx.room.*

@Database(
  entities = arrayOf(LocationData::class),
  version = 1,
  exportSchema = false
)
abstract class RoomDb : RoomDatabase() {

  abstract fun getLocationDao(): LocationDataDao

  companion object {
    private var INSTANCE: RoomDb? = null

    fun getDatabase(context: Context): RoomDb {
      if (INSTANCE == null) {
        synchronized(RoomDb::class.java) {
          if (INSTANCE == null) {

            INSTANCE = Room
              .databaseBuilder(context.applicationContext, RoomDb::class.java, "ww_db")
              .fallbackToDestructiveMigration() // Wipes and rebuilds instead of migrating
              .build()
            // TODO add callback to delete all content and repopulate
          }
        }
      }
      return INSTANCE!!
    }
  }
}
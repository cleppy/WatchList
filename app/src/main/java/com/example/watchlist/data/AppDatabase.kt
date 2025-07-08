package com.example.watchlist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WatchedMovie::class, WatchlistMovie::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao // MovieDao'yu MovieDao olarak adlandırdığınızı varsayıyorum

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movie_database"
                )
                    .fallbackToDestructiveMigration() // Geliştirme aşamasında bu satırı eklemek, veritabanı şeması değiştiğinde eski verileri silip yeni şemayı oluşturur. Üretim uygulamalarında migration yazmak gerekir.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
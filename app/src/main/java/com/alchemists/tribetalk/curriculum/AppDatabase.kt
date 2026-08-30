package com.alchemists.tribetalk.curriculum

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Domain::class,
        Competency::class,
        LearningOutcome::class,
        Lesson::class,
        Activity::class,
        Assessment::class,
        TeachingResource::class,
        Flashcard::class,
        Worksheet::class,
        Classroom::class,
        TeacherClassroom::class,
        Teacher::class,
        Student::class,
        StudentOutcomeProgress::class,
        UserResource::class,
        ContentTranslation::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun nipunDao(): NipunDao

    companion object {
        private const val DB_NAME = "nipun.db"
        private const val ASSET_PATH = "database/nipun.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, seeder: DemoDataSeeder?): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }
        }

        private fun build(context: Context): AppDatabase {
            val appContext = context.applicationContext
            val builder = Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)

            // Use the pre-packaged (bundled) database when present, else the DB is
            // created empty and seeded at runtime by DemoDataSeeder (see NipunRepository).
            return if (assetActuallyPresent(appContext)) {
                builder.createFromAsset(ASSET_PATH).build()
            } else {
                builder.build()
            }
        }

        private fun assetActuallyPresent(context: Context): Boolean {
            return try {
                context.assets.open(ASSET_PATH).use { true }
            } catch (e: Exception) {
                false
            }
        }
    }
}

package com.example.countdowntimer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CountdownEntity::class],
    version = 6
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): CountdownDao

    companion object {
        private val MIGRATION_1_6 = object : Migration(1, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateCountdownsToV6(db)
            }
        }

        private val MIGRATION_2_6 = object : Migration(2, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateCountdownsToV6(db)
            }
        }

        private val MIGRATION_3_6 = object : Migration(3, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateCountdownsToV6(db)
            }
        }

        private val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateCountdownsToV6(db)
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateCountdownsToV6(db)
            }
        }

        private fun migrateCountdownsToV6(db: SupportSQLiteDatabase) {
            val existingColumns = mutableSetOf<String>()
            db.query("PRAGMA table_info(countdowns)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    existingColumns.add(cursor.getString(nameIndex))
                }
            }

            val imageExpr = if ("imageUri" in existingColumns) "imageUri" else "NULL"
            val colorExpr = if ("colorIndex" in existingColumns) "colorIndex" else "0"
            val repeatExpr = if ("repeatYearly" in existingColumns) "repeatYearly" else "0"
            val categoryExpr = if ("category" in existingColumns) "category" else "'General'"
            val archivedExpr = if ("isArchived" in existingColumns) "isArchived" else "0"
            val createdExpr = if ("createdAt" in existingColumns) "createdAt" else "targetTime"

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS countdowns_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    targetTime INTEGER NOT NULL,
                    imageUri TEXT,
                    colorIndex INTEGER NOT NULL DEFAULT 0,
                    repeatYearly INTEGER NOT NULL DEFAULT 0,
                    category TEXT NOT NULL DEFAULT 'General',
                    isArchived INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                INSERT INTO countdowns_new (
                    id, title, targetTime, imageUri, colorIndex, repeatYearly, category, isArchived, createdAt
                )
                SELECT
                    id,
                    title,
                    targetTime,
                    $imageExpr,
                    $colorExpr,
                    $repeatExpr,
                    $categoryExpr,
                    $archivedExpr,
                    $createdExpr
                FROM countdowns
                """.trimIndent()
            )

            db.execSQL("DROP TABLE countdowns")
            db.execSQL("ALTER TABLE countdowns_new RENAME TO countdowns")
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "countdown_db"
                )
                    .addMigrations(
                        MIGRATION_1_6,
                        MIGRATION_2_6,
                        MIGRATION_3_6,
                        MIGRATION_4_6,
                        MIGRATION_5_6
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
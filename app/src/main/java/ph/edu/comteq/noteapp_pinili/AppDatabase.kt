package ph.edu.comteq.noteapp_pinili

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(
    entities = [
        Note::class,
        Tag::class,
        NoteTagCrossRef::class
    ],
    version = 2,
    exportSchema = true  // export to JSON OPTIOANAL
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){
    abstract fun noteDao(): NoteDao

    companion object{
        @Volatile
        private var instance: AppDatabase? = null

        // Migration from version 1 to version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to existing notes table
                database.execSQL("ALTER TABLE notes ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE notes ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")

                // Update existing notes to have current time as updated_at
                database.execSQL("UPDATE notes SET updated_at = created_at")

                // Create tags table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color TEXT NOT NULL DEFAULT '#6200EE'
                    )
                """)

                // Create junction table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS note_tag_cross_ref (
                        note_id INTEGER NOT NULL,
                        tag_id INTEGER NOT NULL,
                        PRIMARY KEY(note_id, tag_id)
                    )
                """)
            }
        }
        fun getDatabase(context: Context): AppDatabase{
            return instance ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "note_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                this.instance = instance
                instance
            }
        }
    }
}
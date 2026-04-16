package database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.Constants.DATABASE_VERSION

@Database(
    entities = [Note::class],
    version = DATABASE_VERSION
)

abstract class NoteDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
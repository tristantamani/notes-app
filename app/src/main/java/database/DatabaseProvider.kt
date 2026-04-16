package database

import android.content.Context
import androidx.room.Room
import com.example.notesapp.Constants.DATABASE_NAME

object DatabaseProvider {

    @Volatile private var instance: NoteDatabase? = null
    fun getInstance(context: Context): NoteDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }
    }

    private fun buildDatabase(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            NoteDatabase::class.java,
            DATABASE_NAME
        ).build()
}
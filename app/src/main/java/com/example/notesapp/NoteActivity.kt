package com.example.notesapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.notesapp.Constants.DEFAULT_NOTE_ID
import com.example.notesapp.Constants.EXTRA_NOTE_ID
import com.example.notesapp.ui.theme.NotesAppTheme
import database.DatabaseProvider
import database.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteActivity: ComponentActivity() {

    private val database by lazy {
        DatabaseProvider.getInstance(this)
    }

    private val dao by lazy {
        database.noteDao()
    }

    private var noteId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        noteId = intent.getLongExtra(EXTRA_NOTE_ID, DEFAULT_NOTE_ID).takeIf {
            it > 0
        }

        setContent {
            NotesAppTheme {

                val context = LocalContext.current

                val titleState = remember {
                    TextFieldState()
                }

                val contentState = remember {
                    TextFieldState()
                }

                var showDeleteDialog by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(noteId) {
                    noteId?.let { id ->
                        val note = withContext(Dispatchers.IO) {
                            dao.getNoteById(id)
                        }
                        titleState.edit {
                            replace(0, length, note.title)
                        }
                        contentState.edit {
                            replace(0, length, note.content)
                        }
                    }
                }
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = 50.dp,
                                start = 10.dp,
                                end = 10.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(context, MainActivity::class.java)
                                    if (titleState.text.isEmpty() && contentState.text.isEmpty()) {
                                        context.startActivity(intent)
                                    } else {
                                        saveNote(titleState, contentState)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (noteId != null) {
                                IconButton(
                                    onClick = {
                                        showDeleteDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    title = { Text("Delete Note") },
                                    text = {
                                        Text(
                                            text = "Are you sure you want to delete this note?"
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                deleteNote(titleState, contentState, context)
                                            }
                                        ) {
                                            Text("Delete")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { }
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .padding(
                                    top = 10.dp,
                                    start = 15.dp,
                                    end = 15.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                if (titleState.text.isEmpty()) {
                                    Text(
                                        text = "Enter title",
                                        fontSize = 36.sp,
                                        color = Color.Gray.copy(alpha = 0.5f)
                                    )
                                }
                                BasicTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(fontSize = 35.sp),
                                    state = titleState
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .padding(
                                    top = 10.dp,
                                    start = 15.dp,
                                    end = 15.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                if (contentState.text.isEmpty()) {
                                    Text(
                                        text = "Enter note content",
                                        fontSize = 16.sp,
                                        color = Color.Gray.copy(alpha = 0.5f)
                                    )
                                }
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    state = contentState
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveNote(titleState: TextFieldState, contentState: TextFieldState) {
        lifecycleScope.launch {
            val title = titleState.text.toString()
            val content = contentState.text.toString()
            if (noteId != null) {
                dao.update(Note(id = noteId!!, title = title, content = content))
            } else {
                dao.insert(Note(title = title, content = content))
            }
            finish()
        }
    }

    private fun deleteNote(titleState: TextFieldState, contentState: TextFieldState, context: Context) {
        lifecycleScope.launch {
            val title = titleState.text.toString()
            val content = contentState.text.toString()
            try {
                if (noteId != null) {
                    dao.delete(Note(id = noteId!!, title = title, content = content))
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("NoteActivity", "Error deleting note", e)
            }
        }
    }
}
package com.example.notesapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.Constants.EXTRA_NOTE_ID
import com.example.notesapp.ui.theme.NotesAppTheme
import database.DatabaseProvider

class MainActivity : ComponentActivity() {

    private val database by lazy {
        DatabaseProvider.getInstance(this)
    }

    private val dao by lazy {
        database.noteDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesAppTheme {

                val context = LocalContext.current

                val notes by dao.getAllNotes().collectAsStateWithLifecycle(
                    initialValue = emptyList()
                )

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
                        Text(
                            modifier = Modifier
                                .padding(10.dp),
                            text = "Notes",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2)
                        ) {
                            items(notes) { note ->
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .clickable {
                                            val intent = Intent(context, NoteActivity::class.java)
                                            intent.putExtra(EXTRA_NOTE_ID, note.id)
                                            context.startActivity(intent)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = note.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = note.content,
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        onClick = {
                            val intent = Intent(context, NoteActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add note")
                    }
                }
            }
        }
    }
}
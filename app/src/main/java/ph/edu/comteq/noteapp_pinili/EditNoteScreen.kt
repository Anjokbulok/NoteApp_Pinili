package ph.edu.comteq.noteapp_pinili

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditScreen(
    noteId: Int?,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val inEditMode = noteId != null
    val scope = rememberCoroutineScope()
    // State for form
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }
    var showTagInput by remember { mutableStateOf(false) }

    // Existing tags
    val allTags by viewModel.allTags.collectAsState(initial = emptyList())
    val selectedTags = remember { mutableStateListOf<Tag>() }
    // For note edit: load note and tags
    LaunchedEffect(noteId) {
        if (inEditMode && noteId != null) {
            val noteWithTags = viewModel.getNoteWithTags(noteId)
            noteWithTags?.let {
                title = it.note.title
                content = it.note.content
                category = it.note.category
                selectedTags.clear()
                selectedTags.addAll(it.tags)
            }
        } else {
            // Add mode: reset fields
            title = ""
            content = ""
            category = ""
            selectedTags.clear()
        }
    }
    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (inEditMode) "Edit Note" else "Add Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val note = Note(
                                    id = noteId ?: 0,
                                    title = title,
                                    content = content,
                                    category = category
                                )
                                if (inEditMode) {
                                    viewModel.updateNoteWithTags(note, selectedTags.toList())
                                } else {
                                    viewModel.insertNoteWithTags(note, selectedTags.toList())
                                }
                                onNavigateBack()
                            }
                            //
                        },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Note")
                    }
                    if (inEditMode) {
                        IconButton(onClick = {
                            scope.launch {
                                noteId?.let {
                                    val note = viewModel.getNoteWithTags(it)?.note
                                    note?.let {
                                        viewModel.delete(note)
                                    }
                                }
                                onNavigateBack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            // Category input
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            // Tag chips + add
            Text("Tags", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allTags.forEach { tag ->
                    val selected = selectedTags.any { it.id == tag.id }
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (selected) selectedTags.removeAll { it.id == tag.id } else selectedTags.add(
                                tag
                            )
                        },
                        label = { Text(tag.name) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                // Add tag button
                AssistChip(onClick = { showTagInput = true }, label = { Text("+ New Tag") })
            }
            if (showTagInput) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("New Tag Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (newTagName.isNotBlank()) {
                            scope.launch {
                                viewModel.insertTag(Tag(name = newTagName.trim()))
                                newTagName = ""
                                showTagInput = false
                            }
                        }
                    }) {
                        Text("Add")
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton({ showTagInput = false }) { Text("Cancel") }
                }
            }
        }
    }
}

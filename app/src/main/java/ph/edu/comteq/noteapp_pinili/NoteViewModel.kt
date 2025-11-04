package ph.edu.comteq.noteapp_pinili

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch


class NoteViewModel(application: Application): AndroidViewModel(application) { // Get an instance of the database and then the DAO from it.

    private val noteDao: NoteDao = AppDatabase.getDatabase(application).noteDao()

    // Track what the user is searching for
    private val _searchQuery = MutableStateFlow("")

    val allNotes: Flow<List<Note>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            noteDao.getALlNotes()  // Show everything
        } else {
            noteDao.searchNotes(query)
        }
    }
    // Call this when user types in search box
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    // Call this to clear the search
    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun delete(note: Note) = viewModelScope.launch {
        noteDao.deleteNote (note)
    }

    // NEW: All notes WITH their tags
    val allNotesWithTags: Flow<List<NoteWithTags>> = noteDao.getAllNotesWithTags()

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun getNoteWithTags(noteId: Int): NoteWithTags? {
        return noteDao.getNoteWithTags(noteId)
    }

    fun insertTag(tag: Tag) = viewModelScope.launch {
        noteDao.insertTag(tag)
    }

    fun updateTag(tag: Tag) = viewModelScope.launch {
        noteDao.updateTag(tag)
    }

    fun deleteTag(tag: Tag) = viewModelScope.launch {
        noteDao.deleteTag(tag)
    }

    // Add a tag to a note
    fun addTagToNote(noteId: Int, tagId: Int) = viewModelScope.launch {
        noteDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    // Remove a tag from a note
    fun removeTagFromNote(noteId: Int, tagId: Int) = viewModelScope.launch {
        noteDao.deleteNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    // Get all notes that have a specific tag
    fun getNotesWithTag(tagId: Int): Flow<List<Note>> {
        return noteDao.getNotesWithTag(tagId)
    }

    val allTags: Flow<List<Tag>> = noteDao.getAllTags()

    fun insertNoteWithTags(note: Note, tags: List<Tag>) = viewModelScope.launch {
        noteDao.insertNoteWithTags(note, tags)
    }

    fun updateNoteWithTags(note: Note, tags: List<Tag>) = viewModelScope.launch {
        noteDao.updateNoteWithTags(note, tags)
    }


}
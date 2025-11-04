package ph.edu.comteq.noteapp_pinili

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {
    @Insert
    abstract suspend fun insertNote(note: Note): Long

    @Update
    abstract suspend fun updateNote(note: Note)

    @Delete
    abstract suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    abstract suspend fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM notes ORDER BY id DESC")
    abstract fun getALlNotes(): Flow<List<Note>>

    @Query("DELETE FROM notes")
    abstract suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes WHERE title " +
            "LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
    abstract fun searchNotes(searchQuery: String): Flow<List<Note>>

    // Connect a note to a tag
    @Insert(onConflict = OnConflictStrategy.IGNORE)  // Ignore if already connected
    abstract suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    // Disconnect a note from a tag
    @Delete
    abstract suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE note_id = :noteId")
    abstract suspend fun deleteAllTagsForNote(noteId: Int)

    // Get all notes WITH their tags
    @Transaction
    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    abstract fun getAllNotesWithTags(): Flow<List<NoteWithTags>>

    // Get a note WITH all its tags
    @Transaction  // Important: Ensures all data loads together
    @Query("SELECT * FROM notes WHERE id = :noteId")
    abstract suspend fun getNoteWithTags(noteId: Int): NoteWithTags?

    @Insert(onConflict = OnConflictStrategy.REPLACE)  // Ignore if already connected
    abstract suspend fun insertTag(tag: Tag): Long

    @Update
    abstract suspend fun updateTag(tag: Tag)

    @Delete
    abstract suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    abstract fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    abstract suspend fun getTagById(id: Int): Tag?

    // Get all notes that have a specific tag
    @Transaction
    @Query("""
        SELECT * FROM notes 
        INNER JOIN note_tag_cross_ref ON notes.id = note_tag_cross_ref.note_id
        WHERE note_tag_cross_ref.tag_id = :tagId
        ORDER BY updated_at DESC
    """)
    abstract fun getNotesWithTag(tagId: Int): Flow<List<Note>>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    abstract suspend fun getTagByName(name: String): Tag?

    @Transaction
    open suspend fun insertNoteWithTags(note: Note, tags: List<Tag>) {
        val noteId = insertNote(note.copy(id = 0))

        for (tag in tags) {
            var existingTag = getTagByName(tag.name)
            if (existingTag == null) {
                val newTagId = insertTag(tag.copy(id = 0))
                existingTag = tag.copy(id = newTagId.toInt())
            }


            val crossRef = NoteTagCrossRef(noteId = noteId.toInt(), tagId = existingTag.id)
            insertNoteTagCrossRef(crossRef)
        }
    }

    @Transaction
    open suspend fun updateNoteWithTags(note: Note, tags: List<Tag>) {
        updateNote(note)
        deleteAllTagsForNote(note.id)
        tags.forEach { tag ->
            var existingTag = getTagByName(tag.name)
            if (existingTag == null) {
                val newTagId = insertTag(tag.copy(id = 0))
                existingTag = tag.copy(id = newTagId.toInt())
            }
            insertNoteTagCrossRef(NoteTagCrossRef(noteId = note.id, tagId = existingTag.id))
        }
    }
}
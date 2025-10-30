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
interface NoteDao {
    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getALlNotes(): Flow<List<Note>>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes WHERE title " +
            "LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
    fun searchNotes(searchQuery: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)  // Ignore if already connected
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Transaction
    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun getAllNotesWithTags(): Flow<List<NoteWithTags>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteWithTags(noteId: Int): NoteWithTags?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Int): Tag?

    @Transaction
    @Query("""
        SELECT * FROM notes 
        INNER JOIN note_tag_cross_ref ON notes.id = note_tag_cross_ref.note_id
        WHERE note_tag_cross_ref.tag_id = :tagId
        ORDER BY updated_at DESC
    """)
    fun getNotesWithTag(tagId: Int): Flow<List<Note>>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?



    @Transaction
    suspend fun insertNoteWithTags(note: Note, tags: List<Tag>) {
        val noteId = insertNote(note)

        for (tag in tags) {
            var existingTag = getTagByName(tag.name)
            if (existingTag == null) {
                val newTagId = insertTag(tag)
                existingTag = tag.copy(id = newTagId.toInt())
            }


            val crossRef = NoteTagCrossRef(noteId = noteId.toInt(), tagId = existingTag.id)
            insertNoteTagCrossRef(crossRef)
        }
    }
}
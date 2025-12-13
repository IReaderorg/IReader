package ireader.domain.plugins.characters

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ireader.core.util.createICoroutineScope
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for the Character Database system.
 */
class CharacterDatabaseManager(
    private val characterRepository: CharacterRepository,
    private val characterDetector: CharacterDetector?
) {
    private val scope: CoroutineScope = createICoroutineScope()
    
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()
    
    private val _relationships = MutableStateFlow<List<CharacterRelationship>>(emptyList())
    val relationships: StateFlow<List<CharacterRelationship>> = _relationships.asStateFlow()
    
    private val _groups = MutableStateFlow<List<CharacterGroup>>(emptyList())
    val groups: StateFlow<List<CharacterGroup>> = _groups.asStateFlow()
    
    // Character CRUD
    
    suspend fun loadCharactersForBook(bookId: Long) {
        _characters.value = characterRepository.getCharactersByBook(bookId)
        _relationships.value = characterRepository.getRelationshipsByBook(bookId)
        _groups.value = characterRepository.getGroupsByBook(bookId)
    }
    
    suspend fun createCharacter(character: Character): Result<Character> {
        return try {
            val saved = characterRepository.saveCharacter(character)
            _characters.value = _characters.value + saved
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCharacter(character: Character): Result<Character> {
        return try {
            val updated = character.copy(updatedAt = currentTimeToLong())
            characterRepository.saveCharacter(updated)
            _characters.value = _characters.value.map { if (it.id == updated.id) updated else it }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCharacter(characterId: String): Result<Unit> {
        return try {
            characterRepository.deleteCharacter(characterId)
            _characters.value = _characters.value.filter { it.id != characterId }
            _relationships.value = _relationships.value.filter { 
                it.character1Id != characterId && it.character2Id != characterId 
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Relationship management
    
    suspend fun createRelationship(relationship: CharacterRelationship): Result<CharacterRelationship> {
        return try {
            val saved = characterRepository.saveRelationship(relationship)
            _relationships.value = _relationships.value + saved
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteRelationship(relationshipId: String): Result<Unit> {
        return try {
            characterRepository.deleteRelationship(relationshipId)
            _relationships.value = _relationships.value.filter { it.id != relationshipId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getRelationshipsForCharacter(characterId: String): List<CharacterRelationship> {
        return _relationships.value.filter { 
            it.character1Id == characterId || it.character2Id == characterId 
        }
    }
    
    // Group management
    
    suspend fun createGroup(group: CharacterGroup): Result<CharacterGroup> {
        return try {
            val saved = characterRepository.saveGroup(group)
            _groups.value = _groups.value + saved
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addCharacterToGroup(groupId: String, characterId: String): Result<Unit> {
        return try {
            val group = _groups.value.find { it.id == groupId }
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            val updated = group.copy(characterIds = group.characterIds + characterId)
            characterRepository.saveGroup(updated)
            _groups.value = _groups.value.map { if (it.id == groupId) updated else it }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Appearance tracking
    
    suspend fun recordAppearance(appearance: CharacterAppearance): Result<Unit> {
        return try {
            characterRepository.saveAppearance(appearance)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAppearances(characterId: String, bookId: Long? = null): List<CharacterAppearance> {
        return characterRepository.getAppearances(characterId, bookId)
    }
    
    // Notes
    
    suspend fun addNote(note: CharacterNote): Result<CharacterNote> {
        return try {
            val saved = characterRepository.saveNote(note)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getNotes(characterId: String): List<CharacterNote> {
        return characterRepository.getNotes(characterId)
    }
    
    // Timeline
    
    suspend fun addTimelineEvent(event: CharacterTimelineEvent): Result<Unit> {
        return try {
            characterRepository.saveTimelineEvent(event)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTimeline(characterId: String): List<CharacterTimelineEvent> {
        return characterRepository.getTimeline(characterId)
    }
    
    // AI Detection
    
    suspend fun detectCharacters(
        bookId: Long,
        chapterIds: List<Long>? = null
    ): Result<List<DetectedCharacter>> {
        val detector = characterDetector
            ?: return Result.failure(IllegalStateException("Character detector not available"))
        
        return try {
            val detected = detector.detectCharacters(bookId, chapterIds)
            Result.success(detected)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun importDetectedCharacters(
        bookId: Long,
        detected: List<DetectedCharacter>
    ): Result<List<Character>> {
        val imported = mutableListOf<Character>()
        
        for (dc in detected) {
            val character = Character(
                id = "char_${currentTimeToLong()}_${(0..9999).random()}",
                name = dc.name,
                aliases = dc.aliases,
                description = dc.description,
                role = dc.role,
                traits = dc.traits,
                bookIds = listOf(bookId),
                createdAt = currentTimeToLong(),
                updatedAt = currentTimeToLong(),
                isUserCreated = false,
                confidence = dc.confidence
            )
            
            createCharacter(character).onSuccess { imported.add(it) }
            
            // Record appearances
            for (appearance in dc.appearances) {
                recordAppearance(CharacterAppearance(
                    id = "app_${currentTimeToLong()}_${(0..9999).random()}",
                    characterId = character.id,
                    bookId = bookId,
                    chapterId = appearance.chapterId,
                    chapterTitle = "",
                    paragraphIndex = appearance.paragraphIndex,
                    textSnippet = appearance.textSnippet,
                    appearanceType = appearance.type,
                    timestamp = currentTimeToLong()
                ))
            }
        }
        
        // Create relationships
        for (dc in detected) {
            val sourceChar = imported.find { it.name == dc.name } ?: continue
            for (rel in dc.relationships) {
                val targetChar = imported.find { it.name == rel.targetName } ?: continue
                createRelationship(CharacterRelationship(
                    id = "rel_${currentTimeToLong()}_${(0..9999).random()}",
                    character1Id = sourceChar.id,
                    character2Id = targetChar.id,
                    relationshipType = rel.type,
                    description = rel.description,
                    bookIds = listOf(bookId),
                    createdAt = currentTimeToLong(),
                    updatedAt = currentTimeToLong()
                ))
            }
        }
        
        return Result.success(imported)
    }
    
    // Search and filter
    
    suspend fun searchCharacters(filter: CharacterFilter): List<Character> {
        return characterRepository.searchCharacters(filter)
    }
    
    // Statistics
    
    suspend fun getCharacterStats(characterId: String): CharacterStats? {
        return characterRepository.getCharacterStats(characterId)
    }
    
    suspend fun getBookCharacterSummary(bookId: Long): BookCharacterSummary {
        val chars = characterRepository.getCharactersByBook(bookId)
        val rels = characterRepository.getRelationshipsByBook(bookId)
        val grps = characterRepository.getGroupsByBook(bookId)
        
        return BookCharacterSummary(
            bookId = bookId,
            totalCharacters = chars.size,
            protagonists = chars.filter { it.role == CharacterRole.PROTAGONIST }.map { it.name },
            antagonists = chars.filter { it.role == CharacterRole.ANTAGONIST }.map { it.name },
            supportingCharacters = chars.count { it.role == CharacterRole.SUPPORTING },
            relationshipCount = rels.size,
            groupCount = grps.size
        )
    }
    
    // Merge characters (for duplicates)
    
    suspend fun mergeCharacters(
        primaryId: String,
        secondaryId: String
    ): Result<Character> {
        return try {
            val primary = _characters.value.find { it.id == primaryId }
                ?: return Result.failure(IllegalArgumentException("Primary character not found"))
            val secondary = _characters.value.find { it.id == secondaryId }
                ?: return Result.failure(IllegalArgumentException("Secondary character not found"))
            
            // Merge data
            val merged = primary.copy(
                aliases = (primary.aliases + secondary.aliases + secondary.name).distinct(),
                traits = (primary.traits + secondary.traits).distinct(),
                tags = (primary.tags + secondary.tags).distinct(),
                bookIds = (primary.bookIds + secondary.bookIds).distinct(),
                updatedAt = currentTimeToLong()
            )
            
            // Update relationships to point to primary
            val updatedRels = _relationships.value.map { rel ->
                when {
                    rel.character1Id == secondaryId -> rel.copy(character1Id = primaryId)
                    rel.character2Id == secondaryId -> rel.copy(character2Id = primaryId)
                    else -> rel
                }
            }
            
            // Save merged character
            characterRepository.saveCharacter(merged)
            
            // Update relationships
            for (rel in updatedRels.filter { it.character1Id == primaryId || it.character2Id == primaryId }) {
                characterRepository.saveRelationship(rel)
            }
            
            // Delete secondary
            characterRepository.deleteCharacter(secondaryId)
            
            // Update state
            _characters.value = _characters.value
                .filter { it.id != secondaryId }
                .map { if (it.id == primaryId) merged else it }
            _relationships.value = updatedRels
            
            Result.success(merged)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Repository interface for character data.
 */
interface CharacterRepository {
    suspend fun getCharactersByBook(bookId: Long): List<Character>
    suspend fun getCharactersBySeries(seriesId: String): List<Character>
    suspend fun saveCharacter(character: Character): Character
    suspend fun deleteCharacter(characterId: String)
    suspend fun searchCharacters(filter: CharacterFilter): List<Character>
    
    suspend fun getRelationshipsByBook(bookId: Long): List<CharacterRelationship>
    suspend fun saveRelationship(relationship: CharacterRelationship): CharacterRelationship
    suspend fun deleteRelationship(relationshipId: String)
    
    suspend fun getGroupsByBook(bookId: Long): List<CharacterGroup>
    suspend fun saveGroup(group: CharacterGroup): CharacterGroup
    suspend fun deleteGroup(groupId: String)
    
    suspend fun saveAppearance(appearance: CharacterAppearance)
    suspend fun getAppearances(characterId: String, bookId: Long?): List<CharacterAppearance>
    
    suspend fun saveNote(note: CharacterNote): CharacterNote
    suspend fun getNotes(characterId: String): List<CharacterNote>
    
    suspend fun saveTimelineEvent(event: CharacterTimelineEvent)
    suspend fun getTimeline(characterId: String): List<CharacterTimelineEvent>
    
    suspend fun getCharacterStats(characterId: String): CharacterStats?
}

/**
 * Interface for AI-powered character detection.
 */
interface CharacterDetector {
    suspend fun detectCharacters(bookId: Long, chapterIds: List<Long>?): List<DetectedCharacter>
    suspend fun detectRelationships(characterNames: List<String>, bookId: Long): List<DetectedRelationship>
}

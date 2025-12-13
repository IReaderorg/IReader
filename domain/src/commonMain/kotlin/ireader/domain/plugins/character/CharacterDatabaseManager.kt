package ireader.domain.plugins.character

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for the character database.
 */
class CharacterDatabaseManager(
    private val repository: CharacterRepository,
    private val aiExtractor: CharacterAIExtractor?
) {
    private val _characters = MutableStateFlow<Map<Long, List<Character>>>(emptyMap())
    val characters: StateFlow<Map<Long, List<Character>>> = _characters.asStateFlow()
    
    private val _selectedCharacter = MutableStateFlow<Character?>(null)
    val selectedCharacter: StateFlow<Character?> = _selectedCharacter.asStateFlow()
    
    // Character CRUD
    
    /**
     * Get all characters for a book.
     */
    suspend fun getCharactersForBook(bookId: Long): List<Character> {
        val chars = repository.getCharactersByBook(bookId)
        _characters.value = _characters.value + (bookId to chars)
        return chars
    }
    
    /**
     * Get a character by ID.
     */
    suspend fun getCharacter(characterId: String): Character? {
        return repository.getCharacter(characterId)
    }
    
    /**
     * Search characters.
     */
    suspend fun searchCharacters(query: CharacterSearchQuery): List<Character> {
        return repository.searchCharacters(query)
    }
    
    /**
     * Create a new character.
     */
    suspend fun createCharacter(
        bookId: Long,
        name: String,
        description: String = "",
        role: CharacterRole = CharacterRole.UNKNOWN,
        imageUrl: String? = null,
        traits: List<CharacterTrait> = emptyList(),
        aliases: List<String> = emptyList()
    ): Character {
        val now = currentTimeToLong()
        val character = Character(
            id = generateCharacterId(),
            bookId = bookId,
            name = name,
            aliases = aliases,
            description = description,
            imageUrl = imageUrl,
            role = role,
            traits = traits,
            isUserCreated = true,
            isConfirmed = true,
            createdAt = now,
            updatedAt = now
        )
        
        repository.saveCharacter(character)
        refreshBookCharacters(bookId)
        return character
    }
    
    /**
     * Update a character.
     */
    suspend fun updateCharacter(character: Character): Character {
        val updated = character.copy(updatedAt = currentTimeToLong())
        repository.saveCharacter(updated)
        refreshBookCharacters(character.bookId)
        return updated
    }
    
    /**
     * Delete a character.
     */
    suspend fun deleteCharacter(characterId: String) {
        val character = repository.getCharacter(characterId) ?: return
        repository.deleteCharacter(characterId)
        refreshBookCharacters(character.bookId)
    }
    
    /**
     * Confirm an AI-extracted character.
     */
    suspend fun confirmCharacter(characterId: String): Character? {
        val character = repository.getCharacter(characterId) ?: return null
        val confirmed = character.copy(isConfirmed = true, updatedAt = currentTimeToLong())
        repository.saveCharacter(confirmed)
        refreshBookCharacters(character.bookId)
        return confirmed
    }

    // Relationships
    
    /**
     * Get relationships for a character.
     */
    suspend fun getRelationships(characterId: String): List<CharacterRelationship> {
        return repository.getRelationships(characterId)
    }
    
    /**
     * Create a relationship between characters.
     */
    suspend fun createRelationship(
        characterId: String,
        relatedCharacterId: String,
        type: RelationshipType,
        description: String = "",
        sentiment: RelationshipSentiment = RelationshipSentiment.NEUTRAL,
        isBidirectional: Boolean = true
    ): CharacterRelationship {
        val now = currentTimeToLong()
        val relationship = CharacterRelationship(
            id = generateRelationshipId(),
            characterId = characterId,
            relatedCharacterId = relatedCharacterId,
            relationshipType = type,
            description = description,
            sentiment = sentiment,
            isBidirectional = isBidirectional,
            createdAt = now,
            updatedAt = now
        )
        
        repository.saveRelationship(relationship)
        return relationship
    }
    
    /**
     * Update a relationship.
     */
    suspend fun updateRelationship(relationship: CharacterRelationship): CharacterRelationship {
        val updated = relationship.copy(updatedAt = currentTimeToLong())
        repository.saveRelationship(updated)
        return updated
    }
    
    /**
     * Delete a relationship.
     */
    suspend fun deleteRelationship(relationshipId: String) {
        repository.deleteRelationship(relationshipId)
    }
    
    /**
     * Get relationship graph for visualization.
     */
    suspend fun getRelationshipGraph(bookId: Long): RelationshipGraph {
        val characters = repository.getCharactersByBook(bookId)
        val relationships = mutableListOf<CharacterRelationship>()
        
        for (character in characters) {
            relationships.addAll(repository.getRelationships(character.id))
        }
        
        return RelationshipGraph(
            nodes = characters.map { char ->
                GraphNode(
                    id = char.id,
                    label = char.name,
                    role = char.role,
                    importance = char.importance,
                    imageUrl = char.imageUrl
                )
            },
            edges = relationships.distinctBy { it.id }.map { rel ->
                GraphEdge(
                    id = rel.id,
                    source = rel.characterId,
                    target = rel.relatedCharacterId,
                    type = rel.relationshipType,
                    sentiment = rel.sentiment,
                    strength = rel.strength,
                    isBidirectional = rel.isBidirectional
                )
            }
        )
    }
    
    // Appearances
    
    /**
     * Track a character appearance in a chapter.
     */
    suspend fun trackAppearance(
        characterId: String,
        bookId: Long,
        chapterId: Long,
        chapterNumber: Int,
        mentionCount: Int = 1,
        dialogueCount: Int = 0,
        interactsWith: List<String> = emptyList()
    ): CharacterAppearance {
        val appearance = CharacterAppearance(
            id = generateAppearanceId(),
            characterId = characterId,
            bookId = bookId,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            mentionCount = mentionCount,
            dialogueCount = dialogueCount,
            interactsWith = interactsWith,
            createdAt = currentTimeToLong()
        )
        
        repository.saveAppearance(appearance)
        
        // Update character's appearance count and last appearance
        val character = repository.getCharacter(characterId)
        if (character != null) {
            val chapterRef = ChapterReference(chapterId, chapterNumber, null)
            val updated = character.copy(
                appearanceCount = character.appearanceCount + 1,
                firstAppearance = character.firstAppearance ?: chapterRef,
                lastAppearance = chapterRef,
                updatedAt = currentTimeToLong()
            )
            repository.saveCharacter(updated)
        }
        
        return appearance
    }
    
    /**
     * Get appearances for a character.
     */
    suspend fun getAppearances(characterId: String): List<CharacterAppearance> {
        return repository.getAppearances(characterId)
    }
    
    /**
     * Get characters appearing in a chapter.
     */
    suspend fun getCharactersInChapter(chapterId: Long): List<Character> {
        return repository.getCharactersInChapter(chapterId)
    }
    
    // Character Arcs
    
    /**
     * Create a character arc.
     */
    suspend fun createArc(
        characterId: String,
        bookId: Long,
        arcType: ArcType,
        title: String,
        description: String,
        startChapter: ChapterReference
    ): CharacterArc {
        val now = currentTimeToLong()
        val arc = CharacterArc(
            id = generateArcId(),
            characterId = characterId,
            bookId = bookId,
            arcType = arcType,
            title = title,
            description = description,
            startChapter = startChapter,
            createdAt = now,
            updatedAt = now
        )
        
        repository.saveArc(arc)
        return arc
    }
    
    /**
     * Add a milestone to an arc.
     */
    suspend fun addArcMilestone(
        arcId: String,
        milestone: ArcMilestone
    ): CharacterArc? {
        val arc = repository.getArc(arcId) ?: return null
        val updated = arc.copy(
            milestones = arc.milestones + milestone,
            updatedAt = currentTimeToLong()
        )
        repository.saveArc(updated)
        return updated
    }
    
    /**
     * Get arcs for a character.
     */
    suspend fun getArcs(characterId: String): List<CharacterArc> {
        return repository.getArcs(characterId)
    }
    
    // Groups
    
    /**
     * Create a character group.
     */
    suspend fun createGroup(
        bookId: Long,
        name: String,
        type: GroupType,
        description: String = "",
        memberIds: List<String> = emptyList(),
        leaderId: String? = null
    ): CharacterGroup {
        val now = currentTimeToLong()
        val group = CharacterGroup(
            id = generateGroupId(),
            bookId = bookId,
            name = name,
            description = description,
            type = type,
            memberIds = memberIds,
            leaderId = leaderId,
            createdAt = now,
            updatedAt = now
        )
        
        repository.saveGroup(group)
        return group
    }
    
    /**
     * Add a character to a group.
     */
    suspend fun addToGroup(groupId: String, characterId: String): CharacterGroup? {
        val group = repository.getGroup(groupId) ?: return null
        if (characterId in group.memberIds) return group
        
        val updated = group.copy(
            memberIds = group.memberIds + characterId,
            updatedAt = currentTimeToLong()
        )
        repository.saveGroup(updated)
        return updated
    }
    
    /**
     * Get groups for a book.
     */
    suspend fun getGroups(bookId: Long): List<CharacterGroup> {
        return repository.getGroups(bookId)
    }
    
    // AI Extraction
    
    /**
     * Extract characters from chapter text using AI.
     */
    suspend fun extractCharactersFromChapter(
        bookId: Long,
        chapterId: Long,
        chapterNumber: Int,
        chapterText: String
    ): List<Character> {
        if (aiExtractor == null) return emptyList()
        
        val existingCharacters = repository.getCharactersByBook(bookId)
        val extracted = aiExtractor.extractCharacters(
            text = chapterText,
            existingCharacters = existingCharacters
        )
        
        val now = currentTimeToLong()
        val newCharacters = mutableListOf<Character>()
        
        for (extractedChar in extracted) {
            // Check if character already exists
            val existing = existingCharacters.find { 
                it.name.equals(extractedChar.name, ignoreCase = true) ||
                it.aliases.any { alias -> alias.equals(extractedChar.name, ignoreCase = true) }
            }
            
            if (existing != null) {
                // Track appearance for existing character
                trackAppearance(
                    characterId = existing.id,
                    bookId = bookId,
                    chapterId = chapterId,
                    chapterNumber = chapterNumber,
                    mentionCount = extractedChar.mentionCount,
                    dialogueCount = extractedChar.dialogueCount
                )
            } else {
                // Create new character
                val character = Character(
                    id = generateCharacterId(),
                    bookId = bookId,
                    name = extractedChar.name,
                    description = extractedChar.description,
                    role = extractedChar.suggestedRole,
                    traits = extractedChar.traits,
                    firstAppearance = ChapterReference(chapterId, chapterNumber, null),
                    lastAppearance = ChapterReference(chapterId, chapterNumber, null),
                    appearanceCount = 1,
                    importance = extractedChar.importance,
                    isUserCreated = false,
                    isConfirmed = false,
                    createdAt = now,
                    updatedAt = now
                )
                
                repository.saveCharacter(character)
                newCharacters.add(character)
            }
        }
        
        // Extract relationships
        val relationships = aiExtractor.extractRelationships(
            text = chapterText,
            characters = existingCharacters + newCharacters
        )
        
        for (rel in relationships) {
            val existing = repository.getRelationshipBetween(rel.characterId, rel.relatedCharacterId)
            if (existing == null) {
                repository.saveRelationship(rel.copy(
                    id = generateRelationshipId(),
                    createdAt = now,
                    updatedAt = now
                ))
            }
        }
        
        refreshBookCharacters(bookId)
        return newCharacters
    }
    
    // Statistics
    
    /**
     * Get character statistics for a book.
     */
    suspend fun getBookStats(bookId: Long): BookCharacterStats {
        return repository.getBookStats(bookId)
    }
    
    // Cross-book linking
    
    /**
     * Link characters across books.
     */
    suspend fun linkCharacters(
        primaryCharacterId: String,
        linkedCharacterId: String,
        linkType: LinkType,
        notes: String = ""
    ): CharacterLink {
        val link = CharacterLink(
            id = generateLinkId(),
            primaryCharacterId = primaryCharacterId,
            linkedCharacterId = linkedCharacterId,
            linkType = linkType,
            notes = notes,
            createdAt = currentTimeToLong()
        )
        
        repository.saveLink(link)
        
        // Update linked character reference
        val linkedChar = repository.getCharacter(linkedCharacterId)
        if (linkedChar != null) {
            repository.saveCharacter(linkedChar.copy(
                linkedCharacterId = primaryCharacterId,
                updatedAt = currentTimeToLong()
            ))
        }
        
        return link
    }
    
    /**
     * Get linked characters.
     */
    suspend fun getLinkedCharacters(characterId: String): List<Character> {
        return repository.getLinkedCharacters(characterId)
    }
    
    // Helpers
    
    private suspend fun refreshBookCharacters(bookId: Long) {
        val chars = repository.getCharactersByBook(bookId)
        _characters.value = _characters.value + (bookId to chars)
    }
    
    private fun generateCharacterId() = "char_${currentTimeToLong()}_${(0..999999).random()}"
    private fun generateRelationshipId() = "rel_${currentTimeToLong()}_${(0..999999).random()}"
    private fun generateAppearanceId() = "app_${currentTimeToLong()}_${(0..999999).random()}"
    private fun generateArcId() = "arc_${currentTimeToLong()}_${(0..999999).random()}"
    private fun generateGroupId() = "grp_${currentTimeToLong()}_${(0..999999).random()}"
    private fun generateLinkId() = "link_${currentTimeToLong()}_${(0..999999).random()}"
}

/**
 * Relationship graph for visualization.
 */
data class RelationshipGraph(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
)

data class GraphNode(
    val id: String,
    val label: String,
    val role: CharacterRole,
    val importance: Float,
    val imageUrl: String?
)

data class GraphEdge(
    val id: String,
    val source: String,
    val target: String,
    val type: RelationshipType,
    val sentiment: RelationshipSentiment,
    val strength: Float,
    val isBidirectional: Boolean
)

/**
 * Repository interface for character data.
 */
interface CharacterRepository {
    suspend fun getCharacter(id: String): Character?
    suspend fun getCharactersByBook(bookId: Long): List<Character>
    suspend fun searchCharacters(query: CharacterSearchQuery): List<Character>
    suspend fun saveCharacter(character: Character)
    suspend fun deleteCharacter(id: String)
    
    suspend fun getRelationships(characterId: String): List<CharacterRelationship>
    suspend fun getRelationshipBetween(char1Id: String, char2Id: String): CharacterRelationship?
    suspend fun saveRelationship(relationship: CharacterRelationship)
    suspend fun deleteRelationship(id: String)
    
    suspend fun getAppearances(characterId: String): List<CharacterAppearance>
    suspend fun getCharactersInChapter(chapterId: Long): List<Character>
    suspend fun saveAppearance(appearance: CharacterAppearance)
    
    suspend fun getArc(id: String): CharacterArc?
    suspend fun getArcs(characterId: String): List<CharacterArc>
    suspend fun saveArc(arc: CharacterArc)
    
    suspend fun getGroup(id: String): CharacterGroup?
    suspend fun getGroups(bookId: Long): List<CharacterGroup>
    suspend fun saveGroup(group: CharacterGroup)
    
    suspend fun getLinkedCharacters(characterId: String): List<Character>
    suspend fun saveLink(link: CharacterLink)
    
    suspend fun getBookStats(bookId: Long): BookCharacterStats
}

/**
 * AI extractor interface for character extraction.
 */
interface CharacterAIExtractor {
    suspend fun extractCharacters(
        text: String,
        existingCharacters: List<Character>
    ): List<ExtractedCharacter>
    
    suspend fun extractRelationships(
        text: String,
        characters: List<Character>
    ): List<CharacterRelationship>
}

/**
 * Extracted character from AI.
 */
data class ExtractedCharacter(
    val name: String,
    val description: String,
    val suggestedRole: CharacterRole,
    val traits: List<CharacterTrait>,
    val mentionCount: Int,
    val dialogueCount: Int,
    val importance: Float
)

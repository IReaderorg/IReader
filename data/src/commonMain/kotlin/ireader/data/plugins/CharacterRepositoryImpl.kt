package ireader.data.plugins

import ireader.domain.plugins.characters.*
import data.CharacterQueries
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Implementation of CharacterRepository using SQLDelight.
 */
class CharacterRepositoryImpl(
    private val queries: CharacterQueries
) : CharacterRepository {
    
    override suspend fun getCharactersByBook(bookId: Long): List<Character> {
        return queries.selectCharactersByBook(bookId.toString())
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getCharactersBySeries(seriesId: String): List<Character> {
        return queries.selectCharactersBySeries(seriesId)
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun saveCharacter(character: Character): Character {
        queries.insertCharacter(
            id = character.id,
            name = character.name,
            aliases = character.aliases.joinToString(","),
            description = character.description,
            image_url = character.imageUrl,
            role = character.role.name,
            traits = character.traits.joinToString(","),
            tags = character.tags.joinToString(","),
            book_ids = character.bookIds.joinToString(","),
            series_id = character.seriesId,
            first_appearance_id = character.firstAppearance?.id,
            created_at = character.createdAt,
            updated_at = character.updatedAt,
            is_user_created = character.isUserCreated,
            confidence = character.confidence.toDouble(),
            metadata = "{}" // Would serialize metadata
        )
        return character
    }
    
    override suspend fun deleteCharacter(characterId: String) {
        queries.deleteCharacter(characterId)
    }
    
    override suspend fun searchCharacters(filter: CharacterFilter): List<Character> {
        val query = filter.query ?: ""
        return queries.searchCharacters(query, query)
            .executeAsList()
            .map { it.toDomain() }
            .filter { char ->
                val filterBookIds = filter.bookIds
                val filterRoles = filter.roles
                val filterTags = filter.tags
                (filterBookIds == null || char.bookIds.any { it in filterBookIds }) &&
                (filterRoles == null || char.role in filterRoles) &&
                (filterTags == null || char.tags.any { it in filterTags }) &&
                (filter.isUserCreated == null || char.isUserCreated == filter.isUserCreated)
            }
            .let { list ->
                when (filter.sortBy) {
                    CharacterSortOption.NAME -> if (filter.sortAscending) list.sortedBy { it.name } else list.sortedByDescending { it.name }
                    CharacterSortOption.ROLE -> list.sortedBy { it.role.ordinal }
                    CharacterSortOption.LAST_UPDATED -> list.sortedByDescending { it.updatedAt }
                    else -> list
                }
            }
    }
    
    override suspend fun getRelationshipsByBook(bookId: Long): List<CharacterRelationship> {
        return queries.selectRelationshipsByBook(bookId.toString())
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun saveRelationship(relationship: CharacterRelationship): CharacterRelationship {
        queries.insertRelationship(
            id = relationship.id,
            character1_id = relationship.character1Id,
            character2_id = relationship.character2Id,
            relationship_type = relationship.relationshipType.name,
            custom_type = relationship.customType,
            description = relationship.description,
            strength = relationship.strength.toDouble(),
            is_symmetric = relationship.isSymmetric,
            book_ids = relationship.bookIds.joinToString(","),
            created_at = relationship.createdAt,
            updated_at = relationship.updatedAt
        )
        return relationship
    }
    
    override suspend fun deleteRelationship(relationshipId: String) {
        queries.deleteRelationship(relationshipId)
    }
    
    override suspend fun getGroupsByBook(bookId: Long): List<CharacterGroup> {
        return queries.selectGroupsByBook(bookId.toString())
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun saveGroup(group: CharacterGroup): CharacterGroup {
        queries.insertGroup(
            id = group.id,
            name = group.name,
            description = group.description,
            character_ids = group.characterIds.joinToString(","),
            group_type = group.groupType.name,
            book_ids = group.bookIds.joinToString(","),
            image_url = group.imageUrl,
            created_at = group.createdAt
        )
        return group
    }
    
    override suspend fun deleteGroup(groupId: String) {
        queries.deleteGroup(groupId)
    }
    
    override suspend fun saveAppearance(appearance: CharacterAppearance) {
        queries.insertAppearance(
            id = appearance.id,
            character_id = appearance.characterId,
            book_id = appearance.bookId,
            chapter_id = appearance.chapterId,
            chapter_title = appearance.chapterTitle,
            paragraph_index = appearance.paragraphIndex.toLong(),
            text_snippet = appearance.textSnippet,
            appearance_type = appearance.appearanceType.name,
            timestamp = appearance.timestamp
        )
    }
    
    override suspend fun getAppearances(characterId: String, bookId: Long?): List<CharacterAppearance> {
        return if (bookId != null) {
            queries.selectAppearancesByCharacterAndBook(characterId, bookId)
                .executeAsList()
                .map { it.toDomain() }
        } else {
            queries.selectAppearancesByCharacter(characterId)
                .executeAsList()
                .map { it.toDomain() }
        }
    }
    
    override suspend fun saveNote(note: CharacterNote): CharacterNote {
        queries.insertNote(
            id = note.id,
            character_id = note.characterId,
            content = note.content,
            book_id = note.bookId,
            chapter_id = note.chapterId,
            note_type = note.noteType.name,
            created_at = note.createdAt,
            updated_at = note.updatedAt
        )
        return note
    }
    
    override suspend fun getNotes(characterId: String): List<CharacterNote> {
        return queries.selectNotesByCharacter(characterId)
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun saveTimelineEvent(event: CharacterTimelineEvent) {
        queries.insertTimelineEvent(
            id = event.id,
            character_id = event.characterId,
            book_id = event.bookId,
            chapter_id = event.chapterId,
            event_type = event.eventType.name,
            title = event.title,
            description = event.description,
            order_index = event.orderIndex.toLong(),
            timestamp = event.timestamp
        )
    }
    
    override suspend fun getTimeline(characterId: String): List<CharacterTimelineEvent> {
        return queries.selectTimelineByCharacter(characterId)
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getCharacterStats(characterId: String): CharacterStats? {
        val appearances = getAppearances(characterId, null)
        if (appearances.isEmpty()) return null
        
        val dialogueCount = appearances.count { it.appearanceType == AppearanceType.DIALOGUE }
        val chapters = appearances.map { it.chapterId }.distinct()
        
        return CharacterStats(
            characterId = characterId,
            totalAppearances = appearances.size,
            dialogueCount = dialogueCount,
            chaptersAppearedIn = chapters.size,
            relationshipCount = queries.selectRelationshipsByCharacter(characterId, characterId)
                .executeAsList().size,
            averageAppearancesPerChapter = if (chapters.isNotEmpty()) 
                appearances.size.toFloat() / chapters.size else 0f,
            mostCommonAppearanceType = appearances
                .groupBy { it.appearanceType }
                .maxByOrNull { it.value.size }?.key ?: AppearanceType.REFERENCE,
            firstAppearanceChapter = appearances.minOfOrNull { it.chapterId.toInt() } ?: 0,
            lastAppearanceChapter = appearances.maxOfOrNull { it.chapterId.toInt() } ?: 0
        )
    }
    
    // Extension functions to convert database entities to domain models
    private fun data.Character.toDomain() = Character(
        id = id,
        name = name,
        aliases = if (aliases.isBlank()) emptyList() else aliases.split(","),
        description = description,
        imageUrl = image_url,
        role = CharacterRole.valueOf(role),
        traits = if (traits.isBlank()) emptyList() else traits.split(","),
        tags = if (tags.isBlank()) emptyList() else tags.split(","),
        bookIds = if (book_ids.isBlank()) emptyList() else book_ids.split(",").map { it.toLong() },
        seriesId = series_id,
        firstAppearance = null,
        createdAt = created_at,
        updatedAt = updated_at,
        isUserCreated = is_user_created,
        confidence = confidence.toFloat()
    )
    
    private fun data.Character_relationship.toDomain() = CharacterRelationship(
        id = id,
        character1Id = character1_id,
        character2Id = character2_id,
        relationshipType = RelationshipType.valueOf(relationship_type),
        customType = custom_type,
        description = description,
        strength = strength.toFloat(),
        isSymmetric = is_symmetric,
        bookIds = if (book_ids.isBlank()) emptyList() else book_ids.split(",").map { it.toLong() },
        createdAt = created_at,
        updatedAt = updated_at
    )
    
    private fun data.Character_group.toDomain() = CharacterGroup(
        id = id,
        name = name,
        description = description,
        characterIds = if (character_ids.isBlank()) emptyList() else character_ids.split(","),
        groupType = GroupType.valueOf(group_type),
        bookIds = if (book_ids.isBlank()) emptyList() else book_ids.split(",").map { it.toLong() },
        imageUrl = image_url,
        createdAt = created_at
    )
    
    private fun data.Character_appearance.toDomain() = CharacterAppearance(
        id = id,
        characterId = character_id,
        bookId = book_id,
        chapterId = chapter_id,
        chapterTitle = chapter_title,
        paragraphIndex = paragraph_index.toInt(),
        textSnippet = text_snippet,
        appearanceType = AppearanceType.valueOf(appearance_type),
        timestamp = timestamp
    )
    
    private fun data.Character_note.toDomain() = CharacterNote(
        id = id,
        characterId = character_id,
        content = content,
        bookId = book_id,
        chapterId = chapter_id,
        noteType = NoteType.valueOf(note_type),
        createdAt = created_at,
        updatedAt = updated_at
    )
    
    private fun data.Character_timeline_event.toDomain() = CharacterTimelineEvent(
        id = id,
        characterId = character_id,
        bookId = book_id,
        chapterId = chapter_id,
        eventType = TimelineEventType.valueOf(event_type),
        title = title,
        description = description,
        orderIndex = order_index.toInt(),
        timestamp = timestamp
    )
}

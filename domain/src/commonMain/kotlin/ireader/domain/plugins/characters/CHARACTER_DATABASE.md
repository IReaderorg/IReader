# Character Database Plugin System

Track characters across books with relationships, appearances, and notes.

## Features

- **Character Glossary**: Per-book character list with descriptions
- **Cross-Book Tracking**: Track characters across series
- **Relationship Mapping**: Define and visualize character relationships
- **Appearance Tracking**: Record where characters appear
- **Timeline Events**: Track character development
- **AI Detection**: Automatically detect characters using AI plugins
- **Notes & Annotations**: Add personal notes to characters

## Usage

### Basic Character Management

```kotlin
// Get characters for a book
val characters = characterManager.loadCharactersForBook(bookId)

// Create a character
val character = Character(
    id = "char_123",
    name = "John Smith",
    aliases = listOf("Johnny", "The Hero"),
    description = "The main protagonist",
    role = CharacterRole.PROTAGONIST,
    traits = listOf("brave", "loyal"),
    bookIds = listOf(bookId),
    createdAt = currentTimeToLong(),
    updatedAt = currentTimeToLong(),
    isUserCreated = true
)
characterManager.createCharacter(character)

// Update character
characterManager.updateCharacter(character.copy(
    description = "Updated description"
))
```

### Relationships

```kotlin
// Create a relationship
val relationship = CharacterRelationship(
    id = "rel_123",
    character1Id = "char_1",
    character2Id = "char_2",
    relationshipType = RelationshipType.FRIEND,
    description = "Best friends since childhood",
    bookIds = listOf(bookId),
    createdAt = currentTimeToLong(),
    updatedAt = currentTimeToLong()
)
characterManager.createRelationship(relationship)

// Get relationships for a character
val relationships = characterManager.getRelationshipsForCharacter("char_1")
```

### Appearance Tracking

```kotlin
// Record an appearance
val appearance = CharacterAppearance(
    id = "app_123",
    characterId = "char_1",
    bookId = bookId,
    chapterId = chapterId,
    chapterTitle = "Chapter 1",
    paragraphIndex = 5,
    textSnippet = "John walked into the room...",
    appearanceType = AppearanceType.FIRST_MENTION,
    timestamp = currentTimeToLong()
)
characterManager.recordAppearance(appearance)

// Get appearances
val appearances = characterManager.getAppearances("char_1", bookId)
```

### AI Detection

```kotlin
// Detect characters using AI
val detected = characterManager.detectCharacters(bookId)

// Import detected characters
detected.onSuccess { characters ->
    characterManager.importDetectedCharacters(bookId, characters)
}
```

### Notes

```kotlin
// Add a note
val note = CharacterNote(
    id = "note_123",
    characterId = "char_1",
    content = "This character reminds me of...",
    noteType = NoteType.GENERAL,
    createdAt = currentTimeToLong(),
    updatedAt = currentTimeToLong()
)
characterManager.addNote(note)
```

### Timeline

```kotlin
// Add timeline event
val event = CharacterTimelineEvent(
    id = "event_123",
    characterId = "char_1",
    bookId = bookId,
    chapterId = chapterId,
    eventType = TimelineEventType.MAJOR_EVENT,
    title = "Discovers the truth",
    description = "Character learns about their past",
    orderIndex = 1,
    timestamp = currentTimeToLong()
)
characterManager.addTimelineEvent(event)
```

### Statistics

```kotlin
// Get character stats
val stats = characterManager.getCharacterStats("char_1")
// Returns: totalAppearances, dialogueCount, chaptersAppearedIn, etc.

// Get book summary
val summary = characterManager.getBookCharacterSummary(bookId)
// Returns: totalCharacters, protagonists, antagonists, etc.
```

## Plugin API

Plugins can access the character database through `CharacterDatabaseApi`:

```kotlin
class MyPlugin : FeaturePlugin {
    private lateinit var characterApi: CharacterDatabaseApi
    
    override fun initialize(context: PluginContext) {
        characterApi = context.getApi(CharacterDatabaseApi::class)
    }
    
    suspend fun showCharacterInfo(characterId: String) {
        val character = characterApi.getCharacter(characterId)
        val relationships = characterApi.getRelationships(characterId)
        val stats = characterApi.getCharacterStats(characterId)
        // Display info...
    }
}
```

## Data Models

### CharacterRole
- PROTAGONIST
- ANTAGONIST
- DEUTERAGONIST
- SUPPORTING
- MINOR
- MENTIONED
- UNKNOWN

### RelationshipType
- FAMILY
- FRIEND
- ENEMY
- LOVER
- MENTOR
- STUDENT
- COLLEAGUE
- RIVAL
- ALLY
- SERVANT
- MASTER
- CUSTOM
- UNKNOWN

### AppearanceType
- FIRST_MENTION
- DIALOGUE
- ACTION
- DESCRIPTION
- REFERENCE

### NoteType
- GENERAL
- SPOILER
- THEORY
- QUESTION
- TIMELINE

### TimelineEventType
- INTRODUCTION
- MAJOR_EVENT
- DEVELOPMENT
- RELATIONSHIP_CHANGE
- DEATH
- TRANSFORMATION
- REVELATION
- CUSTOM

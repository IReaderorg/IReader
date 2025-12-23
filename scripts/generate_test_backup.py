#!/usr/bin/env python3
"""
Script to generate a test backup file with 10,000+ books for IReader performance testing.

Usage:
    python generate_test_backup.py [book_count]
    
Example:
    python generate_test_backup.py 10000
    
Output: test_backup_10000.gz (gzip compressed protobuf format)

Note: IReader expects .gz (gzip) or .json backup files
"""

import struct
import random
import time
import sys
import gzip
from io import BytesIO

# Protobuf wire types
WIRE_VARINT = 0
WIRE_FIXED64 = 1
WIRE_LENGTH_DELIMITED = 2
WIRE_FIXED32 = 5

def encode_varint(value):
    """Encode an integer as a varint."""
    bits = value & 0x7f
    value >>= 7
    result = b''
    while value:
        result += bytes([0x80 | bits])
        bits = value & 0x7f
        value >>= 7
    result += bytes([bits])
    return result

def encode_signed_varint(value):
    """Encode a signed integer using zigzag encoding."""
    if value >= 0:
        return encode_varint(value * 2)
    else:
        return encode_varint(-value * 2 - 1)

def encode_string(value):
    """Encode a string as length-delimited bytes."""
    encoded = value.encode('utf-8')
    return encode_varint(len(encoded)) + encoded

def encode_bytes(value):
    """Encode bytes as length-delimited."""
    return encode_varint(len(value)) + value

def encode_field(field_number, wire_type, value):
    """Encode a protobuf field."""
    tag = (field_number << 3) | wire_type
    return encode_varint(tag) + value

def encode_float(value):
    """Encode a float as fixed32."""
    return struct.pack('<f', value)

def encode_chapter(chapter_data):
    """
    Encode a ChapterProto message.
    
    Fields:
    1: key (string)
    2: name (string)
    3: translator (string)
    4: read (bool/varint)
    5: bookmark (bool/varint)
    6: dateFetch (int64/varint)
    7: dateUpload (int64/varint)
    8: number (float)
    9: sourceOrder (int64/varint)
    10: content (string)
    11: type (int64/varint)
    12: lastPageRead (int64/varint)
    """
    result = b''
    
    # 1: key
    result += encode_field(1, WIRE_LENGTH_DELIMITED, encode_string(chapter_data['key']))
    # 2: name
    result += encode_field(2, WIRE_LENGTH_DELIMITED, encode_string(chapter_data['name']))
    # 3: translator (optional)
    if chapter_data.get('translator'):
        result += encode_field(3, WIRE_LENGTH_DELIMITED, encode_string(chapter_data['translator']))
    # 4: read
    if chapter_data.get('read'):
        result += encode_field(4, WIRE_VARINT, encode_varint(1))
    # 5: bookmark
    if chapter_data.get('bookmark'):
        result += encode_field(5, WIRE_VARINT, encode_varint(1))
    # 6: dateFetch
    if chapter_data.get('dateFetch', 0) > 0:
        result += encode_field(6, WIRE_VARINT, encode_varint(chapter_data['dateFetch']))
    # 7: dateUpload
    if chapter_data.get('dateUpload', 0) > 0:
        result += encode_field(7, WIRE_VARINT, encode_varint(chapter_data['dateUpload']))
    # 8: number (float)
    if chapter_data.get('number', 0) > 0:
        result += encode_field(8, WIRE_FIXED32, encode_float(chapter_data['number']))
    # 9: sourceOrder
    if chapter_data.get('sourceOrder', 0) > 0:
        result += encode_field(9, WIRE_VARINT, encode_varint(chapter_data['sourceOrder']))
    # 10: content (skip to save space)
    # 11: type
    if chapter_data.get('type', 0) > 0:
        result += encode_field(11, WIRE_VARINT, encode_varint(chapter_data['type']))
    # 12: lastPageRead
    if chapter_data.get('lastPageRead', 0) > 0:
        result += encode_field(12, WIRE_VARINT, encode_varint(chapter_data['lastPageRead']))
    
    return result

def encode_history(history_data):
    """
    Encode a HistoryProto message.
    
    Fields:
    1: bookId (int64)
    2: chapterId (int64)
    3: readAt (int64)
    4: progress (int64)
    """
    result = b''
    result += encode_field(1, WIRE_VARINT, encode_varint(history_data['bookId']))
    result += encode_field(2, WIRE_VARINT, encode_varint(history_data['chapterId']))
    result += encode_field(3, WIRE_VARINT, encode_varint(history_data['readAt']))
    if history_data.get('progress', 0) > 0:
        result += encode_field(4, WIRE_VARINT, encode_varint(history_data['progress']))
    return result

def encode_book(book_data):
    """
    Encode a BookProto message.
    
    Fields:
    1: sourceId (int64)
    2: key (string)
    3: title (string)
    4: author (string)
    5: description (string)
    6: genres (repeated string)
    7: status (int64)
    8: cover (string)
    9: customCover (string)
    10: lastUpdate (int64)
    11: initialized (bool)
    12: dateAdded (int64)
    13: viewer (int64)
    14: flags (int64)
    15: chapters (repeated ChapterProto)
    16: categories (repeated int64)
    17: tracks (repeated TrackProto)
    18: histories (repeated HistoryProto)
    """
    result = b''
    
    # 1: sourceId
    result += encode_field(1, WIRE_VARINT, encode_varint(book_data['sourceId']))
    # 2: key
    result += encode_field(2, WIRE_LENGTH_DELIMITED, encode_string(book_data['key']))
    # 3: title
    result += encode_field(3, WIRE_LENGTH_DELIMITED, encode_string(book_data['title']))
    # 4: author
    if book_data.get('author'):
        result += encode_field(4, WIRE_LENGTH_DELIMITED, encode_string(book_data['author']))
    # 5: description
    if book_data.get('description'):
        result += encode_field(5, WIRE_LENGTH_DELIMITED, encode_string(book_data['description']))
    # 6: genres (repeated)
    for genre in book_data.get('genres', []):
        result += encode_field(6, WIRE_LENGTH_DELIMITED, encode_string(genre))
    # 7: status
    if book_data.get('status', 0) > 0:
        result += encode_field(7, WIRE_VARINT, encode_varint(book_data['status']))
    # 8: cover
    if book_data.get('cover'):
        result += encode_field(8, WIRE_LENGTH_DELIMITED, encode_string(book_data['cover']))
    # 9: customCover (skip)
    # 10: lastUpdate
    if book_data.get('lastUpdate', 0) > 0:
        result += encode_field(10, WIRE_VARINT, encode_varint(book_data['lastUpdate']))
    # 11: initialized
    if book_data.get('initialized'):
        result += encode_field(11, WIRE_VARINT, encode_varint(1))
    # 12: dateAdded
    if book_data.get('dateAdded', 0) > 0:
        result += encode_field(12, WIRE_VARINT, encode_varint(book_data['dateAdded']))
    # 13: viewer (skip)
    # 14: flags (skip)
    # 15: chapters (repeated)
    for chapter in book_data.get('chapters', []):
        chapter_bytes = encode_chapter(chapter)
        result += encode_field(15, WIRE_LENGTH_DELIMITED, encode_bytes(chapter_bytes))
    # 16: categories (repeated)
    for cat_id in book_data.get('categories', []):
        result += encode_field(16, WIRE_VARINT, encode_varint(cat_id))
    # 17: tracks (skip)
    # 18: histories (repeated)
    for history in book_data.get('histories', []):
        history_bytes = encode_history(history)
        result += encode_field(18, WIRE_LENGTH_DELIMITED, encode_bytes(history_bytes))
    
    return result

def encode_category(category_data):
    """
    Encode a CategoryProto message.
    
    Fields:
    1: name (string)
    2: order (int64)
    3: updateInterval (int32)
    4: flags (int64)
    """
    result = b''
    result += encode_field(1, WIRE_LENGTH_DELIMITED, encode_string(category_data['name']))
    result += encode_field(2, WIRE_VARINT, encode_varint(category_data['order']))
    if category_data.get('updateInterval', 0) > 0:
        result += encode_field(3, WIRE_VARINT, encode_varint(category_data['updateInterval']))
    if category_data.get('flags', 0) > 0:
        result += encode_field(4, WIRE_VARINT, encode_varint(category_data['flags']))
    return result

def encode_backup(backup_data):
    """
    Encode a Backup message.
    
    Fields:
    1: library (repeated BookProto)
    2: categories (repeated CategoryProto)
    """
    result = b''
    
    # 1: library (repeated)
    for book in backup_data['library']:
        book_bytes = encode_book(book)
        result += encode_field(1, WIRE_LENGTH_DELIMITED, encode_bytes(book_bytes))
    
    # 2: categories (repeated)
    for category in backup_data['categories']:
        category_bytes = encode_category(category)
        result += encode_field(2, WIRE_LENGTH_DELIMITED, encode_bytes(category_bytes))
    
    return result

# Sample data for realistic book generation
GENRES = [
    "Fantasy", "Romance", "Action", "Adventure", "Comedy", "Drama", "Horror",
    "Mystery", "Sci-Fi", "Slice of Life", "Supernatural", "Thriller", "Historical",
    "Martial Arts", "School Life", "Sports", "Tragedy", "Psychological", "Seinen",
    "Shounen", "Shoujo", "Josei", "Isekai", "Harem", "Mecha", "Wuxia", "Xianxia"
]

TITLE_PREFIXES = [
    "The", "A", "My", "Our", "Your", "His", "Her", "Their", "This", "That",
    "One", "Last", "First", "Final", "Ultimate", "Supreme", "Divine", "Eternal",
    "Infinite", "Legendary", "Epic", "Grand", "Great", "True", "Real", "Ancient"
]

TITLE_NOUNS = [
    "Hero", "King", "Queen", "Prince", "Princess", "Knight", "Mage", "Wizard",
    "Dragon", "Phoenix", "Wolf", "Tiger", "Lion", "Eagle", "Sword", "Shield",
    "Crown", "Throne", "Kingdom", "Empire", "World", "Universe", "Realm", "Domain",
    "Path", "Way", "Road", "Journey", "Adventure", "Quest", "Legend", "Myth",
    "Story", "Tale", "Chronicle", "Saga", "Epic", "Novel", "Cultivator", "Immortal"
]

TITLE_SUFFIXES = [
    "of Destiny", "of Fate", "of Power", "of Glory", "of Honor", "of Love",
    "of War", "of Peace", "of Light", "of Darkness", "of Fire", "of Ice",
    "of Thunder", "of Wind", "of Earth", "of Water", "of Life", "of Death",
    "Reborn", "Awakened", "Ascended", "Transcended", "Evolved", "Transformed",
    "Returns", "Rises", "Falls", "Begins", "Ends", "Continues", "System"
]

AUTHOR_FIRST_NAMES = [
    "John", "Jane", "Michael", "Sarah", "David", "Emily", "James", "Emma",
    "Robert", "Olivia", "William", "Sophia", "Richard", "Isabella", "Joseph",
    "Mia", "Thomas", "Charlotte", "Charles", "Amelia", "Daniel", "Harper",
    "Yuki", "Sakura", "Takeshi", "Haruki", "Kenji", "Akira", "Ryu", "Hana",
    "Wei", "Ming", "Chen", "Li", "Zhang", "Wang", "Liu", "Yang"
]

AUTHOR_LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
    "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
    "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
    "Tanaka", "Yamamoto", "Suzuki", "Watanabe", "Sato", "Nakamura", "Kobayashi",
    "Xiao", "Feng", "Huang", "Zhou", "Wu", "Xu", "Sun", "Ma"
]

SOURCES = [
    (1, "Novel Updates"),
    (2, "Royal Road"),
    (3, "Webnovel"),
    (4, "Wuxiaworld"),
    (5, "Light Novel Pub"),
    (6, "Novel Full"),
    (7, "Read Light Novel"),
    (8, "Box Novel"),
    (9, "Novel Bin"),
    (10, "All Novel Full")
]

def generate_title():
    """Generate a random book title."""
    parts = []
    if random.random() > 0.3:
        parts.append(random.choice(TITLE_PREFIXES))
    parts.append(random.choice(TITLE_NOUNS))
    if random.random() > 0.4:
        parts.append(random.choice(TITLE_SUFFIXES))
    return " ".join(parts)

def generate_author():
    """Generate a random author name."""
    return f"{random.choice(AUTHOR_FIRST_NAMES)} {random.choice(AUTHOR_LAST_NAMES)}"

def generate_description():
    """Generate a random book description."""
    templates = [
        "In a world where {0} rules, one {1} must rise to challenge the {2} and restore {3} to the land.",
        "Follow the journey of {0} as they discover their hidden {1} and embark on an epic {2}.",
        "When {0} threatens the kingdom, only the chosen {1} can wield the ancient {2} to save everyone.",
        "A tale of {0}, {1}, and the unbreakable bonds of {2} that transcend time itself.",
        "After being betrayed by {0}, our hero seeks {1} and discovers a power beyond {2}.",
        "In the realm of {0}, where {1} is everything, one person dares to challenge {2}.",
        "The story of a young {0} who dreams of becoming the greatest {1} in all of {2}."
    ]
    
    words = ["power", "destiny", "fate", "love", "hatred", "courage", "wisdom",
             "strength", "magic", "darkness", "light", "hope", "despair", 
             "friendship", "betrayal", "cultivation", "immortality"]
    
    template = random.choice(templates)
    return template.format(random.choice(words), random.choice(words), 
                          random.choice(words), random.choice(words))

CHAPTER_TITLES = [
    "The Beginning", "A New Dawn", "Awakening", "First Steps", "The Journey Begins",
    "Unexpected Encounter", "Hidden Truth", "Rising Storm", "Dark Clouds", "Light in Darkness",
    "The Challenge", "Trial by Fire", "Breaking Through", "New Power", "Revelation",
    "Betrayal", "Alliance", "The Hunt", "Escape", "Confrontation",
    "Battle Begins", "Turning Point", "Victory", "Defeat", "Recovery",
    "Training Arc", "New Technique", "Master's Teaching", "Breakthrough", "Advancement",
    "Secret Realm", "Ancient Ruins", "Hidden Treasure", "Dangerous Path", "Final Test",
    "Tournament Begins", "First Round", "Semifinals", "Finals", "Champion",
    "Return Home", "Family Reunion", "Old Friends", "New Enemies", "Preparation",
    "War Declaration", "Army Gathering", "March to Battle", "Siege", "Last Stand",
    "Sacrifice", "Miracle", "Rebirth", "Transcendence", "Epilogue"
]

VOLUME_NAMES = [
    "Prologue", "Book One: Origins", "Book Two: Rising", "Book Three: Conflict",
    "Book Four: Resolution", "Interlude", "Side Story", "Bonus Chapter"
]

def generate_chapters(book_id, count, read_percentage=0.5):
    """Generate realistic chapters for a book with proper reading progress."""
    now = int(time.time() * 1000)
    # Book was added 30-365 days ago
    book_age_days = random.randint(30, 365)
    book_added_time = now - (book_age_days * 86400000)
    
    # Chapters uploaded over time (older chapters first)
    chapter_interval = (book_age_days * 86400000) // max(count, 1)
    
    chapters = []
    
    # Determine how many chapters have been read (continuous from start)
    chapters_read = int(count * read_percentage * random.uniform(0.8, 1.2))
    chapters_read = min(chapters_read, count)
    
    # Last read chapter (where user stopped)
    last_read_chapter = chapters_read
    
    for i in range(1, count + 1):
        # Chapter upload time (spread over book's lifetime)
        upload_time = book_added_time + (i * chapter_interval)
        # Fetch time is slightly after upload
        fetch_time = upload_time + random.randint(0, 3600000)  # 0-1 hour after upload
        
        # Reading status
        is_read = i <= chapters_read
        is_current = i == last_read_chapter
        
        # Generate chapter name
        if i <= len(CHAPTER_TITLES):
            chapter_title = CHAPTER_TITLES[i - 1]
        else:
            chapter_title = f"{random.choice(TITLE_NOUNS)} {random.choice(TITLE_SUFFIXES)}"
        
        # Add volume prefix occasionally
        volume_num = (i - 1) // 50 + 1  # New volume every 50 chapters
        if count > 50 and i % 50 == 1:
            chapter_name = f"Volume {volume_num} - Chapter {i}: {chapter_title}"
        else:
            chapter_name = f"Chapter {i}: {chapter_title}"
        
        chapters.append({
            'key': f"/novel/{book_id}/chapter-{i}",
            'name': chapter_name,
            'translator': generate_author() if random.random() > 0.85 else "",
            'read': is_read,
            'bookmark': random.random() > 0.97,  # ~3% bookmarked
            'dateFetch': fetch_time,
            'dateUpload': upload_time,
            'number': float(i),
            'sourceOrder': i,
            'type': 0,
            'lastPageRead': random.randint(500, 2000) if is_current else (0 if not is_read else random.randint(1000, 3000))
        })
    
    return chapters, last_read_chapter

def generate_book(book_id, category_ids, reading_status='random'):
    """
    Generate a single book with realistic chapters, history, and updates.
    
    reading_status options:
    - 'reading': Currently reading (30-80% progress, recent history)
    - 'completed': Finished reading (100% progress)
    - 'on_hold': Paused (10-50% progress, old history)
    - 'plan_to_read': Not started (0% progress, no history)
    - 'dropped': Abandoned (5-30% progress, old history)
    - 'random': Random status
    """
    source = random.choice(SOURCES)
    now = int(time.time() * 1000)
    
    # Determine reading status if random
    if reading_status == 'random':
        status_weights = [
            ('reading', 0.25),      # 25% currently reading
            ('completed', 0.20),    # 20% completed
            ('on_hold', 0.15),      # 15% on hold
            ('plan_to_read', 0.25), # 25% plan to read
            ('dropped', 0.15),      # 15% dropped
        ]
        r = random.random()
        cumulative = 0
        for status, weight in status_weights:
            cumulative += weight
            if r <= cumulative:
                reading_status = status
                break
    
    # Chapter count varies by status
    if reading_status == 'completed':
        chapter_count = random.randint(50, 300)  # Completed books tend to be longer
    elif reading_status == 'reading':
        chapter_count = random.randint(30, 500)  # Ongoing books vary
    elif reading_status == 'plan_to_read':
        chapter_count = random.randint(10, 200)
    else:
        chapter_count = random.randint(20, 150)
    
    # Reading progress based on status
    if reading_status == 'completed':
        read_percentage = 1.0
    elif reading_status == 'reading':
        read_percentage = random.uniform(0.3, 0.85)
    elif reading_status == 'on_hold':
        read_percentage = random.uniform(0.1, 0.5)
    elif reading_status == 'dropped':
        read_percentage = random.uniform(0.05, 0.3)
    else:  # plan_to_read
        read_percentage = 0.0
    
    # Generate chapters with proper reading progress
    chapters, last_read_chapter = generate_chapters(book_id, chapter_count, read_percentage)
    
    # Book metadata
    book_genres = random.sample(GENRES, random.randint(1, 5))
    
    # Assign to appropriate category based on reading status
    assigned_categories = []
    if category_ids:
        # Map reading status to category order
        status_to_category = {
            'reading': 1,      # "Reading"
            'completed': 2,    # "Completed"
            'on_hold': 3,      # "On Hold"
            'plan_to_read': 4, # "Plan to Read"
            'dropped': 5,      # "Dropped"
        }
        primary_category = status_to_category.get(reading_status, 1)
        if primary_category in category_ids:
            assigned_categories.append(primary_category)
        
        # Maybe add to Favorites (10% chance for reading/completed)
        if reading_status in ['reading', 'completed'] and random.random() > 0.9:
            if 6 in category_ids:  # Favorites
                assigned_categories.append(6)
        
        # Add language category (30% chance)
        if random.random() > 0.7:
            lang_cat = random.choice([8, 9, 10])  # Korean, Chinese, Japanese
            if lang_cat in category_ids:
                assigned_categories.append(lang_cat)
    
    # Book status (publication status, not reading status)
    # 0=Unknown, 1=Ongoing, 2=Completed, 3=Licensed, 4=Publishing Finished, 5=Cancelled, 6=On Hiatus
    if reading_status == 'completed':
        pub_status = random.choice([2, 4])  # Completed or Publishing Finished
    else:
        pub_status = random.choices([1, 2, 6], weights=[0.6, 0.3, 0.1])[0]  # Mostly ongoing
    
    # Dates
    book_age_days = random.randint(7, 365)
    date_added = now - (book_age_days * 86400000)
    
    # Last update (when new chapters were added)
    if pub_status == 1:  # Ongoing
        # Recent update for ongoing books
        last_update = now - random.randint(0, 7 * 86400000)  # Within last week
    else:
        # Older update for completed/hiatus
        last_update = now - random.randint(30, 180) * 86400000
    
    # Generate reading history
    histories = []
    if reading_status != 'plan_to_read' and last_read_chapter > 0:
        # When was this book last read?
        if reading_status == 'reading':
            # Recently read (within last 7 days)
            last_read_time = now - random.randint(0, 7 * 86400000)
        elif reading_status == 'completed':
            # Finished sometime in the past
            last_read_time = now - random.randint(1, 60) * 86400000
        elif reading_status == 'on_hold':
            # Not read recently (2-8 weeks ago)
            last_read_time = now - random.randint(14, 56) * 86400000
        else:  # dropped
            # Long time ago (1-6 months)
            last_read_time = now - random.randint(30, 180) * 86400000
        
        # Add history entry for last read chapter
        histories.append({
            'bookId': book_id,
            'chapterId': last_read_chapter,
            'readAt': last_read_time,
            'progress': random.randint(80, 100) if reading_status == 'completed' else random.randint(20, 95)
        })
        
        # Maybe add more history entries (for recently read books)
        if reading_status == 'reading' and random.random() > 0.5:
            # Add a few more recent history entries
            for _ in range(random.randint(1, 3)):
                older_chapter = max(1, last_read_chapter - random.randint(1, 10))
                older_time = last_read_time - random.randint(1, 7) * 86400000
                histories.append({
                    'bookId': book_id,
                    'chapterId': older_chapter,
                    'readAt': older_time,
                    'progress': random.randint(50, 100)
                })
    
    return {
        'sourceId': source[0],
        'key': f"/novel/{book_id}",
        'title': generate_title(),
        'author': generate_author(),
        'description': generate_description(),
        'genres': book_genres,
        'status': pub_status,
        'cover': f"https://picsum.photos/seed/{book_id}/300/400",
        'lastUpdate': last_update,
        'initialized': True,
        'dateAdded': date_added,
        'chapters': chapters,
        'categories': assigned_categories,
        'histories': histories,
        '_reading_status': reading_status,  # For stats (not serialized)
        '_chapters_read': last_read_chapter,
        '_total_chapters': chapter_count,
    }

def generate_categories():
    """Generate default categories."""
    return [
        {'name': "Reading", 'order': 1, 'updateInterval': 0, 'flags': 0},
        {'name': "Completed", 'order': 2, 'updateInterval': 0, 'flags': 0},
        {'name': "On Hold", 'order': 3, 'updateInterval': 0, 'flags': 0},
        {'name': "Plan to Read", 'order': 4, 'updateInterval': 0, 'flags': 0},
        {'name': "Dropped", 'order': 5, 'updateInterval': 0, 'flags': 0},
        {'name': "Favorites", 'order': 6, 'updateInterval': 0, 'flags': 0},
        {'name': "Re-reading", 'order': 7, 'updateInterval': 0, 'flags': 0},
        {'name': "Korean", 'order': 8, 'updateInterval': 0, 'flags': 0},
        {'name': "Chinese", 'order': 9, 'updateInterval': 0, 'flags': 0},
        {'name': "Japanese", 'order': 10, 'updateInterval': 0, 'flags': 0},
    ]

def main():
    book_count = 10500
    if len(sys.argv) > 1:
        try:
            book_count = int(sys.argv[1])
        except ValueError:
            print(f"Invalid book count: {sys.argv[1]}, using default: {book_count}")
    
    # Use .bin extension for uncompressed protobuf (the app's restoreFromBytes doesn't decompress)
    # Note: The file picker accepts .gz and .json, so we'll use .gz but store uncompressed data
    # Actually, let's create both versions
    output_file_gz = f"test_backup_{book_count}.gz"
    output_file_bin = f"test_backup_{book_count}.bin"
    
    print(f"Generating test backup with {book_count} books...")
    print("This may take a few minutes...")
    
    categories = generate_categories()
    category_ids = [cat['order'] for cat in categories]
    
    books = []
    stats = {
        'reading': 0,
        'completed': 0,
        'on_hold': 0,
        'plan_to_read': 0,
        'dropped': 0,
    }
    total_chapters_read = 0
    total_chapters = 0
    books_with_history = 0
    
    start_time = time.time()
    
    for i in range(1, book_count + 1):
        book = generate_book(i, category_ids)
        
        # Track stats
        status = book.pop('_reading_status', 'unknown')
        chapters_read = book.pop('_chapters_read', 0)
        book_chapters = book.pop('_total_chapters', 0)
        
        stats[status] = stats.get(status, 0) + 1
        total_chapters_read += chapters_read
        total_chapters += book_chapters
        if book['histories']:
            books_with_history += 1
        
        books.append(book)
        
        if i % 1000 == 0:
            elapsed = time.time() - start_time
            rate = i / elapsed
            remaining = (book_count - i) / rate
            print(f"Generated {i} / {book_count} books... ({rate:.0f} books/sec, ~{remaining:.0f}s remaining)")
    
    print("\nCreating backup structure...")
    backup = {
        'library': books,
        'categories': categories
    }
    
    print("Serializing to protobuf...")
    data = encode_backup(backup)
    
    # Save uncompressed version (for restoreFromBytes which doesn't decompress)
    print(f"Writing uncompressed to: {output_file_bin}")
    with open(output_file_bin, 'wb') as f:
        f.write(data)
    
    # Also save gzip compressed version (for restoreFrom which uses FileSaver.read with gzip)
    print("Compressing with gzip...")
    compressed_data = gzip.compress(data, compresslevel=6)
    
    print(f"Writing compressed to: {output_file_gz}")
    with open(output_file_gz, 'wb') as f:
        f.write(compressed_data)
    
    import os
    file_size_mb = os.path.getsize(output_file_gz) / (1024 * 1024)
    uncompressed_mb = len(data) / (1024 * 1024)
    
    actual_total_chapters = sum(len(book['chapters']) for book in books)
    
    print(f"\n{'='*50}")
    print(f"BACKUP GENERATED SUCCESSFULLY!")
    print(f"{'='*50}")
    print(f"Compressed file: {output_file_gz} ({file_size_mb:.2f} MB)")
    print(f"Uncompressed file: {output_file_bin} ({uncompressed_mb:.2f} MB)")
    print(f"Time: {time.time() - start_time:.1f} seconds")
    print(f"\n--- Library Stats ---")
    print(f"Total Books: {book_count}")
    print(f"Total Chapters: {actual_total_chapters:,}")
    print(f"Avg Chapters/Book: {actual_total_chapters // book_count}")
    print(f"\n--- Reading Status ---")
    print(f"  Currently Reading: {stats['reading']:,} ({stats['reading']*100//book_count}%)")
    print(f"  Completed: {stats['completed']:,} ({stats['completed']*100//book_count}%)")
    print(f"  On Hold: {stats['on_hold']:,} ({stats['on_hold']*100//book_count}%)")
    print(f"  Plan to Read: {stats['plan_to_read']:,} ({stats['plan_to_read']*100//book_count}%)")
    print(f"  Dropped: {stats['dropped']:,} ({stats['dropped']*100//book_count}%)")
    print(f"\n--- Progress ---")
    print(f"  Chapters Read: {total_chapters_read:,} / {total_chapters:,} ({total_chapters_read*100//max(total_chapters,1)}%)")
    print(f"  Books with History: {books_with_history:,} ({books_with_history*100//book_count}%)")
    print(f"\n--- Categories ---")
    for cat in categories:
        print(f"  {cat['name']}")
    print(f"{'='*50}")
    print(f"\nIMPORTANT: The app has a bug where restoreFromBytes doesn't decompress gzip.")
    print(f"Use the .bin file if restoring via the new file picker UI.")
    print(f"Use the .gz file if restoring via the original backup restore method.")

if __name__ == "__main__":
    main()

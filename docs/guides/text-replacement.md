# Text Replacement Feature Guide

## Overview

The Text Replacement feature allows you to automatically replace text patterns in your reading content. This is useful for:
- Fixing common typos or formatting issues
- Removing unwanted text (ads, navigation hints, etc.)
- Customizing your reading experience

## Important Note About TTS (Text-to-Speech)

When using the TTS feature:
- **Brackets and their content are NOT read aloud** by the TTS engine
- **You still see the full text with brackets** in the reader screen
- This is intentional to skip translator notes, annotations, and other bracketed content

Examples of what TTS will skip:
- `(translator note)` - Round brackets with notes
- `[TL: translation]` - Square brackets with translator notes
- `{annotation}` - Curly braces with annotations
- `<HTML tags>` - Angle brackets with tags

If you want brackets to be read aloud, you need to escape them with backslashes: `\[`, `\]`, `\{`, `\}`, `\(`, `\)`

## Accessing Text Replacement

You can access the Text Replacement screen from:
1. **Reader Settings** → Text Replacement → Manage Text Replacements
2. **TTS Settings** → Text Replacement → Manage Text Replacements

## How It Works

Text Replacement works by applying a series of "find and replace" rules to your content. Each rule has:
- **Name**: A descriptive name for the rule
- **Find Text**: The text or pattern to search for
- **Replace With**: The replacement text (leave empty to remove matches)
- **Case Sensitive**: Whether to match exact letter case

## Types of Patterns

### 1. Literal Text (Simple)

For simple text replacement, just type the exact text you want to find:

- **Find**: `khan`
- **Replace**: `khaaan`
- **Result**: "khan" becomes "khaaan"

### 2. Regex Patterns (Advanced)

For more complex matching, you can use regular expressions (regex):

#### Common Regex Patterns

| Pattern | Matches | Example |
|---------|---------|---------|
| `.*` | Any characters (zero or more) | `Read more at.*` matches "Read more at..." |
| `.+` | Any characters (one or more) | `.+chapter` matches any text ending with "chapter" |
| `\d+` | One or more digits | `Chapter \d+` matches "Chapter 1", "Chapter 2", etc. |
| `\w+` | One or more word characters | `\w+ing` matches "reading", "writing", etc. |
| `[abc]` | Any character in brackets | `[aeiou]` matches any vowel |
| `[^abc]` | Any character NOT in brackets | `[^0-9]` matches any non-digit |
| `^text` | Text at start of line | `^Chapter` matches "Chapter" at line start |
| `text$` | Text at end of line | `end.$` matches "end." at line end |
| `(a\|b)` | Either a or b | `(prev\|next)` matches "prev" or "next" |

#### Regex Quantifiers

| Quantifier | Meaning | Example |
|------------|---------|---------|
| `*` | Zero or more | `ab*` matches "a", "ab", "abb", etc. |
| `+` | One or more | `ab+` matches "ab", "abb", etc. (not "a") |
| `?` | Zero or one | `ab?` matches "a" or "ab" |
| `{n}` | Exactly n times | `a{3}` matches "aaa" |
| `{n,}` | n or more times | `a{2,}` matches "aa", "aaa", etc. |
| `{n,m}` | Between n and m times | `a{2,4}` matches "aa", "aaa", or "aaaa" |

## Important: Escaping Special Characters

If you want to match special regex characters literally, you need to escape them with a backslash (`\`):

| Character | Escape With | Example |
|-----------|-------------|---------|
| `{` | `\{` | `\{` matches literal "{" |
| `}` | `\}` | `\}` matches literal "}" |
| `[` | `\[` | `\[` matches literal "[" |
| `]` | `\]` | `\]` matches literal "]" |
| `(` | `\(` | `\(` matches literal "(" |
| `)` | `\)` | `\)` matches literal ")" |
| `.` | `\.` | `\.` matches literal "." |
| `*` | `\*` | `\*` matches literal "*" |
| `+` | `\+` | `\+` matches literal "+" |
| `?` | `\?` | `\?` matches literal "?" |
| `^` | `\^` | `\^` matches literal "^" |
| `$` | `\$` | `\$` matches literal "$" |
| `\|` | `\|` | `\|` matches literal "\|" |

### Common Mistake: Unmatched Braces

**Problem**: If you type `{[}` or `{test`, you'll get an error because these are invalid regex patterns.

**Solution**: 
- To match literal braces: use `\{` and `\}`
- To use braces as quantifiers: use them properly like `{3}` or `{2,4}`

## Examples

### Example 1: Remove Navigation Hints

**Goal**: Remove text like "Use arrow keys to navigate"

- **Find**: `Use arrow keys.*chapter`
- **Replace**: (leave empty)
- **Case Sensitive**: No

### Example 2: Fix Character Name

**Goal**: Replace "Jon" with "John"

- **Find**: `Jon`
- **Replace**: `John`
- **Case Sensitive**: Yes (to avoid changing "jon" in other words)

### Example 3: Remove Chapter Numbers

**Goal**: Remove "Chapter 1", "Chapter 2", etc.

- **Find**: `Chapter \d+[:\s]*`
- **Replace**: (leave empty)
- **Case Sensitive**: No

### Example 4: Remove Ads

**Goal**: Remove "Read more at example.com"

- **Find**: `Read more at .+`
- **Replace**: (leave empty)
- **Case Sensitive**: No

### Example 5: Fix Formatting

**Goal**: Replace multiple spaces with single space

- **Find**: `\s{2,}`
- **Replace**: ` ` (single space)
- **Case Sensitive**: No

## Tips for Using Regex

1. **Start Simple**: Begin with literal text and only use regex when needed
2. **Test Your Pattern**: Use the preview feature if available
3. **Be Specific**: More specific patterns reduce unintended matches
4. **Order Matters**: Rules are applied in order, so put more specific rules first
5. **Escape When Unsure**: If a pattern doesn't work, try escaping special characters

## Troubleshooting

### "Invalid regex pattern" Error

This usually means:
1. **Unmatched brackets**: `[` without `]` or vice versa
2. **Unmatched braces**: `{` without `}` or vice versa
3. **Unmatched parentheses**: `(` without `)` or vice versa
4. **Invalid escape sequence**: `\` followed by an invalid character

**Solution**: Check your pattern for unmatched special characters and escape them if you want to match them literally.

### Pattern Not Matching

1. **Check case sensitivity**: Is "Case Sensitive" enabled when it shouldn't be?
2. **Check for hidden characters**: Copy-paste can include invisible characters
3. **Test with simpler pattern**: Start with a basic pattern and build up

### Unexpected Replacements

1. **Too broad pattern**: Make your pattern more specific
2. **Missing anchors**: Use `^` for start of line and `$` for end of line
3. **Greedy matching**: `.*` matches as much as possible; use `.*?` for minimal matching

## Default Replacements

The app comes with some default text replacements that remove common unwanted text:
- Navigation hints ("Use arrow keys to navigate")
- Promotional text ("Read more at...")
- Other common patterns

These defaults are marked with a "Default" badge and cannot be edited or deleted, but you can disable them.

## Import/Export

You can share your text replacement rules with others or backup your settings:

1. **Export**: Tap the export button to generate a JSON file
2. **Import**: Tap the import button and paste JSON data to load rules

## Best Practices

1. **Name your rules descriptively**: "Fix Khan typo" is better than "Rule 1"
2. **Use case sensitivity wisely**: Only enable when exact case matching is needed
3. **Test before saving**: Make sure your pattern works as expected
4. **Keep patterns simple**: Complex regex is harder to maintain
5. **Document complex patterns**: Add comments in the description field

## Advanced: Regex Cheat Sheet

```
.       - Any character except newline
\d      - Any digit (0-9)
\D      - Any non-digit
\w      - Any word character (a-z, A-Z, 0-9, _)
\W      - Any non-word character
\s      - Any whitespace character (space, tab, newline)
\S      - Any non-whitespace character
^       - Start of line
$       - End of line
\b      - Word boundary
\B      - Non-word boundary

*       - Zero or more
+       - One or more
?       - Zero or one
{n}     - Exactly n times
{n,}    - n or more times
{n,m}   - Between n and m times

[abc]   - Any of a, b, or c
[^abc]  - Not a, b, or c
[a-z]   - Any character from a to z
[0-9]   - Any digit from 0 to 9

(a|b)   - Either a or b
(?:...) - Non-capturing group
(...)   - Capturing group
```

## Getting Help

If you're having trouble with a pattern:
1. Check the inline validation message in the dialog
2. Look at the regex help card that appears when you type special characters
3. Test your pattern with online regex testers (e.g., regex101.com)
4. Start with literal text and gradually add regex features

# Frequently Asked Questions (FAQ)

## General Questions

### What is IReader?
IReader is an open-source eBook and document reader for Android devices. It allows you to read content from various sources, both online and offline, with a highly customizable reading experience.

### Is IReader free?
Yes, IReader is completely free and open-source. There are no ads, in-app purchases, or premium features that require payment.

### Where can I download IReader?
You can download IReader from the [GitHub Releases page](https://github.com/IReaderorg/IReader/releases), F-Droid, or other trusted repositories. It is not available on Google Play Store.

### Does IReader track my reading habits or collect data?
No, IReader does not collect any personal data or reading habits. All your reading data stays on your device unless you explicitly create and share a backup.

## Extensions

### What are extensions?
Extensions are add-ons for IReader that provide access to different content sources. Each extension connects to a specific website or service to browse and download books.

### Why do extensions need to be installed separately?
Extensions are separate from the main app for several reasons:
- It keeps the main app lightweight
- It allows you to install only the sources you need
- It makes it easier to update individual sources
- It provides more flexibility for adding new sources

### Are extensions safe to install?
Extensions from the official repository are safe to install. They are reviewed before being added to the repository. You may see a security warning when installing, but this is because they're not signed with a Google Play certificate, not because they're harmful.

### How do I update extensions?
Go to the Extensions section and look for extensions with an update badge. Tap the update button next to each extension or use "Update All" if available.

## Library Management

### How many books can I add to my library?
There's no hard limit on the number of books you can add, but performance may decrease with extremely large libraries (thousands of books). Consider using categories to organize your collection.

### Can I import my existing eBook collection?
Yes, you can import local eBook files. Go to the Browse tab, select Local Source, and navigate to the folder containing your eBooks.

### Can I organize my books into collections?
Yes, you can create categories to organize your books. See the [Categories guide](Categories.md) for detailed instructions.

### How do I delete a book from my library?
Long-press on the book, then select "Remove from library" or tap the trash icon.

## Reading Features

### Does IReader support different reading modes?
Yes, IReader supports both paged mode (left-to-right or right-to-left swiping) and continuous scrolling mode.

### Can I customize the appearance of text?
Yes, you can customize font, size, weight, spacing, margins, colors, and more. See the [Reading Books guide](Reading-Books.md) for details.

### Does IReader support text-to-speech?
Yes, IReader includes text-to-speech functionality if your device supports it.

### What is Bionic Reading Mode?
Bionic Reading Mode highlights the first part of each word to help your brain read faster by recognizing word shapes more quickly. This can improve reading speed and reduce eye strain for some users.

### Can I read books offline?
Yes, once you've downloaded chapters, you can read them without an internet connection.

## Backup and Data

### How do I back up my library?
Go to Settings > Backup and Restore > Create backup. You can choose what data to include in the backup. See the [Backup and Restore guide](Backup-and-Restore.md) for details.

### Can I transfer my library to another device?
Yes, create a backup on your current device, transfer the backup file to your new device, then restore from that backup on the new device.

### What happens if I uninstall IReader?
Uninstalling IReader will remove all app data, including your library, reading progress, and settings. Make sure to create a backup before uninstalling.

## Technical Issues

### Why does IReader need storage permission?
IReader needs storage permission to:
- Save downloaded books and chapters
- Create and restore backups
- Access local book files
- Save reading preferences and settings

### IReader is using a lot of storage. How can I reduce this?
You can:
- Clear the cache in Settings > Advanced > Clear cache
- Delete downloaded chapters for books you're not currently reading
- Remove books you no longer need from your library

### How do I report a bug or request a feature?
You can report bugs or request features through the [GitHub Issues page](https://github.com/IReaderorg/IReader/issues). Please search existing issues first to avoid duplicates.

## Source-Specific Questions

### Why isn't a particular source working?
Sources can stop working for several reasons:
- The website changed its structure
- The source is region-blocked
- Your internet connection is having issues
- The extension needs to be updated

Try updating the extension or using a different source for the same content.

### Why are some sources slower than others?
Source speed depends on:
- The server's response time
- Your internet connection
- How the source is implemented
- Whether the source has anti-scraping measures

### Can I request a new source?
Yes, you can request new sources through the [GitHub Issues page](https://github.com/IReaderorg/IReader/issues). However, not all requests can be fulfilled due to technical limitations or legal considerations.

## Troubleshooting

### The app crashes frequently. What should I do?
Try these steps:
1. Update to the latest version of the app
2. Clear the app cache
3. Restart your device
4. If the problem persists, check the [Troubleshooting guide](Troubleshooting.md)

### Why can't I install extensions?
See the "Extensions Not Installing" section in the [Troubleshooting guide](Troubleshooting.md).

### Why aren't books showing up in my library?
Make sure you're not filtering by category. Tap the category dropdown and select "All" or "Default". If books still don't appear, try refreshing the library by pulling down on the screen. 
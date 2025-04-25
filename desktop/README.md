# IReader Desktop Application

## Installation Requirements

IReader requires Java 17 or later to run. There are two distribution options:

### Option 1: Standalone Version (Recommended)
- Download the standalone version which includes a packaged JRE
- No additional installation required
- Available as:
  - Windows: `IReader-x.x.x.exe` or `IReader-x.x.x.msi`
  - macOS: `IReader-x.x.x.dmg`
  - Linux: `IReader-x.x.x.deb` or `IReader-x.x.x.rpm`

### Option 2: JAR Version (For advanced users)
If you download the JAR version, you need to install Java manually:

1. Install Java 17 or later:
   - Windows/macOS/Linux: [Adoptium OpenJDK](https://adoptium.net/)
   - Or use your system's package manager

2. Launch IReader:
   - Windows: Use the provided `IReader_launcher.bat` file
   - macOS/Linux: Run `java -Xmx2G -jar IReader-x.x.x.jar`

## Troubleshooting

### Application won't start

#### Windows
1. Check if Java is installed by opening Command Prompt and typing:
   ```
   java -version
   ```
2. If you get "not recognized" error, install Java or use the standalone version

#### macOS
1. Check if Java is installed by opening Terminal and typing:
   ```
   java -version
   ```
2. If you get "command not found", install Java or use the standalone version

#### Linux
1. Check if Java is installed:
   ```
   java -version
   ```
2. Install Java if needed:
   - Ubuntu/Debian: `sudo apt install openjdk-17-jre`
   - Fedora: `sudo dnf install java-17-openjdk`
   - Arch: `sudo pacman -S jre17-openjdk`

### Other Issues

If you experience issues with resources not loading or translation errors:
1. Make sure you have the latest version
2. Try running with console output to see error messages:
   ```
   java -Xmx2G -jar IReader-x.x.x.jar
   ```
3. Report issues on GitHub with the console output 
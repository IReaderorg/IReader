Name:           ireader
Version:        1.0.0
Release:        1%{?dist}
Summary:        Modern eBook reader with offline text-to-speech

License:        MPL-2.0
URL:            https://github.com/yourusername/ireader
Source0:        %{name}-%{version}.tar.gz

BuildArch:      x86_64
Requires:       java-17-openjdk-headless
Requires:       alsa-lib >= 1.0.16

%description
IReader is a feature-rich eBook reader application with built-in
offline text-to-speech support powered by Piper TTS. It supports
multiple eBook formats and provides natural-sounding voices in
20+ languages.

Features:
 * Offline text-to-speech in 20+ languages
 * Support for EPUB, PDF, and other formats
 * Customizable reading experience
 * Cross-platform compatibility
 * Privacy-focused (no cloud dependencies)

%prep
%setup -q

%build
# No build step needed for Java application

%install
rm -rf %{buildroot}

# Create directory structure
mkdir -p %{buildroot}%{_libdir}/%{name}
mkdir -p %{buildroot}%{_libdir}/%{name}/native
mkdir -p %{buildroot}%{_bindir}
mkdir -p %{buildroot}%{_datadir}/applications
mkdir -p %{buildroot}%{_datadir}/icons/hicolor/48x48/apps
mkdir -p %{buildroot}%{_datadir}/icons/hicolor/128x128/apps
mkdir -p %{buildroot}%{_datadir}/icons/hicolor/256x256/apps
mkdir -p %{buildroot}%{_docdir}/%{name}

# Install application files
install -m 644 ireader.jar %{buildroot}%{_libdir}/%{name}/

# Install native libraries
install -m 755 native/*.so %{buildroot}%{_libdir}/%{name}/native/

# Install launcher script
cat > %{buildroot}%{_bindir}/%{name} << 'EOF'
#!/bin/bash
# IReader launcher script

# Set library path for native libraries
export LD_LIBRARY_PATH="%{_libdir}/%{name}/native:$LD_LIBRARY_PATH"

# Launch application
exec java -jar %{_libdir}/%{name}/ireader.jar "$@"
EOF
chmod 755 %{buildroot}%{_bindir}/%{name}

# Install desktop entry
cat > %{buildroot}%{_datadir}/applications/%{name}.desktop << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=IReader
GenericName=eBook Reader
Comment=Modern eBook reader with offline text-to-speech
Exec=%{name} %F
Icon=%{name}
Terminal=false
Categories=Office;Viewer;Literature;
MimeType=application/epub+zip;application/pdf;text/plain;
Keywords=ebook;reader;epub;pdf;tts;text-to-speech;
StartupNotify=true
EOF

# Install icons
install -m 644 icons/48x48.png %{buildroot}%{_datadir}/icons/hicolor/48x48/apps/%{name}.png
install -m 644 icons/128x128.png %{buildroot}%{_datadir}/icons/hicolor/128x128/apps/%{name}.png
install -m 644 icons/256x256.png %{buildroot}%{_datadir}/icons/hicolor/256x256/apps/%{name}.png

# Install documentation
install -m 644 LICENSE %{buildroot}%{_docdir}/%{name}/
install -m 644 README.md %{buildroot}%{_docdir}/%{name}/
install -m 644 THIRD_PARTY_LICENSES.txt %{buildroot}%{_docdir}/%{name}/

%files
%{_bindir}/%{name}
%{_libdir}/%{name}/ireader.jar
%{_libdir}/%{name}/native/*.so
%{_datadir}/applications/%{name}.desktop
%{_datadir}/icons/hicolor/*/apps/%{name}.png
%doc %{_docdir}/%{name}/LICENSE
%doc %{_docdir}/%{name}/README.md
%doc %{_docdir}/%{name}/THIRD_PARTY_LICENSES.txt

%post
# Update icon cache
if [ -x /usr/bin/gtk-update-icon-cache ]; then
    /usr/bin/gtk-update-icon-cache -f -t %{_datadir}/icons/hicolor &>/dev/null || :
fi

# Update desktop database
if [ -x /usr/bin/update-desktop-database ]; then
    /usr/bin/update-desktop-database -q &>/dev/null || :
fi

# Update library cache
/sbin/ldconfig

%postun
if [ $1 -eq 0 ]; then
    # Update icon cache
    if [ -x /usr/bin/gtk-update-icon-cache ]; then
        /usr/bin/gtk-update-icon-cache -f -t %{_datadir}/icons/hicolor &>/dev/null || :
    fi
    
    # Update desktop database
    if [ -x /usr/bin/update-desktop-database ]; then
        /usr/bin/update-desktop-database -q &>/dev/null || :
    fi
    
    # Update library cache
    /sbin/ldconfig
fi

%changelog
* Mon Nov 10 2025 IReader Team <team@ireader.org> - 1.0.0-1
- Initial release
- Offline text-to-speech support with Piper TTS
- Support for 20+ languages
- EPUB and PDF support
- Cross-platform compatibility

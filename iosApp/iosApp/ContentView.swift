import SwiftUI
import UIKit
import iosBuildCheck

/// Main ContentView that hosts the Compose Multiplatform UI
/// This serves as the bridge between SwiftUI and Compose
struct ContentView: View {
    @State private var showingLibrary = false
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Library Tab
            LibraryView()
                .tabItem {
                    Image(systemName: "books.vertical.fill")
                    Text("Library")
                }
                .tag(0)
            
            // Updates Tab
            UpdatesView()
                .tabItem {
                    Image(systemName: "bell.fill")
                    Text("Updates")
                }
                .tag(1)
            
            // History Tab
            HistoryView()
                .tabItem {
                    Image(systemName: "clock.fill")
                    Text("History")
                }
                .tag(2)
            
            // Browse Tab
            BrowseView()
                .tabItem {
                    Image(systemName: "globe")
                    Text("Browse")
                }
                .tag(3)
            
            // More Tab
            MoreView()
                .tabItem {
                    Image(systemName: "ellipsis")
                    Text("More")
                }
                .tag(4)
        }
        .accentColor(.blue)
    }
}

// MARK: - Library View
struct LibraryView: View {
    @State private var searchText = ""
    @State private var books: [BookItem] = []
    
    var body: some View {
        NavigationView {
            VStack {
                if books.isEmpty {
                    EmptyLibraryView()
                } else {
                    BookGridView(books: books)
                }
            }
            .navigationTitle("Library")
            .searchable(text: $searchText, prompt: "Search library")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button("Filter", action: {})
                        Button("Sort", action: {})
                    } label: {
                        Image(systemName: "line.3.horizontal.decrease.circle")
                    }
                }
            }
        }
    }
}

struct EmptyLibraryView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "books.vertical")
                .font(.system(size: 60))
                .foregroundColor(.gray)
            
            Text("Your library is empty")
                .font(.title2)
                .fontWeight(.medium)
            
            Text("Add books from the Browse tab")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .padding()
    }
}

struct BookGridView: View {
    let books: [BookItem]
    let columns = [
        GridItem(.adaptive(minimum: 100, maximum: 150), spacing: 16)
    ]
    
    var body: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 16) {
                ForEach(books) { book in
                    BookCoverView(book: book)
                }
            }
            .padding()
        }
    }
}

struct BookCoverView: View {
    let book: BookItem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            RoundedRectangle(cornerRadius: 8)
                .fill(Color.gray.opacity(0.3))
                .aspectRatio(2/3, contentMode: .fit)
                .overlay(
                    Image(systemName: "book.closed.fill")
                        .font(.largeTitle)
                        .foregroundColor(.gray)
                )
            
            Text(book.title)
                .font(.caption)
                .lineLimit(2)
        }
    }
}

// MARK: - Updates View
struct UpdatesView: View {
    var body: some View {
        NavigationView {
            VStack {
                Image(systemName: "bell.slash")
                    .font(.system(size: 60))
                    .foregroundColor(.gray)
                
                Text("No updates")
                    .font(.title2)
                    .fontWeight(.medium)
                    .padding(.top)
                
                Text("Check back later for new chapters")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .navigationTitle("Updates")
        }
    }
}

// MARK: - History View
struct HistoryView: View {
    var body: some View {
        NavigationView {
            VStack {
                Image(systemName: "clock")
                    .font(.system(size: 60))
                    .foregroundColor(.gray)
                
                Text("No reading history")
                    .font(.title2)
                    .fontWeight(.medium)
                    .padding(.top)
                
                Text("Start reading to see your history")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .navigationTitle("History")
        }
    }
}

// MARK: - Browse View
struct BrowseView: View {
    var body: some View {
        NavigationView {
            List {
                Section("Sources") {
                    NavigationLink(destination: Text("Source List")) {
                        Label("All Sources", systemImage: "globe")
                    }
                    NavigationLink(destination: Text("Extensions")) {
                        Label("Extensions", systemImage: "puzzlepiece.extension")
                    }
                }
                
                Section("Discover") {
                    NavigationLink(destination: Text("Popular")) {
                        Label("Popular", systemImage: "flame")
                    }
                    NavigationLink(destination: Text("Latest")) {
                        Label("Latest Updates", systemImage: "clock.arrow.circlepath")
                    }
                }
            }
            .navigationTitle("Browse")
        }
    }
}

// MARK: - More View
struct MoreView: View {
    var body: some View {
        NavigationView {
            List {
                Section("Settings") {
                    NavigationLink(destination: Text("General")) {
                        Label("General", systemImage: "gear")
                    }
                    NavigationLink(destination: Text("Appearance")) {
                        Label("Appearance", systemImage: "paintbrush")
                    }
                    NavigationLink(destination: Text("Reader")) {
                        Label("Reader", systemImage: "book")
                    }
                    NavigationLink(destination: Text("Downloads")) {
                        Label("Downloads", systemImage: "arrow.down.circle")
                    }
                }
                
                Section("Data") {
                    NavigationLink(destination: Text("Backup")) {
                        Label("Backup & Restore", systemImage: "externaldrive")
                    }
                }
                
                Section("About") {
                    NavigationLink(destination: Text("About")) {
                        Label("About", systemImage: "info.circle")
                    }
                }
            }
            .navigationTitle("More")
        }
    }
}

// MARK: - Models
struct BookItem: Identifiable {
    let id: Int64
    let title: String
    let author: String
    let coverUrl: String?
}

// MARK: - Preview
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

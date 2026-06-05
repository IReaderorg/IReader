import { useState, useEffect } from 'react'

interface ServerInfo {
  name: string
  version: string
  port: string
}

interface Source {
  id: number
  name: string
  lang: string
  supportsLatest: boolean
  iconUrl?: string
}

function App() {
  const [serverInfo, setServerInfo] = useState<ServerInfo | null>(null)
  const [sources, setSources] = useState<Source[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // Fetch server info
    fetch('/api/v1/info')
      .then(res => res.json())
      .then(data => setServerInfo(data))
      .catch(err => setError(err.message))

    // Fetch sources
    fetch('/api/v1/sources')
      .then(res => res.json())
      .then(data => setSources(data))
      .catch(err => setError(err.message))
  }, [])

  return (
    <div style={{ fontFamily: 'system-ui, sans-serif', maxWidth: 800, margin: '0 auto', padding: 20 }}>
      <h1>📖 IReader Server</h1>
      
      {error && (
        <div style={{ background: '#fee', color: '#c00', padding: 10, borderRadius: 8, marginBottom: 20 }}>
          Error: {error}
        </div>
      )}

      {serverInfo && (
        <div style={{ background: '#f0f0f0', padding: 15, borderRadius: 8, marginBottom: 20 }}>
          <h2>Server Info</h2>
          <p><strong>Name:</strong> {serverInfo.name}</p>
          <p><strong>Version:</strong> {serverInfo.version}</p>
          <p><strong>Port:</strong> {serverInfo.port}</p>
        </div>
      )}

      <div style={{ background: '#e8f5e9', padding: 15, borderRadius: 8, marginBottom: 20 }}>
        <h2>Sources ({sources.length})</h2>
        {sources.length === 0 ? (
          <p>No sources installed. Sources will appear here when configured.</p>
        ) : (
          <ul>
            {sources.map(source => (
              <li key={source.id}>
                {source.name} ({source.lang})
              </li>
            ))}
          </ul>
        )}
      </div>

      <div style={{ background: '#e3f2fd', padding: 15, borderRadius: 8 }}>
        <h2>API Endpoints</h2>
        <ul>
          <li><code>GET /api/v1/sources</code> - List sources</li>
          <li><code>GET /api/v1/books</code> - List books</li>
          <li><code>GET /api/v1/books/:id/chapters</code> - Get chapters</li>
        </ul>
      </div>
    </div>
  )
}

export default App

import { useState } from 'react'

function App() {
  const [searchQuery, setSearchQuery] = useState('')

  return (
    <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
      <h1>Tilbud</h1>
      <p>Danish Grocery Weekly Ads Search</p>
      
      <div style={{ marginTop: '2rem' }}>
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search for deals..."
          style={{
            padding: '0.75rem 1rem',
            fontSize: '1rem',
            border: '2px solid #ccc',
            borderRadius: '8px',
            width: '100%',
            maxWidth: '400px',
          }}
        />
        <button
          style={{
            marginLeft: '0.5rem',
            padding: '0.75rem 1.5rem',
            fontSize: '1rem',
            backgroundColor: '#0070f3',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
          }}
        >
          Search
        </button>
      </div>

      <div style={{ marginTop: '2rem', color: '#666' }}>
        <p>Search results will appear here.</p>
        <p>Backend: <code>http://localhost:8080</code></p>
      </div>
    </div>
  )
}

export default App

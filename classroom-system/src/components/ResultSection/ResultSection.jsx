import { useState, useEffect } from 'react';
import './ResultSection.css';

function ResultSection() {
  const [results, setResults] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // You could add a useEffect to load results when the component mounts
  useEffect(() => {
    fetchResults();
  }, []);

  const fetchResults = async () => {
    setIsLoading(true);
    
    try {
      // This is where you would fetch results from your API
      // For now, we'll just set an empty array
      // const response = await fetch('your-api-endpoint');
      // const data = await response.json();
      // setResults(data);
      
      // For demonstration, keeping empty array
      setResults([]);
    } catch (error) {
      console.error('Error fetching results:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="dashboard-section">
      <h2>Your Results</h2>
      <div className="results-container">
        {isLoading ? (
          <div className="loading-indicator">
            <div className="spinner"></div>
            <p>Loading your results...</p>
          </div>
        ) : results.length > 0 ? (
          <div className="results-list">
            {/* Map through results when you have data */}
            {results.map(result => (
              <div key={result.id} className="result-card">
                <h3>{result.examTitle}</h3>
                <div className="result-details">
                  <div className="result-item">
                    <span className="result-label">Score:</span>
                    <span className="result-value">{result.score}%</span>
                  </div>
                  <div className="result-item">
                    <span className="result-label">Date:</span>
                    <span className="result-value">{new Date(result.date).toLocaleDateString()}</span>
                  </div>
                  {/* Add more result details as needed */}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="results-empty">
            <p>No results to display yet.</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default ResultSection;
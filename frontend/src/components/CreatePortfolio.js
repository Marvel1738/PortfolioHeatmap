// frontend/src/components/CreatePortfolio.js

// Import React and useState hook for managing component state
import React, { useState } from 'react';
// Import axios for making HTTP requests to the backend
import axios from 'axios';
// Import useNavigate for redirecting after submission
import { useNavigate } from 'react-router-dom';
// Import custom CSS for styling
import './CreatePortfolio.css';

/**
 * CreatePortfolio component allows users to create their first portfolio.
 * Creates a portfolio via /portfolios/create, adds holdings via /portfolios/{id}/holdings/add,
 * and redirects to the heatmap page on completion.
 * 
 * @returns {JSX.Element} The rendered portfolio creation UI
 */
function CreatePortfolio() {
  // State for portfolio name input
  const [portfolioName, setPortfolioName] = useState('');
  // State for current stock ticker input
  const [ticker, setTicker] = useState('');
  // State for current shares input
  const [shares, setShares] = useState('');
  // State for purchase price input
  const [purchasePrice, setPurchasePrice] = useState('');
  // State for list of holdings to be added
  const [holdings, setHoldings] = useState([]);
  // State for error messages
  const [error, setError] = useState('');
  // State to track created portfolio ID
  const [portfolioId, setPortfolioId] = useState(null);
  // Hook to navigate to other routes
  const navigate = useNavigate();

  /**
   * Handles creating a new portfolio when the user submits the name.
   * Calls /portfolios/create and stores the returned portfolio ID.
   * 
   * @param {Event} e - Form submission event for creating portfolio
   */
  const handleCreatePortfolio = async (e) => {
    e.preventDefault();
    if (!portfolioName) {
      setError('Please enter a portfolio name.');
      return;
    }

    try {
      const token = localStorage.getItem('token'); // Get JWT for auth
      const response = await axios.post(
        'http://localhost:8080/portfolios/create',
        null, // No body, using query params
        {
          params: { name: portfolioName }, // Send name as query param
          headers: {
            'Authorization': `Bearer ${token}`, // Include JWT
          },
        }
      );
      setPortfolioId(response.data.id); // Store portfolio ID from response
      setError(''); // Clear errors
    } catch (err) {
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Failed to create portfolio: ' + errorMessage);
    }
  };

  /**
   * Handles adding a holding to the list before final submission.
   * Validates inputs and updates the holdings state.
   * 
   * @param {Event} e - Form submission event for adding holding
   */
  const handleAddHolding = (e) => {
    e.preventDefault();
    if (!ticker || !shares || !purchasePrice) {
      setError('Please fill in all holding fields.');
      return;
    }
    if (isNaN(shares) || Number(shares) <= 0 || isNaN(purchasePrice) || Number(purchasePrice) <= 0) {
      setError('Shares and purchase price must be positive numbers.');
      return;
    }

    // Add holding to list (will submit to backend later)
    const newHolding = {
      ticker: ticker.toUpperCase(),
      shares: Number(shares),
      purchasePrice: Number(purchasePrice),
      purchaseDate: new Date().toISOString().split('T')[0], // Current date in YYYY-MM-DD
    };
    setHoldings([...holdings, newHolding]);
    setTicker(''); // Clear inputs
    setShares('');
    setPurchasePrice('');
    setError('');
  };

  /**
   * Handles submitting all holdings to the backend for the created portfolio.
   * Calls /portfolios/{portfolioId}/holdings/add for each holding, then redirects.
   * 
   * @param {Event} e - Form submission event for final submission
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!portfolioId) {
      setError('Please create a portfolio first.');
      return;
    }
    if (holdings.length === 0) {
      setError('Please add at least one holding.');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      // Submit each holding to the backend
      for (const holding of holdings) {
        await axios.post(
          `http://localhost:8080/portfolios/${portfolioId}/holdings/add`,
          null, // No body, using query params
          {
            params: {
              ticker: holding.ticker,
              shares: holding.shares,
              purchasePrice: holding.purchasePrice,
              purchaseDate: holding.purchaseDate,
            },
            headers: {
              'Authorization': `Bearer ${token}`, // Include JWT
            },
          }
        );
      }
      setError('');
      navigate('/heatmap'); // Redirect to heatmap
    } catch (err) {
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Failed to add holdings: ' + errorMessage);
    }
  };

  // JSX to render the portfolio creation UI
  return (
    <div className="create-portfolio">
      <h1>Create Your First Portfolio</h1>
      {/* Form to create portfolio if not yet created */}
      {!portfolioId && (
        <form onSubmit={handleCreatePortfolio}>
          <div className="form-group">
            <label>Portfolio Name:</label>
            <input
              type="text"
              value={portfolioName}
              onChange={(e) => setPortfolioName(e.target.value)}
              placeholder="e.g., My First Portfolio"
            />
          </div>
          <button type="submit">Create Portfolio</button>
        </form>
      )}
      {/* Form to add holdings once portfolio is created */}
      {portfolioId && (
        <>
          <form onSubmit={handleAddHolding}>
            <div className="form-group">
              <label>Stock Ticker:</label>
              <input
                type="text"
                value={ticker}
                onChange={(e) => setTicker(e.target.value)}
                placeholder="e.g., AAPL"
              />
            </div>
            <div className="form-group">
              <label>Shares:</label>
              <input
                type="number"
                value={shares}
                onChange={(e) => setShares(e.target.value)}
                placeholder="e.g., 10"
                min="1"
              />
            </div>
            <div className="form-group">
              <label>Purchase Price ($):</label>
              <input
                type="number"
                value={purchasePrice}
                onChange={(e) => setPurchasePrice(e.target.value)}
                placeholder="e.g., 150.00"
                step="0.01"
                min="0.01"
              />
            </div>
            <button type="submit">Add Holding</button>
          </form>
          {/* Display added holdings */}
          {holdings.length > 0 && (
            <div className="holdings-list">
              <h2>Your Holdings:</h2>
              <ul>
                {holdings.map((holding, index) => (
                  <li key={index}>
                    {holding.ticker}: {holding.shares} shares @ ${holding.purchasePrice}
                  </li>
                ))}
              </ul>
            </div>
          )}
          <button className="submit-portfolio" onClick={handleSubmit}>
            Finish Portfolio
          </button>
        </>
      )}
      {error && <p className="error-message">{error}</p>}
    </div>
  );
}

export default CreatePortfolio;
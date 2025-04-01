// frontend/src/components/CreatePortfolio.js

// Import React and hooks for state and side effects
import React, { useState, useEffect } from 'react';
// Import axios for making HTTP requests to the backend
import axios from 'axios';
// Import useNavigate for redirecting after submission
import { useNavigate } from 'react-router-dom';
// Import debounce to limit API calls during typing
import debounce from 'lodash.debounce';
// Import custom CSS for styling
import './CreatePortfolio.css';

/**
 * CreatePortfolio component allows users to create their first portfolio.
 * Features a searchable stock ticker dropdown sorted by market cap, with add,
 * remove, and update functionality for holdings before submission.
 * 
 * @returns {JSX.Element} The rendered portfolio creation UI
 */
function CreatePortfolio() {
  // State for portfolio name input
  const [portfolioName, setPortfolioName] = useState('');
  // State for current ticker input, used for searching and selection
  const [ticker, setTicker] = useState('');
  // State for current shares input, stored as string for decimal support
  const [shares, setShares] = useState('');
  // State for current purchase price input
  const [purchasePrice, setPurchasePrice] = useState('');
  // State for list of holdings to be submitted
  const [holdings, setHoldings] = useState([]);
  // State for error messages displayed to the user
  const [error, setError] = useState('');
  // State for the created portfolio's ID after /portfolios/create
  const [portfolioId, setPortfolioId] = useState(null);
  // State to track the index of the holding being edited, null if not editing
  const [editingIndex, setEditingIndex] = useState(null);
  // State for stock suggestions fetched from the backend
  const [stockSuggestions, setStockSuggestions] = useState([]);
  // State to control visibility of the ticker dropdown
  const [showDropdown, setShowDropdown] = useState(false);
  // Hook for programmatic navigation
  const navigate = useNavigate();

  /**
   * Fetches stock suggestions based on the ticker prefix input.
   * Debounced to prevent excessive API calls during rapid typing.
   * Updates stockSuggestions state and shows/hides the dropdown.
   */
  const fetchStockSuggestions = debounce(async (prefix) => {
    // Hide dropdown and clear suggestions if input is empty
    if (prefix.length === 0) {
      setStockSuggestions([]);
      setShowDropdown(false);
      return;
    }
    try {
      // Fetch stock suggestions from the backend
      const response = await axios.get('http://localhost:8080/stocks/search', {
        params: { prefix },
      });
      setStockSuggestions(response.data);
      setShowDropdown(true); // Show dropdown if results exist
    } catch (err) {
      // Log error and reset dropdown on failure
      console.error('Error fetching stock suggestions:', err);
      setStockSuggestions([]);
      setShowDropdown(false);
    }
  }, 300); // 300ms debounce to balance responsiveness and performance

  // Effect to trigger stock suggestion fetch when ticker changes
  useEffect(() => {
    fetchStockSuggestions(ticker);
    // Cleanup debounce on unmount or ticker change
    return () => fetchStockSuggestions.cancel();
  }, [ticker]);

  /**
   * Handles creating a new portfolio via POST /portfolios/create.
   * Stores the returned portfolio ID for subsequent holding additions.
   */
  const handleCreatePortfolio = async (e) => {
    e.preventDefault();
    if (!portfolioName) {
      setError('Please enter a portfolio name.');
      return;
    }
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        'http://localhost:8080/portfolios/create',
        null,
        {
          params: { name: portfolioName },
          headers: { 'Authorization': `Bearer ${token}` },
        }
      );
      setPortfolioId(response.data.id);
      setError('');
    } catch (err) {
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Failed to create portfolio: ' + errorMessage);
    }
  };

  /**
   * Handles adding or updating a holding in the list.
   * Validates inputs and either adds a new holding or updates an existing one.
   */
  const handleAddOrUpdateHolding = (e) => {
    e.preventDefault();
    if (!ticker || !shares || !purchasePrice) {
      setError('Please fill in all holding fields.');
      return;
    }
    const sharesNum = Number(shares);
    const priceNum = Number(purchasePrice);
    if (isNaN(sharesNum) || sharesNum <= 0 || isNaN(priceNum) || priceNum <= 0) {
      setError('Shares and purchase price must be positive numbers.');
      return;
    }

    const newHolding = {
      ticker: ticker.toUpperCase(),
      shares: sharesNum,
      purchasePrice: priceNum,
      purchaseDate: new Date().toISOString().split('T')[0],
    };

    if (editingIndex !== null) {
      const updatedHoldings = [...holdings];
      updatedHoldings[editingIndex] = newHolding;
      setHoldings(updatedHoldings);
      setEditingIndex(null);
    } else {
      setHoldings([...holdings, newHolding]);
    }

    setTicker('');
    setShares('');
    setPurchasePrice('');
    setError('');
  };

  /**
   * Handles removing a holding from the list by index.
   * Updates the holdings state pre-submission.
   */
  const handleRemoveHolding = (index) => {
    setHoldings(holdings.filter((_, i) => i !== index));
    setError('');
  };

  /**
   * Handles editing an existing holding.
   * Populates the form with the holding’s values and sets editing mode.
   */
  const handleEditHolding = (index) => {
    const holding = holdings[index];
    setTicker(holding.ticker);
    setShares(holding.shares.toString());
    setPurchasePrice(holding.purchasePrice.toString());
    setEditingIndex(index);
    setError('');
  };

  /**
   * Handles selecting a ticker from the dropdown.
   * Sets the ticker input and hides the dropdown.
   */
  const handleTickerSelect = (selectedTicker) => {
    setTicker(selectedTicker);
    setShowDropdown(false);
  };

  /**
   * Handles submitting all holdings to the backend via POST /portfolios/{id}/holdings/add.
   * Redirects to /heatmap on success.
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
      for (const holding of holdings) {
        await axios.post(
          `http://localhost:8080/portfolios/${portfolioId}/holdings/add`,
          null,
          {
            params: {
              ticker: holding.ticker,
              shares: holding.shares,
              purchasePrice: holding.purchasePrice,
              purchaseDate: holding.purchaseDate,
            },
            headers: { 'Authorization': `Bearer ${token}` },
          }
        );
      }
      setError('');
      navigate('/heatmap');
    } catch (err) {
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Failed to add holdings: ' + errorMessage);
    }
  };

  return (
    <div className="create-portfolio">
      <h1>Create Your First Portfolio</h1>
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
      {portfolioId && (
        <>
          <form onSubmit={handleAddOrUpdateHolding}>
            <div className="form-group">
              <label>Stock Ticker:</label>
              <div className="ticker-input-container">
                <input
                  type="text"
                  value={ticker}
                  onChange={(e) => setTicker(e.target.value.toUpperCase())}
                  placeholder="e.g., AAPL"
                  autoComplete="off" // Disable browser autocomplete
                />
                {showDropdown && stockSuggestions.length > 0 && (
                  <ul className="ticker-dropdown">
                    {stockSuggestions.map((stock) => (
                      <li
                        key={stock.ticker}
                        onClick={() => handleTickerSelect(stock.ticker)}
                      >
                        {stock.ticker} - {stock.companyName} 
                        {stock.marketCap ? ` (Market Cap: ${stock.marketCap})` : ''}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
            <div className="form-group">
              <label>Shares:</label>
              <input
                type="number"
                value={shares}
                onChange={(e) => setShares(e.target.value)}
                placeholder="e.g., 10.5"
                step="0.01"
                min="0.01"
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
            <button type="submit">
              {editingIndex !== null ? 'Update Holding' : 'Add Holding'}
            </button>
          </form>
          {holdings.length > 0 && (
            <div className="holdings-list">
              <h2>Your Holdings:</h2>
              <ul>
                {holdings.map((holding, index) => (
                  <li key={index}>
                    {holding.ticker}: {holding.shares} shares @ ${holding.purchasePrice}
                    <button
                      className="edit-button"
                      onClick={() => handleEditHolding(index)}
                    >
                      Edit
                    </button>
                    <button
                      className="remove-button"
                      onClick={() => handleRemoveHolding(index)}
                    >
                      Remove
                    </button>
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
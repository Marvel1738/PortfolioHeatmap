// frontend/src/components/CreatePortfolio.js

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import debounce from 'lodash.debounce';
import './CreatePortfolio.css';

/**
 * CreatePortfolio component allows new users to create initial portfolios.
 * Supports multiple portfolios, submits to /preview when done, with a market cap-sorted ticker dropdown.
 * 
 * @returns {JSX.Element} The rendered portfolio creation UI
 */
function CreatePortfolio() {
  const [portfolioName, setPortfolioName] = useState('');
  const [ticker, setTicker] = useState('');
  const [shares, setShares] = useState('');
  const [purchasePrice, setPurchasePrice] = useState('');
  const [holdings, setHoldings] = useState([]); // Current portfolio holdings
  const [portfolios, setPortfolios] = useState([]); // List of created portfolios
  const [error, setError] = useState('');
  const [stockSuggestions, setStockSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [editingIndex, setEditingIndex] = useState(null);
  const navigate = useNavigate();

  /**
   * Fetches stock suggestions based on ticker prefix, debounced for performance.
   */
  const fetchStockSuggestions = debounce(async (prefix) => {
    if (prefix.length === 0) {
      setStockSuggestions([]);
      setShowDropdown(false);
      return;
    }
    try {
      const response = await axios.get('http://localhost:8080/stocks/search', {
        params: { prefix },
      });
      setStockSuggestions(response.data);
      setShowDropdown(true);
    } catch (err) {
      console.error('Error fetching stock suggestions:', err);
      setStockSuggestions([]);
      setShowDropdown(false);
    }
  }, 300);

  useEffect(() => {
    fetchStockSuggestions(ticker);
    return () => fetchStockSuggestions.cancel();
  }, [ticker]);

  /**
   * Saves the current portfolio locally and resets for a new one.
   */
  const handleSavePortfolio = async (e) => {
    e.preventDefault();
    if (!portfolioName) {
      setError('Please enter a portfolio name.');
      return;
    }
    if (holdings.length === 0) {
      setError('Please add at least one holding.');
      return;
    }
    const newPortfolio = {
      name: portfolioName,
      holdings: [...holdings],
      tempId: Date.now().toString(), // Temporary ID until backend assigns real one
    };
    setPortfolios([...portfolios, newPortfolio]);
    setPortfolioName('');
    setHoldings([]);
    setError('');
  };

  /**
   * Adds a holding to the current portfolio's local list.
   */
  const handleAddHolding = (e) => {
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
   * Selects a ticker from the dropdown.
   */
  const handleTickerSelect = (selectedTicker) => {
    setTicker(selectedTicker);
    setShowDropdown(false);
  };

  const handleEdit = (index) => {
    const holding = holdings[index];
    setTicker(holding.ticker);
    setShares(holding.shares.toString());
    setPurchasePrice(holding.purchasePrice.toString());
    setEditingIndex(index);
  };

  const handleDelete = (index) => {
    const updatedHoldings = holdings.filter((_, i) => i !== index);
    setHoldings(updatedHoldings);
  };

  const handleCancel = () => {
    setTicker('');
    setShares('');
    setPurchasePrice('');
    setEditingIndex(null);
  };

  /**
   * Submits all portfolios to the backend and navigates to /preview.
   */
  const handleDone = async () => {
    if (portfolios.length === 0 && (holdings.length === 0 || !portfolioName)) {
      setError('Please create at least one portfolio with holdings.');
      return;
    }
    // Add current portfolio if not saved yet
    let finalPortfolios = [...portfolios];
    if (portfolioName && holdings.length > 0) {
      finalPortfolios.push({
        name: portfolioName,
        holdings: [...holdings],
        tempId: Date.now().toString(),
      });
    }
    if (finalPortfolios.length === 0) {
      setError('Please create at least one portfolio.');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const createdPortfolios = [];
      for (const portfolio of finalPortfolios) {
        const response = await axios.post(
          'http://localhost:8080/portfolios/create',
          null,
          {
            params: { name: portfolio.name },
            headers: { 'Authorization': `Bearer ${token}` },
          }
        );
        const portfolioId = response.data.id;
        for (const holding of portfolio.holdings) {
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
        createdPortfolios.push({ id: portfolioId, name: portfolio.name, holdings: portfolio.holdings });
      }
      navigate('/preview', { state: { portfolios: createdPortfolios } });
    } catch (err) {
      setError('Failed to save portfolios: ' + (err.response?.data || err.message));
    }
  };

  return (
    <div className="create-portfolio">
      <h1>Create Your First Portfolio</h1>
      <form onSubmit={handleAddHolding}>
        <div className="form-group">
          <label>Portfolio Name:</label>
          <input
            type="text"
            value={portfolioName}
            onChange={(e) => setPortfolioName(e.target.value)}
            placeholder="e.g., My First Portfolio"
          />
        </div>
        <div className="form-group">
          <label>Stock Ticker:</label>
          <div className="ticker-input-container">
            <input
              type="text"
              value={ticker}
              onChange={(e) => setTicker(e.target.value.toUpperCase())}
              placeholder="e.g., AAPL"
              autoComplete="off"
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
        <div className="form-actions">
          <button type="submit">{editingIndex !== null ? 'Update Holding' : 'Add Holding'}</button>
          {editingIndex !== null && (
            <button type="button" onClick={handleCancel}>Cancel</button>
          )}
        </div>
      </form>
      {holdings.length > 0 && (
        <div className="holdings-list">
          <h2>Current Holdings:</h2>
          <ul>
            {holdings.map((holding, index) => (
              <li key={index} className="holding-item">
                <span>{holding.ticker}: {holding.shares} shares @ ${holding.purchasePrice}</span>
                <div className="holding-actions">
                  <button className="edit-button" onClick={() => handleEdit(index)}>Edit</button>
                  <button className="remove-button" onClick={() => handleDelete(index)}>Delete</button>
                </div>
              </li>
            ))}
          </ul>
          <button onClick={handleSavePortfolio}>Save Portfolio</button>
        </div>
      )}
      {portfolios.length > 0 && (
        <div className="portfolio-list">
          <h2>Saved Portfolios:</h2>
          <ul>
            {portfolios.map((portfolio) => (
              <li key={portfolio.tempId}>{portfolio.name} ({portfolio.holdings.length} holdings)</li>
            ))}
          </ul>
        </div>
      )}
      <button className="submit-portfolio" onClick={handleDone}>Done</button>
      {error && <p className="error-message">{error}</p>}
    </div>
  );
}

export default CreatePortfolio;
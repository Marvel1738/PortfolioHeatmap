import React, { useState, useEffect } from 'react';
import './Sidebar.css';
import axios from 'axios';
import debounce from 'lodash.debounce';

function Sidebar({ portfolios, selectedPortfolioId, onPortfolioSelect, holdings }) {
  const [editingHolding, setEditingHolding] = useState(null);
  const [shares, setShares] = useState('');
  const [price, setPrice] = useState('');
  const [isBuying, setIsBuying] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [ticker, setTicker] = useState('');
  const [stockSuggestions, setStockSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [showNewPortfolioModal, setShowNewPortfolioModal] = useState(false);
  const [newPortfolioName, setNewPortfolioName] = useState('');

  // Fetch stock suggestions based on ticker prefix
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
    if (ticker.length >= 2) {
      fetchStockSuggestions(ticker);
    } else {
      setStockSuggestions([]);
      setShowDropdown(false);
    }
  }, [ticker]);

  const handleTickerSelect = (selectedTicker) => {
    setTicker(selectedTicker);
    setShowDropdown(false);
  };

  const handleAddNewHolding = async (e) => {
    e.preventDefault();
    if (!ticker || !shares || !price) {
      console.error('Missing required fields');
      return;
    }
    
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('No authentication token found');
        return;
      }
      
      const sharesNum = Number(shares);
      const priceNum = Number(price);
      
      if (isNaN(sharesNum) || sharesNum <= 0 || isNaN(priceNum) || priceNum <= 0) {
        console.error('Invalid shares or price values');
        return;
      }
      
      await axios.post(
        `http://localhost:8080/portfolios/${selectedPortfolioId}/holdings/add`,
        null,
        {
          params: {
            ticker: ticker,
            shares: sharesNum,
            purchasePrice: priceNum,
            purchaseDate: new Date().toISOString().split('T')[0]
          },
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );
      
      console.log(`Successfully added ${sharesNum} shares of ${ticker} at $${priceNum}`);
      
      // Reset form and close modal
      setTicker('');
      setShares('');
      setPrice('');
      setShowAddModal(false);
      
      // Trigger a refresh of the holdings
      if (onPortfolioSelect) {
        onPortfolioSelect(selectedPortfolioId);
      }
    } catch (err) {
      console.error('Failed to add shares:', err);
    }
  };

  const handleAddShares = (holding) => {
    setIsBuying(true);
    setEditingHolding(holding);
    setShares('');
    setPrice('');
  };

  const handleRemoveShares = (holding) => {
    setIsBuying(false);
    setEditingHolding(holding);
    setShares('');
    setPrice('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!editingHolding || !shares || !price) {
      console.error('Missing required fields');
      return;
    }
    
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('No authentication token found');
        return;
      }
      
      const sharesNum = Number(shares);
      const priceNum = Number(price);
      
      if (isNaN(sharesNum) || sharesNum <= 0 || isNaN(priceNum) || priceNum <= 0) {
        console.error('Invalid shares or price values');
        return;
      }
      
      // Make API call to update shares
      const endpoint = isBuying ? 'add' : 'remove';
      
      // For adding shares, we need to check if the stock already exists in the portfolio
      if (isBuying) {
        // Check if the stock already exists in the portfolio
        const existingHolding = holdings.find(h => h.stock.ticker === editingHolding.stock.ticker);
        
        if (existingHolding) {
          // If the stock already exists, update the existing holding
          await axios.put(
            `http://localhost:8080/portfolios/holdings/${existingHolding.id}`,
            null,
            {
              params: {
                shares: existingHolding.shares + sharesNum
              },
              headers: { 'Authorization': `Bearer ${token}` }
            }
          );
        } else {
          // If the stock doesn't exist, add a new holding
          await axios.post(
            `http://localhost:8080/portfolios/${selectedPortfolioId}/holdings/add`,
            null,
            {
              params: {
                ticker: editingHolding.stock.ticker,
                shares: sharesNum,
                purchasePrice: priceNum,
                purchaseDate: new Date().toISOString().split('T')[0]
              },
              headers: { 'Authorization': `Bearer ${token}` }
            }
          );
        }
      } else {
        // For removing shares, we need to update the existing holding
        if (editingHolding.shares < sharesNum) {
          console.error('Cannot remove more shares than currently owned');
          return;
        }
        
        // Update the existing holding with the new share count
        await axios.put(
          `http://localhost:8080/portfolios/holdings/${editingHolding.id}`,
          null,
          {
            params: {
              shares: editingHolding.shares - sharesNum
            },
            headers: { 'Authorization': `Bearer ${token}` }
          }
        );
      }
      
      console.log(`Successfully ${isBuying ? 'bought' : 'sold'} ${sharesNum} shares of ${editingHolding.stock.ticker} at $${priceNum}`);
      
      // Close the modal and reset state
      setEditingHolding(null);
      setShares('');
      setPrice('');
      
      // Trigger a refresh of the holdings
      if (onPortfolioSelect) {
        onPortfolioSelect(selectedPortfolioId);
      }
    } catch (err) {
      console.error(`Failed to ${isBuying ? 'buy' : 'sell'} shares:`, err);
    }
  };

  const handleCreatePortfolio = async (e) => {
    e.preventDefault();
    if (!newPortfolioName.trim()) {
      console.error('Portfolio name is required');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('No authentication token found');
        return;
      }

      const response = await axios.post(
        'http://localhost:8080/portfolios/create',
        null,
        {
          params: {
            name: newPortfolioName
          },
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );

      console.log('Portfolio created:', response.data);
      setNewPortfolioName('');
      setShowNewPortfolioModal(false);

      // Select the newly created portfolio
      if (onPortfolioSelect) {
        onPortfolioSelect(response.data.id);
      }
    } catch (err) {
      console.error('Failed to create portfolio:', err);
    }
  };

  return (
    <div className="sidebar">
      <button 
        className="new-portfolio-button"
        onClick={() => setShowNewPortfolioModal(true)}
      >
        NEW PORTFOLIO
      </button>

      <div className="portfolio-selector">
        <select 
          value={selectedPortfolioId || ''} 
          onChange={(e) => onPortfolioSelect(e.target.value)}
        >
          <option value="">Select Portfolio</option>
          {portfolios.map(portfolio => (
            <option key={portfolio.id} value={portfolio.id}>
              {portfolio.name}
            </option>
          ))}
        </select>
      </div>
      
      <button 
        className="add-button"
        onClick={() => setShowAddModal(true)}
      >
        ADD
        STOCK
      </button>
      
      <div className="holdings-list">
        {holdings.map(holding => (
          <div key={holding.id} className="holding-item">
            <div className="holding-info">
              <span className="ticker">{holding.stock.ticker}</span>
            </div>
            <div className="holding-actions">
              <button 
                className="action-button add" 
                onClick={() => handleAddShares(holding)}
                style={{ width: '25px', height: '25px', marginRight: '0px' }}
              >
                <span className="plus-icon" style={{ fontSize: '15px' }}>+</span>
              </button>
              <button 
                className="action-button remove" 
                onClick={() => handleRemoveShares(holding)}
                style={{ width: '25px', height: '25px' }}
              >
                <span className="minus-icon" style={{ fontSize: '15px' }}>-</span>
              </button>
            </div>
          </div>
        ))}
      </div>

      {showAddModal && (
        <div className="edit-modal">
          <div className="edit-content">
            <h3>Add New Holding</h3>
            <form onSubmit={handleAddNewHolding}>
              <div className="input-group">
                <label>Stock Ticker:</label>
                <div className="ticker-input-container">
                  <input
                    type="text"
                    value={ticker}
                    onChange={(e) => setTicker(e.target.value.toUpperCase())}
                    placeholder="e.g., AAPL"
                    autoComplete="off"
                    required
                  />
                  {showDropdown && stockSuggestions.length > 0 && (
                    <ul className="ticker-dropdown">
                      {stockSuggestions.map((stock) => (
                        <li
                          key={stock.ticker}
                          onClick={() => handleTickerSelect(stock.ticker)}
                        >
                          {stock.ticker} - {stock.companyName}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
              <div className="input-group">
                <label>Shares:</label>
                <input
                  type="number"
                  value={shares}
                  onChange={(e) => setShares(e.target.value)}
                  placeholder="Enter number of shares"
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
              <div className="input-group">
                <label>Price:</label>
                <input
                  type="number"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="Enter price per share"
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
              <div className="modal-actions">
                <button type="submit" className="submit-button buy">
                  Add Holding
                </button>
                <button 
                  type="button" 
                  className="cancel-button"
                  onClick={() => {
                    setShowAddModal(false);
                    setTicker('');
                    setShares('');
                    setPrice('');
                  }}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {editingHolding && (
        <div className="edit-modal">
          <div className="edit-content">
            <h3>{isBuying ? 'Buy' : 'Sell'} {editingHolding.stock.ticker}</h3>
            <div className="current-shares">
              Current Shares: {editingHolding.shares}
            </div>
            <form onSubmit={handleSubmit}>
              <div className="input-group">
                <label>Shares:</label>
                <input
                  type="number"
                  value={shares}
                  onChange={(e) => setShares(e.target.value)}
                  placeholder="Enter number of shares"
                  required
                />
              </div>
              <div className="input-group">
                <label>Price:</label>
                <input
                  type="number"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="Enter price per share"
                  step="0.01"
                  required
                />
              </div>
              <div className="modal-actions">
                <button type="submit" className={`submit-button ${isBuying ? 'buy' : 'sell'}`}>
                  {isBuying ? 'Buy' : 'Sell'} Shares
                </button>
                <button 
                  type="button" 
                  className="cancel-button"
                  onClick={() => setEditingHolding(null)}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showNewPortfolioModal && (
        <div className="edit-modal">
          <div className="edit-content">
            <h3>Create New Portfolio</h3>
            <form onSubmit={handleCreatePortfolio}>
              <div className="input-group">
                <label>Portfolio Name:</label>
                <input
                  type="text"
                  value={newPortfolioName}
                  onChange={(e) => setNewPortfolioName(e.target.value)}
                  placeholder="Enter portfolio name"
                  required
                />
              </div>
              <div className="modal-actions">
                <button type="submit" className="submit-button buy">
                  Create
                </button>
                <button 
                  type="button" 
                  className="cancel-button"
                  onClick={() => {
                    setShowNewPortfolioModal(false);
                    setNewPortfolioName('');
                  }}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Sidebar; 
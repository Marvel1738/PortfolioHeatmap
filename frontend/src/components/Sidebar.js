import React, { useState } from 'react';
import './Sidebar.css';
import axios from 'axios';

function Sidebar({ portfolios, selectedPortfolioId, onPortfolioSelect, holdings }) {
  const [editingHolding, setEditingHolding] = useState(null);
  const [shares, setShares] = useState('');
  const [price, setPrice] = useState('');
  const [isBuying, setIsBuying] = useState(true);

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

  return (
    <div className="sidebar">
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
      
      <div className="holdings-list">
        {holdings.map(holding => (
          <div key={holding.id} className="holding-item">
            <div className="holding-info">
              <span className="ticker">{holding.stock.ticker}</span>
              {editingHolding?.id === holding.id && (
                <div className="shares-info">
                  Current Shares: {holding.shares}
                </div>
              )}
            </div>
            <div className="holding-actions">
              <button 
                className="action-button add" 
                onClick={() => handleAddShares(holding)}
                style={{ width: '20px', height: '20px', marginRight: '2px' }}
              >
                <span className="plus-icon" style={{ fontSize: '10px' }}>+</span>
              </button>
              <button 
                className="action-button remove" 
                onClick={() => handleRemoveShares(holding)}
                style={{ width: '20px', height: '20px' }}
              >
                <span className="minus-icon" style={{ fontSize: '10px' }}>-</span>
              </button>
            </div>
          </div>
        ))}
      </div>

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
    </div>
  );
}

export default Sidebar; 
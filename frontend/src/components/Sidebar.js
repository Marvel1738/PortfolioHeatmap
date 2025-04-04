import React, { useState } from 'react';
import './Sidebar.css';

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

  const handleSubmit = (e) => {
    e.preventDefault();
    // TODO: Implement API call to update shares
    console.log(`${isBuying ? 'Buying' : 'Selling'} ${shares} shares of ${editingHolding.stock.ticker} at $${price}`);
    setEditingHolding(null);
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
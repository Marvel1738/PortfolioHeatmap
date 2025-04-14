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
  const [searchQuery, setSearchQuery] = useState('');
  const [isPortfolioListOpen, setIsPortfolioListOpen] = useState(false);
  const [renamePortfolioId, setRenamePortfolioId] = useState(null);
  const [renameValue, setRenameValue] = useState('');
  const [localPortfolios, setLocalPortfolios] = useState(portfolios);

  // Sync localPortfolios with portfolios, but preserve local updates
  useEffect(() => {
    setLocalPortfolios((prev) => {
      // Merge portfolios with local changes to avoid overwriting isFavorite
      return portfolios.map((newP) => {
        const existing = prev.find((p) => p.id === newP.id);
        return existing ? { ...newP, isFavorite: existing.isFavorite } : newP;
      });
    });
  }, [portfolios]);

  // Log portfolios for debugging
  console.log('Portfolios:', portfolios);
  console.log('LocalPortfolios:', localPortfolios);

  // Filter holdings based on search query
  const filteredHoldings = holdings.filter((holding) =>
    holding.stock.ticker.toLowerCase().includes(searchQuery.toLowerCase())
  );

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
      alert('Please fill in all required fields');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('You are not authenticated. Please log in.');
        return;
      }

      const sharesNum = Number(shares);
      const priceNum = Number(price);

      if (isNaN(sharesNum) || sharesNum <= 0 || isNaN(priceNum) || priceNum <= 0) {
        alert('Shares and price must be positive numbers');
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
            purchaseDate: new Date().toISOString().split('T')[0],
          },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      setTicker('');
      setShares('');
      setPrice('');
      setShowAddModal(false);

      if (onPortfolioSelect) {
        onPortfolioSelect(selectedPortfolioId);
      }
    } catch (err) {
      console.error('Failed to add shares:', err);
      alert('Failed to add holding. Please try again.');
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
      alert('Please fill in all required fields');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('You are not authenticated. Please log in.');
        return;
      }

      const sharesNum = Number(shares);
      const priceNum = Number(price);

      if (isNaN(sharesNum) || sharesNum <= 0 || isNaN(priceNum) || priceNum <= 0) {
        alert('Shares and price must be positive numbers');
        return;
      }

      if (isBuying) {
        const existingHolding = holdings.find((h) => h.stock.ticker === editingHolding.stock.ticker);

        if (existingHolding) {
          await axios.put(
            `http://localhost:8080/portfolios/holdings/${existingHolding.id}`,
            null,
            {
              params: {
                shares: existingHolding.shares + sharesNum,
              },
              headers: { Authorization: `Bearer ${token}` },
            }
          );
        } else {
          await axios.post(
            `http://localhost:8080/portfolios/${selectedPortfolioId}/holdings/add`,
            null,
            {
              params: {
                ticker: editingHolding.stock.ticker,
                shares: sharesNum,
                purchasePrice: priceNum,
                purchaseDate: new Date().toISOString().split('T')[0],
              },
              headers: { Authorization: `Bearer ${token}` },
            }
          );
        }
      } else {
        if (editingHolding.shares < sharesNum) {
          alert('Cannot sell more shares than you own');
          return;
        }

        await axios.put(
          `http://localhost:8080/portfolios/holdings/${editingHolding.id}`,
          null,
          {
            params: {
              shares: editingHolding.shares - sharesNum,
            },
            headers: { Authorization: `Bearer ${token}` },
          }
        );
      }

      setEditingHolding(null);
      setShares('');
      setPrice('');

      if (onPortfolioSelect) {
        onPortfolioSelect(selectedPortfolioId);
      }
    } catch (err) {
      console.error(`Failed to ${isBuying ? 'buy' : 'sell'} shares:`, err);
      alert(`Failed to ${isBuying ? 'buy' : 'sell'} shares. Please try again.`);
    }
  };

  const handleCreatePortfolio = async (e) => {
    e.preventDefault();
    if (!newPortfolioName.trim()) {
      alert('Portfolio name is required');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('You are not authenticated. Please log in.');
        return;
      }

      const response = await axios.post(
        'http://localhost:8080/portfolios/create',
        null,
        {
          params: {
            name: newPortfolioName,
          },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      setLocalPortfolios([...localPortfolios, response.data]);
      setNewPortfolioName('');
      setShowNewPortfolioModal(false);

      if (onPortfolioSelect) {
        onPortfolioSelect(response.data.id);
      }
    } catch (err) {
      console.error('Failed to create portfolio:', err);
      alert('Failed to create portfolio. Please try again.');
    }
  };

  const handleGenerateRandomPortfolio = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('You are not authenticated. Please log in.');
        return;
      }

      const portfolioName =
        newPortfolioName.trim() || `Random Portfolio ${new Date().toISOString().slice(0, 10)}`;

      const response = await axios.post(
        'http://localhost:8080/portfolios/create-random',
        null,
        {
          params: {
            name: portfolioName,
          },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      setLocalPortfolios([...localPortfolios, response.data]);
      setNewPortfolioName('');
      setShowNewPortfolioModal(false);

      if (onPortfolioSelect) {
        onPortfolioSelect(response.data.id);
      }
    } catch (err) {
      console.error('Failed to create random portfolio:', err);
      alert('Failed to create random portfolio. Please try again.');
    }
  };

  const handleDeletePortfolio = async (portfolioId) => {
    if (!window.confirm('Are you sure you want to delete this portfolio?')) return;
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('You are not authenticated. Please log in.');
        return;
      }

      await axios.delete(`http://localhost:8080/portfolios/${portfolioId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      setLocalPortfolios(localPortfolios.filter((p) => p.id !== portfolioId));

      if (onPortfolioSelect && selectedPortfolioId === portfolioId) {
        const remainingPortfolios = localPortfolios.filter((p) => p.id !== portfolioId);
        onPortfolioSelect(remainingPortfolios[0]?.id || '');
      }
    } catch (err) {
      console.error('Failed to delete portfolio:', err);
      alert('Failed to delete portfolio. Please try again.');
    }
  };

  const handleRenamePortfolio = async (portfolioId, newName) => {
    if (!newName.trim()) {
      alert('Portfolio name is required');
      return;
    }
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('You are not authenticated. Please log in.');
        return;
      }

      const response = await axios.patch(
        `http://localhost:8080/portfolios/${portfolioId}/rename`,
        null,
        {
          params: { name: newName },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      console.log('Rename response:', response.data); // Debug

      setLocalPortfolios(
        localPortfolios.map((p) =>
          p.id === portfolioId ? { ...p, name: response.data.name } : p
        )
      );
      setRenamePortfolioId(null);
      setRenameValue('');

      if (onPortfolioSelect) {
        onPortfolioSelect(portfolioId);
      }
    } catch (err) {
      console.error('Failed to rename portfolio:', err.response?.data || err.message);
      alert('Failed to rename portfolio. Please try again.');
    }
  };

  const handleToggleFavorite = async (portfolioId, isFavorite) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('You are not authenticated. Please log in.');
        return;
      }

      const response = await axios.patch(
        `http://localhost:8080/portfolios/${portfolioId}/favorite`,
        null,
        {
          params: { isFavorite: !isFavorite },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      console.log('Favorite response:', response.data); // Debug

      // Update local portfolios with response
      setLocalPortfolios(
        localPortfolios.map((p) =>
          p.id === portfolioId
            ? { ...p, isFavorite: response.data.isFavorite }
            : p
        )
      );

      if (onPortfolioSelect) {
        onPortfolioSelect(portfolioId);
      }
    } catch (err) {
      console.error('Failed to toggle favorite:', err.response?.data || err.message);
      alert('Failed to toggle favorite. Please try again.');
    }
  };

  const togglePortfolioList = () => {
    setIsPortfolioListOpen(!isPortfolioListOpen);
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
        <div
          className="portfolio-toggle"
          onClick={togglePortfolioList}
          style={{ cursor: 'pointer', fontWeight: 'bold', marginBottom: '10px' }}
        >
          Select Portfolio {isPortfolioListOpen ? '∨' : '>'}
        </div>
        {isPortfolioListOpen && (
          <div className="portfolio-list">
            {localPortfolios.map((portfolio) => (
              <div key={portfolio.id} className="portfolio-item">
                {renamePortfolioId === portfolio.id ? (
                  <div className="rename-container">
                    <input
                      type="text"
                      value={renameValue}
                      onChange={(e) => setRenameValue(e.target.value)}
                      className="rename-input"
                      placeholder="Name"
                    />
                    <button
                      onClick={() => handleRenamePortfolio(portfolio.id, renameValue)}
                      className="action-button save"
                    >
                      Save
                    </button>
                    <button
                      onClick={() => setRenamePortfolioId(null)}
                      className="action-button cancel"
                    >
                      Cancel
                    </button>
                  </div>
                ) : (
                  <div className="portfolio-row">
                    <span
                      onClick={() => onPortfolioSelect(portfolio.id)}
                      className="portfolio-name"
                      style={{
                        fontWeight: selectedPortfolioId === portfolio.id ? 'bold' : 'normal',
                      }}
                    >
                      {selectedPortfolioId === portfolio.id && '✔ '}
                      {portfolio.name}
                    </span>
                    <div className="portfolio-actions">
                      <button
                        onClick={() => {
                          setRenamePortfolioId(portfolio.id);
                          setRenameValue(portfolio.name);
                        }}
                        className="action-button"
                        title="Rename"
                      >
                        ✏️
                      </button>
                      <button
                        onClick={() => handleToggleFavorite(portfolio.id, portfolio.isFavorite)}
                        className="action-button"
                        title={portfolio.isFavorite ? 'Unfavorite' : 'Favorite'}
                      >
                        <span
                          style={{
                            color: portfolio.isFavorite ? '#FFD700' : '#ccc',
                          }}
                        >
                          ★
                        </span>
                      </button>
                      <button
                        onClick={() => handleDeletePortfolio(portfolio.id)}
                        className="action-button"
                        title="Delete"
                      >
                        🗑️
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      <button className="add-button" onClick={() => setShowAddModal(true)}>
        ADD STOCK
      </button>

      <div className="search-container">
        <input
          type="text"
          className="search-input"
          placeholder="Search Ticker"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      <div className="holdings-list">
        {filteredHoldings.map((holding) => (
          <div key={holding.id} className="holding-item">
            <div className="holding-info">
              <span className="ticker">{holding.stock.ticker}</span>
            </div>
            <div className="holding-actions">
              <button
                className="action-button add"
                onClick={() => handleAddShares(holding)}
                style={{ width: '25px', height: '25px' }}
              >
                <span className="plus-icon" style={{ fontSize: '15px' }}>
                  +
                </span>
              </button>
              <button
                className="action-button remove"
                onClick={() => handleRemoveShares(holding)}
                style={{ width: '25px', height: '25px', marginLeft: '1px' }}
              >
                <span className="minus-icon" style={{ fontSize: '15px' }}>
                  -
                </span>
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
            <h3>
              {isBuying ? 'Buy' : 'Sell'} {editingHolding.stock.ticker}
            </h3>
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
                <button
                  type="submit"
                  className={`submit-button ${isBuying ? 'buy' : 'sell'}`}
                >
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
              <button
                type="button"
                className="submit-button random"
                onClick={handleGenerateRandomPortfolio}
                style={{ marginTop: '0px', width: '100%' }}
              >
                Generate Random Portfolio
              </button>
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
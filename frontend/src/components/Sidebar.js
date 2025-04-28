import React, { useState, useEffect } from 'react';
import './Sidebar.css';
import api from '../api/axios.js';
import debounce from 'lodash.debounce';
// Import icons from react-icons
import { FaPencilAlt, FaTrash, FaStar, FaChevronRight } from 'react-icons/fa';

function Sidebar({ portfolios, selectedPortfolioId, onPortfolioSelect, holdings, setPortfolios, onHoldingsChange, authState }) {
  const [isVisible, setIsVisible] = useState(window.innerWidth >= 800);
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
  const [errorModal, setErrorModal] = useState({ show: false, message: '' });
  const [isAddingCash, setIsAddingCash] = useState(false);

  // Add debug logs for authState
  useEffect(() => {
    console.log('Sidebar authState:', authState);
    console.log('Is guest:', authState?.isGuest);
  }, [authState]);

  // Log portfolios for debugging
  console.log('Portfolios:', portfolios);

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
      const response = await api.get('/stocks/search', {
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
    if (ticker.length >= 1) {
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
    if (!ticker || !shares) {
      setErrorModal({
        show: true,
        message: 'Please fill in all required fields (ticker and shares)'
      });
      return;
    }

    // Check if the stock already exists in the portfolio
    const existingHolding = holdings.find(h => h.stock.ticker.toUpperCase() === ticker.toUpperCase());
    if (existingHolding) {
      setErrorModal({
        show: true,
        message: 'This stock is already in your portfolio. To add more shares, use the + button next to the existing holding.'
      });
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setErrorModal({
          show: true,
          message: 'You are not authenticated. Please log in.'
        });
        return;
      }

      const sharesNum = Number(shares);
      const priceNum = price ? Number(price) : null;

      if (isNaN(sharesNum) || sharesNum <= 0 || (price && (isNaN(priceNum) || priceNum <= 0))) {
        setErrorModal({
          show: true,
          message: 'Shares must be a positive number and price (if provided) must be a positive number'
        });
        return;
      }

      await api.post(
        `/portfolios/${selectedPortfolioId}/holdings/add`,
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

      // Reset form and close modal on success
      setTicker('');
      setShares('');
      setPrice('');
      setShowAddModal(false);
      onHoldingsChange();
    } catch (error) {
      console.error('Error adding holding:', error);
      setErrorModal({
        show: true,
        message: 'Failed to add stock. Please try again.'
      });
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

    if (!editingHolding || !shares) {
      alert('Please fill in the required fields');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('You are not authenticated. Please log in.');
        return;
      }

      const sharesNum = Number(shares);
      const priceNum = price ? Number(price) : null;

      if (isNaN(sharesNum) || sharesNum <= 0 || (price && (isNaN(priceNum) || priceNum <= 0))) {
        alert('Shares must be a positive number and price (if provided) must be a positive number');
        return;
      }

      if (isBuying) {
        const existingHolding = holdings.find((h) => h.stock.ticker === editingHolding.stock.ticker);

        if (existingHolding) {
          await api.put(
            `/portfolios/holdings/${existingHolding.id}`,
            null,
            {
              params: {
                shares: existingHolding.shares + sharesNum,
              },
              headers: { Authorization: `Bearer ${token}` },
            }
          );
        } else {
          await api.post(
            `/portfolios/${selectedPortfolioId}/holdings/add`,
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

        await api.put(
          `/portfolios/holdings/${editingHolding.id}`,
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
      onHoldingsChange();
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
        // Handle guest portfolio creation
        const guestPortfolios = JSON.parse(localStorage.getItem('guestPortfolios') || '[]');
        const newPortfolio = {
          id: Date.now(), // Use timestamp as temporary ID
          name: newPortfolioName,
          holdings: []
        };
        guestPortfolios.push(newPortfolio);
        localStorage.setItem('guestPortfolios', JSON.stringify(guestPortfolios));
        setNewPortfolioName('');
        setShowNewPortfolioModal(false);
        if (onPortfolioSelect) {
          onPortfolioSelect(newPortfolio.id);
        }
        return;
      }

      const response = await api.post(
        '/portfolios/create',
        null,
        {
          params: {
            name: newPortfolioName,
          },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

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

      const response = await api.post(
        '/portfolios/create-random',
        null,
        {
          params: {
            name: portfolioName,
          },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

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

      await api.delete(`/portfolios/${portfolioId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (onPortfolioSelect && selectedPortfolioId === portfolioId) {
        const remainingPortfolios = portfolios.filter((p) => p.id !== portfolioId);
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

      await api.put(
        `/portfolios/${portfolioId}/rename`,
        null,
        {
          params: { name: newName },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      console.log('Rename response:', response.data); // Debug

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

      const response = await api.patch(
        `/portfolios/${portfolioId}/favorite`,
        null,
        {
          params: { isFavorite: !isFavorite },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      console.log('Favorite response:', response.data); // Debug

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

  // Update visibility when window is resized
  useEffect(() => {
    const handleResize = () => {
      const windowWidth = window.innerWidth;
      const newIsVisible = windowWidth >= 800;
      setIsVisible(newIsVisible);
      
      // Update heatmap-main class based on sidebar visibility
      const heatmapMain = document.querySelector('.heatmap-main');
      if (heatmapMain) {
        if (!newIsVisible) {
          heatmapMain.classList.add('sidebar-hidden');
        } else {
          heatmapMain.classList.remove('sidebar-hidden');
        }
      }
    };

    window.addEventListener('resize', handleResize);
    
    // Call once to set initial state
    handleResize();
    
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const toggleSidebar = () => {
    const newIsVisible = !isVisible;
    setIsVisible(newIsVisible);
    
    // Toggle class on heatmap-main before the sidebar animation starts
    const heatmapMain = document.querySelector('.heatmap-main');
    if (heatmapMain) {
      if (!newIsVisible) {
        // When hiding sidebar, add class immediately
        heatmapMain.classList.add('sidebar-hidden');
      } else {
        // When showing sidebar, remove class immediately
        heatmapMain.classList.remove('sidebar-hidden');
      }
    }
  };

  const handleAddCash = async (e) => {
    e.preventDefault();
    if (!shares) {
      setErrorModal({
        show: true,
        message: 'Please enter an amount'
      });
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setErrorModal({
          show: true,
          message: 'You are not authenticated. Please log in.'
        });
        return;
      }

      const amount = Number(shares);
      if (isNaN(amount) || amount <= 0) {
        setErrorModal({
          show: true,
          message: 'Amount must be a positive number'
        });
        return;
      }

      await api.post(
        `/portfolios/${selectedPortfolioId}/holdings/add`,
        null,
        {
          params: {
            ticker: 'CASH',
            shares: amount,
            purchasePrice: 1.0,
            purchaseDate: new Date().toISOString().split('T')[0],
          },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      // Reset form and close modal on success
      setShares('');
      setShowAddModal(false);
      setIsAddingCash(false);
      onHoldingsChange();
    } catch (error) {
      console.error('Error adding cash:', error);
      setErrorModal({
        show: true,
        message: 'Failed to add cash. Please try again.'
      });
    }
  };

  return (
    <>
      <div className="sidebar-container">
        <div className={`sidebar ${isVisible ? 'visible' : ''}`}>
          <button
            className="new-portfolio-button"
            onClick={() => setShowNewPortfolioModal(true)}
          >
            NEW PORTFOLIO
          </button>
          
          <div className="portfolio-selector">
            <select
              className="portfolio-dropdown"
              value={selectedPortfolioId || ''}
              onChange={(e) => onPortfolioSelect(e.target.value)}
            >
              <option value="" disabled>
                Select Portfolio
              </option>
              {portfolios.map((portfolio) => (
                <option key={portfolio.id} value={portfolio.id}>
                  {portfolio.name}
                </option>
              ))}
            </select>
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
        </div>
        
        <button 
          className="sidebar-toggle" 
          onClick={toggleSidebar}
          aria-label="Toggle sidebar"
        >
          <FaChevronRight className={`arrow ${isVisible ? 'left' : 'right'}`} />
        </button>
      </div>
      
      {/* All modals moved outside sidebar */}
        
      {showAddModal && (
        <div className="edit-modal">
          <div className="edit-content">
            <h3>{isAddingCash ? 'Add Cash Position' : 'Add New Holding'}</h3>
            {isAddingCash ? (
              <form onSubmit={handleAddCash}>
                <div className="input-group">
                  <label>Amount ($):</label>
                  <input
                    type="number"
                    value={shares}
                    onChange={(e) => setShares(e.target.value)}
                    placeholder="Enter amount"
                    step="0.01"
                    min="0.01"
                    required
                  />
                </div>
                <div className="modal-actions">
                  <button type="submit" className="submit-button buy">
                    Add Cash
                  </button>
                  <button
                    type="button"
                    className="cancel-button"
                    onClick={() => {
                      setShowAddModal(false);
                      setShares('');
                      setIsAddingCash(false);
                    }}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <form onSubmit={handleAddNewHolding}>
                <div style={{ fontSize: '10px', color: 'red', marginBottom: '8px', fontStyle: 'italic' }}>
                  *Only S&P 500 stocks adding all NYSE and Crypto soon
                </div>
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
                  <label>Purchase Price (optional):</label>
                  <input
                    type="number"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    placeholder="Enter price per share (optional)"
                    step="0.01"
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
                <button
                  type="button"
                  className="add-cash-button"
                  onClick={() => {
                    setIsAddingCash(true);
                    setTicker('');
                    setPrice('');
                  }}
                >
                  Add Cash Position
                </button>
              </form>
            )}
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
                />
              </div>
              <div className="input-group">
                <label>{isBuying ? 'Purchase Price / Average Cost Basis: (optional)' : 'Selling Price: (optional)'}</label>
                <input
                  type="number"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="Enter price per share (optional)"
                  step="0.01"
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
              {authState.isGuest && (
                <div style={{ fontSize: '12px', color: '#666', marginTop: '8px', fontStyle: 'italic', textAlign: 'center' }}>
                  You are currently not logged in, login or register to save your Portfolio
                </div>
              )}
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

      {/* Error Modal */}
      {errorModal.show && (
        <div className="modal-overlay">
          <div className="error-modal">
            <div className="error-modal-content">
              <h3>Oops!</h3>
              <p>{errorModal.message}</p>
              <button 
                className="error-modal-button"
                onClick={() => setErrorModal({ show: false, message: '' })}
              >
                Got it
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default Sidebar;
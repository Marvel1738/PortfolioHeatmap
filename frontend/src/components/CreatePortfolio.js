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
 * Creates a portfolio via /portfolios/create, manages a list of holdings with add,
 * remove, and update functionality, and submits via /portfolios/{id}/holdings/add.
 * Supports fractional shares and provides a seamless onboarding experience.
 * 
 * @returns {JSX.Element} The rendered portfolio creation UI
 */
function CreatePortfolio() {
  // State to hold the portfolio name entered by the user, initialized as empty string
  const [portfolioName, setPortfolioName] = useState('');
  // State for the current stock ticker input, initialized as empty string
  const [ticker, setTicker] = useState('');
  // State for the current shares input, stored as string to handle decimals
  const [shares, setShares] = useState('');
  // State for the current purchase price input, stored as string for precision
  const [purchasePrice, setPurchasePrice] = useState('');
  // State to hold the list of holdings added by the user, initialized as empty array
  const [holdings, setHoldings] = useState([]);
  // State to display error messages to the user, initialized as empty string
  const [error, setError] = useState('');
  // State to store the ID of the created portfolio after /portfolios/create call
  const [portfolioId, setPortfolioId] = useState(null);
  // State to track the index of the holding being edited, null when not editing
  const [editingIndex, setEditingIndex] = useState(null);
  // Hook to navigate to other routes (e.g., /heatmap) after submission
  const navigate = useNavigate();

  /**
   * Handles creating a new portfolio when the user submits the name form.
   * Sends a POST request to /portfolios/create with the portfolio name and stores
   * the returned portfolio ID for subsequent holding additions.
   * 
   * @param {Event} e - The form submission event triggered by the user
   */
  const handleCreatePortfolio = async (e) => {
    // Prevent default form submission behavior (page reload)
    e.preventDefault();
    // Validate that a portfolio name is provided
    if (!portfolioName) {
      setError('Please enter a portfolio name.');
      return;
    }

    try {
      // Retrieve the JWT token from localStorage for authentication
      const token = localStorage.getItem('token');
      // Send POST request to create the portfolio
      const response = await axios.post(
        'http://localhost:8080/portfolios/create',
        null, // No request body, using query params
        {
          params: { name: portfolioName }, // Pass portfolio name as query parameter
          headers: { 'Authorization': `Bearer ${token}` }, // Include JWT in header
        }
      );
      // Store the portfolio ID from the response
      setPortfolioId(response.data.id);
      // Clear any previous error messages
      setError('');
    } catch (err) {
      // Handle errors from the API call (e.g., network issues, duplicate name)
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Failed to create portfolio: ' + errorMessage);
    }
  };

  /**
   * Handles adding a new holding or updating an existing one in the holdings list.
   * Validates inputs, supports fractional shares, and updates state accordingly.
   * If editing, replaces the holding at editingIndex; otherwise, adds a new one.
   * 
   * @param {Event} e - The form submission event for adding or updating a holding
   */
  const handleAddOrUpdateHolding = (e) => {
    // Prevent default form behavior
    e.preventDefault();
    // Ensure all required fields are filled
    if (!ticker || !shares || !purchasePrice) {
      setError('Please fill in all holding fields.');
      return;
    }
    // Convert inputs to numbers for validation and storage
    const sharesNum = Number(shares);
    const priceNum = Number(purchasePrice);
    // Validate that shares and price are positive numbers
    if (isNaN(sharesNum) || sharesNum <= 0 || isNaN(priceNum) || priceNum <= 0) {
      setError('Shares and purchase price must be positive numbers.');
      return;
    }

    // Create a new holding object with validated data
    const newHolding = {
      ticker: ticker.toUpperCase(), // Standardize ticker to uppercase
      shares: sharesNum, // Store as number, supports decimals
      purchasePrice: priceNum, // Store as number
      purchaseDate: new Date().toISOString().split('T')[0], // Current date in YYYY-MM-DD
    };

    if (editingIndex !== null) {
      // If editing, update the existing holding at the specified index
      const updatedHoldings = [...holdings];
      updatedHoldings[editingIndex] = newHolding;
      setHoldings(updatedHoldings);
      setEditingIndex(null); // Exit edit mode after update
    } else {
      // If not editing, add the new holding to the list
      setHoldings([...holdings, newHolding]);
    }

    // Clear form inputs for the next entry
    setTicker('');
    setShares('');
    setPurchasePrice('');
    // Clear any previous errors
    setError('');
  };

  /**
   * Handles removing a holding from the list by its index.
   * Filters out the holding and updates the state, only affects pre-submission list.
   * 
   * @param {number} index - The index of the holding to remove from the list
   */
  const handleRemoveHolding = (index) => {
    // Filter out the holding at the specified index
    setHoldings(holdings.filter((_, i) => i !== index));
    // Clear any error messages
    setError('');
  };

  /**
   * Handles initiating the edit process for a specific holding.
   * Populates the form with the holding's current values and sets editingIndex.
   * 
   * @param {number} index - The index of the holding to edit in the list
   */
  const handleEditHolding = (index) => {
    // Get the holding to edit
    const holding = holdings[index];
    // Populate form fields with current values
    setTicker(holding.ticker);
    setShares(holding.shares.toString()); // Convert to string for input compatibility
    setPurchasePrice(holding.purchasePrice.toString());
    // Set the index to indicate editing mode
    setEditingIndex(index);
    // Clear any error messages
    setError('');
  };

  /**
   * Handles submitting the final list of holdings to the backend.
   * Sends a POST request to /portfolios/{portfolioId}/holdings/add for each holding,
   * then redirects to the heatmap page on success.
   * 
   * @param {Event} e - The form submission event for final portfolio submission
   */
  const handleSubmit = async (e) => {
    // Prevent default behavior
    e.preventDefault();
    // Ensure a portfolio has been created
    if (!portfolioId) {
      setError('Please create a portfolio first.');
      return;
    }
    // Ensure at least one holding is added
    if (holdings.length === 0) {
      setError('Please add at least one holding.');
      return;
    }

    try {
      // Retrieve JWT token for authentication
      const token = localStorage.getItem('token');
      // Iterate over holdings and submit each to the backend
      for (const holding of holdings) {
        await axios.post(
          `http://localhost:8080/portfolios/${portfolioId}/holdings/add`,
          null, // No request body, using query params
          {
            params: {
              ticker: holding.ticker,
              shares: holding.shares, // Pass as number, backend handles Double
              purchasePrice: holding.purchasePrice,
              purchaseDate: holding.purchaseDate,
            },
            headers: { 'Authorization': `Bearer ${token}` }, // Include JWT
          }
        );
      }
      // Clear errors on success
      setError('');
      // Redirect to heatmap page
      navigate('/heatmap');
    } catch (err) {
      // Handle errors from API calls (e.g., invalid ticker, server issues)
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Failed to add holdings: ' + errorMessage);
    }
  };

  // JSX to render the portfolio creation UI
  return (
    // Main container div with CSS class for styling
    <div className="create-portfolio">
      {/* Heading for the page */}
      <h1>Create Your First Portfolio</h1>
      {/* Show portfolio name form if portfolio not yet created */}
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
      {/* Show holdings form and list if portfolio is created */}
      {portfolioId && (
        <>
          <form onSubmit={handleAddOrUpdateHolding}>
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
                placeholder="e.g., 10.5"
                step="0.01" // Allow fractional shares
                min="0.01" // Ensure positive values
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
            {/* Button text changes based on edit mode */}
            <div className="addHolding">
            <button id="addHolding"type="submit">
              {editingIndex !== null ? 'Update Holding' : 'Add Holding'}
              </button>
            </div>
          </form>
          {/* Display holdings list if there are any */}
          {holdings.length > 0 && (
            <div className="holdings-list">
              <h2>Your Holdings:</h2>
              <ul>
                {holdings.map((holding, index) => (
                  <li key={index}>
                    {holding.ticker}: {holding.shares} shares @ ${holding.purchasePrice}
                    {/* Edit button to modify the holding */}
                    <button
                      className="edit-button"
                      onClick={() => handleEditHolding(index)}
                    >
                      Edit
                    </button>
                    {/* Remove button to delete the holding */}
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
          {/* Final submission button */}
          <button className="submit-portfolio" onClick={handleSubmit}>
            Finish Portfolio
          </button>
        </>
      )}
      {/* Display error message if present */}
      {error && <p className="error-message">{error}</p>}
    </div>
  );
}

// Export the component as the default export
export default CreatePortfolio;
// frontend/src/components/Preview.js

import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import './Preview.css';

/**
 * Preview component displays created portfolios before final confirmation.
 * Allows adding more portfolios or confirming to proceed to heatmap.
 * 
 * @returns {JSX.Element} The rendered preview UI
 */
function Preview() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const portfolios = state?.portfolios || [];

  /**
   * Navigates back to create-portfolio to add more portfolios.
   */
  const handleAddMore = () => {
    navigate('/create-portfolio', { state: { portfolios } });
  };

  /**
   * Confirms portfolios and navigates to loading page.
   */
  const handleConfirm = () => {
    navigate('/loading', { state: { portfolios } });
  };

  return (
    <div className="preview">
      <h1>Preview Your Portfolios</h1>
      {portfolios.length === 0 ? (
        <p>No portfolios to preview.</p>
      ) : (
        <ul>
          {portfolios.map((portfolio) => (
            <li key={portfolio.id}>
              <h2>{portfolio.name}</h2>
              <ul>
                {portfolio.holdings.map((holding, index) => (
                  <li key={index}>
                    {holding.ticker}: {holding.shares} shares @ ${holding.purchasePrice}
                  </li>
                ))}
              </ul>
            </li>
          ))}
        </ul>
      )}
      <button onClick={handleAddMore}>Add More Portfolios</button>
      <button onClick={handleConfirm}>Confirm</button>
    </div>
  );
}

export default Preview;
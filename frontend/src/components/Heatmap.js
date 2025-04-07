// frontend/src/components/Heatmap.js

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import * as d3 from 'd3';
import './Heatmap.css';
import Sidebar from './Sidebar';

/**
 * Heatmap component stub for displaying portfolio visualizations.
 * To be expanded with squares, dropdown, and sidebar.
 * 
 * @returns {JSX.Element} The rendered heatmap UI
 */
function Heatmap() {
  const [portfolios, setPortfolios] = useState([]);
  const [selectedPortfolioId, setSelectedPortfolioId] = useState(null);
  const [holdings, setHoldings] = useState([]);
  const [timeframe, setTimeframe] = useState('1d');
  const [error, setError] = useState('');
  const [showPercentChange, setShowPercentChange] = useState(true);
  const [showDollarChange, setShowDollarChange] = useState(false);

  // Constants for heatmap
  const BASE_WIDTH = 1200;
  const BASE_HEIGHT = 800;
  const ASPECT_RATIO = BASE_WIDTH / BASE_HEIGHT;
  const MIN_RECTANGLE_SIZE = 60;
  const [scale, setScale] = useState(1);

  const timeframeOptions = [
    { value: '1d', label: '1 Day' },
    { value: '1w', label: '1 Week' },
    { value: '1m', label: '1 Month' },
    { value: '3m', label: '3 Months' },
    { value: '6m', label: '6 Months' },
    { value: 'ytd', label: 'YTD' },
    { value: '1y', label: '1 Year' },
    { value: 'total', label: 'Total Gain/Loss' }
  ];

  // Handle window resize
  useEffect(() => {
    const handleResize = () => {
      const container = document.querySelector('.heatmap-visualization');
      if (container) {
        const containerWidth = container.clientWidth;
        const containerHeight = container.clientHeight;
        const containerRatio = containerWidth / containerHeight;
        
        // Calculate scale based on container size while maintaining aspect ratio
        if (containerRatio > ASPECT_RATIO) {
          // Height limited
          setScale(containerHeight / BASE_HEIGHT);
        } else {
          // Width limited
          setScale(containerWidth / BASE_WIDTH);
        }
      }
    };

    window.addEventListener('resize', handleResize);
    handleResize(); // Initial size

    return () => window.removeEventListener('resize', handleResize);
  }, [ASPECT_RATIO, BASE_HEIGHT, BASE_WIDTH]);

  // Fetch portfolios on component mount
  useEffect(() => {
    const fetchPortfolios = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) throw new Error('No token found');
        
        const response = await axios.get('http://localhost:8080/portfolios/user', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        setPortfolios(response.data);
        if (response.data.length > 0) {
          setSelectedPortfolioId(response.data[0].id);
        }
      } catch (err) {
        setError('Failed to fetch portfolios: ' + err.message);
      }
    };

    fetchPortfolios();
  }, []);

  // Fetch holdings when portfolio is selected
  useEffect(() => {
    if (!selectedPortfolioId) return;

    const fetchHoldings = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) throw new Error('No token found');

        console.log('Fetching holdings for portfolio:', selectedPortfolioId);
        const response = await axios.get(`http://localhost:8080/portfolios/${selectedPortfolioId}?timeframe=${timeframe}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });

        console.log('Full portfolio response:', response.data);
        
        // Calculate total portfolio value and allocations
        let holdingsData = response.data.openPositions || [];
        console.log('Initial holdings data:', holdingsData);

        // If any holding is just an ID, fetch its full details
        const fullHoldingsData = await Promise.all(
          holdingsData.map(async (holding) => {
            // If holding is just an ID number or doesn't have complete data, fetch its details
            if (typeof holding === 'number' || !holding.stock || !holding.shares) {
              try {
                const holdingId = typeof holding === 'number' ? holding : holding.id;
                const holdingResponse = await axios.get(
                  `http://localhost:8080/portfolios/holdings/${holdingId}`,
                  { headers: { 'Authorization': `Bearer ${token}` } }
                );
                
                console.log('Fetched holding details:', holdingResponse.data);
                return {
                  ...holdingResponse.data,
                  percentChange: response.data.timeframePercentageChanges[holdingId] || 0
                };
              } catch (err) {
                console.warn('Failed to fetch holding details:', err);
                return null;
              }
            }
            
            // If we already have the holding data, just use the timeframe change
            console.log('Using existing holding data:', holding);
            return {
              ...holding,
              percentChange: response.data.timeframePercentageChanges[holding.id] || 0
            };
          })
        );

        // Filter out null values and invalid holdings
        const validHoldings = fullHoldingsData.filter(h => {
          console.log('Checking holding:', h);
          console.log('Holding details:', {
            id: h?.id,
            ticker: h?.stock?.ticker,
            shares: h?.shares,
            purchasePrice: h?.purchasePrice,
            currentPrice: h?.currentPrice,
            stock: h?.stock
          });
          
          // More lenient validation - only require essential fields
          const isValid = h && h.stock && h.stock.ticker && h.shares && h.purchasePrice;
          if (!isValid) {
            console.warn('Invalid holding details:', {
              hasHolding: !!h,
              hasStock: !!h?.stock,
              hasTicker: !!h?.stock?.ticker,
              hasShares: !!h?.shares,
              hasPurchasePrice: !!h?.purchasePrice,
              hasCurrentPrice: !!h?.currentPrice,
              holding: h
            });
          }
          return isValid;
        });
        
        console.log('Valid holdings:', validHoldings);

        if (validHoldings.length === 0) {
          setError('Click ADD STOCK to add stocks to your portfolio!');
          setHoldings([]);
          return;
        }

        // Calculate total portfolio value using currentPrice if available, otherwise use purchasePrice
        const totalValue = validHoldings.reduce((sum, h) => {
          const price = h.currentPrice || h.purchasePrice;
          const value = h.shares * price;
          console.log(`Holding value for ${h.stock.ticker}:`, value, `(using price: ${price})`);
          return sum + value;
        }, 0);
        
        console.log('Total portfolio value:', totalValue);

        const holdingsWithAllocation = validHoldings.map(holding => {
          const price = holding.currentPrice || holding.purchasePrice;
          const currentValue = holding.shares * price;
          const allocation = currentValue / totalValue;
          console.log(`${holding.stock.ticker} allocation:`, allocation * 100, '%', `(value: ${currentValue})`);
          return {
            ...holding,
            currentValue,
            allocation
          };
        });

        // Sort by allocation in descending order
        holdingsWithAllocation.sort((a, b) => b.allocation - a.allocation);
        console.log('Final processed holdings:', holdingsWithAllocation);
        setHoldings(holdingsWithAllocation);
      } catch (err) {
        console.error('Error fetching holdings:', err);
        setError('Failed to fetch holdings: ' + err.message);
      }
    };

    fetchHoldings();
  }, [selectedPortfolioId, timeframe]);

  // Get color based on percentage change (Finviz style)
  const getColor = (percentChange) => {
    const pc = Number(percentChange) || 0;
    const baseGray = { r:43, g:49, b:58}; // Using CSS variable --background-primary

    // Cap the percentage change at ±3% for color scaling purposes
    const cappedPC = Math.min(Math.abs(pc), 3); // Cap at 3% for scaling
    const factor = cappedPC / 3; // A value from 0 to 1 based on the percentage change

    // Define target colors for green and red
    const green = { r: 0, g: 153, b: 51 }; // Pure green
    const red = { r: 204, g: 51, b: 51 }; // Pure red

    // Interpolate between gray and the target color (green or red)
    let targetColor;
    if (pc > 0) {
      targetColor = green;
    } else if (pc < 0) {
      targetColor = red;
    } else {
      return `rgb(${baseGray.r}, ${baseGray.g}, ${baseGray.b})`; // Neutral gray for 0%
    }

    // Interpolate the RGB values between gray and the target color
    const r = Math.round(baseGray.r + (targetColor.r - baseGray.r) * factor);
    const g = Math.round(baseGray.g + (targetColor.g - baseGray.g) * factor);
    const b = Math.round(baseGray.b + (targetColor.b - baseGray.b) * factor);

    // Scale opacity from 0.5 (at 0%) to 0.95 (at 3% or higher)
    const opacity = 0.7 + factor * (0.95 - 0.5);

    return `rgba(${r}, ${g}, ${b}, ${opacity})`;
  };

  // Create treemap layout
  const createTreemap = (holdings) => {
    if (!holdings.length) return [];

    const hierarchyData = {
      name: 'portfolio',
      children: holdings.map(h => ({
        name: h.stock.ticker,
        value: h.allocation,
        holding: h
      }))
    };

    const root = d3.hierarchy(hierarchyData)
      .sum(d => d.value)
      .sort((a, b) => b.value - a.value);

    const layout = d3.treemap()
      .size([BASE_WIDTH, BASE_HEIGHT])
      .padding(2) // Increased from 1 to 2 for slightly bigger gaps
      .round(true);

    layout(root);
    return root.leaves();
  };

  const treeMapData = createTreemap(holdings);

  const getColorForPercentage = (percentage) => {
    if (percentage > 0) {
      return `rgba(0, 255, 0, ${Math.min(Math.abs(percentage) / 100, 0.8)})`;
    } else if (percentage < 0) {
      return `rgba(255, 0, 0, ${Math.min(Math.abs(percentage) / 100, 0.8)})`;
    }
    return 'rgba(128, 128, 128, 0.5)';
  };

  const calculateWidth = (holding) => {
    // Scale the width based on the holding's percentage of the total portfolio value
    const scaledWidth = (holding.value / 100) * (BASE_WIDTH * 0.8); // Use 80% of base width
    return Math.max(scaledWidth, MIN_RECTANGLE_SIZE);
  };

  const calculateHeight = (holding) => {
    // Scale the height based on the holding's percentage of the total portfolio value
    const scaledHeight = (holding.value / 100) * (BASE_HEIGHT * 0.8); // Use 80% of base height
    return Math.max(scaledHeight, MIN_RECTANGLE_SIZE);
  };

  const handleCellClick = (holding) => {
    // You can add click handling logic here
    console.log('Clicked holding:', holding);
  };

  const handlePortfolioSelect = (portfolioId) => {
    setSelectedPortfolioId(portfolioId);
  };

  return (
    <div className="heatmap-container">
      <Sidebar 
        portfolios={portfolios}
        selectedPortfolioId={selectedPortfolioId}
        onPortfolioSelect={handlePortfolioSelect}
        holdings={holdings}
      />
      <div className="heatmap-main">
        <div className="heatmap-controls">
          <div className="timeframe-selector">
            <label>Timeframe: </label>
            <select value={timeframe} onChange={(e) => setTimeframe(e.target.value)}>
              {timeframeOptions.map(option => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
          </div>
          <div className="display-options">
            <label>
              <input
                type="checkbox"
                checked={showPercentChange}
                onChange={(e) => setShowPercentChange(e.target.checked)}
              />
              % Change
            </label>
            <label>
              <input
                type="checkbox"
                checked={showDollarChange}
                onChange={(e) => setShowDollarChange(e.target.checked)}
              />
              $ Change
            </label>
          </div>
        </div>
        <div className="heatmap">
          <div className="heatmap-visualization">
            <div 
              className="heatmap-content"
              style={{
                position: 'absolute',
                width: `${BASE_WIDTH}px`,
                height: `${BASE_HEIGHT}px`,
                transform: `scale(${scale})`,
                transformOrigin: 'top left'
              }}
            >
              {holdings.length === 0 && error && <div className="error-message">{error}</div>}
              {treeMapData.map((d, i) => {
  const holding = d.data.holding;
  const width = Math.max(d.x1 - d.x0, MIN_RECTANGLE_SIZE);
  const height = Math.max(d.y1 - d.y0, MIN_RECTANGLE_SIZE);
  const percentChange = holding.percentChange;
  
  let dollarChange;
  if (timeframe === 'total') {
    const totalValue = holding.currentValue;
    dollarChange = (totalValue * percentChange) / 100;
  } else {
    const pricePerShare = holding.currentPrice || holding.purchasePrice || 0; // Fallback to purchasePrice
    dollarChange = (pricePerShare * percentChange) / 100;
  }
  
  const fontSize = Math.min(width, height) * 0.12;
                
                return (
                  <div
                    key={i}
                    className="heatmap-rect"
                    style={{
                      left: `${d.x0}px`,
                      top: `${d.y0}px`,
                      width: `${width}px`,
                      height: `${height}px`,
                      backgroundColor: getColor(percentChange),
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'center',
                      alignItems: 'center',
                      padding: '5px',
                      boxSizing: 'border-box',
                      color: '#ffffff',
                      fontSize: `${fontSize}px`,
                      textAlign: 'center',
                      overflow: 'hidden',
                      fontFamily: 'Arial, sans-serif',
                      textShadow: '1px 1px 1px rgba(0, 0, 0, 0.5)'
                    }}
                  >
                    <div className="ticker" style={{ fontSize: `${fontSize}px` }}>{holding.stock.ticker}</div>
                    {showPercentChange && (
                      <div className="change" style={{ fontSize: `${fontSize * 0.9}px` }}>
                        {percentChange > 0 ? '+' : ''}{percentChange.toFixed(2)}%
                      </div>
                    )}
                    {showDollarChange && (
                      <div className="change" style={{ fontSize: `${fontSize * 0.9}px` }}>
                        {dollarChange >= 0 ? '+' : ''}{dollarChange.toFixed(2)}$
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Heatmap;
// frontend/src/components/Heatmap.js

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import * as d3 from 'd3';
import './Heatmap.css';

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

  // Constants for heatmap
  const BASE_WIDTH = 1200;
  const BASE_HEIGHT = 800;
  const ASPECT_RATIO = BASE_WIDTH / BASE_HEIGHT;
  const MIN_RECTANGLE_SIZE = 60;
  const [scale, setScale] = useState(1);

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
  }, []);

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
        const response = await axios.get(`http://localhost:8080/portfolios/${selectedPortfolioId}`, {
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
                
                // Get latest price for the stock
                const priceResponse = await axios.get(
                  `http://localhost:8080/stocks/price/${holdingResponse.data.stock.ticker}`,
                  { headers: { 'Authorization': `Bearer ${token}` } }
                );
                
                return {
                  ...holdingResponse.data,
                  currentPrice: priceResponse.data.price,
                  percentChange: ((priceResponse.data.price - holdingResponse.data.purchasePrice) / holdingResponse.data.purchasePrice) * 100
                };
              } catch (err) {
                console.warn('Failed to fetch holding details:', err);
                return null;
              }
            }
            
            // If we already have the holding data, just get the latest price
            try {
              const priceResponse = await axios.get(
                `http://localhost:8080/stocks/price/${holding.stock.ticker}`,
                { headers: { 'Authorization': `Bearer ${token}` } }
              );
              
              return {
                ...holding,
                currentPrice: priceResponse.data.price,
                percentChange: ((priceResponse.data.price - holding.purchasePrice) / holding.purchasePrice) * 100
              };
            } catch (err) {
              console.warn('Failed to fetch price for holding:', err);
              return null;
            }
          })
        );

        // Filter out null values and invalid holdings
        const validHoldings = fullHoldingsData.filter(h => {
          const isValid = h && h.stock && h.stock.ticker && h.shares && h.purchasePrice && h.currentPrice;
          if (!isValid) {
            console.warn('Filtered out invalid holding:', h);
          }
          return isValid;
        });
        
        console.log('Valid holdings:', validHoldings);

        if (validHoldings.length === 0) {
          setError('No valid holdings found in portfolio');
          setHoldings([]);
          return;
        }

        const totalValue = validHoldings.reduce((sum, h) => {
          const value = h.shares * h.currentPrice;
          console.log(`Holding value for ${h.stock.ticker}:`, value);
          return sum + value;
        }, 0);
        
        console.log('Total portfolio value:', totalValue);

        const holdingsWithAllocation = validHoldings.map(holding => {
          const currentValue = holding.shares * holding.currentPrice;
          const allocation = currentValue / totalValue;
          console.log(`${holding.stock.ticker} allocation:`, allocation * 100, '%');
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
  }, [selectedPortfolioId]);

  // Get color based on percentage change (Finviz style)
  const getColor = (percentChange) => {
    const pc = Number(percentChange) || 0;
    if (pc >= 3) return '#1C7D43';
    if (pc >= 2) return '#1D8946';
    if (pc >= 1) return '#209650';
    if (pc > 0) return '#23A359';
    if (pc === 0) return '#424242';
    if (pc >= -1) return '#B82E2E';
    if (pc >= -2) return '#A82828';
    if (pc >= -3) return '#982323';
    return '#881E1E';
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
      .padding(1)
      .round(true);

    layout(root);
    return root.leaves();
  };

  const treeMapData = createTreemap(holdings);

  return (
    <div className="heatmap-container">
      <div className="heatmap-controls">
        <div className="portfolio-selector">
          <label>Portfolio: </label>
          <select 
            value={selectedPortfolioId || ''} 
            onChange={(e) => setSelectedPortfolioId(e.target.value)}
          >
            {portfolios.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>

        <div className="timeframe-selector">
          <label>Timeframe: </label>
          <select value={timeframe} onChange={(e) => setTimeframe(e.target.value)}>
            <option value="1d">1 Day</option>
            <option value="1w">1 Week</option>
            <option value="1m">1 Month</option>
            <option value="3m">3 Months</option>
            <option value="6m">6 Months</option>
            <option value="ytd">YTD</option>
            <option value="1y">1 Year</option>
            <option value="total">Total Gain/Loss</option>
          </select>
        </div>
      </div>

      <div className="heatmap-visualization">
        <div 
          className="heatmap-content"
          style={{
            position: 'relative',
            width: `${BASE_WIDTH}px`,
            height: `${BASE_HEIGHT}px`,
            transform: `scale(${scale})`,
            transformOrigin: 'top left'
          }}
        >
          {error && <div className="error-message">{error}</div>}
          {treeMapData.map((d, i) => {
            const holding = d.data.holding;
            const width = Math.max(d.x1 - d.x0, MIN_RECTANGLE_SIZE);
            const height = Math.max(d.y1 - d.y0, MIN_RECTANGLE_SIZE);
            const percentChange = holding.percentChange;
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
                  fontFamily: 'Arial, sans-serif'
                }}
              >
                <div className="ticker">{holding.stock.ticker}</div>
                <div className="change">
                  {percentChange > 0 ? '+' : ''}{percentChange.toFixed(2)}%
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export default Heatmap;
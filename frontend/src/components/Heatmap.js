import React, { useState, useEffect } from 'react';
import axios from 'axios';
import * as d3 from 'd3';
import './Heatmap.css';
import Sidebar from './Sidebar.js';
import ReactDOM from 'react-dom';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip as ChartTooltip,
  Legend,
} from 'chart.js';
import { FaPencilAlt, FaTrash } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ChartTooltip,
  Legend
);

/**
 * Heatmap component for displaying portfolio visualizations with a tooltip on hover.
 * 
 * @returns {JSX.Element} The rendered heatmap UI
 */
function Heatmap() {
  const navigate = useNavigate();
  const [portfolios, setPortfolios] = useState([]);
  const [selectedPortfolioId, setSelectedPortfolioId] = useState(null);
  const [portfolioData, setPortfolioData] = useState(null);
  const [holdings, setHoldings] = useState([]);
  const [timeframe, setTimeframe] = useState('1d');
  const [error, setError] = useState('');
  const [showPercentChange, setShowPercentChange] = useState(true);
  const [showDollarChange, setShowDollarChange] = useState(false);
  const [tooltip, setTooltip] = useState({
    visible: false,
    x: 0,
    y: 0,
    data: null,
    chartData: null,
    chartError: '',
  });
  const [chartCache, setChartCache] = useState({});
  const [renamePortfolioId, setRenamePortfolioId] = useState(null);
  const [renameValue, setRenameValue] = useState('');

  const BASE_WIDTH = 1200;
  const BASE_HEIGHT = 800;
  const ASPECT_RATIO = BASE_WIDTH / BASE_HEIGHT;
  const MIN_RECTANGLE_SIZE = 30;
  const [scale, setScale] = useState(1);

  const timeframeOptions = [
    { value: '1d', label: '1 Day' },
    { value: '1w', label: '1 Week' },
    { value: '1m', label: '1 Month' },
    { value: '3m', label: '3 Months' },
    { value: '6m', label: '6 Months' },
    { value: 'ytd', label: 'YTD' },
    { value: '1y', label: '1 Year' },
    { value: 'total', label: 'Total Gain/Loss' },
  ];

  const timeframeRanges = {
    '1d': 3,
    '1w': 6,
    '1m': 9,
    '3m': 15,
    '6m': 24,
    'ytd': 30,
    '1y': 30,
    'total': 60,
  };

  // Handle window resize
  useEffect(() => {
    const handleResize = () => {
      const container = document.querySelector('.heatmap-visualization');
      if (container) {
        const containerWidth = container.clientWidth;
        const containerHeight = container.clientHeight;
        const containerRatio = containerWidth / containerHeight;

        if (containerRatio > ASPECT_RATIO) {
          setScale(containerHeight / BASE_HEIGHT);
        } else {
          setScale(containerWidth / BASE_WIDTH);
        }
      }
    };

    window.addEventListener('resize', handleResize);
    handleResize();
    return () => window.removeEventListener('resize', handleResize);
  }, [ASPECT_RATIO, BASE_HEIGHT, BASE_WIDTH]);

  // Fetch portfolios on mount
  useEffect(() => {
    const fetchPortfolios = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          // If no token, check for guest portfolios in localStorage
          const guestPortfolios = JSON.parse(localStorage.getItem('guestPortfolios') || '[]');
          setPortfolios(guestPortfolios);
          if (guestPortfolios.length > 0) {
            const initialPortfolioId = guestPortfolios[0].id;
            setSelectedPortfolioId(initialPortfolioId);
            localStorage.setItem('currentPortfolioId', initialPortfolioId);
          } else {
            setError('Click NEW PORTFOLIO to create a portfolio!');
          }
          return;
        }

        const response = await axios.get('http://localhost:8080/portfolios/user', {
          headers: { 'Authorization': `Bearer ${token}` },
        });

        setPortfolios(response.data);
        if (response.data.length > 0) {
          const initialPortfolioId = response.data[0].id;
          setSelectedPortfolioId(initialPortfolioId);
          localStorage.setItem('currentPortfolioId', initialPortfolioId);
        } else {
          setError('Click NEW PORTFOLIO to create a portfolio!');
        }
      } catch (err) {
        setError('Failed to fetch portfolios: ' + err.message);
      }
    };

    fetchPortfolios();
  }, []);

  // Fetch holdings when portfolio or timeframe changes
  useEffect(() => {
    if (!selectedPortfolioId) {
      setError('Select portfolio to see Heatmap!');
      setHoldings([]);
      return;
    }

    const fetchHoldings = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) throw new Error('No token found');

        const response = await axios.get(
          `http://localhost:8080/portfolios/${selectedPortfolioId}?timeframe=${timeframe}`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );

        setPortfolioData(response.data);

        let holdingsData = response.data.openPositions || [];

        const fullHoldingsData = await Promise.all(
          holdingsData.map(async (holding) => {
            if (typeof holding === 'number' || !holding.stock || !holding.shares) {
              try {
                const holdingId = typeof holding === 'number' ? holding : holding.id;
                const holdingResponse = await axios.get(
                  `http://localhost:8080/portfolios/holdings/${holdingId}`,
                  { headers: { 'Authorization': `Bearer ${token}` } }
                );
                return {
                  ...holdingResponse.data,
                  percentChange: response.data.timeframePercentageChanges[holdingId] || 0,
                };
              } catch (err) {
                console.warn('Failed to fetch holding details:', err);
                return null;
              }
            }
            return {
              ...holding,
              percentChange: response.data.timeframePercentageChanges[holding.id] || 0,
            };
          })
        );

        const validHoldings = fullHoldingsData.filter((h) => h !== null);
        if (validHoldings.length === 0) {
          setError('Click ADD STOCK to add stocks to your portfolio!');
          setHoldings([]);
          return;
        }

        const tickers = validHoldings.map((h) => h.stock.ticker);
        const batchResponse = await axios.get('http://localhost:8080/stocks/batch-prices', {
          params: { symbols: tickers },
          headers: { 'Authorization': `Bearer ${token}` },
          paramsSerializer: (params) => `symbols=${params.symbols.join(',')}`,
        });
        const currentPrices = batchResponse.data.reduce((acc, stockPrice) => {
          acc[stockPrice.symbol] = stockPrice.price;
          return acc;
        }, {});

        const totalValue = validHoldings.reduce((sum, h) => {
          const price = currentPrices[h.stock.ticker] || h.purchasePrice;
          return sum + h.shares * price;
        }, 0);

        const holdingsWithAllocation = validHoldings.map((holding) => {
          const currentPrice = currentPrices[holding.stock.ticker] || holding.purchasePrice;
          const currentValue = holding.shares * currentPrice;
          const allocation = currentValue / totalValue;
          const percentChange = holding.percentChange;

          let dollarChange;
          if (timeframe === 'total') {
            dollarChange = (currentValue * percentChange) / 100;
          } else {
            dollarChange = (currentPrice * percentChange) / 100;
          }

          return {
            ...holding,
            currentPrice,
            currentValue,
            allocation,
            percentChange,
            dollarChange,
          };
        });

        holdingsWithAllocation.sort((a, b) => b.allocation - a.allocation);
        setHoldings(holdingsWithAllocation);
      } catch (err) {
        console.error('Error fetching holdings:', err);
        setError('Failed to fetch holdings: ' + err.message);
      }
    };

    fetchHoldings();
  }, [selectedPortfolioId, timeframe]);

  // Handle rename portfolio
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

      setPortfolios(
        portfolios.map((p) =>
          p.id === portfolioId ? { ...p, name: response.data.name } : p
        )
      );
      setRenamePortfolioId(null);
      setRenameValue('');
    } catch (err) {
      console.error('Failed to rename portfolio:', err.response?.data || err.message);
      alert('Failed to rename portfolio. Please try again.');
    }
  };

  // Handle delete portfolio
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

      const updatedPortfolios = portfolios.filter((p) => p.id !== portfolioId);
      setPortfolios(updatedPortfolios);

      if (selectedPortfolioId === portfolioId) {
        setSelectedPortfolioId(updatedPortfolios[0]?.id || null);
      }
    } catch (err) {
      console.error('Failed to delete portfolio:', err);
      alert('Failed to delete portfolio. Please try again.');
    }
  };

  // Get color based on percentage change (Finviz style with dynamic scaling)
  const getColor = (percentChange, timeframe) => {
    const pc = Number(percentChange) || 0;
    const baseGray = { r: 43, g: 49, b: 58 };
    const green = { r: 0, g: 153, b: 51 };
    const red = { r: 204, g: 51, b: 51 };

    const maxRange = timeframeRanges[timeframe];
    const cappedPC = Math.min(Math.abs(pc), maxRange);
    const factor = cappedPC / maxRange;

    let targetColor;
    if (pc > 0) {
      targetColor = green;
    } else if (pc < 0) {
      targetColor = red;
    } else {
      return `rgb(${baseGray.r}, ${baseGray.g}, ${baseGray.b})`;
    }

    const r = Math.round(baseGray.r + (targetColor.r - baseGray.r) * factor);
    const g = Math.round(baseGray.g + (targetColor.g - baseGray.g) * factor);
    const b = Math.round(baseGray.b + (targetColor.b - baseGray.b) * factor);

    const opacity = 0.7 + factor * (0.95 - 0.7);
    return `rgba(${r}, ${g}, ${b}, ${opacity})`;
  };

  // Create treemap layout
  const createTreemap = (holdings) => {
    if (!holdings.length) return [];

    const hierarchyData = {
      name: 'portfolio',
      children: holdings.map((h) => ({
        name: h.stock.ticker,
        value: h.allocation,
        holding: h,
      })),
    };

    const root = d3
      .hierarchy(hierarchyData)
      .sum((d) => d.value)
      .sort((a, b) => b.value - a.value);

    const layout = d3
      .treemap()
      .size([BASE_WIDTH, BASE_HEIGHT])
      .padding(2)
      .round(true);

    layout(root);
    return root.leaves();
  };

  const treeMapData = createTreemap(holdings);

  // Calculate performance rank based on percentChange
  const getPerformanceRank = (holding) => {
    const sortedHoldings = [...holdings].sort(
      (a, b) => b.percentChange - a.percentChange
    );
    const rank = sortedHoldings.findIndex(
      (h) => h.stock.ticker === holding.stock.ticker
    ) + 1;
    return rank;
  };

  // Tooltip event handlers
  const handleMouseEnter = async (e, holding) => {
    let chartData = null;
    let chartError = '';

    if (chartCache[holding.stock.ticker]) {
      chartData = chartCache[holding.stock.ticker];
    } else {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(
          `https://financialmodelingprep.com/api/v3/historical-chart/5min/${holding.stock.ticker}?apikey=${process.env.REACT_APP_FMP_API_KEY}`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );

        if (!response.data || response.data.length === 0) {
          chartError = 'No intraday data available';
        } else {
          const labels = response.data.map((point) => point.date.slice(11, 16));
          const prices = response.data.map((point) => point.close);

          chartData = {
            labels: labels.reverse(),
            datasets: [
              {
                label: 'Close Price',
                data: prices.reverse(),
                borderColor: 'rgba(75, 192, 192, 1)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                fill: false,
                tension: 0.1,
                pointRadius: 0,
              },
            ],
          };

          setChartCache((prev) => ({
            ...prev,
            [holding.stock.ticker]: chartData,
          }));
        }
      } catch (err) {
        console.error('Error fetching chart data:', err);
        chartError = 'Failed to load chart';
      }
    }

    setTooltip({
      visible: true,
      x: e.clientX,
      y: e.clientY,
      data: holding,
      chartData,
      chartError,
    });
  };

  const handleMouseLeave = () => {
    setTooltip((prev) => ({
      ...prev,
      visible: false,
      data: null,
      chartData: null,
      chartError: '',
    }));
  };

  const handleMouseMove = (e) => {
    setTooltip((prev) => ({
      ...prev,
      x: e.clientX,
      y: e.clientY,
    }));
  };

  const handlePortfolioSelect = (portfolioId) => {
    const numericId = parseInt(portfolioId, 10);
    setSelectedPortfolioId(numericId || null);
    localStorage.setItem('currentPortfolioId', numericId || '');

    const selectedPortfolio = portfolios.find((p) => p.id === numericId);
    setRenamePortfolioId(null);
    setRenameValue(selectedPortfolio ? selectedPortfolio.name : '');
  };

  // Handle double click to navigate to detailed chart
  const handleStockDoubleClick = (ticker) => {
    navigate(`/chart/${ticker}`);
  };

  // Generate percentage markers for the color scale
  const getColorScaleMarkers = (timeframe) => {
    const maxRange = timeframeRanges[timeframe] || 10;
    const steps = [
      -maxRange,
      -(maxRange * 2) / 3,
      -maxRange / 3,
      0,
      maxRange / 3,
      (maxRange * 2) / 3,
      maxRange,
    ];
    return steps.map((value) => ({
      value,
      label: value >= 0 ? `+${value}%` : `${value}%`,
    }));
  };

  // Get the current portfolio name
  const selectedPortfolio = portfolios.find((p) => p.id === selectedPortfolioId);
  const portfolioName = selectedPortfolio ? selectedPortfolio.name : 'No Portfolio Selected';

  return (
    <div className="heatmap-container">
      <Sidebar
        portfolios={portfolios}
        selectedPortfolioId={selectedPortfolioId}
        onPortfolioSelect={handlePortfolioSelect}
        holdings={holdings}
        setPortfolios={setPortfolios}
      />
      <div className="heatmap-main">
        <div className="portfolio-header">
          {renamePortfolioId === selectedPortfolioId ? (
            <div className="rename-container">
              <input
                type="text"
                value={renameValue}
                onChange={(e) => setRenameValue(e.target.value)}
                className="rename-input"
                placeholder="Portfolio Name"
              />
              <button
                onClick={() => handleRenamePortfolio(selectedPortfolioId, renameValue)}
                className="action-button save"
              >
                Save
              </button>
              <button
                onClick={() => {
                  setRenamePortfolioId(null);
                  setRenameValue(portfolios.find((p) => p.id === selectedPortfolioId)?.name || '');
                }}
                className="action-button cancel"
              >
                Cancel
              </button>
            </div>
          ) : (
            <div className="portfolio-title">
              <h2>
                {selectedPortfolioId ? (portfolios.find((p) => p.id === selectedPortfolioId)?.name || 'Loading...') : 'No Portfolio Selected'}
              </h2>

              {selectedPortfolioId && (
                <div className="portfolio-actions">
                  <button
                    onClick={() => {
                      const selectedPortfolio = portfolios.find((p) => p.id === selectedPortfolioId);
                      setRenamePortfolioId(selectedPortfolioId);
                      setRenameValue(selectedPortfolio ? selectedPortfolio.name : '');
                    }}
                    className="action-button rename"
                  >
                    <FaPencilAlt size={16} />
                  </button>
                  <button
                    onClick={() => handleDeletePortfolio(selectedPortfolioId)}
                    className="action-button delete"
                  >
                    <FaTrash size={16} />
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
        <div className="heatmap-controls">
          <div className="timeframe-selector">
            <label>Timeframe: </label>
            <select value={timeframe} onChange={(e) => setTimeframe(e.target.value)}>
              {timeframeOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
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
          {portfolios.length > 0 && holdings.length > 0 && (
            <div className="heatmap-message">
              *Double click stock to see detailed chart
            </div>
          )}
          <div className="heatmap-and-scale" style={{ display: 'flex', alignItems: 'center', width: '100%', maxWidth: '1200px' }}>
            <div className="heatmap-visualization">
              <div
                className="heatmap-content"
                style={{
                  position: 'absolute',
                  width: `${BASE_WIDTH}px`,
                  height: `${BASE_HEIGHT}px`,
                  transform: `scale(${scale})`,
                  transformOrigin: 'top left',
                }}
              >
                {holdings.length === 0 && error && (
                  <div className="error-message">{error}</div>
                )}
                {treeMapData.map((d, i) => {
                  const holding = d.data.holding;
                  const width = Math.max(d.x1 - d.x0, MIN_RECTANGLE_SIZE);
                  const height = Math.max(d.y1 - d.y0, MIN_RECTANGLE_SIZE);
                  const percentChange = holding.percentChange;

                  let dollarChange;
                  if (timeframe === 'total') {
                    dollarChange = (holding.currentValue * percentChange) / 100;
                  } else {
                    dollarChange = (holding.currentPrice * percentChange) / 100;
                  }

                  const fontSize = Math.min(width, height) * 0.12;

                  return (
                    <div
                      key={i}
                      className="heatmap-rect"
                      style={{
                        position: 'absolute',
                        left: `${d.x0}px`,
                        top: `${d.y0}px`,
                        width: `${width}px`,
                        height: `${height}px`,
                        backgroundColor: getColor(percentChange, timeframe),
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
                        textShadow: '1px 1px 1px rgba(0, 0, 0, 0.9)',
                        cursor: 'default',
                      }}
                      onMouseEnter={(e) => handleMouseEnter(e, holding)}
                      onMouseMove={handleMouseMove}
                      onMouseLeave={handleMouseLeave}
                      onDoubleClick={() => handleStockDoubleClick(holding.stock.ticker)}
                    >
                      <div className="ticker" style={{ fontSize: `${fontSize}px` }}>
                        {holding.stock.ticker}
                      </div>
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
            {portfolioData && holdings.length > 0 && (
              <div
                className="color-scale"
                style={{
                  fontFamily: 'Arial, sans-serif',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'center',
                  gap: '1px',
                  marginLeft: '20px',
                  height: `${BASE_HEIGHT * 0.75 * scale}px`,
                  width: `${BASE_WIDTH * 0.06 * scale}px`,
                  minWidth: '40px',
                }}
              >
                {getColorScaleMarkers(timeframe).map((marker, index) => (
                  <div
                    key={index}
                    style={{
                      backgroundColor: getColor(marker.value, timeframe),
                      color: '#fff',
                      flex: 1,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '12px',
                      fontWeight: 'bold',
                      minHeight: '0',
                    }}
                  >
                    {marker.label}
                  </div>
                ))}
              </div>
            )}
          </div>
          {portfolioData && holdings.length > 0 && (
            <div className="portfolio-summary" style={{ textAlign: 'left', fontFamily: 'Arial, sans-serif', width: '100%' }}>
              <h3>Portfolio Summary</h3>
              <ul style={{ listStyle: 'none', padding: 0 }}>
                <li><strong>Total % Return:</strong> {portfolioData.totalPercentageReturn.toFixed(2)}%</li>
                <li><strong>Total $ Return:</strong> ${portfolioData.totalDollarReturn.toFixed(2)}</li>
                <li><strong>Current Value:</strong> ${portfolioData.totalPortfolioValue.toFixed(2)}</li>
              </ul>
            </div>
          )}
        </div>
      </div>
      {tooltip.visible && tooltip.data &&
        ReactDOM.createPortal(
          <div
            className="heatmap-tooltip"
            style={{
              position: 'fixed',
              left: `${tooltip.x - 1}px`,
              top: `${tooltip.y - 249}px`,
              backgroundColor: 'black',
              color: '#ffffff',
              borderRadius: '6px',
              fontSize: '14px',
              pointerEvents: 'none',
              width: '300px',
              whiteSpace: 'nowrap',
              transition: 'none',
              cursor: 'pointer'
            }}
          >
            <div style={{marginBottom: '8px' }}><strong>{tooltip.data.stock.ticker}</strong></div>
            <div>Company: {tooltip.data.stock.companyName || 'N/A'}</div>
            <div>Allocation: {(tooltip.data.allocation * 100).toFixed(2)}%</div>
            <div>Current Value: ${tooltip.data.currentValue.toFixed(2)}</div>
            <div>Performance Rank: {getPerformanceRank(tooltip.data)} of {holdings.length}</div>
            <div>Current Price: ${tooltip.data.currentPrice}</div>
            {tooltip.chartError ? (
              <div style={{ fontSize: '12px', color: '#ff6666', marginTop: '8px' }}>
                {tooltip.chartError}
              </div>
            ) : tooltip.chartData ? (
              <div style={{ marginTop: '10px', width: '100%', height: '100px' }}>
                <Line
                  data={tooltip.chartData}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                      legend: { display: false },
                      tooltip: { enabled: false },
                    },
                    scales: {
                      x: {
                        display: false,
                        grid: { display: false },
                      },
                      y: {
                        display: false,
                        grid: { display: false },
                      },
                    },
                    elements: {
                      line: { borderWidth: 1 },
                      point: { radius: 0 },
                    },
                  }}
                />
              </div>
            ) : (
              <div style={{ fontSize: '12px', color: '#ffffff', marginTop: '8px' }}>
                Loading chart...
              </div>
            )}
          </div>,
          document.body
        )
      }
    </div>
  );
}

export default Heatmap;
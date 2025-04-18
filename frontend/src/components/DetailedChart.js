import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Chart as ChartJS, 
  CategoryScale, 
  LinearScale, 
  PointElement, 
  LineElement, 
  Title, 
  Tooltip, 
  Legend,
  Filler,
  BarElement } from 'chart.js';
import { Line } from 'react-chartjs-2';
import './DetailedChart.css';
import { debounce } from 'lodash';

// Register required Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

// Custom candlestick chart plugin
const candlestickPlugin = {
  id: 'candlestick',
  beforeDatasetsDraw(chart, args, options) {
    const { ctx, data, chartArea, scales } = chart;
    const { top, bottom, left, right, width, height } = chartArea;
    const { x, y } = scales;

    // Calculate appropriate candlestick width based on the number of data points
    const dataPoints = data.datasets[0].data.length;
    // Width calculations: more data points = thinner candles
    const wickWidth = dataPoints > 180 ? 0.5 : 1;
    
    // Dynamic candleWidth based on data density
    let candleWidth;
    if (dataPoints > 500) {
      candleWidth = 2; // 5-year data
    } else if (dataPoints > 250) {
      candleWidth = 3; // 1-year data
    } else if (dataPoints > 120) {
      candleWidth = 4; // 6-month data
    } else if (dataPoints > 60) {
      candleWidth = 6; // 3-month data
    } else {
      candleWidth = 8; // 1-month data or less
    }
    
    // Scale candleWidth if chart is too narrow
    const availableWidthPerCandle = width / dataPoints;
    if (availableWidthPerCandle < candleWidth) {
      candleWidth = Math.max(1, availableWidthPerCandle - 1);
    }

    // For each data point in our dataset
    data.datasets[0].data.forEach((dataPoint, index) => {
      const xValue = x.getPixelForValue(index);
      const yOpen = y.getPixelForValue(dataPoint.open);
      const yClose = y.getPixelForValue(dataPoint.close);
      const yHigh = y.getPixelForValue(dataPoint.high);
      const yLow = y.getPixelForValue(dataPoint.low);

      const isGreen = dataPoint.close >= dataPoint.open;
      
      // Draw the wick (high to low vertical line)
      ctx.beginPath();
      ctx.strokeStyle = isGreen ? '#00FF44' : '#FF3333';
      ctx.lineWidth = wickWidth;
      ctx.moveTo(xValue, yHigh);
      ctx.lineTo(xValue, yLow);
      ctx.stroke();
      
      // Draw the candle body (open to close rectangle)
      ctx.fillStyle = isGreen ? '#00FF44' : '#FF3333';
      ctx.fillRect(
        xValue - candleWidth / 2,
        yOpen,
        candleWidth,
        yClose - yOpen
      );
    });
  }
};

function DetailedChart() {
  const { ticker } = useParams();
  const navigate = useNavigate();
  const [chartData, setChartData] = useState(null);
  const [stockInfo, setStockInfo] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [priceRange, setPriceRange] = useState({ min: 0, max: 0 });
  const [timeframe, setTimeframe] = useState('1y');
  const [windowWidth, setWindowWidth] = useState(window.innerWidth);
  const [searchTicker, setSearchTicker] = useState('');
  const [stockSuggestions, setStockSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  // Track window resize
  useEffect(() => {
    const handleResize = () => {
      setWindowWidth(window.innerWidth);
    };
    
    window.addEventListener('resize', handleResize);
    
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

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
    if (searchTicker.length >= 1) {
      fetchStockSuggestions(searchTicker);
    } else {
      setStockSuggestions([]);
      setShowDropdown(false);
    }
  }, [searchTicker]);

  const handleTickerSelect = (selectedTicker) => {
    setSearchTicker('');
    setShowDropdown(false);
    navigate(`/chart/${selectedTicker}`);
  };

  // Fetch stock data
  useEffect(() => {
    // Reset chart and data when ticker changes
    setChartData(null);
    
    const fetchStockData = async () => {
      setIsLoading(true);
      
      try {
        const token = localStorage.getItem('token');
        if (!token) throw new Error('No token found');

        // Fetch stock information
        const infoResponse = await axios.get(
          `http://localhost:8080/stocks/info/${ticker}`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        setStockInfo(infoResponse.data);

        // Fetch candlestick data instead of historical data
        const candlestickResponse = await axios.get(
          `http://localhost:8080/stocks/candlestick/${ticker}?timeframe=${timeframe}`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );

        if (!candlestickResponse.data || candlestickResponse.data.length === 0) {
          setError('No candlestick data available');
          setIsLoading(false);
          return;
        }

        // Format data for Chart.js with candlestick plugin
        const data = candlestickResponse.data;
        
        // Sort data chronologically (oldest to newest)
        data.sort((a, b) => new Date(a.date) - new Date(b.date));
        
        console.log(`Received ${data.length} candlestick data points for timeframe: ${timeframe}`);
        
        // Find price range for y-axis
        const highPrices = data.map(point => point.high);
        const lowPrices = data.map(point => point.low);
        const minPrice = Math.min(...lowPrices);
        const maxPrice = Math.max(...highPrices);
        setPriceRange({ min: minPrice, max: maxPrice });
        
        // Format dates for display
        const formattedData = data.map(point => {
          const date = new Date(point.date);
          return {
            x: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
            open: point.open,
            high: point.high,
            low: point.low,
            close: point.close,
            date: date
          };
        });

        // Create chart data - the visible element is just a placeholder
        // The actual candlesticks are drawn by our plugin
        setChartData({
          labels: formattedData.map(point => point.x),
          datasets: [
            {
              data: formattedData,
              borderColor: 'rgba(0, 0, 0, 0)',
              backgroundColor: 'rgba(0, 0, 0, 0)',
              pointRadius: 0,
              tension: 0
            }
          ],
        });
        setIsLoading(false);
      } catch (err) {
        console.error('Error fetching stock data:', err);
        setError(`Failed to load data: ${err.message}`);
        setIsLoading(false);
      }
    };

    fetchStockData();
  }, [ticker, timeframe]);

  const handleGoBack = () => {
    navigate('/heatmap');
  };

  const handleTimeframeChange = (event) => {
    setTimeframe(event.target.value);
  };

  const getYAxisRange = () => {
    if (priceRange.min === priceRange.max) return { min: 0, max: 100 };
    
    // Add padding to the top and bottom
    const range = priceRange.max - priceRange.min;
    const padding = range * 0.1;
    
    let min = priceRange.min - padding;
    let max = priceRange.max + padding;
    
    // Clean up the range to nice round numbers
    if (max < 1) {
      // For penny stocks - round to nearest 0.05 or 0.01
      const precision = max < 0.1 ? 0.01 : 0.05;
      min = Math.floor(min / precision) * precision;
      max = Math.ceil(max / precision) * precision;
    } else if (max < 10) {
      // For stocks under $10 - round to nearest 0.5
      min = Math.floor(min * 2) / 2;
      max = Math.ceil(max * 2) / 2;
    } else if (max < 100) {
      // For stocks under $100 - round to nearest 5
      min = Math.floor(min / 5) * 5;
      max = Math.ceil(max / 5) * 5;
    } else if (max < 1000) {
      // For stocks under $1000 - round to nearest 10
      min = Math.floor(min / 10) * 10;
      max = Math.ceil(max / 10) * 10;
    } else {
      // For very high-priced stocks - round to nearest 50
      min = Math.floor(min / 50) * 50;
      max = Math.ceil(max / 50) * 50;
    }
    
    return { min, max };
  };

  // Calculate appropriate tick values
  const getTickValues = () => {
    const range = getYAxisRange();
    const min = range.min;
    const max = range.max;
    const diff = max - min;
    
    // Determine how many ticks based on screen size
    const tickCount = windowWidth < 768 ? 5 : 7;
    
    // Calculate clean step size
    let stepSize;
    if (max < 1) {
      // Penny stocks
      stepSize = max < 0.1 ? 0.01 : 0.05;
    } else if (max < 10) {
      // Low price
      stepSize = 0.5;
    } else if (max < 100) {
      // Medium price
      stepSize = 5;
    } else if (max < 1000) {
      // High price
      stepSize = 10;
    } else {
      // Very high price
      stepSize = 50;
    }
    
    // Generate equally spaced values
    const values = [];
    for (let i = 0; i <= tickCount; i++) {
      const value = min + (diff / tickCount) * i;
      // Round to the nearest step
      const roundedValue = Math.round(value / stepSize) * stepSize;
      values.push(roundedValue);
    }
    
    // Remove duplicates
    return [...new Set(values)];
  };

  // Adjust max ticks based on screen width
  const getMaxTicks = () => {
    if (windowWidth < 600) return 6;
    if (windowWidth < 960) return 8;
    if (windowWidth < 1280) return 10;
    return 12;
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      candlestick: {}, // Enable our candlestick plugin
      legend: {
        display: false, // Hide legend
      },
      tooltip: {
        mode: 'index',
        intersect: false,
        callbacks: {
          title: function(tooltipItems) {
            const item = tooltipItems[0];
            const dataPoint = chartData.datasets[0].data[item.dataIndex];
            return dataPoint.x;
          },
          label: function(context) {
            const dataPoint = chartData.datasets[0].data[context.dataIndex];
            const isGreen = dataPoint.close >= dataPoint.open;
            const colorUp = '#00FF44';
            const colorDown = '#FF3333';
            
            return [
              `Open: $${dataPoint.open.toFixed(2)}`,
              `High: $${dataPoint.high.toFixed(2)}`,
              `Low: $${dataPoint.low.toFixed(2)}`,
              `Close: ${isGreen ? '📈' : '📉'} $${dataPoint.close.toFixed(2)}`
            ];
          },
          labelTextColor: function(context) {
            const dataPoint = chartData.datasets[0].data[context.dataIndex];
            return dataPoint.close >= dataPoint.open ? '#00FF44' : '#FF3333';
          }
        },
        backgroundColor: 'rgba(25, 30, 36, 0.9)',
        titleFont: {
          size: 14,
          family: 'Arial, sans-serif'
        },
        bodyFont: {
          size: 14,
          family: 'Arial, sans-serif'
        },
        titleColor: 'rgb(190, 210, 230)',
        bodyColor: 'rgb(190, 210, 230)',
        borderColor: 'rgb(81, 89, 97)',
        padding: 10
      }
    },
    scales: {
      x: {
        grid: {
          display: true, // Show vertical gridlines for month ticks
          drawBorder: false,
          color: function() {
            return ' rgb(190, 210, 230, 0.10)';
          },
          tickLength: 8, // Shorter tick marks
        },
        ticks: {
          font: {
            size: windowWidth < 768 ? 10 : 12,
            family: 'Arial, sans-serif'
          },
          color: function() {
            return 'rgb(190, 210, 230)';
          },
          padding: 10, // Add padding below labels
          maxRotation: 0,
          autoSkip: true,
          maxTicksLimit: getMaxTicks() // Adaptive number of ticks based on screen width
        },
        border: {
          display: false
        }
      },
      y: {
        display: true,
        position: 'left', // Price scale on the left
        min: getYAxisRange().min,
        max: getYAxisRange().max,
        ticks: {
          callback: function(value) {
            // Format long decimal numbers intelligently
            if (Math.abs(value) < 0.01) {
              return '$' + value.toFixed(4);
            } else if (Math.abs(value) < 1) {
              return '$' + value.toFixed(2);
            } else if (Math.abs(value) >= 1000) {
              return '$' + value.toLocaleString('en-US', {
                minimumFractionDigits: 0,
                maximumFractionDigits: 0
              });
            } else {
              return '$' + value.toLocaleString('en-US', {
                minimumFractionDigits: 0,
                maximumFractionDigits: 2
              });
            }
          },
          font: {
            size: windowWidth < 768 ? 10 : 12,
            family: 'Arial, sans-serif'
          },
          color: function() {
            return 'rgb(190, 210, 230)';
          },
          stepSize: undefined,
          count: undefined,
          autoSkip: false,
          values: getTickValues()
        },
        grid: {
          color: function() {
            return 'rgba(190, 210, 230, 0.10)';
          },
          drawBorder: false,
        },
        border: {
          display: false
        }
      }
    },
    elements: {
      line: {
        tension: 0 // No curve for candlesticks
      }
    },
    layout: {
      padding: {
        left: windowWidth < 768 ? 20 : 40, // Increased padding for better centering
        right: windowWidth < 768 ? 20 : 40,
        top: windowWidth < 768 ? 15 : 25,
        bottom: windowWidth < 768 ? 15 : 25
      }
    }
  };

  const timeframeOptions = [
    { value: '1m', label: '1 Month' },
    { value: '3m', label: '3 Months' },
    { value: '6m', label: '6 Months' },
    { value: '1y', label: '1 Year' },
    { value: '5y', label: '5 Years' }
  ];

  return (
    <div className="detailed-chart-container dark-theme">
      <div className="chart-header">
        <div className="top-row">
          <button className="back-button" onClick={handleGoBack}>
            ← Back
          </button>
          
          <div className="search-container">
            <div className="ticker-input-container">
              <input
                type="text"
                value={searchTicker}
                onChange={(e) => setSearchTicker(e.target.value.toUpperCase())}
                placeholder="Search ticker..."
                className="search-input"
                autoComplete="off"
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
        </div>
        
        <div className="header-content">
          {stockInfo && (
            <>
              <div className="stock-name-ticker">
                <h2 className="ticker-symbol">{ticker}</h2>
                <span className="company-name-text">{stockInfo.companyName}</span>
              </div>

              <div className="timeframe-selector">
                <select value={timeframe} onChange={handleTimeframeChange}>
                  {timeframeOptions.map(option => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                  ))}
                </select>
              </div>
              <div className="price-info">
                <p className="current-price">${stockInfo.price?.toFixed(2) || 'N/A'}</p>
                <p className={`price-change ${stockInfo.change >= 0 ? 'positive' : 'negative'}`}>
                  {stockInfo.change > 0 ? '+' : ''}
                  {stockInfo.change?.toFixed(2) || 'N/A'} (
                  {stockInfo.changePercent > 0 ? '+' : ''}
                  {stockInfo.changePercent?.toFixed(2) || 'N/A'}%)
                </p>
              </div>
            </>
          )}
        </div>
      </div>

      <div className="chart-container">
        {isLoading ? (
          <div className="loading-spinner">Loading chart data...</div>
        ) : error ? (
          <div className="error-message">{error}</div>
        ) : chartData ? (
          <Line 
            data={chartData} 
            options={chartOptions} 
            plugins={[candlestickPlugin]}
            id="stockChart" 
            className="stock-chart"
          />
        ) : (
          <div>No data available</div>
        )}
      </div>
      
      <div className="ad-space">
        Advertisement Space
      </div>
    </div>
  );
}

export default DetailedChart;

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
      ctx.strokeStyle = isGreen ? '#26a69a' : '#ef5350';
      ctx.lineWidth = wickWidth;
      ctx.moveTo(xValue, yHigh);
      ctx.lineTo(xValue, yLow);
      ctx.stroke();
      
      // Draw the candle body (open to close rectangle)
      ctx.fillStyle = isGreen ? '#26a69a' : '#ef5350';
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
    
    // Add 10% padding to the top and bottom
    const range = priceRange.max - priceRange.min;
    const padding = range * 0.1;
    
    const min = Math.floor(priceRange.min - padding);
    const max = Math.ceil(priceRange.max + padding);
    
    // Generate nice round numbers
    return {
      min: Math.floor(min / 10) * 10,
      max: Math.ceil(max / 10) * 10
    };
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
            return [
              `Open: $${dataPoint.open.toFixed(2)}`,
              `High: $${dataPoint.high.toFixed(2)}`,
              `Low: $${dataPoint.low.toFixed(2)}`,
              `Close: $${dataPoint.close.toFixed(2)}`
            ];
          }
        },
        backgroundColor: 'rgba(69, 79, 86, 1)',
        titleFont: {
          size: 14,
          family: 'Arial, sans-serif'
        },
        bodyFont: {
          size: 14,
          family: 'Arial, sans-serif'
        },
        titleColor: 'rgb(98, 111, 126)',
        bodyColor: 'rgb(98, 111, 126)',
        borderColor: 'rgb(98, 111, 126)'
      }
    },
    scales: {
      x: {
        grid: {
          display: true, // Show vertical gridlines for month ticks
          drawBorder: false,
          color: function() {
            return 'rgba(98, 111, 126, 0.25)';
          },
          tickLength: 8, // Shorter tick marks
        },
        ticks: {
          font: {
            size: windowWidth < 768 ? 10 : 12,
            family: 'Arial, sans-serif'
          },
          color: function() {
            return 'rgb(98, 111, 126)';
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
            return '$' + value;
          },
          font: {
            size: windowWidth < 768 ? 10 : 12,
            family: 'Arial, sans-serif'
          },
          color: function() {
            return 'rgb(98, 111, 126)';
          },
          stepSize: Math.ceil((getYAxisRange().max - getYAxisRange().min) / 8 / 10) * 10,
          count: windowWidth < 768 ? 6 : 8 // Fewer ticks on mobile
        },
        grid: {
          color: function() {
            return 'rgba(98, 111, 126, 0.15)';
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
        left: windowWidth < 768 ? 10 : 20, // Less padding on mobile
        right: windowWidth < 768 ? 5 : 10,
        top: windowWidth < 768 ? 10 : 20,
        bottom: windowWidth < 768 ? 5 : 10
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
        <button className="back-button" onClick={handleGoBack}>
          ← Back
        </button>
        
        <div className="header-content">
          {stockInfo && (
            <>
              <div className="stock-name-ticker">
                <h2 className="ticker-symbol">{ticker}</h2>
                <span className="company-name">{stockInfo.companyName}</span>
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
    </div>
  );
}

export default DetailedChart;

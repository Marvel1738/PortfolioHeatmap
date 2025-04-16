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
  Filler } from 'chart.js';
import { Line } from 'react-chartjs-2';
import './DetailedChart.css';

// Register required Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

function DetailedChart() {
  const { ticker } = useParams();
  const navigate = useNavigate();
  const [chartData, setChartData] = useState(null);
  const [stockInfo, setStockInfo] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [priceRange, setPriceRange] = useState({ min: 0, max: 0 });

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

        // Fetch historical data for 1 year
        const historyResponse = await axios.get(
          `http://localhost:8080/stocks/history/${ticker}?timeframe=1y`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );

        if (!historyResponse.data || historyResponse.data.length === 0) {
          setError('No historical data available');
          setIsLoading(false);
          return;
        }

        // Format data for Chart.js
        const data = historyResponse.data;
        
        // Sort data chronologically (oldest to newest)
        data.sort((a, b) => new Date(a.date) - new Date(b.date));
        
        // Find price range
        const prices = data.map(point => point.close);
        const minPrice = Math.min(...prices);
        const maxPrice = Math.max(...prices);
        setPriceRange({ min: minPrice, max: maxPrice });
        
        // Format dates to show month and year
        const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        
        // Group data by month
        const monthlyData = {};
        
        data.forEach(point => {
          const date = new Date(point.date);
          const monthYearKey = `${monthNames[date.getMonth()]} ${date.getFullYear()}`;
          
          if (!monthlyData[monthYearKey]) {
            monthlyData[monthYearKey] = {
              prices: [],
              date: date
            };
          }
          
          monthlyData[monthYearKey].prices.push(point.close);
        });
        
        // Sort by date from oldest to newest (left to right)
        const sortedMonths = Object.entries(monthlyData)
          .sort((a, b) => a[1].date - b[1].date) // Chronological order - oldest first
          .map(([monthYear, data]) => {
            return {
              label: monthYear,
              price: data.prices.reduce((sum, price) => sum + price, 0) / data.prices.length
            };
          });

        const labels = sortedMonths.map(month => month.label);
        const avgPrices = sortedMonths.map(month => month.price);

        // Create chart data
        setChartData({
          labels: labels,
          datasets: [
            {
              label: 'Price',
              data: avgPrices,
              borderColor: '#26a69a', // Teal color like in example
              backgroundColor: 'rgba(38, 166, 154, 0.1)', // Light teal for fill
              fill: true,
              tension: 0.4,
              pointRadius: 3, // Add small points
              pointBackgroundColor: '#26a69a',
              borderWidth: 2,
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
  }, [ticker]);

  const handleGoBack = () => {
    navigate('/heatmap');
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

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false, // Hide legend
      },
      tooltip: {
        mode: 'index',
        intersect: false,
        callbacks: {
          label: function(context) {
            return `$${context.parsed.y.toFixed(2)}`;
          }
        },
        backgroundColor: 'rgba(0, 0, 0, 0.7)',
        titleFont: {
          size: 14
        },
        bodyFont: {
          size: 14
        }
      }
    },
    scales: {
      x: {
        grid: {
          display: true, // Show vertical gridlines for month ticks
          drawBorder: false,
          color: 'rgba(255, 255, 255, 0.1)', // Light vertical grid
          tickLength: 8, // Shorter tick marks
        },
        ticks: {
          font: {
            size: 12
          },
          color: '#666',
          padding: 10, // Add padding below labels
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
            size: 12
          },
          color: '#666',
          stepSize: Math.ceil((getYAxisRange().max - getYAxisRange().min) / 8 / 10) * 10,
          count: 8 // About 8 tick marks for clean display
        },
        grid: {
          color: 'rgba(255, 255, 255, 0.05)', // Very light horizontal gridlines
          drawBorder: false,
        },
        border: {
          display: false
        }
      }
    },
    // Important - disable all other scales and extra decimals
    parsing: {
      yAxisKey: 'price'
    },
    elements: {
      line: {
        tension: 0.4 // Smoother curve
      }
    },
    layout: {
      padding: {
        left: 20, // More padding on left for price scale
        right: 10,
        top: 20,
        bottom: 10
      }
    }
  };

  return (
    <div className="detailed-chart-container dark-theme">
      <div className="chart-header">
        <button className="back-button" onClick={handleGoBack}>
          ← Back to Heatmap
        </button>
        <h1>{ticker} - 1 Year Performance</h1>
        {stockInfo && (
          <div className="stock-info">
            <h2>{stockInfo.companyName}</h2>
            <div className="price-info">
              <p className="current-price">${stockInfo.price?.toFixed(2) || 'N/A'}</p>
              <p className={`price-change ${stockInfo.change >= 0 ? 'positive' : 'negative'}`}>
                {stockInfo.change > 0 ? '+' : ''}
                {stockInfo.change?.toFixed(2) || 'N/A'} (
                {stockInfo.changePercent > 0 ? '+' : ''}
                {stockInfo.changePercent?.toFixed(2) || 'N/A'}%)
              </p>
            </div>
          </div>
        )}
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

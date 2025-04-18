import React from 'react';
import './Page.css';

const Help = () => {
  return (
    <div className="page-container">
      <div className="page-content">
        <h1>Help Center</h1>
        
        <section>
          <h2>Getting Started</h2>
          <p>Welcome to Portfolio Heatmap! Here's how to get started:</p>
          <ol>
            <li>Create an account and log in or continue as guest</li>
            <li>Create a new portfolio or edit existing one</li>
            <li>Add your stock holdings using the sidebar</li>
            <li>View your portfolio performance on the heatmap</li>
            <li>Click on any stock to see detailed charts</li>
          </ol>
        </section>

        <section>
          <h2>Frequently Asked Questions</h2>
          
          <div className="faq-item">
            <h3>How do I add stocks to my portfolio?</h3>
            <p>Use the "Add New Holding" button in the sidebar. Enter the stock ticker, number of shares, and purchase price.</p>
          </div>

          <div className="faq-item">
            <h3>How often is the data updated?</h3>
            <p>Stock prices are updated in real-time during market hours. Historical data is updated daily.</p>
          </div>

          <div className="faq-item">
            <h3>Can I edit or delete my holdings?</h3>
            <p>Yes, you can edit or delete any holding by clicking the three dots menu next to the holding in the sidebar.</p>
          </div>

          <div className="faq-item">
            <h3>What do the colors in the heatmap mean?</h3>
            <p>Red indicates negative performance, green indicates positive performance. The intensity of the color shows the magnitude of the change.</p>
          </div>
        </section>

        <section>
          <h2>Contact Support</h2>
          <p>If you need additional help, please contact our support team:</p>
          <ul>
            <li>Email: portfolioheatmap@gmail.com</li>
            <li>Response time: Within 24 hours</li>
          </ul>
        </section>

        <section>
          <h2>Technical Requirements</h2>
          <p>Portfolio Heatmap works best with:</p>
          <ul>
            <li>Latest version of Chrome, Firefox, Safari, or Edge</li>
            <li>JavaScript enabled</li>
            <li>Minimum screen resolution of 1024x768</li>
          </ul>
        </section>
      </div>
    </div>
  );
};

export default Help; 
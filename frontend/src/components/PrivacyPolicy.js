import React from 'react';
import './Page.css';

const PrivacyPolicy = () => {
  return (
    <div className="page-container">
      <div className="page-content">
        <h1>Privacy Policy</h1>
        <p>Last updated: {new Date().toLocaleDateString()}</p>
        
        <section>
          <h2>1. Information We Collect</h2>
          <p>We collect information that you provide directly to us, including:</p>
          <ul>
            <li>Account information (email, username)</li>
            <li>Portfolio data (stock holdings, transactions)</li>
            <li>Usage data (how you interact with our service)</li>
          </ul>
        </section>

        <section>
          <h2>2. How We Use Your Information</h2>
          <p>We use the information we collect to:</p>
          <ul>
            <li>Provide and maintain our services</li>
            <li>Improve user experience</li>
          </ul>
        </section>

        <section>
          <h2>3. Data Security</h2>
          <p>We implement appropriate security measures to protect your personal information. However, no method of transmission over the internet is 100% secure.</p>
        </section>

        <section>
          <h2>4. Third-Party Services</h2>
          <p>We use third-party services for:</p>
          <ul>
            <li>Stock market data (Financial Modeling Prep API, Alpha Vantage API)</li>
            <li>Analytics and monitoring</li>
            <li>Hosting and infrastructure</li>
          </ul>
        </section>

        <section>
          <h2>5. Your Rights</h2>
          <p>You have the right to:</p>
          <ul>
            <li>Access your personal data</li>
            <li>Correct inaccurate data</li>
            <li>Request deletion of your data</li>
            <li>Opt-out of marketing communications</li>
          </ul>
        </section>

        <section>
          <h2>6. Contact Us</h2>
          <p>If you have any questions about this Privacy Policy, please contact us at:</p>
          <p>Email: portfolioheatmap@gmail.com</p>
        </section>
      </div>
    </div>
  );
};

export default PrivacyPolicy; 
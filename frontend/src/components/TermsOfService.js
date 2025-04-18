import React from 'react';
import './Page.css';

const TermsOfService = () => {
  return (
    <div className="page-container">
      <div className="page-content">
        <h1>Terms of Service</h1>
        <p>Last updated: {new Date().toLocaleDateString()}</p>
        
        <section>
          <h2>1. Acceptance of Terms</h2>
          <p>By accessing and using Portfolio Heatmap, you agree to be bound by these Terms of Service and all applicable laws and regulations.</p>
        </section>

        <section>
          <h2>2. Use License</h2>
          <p>Permission is granted to temporarily use Portfolio Heatmap for personal, non-commercial purposes only. This is the grant of a license, not a transfer of title.</p>
        </section>

        <section>
          <h2>3. User Responsibilities</h2>
          <p>You agree to:</p>
          <ul>
            <li>Provide accurate and complete information</li>
            <li>Maintain the security of your account</li>
            <li>Not use the service for any illegal purpose</li>
            <li>Not interfere with the service's operation</li>
          </ul>
        </section>

        <section>
          <h2>4. Data and Privacy</h2>
          <p>Your use of Portfolio Heatmap is also governed by our Privacy Policy. Please review our Privacy Policy to understand our practices.</p>
        </section>

        <section>
          <h2>5. Disclaimer</h2>
          <p>The information provided by Portfolio Heatmap is for informational purposes only and should not be considered financial advice. We make no guarantees about the accuracy or completeness of the information.</p>
        </section>

        <section>
          <h2>6. Limitation of Liability</h2>
          <p>Portfolio Heatmap shall not be liable for any indirect, incidental, special, consequential, or punitive damages resulting from your use of the service.</p>
        </section>

        <section>
          <h2>7. Changes to Terms</h2>
          <p>We reserve the right to modify these terms at any time. Your continued use of the service following any changes indicates your acceptance of the new terms.</p>
        </section>

        <section>
          <h2>8. Contact Information</h2>
          <p>Questions about the Terms of Service should be sent to us at:</p>
          <p>Email: portfolioheatmap@gmail.com</p>
        </section>
      </div>
    </div>
  );
};

export default TermsOfService; 
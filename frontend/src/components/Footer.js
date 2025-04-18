import React from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="mission">
          Elite Asset Allocation
        </div>
        <div className="footer-links">
          <Link to="/privacy" className="footer-link">Privacy Policy</Link>
          <span className="separator">•</span>
          <Link to="/terms" className="footer-link">Terms of Service</Link>
          <span className="separator">•</span>
          <Link to="/about" className="footer-link">About</Link>
          <span className="separator">•</span>
          <Link to="/help" className="footer-link">Help</Link>
          <span className="separator">•</span>
          <a 
            href="mailto:support@portfolioheatmap.com" 
            className="footer-link" 
            target="_blank" 
            rel="noopener noreferrer"
          >
            Contact
          </a>
        </div>
      </div>
    </footer>
  );
}

export default Footer; 
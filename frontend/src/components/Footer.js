import React from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-links">
          <Link to="/about" target="_blank" className="footer-link">About</Link>
          <span className="separator">•</span>
          <Link to="/privacy" target="_blank" className="footer-link">Privacy Policy</Link>
          <span className="separator">•</span>
          <Link to="/terms" target="_blank" className="footer-link">Terms of Service</Link>
          <span className="separator">•</span>
          <Link to="/help" target="_blank" className="footer-link">Help</Link>
          <span className="separator">•</span>
          <Link to="/contact" target="_blank" className="footer-link">Contact</Link>
        </div>
        <p className="copyright">© {new Date().getFullYear()} Portfolio Heatmap. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default Footer; 
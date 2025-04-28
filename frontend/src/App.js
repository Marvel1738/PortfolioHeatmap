// frontend/src/App.js

import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import Heatmap from './components/Heatmap';
import DetailedChart from './components/DetailedChart';
import About from './components/About';
import PrivacyPolicy from './components/PrivacyPolicy';
import TermsOfService from './components/TermsOfService';
import Help from './components/Help';
import Contact from './components/Contact';
import './App.css';
import Login from './components/Login';
import Register from './components/Register';
import GuestLogin from './components/GuestLogin';
import api from './api/axios.js';

/**
 * App component serves as the root of the React frontend.
 * Configures routing for all app pages in the Portfolio Heatmap flow.
 * 
 * @returns {JSX.Element} The rendered App UI with routing
 */
function App() {
  const [authState, setAuthState] = useState({
    isAuthenticated: false,
    isGuest: false,
    username: '',
    token: null
  });

  const updateAuthState = (newState) => {
    console.log('Updating auth state:', newState);
    setAuthState(newState);
    if (newState.token) {
      localStorage.setItem('token', newState.token);
    } else {
      localStorage.removeItem('token');
    }
  };

  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      console.log('Initial token from localStorage:', token);

      if (token) {
        try {
          const tokenPayload = JSON.parse(atob(token.split('.')[1]));
          console.log('Token payload:', tokenPayload);
          
          // Check if token is expired
          const expirationTime = tokenPayload.exp * 1000; // Convert to milliseconds
          const currentTime = Date.now();
          
          if (currentTime < expirationTime) {
            const username = tokenPayload.sub;
            // Check if username starts with 'guest_' to determine if it's a guest user
            const isGuestUser = username.startsWith('guest_');
            const userId = tokenPayload.userId;
            
            // Save the user ID in localStorage
            if (userId) {
              localStorage.setItem('userId', userId);
            }
            
            updateAuthState({
              isAuthenticated: true,
              isGuest: isGuestUser,
              username: username,
              token: token
            });
          } else {
            console.log('Token expired');
            localStorage.removeItem('userId');
            localStorage.removeItem('token');
            updateAuthState({
              isAuthenticated: false,
              isGuest: false,
              username: '',
              token: null
            });
          }
        } catch (error) {
          console.error('Error parsing token:', error);
          localStorage.removeItem('userId');
          localStorage.removeItem('token');
          updateAuthState({
            isAuthenticated: false,
            isGuest: false,
            username: '',
            token: null
          });
        }
      } else {
        // If no token, check if we have guest portfolios in localStorage
        const guestPortfolios = JSON.parse(localStorage.getItem('guestPortfolios') || '[]');
        if (guestPortfolios.length > 0) {
          // If we have guest portfolios, we should be in guest mode
          updateAuthState({
            isAuthenticated: true,
            isGuest: true,
            username: 'Guest',
            token: null
          });
        } else {
          updateAuthState({
            isAuthenticated: false,
            isGuest: false,
            username: '',
            token: null
          });
        }
      }
    };

    initializeAuth();
  }, []);

  // Protected route component
  const ProtectedRoute = ({ children }) => {
    const navigate = useNavigate();
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);

    useEffect(() => {
      const checkAuth = async () => {
        // If we're already authenticated, no need to check further
        if (authState.isAuthenticated) {
          setIsCheckingAuth(false);
          return;
        }

        // Only generate guest token if there's no token at all
        const token = localStorage.getItem('token');
        if (!token) {
          try {
            const response = await api.post('/auth/guest');
            const newToken = response.data;
            const tokenPayload = JSON.parse(atob(newToken.split('.')[1]));
            updateAuthState({
              isAuthenticated: true,
              isGuest: true,
              username: 'Guest',
              token: newToken
            });
          } catch (error) {
            console.error('Failed to generate guest token:', error);
            navigate('/login');
          }
        }
        setIsCheckingAuth(false);
      };

      checkAuth();
    }, [authState.isAuthenticated, navigate]);

    if (isCheckingAuth) {
      return <div>Loading...</div>;
    }

    if (!authState.isAuthenticated) {
      return <Navigate to="/login" />;
    }

    return children;
  };

  return (
    <Router>
      <div className="App">
        <Header authState={authState} updateAuthState={updateAuthState} />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate to="/heatmap" replace />} />
            <Route
              path="/heatmap"
              element={
                <ProtectedRoute>
                  <Heatmap authState={authState} />
                </ProtectedRoute>
              }
            />
            <Route path="/chart/:ticker" element={<DetailedChart />} />
            <Route path="/about" element={<About />} />
            <Route path="/privacy" element={<PrivacyPolicy />} />
            <Route path="/terms" element={<TermsOfService />} />
            <Route path="/help" element={<Help />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/login" element={<Login updateAuthState={updateAuthState} />} />
            <Route path="/register" element={<Register updateAuthState={updateAuthState} />} />
            <Route path="*" element={<Navigate to="/heatmap" replace />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
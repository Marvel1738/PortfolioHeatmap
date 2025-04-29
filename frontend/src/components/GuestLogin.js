import React from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './Login.css';
import api from '../api/axios.js';

const GuestLogin = ({ updateAuthState }) => {
    const navigate = useNavigate();

    const handleGuestLogin = async () => {
        console.log('Starting guest login process');
        try {
            console.log('Sending guest login request');
            const response = await api.post('/auth/guest');
            
            // Check if response is HTML
            if (typeof response.data === 'string' && response.data.includes('<!doctype html>')) {
                console.error('Received HTML instead of token');
                throw new Error('Invalid response from server');
            }
            
            const token = response.data;
            console.log('Received token from guest login');
            
            // Store the token first
            localStorage.setItem('token', token);
            
            try {
                // Parse the token to get user info
                const tokenParts = token.split('.');
                if (tokenParts.length !== 3) {
                    throw new Error('Invalid token format');
                }
                
                // Add padding if needed
                let base64 = tokenParts[1].replace(/-/g, '+').replace(/_/g, '/');
                while (base64.length % 4) {
                    base64 += '=';
                }
                
                const tokenPayload = JSON.parse(atob(base64));
                const username = tokenPayload.sub;
                const userId = tokenPayload.userId;
                console.log('Parsed token payload:', { username, userId });
                
                // Save the guest user ID in localStorage
                localStorage.setItem('guestUserId', userId);
                console.log('Saved guest user ID in localStorage');
                
                // Update auth state
                updateAuthState({
                    isAuthenticated: true,
                    isGuest: true,
                    username: username,
                    token: token
                });
                console.log('Updated auth state for guest user');
                
                navigate('/heatmap');
            } catch (parseError) {
                console.error('Error parsing token:', parseError);
                // Even if we can't parse the token, we can still proceed with the login
                // since we have the token stored
                updateAuthState({
                    isAuthenticated: true,
                    isGuest: true,
                    username: 'guest',
                    token: token
                });
                navigate('/heatmap');
            }
        } catch (error) {
            console.error('Guest login failed:', error);
            if (error.response) {
                console.error('Error response:', error.response.data);
            }
        }
    };

    return (
        <button 
            onClick={handleGuestLogin}
            className="guest-login-button"
        >
            Continue as Guest
        </button>
    );
};

export default GuestLogin; 
import React from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './Login.css';

const GuestLogin = ({ updateAuthState }) => {
    const navigate = useNavigate();

    const handleGuestLogin = async () => {
        console.log('Starting guest login process');
        try {
            console.log('Sending guest login request');
            const response = await axios.post('/auth/guest');
            const token = response.data;
            console.log('Received token from guest login');
            
            // Parse the token to get user info
            const tokenPayload = JSON.parse(atob(token.split('.')[1]));
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
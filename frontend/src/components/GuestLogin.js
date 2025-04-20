import React from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './Login.css';

const GuestLogin = ({ updateAuthState }) => {
    const navigate = useNavigate();

    const handleGuestLogin = async () => {
        try {
            const response = await axios.post('http://localhost:8080/auth/guest');
            const token = response.data;
            
            // Parse the token to get user info
            const tokenPayload = JSON.parse(atob(token.split('.')[1]));
            const username = tokenPayload.sub;
            const userId = tokenPayload.userId; // Assuming the backend includes userId in the token
            
            // Save the guest user ID in localStorage
            localStorage.setItem('guestUserId', userId);
            
            // Update auth state
            updateAuthState({
                isAuthenticated: true,
                isGuest: true,
                username: username,
                token: token
            });
            
            navigate('/heatmap');
        } catch (error) {
            console.error('Guest login failed:', error);
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
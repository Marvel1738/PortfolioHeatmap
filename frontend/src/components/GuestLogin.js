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
            const tokenPayload = JSON.parse(atob(token.split('.')[1]));
            
            updateAuthState({
                isAuthenticated: true,
                isGuest: true,
                username: 'Guest',
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
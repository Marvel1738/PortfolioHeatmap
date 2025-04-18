import React, { useState } from 'react';
import './Page.css';

const Contact = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Here you would typically send the form data to your backend
    console.log('Form submitted:', formData);
    alert('Thank you for your message! We will get back to you soon.');
    setFormData({
      name: '',
      email: '',
      subject: '',
      message: ''
    });
  };

  return (
    <div className="page-container">
      <div className="page-content">
        <h1>Contact Us</h1>
        
        <section className="contact-info">
          <h2>Get in Touch</h2>
          <p>We'd love to hear from you! Whether you have a question about features, need technical support, or want to provide feedback, our team is ready to help.</p>
          
          <div className="contact-methods">
            <div className="contact-method">
              <h3>Email:</h3>
              <p>portfolioheatmap@gmail.com</p>
            </div>
            
            <div className="contact-method">
              <h3>Response Time</h3>
              <p>We aim to respond to all inquiries within 24 hours during business days.</p>
            </div>
          </div>
        </section>

        <section className="contact-form">
          <h2>Send Us a Message</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="name">Name</label>
              <input
                type="text"
                id="name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="subject">Subject</label>
              <input
                type="text"
                id="subject"
                name="subject"
                value={formData.subject}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="message">Message</label>
              <textarea
                id="message"
                name="message"
                value={formData.message}
                onChange={handleChange}
                required
                rows="5"
              />
            </div>

            <button type="submit" className="submit-button">Send Message</button>
          </form>
        </section>
      </div>
    </div>
  );
};

export default Contact; 
// src/pages/about.js
import React from 'react';
import './About.css';
import toTheMoonPic from './toTheMoon.png'; // Replace with your image path


const About = () => {
  return (
    <div className="about-container">
      <h1 className="about-title">About</h1>
      <img src={toTheMoonPic} alt="Funny picture" className="about-image" />
      <blockquote className="about-quote">
        "Don’t put all your eggs in one basket!"
      </blockquote>

      <section className="about-section">
        <h2>What's Portfolio Diversification?</h2>
        <p>
          At the core of smart investing lies one golden rule: portfolio
          diversification. Spreading your investments across different assets can
          help minimize risk, protect against market swings, and make sure one bad
          stock doesn’t take your entire portfolio down with it. Whether you're
          investing in tech giants or penny stocks, seeing how your money is
          distributed (and how each part is performing) is crucial.
        </p>
        <p>
          But traditional spreadsheets and apps just don’t cut it. You need
          something more visual, more intuitive — something built for the modern
          investor.
        </p>
      </section>

      <section className="about-section">
        <h2>Why I Built Portfolio Heatmap</h2>
        <p>
          Portfolio Heatmap is the ultimate tool for investors to manage their
          assets efficiently — and actually enjoy doing it. It helps you visualize
          your portfolio in a way that’s clear, dynamic, and actually useful. By
          showing your portfolio as a heatmap, you can instantly understand your
          allocations, performance, and risk exposure. No more guessing or digging
          through spreadsheets.
        </p>
        <p>
          I built this tool for myself — because I wanted something that looked
          good, worked well, and helped me make better decisions. But I realized it
          could help others too. Whether you're new to investing or managing a
          seven-figure portfolio, Portfolio Heatmap brings clarity to your chaos
          and baskets for your eggs. Let's make smart investments and reach
          financial freedom together!
        </p>
        <p className="about-founder">— Founder/Owner Marvel Bana</p>
      </section>
    </div>
  );
};

export default About;
<div className="app-container">
  <Header authState={authState} onLogout={handleLogout} onLogin={handleLogin} onRegister={handleRegister} />
  <Routes>
    <Route path="/" element={<Home />} />
    <Route
      path="/heatmap"
      element={
        <Heatmap
          authState={authState}
        />
      }
    />
  </Routes>
</div>

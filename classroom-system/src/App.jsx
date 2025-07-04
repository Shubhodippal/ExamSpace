import { useState, useEffect } from 'react'
import { Routes, Route, useNavigate } from 'react-router-dom'
import './App.css'
import ContactForm from './components/ContactForm'
import LoginSignup from './components/LoginSignup'
import Dashboard from './pages/Dashboard/Dashboard'
import { AuthProvider, useAuth } from './contexts/AuthContext'

// Create a protected route component
function ProtectedRoute({ element }) {
  const { isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();
  
  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, isLoading, navigate]);
  
  if (isLoading) {
    return (
      <div className="dashboard-loading">
        <div className="spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }
  
  return isAuthenticated ? element : null;
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

// Make sure AppContent is defined AFTER App, not before it
function AppContent() {
  const [currentView, setCurrentView] = useState('home');
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const scrollToSection = (sectionId) => {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ 
        behavior: 'smooth',
        block: 'start'
      });
    }
  }

  const navigateToAuth = () => {
    // Check if the user is already authenticated
    if (isAuthenticated) {
      // If authenticated, navigate to dashboard
      navigate('/dashboard');
    } else {
      // If not authenticated, show login screen
      setCurrentView('login');
    }
  }

  const navigateToHome = () => {
    setCurrentView('home')
  }

  // Routes for navigation with React Router
  return (
    <Routes>
      <Route path="/dashboard" element={<ProtectedRoute element={<Dashboard />} />} />
      <Route path="*" element={
        currentView === 'login' ? (
          <div className="app">
            <header className="header">
              <div className="nav-container">
                <div className="logo">
                  <h2>📝 ExamSpace</h2>
                </div>
                <nav className="nav-links">
                  <button 
                    className="nav-button" 
                    onClick={navigateToHome}
                  >
                    Back to Home
                  </button>
                </nav>
              </div>
            </header>
            <main className="main-content">
              <section className="auth-section">
                <LoginSignup />
              </section>
            </main>
          </div>
        ) : (
          <div className="app">
            <header className="header">
              <div className="nav-container">
                <div className="logo">
                  <h2>📝 ExamSpace</h2>
                </div>
                <nav className="nav-links">
                  <button 
                    className="nav-button" 
                    onClick={() => scrollToSection('features')}
                  >
                    Features
                  </button>
                  <button 
                    className="nav-button" 
                    onClick={() => scrollToSection('about')}
                  >
                    About
                  </button>
                  <button 
                    className="nav-button" 
                    onClick={() => scrollToSection('contact')}
                  >
                    Contact
                  </button>
                  <button 
                    className="btn btn-secondary" 
                    onClick={navigateToAuth}
                    style={{ marginLeft: '1rem' }}
                  >
                    {isAuthenticated ? 'Go to Dashboard' : 'Login / Sign Up'}
                  </button>
                </nav>
              </div>
            </header>

            <main className="main-content">
              {/* Hero Section */}
              <section className="hero">
                <div className="hero-content">
                  <h1>Welcome to ExamSpace</h1>
                  <p>Your comprehensive platform for online learning and assessment</p>
                  <div className="hero-features">
                    <div className="feature-item">
                      <span className="feature-icon">📝</span>
                      <span>Online Exams</span>
                    </div>
                    <div className="feature-item">
                      <span className="feature-icon">👥</span>
                      <span>User Management</span>
                    </div>
                    <div className="feature-item">
                      <span className="feature-icon">📊</span>
                      <span>Real-time Results</span>
                    </div>
                  </div>
                  <div style={{ marginTop: '2rem' }}>
                    <button 
                      className="btn btn-primary btn-large"
                      onClick={navigateToAuth}
                    >
                      {isAuthenticated ? 'Access Dashboard' : 'Get Started'}
                    </button>
                  </div>
                </div>
              </section>

              {/* Features Section */}
              <section id="features" className="features">
                <div className="container">
                  <h2>Platform Features</h2>
                  <div className="features-grid">
                    <div className="feature-card">
                      <div className="feature-icon-large">🎯</div>
                      <h3>Smart Assessments</h3>
                      <p>Create and manage comprehensive online examinations with various question types and automated grading.</p>
                    </div>
                    <div className="feature-card">
                      <div className="feature-icon-large">🔐</div>
                      <h3>Secure Authentication</h3>
                      <p>Advanced JWT-based authentication system with email verification and password reset functionality.</p>
                    </div>
                    <div className="feature-card">
                      <div className="feature-icon-large">📈</div>
                      <h3>Analytics Dashboard</h3>
                      <p>Track student performance, exam statistics, and generate detailed reports for better insights.</p>
                    </div>
                    <div className="feature-card">
                      <div className="feature-icon-large">🌐</div>
                      <h3>Multi-Platform Access</h3>
                      <p>Access your classroom from anywhere with our responsive web application and cross-platform support.</p>
                    </div>
                  </div>
                </div>
              </section>

              {/* About Section */}
              <section id="about" className="about">
                <div className="container">
                  <h2>About Our Platform</h2>
                  <div className="about-content">
                    <p>ExamSpace is designed to revolutionize the way educational institutions conduct online assessments and manage their learning environments.</p>
                    <p>Built with modern web technologies and security best practices, we provide a reliable and user-friendly platform for educators and students alike.</p>
                  </div>
                </div>
              </section>

              {/* Contact Section */}
              <section id="contact" className="contact">
                <div className="container">
                  <h2>Contact Us</h2>
                  <div className="contact-content">
                    <ContactForm />
                  </div>
                </div>
              </section>
            </main>

            <footer className="footer">
              <div className="container">
                <p>&copy; 2025 ExamSpace. All rights reserved.</p>
              </div>
            </footer>
          </div>
        )
      } />
    </Routes>
  )
}

export default App;

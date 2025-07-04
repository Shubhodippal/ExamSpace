import { createContext, useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import TokenService from '../services/TokenService';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const navigate = useNavigate();
  
  const checkAuthentication = async () => {
    const accessToken = TokenService.getAccessToken();
    
    if (!accessToken) {
      setIsAuthenticated(false);
      setIsLoading(false);
      return false;
    }
    
    if (TokenService.isTokenExpired(accessToken)) {
      // Access token expired, try to refresh
      const refreshSuccess = await TokenService.refreshAccessToken();
      setIsAuthenticated(refreshSuccess);
      setIsLoading(false);
      return refreshSuccess;
    } else {
      // Access token valid
      setIsAuthenticated(true);
      setIsLoading(false);
      return true;
    }
  };
  
  const logout = () => {
    TokenService.clearTokens();
    setIsAuthenticated(false);
    navigate('/');
  };
  
  // Check auth on component mount
  useEffect(() => {
    checkAuthentication();
  }, []);
  
  // Set up interval to periodically check token validity
  useEffect(() => {
    const intervalId = setInterval(async () => {
      const accessToken = TokenService.getAccessToken();
      
      if (accessToken && TokenService.isTokenExpired(accessToken)) {
        const refreshSuccess = await TokenService.refreshAccessToken();
        
        if (!refreshSuccess && isAuthenticated) {
          // Both tokens expired, show login modal
          setShowLoginModal(true);
          setIsAuthenticated(false);
        }
      }
    }, 30000); // Check every 30 seconds
    
    return () => clearInterval(intervalId);
  }, [isAuthenticated]);
  
  const value = {
    isAuthenticated,
    isLoading,
    showLoginModal,
    setShowLoginModal,
    checkAuthentication,
    logout
  };
  
  return (
    <AuthContext.Provider value={value}>
      {children}
      {showLoginModal && (
        <div className="auth-modal">
          <div className="auth-modal-content">
            <h2>Session Expired</h2>
            <p>Your session has expired. Please log in again to continue.</p>
            <button
              className="btn btn-primary"
              onClick={() => {
                setShowLoginModal(false);
                navigate('/');
              }}
            >
              Log In
            </button>
          </div>
        </div>
      )}
    </AuthContext.Provider>
  );
}

// Add this useAuth hook
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
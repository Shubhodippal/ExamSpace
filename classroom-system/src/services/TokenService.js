const TokenService = {
  getAccessToken: () => localStorage.getItem('token'),
  getRefreshToken: () => localStorage.getItem('refreshToken'),
  
  updateTokens: (accessToken, refreshToken) => {
    localStorage.setItem('token', accessToken);
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }
  },
  
  clearTokens: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  },
  
  decodeToken: (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  },
  
  isTokenExpired: (token) => {
    if (!token) return true;
    
    const decodedToken = TokenService.decodeToken(token);
    if (!decodedToken) return true;
    
    const currentTime = Date.now() / 1000;
    return decodedToken.exp && decodedToken.exp < currentTime;
  },
  
  refreshAccessToken: async () => {
    const refreshToken = TokenService.getRefreshToken();
    
    if (!refreshToken) {
      return false;
    }
    
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/users/refresh-token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify({ refreshToken })
      });
      
      const data = await response.json();
      
      if (response.ok) {
        TokenService.updateTokens(data.token, data.refreshToken);
        console.log('Token refreshed successfully');
        return true;
      } else {
        TokenService.clearTokens();
        return false;
      }
    } catch (error) {
      console.error('Error refreshing token:', error);
      TokenService.clearTokens();
      return false;
    }
  }
};

export default TokenService;
import { useState, useEffect } from 'react';
import TokenService from '../../services/TokenService';
import './ProfileSection.css';

function ProfileSection({ userData }) {
  const [profileData, setProfileData] = useState({
    name: '',
    email: '',
    phone: ''
  });
  const [isUpdating, setIsUpdating] = useState(false);
  const [updateMessage, setUpdateMessage] = useState({ type: '', text: '' });

  // Function to get user data from token
  const getUserDataFromToken = () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) return null;
      
      // Decode the JWT token
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      
      const decoded = JSON.parse(jsonPayload);
      return {
        name: decoded.name || null,
        email: decoded.email || null,
        phone: null // Token typically doesn't contain phone info
      };
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  };

  // Initialize profile data from token or props on component mount
  useEffect(() => {
    // First try to get data from token
    const tokenData = getUserDataFromToken();
    
    if (tokenData && tokenData.name) {
      setProfileData({
        name: tokenData.name,
        email: tokenData.email || '',
        phone: userData?.phone || '' // Fall back to userData for phone
      });
    } else if (userData) {
      // Fall back to userData prop if token data is not available
      setProfileData({
        name: userData.name || '',
        email: userData.email || '',
        phone: userData.phone || ''
      });
    }
  }, [userData]);

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    setIsUpdating(true);
    setUpdateMessage({ type: '', text: '' });

    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/users/update-profile`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify({
          name: profileData.name,
          email: profileData.email,
          phone: profileData.phone || null
        })
      });

      const data = await response.json();

      if (response.ok) {
        setUpdateMessage({ type: 'success', text: data.message || 'Profile updated successfully!' });
        
        // Update localStorage user data if it exists
        const userInfo = localStorage.getItem('user');
        if (userInfo) {
          try {
            const parsedUserInfo = JSON.parse(userInfo);
            localStorage.setItem('user', JSON.stringify({
              ...parsedUserInfo,
              name: profileData.name,
              email: profileData.email,
              phone: profileData.phone
            }));
          } catch (error) {
            console.error('Error updating user data in localStorage:', error);
          }
        }
        
        // Refresh token and reload page after successful profile update
        try {
          // Show refreshing message
          setUpdateMessage({ type: 'success', text: 'Profile updated successfully! Refreshing session...' });
          
          // Refresh the token
          const tokenRefreshed = await TokenService.refreshAccessToken();
          
          if (tokenRefreshed) {
            // Set a short timeout to allow user to see the success message before reload
            setTimeout(() => {
              window.location.reload();
            }, 1000);
          } else {
            // If token refresh fails, still show success for profile update
            setUpdateMessage({ 
              type: 'success', 
              text: 'Profile updated successfully! Please refresh the page to see changes.'
            });
          }
        } catch (refreshError) {
          console.error('Error refreshing token after profile update:', refreshError);
          // Still show success for profile update
          setUpdateMessage({ 
            type: 'success', 
            text: 'Profile updated successfully! Please refresh the page manually to see changes.'
          });
        }
      } else {
        setUpdateMessage({ type: 'error', text: data.message || 'Failed to update profile' });
      }
    } catch (error) {
      console.error('Error updating profile:', error);
      setUpdateMessage({ type: 'error', text: 'Network error. Please try again.' });
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="dashboard-section">
      <h2>Profile Settings</h2>
      <div className="profile-container">
        <div className="profile-card">
          <div className="profile-header">
            <div className="profile-avatar">
              {userData?.name?.charAt(0) || 'U'}
            </div>
            <div className="profile-info">
              <h3>{userData?.name || 'User'}</h3>
              <p>{userData?.email || 'No email available'}</p>
            </div>
          </div>
          
          <form className="profile-form" onSubmit={handleProfileUpdate}>
            <div className="form-group">
              <label htmlFor="name">Full Name</label>
              <input
                type="text"
                id="name"
                name="name"
                value={profileData.name}
                onChange={handleProfileChange}
                required
                disabled={isUpdating}
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <input
                type="email"
                id="email"
                name="email"
                value={profileData.email}
                onChange={handleProfileChange}
                required
                disabled={isUpdating}
                readOnly
                className="read-only-field"
              />
              <small className="field-note">Email address cannot be changed</small>
            </div>
            
            <div className="form-group">
              <label htmlFor="phone">Phone Number (Optional)</label>
              <input
                type="tel"
                id="phone"
                name="phone"
                value={profileData.phone || ''}
                onChange={handleProfileChange}
                disabled={isUpdating}
              />
            </div>
            
            {updateMessage.text && (
              <div className={`status-message ${updateMessage.type}`}>
                {updateMessage.type === 'success' ? '✅ ' : updateMessage.type === 'error' ? '❌ ' : ''}
                {updateMessage.text}
              </div>
            )}
            
            <div className="form-actions">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={isUpdating}
              >
                {isUpdating ? 'Updating...' : 'Update Profile'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default ProfileSection;
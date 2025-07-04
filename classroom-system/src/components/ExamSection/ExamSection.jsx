import { useState, useEffect } from 'react';
import './ExamSection.css';
import QuestionGenerator from './QuestionGenerator';
import CreateExamForm from './CreateExamForm';
import QuestionListEditor from './QuestionListEditor';
import EditExamForm from './EditExamForm';
import ExamToggleButton from './ExamToggleButton';

function ExamSection() {
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [exams, setExams] = useState([]);
  const [sharedExams, setSharedExams] = useState([]);
  const [isLoadingSharedExams, setIsLoadingSharedExams] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showEditForm, setShowEditForm] = useState(false);
  const [currentExam, setCurrentExam] = useState(null);
  // Question editor state
  const [showQuestionEditor, setShowQuestionEditor] = useState(false);
  const [selectedExamForQuestions, setSelectedExamForQuestions] = useState(null);
  // Question list editor state
  const [showQuestionListEditor, setShowQuestionListEditor] = useState(false);
  // Tab state
  const [activeTab, setActiveTab] = useState('myExams');

  // Fetch exams on component mount
  useEffect(() => {
    fetchExams();
    fetchSharedExams();
  }, []);

  const getUserIdFromToken = () => {
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
      return decoded.userId || decoded.sub; // depending on your token structure
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  };

  // Add this function to get email from token
  const getUserEmailFromToken = () => {
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
      return decoded.email || null; // Extract email from token
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  };

  const fetchExams = async () => {
    setIsRefreshing(true);
    
    try {
      const userId = getUserIdFromToken();
      
      if (!userId) {
        console.error('User ID not found in token');
        return;
      }
      
      const response = await fetch(`http://localhost:8080/exam/my-exams/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        }
      });
      
      if (!response.ok) {
        throw new Error(`Error fetching exams: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (data.status === 'success' && Array.isArray(data.exams)) {
        // Map API response to component state format
        const formattedExams = data.exams.map(exam => ({
          id: exam.id,
          examId: exam.examId,
          title: exam.examName,
          description: `Status: ${exam.state}, Marks: ${exam.marks}`,
          duration: exam.timeLimit,
          startTime: exam.startAt,
          endTime: exam.endAt,
          passcode: exam.examPasscode,
          state: exam.state,
          createdAt: exam.createdAt,
          sharing: exam.sharing
        }));
        
        setExams(formattedExams);
      } else {
        console.error('Invalid response format:', data);
      }
    } catch (error) {
      console.error('Error fetching exams:', error);
    } finally {
      setIsRefreshing(false);
    }
  };

  const fetchSharedExams = async () => {
    setIsLoadingSharedExams(true);
    
    try {
      // Get email from JWT token instead of hardcoding
      const userEmail = getUserEmailFromToken();
      
      if (!userEmail) {
        console.error('User email not found in token');
        setIsLoadingSharedExams(false);
        return;
      }
      
      const response = await fetch(`http://localhost:8080/exam/shared-exams?email=${encodeURIComponent(userEmail)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        }
      });
      
      if (!response.ok) {
        throw new Error(`Error fetching shared exams: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (data.status === 'success' && Array.isArray(data.exams)) {
        // Format the shared exams data
        const formattedSharedExams = data.exams.map(exam => ({
          id: exam.examId,
          examId: exam.examId,
          title: exam.examName,
          description: `Status: ${exam.status}, Marks: ${exam.marks}`,
          duration: exam.duration,
          state: exam.status, // Map status to state for consistency
          createdAt: exam.createdAt,
          sharing: exam.sharing,
          passcode: exam.examPasscode,
          creatorName: exam.creatorName,
          creatorEmail: exam.creatorEmail
        }));
        
        setSharedExams(formattedSharedExams);
      } else {
        console.error('Invalid response format for shared exams:', data);
      }
    } catch (error) {
      console.error('Error fetching shared exams:', error);
    } finally {
      setIsLoadingSharedExams(false);
    }
  };

  const handleCreateExam = () => {
    setShowCreateForm(true);
  };

  const handleCancelCreate = () => {
    setShowCreateForm(false);
  };

  const handleExamCreated = () => {
    // Refresh the exams list
    fetchExams();
    // Close the create form
    setShowCreateForm(false);
  };

  const handleEditExam = (exam) => {
    setCurrentExam(exam);
    setShowEditForm(true);
  };

  const handleCancelEdit = () => {
    setShowEditForm(false);
    setCurrentExam(null);
  };

  const handleUpdateExam = () => {
    // Refresh the exams list
    fetchExams();
    fetchSharedExams(); // Also refresh shared exams
    // Close the edit form
    handleCancelEdit();
  };

  const handleToggleExamStatus = (exam, newStatus) => {
    // Update local state for my exams
    const updatedExams = exams.map(e => {
      if (e.examId === exam.examId) {
        return {
          ...e,
          state: newStatus,
          description: `Status: ${newStatus}, Marks: ${e.description.replace(/^Status: .+?, Marks: (.+?)$/, '$1')}`
        };
      }
      return e;
    });
    
    // Update local state for shared exams
    const updatedSharedExams = sharedExams.map(e => {
      if (e.examId === exam.examId) {
        return {
          ...e,
          state: newStatus,
          description: `Status: ${newStatus}, Marks: ${e.description.replace(/^Status: .+?, Marks: (.+?)$/, '$1')}`
        };
      }
      return e;
    });
    
    setExams(updatedExams);
    setSharedExams(updatedSharedExams);
  };

  const handleEditQuestions = (exam) => {
    setSelectedExamForQuestions(exam);
    setShowQuestionEditor(true);
  };

  const handleCancelQuestionEditor = () => {
    setShowQuestionEditor(false);
    setSelectedExamForQuestions(null);
  };

  const handleEditExistingQuestions = (exam) => {
    setSelectedExamForQuestions(exam);
    setShowQuestionListEditor(true);
  };

  const handleCloseQuestionEditor = () => {
    setShowQuestionListEditor(false);
    setSelectedExamForQuestions(null);
  };

  const handleDeleteExam = async (exam) => {
    // First confirmation - require typing DELETE
    const deleteConfirmation = prompt(`Type "DELETE" (in all capitals) to confirm that you want to delete the exam "${exam.title}"`);
    
    // Check if user typed DELETE correctly
    if (deleteConfirmation !== "DELETE") {
      alert("Exam deletion cancelled. You must type DELETE exactly to proceed.");
      return;
    }
    
    // Second confirmation dialog
    if (!window.confirm(`Are you sure you want to delete the exam "${exam.title}" and all its questions? This action cannot be undone.`)) {
      return; // User canceled the operation
    }
    
    try {
      const response = await fetch(`http://localhost:8080/exam/${exam.examId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        }
      });
      
      if (!response.ok) {
        throw new Error(`Error deleting exam: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (data.status === 'success') {
        // Remove exam from local state
        if (activeTab === 'myExams') {
          setExams(exams.filter(e => e.examId !== exam.examId));
        } else {
          setSharedExams(sharedExams.filter(e => e.examId !== exam.examId));
        }
        
        // Show success message
        alert(data.message || 'Exam deleted successfully');
      } else {
        throw new Error(data.message || 'Failed to delete exam');
      }
    } catch (error) {
      console.error('Error deleting exam:', error);
      alert(`Failed to delete exam: ${error.message}`);
    }
  };

  // Render a single exam card (used for both my exams and shared exams)
  const renderExamCard = (exam, isShared = false) => (
    <div key={exam.id} className="exam-card">
      <h3>{exam.title}</h3>
      <p>{exam.description}</p>
      {isShared && (
        <p className="exam-creator">Created by: {exam.creatorName} ({exam.creatorEmail})</p>
      )}
      <div className="exam-details">
        <div className="exam-info-grid">
          <span className="exam-id">
            <strong>Exam ID:</strong> {exam.examId}
          </span>
          <span className="exam-duration">
            <strong>Duration:</strong> {exam.duration} minutes
          </span>
          <span className="exam-created">
            <strong>Created:</strong> {new Date(exam.createdAt).toLocaleString()}
          </span>
          {exam.sharing && (
            <span className="exam-sharing">
              <strong>Shared with:</strong> {exam.sharing.split(',').map(email => email.trim()).join(', ')}
            </span>
          )}
        </div>
        <div className="exam-actions">
          <ExamToggleButton 
            exam={exam}
            onToggle={handleToggleExamStatus}
          />
          <button 
            className="edit-button"
            onClick={() => handleEditExam(exam)}
          >
            <i className="edit-icon">✏️</i> Edit Exam
          </button>
          <button 
            className="edit-button question-button"
            onClick={() => handleEditQuestions(exam)}
          >
            <i className="edit-icon">➕</i> Generate Questions
          </button>
          <button 
            className="edit-button question-button edit-question-button"
            onClick={() => handleEditExistingQuestions(exam)}
          >
            <i className="edit-icon">❓</i> Edit Questions
          </button>
          {/* Add Delete Button - only show for owned exams, not shared ones */}
          {!isShared && (
            <button 
              className="edit-button delete-button"
              onClick={(e) => {
                e.stopPropagation();
                handleDeleteExam(exam);
              }}
            >
              <i className="edit-icon">🗑️</i> Delete Exam
            </button>
          )}
        </div>
      </div>
    </div>
  );

  return (
    <div className="dashboard-section">
      <div className="section-header">
        <h2>Exams</h2>
        <button 
          className="btn btn-primary"
          onClick={handleCreateExam}
        >
          Create Exam
        </button>
      </div>
      
      {/* Create Exam Form */}
      {showCreateForm && (
        <CreateExamForm 
          onSubmit={handleExamCreated} 
          onCancel={handleCancelCreate}
          getUserId={getUserIdFromToken}
        />
      )}
      
      {/* Edit Exam Form */}
      {showEditForm && currentExam && (
        <EditExamForm
          exam={currentExam}
          onCancel={handleCancelEdit}
          onUpdate={handleUpdateExam}
        />
      )}
      
      {/* Question Generator Component */}
      {showQuestionEditor && selectedExamForQuestions && (
        <QuestionGenerator
          isVisible={showQuestionEditor}
          examData={selectedExamForQuestions}
          onClose={handleCancelQuestionEditor}
          getUserId={getUserIdFromToken}
        />
      )}
      
      {/* Question List Editor Component */}
      {showQuestionListEditor && selectedExamForQuestions && (
        <QuestionListEditor
          isVisible={showQuestionListEditor}
          examData={selectedExamForQuestions}
          onClose={handleCloseQuestionEditor}
          getUserId={getUserIdFromToken}
        />
      )}
      
      {/* Tab Navigation */}
      <div className="exam-tabs">
        <button 
          className={`tab-button ${activeTab === 'myExams' ? 'active' : ''}`}
          onClick={() => setActiveTab('myExams')}
        >
          My Exams
        </button>
        <button 
          className={`tab-button ${activeTab === 'sharedExams' ? 'active' : ''}`}
          onClick={() => setActiveTab('sharedExams')}
        >
          Shared With Me
        </button>
      </div>
      
      {/* Tab Content Container */}
      <div className="exams-container">
        {/* My Exams Tab */}
        {activeTab === 'myExams' && (
          <>
            {isRefreshing ? (
              <div className="loading-indicator">
                <div className="spinner"></div>
                <p>Loading exams...</p>
              </div>
            ) : exams.length > 0 ? (
              <div className="exams-list">
                {exams.map(exam => renderExamCard(exam))}
              </div>
            ) : (
              <div className="exams-empty">
                <p>No exams available at the moment.</p>
                <button 
                  className="btn btn-primary"
                  onClick={fetchExams}
                  disabled={isRefreshing}
                >
                  {isRefreshing ? 'Refreshing...' : 'Refresh'}
                </button>
              </div>
            )}
          </>
        )}
        
        {/* Shared Exams Tab */}
        {activeTab === 'sharedExams' && (
          <>
            {isLoadingSharedExams ? (
              <div className="loading-indicator">
                <div className="spinner"></div>
                <p>Loading shared exams...</p>
              </div>
            ) : sharedExams.length > 0 ? (
              <div className="exams-list">
                {sharedExams.map(exam => renderExamCard(exam, true))}
              </div>
            ) : (
              <div className="exams-empty">
                <p>No shared exams available at the moment.</p>
                <button 
                  className="btn btn-primary"
                  onClick={fetchSharedExams}
                  disabled={isLoadingSharedExams}
                >
                  {isLoadingSharedExams ? 'Refreshing...' : 'Refresh'}
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default ExamSection;
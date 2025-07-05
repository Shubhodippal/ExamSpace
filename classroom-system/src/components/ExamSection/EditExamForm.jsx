import { useState, useEffect } from 'react';
import './ExamSection.css';

function EditExamForm({ exam, onCancel, onUpdate}) {
  const [currentExam, setCurrentExam] = useState({
    examId: '',
    title: '',
    description: '',
    passcode: '',
    sharing: ''
  });

  useEffect(() => {
    if (exam) {
      setCurrentExam({
        ...exam,
        title: exam.title,
        description: exam.description.replace(/^Status: .+?, Marks: (.+?)$/, '$1'),
        passcode: exam.passcode || '',
        sharing: exam.sharing || ''
      });
    }
  }, [exam]);

  const handleEditExamChange = (e) => {
    const { name, value } = e.target;
    setCurrentExam({
      ...currentExam,
      [name]: value,
    });
  };

  const handleUpdateExam = async (e) => {
    e.preventDefault();
    
    try {
      const response = await fetch(`http://localhost:8080/exam/${currentExam.examId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify({
          examName: currentExam.title,
          marks: currentExam.description.replace(/^Status: .+?, Marks: (.+?)$/, '$1'),
          examPasscode: currentExam.passcode,
          sharing: currentExam.sharing || ''
        })
      });
      
      if (!response.ok) {
        throw new Error(`Error updating exam: ${response.status}`);
      }
      
      onUpdate();
    } catch (error) {
      console.error('Error updating exam:', error);
    }
  };

  return (
    <div className="create-exam-form">
      <h3>Edit Exam</h3>
      <form onSubmit={handleUpdateExam}>
        <div className="form-group">
          <label htmlFor="edit-title">Exam Title</label>
          <input
            type="text"
            id="edit-title"
            name="title"
            value={currentExam.title}
            onChange={handleEditExamChange}
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="edit-description">Marks</label>
          <input
            type="number"
            id="edit-description"
            name="description"
            value={currentExam.description.replace(/^Status: .+?, Marks: (.+?)$/, '$1')}
            onChange={handleEditExamChange}
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="edit-passcode">Exam Passcode</label>
          <input
            type="text"
            id="edit-passcode"
            name="passcode"
            value={currentExam.passcode || ''}
            onChange={handleEditExamChange}
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="edit-sharing">Share with (comma-separated emails)</label>
          <input
            type="text"
            id="edit-sharing"
            name="sharing"
            value={currentExam.sharing || ''}
            onChange={handleEditExamChange}
            placeholder="Enter emails separated by commas (e.g., user1@example.com, user2@example.com)"
          />
          <small className="form-text text-muted">You can share this exam with multiple users by separating email addresses with commas.</small>
        </div>
        
        <div className="form-actions">
          <button type="button" className="btn btn-secondary" onClick={onCancel}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary">
            Update Exam
          </button>
        </div>
      </form>
    </div>
  );
}

export default EditExamForm;
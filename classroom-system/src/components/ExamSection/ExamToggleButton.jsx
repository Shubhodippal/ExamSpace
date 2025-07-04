import { useState } from 'react';
import './ExamSection.css';

function ExamToggleButton({ exam, onToggle}) {
  const [isLoading, setIsLoading] = useState(false);

  const handleToggleExamStatus = async () => {
    setIsLoading(true);
    
    try {
      const newStatus = exam.state === 'ON' ? 'OFF' : 'ON';
      
      const response = await fetch(`http://localhost:8080/exam/${exam.examId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify({
          state: newStatus
        })
      });
      
      if (!response.ok) {
        throw new Error(`Error toggling exam status: ${response.status}`);
      }
      
      // Call the parent component's handler
      onToggle(exam, newStatus);
    } catch (error) {
      console.error('Error toggling exam status:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="toggle-container">
      <label className="switch">
        <input 
          type="checkbox" 
          checked={exam.state === 'ON'} 
          onChange={handleToggleExamStatus}
          disabled={isLoading}
        />
        <span className="slider round"></span>
        <span className="status-label">{exam.state === 'ON' ? 'ON' : 'OFF'}</span>
      </label>
    </div>
  );
}

export default ExamToggleButton;
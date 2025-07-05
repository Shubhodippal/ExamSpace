import { useState } from 'react';
import './ExamSection.css';

function QuestionGenerator({ 
  isVisible, 
  examData, 
  onClose, 
  getUserId 
}) {
  // State variables
  const [questionSource, setQuestionSource] = useState('notes');
  const [aiQuestionParams, setAiQuestionParams] = useState({
    subject: '',
    topic: '',
    topics: '',
    level: 'medium',
    numberOfQuestions: examData ? parseInt(examData.description.replace(/^Status: .+?, Marks: (.+?)$/, '$1')) || 10 : 10
  });
  const [contentQuestionParams, setContentQuestionParams] = useState({
    numberOfQuestions: 10,
    difficulty: 'medium'
  });
  const [isGeneratingQuestions, setIsGeneratingQuestions] = useState(false);
  const [questionGenerationSuccess, setQuestionGenerationSuccess] = useState(false);
  const [manualQuestion, setManualQuestion] = useState({
    question: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    correctAns: 'A'
  });
  const [manualQuestionSuccess, setManualQuestionSuccess] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadSuccess, setUploadSuccess] = useState(false);
  const [uploadedContent, setUploadedContent] = useState(null);

  // Handler functions
  const handleAiParamsChange = (e) => {
    const { name, value } = e.target;
    setAiQuestionParams({
      ...aiQuestionParams,
      [name]: value,
    });
  };

  const handleContentParamsChange = (e) => {
    const { name, value } = e.target;
    setContentQuestionParams({
      ...contentQuestionParams,
      [name]: value
    });
  };

  const handleFileUpload = (e) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      console.log(`${files.length} files selected`);
      
      const formData = new FormData();
      formData.append('examId', examData.examId);
      
      for (let i = 0; i < files.length; i++) {
        formData.append('files', files[i]);
        console.log(`Adding file: ${files[i].name} (${files[i].type})`);
      }
      
      uploadFiles(formData);
    }
  };

  const uploadFiles = async (formData) => {
    setIsUploading(true);
    setUploadSuccess(false);
    
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/exam/upload`, {
        method: 'POST',
        headers: {
          'X-API-Key': 'Meowmeowmeow123456789'
        },
        body: formData
      });
      
      const data = await response.json();
      console.log('Upload response:', data);
      
      if (response.ok) {
        setUploadSuccess(true);
        setUploadedContent(data);
      }
      
    } catch (error) {
      console.error('Error uploading files:', error);
    } finally {
      setIsUploading(false);
    }
  };

  const handleManualQuestionSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const userId = getUserId();
      
      if (!userId) {
        console.error('User ID not found in token');
        return;
      }
      
      if (!manualQuestion.question || !manualQuestion.optionA || !manualQuestion.optionB) {
        alert('Question, Option A and Option B are required');
        return;
      }
      
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/exam/add-question`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify({
          examUid: examData.examId,
          creatorUid: userId,
          question: manualQuestion.question,
          optionA: manualQuestion.optionA,
          optionB: manualQuestion.optionB,
          optionC: manualQuestion.optionC || null,
          optionD: manualQuestion.optionD || null,
          correctAns: manualQuestion.correctAns
        })
      });
      
      if (!response.ok) {
        throw new Error(`Error creating question: ${response.status}`);
      }
      
      setManualQuestion({
        question: '',
        optionA: '',
        optionB: '',
        optionC: '',
        optionD: '',
        correctAns: 'A'
      });
      
      setManualQuestionSuccess(true);
      
      setTimeout(() => {
        setManualQuestionSuccess(false);
      }, 3000);
    } catch (error) {
      console.error('Error creating question:', error);
      alert('Failed to create question. Please try again.');
    }
  };

  const handleAiQuestionSubmit = async (e) => {
    e.preventDefault();
    setIsGeneratingQuestions(true);
    setQuestionGenerationSuccess(false);
    
    try {
      const userId = getUserId();
      
      if (!userId) {
        console.error('User ID not found in token');
        return;
      }
      
      const specificAreas = aiQuestionParams.topics
        .split(',')
        .map(topic => topic.trim())
        .filter(topic => topic.length > 0);
      
      const numberOfQuestions = parseInt(aiQuestionParams.numberOfQuestions) || 10;
      
      const requestData = {
        examUid: examData.examId,
        creatorUid: userId,
        numberOfQuestions: numberOfQuestions,
        subject: aiQuestionParams.subject,
        topic: aiQuestionParams.topic,
        specificAreas: specificAreas,
        difficulty: aiQuestionParams.level
      };
      
      console.log('Sending AI question generation request:', requestData);
      
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/exam/generate-questions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify(requestData)
      });
      
      const data = await response.json();
      console.log('AI question generation response:', data);
      
      if (response.ok) {
        setQuestionGenerationSuccess(true);
        
        setTimeout(() => {
          onClose();
        }, 3000);
      } else {
        console.error('Error details:', data);
        throw new Error(`Error generating questions: ${response.status}`);
      }
      
    } catch (error) {
      console.error('Error generating AI questions:', error);
      alert('Failed to generate questions. Please try again.');
    } finally {
      setIsGeneratingQuestions(false);
    }
  };

  const handleGenerateQuestionsFromContent = async (e) => {
    e.preventDefault();
    setIsGeneratingQuestions(true);
    setQuestionGenerationSuccess(false);
    
    try {
      const userId = getUserId();
      
      if (!userId) {
        console.error('User ID not found in token');
        return;
      }
      
      if (!uploadedContent) {
        throw new Error('No content available from uploaded files');
      }
      
      console.log('Upload content structure:', uploadedContent);
      
      const results = uploadedContent.results || {};
      
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/exam/generate-questions-from-content`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify({
          examUid: examData.examId,
          creatorUid: userId,
          numberOfQuestions: parseInt(contentQuestionParams.numberOfQuestions),
          difficulty: contentQuestionParams.difficulty,
          uploadResults: results
        })
      });
      
      const data = await response.json();
      console.log('Questions generation response:', data);
      
      if (response.ok) {
        setQuestionGenerationSuccess(true);
        
        setTimeout(() => {
          onClose();
        }, 3000);
      } else {
        console.error('Error details:', data);
        throw new Error(`Error generating questions: ${response.status}`);
      }
    } catch (error) {
      console.error('Error generating questions from content:', error);
      alert('Failed to generate questions. Please try again.');
    } finally {
      setIsGeneratingQuestions(false);
    }
  };

  if (!isVisible) return null;

  return (
    <div className="question-editor-overlay">
      <div className="question-editor-modal">
        <div className="modal-header">
          <h3>Edit Questions for: {examData.title}</h3>
          <button 
            className="close-button"
            onClick={onClose}
          >
            ✕
          </button>
        </div>
        
        <div className="question-source-selector">
          <div className="source-options">
            <button 
              className={`source-option ${questionSource === 'notes' ? 'active' : ''}`}
              onClick={() => setQuestionSource('notes')}
            >
              <i className="source-icon">📄</i>
              <span>Generate from Notes (AI)</span>
            </button>
            <button 
              className={`source-option ${questionSource === 'ai' ? 'active' : ''}`}
              onClick={() => setQuestionSource('ai')}
            >
              <i className="source-icon">🤖</i>
              <span>Generate from Topics (AI)</span>
            </button>
            <button 
              className={`source-option ${questionSource === 'manual' ? 'active' : ''}`}
              onClick={() => setQuestionSource('manual')}
            >
              <i className="source-icon">✏️</i>
              <span>Create Questions Manually</span>
            </button>
          </div>
        </div>
        
        {questionSource === 'notes' && (
          <form onSubmit={(e) => e.preventDefault()} className="question-form">
            {isUploading ? (
              <div className="uploading-overlay">
                <div className="circular-progress"></div>
                <p>Extracting your notes...</p>
              </div>
            ) : isGeneratingQuestions ? (
              <div className="uploading-overlay">
                <div className="circular-progress"></div>
                <p>Generating questions...</p>
              </div>
            ) : questionGenerationSuccess ? (
              <div className="success-message">
                <div className="success-icon">✅</div>
                <p>Questions successfully generated from your notes!</p>
                <p className="success-submessage">Redirecting to exam dashboard...</p>
              </div>
            ) : uploadSuccess ? (
              <div className="success-message">
                <div className="success-icon">✅</div>
                <p>Your notes are analyzed! Configure and generate questions from your notes.</p>
                
                <div className="form-group">
                  <label htmlFor="numberOfQuestions">Number of Questions</label>
                  <input
                    type="number"
                    id="numberOfQuestions"
                    name="numberOfQuestions"
                    min="1"
                    max="100"
                    value={contentQuestionParams.numberOfQuestions}
                    onChange={handleContentParamsChange}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="difficulty">Difficulty Level</label>
                  <select
                    id="difficulty"
                    name="difficulty"
                    value={contentQuestionParams.difficulty}
                    onChange={handleContentParamsChange}
                  >
                    <option value="easy">Easy</option>
                    <option value="medium">Medium</option>
                    <option value="hard">Hard</option>
                  </select>
                </div>
                
                <div className="form-actions">
                  <button type="button" className="btn btn-secondary" onClick={onClose}>
                    Cancel
                  </button>
                  <button type="button" className="btn btn-primary" onClick={handleGenerateQuestionsFromContent}>
                    Generate Questions
                  </button>
                </div>
              </div>
            ) : (
              <>
                <div className="form-group">
                  <label htmlFor="notes-file">Upload Notes Files</label>
                  <input
                    type="file"
                    id="notes-file"
                    name="notesFile"
                    multiple
                    onChange={handleFileUpload}
                    accept=".pdf,.doc,.docx,.txt,.png,.jpg,.jpeg"
                    required
                  />
                  <small>Supported formats: PDF, DOC, DOCX, TXT, PNG, JPG, JPEG</small>
                </div>
                
                <div className="form-actions">
                  <button type="button" className="btn btn-secondary" onClick={onClose}>
                    Cancel
                  </button>
                  <button type="button" className="btn btn-primary" onClick={() => {
                    const fileInput = document.getElementById('notes-file');
                    if (fileInput.files.length > 0) {
                      const formData = new FormData();
                      formData.append('examId', examData.examId);
                      for (let i = 0; i < fileInput.files.length; i++) {
                        formData.append('files', fileInput.files[i]);
                      }
                      uploadFiles(formData);
                    } else {
                      alert('Please select at least one file');
                    }
                  }}>
                    Upload Notes
                  </button>
                </div>
              </>
            )}
          </form>
        )}
        
        {questionSource === 'manual' && (
          <form onSubmit={handleManualQuestionSubmit} className="question-form">
            {manualQuestionSuccess ? (
              <div className="success-message">
                <div className="success-icon">✅</div>
                <p>Question added successfully!</p>
                <p className="success-submessage">You can add more questions or close the form.</p>
              </div>
            ) : (
              <>
                <div className="form-group">
                  <label htmlFor="question">Question Text</label>
                  <textarea
                    id="question"
                    name="question"
                    value={manualQuestion.question}
                    onChange={(e) => setManualQuestion({...manualQuestion, question: e.target.value})}
                    rows="3"
                    placeholder="Enter your question here"
                    required
                  ></textarea>
                </div>
                
                <div className="form-group">
                  <label htmlFor="optionA">Option A</label>
                  <input
                    type="text"
                    id="optionA"
                    name="optionA"
                    value={manualQuestion.optionA}
                    onChange={(e) => setManualQuestion({...manualQuestion, optionA: e.target.value})}
                    placeholder="First answer option"
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="optionB">Option B</label>
                  <input
                    type="text"
                    id="optionB"
                    name="optionB"
                    value={manualQuestion.optionB}
                    onChange={(e) => setManualQuestion({...manualQuestion, optionB: e.target.value})}
                    placeholder="Second answer option"
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="optionC">Option C (Optional)</label>
                  <input
                    type="text"
                    id="optionC"
                    name="optionC"
                    value={manualQuestion.optionC}
                    onChange={(e) => setManualQuestion({...manualQuestion, optionC: e.target.value})}
                    placeholder="Third answer option (optional)"
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="optionD">Option D (Optional)</label>
                  <input
                    type="text"
                    id="optionD"
                    name="optionD"
                    value={manualQuestion.optionD}
                    onChange={(e) => setManualQuestion({...manualQuestion, optionD: e.target.value})}
                    placeholder="Fourth answer option (optional)"
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="correctAns">Correct Answer</label>
                  <select
                    id="correctAns"
                    name="correctAns"
                    value={manualQuestion.correctAns}
                    onChange={(e) => setManualQuestion({...manualQuestion, correctAns: e.target.value})}
                    required
                  >
                    <option value="A">A</option>
                    <option value="B">B</option>
                    <option value="C">C</option>
                    <option value="D">D</option>
                  </select>
                </div>
                
                <div className="form-actions">
                  <button type="button" className="btn btn-secondary" onClick={onClose}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary">
                    Add Question
                  </button>
                </div>
              </>
            )}
          </form>
        )}
        
        {questionSource === 'ai' && (
          <form onSubmit={handleAiQuestionSubmit} className="question-form">
            {isGeneratingQuestions ? (
              <div className="uploading-overlay">
                <div className="circular-progress"></div>
                <p>Generating questions with AI...</p>
              </div>
            ) : questionGenerationSuccess ? (
              <div className="success-message">
                <div className="success-icon">✅</div>
                <p>Questions successfully generated with AI!</p>
                <p className="success-submessage">Redirecting to exam dashboard...</p>
              </div>
            ) : (
              <>
                <div className="form-group">
                  <label htmlFor="subject">Subject</label>
                  <input
                    type="text"
                    id="subject"
                    name="subject"
                    value={aiQuestionParams.subject}
                    onChange={handleAiParamsChange}
                    placeholder="e.g. Mathematics, Physics, Computer Science"
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="topic">Main Topic</label>
                  <input
                    type="text"
                    id="topic"
                    name="topic"
                    value={aiQuestionParams.topic}
                    onChange={handleAiParamsChange}
                    placeholder="e.g. Calculus, Mechanics, Data Structures"
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="topics">Specific Areas (comma separated)</label>
                  <textarea
                    id="topics"
                    name="topics"
                    value={aiQuestionParams.topics}
                    onChange={handleAiParamsChange}
                    placeholder="e.g. Arrays, Linked Lists, Trees, Graphs"
                    required
                    rows="3"
                  ></textarea>
                </div>
                
                <div className="form-group">
                  <label htmlFor="numberOfQuestions">Number of Questions</label>
                  <input
                    type="number"
                    id="numberOfQuestions"
                    name="numberOfQuestions"
                    min="1"
                    max="100"
                    value={aiQuestionParams.numberOfQuestions}
                    onChange={handleAiParamsChange}
                    required
                  />
                  <small>Default is set to the exam's total marks</small>
                </div>
                
                <div className="form-group">
                  <label htmlFor="level">Difficulty Level</label>
                  <select
                    id="level"
                    name="level"
                    value={aiQuestionParams.level}
                    onChange={handleAiParamsChange}
                  >
                    <option value="easy">Easy</option>
                    <option value="medium">Medium</option>
                    <option value="hard">Hard</option>
                  </select>
                </div>
                
                <div className="form-actions">
                  <button type="button" className="btn btn-secondary" onClick={onClose}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary">
                    Generate AI Questions
                  </button>
                </div>
              </>
            )}
          </form>
        )}
      </div>
    </div>
  );
}

export default QuestionGenerator;
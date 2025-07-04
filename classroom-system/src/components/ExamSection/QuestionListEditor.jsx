import { useState, useEffect } from 'react';
import './ExamSection.css';

function QuestionListEditor({ 
  isVisible, 
  examData, 
  onClose, 
  getUserId 
}) {
  const [examQuestions, setExamQuestions] = useState([]);
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [isLoadingQuestions, setIsLoadingQuestions] = useState(true);
  const [showExportDropdown, setShowExportDropdown] = useState(false);

  // Fetch questions when component mounts or examData changes
  useEffect(() => {
    if (examData && isVisible) {
      fetchQuestions();
    }
  }, [examData, isVisible]);

  const fetchQuestions = async () => {
    setIsLoadingQuestions(true);
    
    try {
      const response = await fetch(`http://localhost:8080/exam/${examData.examId}/questions`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        }
      });
      
      if (!response.ok) {
        throw new Error(`Error fetching questions: ${response.status}`);
      }
      
      const data = await response.json();
      console.log('Questions data:', data);
      
      if (data.status === 'success') {
        setExamQuestions(data.questions || []);
      } else {
        console.error('Invalid response format:', data);
      }
    } catch (error) {
      console.error('Error fetching questions:', error);
      alert('Failed to load questions. Please try again.');
    } finally {
      setIsLoadingQuestions(false);
    }
  };

  const handleEditQuestion = (question) => {
    setEditingQuestion({...question});
  };

  const handleCancelEditQuestion = () => {
    setEditingQuestion(null);
  };

  const handleQuestionChange = (e) => {
    const { name, value } = e.target;
    setEditingQuestion({
      ...editingQuestion,
      [name]: value
    });
  };

  const handleSaveQuestion = async () => {
    try {
      const response = await fetch(`http://localhost:8080/exam/question/${editingQuestion.questionUid}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify({
          question: editingQuestion.question,
          optionA: editingQuestion.optionA,
          optionB: editingQuestion.optionB,
          optionC: editingQuestion.optionC,
          optionD: editingQuestion.optionD,
          correctAns: editingQuestion.correctAns
        })
      });
      
      if (!response.ok) {
        throw new Error(`Error updating question: ${response.status}`);
      }
      
      // Update the question in the local state
      const updatedQuestions = examQuestions.map(q => 
        q.questionUid === editingQuestion.questionUid ? editingQuestion : q
      );
      setExamQuestions(updatedQuestions);
      setEditingQuestion(null);
      
      alert('Question updated successfully!');
    } catch (error) {
      console.error('Error updating question:', error);
      alert('Failed to update question. Please try again.');
    }
  };

  const handleDeleteQuestion = async (questionUid) => {
    // Define questionToDelete in the outer scope so it's accessible in catch
    const questionToDelete = examQuestions.find(q => q.questionUid === questionUid);
    try {
      // Start by visually removing the question immediately for better UX
      const updatedQuestions = examQuestions.filter(q => q.questionUid !== questionUid);
      setExamQuestions(updatedQuestions);
      
      // Then perform the actual deletion in the background
      const response = await fetch(`http://localhost:8080/exam/question/${questionUid}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': import.meta.env.VITE_API_KEY
        }
      });
      
      if (!response.ok) {
        throw new Error(`Error deleting question: ${response.status}`);
      }
      
      // No need for success alert - deletion is already visually confirmed
    } catch (error) {
      console.error('Error deleting question:', error);
      // If there was an error, restore the question in the UI
      setExamQuestions(prev => [...prev, questionToDelete].sort((a, b) => 
        examQuestions.findIndex(q => q.questionUid === a.questionUid) - 
        examQuestions.findIndex(q => q.questionUid === b.questionUid)
      ));
      // Show error in console instead of alert
    }
  };

  const handleExportToWord = () => {
    if (examQuestions.length === 0) {
      alert('No questions to export');
      return;
    }
    
    // Format as HTML with MS Word compatible tags
    const examTitle = examData.title;
    let htmlContent = `<!DOCTYPE html>
<html xmlns:o="urn:schemas-microsoft-com:office:office" 
      xmlns:w="urn:schemas-microsoft-com:office:word" 
      xmlns="http://www.w3.org/TR/REC-html40">
<head>
  <meta charset="utf-8">
  <title>${examTitle}</title>
  <!--[if gte mso 9]>
  <xml>
    <w:WordDocument>
      <w:View>Print</w:View>
      <w:Zoom>100</w:Zoom>
    </w:WordDocument>
  </xml>
  <![endif]-->
  <style>
    body { font-family: 'Calibri', sans-serif; }
    h1 { text-align: center; color: #2a5885; }
    .question { margin-bottom: 20px; }
    .option { margin-left: 20px; }
    .correct { font-weight: bold; color: #4CAF50; }
  </style>
</head>
<body>
  <h1>${examTitle}</h1>`;
    
    examQuestions.forEach((q, index) => {
      htmlContent += `<div class="question">
        <p><b>Question ${index + 1}:</b> ${q.question}</p>
        <p class="option ${q.correctAns === 'A' ? 'correct' : ''}">A) ${q.optionA}</p>
        <p class="option ${q.correctAns === 'B' ? 'correct' : ''}">B) ${q.optionB}</p>`;
      if (q.optionC) {
        htmlContent += `<p class="option ${q.correctAns === 'C' ? 'correct' : ''}">C) ${q.optionC}</p>`;
      }
      if (q.optionD) {
        htmlContent += `<p class="option ${q.correctAns === 'D' ? 'correct' : ''}">D) ${q.optionD}</p>`;
      }
      htmlContent += `<p><i>Correct Answer: ${q.correctAns}</i></p>
      </div>`;
    });
    
    htmlContent += `</body></html>`;

    // Create blob and download as HTML file with .doc extension
    const blob = new Blob([htmlContent], { type: 'application/msword' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${examTitle.replace(/\s+/g, '_')}_questions.doc`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    setShowExportDropdown(false);
  };

  const handleExportToExcel = () => {
    if (examQuestions.length === 0) {
      alert('No questions to export');
      return;
    }
    
    // Format as CSV for Excel
    const examTitle = examData.title;
    let csv = 'Question Number,Question,Option A,Option B,Option C,Option D,Correct Answer\n';
    examQuestions.forEach((q, index) => {
      csv += `${index + 1},"${q.question.replace(/"/g, '""')}","${q.optionA.replace(/"/g, '""')}","${q.optionB.replace(/"/g, '""')}",`;
      csv += `"${q.optionC ? q.optionC.replace(/"/g, '""') : ''}","${q.optionD ? q.optionD.replace(/"/g, '""') : ''}","${q.correctAns}"\n`;
    });

    // Create blob and download
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${examTitle.replace(/\s+/g, '_')}_questions.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    setShowExportDropdown(false);
  };

  const handleExportToPDF = () => {
    if (examQuestions.length === 0) {
      alert('No questions to export');
      return;
    }
    
    // Create a hidden iframe to generate the PDF content
    const iframe = document.createElement('iframe');
    iframe.style.display = 'none';
    document.body.appendChild(iframe);
    
    const examTitle = examData.title;
    let content = `<!DOCTYPE html>
<html>
<head>
  <title>${examTitle} Questions</title>
  <style>
    body { font-family: Arial, sans-serif; padding: 20px; }
    h1 { text-align: center; }
    .question { margin-bottom: 20px; }
    .correct { font-weight: bold; color: #4CAF50; }
    @media print {
      @page { size: auto; margin: 10mm; }
    }
  </style>
</head>
<body>
  <h1>${examTitle} - Questions</h1>`;
    
    examQuestions.forEach((q, index) => {
      content += `<div class="question">
        <p><strong>${index + 1}.</strong> ${q.question}</p>
        <p class="${q.correctAns === 'A' ? 'correct' : ''}">A) ${q.optionA}</p>
        <p class="${q.correctAns === 'B' ? 'correct' : ''}">B) ${q.optionB}</p>
        ${q.optionC ? `<p class="${q.correctAns === 'C' ? 'correct' : ''}">C) ${q.optionC}</p>` : ''}
        ${q.optionD ? `<p class="${q.correctAns === 'D' ? 'correct' : ''}">D) ${q.optionD}</p>` : ''}
      </div>`;
    });
    
    content += '</body></html>';
    
    // Write content to iframe
    iframe.contentWindow.document.open();
    iframe.contentWindow.document.write(content);
    iframe.contentWindow.document.close();
    
    // Use setTimeout to ensure the content is fully loaded
    setTimeout(() => {
      // Trigger the PDF download
      iframe.contentWindow.focus();
      iframe.contentWindow.print();
      
      // Clean up the iframe after printing dialog is closed
      setTimeout(() => {
        document.body.removeChild(iframe);
      }, 100);
    }, 250);
    
    setShowExportDropdown(false);
  };

  const handleImportQuestions = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    
    // Validate file type
    if (file.type !== 'text/csv' && !file.name.endsWith('.csv')) {
      alert('Please select a CSV file');
      return;
    }
    
    try {
      const reader = new FileReader();
      reader.onload = async (event) => {
        const csvData = event.target.result;
        const lines = csvData.split('\n');
        
        // Skip header row
        if (lines.length < 2) {
          alert('The CSV file is empty or invalid');
          return;
        }
        
        const userId = getUserId();
        if (!userId) {
          console.error('User ID not found in token');
          return;
        }
        
        // Process each row (skip the header row)
        const importedQuestions = [];
        const errors = [];
        
        for (let i = 1; i < lines.length; i++) {
          if (!lines[i].trim()) continue; // Skip empty lines
          
          // Parse CSV line, handling quoted values with commas
          const parseCSVLine = (line) => {
            const result = [];
            let insideQuotes = false;
            let currentValue = '';
            
            for (let char of line) {
              if (char === '"') {
                insideQuotes = !insideQuotes;
              } else if (char === ',' && !insideQuotes) {
                result.push(currentValue);
                currentValue = '';
              } else {
                currentValue += char;
              }
            }
            
            // Add the last value
            result.push(currentValue);
            return result;
          };
          
          const values = parseCSVLine(lines[i]);
          
          if (values.length < 6) {
            errors.push(`Line ${i+1}: Not enough columns`);
            continue;
          }
          
          // Column order: Question Number, Question, Option A, Option B, Option C, Option D, Correct Answer
          //const questionNumber = values[0];
          const question = values[1].replace(/""/g, '"'); // Replace double quotes with single quotes
          const optionA = values[2].replace(/""/g, '"');
          const optionB = values[3].replace(/""/g, '"');
          const optionC = values[4].replace(/""/g, '"') || null;
          const optionD = values[5].replace(/""/g, '"') || null;
          const correctAns = values[6] ? values[6].trim() : 'A';
          
          if (!question || !optionA || !optionB) {
            errors.push(`Line ${i+1}: Missing required fields`);
            continue;
          }
          
          try {
            const response = await fetch(`http://localhost:8080/exam/add-question`, {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'X-API-Key': import.meta.env.VITE_API_KEY
              },
              body: JSON.stringify({
                examUid: examData.examId,
                creatorUid: userId,
                question,
                optionA,
                optionB,
                optionC,
                optionD,
                correctAns
              })
            });
            
            if (!response.ok) {
              throw new Error(`Error importing question: ${response.status}`);
            }
            
            importedQuestions.push(question);
          } catch (error) {
            errors.push(`Line ${i+1}: ${error.message}`);
          }
        }
        
        // Show import results
        if (importedQuestions.length > 0) {
          alert(`Successfully imported ${importedQuestions.length} questions.`);
          // Refresh question list
          fetchQuestions();
        }
        
        if (errors.length > 0) {
          console.error('Import errors:', errors);
          alert(`${errors.length} errors occurred during import. See console for details.`);
        }
        
        // Clear the file input
        e.target.value = null;
      };
      
      reader.readAsText(file);
    } catch (error) {
      console.error('Error importing questions:', error);
      alert('Failed to import questions. Please check your file format and try again.');
    }
  };

  if (!isVisible) return null;

  return (
    <div className="question-editor-overlay">
      <div className="question-editor-modal question-list-modal">
        <div className="modal-header">
          <h3>Edit Questions for: {examData.title}</h3>
          <button 
            className="close-button"
            onClick={onClose}
          >
            ✕
          </button>
        </div>
        
        {isLoadingQuestions ? (
          <div className="loading-overlay">
            <div className="circular-progress"></div>
            <p>Loading questions...</p>
          </div>
        ) : examQuestions.length === 0 ? (
          <div className="no-questions-message">
            <div className="questions-list-header">
              <h4>No Questions</h4>
              
              <div className="question-actions-container">
                {/* Import Button */}
                <div className="import-container">
                  <label htmlFor="import-csv" className="import-button">
                    <i className="import-icon">📥</i> Import CSV
                  </label>
                  <input 
                    type="file"
                    id="import-csv"
                    accept=".csv"
                    onChange={handleImportQuestions}
                    style={{ display: 'none' }}
                  />
                </div>
              </div>
            </div>
            <p>No questions found for this exam. Please generate questions first or import from CSV.</p>
          </div>
        ) : (
          <div className="questions-container">
            {editingQuestion ? (
              <div className="question-edit-form">
                <h4>Edit Question</h4>
                <div className="form-group">
                  <label htmlFor="question">Question</label>
                  <textarea
                    id="question"
                    name="question"
                    value={editingQuestion.question}
                    onChange={handleQuestionChange}
                    rows="3"
                    required
                  ></textarea>
                </div>
                
                <div className="form-group">
                  <label htmlFor="optionA">Option A</label>
                  <input
                    type="text"
                    id="optionA"
                    name="optionA"
                    value={editingQuestion.optionA || ''}
                    onChange={handleQuestionChange}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="optionB">Option B</label>
                  <input
                    type="text"
                    id="optionB"
                    name="optionB"
                    value={editingQuestion.optionB || ''}
                    onChange={handleQuestionChange}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="optionC">Option C</label>
                  <input
                    type="text"
                    id="optionC"
                    name="optionC"
                    value={editingQuestion.optionC || ''}
                    onChange={handleQuestionChange}
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="optionD">Option D</label>
                  <input
                    type="text"
                    id="optionD"
                    name="optionD"
                    value={editingQuestion.optionD || ''}
                    onChange={handleQuestionChange}
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="correctAns">Correct Answer</label>
                  <select
                    id="correctAns"
                    name="correctAns"
                    value={editingQuestion.correctAns}
                    onChange={handleQuestionChange}
                    required
                  >
                    <option value="A">A</option>
                    <option value="B">B</option>
                    <option value="C">C</option>
                    <option value="D">D</option>
                  </select>
                </div>
                
                <div className="form-actions">
                  <button type="button" className="btn btn-secondary" onClick={handleCancelEditQuestion}>
                    Cancel
                  </button>
                  <button type="button" className="btn btn-primary" onClick={handleSaveQuestion}>
                    Save Question
                  </button>
                </div>
              </div>
            ) : (
              <>
                <div className="questions-list-header">
                  <h4>Total Questions: {examQuestions.length}</h4>
                  
                  <div className="question-actions-container">
                    {/* Export Dropdown */}
                    <div className="export-dropdown-container">
                      <button 
                        className="export-button"
                        onClick={() => setShowExportDropdown(!showExportDropdown)}
                      >
                        <i className="export-icon">📤</i> Export Questions
                      </button>
                      
                      {showExportDropdown && (
                        <div className="export-dropdown">
                          <button onClick={handleExportToWord}>
                            <i className="export-format-icon">📄</i> Export as Word
                          </button>
                          <button onClick={handleExportToExcel}>
                            <i className="export-format-icon">📊</i> Export as Excel
                          </button>
                          <button onClick={handleExportToPDF}>
                            <i className="export-format-icon">📑</i> Export as PDF
                          </button>
                        </div>
                      )}
                    </div>
                    
                    {/* Import Button */}
                    <div className="import-container">
                      <label htmlFor="import-csv" className="import-button">
                        <i className="import-icon">📥</i> Import CSV
                      </label>
                      <input 
                        type="file"
                        id="import-csv"
                        accept=".csv"
                        onChange={handleImportQuestions}
                        style={{ display: 'none' }}
                      />
                    </div>
                  </div>
                </div>
                
                <div className="questions-list">
                  {examQuestions.map((question, index) => (
                    <div key={question.questionUid} className="question-item">
                      <div className="question-content">
                        <span className="question-number">{index + 1}.</span>
                        <div className="question-text">
                          <p>{question.question}</p>
                          <div className="question-options">
                            <div className="option">
                              <span className={`option-label ${question.correctAns === 'A' ? 'correct' : ''}`}>A:</span> 
                              {question.optionA}
                            </div>
                            <div className="option">
                              <span className={`option-label ${question.correctAns === 'B' ? 'correct' : ''}`}>B:</span> 
                              {question.optionB}
                            </div>
                            {question.optionC && (
                              <div className="option">
                                <span className={`option-label ${question.correctAns === 'C' ? 'correct' : ''}`}>C:</span> 
                                {question.optionC}
                              </div>
                            )}
                            {question.optionD && (
                              <div className="option">
                                <span className={`option-label ${question.correctAns === 'D' ? 'correct' : ''}`}>D:</span> 
                                {question.optionD}
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                      <div className="question-actions">
                        <button 
                          className="edit-button"
                          onClick={() => handleEditQuestion(question)}
                        >
                          <i className="edit-icon">✏️</i> Edit
                        </button>
                        <button 
                          className="delete-button"
                          onClick={() => handleDeleteQuestion(question.questionUid)}
                        >
                          <i className="delete-icon">🗑️</i> Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default QuestionListEditor;
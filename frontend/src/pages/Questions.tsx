import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  RadioGroup,
  FormControlLabel,
  Radio,
  Checkbox,
  TextField,
  Button,
  LinearProgress,
  Alert,
  Chip,
  Grid,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  ImageList,
  ImageListItem,
  ImageListItemBar,
  CircularProgress
} from '@mui/material'
import {
  PhotoCamera,
  ArrowForward,
  ArrowBack,
  CheckCircle,
  Error as ErrorIcon
} from '@mui/icons-material'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { questionAPI, pricingAPI } from '../services/api'
import { useAuth } from '../hooks/useAuth'

interface Question {
  id: number
  text: string
  slug: string
  questionType: string
  required: boolean
  active: boolean
  sortOrder: number
  options: Option[]
}

interface Option {
  id: number
  text: string
  slug: string
  deductionValue: number
  deductionType: string
  imageUrl: string | null
  active: boolean
  sortOrder: number
}

interface Answer {
  questionId: number
  optionId?: number
  answerText?: string
  imageUrl?: string
}

const Questions: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const queryClient = useQueryClient()
  const { user } = useAuth()
  
  // Get model ID from previous step
  const modelId = location.state?.modelId
  const modelName = location.state?.modelName
  
  // State management
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0)
  const [answers, setAnswers] = useState<Answer[]>([])
  const [selectedImage, setSelectedImage] = useState<File | null>(null)
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const [showImageDialog, setShowImageDialog] = useState(false)
  const [loading, setLoading] = useState(false)
  
  // Fetch questions
  const { data: questions, isLoading, error } = useQuery(
    ['questions'],
    async () => {
      const response = await questionAPI.getQuestions()
      return response.data
    },
    {
      enabled: !!modelId,
      onSuccess: (data: Question[]) => {
        // Initialize answers array
        const initialAnswers = data.map((q: Question) => ({
          questionId: q.id,
          optionId: undefined,
          answerText: '',
          imageUrl: ''
        }))
        setAnswers(initialAnswers)
      }
    }
  )

  // Calculate current progress
  const progress = questions ? Math.round((currentQuestionIndex / questions.data.length) * 100) : 0

  // Handle answer changes
  const handleAnswerChange = (questionId: number, value: any, type: string) => {
    setAnswers((prev: Answer[]) => prev.map((answer: Answer) => 
      answer.questionId === questionId 
        ? { ...answer, [type]: value }
        : answer
    ))
  }

  // Handle image upload
  const handleImageUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      setSelectedImage(file)
      const reader = new FileReader()
      reader.onload = () => {
        setImagePreview(reader.result as string)
        setShowImageDialog(true)
      }
      reader.readAsDataURL(file)
    }
  }

  // Save image to answer
  const saveImage = () => {
    if (selectedImage && imagePreview) {
      // Here you would typically upload to server and get URL
      // For now, we'll use the base64 data
      handleAnswerChange(currentQuestionIndex, imagePreview, 'imageUrl')
      setShowImageDialog(false)
      setSelectedImage(null)
      setImagePreview(null)
    }
  }

  // Navigate to next question
  const handleNext = () => {
    if (currentQuestionIndex < (questions?.data.length || 0) - 1) {
      setCurrentQuestionIndex((prev: number) => prev + 1)
    } else {
      handleSubmit()
    }
  }

  // Navigate to previous question
  const handleBack = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex((prev: number) => prev - 1)
    }
  }

  // Submit answers and calculate price
  const handleSubmit = async () => {
    if (!modelId) {
      navigate('/device-selection')
      return
    }

    setLoading(true)
    
    try {
      // Calculate price with answers
      const response = await pricingAPI.calculatePrice(
        modelId,
        answers.filter((a: Answer) => a.optionId || a.answerText || a.imageUrl)
      )
      
      // Navigate to pricing page with results
      navigate('/pricing', {
        state: {
          modelId,
          modelName,
          answers,
          priceResult: response.data,
          questions: questions?.data
        }
      })
    } catch (error) {
      console.error('Error calculating price:', error)
      // Handle error - show alert or retry
    } finally {
      setLoading(false)
    }
  }

  // Check if current question is answered
  const isQuestionAnswered = () => {
    const currentQuestion = questions?.data[currentQuestionIndex]
    const currentAnswer = answers[currentQuestionIndex]
    
    if (!currentQuestion) return false
    
    if (currentQuestion.questionType === 'radio') {
      return currentAnswer.optionId !== undefined
    } else if (currentQuestion.questionType === 'checkbox') {
      return currentAnswer.optionId !== undefined
    } else if (currentQuestion.questionType === 'image') {
      return currentAnswer.imageUrl !== undefined && currentAnswer.imageUrl !== ''
    }
    
    return true
  }

  if (isLoading) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <LinearProgress />
        <Typography variant="h6" sx={{ mt: 2, textAlign: 'center' }}>
          Loading questions...
        </Typography>
      </Container>
    )
  }

  if (error || !questions) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">
          Failed to load questions. Please try again.
        </Alert>
        <Button 
          variant="contained" 
          onClick={() => navigate('/device-selection')}
          sx={{ mt: 2 }}
        >
          Go Back
        </Button>
      </Container>
    )
  }

  const currentQuestion = questions.data[currentQuestionIndex]
  const currentAnswer = answers[currentQuestionIndex]

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Device Condition Assessment
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" gutterBottom>
          {modelName}
        </Typography>
        <LinearProgress 
          variant="determinate" 
          value={progress} 
          sx={{ mt: 2, height: 8, borderRadius: 4 }}
        />
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Question {currentQuestionIndex + 1} of {questions.data.length}
        </Typography>
      </Box>

      {/* Question Card */}
      <Card elevation={3}>
        <CardContent>
          {/* Question Header */}
          <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
            <Chip 
              label={currentQuestion.questionType.toUpperCase()} 
              color="primary" 
              size="small"
            />
            {currentQuestion.required && (
              <Chip 
                label="Required" 
                color="error" 
                size="small"
                variant="outlined"
              />
            )}
            <Box sx={{ flex: 1, textAlign: 'right' }}>
              <Typography variant="caption" color="text.secondary">
                Question {currentQuestionIndex + 1}
              </Typography>
            </Box>
          </Box>

          {/* Question Text */}
          <Typography variant="h6" gutterBottom>
            {currentQuestion.text}
          </Typography>

          {/* Answer Options */}
          <Box sx={{ mt: 3 }}>
            {currentQuestion.questionType === 'radio' && (
              <RadioGroup
                value={currentAnswer.optionId || ''}
                onChange={(e) => handleAnswerChange(currentQuestion.id, parseInt(e.target.value), 'optionId')}
              >
                {currentQuestion.options.map((option) => (
                  <FormControlLabel
                    key={option.id}
                    value={option.id}
                    control={<Radio />}
                    label={
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                        <span>{option.text}</span>
                        {option.deductionValue > 0 && (
                          <Typography variant="caption" color="error">
                            -₹{option.deductionValue}
                          </Typography>
                        )}
                      </Box>
                    }
                  />
                ))}
              </RadioGroup>
            )}

            {currentQuestion.questionType === 'checkbox' && (
              <Box>
                {currentQuestion.options.map((option) => (
                  <FormControlLabel
                    key={option.id}
                    control={
                      <Checkbox
                        checked={currentAnswer.optionId === option.id}
                        onChange={(e) => handleAnswerChange(
                          currentQuestion.id, 
                          e.target.checked ? option.id : undefined, 
                          'optionId'
                        )}
                      />
                    }
                    label={
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                        <span>{option.text}</span>
                        {option.deductionValue > 0 && (
                          <Typography variant="caption" color="error">
                            -₹{option.deductionValue}
                          </Typography>
                        )}
                      </Box>
                    }
                  />
                ))}
              </Box>
            )}

            {currentQuestion.questionType === 'image' && (
              <Box>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Upload a clear photo showing the condition
                </Typography>
                
                {/* Current Image Preview */}
                {currentAnswer.imageUrl && (
                  <Box sx={{ mb: 2 }}>
                    <ImageList cols={1} rowHeight={200}>
                      <ImageListItem>
                        <img
                          src={currentAnswer.imageUrl}
                          alt="Uploaded condition"
                          style={{ width: '100%', height: '200px', objectFit: 'cover', borderRadius: 8 }}
                        />
                        <ImageListItemBar
                          title="Current Upload"
                          actionIcon={
                            <IconButton
                              sx={{ color: 'white' }}
                              onClick={() => handleAnswerChange(currentQuestion.id, '', 'imageUrl')}
                            >
                              Remove
                            </IconButton>
                          }
                        />
                      </ImageListItem>
                    </ImageList>
                  </Box>
                )}

                {/* Upload Button */}
                <Button
                  variant="outlined"
                  startIcon={<PhotoCamera />}
                  component="label"
                  fullWidth
                >
                  {currentAnswer.imageUrl ? 'Change Photo' : 'Upload Photo'}
                  <input
                    type="file"
                    hidden
                    accept="image/*"
                    onChange={handleImageUpload}
                  />
                </Button>
              </Box>
            )}
          </Box>
        </CardContent>
      </Card>

      {/* Navigation */}
      <Box sx={{ mt: 4, display: 'flex', justifyContent: 'space-between' }}>
        <Button
          variant="outlined"
          startIcon={<ArrowBack />}
          onClick={handleBack}
          disabled={currentQuestionIndex === 0}
        >
          Back
        </Button>

        <Button
          variant="contained"
          endIcon={<ArrowForward />}
          onClick={handleNext}
          disabled={!isQuestionAnswered()}
          sx={{ 
            backgroundColor: isQuestionAnswered() ? 'primary.main' : 'grey.300',
            color: isQuestionAnswered() ? 'white' : 'text.primary'
          }}
        >
          {currentQuestionIndex === questions.data.length - 1 ? 'Calculate Price' : 'Next'}
        </Button>
      </Box>

      {/* Loading Overlay */}
      {loading && (
        <Box
          sx={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 9999
          }}
        >
          <Box sx={{ textAlign: 'center', color: 'white' }}>
            <CircularProgress color="inherit" />
            <Typography variant="h6" sx={{ mt: 2 }}>
              Calculating your device value...
            </Typography>
          </Box>
        </Box>
      )}

      {/* Image Upload Dialog */}
      <Dialog open={showImageDialog} onClose={() => setShowImageDialog(false)}>
        <DialogTitle>Preview Image</DialogTitle>
        <DialogContent>
          {imagePreview && (
            <img
              src={imagePreview}
              alt="Preview"
              style={{ width: '100%', maxHeight: '400px', objectFit: 'contain' }}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowImageDialog(false)}>Cancel</Button>
          <Button onClick={saveImage} variant="contained">Use This Image</Button>
        </DialogActions>
      </Dialog>
    </Container>
  )
}

export default Questions
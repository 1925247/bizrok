import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Grid,
  Chip,
  Button,
  LinearProgress,
  Alert,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Divider,
  Paper,
  Avatar,
  IconButton
} from '@mui/material'
import {
  ArrowBack,
  ArrowForward,
  TrendingUp,
  TrendingDown,
  Info,
  CheckCircle,
  Error as ErrorIcon
} from '@mui/icons-material'
import { useMutation } from '@tanstack/react-query'
import { orderAPI } from '../services/api'
import { useAuth } from '../hooks/useAuth'

interface PriceResult {
  success: boolean
  finalPrice: number
  totalDeductions: number
  groupDeductionsTotal: number
  breakdown: {
    basePrice: number
    displayDeductions: number
    batteryDeductions: number
    totalDeductions: number
    finalPrice: number
  }
}

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

const Pricing: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { user } = useAuth()
  
  // Get data from previous step
  const { modelId, modelName, answers, priceResult, questions } = location.state || {}
  
  const [showBreakdown, setShowBreakdown] = useState(false)
  
  // Calculate deductions by group
  const calculateDeductionsByGroup = () => {
    if (!questions || !answers) return []
    
    const deductions: Array<{ group: string; amount: number; details: string[] }> = []
    
    // Group questions by their group
    const groupedQuestions = questions.reduce((acc: any, question: any) => {
      const groupId = question.groupId || 'unknown'
      if (!acc[groupId]) {
        acc[groupId] = {
          group: question.groupName || question.group?.name || 'Unknown',
          questions: []
        }
      }
      acc[groupId].questions.push(question)
      return acc
    }, {})
    
    // Calculate deductions for each group
    Object.values(groupedQuestions).forEach((groupData: any) => {
      let groupDeduction = 0
      const details: string[] = []
      
      groupData.questions.forEach((question: any) => {
        const answer = answers.find((a: Answer) => a.questionId === question.id)
        if (answer && answer.optionId) {
          const option = question.options.find((o: any) => o.id === answer.optionId)
          if (option && option.deductionValue > 0) {
            groupDeduction += option.deductionValue
            details.push(`${question.text}: -₹${option.deductionValue}`)
          }
        }
      })
      
      if (groupDeduction > 0) {
        deductions.push({
          group: groupData.group,
          amount: groupDeduction,
          details
        })
      }
    })
    
    return deductions
  }
  
  const deductionsByGroup = calculateDeductionsByGroup()
  
  // Navigate to order summary
  const handleContinue = () => {
    navigate('/order-summary', {
      state: {
        modelId,
        modelName,
        answers,
        priceResult,
        questions
      }
    })
  }
  
  // Go back to questions
  const handleBack = () => {
    navigate('/questions', {
      state: {
        modelId,
        modelName
      }
    })
  }
  
  if (!modelId || !priceResult) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">
          Invalid pricing data. Please go back and complete the questions.
        </Alert>
        <Button 
          variant="contained" 
          onClick={handleBack}
          sx={{ mt: 2 }}
        >
          Go Back to Questions
        </Button>
      </Container>
    )
  }
  
  const basePrice = priceResult.breakdown?.basePrice || 0
  const finalPrice = priceResult.finalPrice || 0
  const totalDeductions = priceResult.totalDeductions || 0
  const savingsPercentage = basePrice > 0 ? Math.round((totalDeductions / basePrice) * 100) : 0
  
  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Price Calculation
        </Typography>
        <Typography variant="h6" color="text.secondary" gutterBottom>
          {modelName}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Based on your device condition assessment
        </Typography>
      </Box>

      {/* Price Summary Card */}
      <Card elevation={3} sx={{ mb: 4 }}>
        <CardContent>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h6" color="text.secondary" gutterBottom>
                  Base Price
                </Typography>
                <Typography variant="h4" color="text.primary">
                  ₹{basePrice.toLocaleString()}
                </Typography>
              </Box>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h6" color="text.secondary" gutterBottom>
                  Your Price
                </Typography>
                <Typography variant="h3" color="success.main" sx={{ fontWeight: 'bold' }}>
                  ₹{finalPrice.toLocaleString()}
                </Typography>
                {totalDeductions > 0 && (
                  <Typography variant="body2" color="error.main">
                    Deductions: -₹{totalDeductions.toLocaleString()}
                  </Typography>
                )}
              </Box>
            </Grid>
          </Grid>
          
          {/* Progress Bar */}
          {basePrice > 0 && (
            <Box sx={{ mt: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="caption" color="text.secondary">
                  Value Retention
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {Math.round((finalPrice / basePrice) * 100)}%
                </Typography>
              </Box>
              <LinearProgress 
                variant="determinate" 
                value={(finalPrice / basePrice) * 100} 
                sx={{ height: 10, borderRadius: 5 }}
                color="success"
              />
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Deductions Breakdown */}
      <Card elevation={3} sx={{ mb: 4 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">
              Deductions Breakdown
            </Typography>
            <Button
              variant="outlined"
              size="small"
              onClick={() => setShowBreakdown(!showBreakdown)}
              endIcon={showBreakdown ? <TrendingUp /> : <TrendingDown />}
            >
              {showBreakdown ? 'Hide' : 'Show'} Details
            </Button>
          </Box>
          
          {deductionsByGroup.length > 0 ? (
            deductionsByGroup.map((group, index) => (
              <Box key={index} sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                  <Typography variant="subtitle2" color="text.secondary">
                    {group.group}
                  </Typography>
                  <Typography variant="subtitle2" color="error.main">
                    -₹{group.amount.toLocaleString()}
                  </Typography>
                </Box>
                {showBreakdown && group.details.length > 0 && (
                  <List dense>
                    {group.details.map((detail, idx) => (
                      <ListItem key={idx}>
                        <ListItemText primary={detail} />
                      </ListItem>
                    ))}
                  </List>
                )}
                {index < deductionsByGroup.length - 1 && <Divider />}
              </Box>
            ))
          ) : (
            <Box sx={{ textAlign: 'center', py: 3 }}>
              <CheckCircle color="success" sx={{ fontSize: 40, mb: 1 }} />
              <Typography variant="body2" color="success.main">
                No deductions applied! Your device is in excellent condition.
              </Typography>
            </Box>
          )}
          
          {totalDeductions > 0 && (
            <Box sx={{ mt: 2, p: 2, backgroundColor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="body2" color="text.secondary">
                Total Deductions: ₹{totalDeductions.toLocaleString()}
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Value Information */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Avatar sx={{ bgcolor: 'success.main', mb: 2, mx: 'auto' }}>
              <TrendingUp />
            </Avatar>
            <Typography variant="h6">Excellent Value</Typography>
            <Typography variant="body2" color="text.secondary">
              You're getting a fair price based on current market rates
            </Typography>
          </Paper>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Avatar sx={{ bgcolor: 'primary.main', mb: 2, mx: 'auto' }}>
              <Info />
            </Avatar>
            <Typography variant="h6">Market Rate</Typography>
            <Typography variant="body2" color="text.secondary">
              Price calculated using our dynamic pricing algorithm
            </Typography>
          </Paper>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Avatar sx={{ bgcolor: 'warning.main', mb: 2, mx: 'auto' }}>
              <CheckCircle />
            </Avatar>
            <Typography variant="h6">Instant Payment</Typography>
            <Typography variant="body2" color="text.secondary">
              Get paid immediately after device verification
            </Typography>
          </Paper>
        </Grid>
      </Grid>

      {/* Action Buttons */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
        <Button
          variant="outlined"
          startIcon={<ArrowBack />}
          onClick={handleBack}
          sx={{ flex: 1 }}
        >
          Back to Questions
        </Button>

        <Button
          variant="contained"
          endIcon={<ArrowForward />}
          onClick={handleContinue}
          size="large"
          sx={{ 
            flex: 2,
            backgroundColor: 'success.main',
            '&:hover': {
              backgroundColor: 'success.dark'
            }
          }}
        >
          Continue to Order Summary
        </Button>
      </Box>

      {/* Footer Information */}
      <Box sx={{ mt: 4, p: 3, backgroundColor: 'grey.50', borderRadius: 2 }}>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          <Info fontSize="small" sx={{ mr: 1, verticalAlign: 'middle' }} />
          Price Guarantee
        </Typography>
        <Typography variant="body2" color="text.secondary">
          This price is guaranteed for 24 hours. Our field executive will verify your device 
          condition during pickup and the final price may be adjusted based on actual condition.
        </Typography>
      </Box>
    </Container>
  )
}

export default Pricing
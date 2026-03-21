import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Grid,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Divider,
  Alert,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Paper
} from '@mui/material'
import {
  ArrowBack,
  ArrowForward,
  CheckCircle,
  LocationOn,
  AccountCircle,
  Payment,
  CalendarToday
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

const OrderSummary: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { user } = useAuth()
  
  // Get data from previous step
  const { modelId, modelName, answers, priceResult, questions } = location.state || {}
  
  // Form state
  const [pickupAddress, setPickupAddress] = useState('')
  const [pickupPincode, setPickupPincode] = useState('')
  const [pickupDate, setPickupDate] = useState('')
  const [pickupTime, setPickupTime] = useState('')
  const [bankAccountNumber, setBankAccountNumber] = useState('')
  const [bankIfsc, setBankIfsc] = useState('')
  const [bankAccountName, setBankAccountName] = useState('')
  const [activeStep, setActiveStep] = useState(0)
  
  // Validation state
  const [errors, setErrors] = useState<any>({})
  
  // Create order mutation
  const createOrderMutation = useMutation(
    (orderData: any) => orderAPI.createOrder(orderData),
    {
      onSuccess: (response: any) => {
        navigate('/kyc-verification', {
          state: {
            orderId: response.data.id,
            orderNumber: response.data.orderNumber,
            finalPrice: response.data.finalPrice
          }
        })
      },
      onError: (error: any) => {
        console.error('Order creation failed:', error)
        setErrors({ general: 'Failed to create order. Please try again.' })
      }
    }
  )
  
  // Calculate selected answers details
  const getSelectedAnswersDetails = () => {
    if (!questions || !answers) return []
    
    return questions.map((question: Question) => {
      const answer = answers.find((a: Answer) => a.questionId === question.id)
      let answerText = 'Not answered'
      let deduction = 0
      
      if (answer) {
        if (question.questionType === 'radio' || question.questionType === 'checkbox') {
          if (answer.optionId) {
            const option = question.options.find((o: Option) => o.id === answer.optionId)
            if (option) {
              answerText = option.text
              deduction = option.deductionValue || 0
            }
          }
        } else if (question.questionType === 'image') {
          answerText = answer.imageUrl ? 'Image uploaded' : 'No image'
        }
      }
      
      return {
        question: question.text,
        answer: answerText,
        deduction: deduction
      }
    }).filter(item => item.deduction > 0 || item.answer !== 'Not answered')
  }
  
  const selectedAnswers = getSelectedAnswersDetails()
  
  // Validation functions
  const validateStep = (step: number) => {
    const newErrors: any = {}
    
    if (step === 0) {
      // Pickup details validation
      if (!pickupAddress.trim()) newErrors.pickupAddress = 'Pickup address is required'
      if (!pickupPincode.trim()) newErrors.pickupPincode = 'Pincode is required'
      if (!pickupDate) newErrors.pickupDate = 'Pickup date is required'
      if (!pickupTime) newErrors.pickupTime = 'Pickup time is required'
    } else if (step === 1) {
      // Bank details validation
      if (!bankAccountNumber.trim()) newErrors.bankAccountNumber = 'Account number is required'
      else if (!/^\d{9,18}$/.test(bankAccountNumber)) newErrors.bankAccountNumber = 'Invalid account number'
      
      if (!bankIfsc.trim()) newErrors.bankIfsc = 'IFSC code is required'
      else if (!/^[A-Z]{4}0[A-Z0-9]{6}$/.test(bankIfsc.toUpperCase())) newErrors.bankIfsc = 'Invalid IFSC code'
      
      if (!bankAccountName.trim()) newErrors.bankAccountName = 'Account holder name is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }
  
  // Handle next step
  const handleNext = () => {
    if (validateStep(activeStep)) {
      setActiveStep((prev) => prev + 1)
    }
  }
  
  // Handle back step
  const handleBack = () => {
    setActiveStep((prev) => prev - 1)
  }
  
  // Submit order
  const handleSubmitOrder = () => {
    if (!validateStep(0) || !validateStep(1)) {
      return
    }
    
    const orderData = {
      modelId,
      pickupAddress,
      pickupPincode,
      pickupDate,
      pickupTime,
      bankAccountNumber,
      bankIfsc,
      bankAccountName,
      answers: answers.filter((a: Answer) => a.optionId || a.answerText || a.imageUrl)
    }
    
    createOrderMutation.mutate(orderData)
  }
  
  if (!modelId || !priceResult) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">
          Invalid order data. Please go back and complete the previous steps.
        </Alert>
        <Button 
          variant="contained" 
          onClick={() => navigate('/pricing')}
          sx={{ mt: 2 }}
        >
          Go Back to Pricing
        </Button>
      </Container>
    )
  }
  
  const finalPrice = priceResult.finalPrice || 0
  
  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Order Summary
        </Typography>
        <Typography variant="h6" color="text.secondary" gutterBottom>
          {modelName}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Final Price: ₹{finalPrice.toLocaleString()}
        </Typography>
      </Box>

      {/* Stepper */}
      <Box sx={{ mb: 4 }}>
        <Stepper activeStep={activeStep} orientation="vertical">
          {/* Step 1: Pickup Details */}
          <Step>
            <StepLabel icon={<LocationOn />}>
              Pickup Details
            </StepLabel>
            <StepContent>
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Pickup Address"
                    value={pickupAddress}
                    onChange={(e) => setPickupAddress(e.target.value)}
                    error={!!errors.pickupAddress}
                    helperText={errors.pickupAddress}
                    multiline
                    rows={3}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Pincode"
                    value={pickupPincode}
                    onChange={(e) => setPickupPincode(e.target.value)}
                    error={!!errors.pickupPincode}
                    helperText={errors.pickupPincode}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="City"
                    value="New Delhi" // Could be auto-filled based on pincode
                    disabled
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Pickup Date"
                    type="date"
                    value={pickupDate}
                    onChange={(e) => setPickupDate(e.target.value)}
                    error={!!errors.pickupDate}
                    helperText={errors.pickupDate}
                    InputLabelProps={{
                      shrink: true,
                    }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth error={!!errors.pickupTime}>
                    <InputLabel>Preferred Time Slot</InputLabel>
                    <Select
                      value={pickupTime}
                      label="Preferred Time Slot"
                      onChange={(e) => setPickupTime(e.target.value)}
                    >
                      <MenuItem value="10:00 AM - 12:00 PM">10:00 AM - 12:00 PM</MenuItem>
                      <MenuItem value="12:00 PM - 2:00 PM">12:00 PM - 2:00 PM</MenuItem>
                      <MenuItem value="2:00 PM - 4:00 PM">2:00 PM - 4:00 PM</MenuItem>
                      <MenuItem value="4:00 PM - 6:00 PM">4:00 PM - 6:00 PM</MenuItem>
                    </Select>
                    {errors.pickupTime && (
                      <Typography variant="caption" color="error">
                        {errors.pickupTime}
                      </Typography>
                    )}
                  </FormControl>
                </Grid>
              </Grid>
              
              <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                <Button disabled>Back</Button>
                <Button variant="contained" onClick={handleNext}>
                  Continue to Bank Details
                </Button>
              </Box>
            </StepContent>
          </Step>

          {/* Step 2: Bank Details */}
          <Step>
            <StepLabel icon={<Payment />}>
              Bank Details
            </StepLabel>
            <StepContent>
              <Grid container spacing={3}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Bank Account Number"
                    value={bankAccountNumber}
                    onChange={(e) => setBankAccountNumber(e.target.value)}
                    error={!!errors.bankAccountNumber}
                    helperText={errors.bankAccountNumber}
                    placeholder="Enter your account number"
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Confirm Account Number"
                    value={bankAccountNumber}
                    onChange={(e) => setBankAccountNumber(e.target.value)}
                    error={!!errors.bankAccountNumber}
                    helperText="Please ensure account number matches"
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="IFSC Code"
                    value={bankIfsc}
                    onChange={(e) => setBankIfsc(e.target.value)}
                    error={!!errors.bankIfsc}
                    helperText={errors.bankIfsc}
                    placeholder="e.g., SBIN0001234"
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Account Holder Name"
                    value={bankAccountName}
                    onChange={(e) => setBankAccountName(e.target.value)}
                    error={!!errors.bankAccountName}
                    helperText={errors.bankAccountName}
                    placeholder="As per bank records"
                  />
                </Grid>
              </Grid>
              
              <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                <Button onClick={handleBack}>Back</Button>
                <Button variant="contained" onClick={handleSubmitOrder} disabled={createOrderMutation.isPending}>
                  {createOrderMutation.isPending ? 'Creating Order...' : 'Submit Order'}
                </Button>
              </Box>
            </StepContent>
          </Step>
        </Stepper>
      </Box>

      {/* Order Summary Card */}
      <Card elevation={3} sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Order Summary
          </Typography>
          
          <List>
            <ListItem>
              <ListItemText primary="Device" secondary={modelName} />
              <ListItemSecondaryAction>
                <Typography variant="body2" color="text.secondary">
                  Base Price: ₹{priceResult.breakdown?.basePrice?.toLocaleString() || 'N/A'}
                </Typography>
              </ListItemSecondaryAction>
            </ListItem>
            
            <Divider />
            
            <ListItem>
              <ListItemText primary="Condition Assessment" />
              <ListItemSecondaryAction>
                <Chip 
                  label={`${selectedAnswers.length} questions answered`}
                  color="primary"
                  size="small"
                />
              </ListItemSecondaryAction>
            </ListItem>
            
            {selectedAnswers.map((item, index) => (
              <ListItem key={index} dense>
                <ListItemText 
                  primary={item.question}
                  secondary={item.answer}
                />
                {item.deduction > 0 && (
                  <ListItemSecondaryAction>
                    <Typography variant="body2" color="error">
                      -₹{item.deduction}
                    </Typography>
                  </ListItemSecondaryAction>
                )}
              </ListItem>
            ))}
            
            <Divider />
            
            <ListItem>
              <ListItemText primary="Total Deductions" />
              <ListItemSecondaryAction>
                <Typography variant="body2" color="error">
                  -₹{priceResult.totalDeductions?.toLocaleString() || '0'}
                </Typography>
              </ListItemSecondaryAction>
            </ListItem>
            
            <Divider />
            
            <ListItem>
              <ListItemText 
                primary="Final Price"
                secondary="You will receive this amount after device verification"
              />
              <ListItemSecondaryAction>
                <Typography variant="h6" color="success.main">
                  ₹{finalPrice.toLocaleString()}
                </Typography>
              </ListItemSecondaryAction>
            </ListItem>
          </List>
        </CardContent>
      </Card>

      {/* Important Information */}
      <Box sx={{ mb: 4, p: 3, backgroundColor: 'grey.50', borderRadius: 2 }}>
        <Typography variant="h6" gutterBottom>
          Important Information
        </Typography>
        <List>
          <ListItem>
            <ListItemText 
              primary="Device Verification"
              secondary="Our field executive will verify your device condition during pickup"
            />
          </ListItem>
          <ListItem>
            <ListItemText 
              primary="Payment"
              secondary="Payment will be processed within 24 hours after successful verification"
            />
          </ListItem>
          <ListItem>
            <ListItemText 
              primary="Cancellation"
              secondary="You can cancel your order before device pickup"
            />
          </ListItem>
        </List>
      </Box>

      {/* Error Display */}
      {errors.general && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {errors.general}
        </Alert>
      )}
    </Container>
  )
}

export default OrderSummary
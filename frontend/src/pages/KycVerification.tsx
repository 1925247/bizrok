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
  Alert,
  LinearProgress,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material'
import {
  PhotoCamera,
  CheckCircle,
  Error as ErrorIcon,
  Upload,
  Person,
  DocumentScanner
} from '@mui/icons-material'
import { useMutation, useQuery } from '@tanstack/react-query'
import { orderAPI } from '../services/api'
import { useAuth } from '../hooks/useAuth'

interface KycStatus {
  isKycVerified: boolean
  latestDocumentStatus: string | null
  verificationNotes: string | null
  submittedAt: string | null
  verifiedAt: string | null
}

const KycVerification: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { user } = useAuth()
  
  // Get data from previous step
  const { orderId, orderNumber, finalPrice } = location.state || {}
  
  // Form state
  const [documentImage, setDocumentImage] = useState<File | null>(null)
  const [selfieImage, setSelfieImage] = useState<File | null>(null)
  const [documentPreview, setDocumentPreview] = useState<string | null>(null)
  const [selfiePreview, setSelfiePreview] = useState<string | null>(null)
  const [activeStep, setActiveStep] = useState(0)
  const [showDocumentDialog, setShowDocumentDialog] = useState(false)
  const [showSelfieDialog, setShowSelfieDialog] = useState(false)
  
  // KYC status
  const [kycStatus, setKycStatus] = useState<KycStatus | null>(null)
  
  // KYC submission mutation
  const submitKycMutation = useMutation(
    (formData: FormData) => orderAPI.submitKyc(formData),
    {
      onSuccess: (response) => {
        console.log('KYC submitted successfully:', response)
        setActiveStep(2) // Move to completion step
        // Fetch updated KYC status
        kycStatusQuery.refetch()
      },
      onError: (error) => {
        console.error('KYC submission failed:', error)
        // Handle error
      }
    }
  )
  
  // Get KYC status query
  const kycStatusQuery = useQuery(
    ['kyc-status', user?.email],
    () => orderAPI.getKycStatus(),
    {
      enabled: !!user?.email,
      onSuccess: (data) => {
        setKycStatus(data.data)
      }
    }
  )
  
  // Handle document image upload
  const handleDocumentUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      setDocumentImage(file)
      const reader = new FileReader()
      reader.onload = () => {
        setDocumentPreview(reader.result as string)
        setShowDocumentDialog(true)
      }
      reader.readAsDataURL(file)
    }
  }
  
  // Handle selfie image upload
  const handleSelfieUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      setSelfieImage(file)
      const reader = new FileReader()
      reader.onload = () => {
        setSelfiePreview(reader.result as string)
        setShowSelfieDialog(true)
      }
      reader.readAsDataURL(file)
    }
  }
  
  // Save document image
  const saveDocumentImage = () => {
    setShowDocumentDialog(false)
    // Image is already set in state
  }
  
  // Save selfie image
  const saveSelfieImage = () => {
    setShowSelfieDialog(false)
    // Image is already set in state
  }
  
  // Submit KYC
  const handleSubmitKyc = () => {
    if (!documentImage || !selfieImage) {
      alert('Please upload both document and selfie images')
      return
    }
    
    const formData = new FormData()
    formData.append('documentImage', documentImage)
    formData.append('selfieImage', selfieImage)
    
    submitKycMutation.mutate(formData)
  }
  
  // Continue to order confirmation
  const handleContinue = () => {
    navigate('/order-confirmation', {
      state: {
        orderId,
        orderNumber,
        finalPrice
      }
    })
  }
  
  if (!orderId || !orderNumber) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">
          Invalid order data. Please go back and complete the previous steps.
        </Alert>
        <Button 
          variant="contained" 
          onClick={() => navigate('/order-summary')}
          sx={{ mt: 2 }}
        >
          Go Back to Order Summary
        </Button>
      </Container>
    )
  }
  
  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <Typography variant="h3" component="h1" gutterBottom>
          KYC Verification
        </Typography>
        <Typography variant="h6" color="text.secondary" gutterBottom>
          Order #{orderNumber}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Final Price: ₹{finalPrice?.toLocaleString()}
        </Typography>
      </Box>

      {/* Current KYC Status */}
      {kycStatus && (
        <Box sx={{ mb: 4 }}>
          <Alert 
            severity={kycStatus.isKycVerified ? "success" : "info"}
            sx={{ mb: 2 }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              {kycStatus.isKycVerified ? (
                <CheckCircle color="success" />
              ) : (
                <ErrorIcon color="error" />
              )}
              <Box>
                <Typography variant="subtitle2">
                  KYC Status: {kycStatus.isKycVerified ? 'Verified' : 'Pending'}
                </Typography>
                {kycStatus.verificationNotes && (
                  <Typography variant="body2">
                    {kycStatus.verificationNotes}
                  </Typography>
                )}
              </Box>
            </Box>
          </Alert>
        </Box>
      )}

      {/* Stepper */}
      <Box sx={{ mb: 4 }}>
        <Stepper activeStep={activeStep} orientation="vertical">
          {/* Step 1: Upload Documents */}
          <Step>
            <StepLabel icon={<DocumentScanner />}>
              Upload Identity Document
            </StepLabel>
            <StepContent>
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Upload a clear photo of your identity document (Aadhaar, PAN, or Driving License)
                  </Typography>
                </Grid>
                
                {/* Document Upload */}
                <Grid item xs={12} md={6}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                        <DocumentScanner color="primary" />
                        <Typography variant="h6">Identity Document</Typography>
                      </Box>
                      
                      {documentPreview ? (
                        <Box sx={{ mb: 2 }}>
                          <img
                            src={documentPreview}
                            alt="Document preview"
                            style={{ width: '100%', borderRadius: 8 }}
                          />
                        </Box>
                      ) : (
                        <Box
                          sx={{
                            border: '2px dashed grey',
                            borderRadius: 2,
                            p: 3,
                            textAlign: 'center',
                            mb: 2
                          }}
                        >
                          <Upload sx={{ fontSize: 40, color: 'text.secondary', mb: 1 }} />
                          <Typography variant="body2" color="text.secondary">
                            No document uploaded
                          </Typography>
                        </Box>
                      )}
                      
                      <Button
                        variant="contained"
                        startIcon={<PhotoCamera />}
                        component="label"
                        fullWidth
                      >
                        {documentPreview ? 'Change Document' : 'Upload Document'}
                        <input
                          type="file"
                          hidden
                          accept="image/*"
                          onChange={handleDocumentUpload}
                        />
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
                
                {/* Selfie Upload */}
                <Grid item xs={12} md={6}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                        <Person color="primary" />
                        <Typography variant="h6">Selfie</Typography>
                      </Box>
                      
                      {selfiePreview ? (
                        <Box sx={{ mb: 2 }}>
                          <img
                            src={selfiePreview}
                            alt="Selfie preview"
                            style={{ width: '100%', borderRadius: 8 }}
                          />
                        </Box>
                      ) : (
                        <Box
                          sx={{
                            border: '2px dashed grey',
                            borderRadius: 2,
                            p: 3,
                            textAlign: 'center',
                            mb: 2
                          }}
                        >
                          <PhotoCamera sx={{ fontSize: 40, color: 'text.secondary', mb: 1 }} />
                          <Typography variant="body2" color="text.secondary">
                            No selfie uploaded
                          </Typography>
                        </Box>
                      )}
                      
                      <Button
                        variant="contained"
                        startIcon={<PhotoCamera />}
                        component="label"
                        fullWidth
                      >
                        {selfiePreview ? 'Change Selfie' : 'Upload Selfie'}
                        <input
                          type="file"
                          hidden
                          accept="image/*"
                          onChange={handleSelfieUpload}
                        />
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
                
                {/* Guidelines */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                      Upload Guidelines
                    </Typography>
                    <List>
                      <ListItem>
                        <ListItemIcon>
                          <CheckCircle color="success" />
                        </ListItemIcon>
                        <ListItemText
                          primary="Clear and legible"
                          secondary="Make sure all text is readable"
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemIcon>
                          <CheckCircle color="success" />
                        </ListItemIcon>
                        <ListItemText
                          primary="No glare or shadows"
                          secondary="Avoid reflections on the document"
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemIcon>
                          <CheckCircle color="success" />
                        </ListItemIcon>
                        <ListItemText
                          primary="Full document visible"
                          secondary="No cropped edges"
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemIcon>
                          <ErrorIcon color="error" />
                        </ListItemIcon>
                        <ListItemText
                          primary="No filters or edits"
                          secondary="Use original, unedited photos"
                        />
                      </ListItem>
                    </List>
                  </Paper>
                </Grid>
              </Grid>
              
              <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                <Button disabled>Back</Button>
                <Button 
                  variant="contained" 
                  onClick={() => setActiveStep(1)}
                  disabled={!documentImage || !selfieImage}
                >
                  Continue to Review
                </Button>
              </Box>
            </StepContent>
          </Step>

          {/* Step 2: Review Documents */}
          <Step>
            <StepLabel icon={<CheckCircle />}>
              Review Documents
            </StepLabel>
            <StepContent>
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Please review your uploaded documents before submission
                  </Typography>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>Identity Document</Typography>
                      {documentPreview && (
                        <img
                          src={documentPreview}
                          alt="Document review"
                          style={{ width: '100%', borderRadius: 8 }}
                        />
                      )}
                    </CardContent>
                  </Card>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>Selfie</Typography>
                      {selfiePreview && (
                        <img
                          src={selfiePreview}
                          alt="Selfie review"
                          style={{ width: '100%', borderRadius: 8 }}
                        />
                      )}
                    </CardContent>
                  </Card>
                </Grid>
                
                <Grid item xs={12}>
                  <Alert severity="info">
                    <Typography variant="body2">
                      By submitting these documents, you confirm they are genuine and belong to you.
                      Our system will automatically verify the documents and perform face matching.
                    </Typography>
                  </Alert>
                </Grid>
              </Grid>
              
              <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
                <Button onClick={() => setActiveStep(0)}>Back</Button>
                <Button 
                  variant="contained" 
                  onClick={handleSubmitKyc}
                  disabled={submitKycMutation.isPending}
                >
                  {submitKycMutation.isPending ? 'Submitting...' : 'Submit for Verification'}
                </Button>
              </Box>
            </StepContent>
          </Step>

          {/* Step 3: Submission Complete */}
          <Step>
            <StepLabel icon={<CheckCircle />}>
              Verification Submitted
            </StepLabel>
            <StepContent>
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <CheckCircle color="success" sx={{ fontSize: 60, mb: 2 }} />
                <Typography variant="h5" gutterBottom>
                  KYC Verification Submitted
                </Typography>
                <Typography variant="body1" color="text.secondary" gutterBottom>
                  Our team will review your documents and verify your identity.
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  You will receive a notification once verification is complete.
                </Typography>
              </Box>
              
              <Box sx={{ mt: 3, display: 'flex', justifyContent: 'space-between' }}>
                <Button onClick={() => setActiveStep(1)}>Back</Button>
                <Button 
                  variant="contained" 
                  color="success"
                  onClick={handleContinue}
                >
                  Continue to Order Confirmation
                </Button>
              </Box>
            </StepContent>
          </Step>
        </Stepper>
      </Box>

      {/* Progress Indicator */}
      {submitKycMutation.isPending && (
        <Box sx={{ mb: 4 }}>
          <LinearProgress />
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1, textAlign: 'center' }}>
            Submitting your KYC documents for verification...
          </Typography>
        </Box>
      )}

      {/* Document Upload Dialogs */}
      <Dialog open={showDocumentDialog} onClose={() => setShowDocumentDialog(false)}>
        <DialogTitle>Preview Document</DialogTitle>
        <DialogContent>
          {documentPreview && (
            <img
              src={documentPreview}
              alt="Document preview"
              style={{ width: '100%', maxHeight: '400px', objectFit: 'contain' }}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDocumentDialog(false)}>Cancel</Button>
          <Button onClick={saveDocumentImage} variant="contained">Use This Document</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={showSelfieDialog} onClose={() => setShowSelfieDialog(false)}>
        <DialogTitle>Preview Selfie</DialogTitle>
        <DialogContent>
          {selfiePreview && (
            <img
              src={selfiePreview}
              alt="Selfie preview"
              style={{ width: '100%', maxHeight: '400px', objectFit: 'contain' }}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowSelfieDialog(false)}>Cancel</Button>
          <Button onClick={saveSelfieImage} variant="contained">Use This Selfie</Button>
        </DialogActions>
      </Dialog>
    </Container>
  )
}

export default KycVerification
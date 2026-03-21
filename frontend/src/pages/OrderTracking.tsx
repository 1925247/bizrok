import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation, useParams } from 'react-router-dom'
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Grid,
  Button,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Alert,
  LinearProgress,
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  Paper,
  Avatar
} from '@mui/material'
import {
  CheckCircle,
  Error as ErrorIcon,
  LocalShipping,
  Assignment,
  Done,
  PendingActions,
  Cancel,
  CalendarToday,
  AccountCircle,
  LocationOn,
  Payment as PaymentIcon
} from '@mui/icons-material'
import { useQuery } from '@tanstack/react-query'
import { orderAPI } from '../services/api'
import { useAuth } from '../hooks/useAuth'

interface OrderResponse {
  id: number
  orderNumber: string
  status: string
  basePrice: number
  finalPrice: number
  totalDeductions: number
  pickupAddress: string
  pickupPincode: string
  pickupDate: string
  pickupTime: string
  bankAccountNumber: string
  bankIfsc: string
  bankAccountName: string
  kycVerified: boolean
  faceMatchVerified: boolean
  bankDetailsVerified: boolean
  notes: string | null
  createdAt: string
  updatedAt: string
  model: {
    id: number
    name: string
    brandName: string
    categoryName: string
    basePrice: number
    variantInfo: string
    imageUrl: string | null
  }
  answers: Array<{
    id: number
    question: {
      id: number
      text: string
      slug: string
      questionType: string
    }
    option: {
      id: number
      text: string
      deductionValue: number
    } | null
    answerText: string | null
    imageUrl: string | null
  }>
  priceSnapshots: Array<{
    id: number
    basePrice: number
    totalDeductions: number
    finalPrice: number
    calculatedAt: string
  }>
}

const OrderTracking: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { orderId } = useParams<{ orderId: string }>()
  const { user } = useAuth()
  
  // Get order ID from URL params or state
  const orderNumber = location.state?.orderNumber || orderId
  
  // Order status tracking
  const [currentStatus, setCurrentStatus] = useState<string>('')
  
  // Order details query
  const orderQuery = useQuery(
    ['order', orderNumber],
    () => orderAPI.getOrder(Number(orderNumber)),
    {
      enabled: !!orderNumber,
      onSuccess: (data) => {
        setCurrentStatus(data.data.status)
      }
    }
  )
  
  // Status timeline data
  const getStatusTimeline = () => {
    const statuses = [
      { key: 'CREATED', label: 'Order Created', icon: <Assignment />, color: 'primary' },
      { key: 'ASSIGNED', label: 'Partner Assigned', icon: <LocalShipping />, color: 'info' },
      { key: 'IN_PROGRESS', label: 'Pickup Scheduled', icon: <CalendarToday />, color: 'warning' },
      { key: 'QC_DONE', label: 'Quality Check Complete', icon: <Done />, color: 'success' },
      { key: 'COMPLETED', label: 'Order Completed', icon: <CheckCircle />, color: 'success' },
      { key: 'REJECTED', label: 'Order Rejected', icon: <Cancel />, color: 'error' }
    ]
    
    return statuses
  }
  
  // Get status color and icon
  const getStatusInfo = (status: string) => {
    const statusMap: any = {
      'CREATED': { color: 'primary', icon: <Assignment /> },
      'ASSIGNED': { color: 'info', icon: <LocalShipping /> },
      'IN_PROGRESS': { color: 'warning', icon: <CalendarToday /> },
      'QC_DONE': { color: 'success', icon: <Done /> },
      'COMPLETED': { color: 'success', icon: <CheckCircle /> },
      'REJECTED': { color: 'error', icon: <Cancel /> }
    }
    
    return statusMap[status] || { color: 'default', icon: <PendingActions /> }
  }
  
  // Handle order cancellation
  const handleCancelOrder = () => {
    if (window.confirm('Are you sure you want to cancel this order?')) {
      // Call cancel order API
      // orderAPI.updateOrderStatus(orderNumber, 'CANCELLED')
      alert('Order cancellation feature will be implemented soon.')
    }
  }
  
  // Handle order reschedule
  const handleReschedule = () => {
    alert('Reschedule feature will be implemented soon.')
  }
  
  if (!orderNumber) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">
          Invalid order number. Please check your order details.
        </Alert>
        <Button 
          variant="contained" 
          onClick={() => navigate('/dashboard/orders')}
          sx={{ mt: 2 }}
        >
          View My Orders
        </Button>
      </Container>
    )
  }
  
  if (orderQuery.isLoading) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <LinearProgress />
        <Typography variant="h6" sx={{ mt: 2, textAlign: 'center' }}>
          Loading order details...
        </Typography>
      </Container>
    )
  }
  
  if (orderQuery.isError) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">
          Failed to load order details. Please try again.
        </Alert>
        <Button 
          variant="contained" 
          onClick={() => orderQuery.refetch()}
          sx={{ mt: 2 }}
        >
          Retry
        </Button>
      </Container>
    )
  }
  
  const order = orderQuery.data?.data
  
  if (!order) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">
          Order not found. Please check your order number.
        </Alert>
        <Button 
          variant="contained" 
          onClick={() => navigate('/dashboard/orders')}
          sx={{ mt: 2 }}
        >
          View My Orders
        </Button>
      </Container>
    )
  }
  
  const statusTimeline = getStatusTimeline()
  const currentStatusInfo = getStatusInfo(order.status)
  
  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Order Tracking
        </Typography>
        <Typography variant="h6" color="text.secondary" gutterBottom>
          Order #{order.orderNumber}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {order.model.brandName} {order.model.name}
        </Typography>
      </Box>

      {/* Current Status */}
      <Card elevation={3} sx={{ mb: 4 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar sx={{ bgcolor: currentStatusInfo.color }}>
                {currentStatusInfo.icon}
              </Avatar>
              <Box>
                <Typography variant="h6">Current Status</Typography>
                <Typography variant="subtitle1" color="text.secondary">
                  {order.status.replace('_', ' ')}
                </Typography>
              </Box>
            </Box>
            <Chip 
              label={order.status} 
              color={currentStatusInfo.color as any}
              variant="outlined"
            />
          </Box>
          
          {/* Status Timeline */}
          <Box sx={{ mt: 3 }}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Order Progress
            </Typography>
            <Timeline position="left">
              {statusTimeline.map((status, index) => {
                const isCompleted = statusTimeline.findIndex(s => s.key === order.status) > index
                const isCurrent = status.key === order.status
                
                return (
                  <TimelineItem key={index}>
                    <TimelineSeparator>
                      <TimelineDot color={isCompleted ? "success" : "grey"}>
                        {status.icon}
                      </TimelineDot>
                      {index < statusTimeline.length - 1 && <TimelineConnector />}
                    </TimelineSeparator>
                    <TimelineContent>
                      <Typography variant={isCurrent ? "subtitle1" : "body2"}>
                        {status.label}
                      </Typography>
                    </TimelineContent>
                  </TimelineItem>
                )
              })}
            </Timeline>
          </Box>
        </CardContent>
      </Card>

      {/* Order Details */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {/* Device Information */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                <Avatar src={order.model.imageUrl || undefined}>
                  {order.model.name.charAt(0)}
                </Avatar>
                <Box>
                  <Typography variant="h6">{order.model.name}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {order.model.brandName} • {order.model.categoryName}
                  </Typography>
                </Box>
              </Box>
              
              <List>
                <ListItem>
                  <ListItemIcon>
                    <PaymentIcon color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Final Price"
                    secondary={`₹${order.finalPrice.toLocaleString()}`}
                  />
                </ListItem>
                <ListItem>
                  <ListItemIcon>
                    <Assignment color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Base Price"
                    secondary={`₹${order.basePrice.toLocaleString()}`}
                  />
                </ListItem>
                <ListItem>
                  <ListItemIcon>
                    <ErrorIcon color="error" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Deductions"
                    secondary={`-₹${order.totalDeductions.toLocaleString()}`}
                  />
                </ListItem>
              </List>
            </CardContent>
          </Card>
        </Grid>

        {/* Pickup Details */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Pickup Details</Typography>
              
              <List>
                <ListItem>
                  <ListItemIcon>
                    <LocationOn color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Address"
                    secondary={order.pickupAddress}
                  />
                </ListItem>
                <ListItem>
                  <ListItemIcon>
                    <CalendarToday color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Date & Time"
                    secondary={`${order.pickupDate} • ${order.pickupTime}`}
                  />
                </ListItem>
                <ListItem>
                  <ListItemIcon>
                    <AccountCircle color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Account Holder"
                    secondary={order.bankAccountName}
                  />
                </ListItem>
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Verification Status */}
      <Card elevation={3} sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>Verification Status</Typography>
          
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <CheckCircle color={order.kycVerified ? "success" : "disabled"} sx={{ fontSize: 40, mb: 1 }} />
                <Typography variant="body2">KYC Verified</Typography>
                <Chip 
                  label={order.kycVerified ? "Verified" : "Pending"} 
                  color={order.kycVerified ? "success" : "default"}
                  size="small"
                  sx={{ mt: 1 }}
                />
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <CheckCircle color={order.faceMatchVerified ? "success" : "disabled"} sx={{ fontSize: 40, mb: 1 }} />
                <Typography variant="body2">Face Match</Typography>
                <Chip 
                  label={order.faceMatchVerified ? "Verified" : "Pending"} 
                  color={order.faceMatchVerified ? "success" : "default"}
                  size="small"
                  sx={{ mt: 1 }}
                />
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <CheckCircle color={order.bankDetailsVerified ? "success" : "disabled"} sx={{ fontSize: 40, mb: 1 }} />
                <Typography variant="body2">Bank Verified</Typography>
                <Chip 
                  label={order.bankDetailsVerified ? "Verified" : "Pending"} 
                  color={order.bankDetailsVerified ? "success" : "default"}
                  size="small"
                  sx={{ mt: 1 }}
                />
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Action Buttons */}
      {order.status !== 'COMPLETED' && order.status !== 'REJECTED' && (
        <Box sx={{ mb: 4, display: 'flex', gap: 2, justifyContent: 'center' }}>
          <Button
            variant="outlined"
            onClick={handleReschedule}
            disabled={order.status === 'COMPLETED' || order.status === 'REJECTED'}
          >
            Reschedule Pickup
          </Button>
          
          <Button
            variant="outlined"
            color="error"
            onClick={handleCancelOrder}
            disabled={order.status === 'COMPLETED' || order.status === 'REJECTED'}
          >
            Cancel Order
          </Button>
        </Box>
      )}

      {/* Order Notes */}
      {order.notes && (
        <Alert severity="info" sx={{ mb: 4 }}>
          <Typography variant="body2">
            <strong>Note:</strong> {order.notes}
          </Typography>
        </Alert>
      )}

      {/* Contact Support */}
      <Card elevation={3}>
        <CardContent>
          <Typography variant="h6" gutterBottom>Need Help?</Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            If you have any questions about your order, please contact our support team.
          </Typography>
          
          <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
            <Button variant="outlined">
              Contact Support
            </Button>
            <Button variant="contained">
              Track Another Order
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Container>
  )
}

export default OrderTracking
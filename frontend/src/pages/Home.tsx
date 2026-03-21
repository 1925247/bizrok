import React from 'react'
import { Box, Container, Typography, Button, Grid, Card, CardContent, Chip } from '@mui/material'
import { 
  Smartphone, 
  Laptop, 
  Tablet, 
  Watch, 
  TrendingUp, 
  LocalShipping, 
  Payment, 
  Security 
} from '@mui/icons-material'
import { useNavigate } from 'react-router-dom'
import { useQuery } from 'react-query'

import { deviceAPI } from '../services/api'

const Home: React.FC = () => {
  const navigate = useNavigate()
  
  // Fetch categories for device types
  const { data: categories, isLoading } = useQuery('categories', deviceAPI.getCategories)

  const handleStartSelling = () => {
    navigate('/device-selection')
  }

  const features = [
    {
      icon: <TrendingUp color="primary" />,
      title: 'Best Price Guaranteed',
      description: 'Get the highest market price for your devices'
    },
    {
      icon: <LocalShipping color="primary" />,
      title: 'Free Pickup',
      description: 'We come to your doorstep for device collection'
    },
    {
      icon: <Payment color="primary" />,
      title: 'Instant Payment',
      description: 'Get paid immediately after device verification'
    },
    {
      icon: <Security color="primary" />,
      title: 'Secure Process',
      description: 'Your data and device are handled with care'
    }
  ]

  const deviceTypes = [
    { icon: <Smartphone />, name: 'Smartphones', count: '1000+' },
    { icon: <Laptop />, name: 'Laptops', count: '500+' },
    { icon: <Tablet />, name: 'Tablets', count: '300+' },
    { icon: <Watch />, name: 'Smartwatches', count: '200+' }
  ]

  return (
    <Box>
      {/* Hero Section */}
      <Box 
        sx={{ 
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          color: 'white',
          py: 8,
          textAlign: 'center'
        }}
      >
        <Container maxWidth="md">
          <Typography variant="h2" component="h1" gutterBottom fontWeight="bold">
            Sell Your Old Devices
          </Typography>
          <Typography variant="h5" component="p" gutterBottom sx={{ opacity: 0.9, mb: 4 }}>
            Get the best price for your used electronics with instant payment and free pickup
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={handleStartSelling}
            sx={{ 
              bgcolor: 'white', 
              color: 'primary.main', 
              px: 4, 
              py: 2,
              fontSize: '1.1rem',
              fontWeight: 'bold',
              '&:hover': {
                bgcolor: 'grey.100',
                transform: 'translateY(-2px)',
                transition: 'transform 0.2s'
              }
            }}
          >
            Start Selling Now
          </Button>
        </Container>
      </Box>

      {/* Features Section */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Typography variant="h3" component="h2" align="center" gutterBottom>
          Why Choose Bizrok?
        </Typography>
        <Grid container spacing={4} sx={{ mt: 2 }}>
          {features.map((feature, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Card sx={{ height: '100%', textAlign: 'center', p: 2 }}>
                <CardContent>
                  <Box sx={{ mb: 2, display: 'flex', justifyContent: 'center' }}>
                    {feature.icon}
                  </Box>
                  <Typography variant="h6" component="h3" gutterBottom>
                    {feature.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {feature.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* Device Types Section */}
      <Container maxWidth="md" sx={{ py: 8 }}>
        <Typography variant="h3" component="h2" align="center" gutterBottom>
          Supported Devices
        </Typography>
        <Grid container spacing={3} sx={{ mt: 2 }}>
          {deviceTypes.map((device, index) => (
            <Grid item xs={6} sm={3} key={index}>
              <Card sx={{ 
                textAlign: 'center', 
                p: 3,
                '&:hover': {
                  transform: 'translateY(-4px)',
                  transition: 'transform 0.3s',
                  boxShadow: 3
                }
              }}>
                <Box sx={{ mb: 2, display: 'flex', justifyContent: 'center' }}>
                  {device.icon}
                </Box>
                <Typography variant="h6" component="h3" gutterBottom>
                  {device.name}
                </Typography>
                <Chip label={device.count} variant="outlined" />
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* Categories Section */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Typography variant="h3" component="h2" align="center" gutterBottom>
          Browse Categories
        </Typography>
        <Grid container spacing={3} sx={{ mt: 2 }}>
          {categories?.data?.map((category: any) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={category.id}>
              <Card 
                sx={{ 
                  height: '100%',
                  cursor: 'pointer',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    transition: 'transform 0.3s',
                    boxShadow: 3
                  }
                }}
                onClick={() => navigate('/device-selection')}
              >
                <CardContent>
                  <Typography variant="h6" component="h3" gutterBottom>
                    {category.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {category.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* CTA Section */}
      <Box 
        sx={{ 
          background: 'linear-gradient(135deg, #2c3e50 0%, #34495e 100%)',
          color: 'white',
          py: 8,
          textAlign: 'center'
        }}
      >
        <Container maxWidth="sm">
          <Typography variant="h4" component="h2" gutterBottom>
            Ready to Sell?
          </Typography>
          <Typography variant="h6" component="p" gutterBottom sx={{ opacity: 0.8, mb: 4 }}>
            Join thousands of satisfied customers who have sold their devices with us
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={handleStartSelling}
            sx={{ 
              bgcolor: 'primary.main', 
              px: 4, 
              py: 2,
              fontSize: '1.1rem',
              fontWeight: 'bold',
              '&:hover': {
                bgcolor: 'primary.dark',
                transform: 'translateY(-2px)',
                transition: 'transform 0.2s'
              }
            }}
          >
            Get Started
          </Button>
        </Container>
      </Box>
    </Box>
  )
}

export default Home
import React, { useState } from 'react'
import { Box, Container, Typography, Grid, Card, CardContent, Button, TextField, Autocomplete, Chip } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useQuery } from 'react-query'

import { deviceAPI } from '../services/api'

interface Category {
  id: number
  name: string
  description: string
}

interface Brand {
  id: number
  name: string
  slug: string
}

interface Model {
  id: number
  name: string
  slug: string
  brandName: string
  categoryName: string
  basePrice: number
  variantInfo: string
  imageUrl: string
  isActive: boolean
  sortOrder: number
}

const DeviceSelection: React.FC = () => {
  const navigate = useNavigate()
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null)
  const [selectedBrand, setSelectedBrand] = useState<Brand | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  
  // Fetch data
  const { data: categoriesData } = useQuery('categories', deviceAPI.getCategories)
  const { data: brandsData } = useQuery('brands', deviceAPI.getBrands)
  const { data: modelsData, isLoading: modelsLoading } = useQuery(
    ['models', selectedCategory?.id, selectedBrand?.id],
    () => deviceAPI.getModels(selectedCategory?.id, selectedBrand?.id),
    { enabled: !!selectedCategory }
  )

  const categories = categoriesData?.data || []
  const brands = brandsData?.data || []
  const models = modelsData?.data || []

  // Filter models based on search
  const filteredModels = models.filter(model =>
    model.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    model.variantInfo.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const handleSelectModel = (model: Model) => {
    // Store selected model in localStorage or context
    localStorage.setItem('selectedModel', JSON.stringify(model))
    navigate('/questions')
  }

  const handleCategoryChange = (category: Category | null) => {
    setSelectedCategory(category)
    setSelectedBrand(null)
    setSearchTerm('')
  }

  const handleBrandChange = (brand: Brand | null) => {
    setSelectedBrand(brand)
    setSearchTerm('')
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Select Your Device
      </Typography>
      
      {/* Filters */}
      <Box sx={{ mb: 4, p: 3, bgcolor: 'background.paper', borderRadius: 2, boxShadow: 1 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={4}>
            <Autocomplete
              options={categories}
              getOptionLabel={(option) => option.name}
              value={selectedCategory}
              onChange={(_, newValue) => handleCategoryChange(newValue)}
              renderInput={(params) => (
                <TextField {...params} label="Category" variant="outlined" />
              )}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Autocomplete
              options={brands}
              getOptionLabel={(option) => option.name}
              value={selectedBrand}
              onChange={(_, newValue) => handleBrandChange(newValue)}
              disabled={!selectedCategory}
              renderInput={(params) => (
                <TextField {...params} label="Brand" variant="outlined" />
              )}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Search models..."
              variant="outlined"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </Grid>
        </Grid>
      </Box>

      {/* Models Grid */}
      <Grid container spacing={3}>
        {modelsLoading ? (
          <Grid item xs={12}>
            <Typography>Loading models...</Typography>
          </Grid>
        ) : filteredModels.length > 0 ? (
          filteredModels.map((model: Model) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={model.id}>
              <Card 
                sx={{ 
                  height: '100%',
                  cursor: 'pointer',
                  transition: 'transform 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 3
                  }
                }}
                onClick={() => handleSelectModel(model)}
              >
                <Box sx={{ p: 2, textAlign: 'center' }}>
                  {model.imageUrl ? (
                    <Box
                      component="img"
                      src={model.imageUrl}
                      alt={model.name}
                      sx={{ 
                        width: '100%', 
                        height: 120, 
                        objectFit: 'cover',
                        borderRadius: 1,
                        mb: 2
                      }}
                    />
                  ) : (
                    <Box
                      sx={{
                        width: '100%',
                        height: 120,
                        bgcolor: 'grey.200',
                        borderRadius: 1,
                        mb: 2,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                    >
                      <Typography color="text.secondary">No Image</Typography>
                    </Box>
                  )}
                </Box>
                <CardContent>
                  <Typography variant="h6" component="h3" gutterBottom>
                    {model.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {model.variantInfo}
                  </Typography>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2 }}>
                    <Chip 
                      label={model.brandName} 
                      variant="outlined" 
                      size="small"
                    />
                    <Typography variant="h6" color="primary" fontWeight="bold">
                      ₹{model.basePrice.toLocaleString()}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))
        ) : (
          <Grid item xs={12}>
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" color="text.secondary">
                No models found. Please select a category and brand.
              </Typography>
            </Box>
          </Grid>
        )}
      </Grid>

      {/* Quick Actions */}
      <Box sx={{ mt: 4, textAlign: 'center' }}>
        <Button
          variant="outlined"
          onClick={() => navigate('/')}
          sx={{ mr: 2 }}
        >
          Back to Home
        </Button>
        <Button
          variant="contained"
          onClick={() => navigate('/pricing')}
        >
          Skip to Pricing
        </Button>
      </Box>
    </Container>
  )
}

export default DeviceSelection
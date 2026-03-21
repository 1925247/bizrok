import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Chip,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Tooltip,
  Alert,
  CircularProgress,
  useTheme,
  useMediaQuery
} from '@mui/material';
import {
  BarChart,
  LineChart,
  PieChart,
  TrendingUp,
  ShoppingCart,
  People,
  AttachMoney,
  DateRange,
  LocationOn,
  TrendingDown,
  ShowChart
} from '@mui/icons-material';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip as ChartTooltip,
  Legend,
  TimeScale
} from 'chart.js';
import 'chartjs-adapter-date-fns';
import { Bar, Line, Pie } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  ChartTooltip,
  Legend,
  TimeScale
);

interface DashboardData {
  revenueMetrics: {
    totalRevenue: number;
    avgOrderValue: number;
    orderCount: number;
    revenueByStatus: Record<string, number>;
  };
  orderMetrics: {
    ordersByStatus: Record<string, number>;
    conversionRate: number;
    avgProcessingTime: number;
    totalOrders: number;
  };
  userMetrics: {
    newUsers: number;
    activeUsers: number;
    retentionRate: number;
    avgOrdersPerUser: number;
  };
  modelPerformance: {
    topModels: Array<{
      model: string;
      brand: string;
      orderCount: number;
    }>;
    avgPriceByCategory: Record<string, number>;
  };
  geographicAnalysis: {
    ordersByState: Record<string, number>;
    ordersByCity: Record<string, number>;
    revenueByState: Record<string, number>;
  };
  timeTrends: {
    dailyOrders: Record<string, number>;
    hourlyOrders: Record<string, number>;
    weeklyRevenue: Record<string, number>;
  };
}

interface ModelAnalytics {
  modelName: string;
  basePrice: number;
  totalOrders: number;
  priceAnalysis: {
    minPrice: number;
    maxPrice: number;
    avgPrice: number;
    priceRanges: Record<string, number>;
  };
  conditionAnalysis: {
    statusDistribution: Record<string, number>;
    avgProcessingTime: Record<string, number>;
  };
  timeAnalysis: {
    monthlyOrders: Record<string, number>;
    dayOfWeekOrders: Record<string, number>;
  };
}

const AdminDashboard: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.down('md'));

  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [modelAnalytics, setModelAnalytics] = useState<ModelAnalytics | null>(null);
  const [selectedModel, setSelectedModel] = useState<string>('');
  const [dateRange, setDateRange] = useState({
    start: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    end: new Date().toISOString().split('T')[0]
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Mock data for demonstration
  const mockDashboardData: DashboardData = {
    revenueMetrics: {
      totalRevenue: 2547890,
      avgOrderValue: 12500,
      orderCount: 204,
      revenueByStatus: { '1': 150000, '2': 2397890, '3': 0, '4': 0 }
    },
    orderMetrics: {
      ordersByStatus: { '1': 15, '2': 165, '3': 12, '4': 12 },
      conversionRate: 0.81,
      avgProcessingTime: 48.5,
      totalOrders: 204
    },
    userMetrics: {
      newUsers: 89,
      activeUsers: 156,
      retentionRate: 0.65,
      avgOrdersPerUser: 1.31
    },
    modelPerformance: {
      topModels: [
        { model: 'iPhone 14 Pro', brand: 'Apple', orderCount: 45 },
        { model: 'Galaxy S23', brand: 'Samsung', orderCount: 32 },
        { model: 'Pixel 7 Pro', brand: 'Google', orderCount: 28 },
        { model: 'OnePlus 11', brand: 'OnePlus', orderCount: 24 },
        { model: 'Mi 13 Pro', brand: 'Xiaomi', orderCount: 18 }
      ],
      avgPriceByCategory: {
        'Smartphone': 18500,
        'Laptop': 35200,
        'Tablet': 12800,
        'Smartwatch': 4500
      }
    },
    geographicAnalysis: {
      ordersByState: {
        'Maharashtra': 67,
        'Karnataka': 54,
        'Delhi': 42,
        'Tamil Nadu': 31,
        'Telangana': 28
      },
      ordersByCity: {
        'Mumbai': 45,
        'Bangalore': 38,
        'Delhi': 35,
        'Hyderabad': 22,
        'Chennai': 18
      },
      revenueByState: {
        'Maharashtra': 895000,
        'Karnataka': 723000,
        'Delhi': 587000,
        'Tamil Nadu': 234000,
        'Telangana': 108000
      }
    },
    timeTrends: {
      dailyOrders: {
        '2024-01-15': 8,
        '2024-01-16': 12,
        '2024-01-17': 15,
        '2024-01-18': 9,
        '2024-01-19': 18,
        '2024-01-20': 22,
        '2024-01-21': 14
      },
      hourlyOrders: {
        '9': 5,
        '10': 8,
        '11': 12,
        '12': 15,
        '13': 18,
        '14': 22,
        '15': 25,
        '16': 28,
        '17': 24,
        '18': 19,
        '19': 12,
        '20': 8
      },
      weeklyRevenue: {
        'Week 3': 856000,
        'Week 4': 945000,
        'Week 5': 746890
      }
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, [dateRange]);

  const loadDashboardData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      setDashboardData(mockDashboardData);
    } catch (err) {
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const loadModelAnalytics = async (modelId: string) => {
    setLoading(true);
    setError(null);
    
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500));
      setModelAnalytics({
        modelName: 'iPhone 14 Pro',
        basePrice: 120000,
        totalOrders: 45,
        priceAnalysis: {
          minPrice: 85000,
          maxPrice: 115000,
          avgPrice: 98500,
          priceRanges: {
            'Under 10K': 0,
            '10K-25K': 2,
            '25K-50K': 8,
            'Above 50K': 35
          }
        },
        conditionAnalysis: {
          statusDistribution: { '1': 2, '2': 38, '3': 3, '4': 2 },
          avgProcessingTime: { '1': 24, '2': 48, '3': 72, '4': 96 }
        },
        timeAnalysis: {
          monthlyOrders: {
            '2024-1': 25,
            '2024-2': 20
          },
          dayOfWeekOrders: {
            '1': 5,
            '2': 8,
            '3': 7,
            '4': 9,
            '5': 10,
            '6': 4,
            '7': 2
          }
        }
      });
    } catch (err) {
      setError('Failed to load model analytics');
    } finally {
      setLoading(false);
    }
  };

  const getRevenueChartData = () => {
    if (!dashboardData) return { labels: [], datasets: [] };
    
    const labels = Object.keys(dashboardData.timeTrends.dailyOrders);
    const data = Object.values(dashboardData.timeTrends.dailyOrders);

    return {
      labels,
      datasets: [
        {
          label: 'Daily Orders',
          data,
          borderColor: theme.palette.primary.main,
          backgroundColor: theme.palette.primary.light,
          tension: 0.4,
        },
      ],
    };
  };

  const getOrderStatusChartData = () => {
    if (!dashboardData) return { labels: [], datasets: [] };
    
    const labels = ['Pending', 'Completed', 'Rejected', 'Cancelled'];
    const data = Object.values(dashboardData.orderMetrics.ordersByStatus);

    return {
      labels,
      datasets: [
        {
          data,
          backgroundColor: [
            theme.palette.warning.main,
            theme.palette.success.main,
            theme.palette.error.main,
            theme.palette.secondary.main,
          ],
        },
      ],
    };
  };

  const getHourlyOrdersChartData = () => {
    if (!dashboardData) return { labels: [], datasets: [] };
    
    const labels = Object.keys(dashboardData.timeTrends.hourlyOrders).map(hour => `${hour}:00`);
    const data = Object.values(dashboardData.timeTrends.hourlyOrders);

    return {
      labels,
      datasets: [
        {
          label: 'Orders per Hour',
          data,
          backgroundColor: theme.palette.primary.main,
          borderColor: theme.palette.primary.dark,
          borderWidth: 1,
        },
      ],
    };
  };

  const getGeographicChartData = () => {
    if (!dashboardData) return { labels: [], datasets: [] };
    
    const labels = Object.keys(dashboardData.geographicAnalysis.ordersByState);
    const data = Object.values(dashboardData.geographicAnalysis.ordersByState);

    return {
      labels,
      datasets: [
        {
          label: 'Orders by State',
          data,
          backgroundColor: theme.palette.primary.main,
        },
      ],
    };
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  };

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('en-IN').format(num);
  };

  if (loading && !dashboardData) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ flexGrow: 1, p: isMobile ? 1 : 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1" fontWeight="bold">
          Business Analytics Dashboard
        </Typography>
        <Box display="flex" gap={2} alignItems="center">
          <TextField
            label="Start Date"
            type="date"
            value={dateRange.start}
            onChange={(e) => setDateRange({...dateRange, start: e.target.value})}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <TextField
            label="End Date"
            type="date"
            value={dateRange.end}
            onChange={(e) => setDateRange({...dateRange, end: e.target.value})}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <Button variant="contained" onClick={loadDashboardData}>
            Refresh Data
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Key Metrics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="textSecondary" gutterBottom variant="body2">
                    Total Revenue
                  </Typography>
                  <Typography variant="h5" fontWeight="bold">
                    {formatCurrency(dashboardData?.revenueMetrics.totalRevenue || 0)}
                  </Typography>
                  <Typography variant="body2" color="success.main">
                    <TrendingUp fontSize="small" /> +12.5% from last month
                  </Typography>
                </Box>
                <AttachMoney color="primary" sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="textSecondary" gutterBottom variant="body2">
                    Avg Order Value
                  </Typography>
                  <Typography variant="h5" fontWeight="bold">
                    {formatCurrency(dashboardData?.revenueMetrics.avgOrderValue || 0)}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    {formatNumber(dashboardData?.revenueMetrics.orderCount || 0)} orders
                  </Typography>
                </Box>
                <ShoppingCart color="primary" sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="textSecondary" gutterBottom variant="body2">
                    Conversion Rate
                  </Typography>
                  <Typography variant="h5" fontWeight="bold">
                    {((dashboardData?.orderMetrics.conversionRate || 0) * 100).toFixed(1)}%
                  </Typography>
                  <Typography variant="body2" color="success.main">
                    <TrendingUp fontSize="small" /> +3.2% from last week
                  </Typography>
                </Box>
                <TrendingUp color="primary" sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="textSecondary" gutterBottom variant="body2">
                    Active Users
                  </Typography>
                  <Typography variant="h5" fontWeight="bold">
                    {formatNumber(dashboardData?.userMetrics.activeUsers || 0)}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    {((dashboardData?.userMetrics.retentionRate || 0) * 100).toFixed(1)}% retention
                  </Typography>
                </Box>
                <People color="primary" sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Charts Grid */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {/* Revenue Trend Chart */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                <ShowChart fontSize="small" /> Revenue Trend
              </Typography>
              <Box height={isMobile ? 200 : 300}>
                <Line
                  data={getRevenueChartData()}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                      legend: { display: false },
                    },
                    scales: {
                      y: {
                        beginAtZero: true,
                      },
                    },
                  }}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Order Status Distribution */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Order Status Distribution
              </Typography>
              <Box height={isMobile ? 200 : 250}>
                <Pie
                  data={getOrderStatusChartData()}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                      legend: {
                        position: 'bottom',
                        labels: {
                          usePointStyle: true,
                        },
                      },
                    },
                  }}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Hourly Orders */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                <DateRange fontSize="small" /> Orders by Hour
              </Typography>
              <Box height={isMobile ? 200 : 250}>
                <Bar
                  data={getHourlyOrdersChartData()}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                      legend: { display: false },
                    },
                    scales: {
                      y: {
                        beginAtZero: true,
                      },
                    },
                  }}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Geographic Distribution */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                <LocationOn fontSize="small" /> Geographic Distribution
              </Typography>
              <Box height={isMobile ? 200 : 250}>
                <Bar
                  data={getGeographicChartData()}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                      legend: { display: false },
                    },
                    scales: {
                      y: {
                        beginAtZero: true,
                      },
                    },
                  }}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Detailed Tables */}
      <Grid container spacing={3}>
        {/* Top Performing Models */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Top Performing Models
              </Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Model</TableCell>
                      <TableCell>Brand</TableCell>
                      <TableCell align="right">Orders</TableCell>
                      <TableCell align="right">Avg Price</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {dashboardData?.modelPerformance.topModels.map((model, index) => (
                      <TableRow key={index}>
                        <TableCell>{model.model}</TableCell>
                        <TableCell>
                          <Chip label={model.brand} size="small" color="primary" />
                        </TableCell>
                        <TableCell align="right">{formatNumber(model.orderCount)}</TableCell>
                        <TableCell align="right">
                          {formatCurrency(dashboardData.modelPerformance.avgPriceByCategory[model.brand] || 0)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Model Analytics */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">
                  Model Analytics
                </Typography>
                <FormControl size="small" sx={{ minWidth: 200 }}>
                  <InputLabel>Model</InputLabel>
                  <Select
                    value={selectedModel}
                    label="Model"
                    onChange={(e) => {
                      setSelectedModel(e.target.value);
                      loadModelAnalytics(e.target.value);
                    }}
                  >
                    <MenuItem value="iphone14">iPhone 14 Pro</MenuItem>
                    <MenuItem value="galaxys23">Galaxy S23</MenuItem>
                    <MenuItem value="pixel7">Pixel 7 Pro</MenuItem>
                  </Select>
                </FormControl>
              </Box>
              
              {modelAnalytics && (
                <Box>
                  <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                    {modelAnalytics.modelName} Analysis
                  </Typography>
                  
                  <Grid container spacing={2} sx={{ mb: 2 }}>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="textSecondary">Base Price</Typography>
                      <Typography variant="h6">{formatCurrency(modelAnalytics.basePrice)}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="textSecondary">Total Orders</Typography>
                      <Typography variant="h6">{formatNumber(modelAnalytics.totalOrders)}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="textSecondary">Avg Price</Typography>
                      <Typography variant="h6">{formatCurrency(modelAnalytics.priceAnalysis.avgPrice)}</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2" color="textSecondary">Price Range</Typography>
                      <Typography variant="h6">
                        {formatCurrency(modelAnalytics.priceAnalysis.minPrice)} - {formatCurrency(modelAnalytics.priceAnalysis.maxPrice)}
                      </Typography>
                    </Grid>
                  </Grid>

                  <Box display="flex" gap={1} flexWrap="wrap">
                    {Object.entries(modelAnalytics.priceAnalysis.priceRanges).map(([range, count]) => (
                      <Chip
                        key={range}
                        label={`${range}: ${formatNumber(count)}`}
                        size="small"
                        variant="outlined"
                      />
                    ))}
                  </Box>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Action Buttons */}
      <Box mt={4} display="flex" gap={2} flexWrap="wrap">
        <Button variant="outlined" startIcon={<TrendingUp />}>
          Export Report
        </Button>
        <Button variant="outlined" startIcon={<People />}>
          User Segmentation
        </Button>
        <Button variant="outlined" startIcon={<LocationOn />}>
          Market Analysis
        </Button>
        <Button variant="outlined" startIcon={<AttachMoney />}>
          Revenue Forecast
        </Button>
      </Box>
    </Box>
  );
};

export default AdminDashboard;
import { Route, Routes } from 'react-router-dom';
import { NavBar } from './components/NavBar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { DashboardPage } from './pages/DashboardPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterProducerPage } from './pages/RegisterProducerPage';
import { RegisterRestaurantPage } from './pages/RegisterRestaurantPage';

export function App() {
  return (
    <>
      <NavBar />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/cadastro/produtor" element={<RegisterProducerPage />} />
          <Route path="/cadastro/restaurante" element={<RegisterRestaurantPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>
        </Routes>
      </main>
    </>
  );
}

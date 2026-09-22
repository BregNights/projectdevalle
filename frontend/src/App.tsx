import { GoogleOAuthProvider } from '@react-oauth/google';
import { Route, Routes } from 'react-router-dom';
import { NavBar } from './components/NavBar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AccountPage } from './pages/AccountPage';
import { CatalogPage } from './pages/CatalogPage';
import { DashboardPage } from './pages/DashboardPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { MyOffersPage } from './pages/MyOffersPage';
import { RegisterProducerPage } from './pages/RegisterProducerPage';
import { RegisterRestaurantPage } from './pages/RegisterRestaurantPage';
import { SocialSignupChooserPage } from './pages/SocialSignupChooserPage';

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID ?? '';

function AppRoutes() {
  return (
    <>
      <NavBar />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/recuperar-senha" element={<ForgotPasswordPage />} />
          <Route path="/cadastro/escolher-tipo" element={<SocialSignupChooserPage />} />
          <Route path="/cadastro/produtor" element={<RegisterProducerPage />} />
          <Route path="/cadastro/restaurante" element={<RegisterRestaurantPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/perfil" element={<AccountPage />} />
            <Route path="/ofertas" element={<MyOffersPage />} />
            <Route path="/catalogo" element={<CatalogPage />} />
          </Route>
        </Routes>
      </main>
    </>
  );
}

export function App() {
  return (
    <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
      <AppRoutes />
    </GoogleOAuthProvider>
  );
}

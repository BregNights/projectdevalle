import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useCart } from '../cart/CartContext';

export function NavBar() {
  const { token, claims, logout } = useAuth();
  const cart = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">
        projectdevalle
      </Link>
      <nav className="navbar-links">
        {token && claims ? (
          <>
            <Link to="/dashboard">Meu cadastro</Link>
            {(claims.role === 'PRODUCER' || claims.role === 'RESTAURANT') && (
              <Link to="/perfil">Editar cadastro</Link>
            )}
            {claims.role === 'PRODUCER' && <Link to="/ofertas">Minhas ofertas</Link>}
            {(claims.role === 'PRODUCER' || claims.role === 'RESTAURANT' || claims.role === 'ADMINISTRATOR') && (
              <Link to="/catalogo">Catálogo</Link>
            )}
            {(claims.role === 'PRODUCER' || claims.role === 'RESTAURANT') && <Link to="/pedidos">Pedidos</Link>}
            {claims.role === 'RESTAURANT' && (
              <>
                <Link to="/pedidos-recorrentes">Recorrentes</Link>
                <Link to="/carrinho">Carrinho{cart.items.length > 0 ? ` (${cart.items.length})` : ''}</Link>
              </>
            )}
            <span className="navbar-role">{claims.role}</span>
            <button type="button" onClick={handleLogout}>
              Sair
            </button>
          </>
        ) : (
          <>
            <Link to="/cadastro/produtor">Sou produtor</Link>
            <Link to="/cadastro/restaurante">Sou restaurante</Link>
            <Link to="/login">Entrar</Link>
          </>
        )}
      </nav>
    </header>
  );
}

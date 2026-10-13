import type { FormEvent } from 'react';
import { Link, NavLink, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { ROLE_LABELS } from '../api/labels';
import { useAuth } from '../auth/AuthContext';
import { useCart } from '../cart/CartContext';

const MARKETPLACE_PATHS = ['/', '/catalogo'];

// Cabeçalho de marketplace: busca sempre à mão e carrinho visível também para visitantes (RN47/RN75).
export function NavBar() {
  const { token, claims, logout } = useAuth();
  const cart = useCart();
  const navigate = useNavigate();
  const location = useLocation();
  const [params] = useSearchParams();
  const onMarketplace = MARKETPLACE_PATHS.includes(location.pathname);
  const search = onMarketplace ? (params.get('q') ?? '') : '';

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // A busca mantém o layout escolhido no catálogo; de outras telas, leva ao catálogo.
  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const value = String(new FormData(event.currentTarget).get('q') ?? '').trim();
    const next = new URLSearchParams(onMarketplace ? params : undefined);
    if (value) next.set('q', value);
    else next.delete('q');
    const query = next.toString();
    navigate(`${onMarketplace ? location.pathname : '/'}${query ? `?${query}` : ''}`);
  };

  const role = claims?.role;
  const cartCount = cart.items.length;

  return (
    <header className="navbar">
      <div className="navbar-top">
        <Link to="/" className="navbar-brand">
          projectdevalle
        </Link>

        <form className="navbar-search" role="search" onSubmit={handleSearch}>
          <label htmlFor="navbar-search-input" className="mk-visually-hidden">
            Buscar no catálogo
          </label>
          <input
            id="navbar-search-input"
            key={search}
            name="q"
            type="search"
            defaultValue={search}
            placeholder="Buscar tilápia, alface, queijo colonial..."
            autoComplete="off"
          />
          <button type="submit" aria-label="Buscar">
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path
                fill="currentColor"
                d="M15.5 14h-.79l-.28-.27A6.47 6.47 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5Zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14Z"
              />
            </svg>
          </button>
        </form>

        <nav className="navbar-actions" aria-label="Conta">
          {token && claims ? (
            <>
              <span className="navbar-role">{ROLE_LABELS[claims.role] ?? claims.role}</span>
              <button type="button" className="navbar-logout" onClick={handleLogout}>
                Sair
              </button>
            </>
          ) : (
            <>
              <Link to="/como-funciona" className="navbar-link navbar-link-optional">
                Como funciona
              </Link>
              <Link to="/cadastro/produtor" className="navbar-link navbar-link-optional">
                Venda aqui
              </Link>
              <Link to="/login" className="navbar-login">
                Entrar
              </Link>
            </>
          )}
          {cart.enabled && (
            <Link to="/carrinho" className="navbar-cart" aria-label={`Carrinho, ${cartCount} itens`}>
              <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
                <path
                  fill="currentColor"
                  d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2ZM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42a.25.25 0 0 1-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1 1 0 0 0 20 4H5.21l-.94-2H1Zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2Z"
                />
              </svg>
              {cartCount > 0 && <span className="navbar-cart-count">{cartCount}</span>}
            </Link>
          )}
        </nav>
      </div>

      {token && claims && (
        <nav className="navbar-sub" aria-label="Minha área">
          <NavLink to="/" end>
            Catálogo
          </NavLink>
          <NavLink to="/dashboard">Meu cadastro</NavLink>
          {(role === 'PRODUCER' || role === 'RESTAURANT') && <NavLink to="/perfil">Editar cadastro</NavLink>}
          {role === 'PRODUCER' && <NavLink to="/ofertas">Minhas ofertas</NavLink>}
          {(role === 'PRODUCER' || role === 'RESTAURANT') && <NavLink to="/pedidos">Pedidos</NavLink>}
          {role === 'RESTAURANT' && <NavLink to="/pedidos-recorrentes">Recorrentes</NavLink>}
        </nav>
      )}
    </header>
  );
}

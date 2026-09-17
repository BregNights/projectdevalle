import { Link } from 'react-router-dom';

export function HomePage() {
  return (
    <div className="page page-home">
      <h1>Conectando produtores e restaurantes</h1>
      <p>
        Plataforma para produtores rurais e pescadores do Vale do Itajaí e litoral norte de SC venderem
        diretamente para restaurantes da região.
      </p>
      <div className="home-cards">
        <div className="card">
          <h2>Sou produtor ou pescador</h2>
          <p>Cadastre sua propriedade ou ponto de coleta e comece a oferecer seus produtos.</p>
          <Link className="button" to="/cadastro/produtor">
            Cadastrar como produtor
          </Link>
        </div>
        <div className="card">
          <h2>Sou restaurante</h2>
          <p>Cadastre seu estabelecimento e encontre produtores próximos.</p>
          <Link className="button" to="/cadastro/restaurante">
            Cadastrar como restaurante
          </Link>
        </div>
      </div>
      <p>
        Já tem cadastro? <Link to="/login">Entre na sua conta</Link>.
      </p>
    </div>
  );
}

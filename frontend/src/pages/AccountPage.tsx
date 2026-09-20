import { useAuth } from '../auth/AuthContext';
import { ProducerAccountPage } from './ProducerAccountPage';
import { RestaurantAccountPage } from './RestaurantAccountPage';

export function AccountPage() {
  const { claims } = useAuth();

  if (claims?.role === 'PRODUCER') {
    return <ProducerAccountPage />;
  }

  if (claims?.role === 'RESTAURANT') {
    return <RestaurantAccountPage />;
  }

  return (
    <div className="page">
      <h1>Editar cadastro</h1>
      <p>Esta página é exclusiva para contas de produtor ou restaurante.</p>
    </div>
  );
}

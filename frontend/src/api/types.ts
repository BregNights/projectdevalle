export type TaxDocumentType = 'CPF' | 'CNPJ';

export type ProductionType = 'FARMING' | 'FISHING' | 'LIVESTOCK' | 'ARTISANAL_PROCESSING';

export type SupportingDocumentType = 'CPF' | 'CNPJ' | 'DAP_CAF' | 'FISHING_LICENSE';

export type EstablishmentCategory = 'FINE_DINING' | 'BISTRO' | 'CHAIN' | 'OTHER';

export type RegistrationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';

export interface AddressInput {
  street: string;
  number: string;
  neighborhood: string;
  city: string;
  state: string;
  zipCode: string;
  complement?: string;
}

export interface SupportingDocumentInput {
  type: SupportingDocumentType;
  documentNumber: string;
  fileUrl: string;
}

export interface RegisterProducerRequest {
  email: string;
  password: string;
  name: string;
  taxDocumentType: TaxDocumentType;
  taxDocumentNumber: string;
  productionType: ProductionType;
  originAddress: AddressInput;
  supportingDocuments: SupportingDocumentInput[];
}

export interface ProducerRegistrationResponse {
  producerId: string;
  userId: string;
  geocodingPending: boolean;
}

export interface ProducerResponse {
  id: string;
  name: string;
  productionType: ProductionType;
  status: RegistrationStatus;
  geocodingPending: boolean;
  visibleCertifications: string[];
}

export interface ContactInput {
  name: string;
  role?: string;
  phone?: string;
  email?: string;
}

export interface RegisterRestaurantRequest {
  email: string;
  password: string;
  corporateName: string;
  cnpj: string;
  category: EstablishmentCategory;
  contact: ContactInput;
  initialAddressLabel: string;
  initialAddress: AddressInput;
}

export interface RestaurantRegistrationResponse {
  restaurantId: string;
  userId: string;
}

export interface RestaurantResponse {
  id: string;
  corporateName: string;
  category: EstablishmentCategory;
  status: RegistrationStatus;
  deliveryAddressCount: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
}

export interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
}

export interface RecentUser {
  email: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface AdminMetricsResponse {
  totalProducers: number;
  totalRestaurants: number;
  totalUsers: number;
  activeUsers: number;
  producersByStatus: Record<string, number>;
  restaurantsByStatus: Record<string, number>;
  producersByProductionType: Record<string, number>;
  restaurantsByCategory: Record<string, number>;
  usersByRole: Record<string, number>;
  producersGeocodingPending: number;
  newProducersLast7Days: number;
  newRestaurantsLast7Days: number;
  newProducersLast30Days: number;
  newRestaurantsLast30Days: number;
  topProducerCities: Record<string, number>;
  recentUsers: RecentUser[];
}

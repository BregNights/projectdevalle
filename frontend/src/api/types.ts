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

export type SocialLoginStatus = 'AUTHENTICATED' | 'REGISTRATION_REQUIRED';

export interface SocialLoginRequest {
  idToken: string;
}

export interface SocialLoginResponse {
  status: SocialLoginStatus;
  accessToken: string | null;
  email: string | null;
  displayName: string | null;
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

export type ProductCategory =
  | 'VEGETABLES'
  | 'FRUITS'
  | 'FISH'
  | 'MEAT_POULTRY'
  | 'DAIRY'
  | 'GRAINS_CEREALS'
  | 'PROCESSED'
  | 'OTHER';

export type MeasurementUnit = 'KILOGRAM' | 'GRAM' | 'LITER' | 'UNIT' | 'DOZEN' | 'BOX';

export type RecurrenceType = 'RECURRING' | 'ONE_TIME';

export type OfferStatus = 'ACTIVE' | 'PAUSED' | 'SOLD_OUT' | 'REMOVED';

export type DayOfWeekName =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export interface OfferResponse {
  id: string;
  producerId: string;
  productName: string;
  category: ProductCategory;
  unit: MeasurementUnit;
  price: number;
  quantityAvailable: number;
  recurrenceType: RecurrenceType;
  recurrenceDayOfWeek: DayOfWeekName | null;
  availabilityFrom: string | null;
  availabilityUntil: string | null;
  photoUrls: string[];
  status: OfferStatus;
}

export interface PublishOfferRequest {
  productName: string;
  category: ProductCategory;
  unit: MeasurementUnit;
  price: number;
  quantityAvailable: number;
  recurrenceType: RecurrenceType;
  recurrenceDayOfWeek?: DayOfWeekName | null;
  availabilityFrom?: string | null;
  availabilityUntil?: string | null;
  photoUrls?: string[];
}

export interface UpdateOfferDetailsRequest {
  price: number;
  availabilityFrom?: string | null;
  availabilityUntil?: string | null;
  photoUrls?: string[];
}

export interface UpdateOfferQuantityRequest {
  quantityAvailable: number;
}

export interface CatalogEntryResponse {
  offer: OfferResponse;
  producerId: string;
  producerName: string | null;
  producerCity: string | null;
  distanceKilometers: number | null;
  distanceDurationMinutes: number | null;
}

export type BankAccountType = 'CHECKING' | 'SAVINGS';

export interface BankDetailsResponse {
  bankName: string;
  agency: string;
  account: string;
  accountType: BankAccountType;
  accountHolder: string;
}

export interface AddressResponse {
  street: string;
  number: string;
  neighborhood: string;
  city: string;
  state: string;
  zipCode: string;
  complement: string | null;
}

export interface ProducerAccountResponse {
  id: string;
  name: string;
  taxDocumentNumber: string;
  originAddress: AddressResponse;
  geocodingPending: boolean;
  bankDetails: BankDetailsResponse | null;
  deliveryAreaMunicipalities: string[];
}

export interface UpdateProducerProfileRequest {
  name: string;
}

export interface UpdateBankDetailsRequest {
  bankName: string;
  agency: string;
  account: string;
  accountType: BankAccountType;
  accountHolder: string;
}

export interface UpdateDeliveryAreaRequest {
  municipalities: string[];
}

export interface UpdateOriginAddressRequest {
  address: AddressInput;
}

export interface UpdateOriginAddressResponse {
  geocodingPending: boolean;
}

export interface ContactResponse {
  name: string;
  role: string | null;
  phone: string | null;
  email: string | null;
}

export interface DeliveryAddressResponse {
  id: string;
  label: string;
  address: AddressResponse;
  primary: boolean;
  geocodingPending: boolean;
}

export interface RestaurantAccountResponse {
  id: string;
  corporateName: string;
  cnpj: string;
  category: EstablishmentCategory;
  contact: ContactResponse;
  deliveryAddresses: DeliveryAddressResponse[];
}

export interface UpdateRestaurantProfileRequest {
  corporateName: string;
  category: EstablishmentCategory;
  contact: ContactInput;
}

export interface AddDeliveryAddressRequest {
  label: string;
  address: AddressInput;
  primary: boolean;
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

export const PRODUCTION_TYPE_LABELS: Record<string, string> = {
  FARMING: 'Agricultura',
  FISHING: 'Pesca',
  LIVESTOCK: 'Pecuária',
  ARTISANAL_PROCESSING: 'Processamento artesanal',
};

export const CATEGORY_LABELS: Record<string, string> = {
  FINE_DINING: 'Alta gastronomia',
  BISTRO: 'Bistrô',
  CHAIN: 'Rede',
  OTHER: 'Outro',
};

export const CERTIFICATION_LABELS: Record<string, string> = {
  ORGANIC: 'Orgânico',
  ORIGIN_SEAL: 'Selo de origem',
  GOOD_FISHING_PRACTICES: 'Boas práticas de pesca',
  OTHER: 'Outra certificação',
};

export const ROLE_LABELS: Record<string, string> = {
  PRODUCER: 'Produtor',
  RESTAURANT: 'Restaurante',
  ADMINISTRATOR: 'Administrador',
  LOGISTICS_OPERATOR: 'Operador logístico',
};

export const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pendente',
  APPROVED: 'Aprovado',
  REJECTED: 'Rejeitado',
  SUSPENDED: 'Suspenso',
};

export const PRODUCT_CATEGORY_LABELS: Record<string, string> = {
  VEGETABLES: 'Hortifruti',
  FRUITS: 'Frutas',
  FISH: 'Pescado',
  MEAT_POULTRY: 'Carnes e aves',
  DAIRY: 'Laticínios',
  GRAINS_CEREALS: 'Grãos e cereais',
  PROCESSED: 'Processados',
  OTHER: 'Outro',
};

// RN06 — categorias perecíveis exigem data de validade obrigatória na oferta.
export const PERISHABLE_CATEGORIES = new Set(['VEGETABLES', 'FRUITS', 'FISH', 'MEAT_POULTRY', 'DAIRY']);

export const MEASUREMENT_UNIT_LABELS: Record<string, string> = {
  KILOGRAM: 'kg',
  GRAM: 'g',
  LITER: 'L',
  UNIT: 'unidade',
  DOZEN: 'dúzia',
  BOX: 'caixa',
};

export const OFFER_STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Ativa',
  PAUSED: 'Pausada',
  SOLD_OUT: 'Esgotada',
  REMOVED: 'Removida',
};

export const DAY_OF_WEEK_LABELS: Record<string, string> = {
  MONDAY: 'Segunda-feira',
  TUESDAY: 'Terça-feira',
  WEDNESDAY: 'Quarta-feira',
  THURSDAY: 'Quinta-feira',
  FRIDAY: 'Sexta-feira',
  SATURDAY: 'Sábado',
  SUNDAY: 'Domingo',
};

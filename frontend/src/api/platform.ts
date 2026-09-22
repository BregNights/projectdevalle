import { useEffect, useState } from 'react';
import { apiClient } from './client';
import type { ProductCategory } from './types';

export interface CoverageRegion {
  name: string;
  cities: string[];
}

export interface PlatformCoverage {
  coverageRegions: CoverageRegion[];
  enabledProductCategories: ProductCategory[];
}

export interface PlatformSettings extends PlatformCoverage {
  commissionPercentage: number;
}

// RF43 — regiões/municípios atendidos e categorias habilitadas pela administração (informação pública).
export function usePlatformCoverage(): PlatformCoverage | null {
  const [coverage, setCoverage] = useState<PlatformCoverage | null>(null);

  useEffect(() => {
    let cancelled = false;
    apiClient
      .get<PlatformCoverage>('/api/v1/platform/coverage')
      .then((data) => {
        if (!cancelled) setCoverage(data);
      })
      .catch(() => {
        // Sem a lista, as telas continuam funcionando (sem sugestões de cidade/região).
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return coverage;
}

export function allCoveredCities(coverage: PlatformCoverage | null): string[] {
  return coverage ? coverage.coverageRegions.flatMap((region) => region.cities) : [];
}

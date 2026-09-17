# Frontend — projectdevalle

SPA em React + TypeScript (Vite) que consome a API REST do backend Spring Boot.

## Rodando localmente

```bash
npm install
npm run dev
```

Por padrão a API é esperada em `http://localhost:8080`. Para apontar para outro endereço, copie
`.env.example` para `.env.local` e ajuste `VITE_API_BASE_URL`.

O backend precisa permitir CORS para a origem do Vite (`http://localhost:5173` por padrão) — configure
`CORS_ALLOWED_ORIGINS` no backend de acordo.

## Estrutura

- `src/api/` — cliente HTTP e tipos compartilhados com os DTOs do backend.
- `src/auth/` — contexto de autenticação (token JWT em `localStorage`, decodificação do papel do usuário).
- `src/pages/` — telas: home, cadastro de produtor, cadastro de restaurante, login e dashboard.
- `src/components/` — componentes reutilizáveis (navbar, badge de status, rota protegida).

## Escopo atual

Cobre o fluxo de cadastro e autoatendimento (RF01, RF02, RF06): cadastro de produtor/restaurante, login e
consulta do próprio status de aprovação. Não há painel de administração nesta fase — a aprovação de
cadastros é feita apenas via API.

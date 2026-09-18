import type { ReactNode } from 'react';
import { ImageTextBlock } from '../components/ImageTextBlock';

const IMAGES = {
  producer: 'https://images.unsplash.com/photo-1757627550652-30788bfce978?auto=format&fit=crop&w=1000&q=80',
  restaurant: 'https://images.unsplash.com/photo-1761095596765-c8abe01d3aea?auto=format&fit=crop&w=1000&q=80',
};

const PRODUCER_BENEFITS = [
  {
    title: 'Venda direta, sem atravessador',
    text: 'Negocie diretamente com restaurantes da sua região e fique com uma margem maior sobre o que produz.',
  },
  {
    title: 'Ofertas do jeito que sua produção funciona',
    text: 'Publique ofertas recorrentes (ex.: toda quinta-feira) ou pontuais, conforme a safra ou a pesca do dia.',
  },
  {
    title: 'Preço de referência sugerido',
    text: 'Veja uma faixa de preço baseada no histórico da categoria e da região antes de publicar uma oferta — a decisão final é sempre sua.',
  },
  {
    title: 'Planeje com a demanda da região',
    text: 'Um painel agregado mostra o que os restaurantes vão precisar nas próximas semanas, por produto e por região.',
  },
  {
    title: 'Pagamento protegido',
    text: 'O valor fica retido até a confirmação da entrega, então você não corre risco de calote.',
  },
  {
    title: 'Reputação que valoriza quem cumpre',
    text: 'Avaliações de pontualidade e qualidade constroem sua reputação — e certificações (orgânico, boas práticas) ficam visíveis no seu perfil.',
  },
];

const RESTAURANT_BENEFITS = [
  {
    title: 'Catálogo pesquisável de verdade',
    text: 'Filtre por categoria, região, produtor, certificação, preço e prazo de entrega — e compare ofertas equivalentes lado a lado.',
  },
  {
    title: 'Distância antes de comprar',
    text: 'Cada oferta mostra a distância aproximada até o produtor, para você decidir com informação de logística real.',
  },
  {
    title: 'Rastreabilidade de origem',
    text: 'Produtor, data de colheita ou captura e lote ficam registrados em cada pedido — prontos para você comunicar ao seu cliente final.',
  },
  {
    title: 'Negociação assistida',
    text: 'Proponha quantidade e preço, receba uma contraproposta do produtor e só confirme quando fizer sentido para os dois lados.',
  },
  {
    title: 'Pedidos recorrentes automatizados',
    text: 'Configure uma vez e deixe repetir semanalmente, sem recriar o pedido manualmente toda vez.',
  },
  {
    title: 'Acompanhamento da entrega',
    text: 'Status rastreável do pedido, do preparo ao transporte, com estimativa de chegada.',
  },
];

const DIFFERENTIALS = [
  {
    icon: '📍',
    title: 'Geolocalização real',
    text: 'Distância, tempo estimado e roteirização de coleta entre múltiplos produtores — pensado para estradas rurais, não só vias urbanas.',
  },
  {
    icon: '🔒',
    title: 'Pagamento com garantia',
    text: 'Escrow: o valor só é liberado ao produtor depois que o restaurante confirma o recebimento.',
  },
  {
    icon: '📈',
    title: 'Inteligência de preço',
    text: 'Histórico de preços por produto, produtor e região, para negociar com mais informação dos dois lados.',
  },
  {
    icon: '📶',
    title: 'Feito para conexão instável',
    text: 'Cadastro de oferta funciona mesmo com internet fraca, comum em áreas rurais e litorâneas, sincronizando quando a conexão voltar.',
  },
];

const STEPS = [
  { number: '1', title: 'Cadastre-se', text: 'Produtor ou restaurante, com verificação simples de documentos.' },
  { number: '2', title: 'Aprovação', text: 'A administração da plataforma valida o cadastro antes de liberar o acesso.' },
  { number: '3', title: 'Publique ou negocie', text: 'Ofertas, necessidades futuras ou propostas — o combinado vira pedido.' },
  { number: '4', title: 'Entrega e avaliação', text: 'Acompanhe o pedido até a entrega e avalie a experiência.' },
];

function Section({
  alt,
  eyebrow,
  children,
  className,
}: {
  alt?: boolean;
  eyebrow?: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <section className={`section${alt ? ' section-alt' : ''}${className ? ` ${className}` : ''}`}>
      <div className="section-inner">
        {eyebrow && <h2 className="section-eyebrow">{eyebrow}</h2>}
        {children}
      </div>
    </section>
  );
}

export function HomePage() {
  return (
    <div className="landing">
      <section className="hero">
        <div className="section-inner">
          <h1>Conectando produtores e restaurantes no Vale do Itajaí e litoral norte de SC</h1>
          <p>
            Uma plataforma para produtores rurais e pescadores venderem direto para restaurantes da região —
            com geolocalização, pagamento protegido e rastreabilidade de origem.
          </p>
          <p className="hero-note">Use o menu no topo da página para se cadastrar como produtor ou restaurante.</p>
        </div>
      </section>

      <Section eyebrow="Para produtores e pescadores">
        <ImageTextBlock
          image={IMAGES.producer}
          alt="Caixas de hortaliças frescas recém-colhidas"
          eyebrow="Do campo e do mar direto pro restaurante"
          title="Sua produção vale mais quando vendida direto"
          text="Sem atravessador, sem intermediário levando a margem que devia ser sua. Você publica, negocia e recebe com garantia."
          bullets={[
            'Ofertas recorrentes ou pontuais, do jeito que sua produção funciona',
            'Preço de referência sugerido, mas a decisão é sempre sua',
            'Pagamento retido até a confirmação da entrega',
          ]}
        />
        <div className="feature-grid">
          {PRODUCER_BENEFITS.map((item) => (
            <div className="feature-card" key={item.title}>
              <h3>{item.title}</h3>
              <p>{item.text}</p>
            </div>
          ))}
        </div>
      </Section>

      <Section alt eyebrow="Para restaurantes">
        <ImageTextBlock
          image={IMAGES.restaurant}
          alt="Chef preparando um prato em cozinha profissional"
          eyebrow="Ingredientes locais, com rastreabilidade real"
          title="Compre com informação, não no escuro"
          text="Veja distância, certificações e histórico de preço antes de fechar. Depois, acompanhe a entrega até a sua cozinha."
          bullets={[
            'Catálogo filtrável por região, certificação e prazo',
            'Rastreabilidade de origem em cada pedido',
            'Negociação assistida e pedidos recorrentes automatizados',
          ]}
          reverse
        />
        <div className="feature-grid">
          {RESTAURANT_BENEFITS.map((item) => (
            <div className="feature-card" key={item.title}>
              <h3>{item.title}</h3>
              <p>{item.text}</p>
            </div>
          ))}
        </div>
      </Section>

      <Section eyebrow="Diferenciais">
        <div className="differential-grid">
          {DIFFERENTIALS.map((item) => (
            <div className="differential-card" key={item.title}>
              <span className="differential-icon">{item.icon}</span>
              <h3>{item.title}</h3>
              <p>{item.text}</p>
            </div>
          ))}
        </div>
      </Section>

      <Section alt eyebrow="Como funciona">
        <div className="steps">
          {STEPS.map((step) => (
            <div className="step" key={step.number}>
              <span className="step-number">{step.number}</span>
              <div>
                <h3>{step.title}</h3>
                <p>{step.text}</p>
              </div>
            </div>
          ))}
        </div>
      </Section>

      <Section className="cta-final">
        <h2>Pronto para começar?</h2>
        <p>
          Use o menu no topo da página — <strong>Sou produtor</strong>, <strong>Sou restaurante</strong> ou{' '}
          <strong>Entrar</strong>, se já tiver cadastro.
        </p>
      </Section>
    </div>
  );
}

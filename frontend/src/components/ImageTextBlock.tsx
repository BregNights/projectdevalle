import type { ReactNode } from 'react';

export function ImageTextBlock({
  image,
  alt,
  eyebrow,
  title,
  text,
  bullets,
  reverse,
}: {
  image: string;
  alt: string;
  eyebrow: string;
  title: string;
  text: string;
  bullets: string[];
  reverse?: boolean;
}) {
  const media = (
    <div className="image-text-media" key="media">
      <img src={image} alt={alt} loading="lazy" />
    </div>
  );

  const copy = (
    <div className="image-text-copy" key="copy">
      <span className="image-text-eyebrow">{eyebrow}</span>
      <h3>{title}</h3>
      <p>{text}</p>
      <ul className="bullet-list">
        {bullets.map((bullet) => (
          <li key={bullet}>{bullet}</li>
        ))}
      </ul>
    </div>
  );

  const children: ReactNode[] = reverse ? [copy, media] : [media, copy];

  return <div className="image-text-block">{children}</div>;
}

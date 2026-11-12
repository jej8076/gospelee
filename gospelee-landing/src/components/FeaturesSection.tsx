'use client';

import styles from './FeaturesSection.module.css';

const features = [
  {
    tag: '01 / ZERO DISTRACTIONS',
    title: '말씀 앞에 광고는 없습니다',
    description: '기도와 묵상의 순간을 방해하는 배너나 영상 광고는 일절 넣지 않습니다. 오직 말씀과 나만의 고요한 자리.',
    badge: '광고 없음',
  },
  {
    tag: '02 / SMALL CHURCH',
    title: '작은 교회는 평생 무료',
    description: '재정이 부담되는 종이 주보 대신 모바일 주보를. 30명 이하 개척교회는 평생 무료입니다.',
    badge: '30명 이하 · 평생 무료',
  },
  {
    tag: '03 / INDIE EXPERIMENT',
    title: '1인 개발자의 지속 가능한 도전',
    description: '거대 자본의 잣대가 아닌, 기독교 문화와 작은 공동체에 꼭 필요한 도구를 사명감으로 빚어갑니다.',
    badge: 'Gospelee Ecosystem',
  },
];

const FeaturesSection = () => {
  return (
    <section id="values" className={styles.features}>
      <div className="container">
        <div className={styles.header}>
          <span className={styles.sectionTag}>CRAFT & VALUES</span>
          <h2 className={styles.title}>
            광고와 타협하지 않고,<br />
            온전히 말씀과 교회를 향해.
          </h2>
          <p className={styles.subtitle}>
            정보를 나열하기보다, 한 명의 개발자로서 지키고 싶은 본질에 집중했습니다.
          </p>
        </div>

        <div className={styles.grid}>
          {features.map((feature, index) => (
            <div key={index} className={styles.card}>
              <div className={styles.cardHeader}>
                <span className={styles.cardTag}>{feature.tag}</span>
                <span className={styles.cardBadge}>{feature.badge}</span>
              </div>
              <h3 className={styles.cardTitle}>{feature.title}</h3>
              <p className={styles.cardDescription}>{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default FeaturesSection;

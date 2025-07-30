'use client';

import styles from './FeaturesSection.module.css';

const FeaturesSection = () => {
  const features = [
    {
      icon: '📖',
      title: '성경 읽기',
      description: '직접 타이핑하여 지루하지 않게 성경을 읽을 수 있어요'
    },
    {
      icon: '📝',
      title: '말씀 묵상',
      description: '선택한 말씀으로 묵상을 기록할 수 있어요'
    },
    {
      icon: '👥',
      title: '커뮤니티',
      description: '믿음의 동료들과 함께 말씀을 나누고 서로 격려해보세요.'
    }
  ];

  return (
      <section className={styles.features}>
        <div className="container">
          <div className={styles.featuresContent}>
            <div className={styles.header}>
              <h2 className={styles.title}>
                <span className={styles.highlight}>OOG에서는</span>
              </h2>
              <p className={styles.subtitle}>

              </p>
            </div>

            <div className={styles.grid}>
              {features.map((feature, index) => (
                  <div key={index} className={styles.card}>
                    <div className={styles.cardIcon}>
                      {feature.icon}
                    </div>
                    <h3 className={styles.cardTitle}>
                      {feature.title}
                    </h3>
                    <p className={styles.cardDescription}>
                      {feature.description}
                    </p>
                  </div>
              ))}
            </div>
          </div>
        </div>
      </section>
  );
};

export default FeaturesSection;

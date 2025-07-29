'use client';

import styles from './FeaturesSection.module.css';

const FeaturesSection = () => {
  const features = [
    {
      icon: '📖',
      title: '성경 읽기',
      description: '다양한 번역본으로 성경을 읽고 개인적인 묵상을 기록해보세요.'
    },
    {
      icon: '💭',
      title: '말씀 묵상',
      description: '일일 말씀과 함께 깊이 있는 묵상의 시간을 가져보세요.'
    },
    {
      icon: '📝',
      title: '기도 노트',
      description: '기도 제목과 응답을 기록하며 하나님과의 관계를 깊게 해보세요.'
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

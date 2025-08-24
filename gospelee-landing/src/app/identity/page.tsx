'use client';

import {useEffect} from 'react';
import Header from '@/components/Header'
import Footer from '@/components/Footer'
import styles from './identity.module.css'

export default function BrandStoryPage() {
  useEffect(() => {
    const observerOptions = {
      threshold: 0.3,
      rootMargin: '0px 0px -100px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add(styles.animate);
        }
      });
    }, observerOptions);

    const sections = document.querySelectorAll(`.${styles.sectionContent}`);
    sections.forEach((section) => observer.observe(section));

    return () => observer.disconnect();
  }, []);

  return (
      <main>
        <Header/>
        <section className={styles.brandStory}>
          <div className="container">

            {/* Hero Section */}
            <div className={styles.hero}>
              <h1 className={styles.mainTitle}>"나눌수록 성장하는 믿음"</h1>
              <p className={styles.heroSubtitle}>믿음은 누군가에게 고백하며 성장한다고 생각합니다</p>
            </div>

            {/* Vision Section */}
            <div className={styles.section}>
              <div className={styles.sectionContent}>
                <h2 className={styles.slogan}>
                  "말씀과 함께하는<br/>
                  새로운 일상"
                </h2>
                <p className={styles.description}>
                  바쁜 현대인들이 말씀과 함께할 수 있도록<br/>
                  돕는 것이 저희의 목표입니다
                </p>
              </div>
            </div>

            {/* Story Section */}
            <div className={styles.section}>
              <div className={styles.sectionContent}>
                <h2 className={styles.slogan}>
                  "더 가깝게, 더 재밌게,<br/>
                  더 깊이 있게"
                </h2>
                <p className={styles.description}>
                  성경을 다양하게 묵상할 수 있고<br/>
                  교회와 함께 삶에 적용할 수 있는 도구를 제공합니다
                </p>
              </div>
            </div>

            {/* Process Section */}
            <div className={styles.section}>
              <div className={styles.sectionContent}>
                <h2 className={styles.slogan}>
                  "함께 만들어가는<br/>
                  기독교 문화"
                </h2>
                <p className={styles.description}>
                  세상의 작은 틈 속에서<br/>
                  하나님의 말씀만이 희망임을 은은히 전파합니다
                </p>
              </div>
            </div>

            {/* Future Section */}
            <div className={styles.section}>
              <div className={styles.sectionContent}>
                <h2 className={styles.slogan}>
                  "세상을 살아가는 실력"
                </h2>
                <p className={styles.description}>
                  세상 속에서도 경쟁력있고 인정받는<br/>
                  그리스도인이 되는 걸 목표합니다
                </p>
              </div>
            </div>
          </div>
        </section>
        <Footer/>
      </main>
  )
}

import styles from './TrustStrip.module.css';

const items = [
  {label: '광고 없음', sub: '배너도 영상 광고도 넣지 않아요'},
  {label: '개척교회 평생 무료', sub: '30명 이하 교회 모바일 주보'},
  {label: '카카오 · 애플 로그인', sub: '가입 절차 없이 바로 시작'},
  {label: 'iOS · Android 출시', sub: 'App Store와 Google Play에서 받아요'},
];

const TrustStrip = () => (
    <section className={styles.strip} aria-label="Podo 한눈에 보기">
      <ul className={`container ${styles.list}`}>
        {items.map((item) => (
            <li key={item.label} className={styles.item}>
              <span className={styles.dot} aria-hidden="true"/>
              <div>
                <strong className={styles.label}>{item.label}</strong>
                <span className={styles.sub}>{item.sub}</span>
              </div>
            </li>
        ))}
      </ul>
    </section>
);

export default TrustStrip;

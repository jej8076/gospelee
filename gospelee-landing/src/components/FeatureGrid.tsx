'use client';

import {ReactNode, useEffect, useRef, useState} from 'react';
import styles from './FeatureGrid.module.css';

// =============================================================================
// 카드별 미니 시연 — 전부 CSS 애니메이션이고, 화면에 보일 때만 재생된다
// =============================================================================

const TypingVisual = () => (
    <div className={styles.verseBox}>
      <span className={styles.verseRef}>요한복음 15:5</span>
      <span className={styles.typeLine}>나는 포도나무요 너희는 가지니</span>
      <span className={styles.typeLine2}>사람이 열매를 많이 맺나니</span>
    </div>
);

const ReadVisual = () => (
    <div className={styles.readBox}>
      <span className={styles.bar} style={{width: '92%'}}/>
      <span className={`${styles.bar} ${styles.marker}`} style={{width: '78%'}}/>
      <span className={styles.bar} style={{width: '86%'}}/>
      <span className={styles.noteChip}>메모 · 오늘 마음에 남은 구절</span>
    </div>
);

const JournalVisual = () => (
    <div className={styles.journalBox}>
      <span className={styles.dateChip}>11월 14일 · 묵상</span>
      <span className={`${styles.bar} ${styles.grow1}`} style={{width: '88%'}}/>
      <span className={`${styles.bar} ${styles.grow2}`} style={{width: '70%'}}/>
      <span className={`${styles.bar} ${styles.grow3}`} style={{width: '45%'}}/>
    </div>
);

const PushVisual = () => (
    <div className={styles.pushStage}>
      <div className={styles.pushCard}>
        <span className={styles.pushHead}>Podo · 지금</span>
        <strong>교회 공지사항</strong>
        <span className={styles.pushBody}>금요 찬양 연습 안내</span>
      </div>
    </div>
);

const BulletinVisual = () => (
    <div className={styles.bStage}>
      <div className={styles.bTile}>
        <span className={styles.bPdf}>PDF</span>
        <div className={styles.bMeta}>
          <span><b className={styles.bLatest}>최신</b>11월 9일 주일</span>
          <strong>주일예배 주보</strong>
        </div>
        <span className={styles.bChevron}>›</span>
      </div>
      <div className={styles.bViewer}>
        <div className={styles.bPage}>
          <span style={{width: '60%'}}/><span/><span/><span style={{width: '80%'}}/><span/>
        </div>
        <span className={styles.bBadge}>1 / 2</span>
      </div>
    </div>
);

const PrayerVisual = () => (
    <div className={styles.drawStage}>
      <div className={styles.dBack}/>
      <div className={styles.dPaper}><span/><span/><span/></div>
      <div className={styles.dFront}>♥</div>
      <span className={styles.dChip}>기도했어요 🙏</span>
    </div>
);

const MinistryVisual = () => (
    <div className={styles.prayStage}>
      <span className={styles.inviteChip}>새벽기도 모임 · 초대 링크</span>
      <div className={styles.avatars}>
        {['민', '서', '준', '하'].map((n, i) => (
            <span key={n} className={styles.join} style={{animationDelay: `${0.9 + i * 0.7}s`}}>{n}</span>
        ))}
      </div>
    </div>
);

const AdminVisual = () => (
    <div className={styles.adminRow}>
      <div>
        <strong>가입 요청</strong>
        <span className={styles.adminSub}>새로 온 성도 1명</span>
      </div>
      <span className={styles.approveBtn}>
        <span className={styles.approveText}>승인</span>
        <span className={styles.approvedText}>승인됨 ✓</span>
      </span>
    </div>
);

// =============================================================================
// 카드 정의
// =============================================================================

const features: { tag: string; title: string; description: string; visual: ReactNode }[] = [
  {
    tag: '말씀',
    title: '손끝으로 새기는 필사',
    description: '한 글자씩 직접 치며 말씀에 머물러요. 오늘 마친 날은 체크로 남아요.',
    visual: <TypingVisual/>,
  },
  {
    tag: '말씀',
    title: '읽다가 바로 필기',
    description: '성경을 읽다가 마음에 남은 구절에 표시하고 메모를 남겨요.',
    visual: <ReadVisual/>,
  },
  {
    tag: '말씀',
    title: '묵상일지',
    description: '읽은 구절과 함께 그날의 묵상을 기록해 두고 다시 꺼내 봐요.',
    visual: <JournalVisual/>,
  },
  {
    tag: '교회',
    title: '공지와 푸시 알림',
    description: '교회 공지가 올라오면 알림이 오고, 누르면 그 공지가 바로 열려요.',
    visual: <PushVisual/>,
  },
  {
    tag: '교회',
    title: '모바일 주보',
    description: '종이 주보 대신, 교회가 올린 주보를 휴대폰에서 넘겨 봐요.',
    visual: <BulletinVisual/>,
  },
  {
    tag: '나눔',
    title: '기도 쪽지',
    description: '기도제목을 쪽지로 올리면, 성도들이 상자에서 한 장 뽑아 함께 기도해 줘요.',
    visual: <PrayerVisual/>,
  },
  {
    tag: '나눔',
    title: '내가 만드는 모임',
    description: '워십팀, 선교단체처럼 교회와 별개로 모임을 만들고 초대 링크로 모아요.',
    visual: <MinistryVisual/>,
  },
  {
    tag: '운영',
    title: '교회 관리',
    description: '가입 요청 승인, 푸시 그룹, 공지 관리까지 앱에서 처리해요.',
    visual: <AdminVisual/>,
  },
];

// =============================================================================
// 섹션
// =============================================================================

/** 화면에 보이는 동안에만 true — 카드 애니메이션이 안 보이는 곳에서 돌지 않게 한다 */
const useInView = () => {
  const ref = useRef<HTMLDivElement>(null);
  const [inView, setInView] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const observer = new IntersectionObserver(([entry]) => setInView(entry.isIntersecting), {threshold: 0.3});
    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  return {ref, inView};
};

const FeatureCard = ({tag, title, description, visual}: typeof features[number]) => {
  const {ref, inView} = useInView();
  return (
      <article className={`${styles.card} ${inView ? styles.live : ''}`} ref={ref}>
        <div className={styles.stage} aria-hidden="true">{visual}</div>
        <span className={styles.tag}>{tag}</span>
        <h3 className={styles.cardTitle}>{title}</h3>
        <p className={styles.cardDesc}>{description}</p>
      </article>
  );
};

const FeatureGrid = () => (
    <section id="features" className={styles.section}>
      <div className="container">
        <div className={styles.header}>
          <span className={styles.sectionTag}>FEATURES</span>
          <h2 className={styles.title}>말씀에서 공동체까지,<br/>하루에 필요한 것만.</h2>
          <p className={styles.subtitle}>화면 속 움직임은 실제 앱의 흐름을 단순하게 옮긴 예시예요.</p>
        </div>
        <div className={styles.grid}>
          {features.map((f) => <FeatureCard key={f.title} {...f}/>)}
        </div>
      </div>
    </section>
);

export default FeatureGrid;

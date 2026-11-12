import styles from './CompareSection.module.css';

const rows = [
  {
    topic: '공지',
    chat: '대화 사이에 묻혀서 다시 찾기 어려워요.',
    podo: '공지만 모아 보고, 푸시로 놓치지 않게 알려 줘요.',
  },
  {
    topic: '주보',
    chat: '이미지나 PDF를 올리고 다시 내려받아야 해요.',
    podo: '교회가 올린 주보를 휴대폰에서 바로 넘겨 봐요.',
  },
  {
    topic: '기도',
    chat: '기도 제목이 대화에 흘러가 버려요.',
    podo: '쪽지로 올리면 성도들이 뽑아서 기도해 줘요. 익명도 가능해요.',
  },
  {
    topic: '말씀',
    chat: '말씀 읽기와 묵상은 다른 앱을 오가야 해요.',
    podo: '읽기, 필사, 필기, 묵상일지가 한 곳에 있어요.',
  },
  {
    topic: '모임',
    chat: '새 모임마다 방을 만들고 사람을 일일이 초대해요.',
    podo: '초대 링크 하나로 모으고, 가입 요청은 승인으로 관리해요.',
  },
];

const CompareSection = () => (
    <section className={styles.section} aria-labelledby="compare-title">
      <div className="container">
        <div className={styles.header}>
          <span className={styles.sectionTag}>WHY PODO</span>
          <h2 id="compare-title" className={styles.title}>그냥 단체 채팅방이면<br/>안 되나요?</h2>
          <p className={styles.subtitle}>채팅방은 대화에 좋고, Podo는 교회와 모임의 일에 맞췄어요.</p>
        </div>

        <div className={styles.table} role="table" aria-label="단체 채팅방과 Podo 비교">
          <div className={`${styles.row} ${styles.head}`} role="row">
            <span role="columnheader"/>
            <span role="columnheader" className={styles.colChat}>단체 채팅방</span>
            <span role="columnheader" className={styles.colPodo}>Podo</span>
          </div>
          {rows.map((row) => (
              <div key={row.topic} className={styles.row} role="row">
                <strong role="rowheader" className={styles.topic}>{row.topic}</strong>
                <span role="cell" className={styles.chat}>{row.chat}</span>
                <span role="cell" className={styles.podo}>{row.podo}</span>
              </div>
          ))}
        </div>
      </div>
    </section>
);

export default CompareSection;

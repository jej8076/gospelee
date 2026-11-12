import styles from './OperatorSection.module.css';

const items = [
  {title: '가입 요청 승인', desc: '새로 온 성도의 가입 요청을 앱에서 확인하고 승인해요.'},
  {title: '공지와 푸시 그룹', desc: '공지를 올리고, 알림을 받을 그룹을 나눠서 보내요.'},
  {title: '모바일 주보 업로드', desc: '교회가 만든 주보 PDF를 올리면 성도들이 휴대폰에서 봐요.'},
];

const OperatorSection = () => (
    <section className={styles.section} aria-labelledby="operator-title">
      <div className={`container ${styles.inner}`}>
        <div>
          <span className={styles.sectionTag}>FOR CHURCH LEADERS</span>
          <h2 id="operator-title" className={styles.title}>교회를 돌보는 분도<br/>앱 하나로 충분해요.</h2>
          <p className={styles.subtitle}>
            따로 시스템을 들이지 않아도, 교회 운영에 필요한 일을 앱에서 바로 처리할 수 있어요.
          </p>
          <ul className={styles.list}>
            {items.map((item) => (
                <li key={item.title} className={styles.item}>
                  <span className={styles.check} aria-hidden="true">✓</span>
                  <div>
                    <strong className={styles.itemTitle}>{item.title}</strong>
                    <span className={styles.itemDesc}>{item.desc}</span>
                  </div>
                </li>
            ))}
          </ul>
        </div>

        <div className={styles.panel} aria-hidden="true">
          <div className={styles.panelHead}>
            <span>교회 관리</span>
            <span>예시 화면</span>
          </div>
          <div className={styles.row}>
            <div>
              <strong>가입 요청</strong>
              <span className={styles.rowSub}>새로 온 성도 1명</span>
            </div>
            <span className={styles.approve}>승인</span>
          </div>
          <div className={styles.row}>
            <div>
              <strong>금요 찬양 연습 안내</strong>
              <span className={styles.rowSub}>공지 · 푸시 발송 완료</span>
            </div>
            <span className={styles.chip}>발송됨</span>
          </div>
          <div className={styles.row}>
            <div>
              <strong>주일예배 주보</strong>
              <span className={styles.rowSub}>PDF · 11월 9일 주일</span>
            </div>
            <span className={styles.chip}>최신</span>
          </div>
        </div>
      </div>
    </section>
);

export default OperatorSection;

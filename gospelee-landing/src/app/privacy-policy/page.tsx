import styles from './PrivacyPolicy.module.css';

const PrivacyPolicyPage = () => {
  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <h1 className={styles.title}>개인정보 처리방침</h1>
        
        <section className={styles.section}>
          <h2>1. 개인정보의 처리 목적</h2>
          <p>Gospelee(이하 "회사")는 다음의 목적을 위하여 개인정보를 처리합니다.</p>
          <ul>
            <li>회원 가입 및 관리</li>
            <li>서비스 제공 및 운영</li>
            <li>고객 문의 및 지원</li>
            <li>서비스 개선 및 개발</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>2. 처리하는 개인정보의 항목</h2>
          <p>회사는 카카오 소셜 로그인을 통해 다음의 개인정보를 수집합니다.</p>
          <ul>
            <li>필수항목: 이름, 전화번호</li>
            <li>자동 수집항목: 서비스 이용 기록, 접속 로그, 쿠키, 접속 IP 정보</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>3. 개인정보의 처리 및 보유 기간</h2>
          <p>회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시에 동의받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.</p>
          <ul>
            <li>회원 탈퇴 시까지</li>
            <li>관계 법령 위반에 따른 수사·조사 등이 진행중인 경우에는 해당 수사·조사 종료 시까지</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>4. 개인정보의 제3자 제공</h2>
          <p>회사는 정보주체의 개인정보를 제1조(개인정보의 처리 목적)에서 명시한 범위 내에서만 처리하며, 정보주체의 동의, 법률의 특별한 규정 등 개인정보 보호법 제17조 및 제18조에 해당하는 경우에만 개인정보를 제3자에게 제공합니다.</p>
        </section>

        <section className={styles.section}>
          <h2>5. 개인정보처리의 위탁</h2>
          <p>회사는 원활한 개인정보 업무처리를 위하여 다음과 같이 개인정보 처리업무를 위탁하고 있습니다.</p>
          <ul>
            <li>위탁받는 자: 카카오</li>
            <li>위탁하는 업무의 내용: 소셜 로그인 서비스 제공</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>6. 정보주체의 권리·의무 및 그 행사방법</h2>
          <p>정보주체는 회사에 대해 언제든지 다음 각 호의 개인정보 보호 관련 권리를 행사할 수 있습니다.</p>
          <ul>
            <li>개인정보 처리정지 요구</li>
            <li>개인정보 열람요구</li>
            <li>개인정보 정정·삭제요구</li>
            <li>개인정보 처리정지 요구</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>7. 개인정보의 안전성 확보조치</h2>
          <p>회사는 개인정보보호법 제29조에 따라 다음과 같이 안전성 확보에 필요한 기술적/관리적 및 물리적 조치를 하고 있습니다.</p>
          <ul>
            <li>개인정보 취급 직원의 최소화 및 교육</li>
            <li>개인정보에 대한 접근 제한</li>
            <li>개인정보의 암호화</li>
            <li>해킹 등에 대비한 기술적 대책</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>8. 개인정보 보호책임자</h2>
          <p>회사는 개인정보 처리에 관한 업무를 총괄해서 책임지고, 개인정보 처리와 관련한 정보주체의 불만처리 및 피해구제 등을 위하여 아래와 같이 개인정보 보호책임자를 지정하고 있습니다.</p>
          <p><strong>개인정보 보호책임자</strong></p>
          <ul>
            <li>성명: 정의진</li>
            <li>연락처: jej8076@gmail.com</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>9. 개인정보 처리방침 변경</h2>
          <p>이 개인정보처리방침은 시행일로부터 적용되며, 법령 및 방침에 따른 변경내용의 추가, 삭제 및 정정이 있는 경우에는 변경사항의 시행 7일 전부터 공지사항을 통하여 고지할 것입니다.</p>
        </section>

        <div className={styles.effectiveDate}>
          시행일자: 2025년 8월 28일
        </div>
      </div>
    </div>
  );
};

export default PrivacyPolicyPage;

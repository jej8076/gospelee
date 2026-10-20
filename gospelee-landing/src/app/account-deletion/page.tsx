import type {Metadata} from 'next';
import styles from '../privacy-policy/PrivacyPolicy.module.css';

export const metadata: Metadata = {
  title: '계정 및 데이터 삭제 안내 — 포도(Podo)',
  description: '포도(Podo) 앱의 계정과 데이터를 삭제하는 방법과 삭제·보관되는 정보를 안내합니다.',
};

const AccountDeletionPage = () => {
  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <h1 className={styles.title}>계정 및 데이터 삭제 안내</h1>

        <section className={styles.section}>
          <p>이 페이지는 <strong>포도(Podo)</strong> 앱(Android 패키지명 org.podo)의 계정과 개인정보를
            삭제하는 방법을 안내합니다. 삭제는 앱에서 직접 할 수 있고, 앱에 로그인할 수 없는 경우에는
            이메일로 요청할 수 있습니다.</p>
        </section>

        <section className={styles.section}>
          <h2>1. 앱에서 직접 삭제하기</h2>
          <ul>
            <li>포도 앱을 열고 로그인한 뒤, 하단의 <strong>설정</strong> 탭으로 이동합니다.</li>
            <li><strong>회원탈퇴</strong>를 누릅니다.</li>
            <li>안내 문구를 확인하고 요청하는 문구를 입력한 뒤 <strong>탈퇴</strong>를 누르면 즉시 처리됩니다.</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>2. 앱에 로그인할 수 없는 경우</h2>
          <p>아래 이메일로 계정 삭제를 요청해 주세요. 가입에 사용한 소셜 계정의 종류(카카오 또는 Apple)와
            가입 이메일을 함께 알려 주시면 본인 확인 후 처리합니다.</p>
          <ul>
            <li>이메일: <a href="mailto:jej8076@gmail.com?subject=포도 계정 삭제 요청">jej8076@gmail.com</a></li>
            <li>제목: 포도 계정 삭제 요청</li>
            <li>처리 기간: 본인 확인 후 영업일 기준 7일 이내</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>3. 삭제되는 정보</h2>
          <p>탈퇴하면 다음 정보가 즉시 삭제됩니다.</p>
          <ul>
            <li>이름, 닉네임, 전화번호, 이메일 등 계정 정보와 로그인 연동 정보</li>
            <li>푸시 알림 토큰</li>
            <li>작성한 묵상 일지</li>
            <li>성경 읽기·필기 기록과 통독 참여 기록</li>
            <li>공유받은 묵상 목록, 차단한 사용자 목록</li>
            <li>교회 가입 및 승인 이력</li>
            <li>Apple 계정으로 가입한 경우 Apple 로그인 연결(토큰 폐기)</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>4. 익명으로 남는 정보</h2>
          <p>다른 사용자에게 이미 공유한 <strong>나눔 묵상</strong>과 그 묵상에 남긴 <strong>댓글</strong>은
            삭제되지 않고 남을 수 있습니다. 이때 작성자는 계정 정보와 완전히 분리되어 <strong>"탈퇴한 사용자"</strong>로만
            표시되며, 작성자를 알아볼 수 없습니다.</p>
          <p>남기고 싶지 않은 콘텐츠는 탈퇴하기 전에 앱에서 먼저 지워 주세요.</p>
          <ul>
            <li>나눔 묵상: 내가 공유한 묵상의 공유 화면에서 <strong>공유 중지</strong>를 누릅니다.</li>
            <li>댓글: 내가 쓴 댓글의 메뉴(⋯)에서 <strong>삭제</strong>를 누릅니다.</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>5. 일정 기간 보관되는 정보</h2>
          <ul>
            <li>다른 사용자 콘텐츠에 대한 신고 내역: 서비스 운영과 분쟁 대응을 위해 작성자 식별 정보 없이 보관합니다.</li>
            <li>서버 접속 기록 등 관계 법령에 따라 보존해야 하는 정보: 해당 법령이 정한 기간 동안만 보관한 뒤 삭제합니다.</li>
          </ul>
        </section>

        <section className={styles.section}>
          <h2>6. 소셜 로그인 연결 해제</h2>
          <p>카카오 계정으로 가입한 경우 앱에서 탈퇴해도 카카오 쪽의 서비스 연결 기록은 남아 있을 수 있습니다.
            카카오톡 또는 카카오계정 사이트의 <strong>연결된 서비스 관리</strong>에서 "포도"의 연결을 직접 끊을 수 있습니다.
            Apple 계정은 탈퇴 시 연결이 함께 해제됩니다.</p>
        </section>

        <section className={styles.section}>
          <h2>7. 같은 계정으로 다시 가입하면</h2>
          <p>탈퇴 후 같은 소셜 계정으로 다시 로그인하면 새 계정으로 가입되며, 삭제된 데이터는 복구되지 않습니다.</p>
        </section>

        <div className={styles.effectiveDate}>
          시행일자: 2026년 10월 20일
        </div>
      </div>
    </div>
  );
};

export default AccountDeletionPage;

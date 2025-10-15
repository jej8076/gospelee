'use client';

import Header from '@/components/Header';
import Footer from '@/components/Footer';
import styles from './Support.module.css';

const supportItems = [
  // {
  //   title: '자주 묻는 질문',
  //   description: '일반적인 질문과 답변을 확인하세요',
  //   icon: '❓',
  //   content: [
  //     {
  //       question: '비밀번호를 잊어버렸어요',
  //       answer: '관리자에게 문의하여 비밀번호 재설정을 요청하세요.'
  //     },
  //     {
  //       question: '공지사항 작성 시 파일 업로드가 안돼요',
  //       answer: '파일 크기는 10MB 이하여야 하며, 지원되는 형식은 PDF, DOC, DOCX, JPG, PNG입니다.'
  //     },
  //     {
  //       question: 'QR 코드 로그인이 작동하지 않아요',
  //       answer: 'QR 코드는 5분간 유효합니다. 새로운 QR 코드를 생성해 주세요.'
  //     }
  //   ]
  // },
  {
    title: '문제 신고',
    description: '시스템 오류나 버그를 신고해 주세요',
    icon: '⚠️',
    content: [
      {
        question: '버그 신고 방법',
        answer: 'jej8076@gmail.com으로 문제 상황과 스크린샷을 함께 보내주세요.'
      }
    ]
  },
  // {
  //   title: '사용 가이드',
  //   description: '시스템 사용법을 확인하세요',
  //   icon: '📖',
  //   content: [
  //     {
  //       question: '공지사항 작성하기',
  //       answer: '관리 > 공지사항 관리에서 새 공지사항을 작성할 수 있습니다.'
  //     },
  //     {
  //       question: '파일 첨부하기',
  //       answer: '공지사항 작성 시 하단의 파일 첨부 버튼을 클릭하여 파일을 업로드하세요.'
  //     }
  //   ]
  // },
  {
    title: '문의하기',
    description: '직접 문의사항을 보내주세요',
    icon: '💬',
    content: [
      {
        question: '이메일 문의',
        answer: 'jej8076@gmail.com으로 문의해 주세요.'
      },
      {
        question: '응답 시간',
        answer: '평일 기준 24시간 이내에 답변드립니다.'
      }
    ]
  }
];

export default function Support() {
  return (
      <main>
        <Header/>
        <section className={styles.support}>
          <div className="container">
            <div className={styles.header}>
              <h1 className={styles.title}>고객 지원</h1>
              <p className={styles.subtitle}>
                시스템 사용에 도움이 필요하시면 아래 정보를 참고하세요
              </p>
            </div>

            <div className={styles.grid}>
              {supportItems.map((item) => (
                  <div key={item.title} className={styles.card}>
                    <div className={styles.cardHeader}>
                      <div className={styles.iconWrapper}>
                        <span className={styles.icon}>{item.icon}</span>
                      </div>
                      <div className={styles.cardInfo}>
                        <h3 className={styles.cardTitle}>{item.title}</h3>
                        <p className={styles.cardDescription}>{item.description}</p>
                      </div>
                    </div>

                    <div className={styles.content}>
                      {item.content.map((faq, index) => (
                          <div key={index} className={styles.faqItem}>
                            <dt className={styles.question}>{faq.question}</dt>
                            <dd className={styles.answer}>{faq.answer}</dd>
                          </div>
                      ))}
                    </div>
                  </div>
              ))}
            </div>

            <div className={styles.systemInfo}>
              <h2 className={styles.systemTitle}>시스템 정보</h2>
              <div className={styles.infoGrid}>
                <div className={styles.infoItem}>
                  <dt className={styles.infoLabel}>버전</dt>
                  <dd className={styles.infoValue}>v1.0.0</dd>
                </div>
                <div className={styles.infoItem}>
                  <dt className={styles.infoLabel}>마지막 업데이트</dt>
                  <dd className={styles.infoValue}>2025년 10월 15일</dd>
                </div>
              </div>
            </div>
          </div>
        </section>
        <Footer/>
      </main>
  );
}

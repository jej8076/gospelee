import Header from '@/components/Header'
import Footer from '@/components/Footer'
import styles from './developer.module.css'

export default function DeveloperPage() {
  return (
    <main>
      <Header />
      <section className={styles.developer}>
        <div className="container">
          <div className={styles.content}>
            <h1 className={styles.title}>개발자 이야기</h1>
            <div className={styles.story}>
              <div className={styles.intro}>
                <h2>Gospelee를 만든 이유</h2>
                <p>
                  안녕하세요, Gospelee 개발팀입니다. 저희는 성경을 더 가깝게, 더 깊이 있게 
                  읽을 수 있는 방법을 고민하다가 이 앱을 만들게 되었습니다.
                </p>
              </div>
              
              <div className={styles.section}>
                <h3>우리의 비전</h3>
                <p>
                  현대인들이 바쁜 일상 속에서도 하나님의 말씀과 함께할 수 있도록 돕는 것이 
                  저희의 목표입니다. 단순히 성경을 읽는 것을 넘어서, 말씀을 묵상하고 
                  실천할 수 있는 도구를 제공하고자 합니다.
                </p>
              </div>
              
              <div className={styles.section}>
                <h3>개발 과정</h3>
                <p>
                  많은 크리스천 개발자들과 목회자들의 조언을 받아가며 개발했습니다. 
                  사용자의 피드백을 소중히 여기며, 지속적으로 개선해 나가고 있습니다.
                </p>
              </div>
              
              <div className={styles.section}>
                <h3>앞으로의 계획</h3>
                <p>
                  더 많은 기능과 콘텐츠를 추가하여, 성경 읽기가 더욱 풍성한 경험이 
                  될 수 있도록 노력하겠습니다. 여러분의 소중한 의견을 기다립니다.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>
      <Footer />
    </main>
  )
}

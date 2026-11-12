'use client';

import {useCallback, useEffect, useRef, useState} from 'react';
import Image from 'next/image';
import styles from './DemoPhone.module.css';

// =============================================================================
// 장면 정의 — 모든 장면은 하나의 시계(ms)에서 파생되는 순수 함수로 그린다
// =============================================================================

type SceneId = 'typing' | 'notice' | 'prayer' | 'ministry';

const SCENES: { id: SceneId; label: string; duration: number }[] = [
  {id: 'typing', label: '말씀 필사', duration: 9000},
  {id: 'notice', label: '공지·푸시', duration: 8000},
  {id: 'prayer', label: '기도 쪽지', duration: 8500},
  {id: 'ministry', label: '모임', duration: 9500},
];

const TICK = 50;

const VERSE = '나는 포도나무요 너희는 가지니 사람이 내 안에, 내가 그 안에 거하면 사람이 열매를 많이 맺나니';
const PRAYER = '면접을 앞두고 있어요. 함께 기도해 주세요.';

/** 시작 시각(from) 이후 interval 마다 한 글자씩 보여 줄 글자 수 */
const typedCount = (ms: number, from: number, interval: number, max: number) =>
    Math.max(0, Math.min(max, Math.floor((ms - from) / interval)));

// =============================================================================
// 공통 조각
// =============================================================================

const StatusBar = ({dark = false}: { dark?: boolean }) => (
    <div className={`${styles.statusBar} ${dark ? styles.statusDark : ''}`}>
      <span className={styles.statusTime}>9:41</span>
      <div className={styles.dynamicIsland}/>
      <div className={styles.statusIcons}>
        <span>5G</span>
        <div className={styles.batteryIcon}/>
      </div>
    </div>
);

/** 손가락으로 누르는 느낌의 물결. 보이는 순간부터 한 번 재생된다 */
const Tap = ({show, x, y}: { show: boolean; x: string; y: string }) =>
    show ? <span className={styles.tap} style={{left: x, top: y}}/> : null;

// =============================================================================
// 1. 말씀 필사
// =============================================================================

const TypingScene = ({ms}: { ms: number }) => {
  const n = typedCount(ms, 700, 62, VERSE.length);
  const done = n >= VERSE.length;
  const percent = Math.round((n / VERSE.length) * 100);

  return (
      <div className={styles.scene}>
        <div className={styles.appHeader}>
          <div className={styles.appBrand}>
            <Image src="/images/logo/podo_logo.svg" alt="" width={22} height={18}/>
            <span className={styles.appName}>Podo</span>
          </div>
          <span className={styles.modeBadge}>필사 모드</span>
        </div>

        <div className={styles.card}>
          <div className={styles.verseMeta}>
            <span className={styles.verseBook}>요한복음 15:5</span>
            <span className={styles.verseVersion}>개역개정</span>
          </div>
          <p className={styles.verse}>
            <span className={styles.typed}>{VERSE.slice(0, n)}</span>
            {!done && <span className={styles.cursor}/>}
            <span className={styles.rest}>{VERSE.slice(n)}</span>
          </p>
        </div>

        <div className={styles.progressRow}>
          <div className={styles.progressTrack}>
            <div className={styles.progressFill} style={{width: `${percent}%`}}/>
          </div>
          <span className={styles.progressText}>{percent}%</span>
        </div>

        <div className={`${styles.doneChip} ${done ? styles.show : ''}`}>
          <span className={styles.check}>✓</span> 오늘의 필사를 마쳤어요
        </div>

        <div className={styles.week}>
          {['월', '화', '수', '목', '금', '토', '일'].map((day, i) => (
              <div key={day} className={styles.weekDay}>
                <span className={`${styles.weekDot} ${i < 3 || (i === 3 && done) ? styles.weekDotOn : ''}`}>
                  {i < 3 || (i === 3 && done) ? '✓' : ''}
                </span>
                <span className={styles.weekLabel}>{day}</span>
              </div>
          ))}
        </div>

        <p className={styles.caption}>광고 없이, 말씀에만 머무는 시간</p>
      </div>
  );
};

// =============================================================================
// 2. 공지 + 푸시
// =============================================================================

const NoticeScene = ({ms}: { ms: number }) => {
  const showDetail = ms >= 3800;
  const pushIn = ms >= 900 && !showDetail;

  if (!showDetail) {
    return (
        <div className={`${styles.scene} ${styles.lock}`}>
          <div className={styles.lockClock}>
            <span className={styles.lockDate}>11월 14일 금요일</span>
            <span className={styles.lockTime}>9:41</span>
          </div>
          <div className={`${styles.push} ${pushIn ? styles.pushIn : ''}`}>
            <div className={styles.pushHead}>
              <Image src="/images/logo/podo_logo.svg" alt="" width={14} height={12}/>
              <span>Podo</span>
              <span className={styles.pushWhen}>지금</span>
            </div>
            <strong className={styles.pushTitle}>[청년부] 공지사항</strong>
            <span className={styles.pushBody}>금요 찬양 연습은 7시에 시작해요</span>
          </div>
          <Tap show={ms >= 3200} x="50%" y="252px"/>
        </div>
    );
  }

  const read = ms >= 6000;
  return (
      <div className={`${styles.scene} ${styles.slideIn}`}>
        <div className={styles.navBar}>
          <span className={styles.back}>‹</span>
          <span className={styles.navTitle}>청년부 공지</span>
        </div>
        <div className={styles.card}>
          <div className={styles.author}>
            <span className={styles.avatar}>찬</span>
            <div>
              <strong>찬양팀 리더</strong>
              <span className={styles.muted}>방금</span>
            </div>
          </div>
          <h3 className={styles.noticeTitle}>금요 찬양 연습 안내</h3>
          <p className={styles.noticeBody}>
            이번 주 금요일은 7시에 시작해요. 악보는 단체 공유 폴더에 올려 두었으니 미리 한 번씩 들어 와 주세요.
          </p>
        </div>
        <div className={`${styles.readChip} ${read ? styles.show : ''}`}>
          <span className={styles.check}>✓</span> 확인했어요
        </div>
        <div className={`${styles.card} ${styles.oldCard}`}>
          <strong className={styles.oldTitle}>주일 오후 소그룹 모임 장소 변경</strong>
          <span className={styles.muted}>지난 주 · 확인함</span>
        </div>
      </div>
  );
};

// =============================================================================
// 3. 기도 쪽지
// =============================================================================

const PrayerScene = ({ms}: { ms: number }) => {
  const n = typedCount(ms, 600, 85, PRAYER.length);
  const sent = ms >= 3700;
  const joined = [4600, 5600, 6600].filter((t) => ms >= t).length;
  const names = ['김', '이', '박'];

  return (
      <div className={styles.scene}>
        <div className={styles.navBar}>
          <span className={styles.navTitle}>기도 쪽지</span>
          <span className={styles.modeBadge}>금요 청년 기도</span>
        </div>

        <div className={`${styles.card} ${sent ? styles.cardSent : ''}`}>
          <p className={styles.noteText}>
            {PRAYER.slice(0, n)}
            {!sent && <span className={styles.cursor}/>}
          </p>
          <button
              type="button"
              tabIndex={-1}
              className={`${styles.sendBtn} ${ms >= 3000 && !sent ? styles.pressed : ''} ${sent ? styles.sendBtnDone : ''}`}
          >
            {sent ? '전달했어요 ✓' : '그룹에 전달하기'}
          </button>
        </div>
        <Tap show={ms >= 3300 && ms < 4300} x="50%" y="236px"/>

        <div className={`${styles.prayers} ${sent ? styles.show : ''}`}>
          <div className={styles.avatars}>
            {names.map((name, i) => (
                <span key={name} className={`${styles.avatar} ${i < joined ? styles.avatarOn : ''}`}>
              {name}
            </span>
            ))}
          </div>
          <span className={styles.prayText}>
          {joined === 0 ? '곧 함께 기도해 줄 거예요' : `${joined}명이 함께 기도하고 있어요`}
        </span>
        </div>

        <p className={styles.caption}>혼자 짊어지지 않도록</p>
      </div>
  );
};

// =============================================================================
// 4. 모임
// =============================================================================

const MinistryScene = ({ms}: { ms: number }) => {
  const joinScreen = ms >= 3300;

  if (!joinScreen) {
    return (
        <div className={styles.scene}>
          <div className={styles.navBar}>
            <span className={styles.back}>‹</span>
            <span className={styles.navTitle}>민수</span>
          </div>
          <div className={styles.chat}>
            <div className={styles.bubble}>다들 새벽에 같이 말씀 읽어 볼래요?</div>
            <div className={`${styles.bubble} ${styles.bubbleMine}`}>좋아요! 어디서 모여요?</div>
            <div className={styles.bubble}>새벽기도 모임에 초대할게요 🙏</div>
            <div className={`${styles.inviteCard} ${ms >= 2600 ? styles.pressed : ''}`}>
              <span className={styles.inviteIcon}>
                <Image src="/images/logo/podo_logo.svg" alt="" width={18} height={15}/>
              </span>
              <div>
                <strong>새벽기도 모임</strong>
                <span className={styles.muted}>Podo 모임 초대 링크</span>
              </div>
            </div>
          </div>
          <Tap show={ms >= 2600} x="34%" y="258px"/>
        </div>
    );
  }

  const requested = ms >= 5200;
  const approved = ms >= 7000;
  return (
      <div className={`${styles.scene} ${styles.slideIn}`}>
        <div className={styles.navBar}>
          <span className={styles.back}>‹</span>
          <span className={styles.navTitle}>모임 가입</span>
        </div>
        <div className={styles.joinHero}>
          <span className={styles.joinIcon}>새</span>
          <strong className={styles.joinName}>새벽기도 모임</strong>
          <span className={styles.muted}>매일 새벽 5시 · 말씀 나눔과 기도</span>
          <span className={styles.members}>멤버 12명</span>
        </div>
        <button
            type="button"
            tabIndex={-1}
            className={`${styles.joinBtn} ${ms >= 4700 && !requested ? styles.pressed : ''} ${approved ? styles.joinBtnDone : ''}`}
        >
          {approved ? '모임에 들어왔어요 ✓' : requested ? '승인을 기다리고 있어요' : '가입 요청하기'}
        </button>
        <Tap show={ms >= 4900 && ms < 5800} x="50%" y="294px"/>
        <div className={`${styles.toast} ${approved ? styles.show : ''}`}>가입이 승인됐어요</div>

        <p className={styles.caption}>작은 모임도 교회처럼 모일 수 있게</p>
      </div>
  );
};

// =============================================================================
// 데모 폰 본체
// =============================================================================

const DemoPhone = () => {
  const [index, setIndex] = useState(0);
  const [ms, setMs] = useState(0);
  const [hovered, setHovered] = useState(false);
  const [visible, setVisible] = useState(true);
  const [reduced, setReduced] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);

  const scene = SCENES[index];

  // 모션 줄이기 설정이면 자동 재생 없이 각 장면의 마지막 모습을 정지해서 보여 준다
  useEffect(() => {
    const query = window.matchMedia('(prefers-reduced-motion: reduce)');
    const apply = () => {
      setReduced(query.matches);
      if (query.matches) setMs(SCENES[index].duration - 1);
    };
    apply();
    query.addEventListener('change', apply);
    return () => query.removeEventListener('change', apply);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 화면 밖이거나 다른 탭이면 멈춘다
  useEffect(() => {
    const el = rootRef.current;
    if (!el) return;
    const observer = new IntersectionObserver(([entry]) => setVisible(entry.isIntersecting), {threshold: 0.2});
    observer.observe(el);
    const onVisibility = () => setVisible(!document.hidden);
    document.addEventListener('visibilitychange', onVisibility);
    return () => {
      observer.disconnect();
      document.removeEventListener('visibilitychange', onVisibility);
    };
  }, []);

  const running = !reduced && visible && !hovered;

  useEffect(() => {
    if (!running) return;
    const id = setInterval(() => setMs((m) => m + TICK), TICK);
    return () => clearInterval(id);
  }, [running]);

  // 장면이 끝나면 다음 장면으로 넘어간다
  useEffect(() => {
    if (reduced || ms < scene.duration) return;
    setIndex((i) => (i + 1) % SCENES.length);
    setMs(0);
  }, [ms, scene.duration, reduced]);

  const select = useCallback((i: number) => {
    setIndex(i);
    setMs(reduced ? SCENES[i].duration - 1 : 0);
  }, [reduced]);

  return (
      <div className={styles.wrapper} ref={rootRef}>
        <div
            className={styles.deviceFrame}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
            role="img"
            aria-label={`Podo 앱 예시 화면: ${scene.label}`}
        >
          <div className={styles.deviceScreen} aria-hidden="true">
            <StatusBar dark={scene.id === 'notice' && ms < 3800}/>
            {scene.id === 'typing' && <TypingScene ms={ms}/>}
            {scene.id === 'notice' && <NoticeScene ms={ms}/>}
            {scene.id === 'prayer' && <PrayerScene ms={ms}/>}
            {scene.id === 'ministry' && <MinistryScene ms={ms}/>}
          </div>
        </div>

        <div className={styles.tabs} role="tablist" aria-label="앱 기능 미리보기">
          {SCENES.map((s, i) => (
              <button
                  key={s.id}
                  type="button"
                  role="tab"
                  aria-selected={i === index}
                  className={`${styles.tab} ${i === index ? styles.tabActive : ''}`}
                  onClick={() => select(i)}
              >
                {s.label}
                <span
                    className={styles.tabBar}
                    style={{width: i === index ? `${Math.min(100, (ms / s.duration) * 100)}%` : '0%'}}
                />
              </button>
          ))}
        </div>
        <span className={styles.sample}>예시 화면 · 실제 앱과 일부 다를 수 있어요</span>
      </div>
  );
};

export default DemoPhone;

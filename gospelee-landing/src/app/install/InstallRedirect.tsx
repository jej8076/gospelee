'use client';

import {useEffect} from 'react';
import styles from './Install.module.css';

const APP_STORE_URL = 'https://apps.apple.com/app/id6766432856';
const PLAY_STORE_URL = 'https://play.google.com/store/apps/details?id=org.podo';

/// 기기 OS에 맞는 스토어 주소. 판별할 수 없으면(PC 등) null
const detectStoreUrl = (): string | null => {
  const ua = navigator.userAgent;
  if (/android/i.test(ua)) return PLAY_STORE_URL;
  if (/iPhone|iPad|iPod/i.test(ua)) return APP_STORE_URL;
  // iPadOS 13+ 사파리는 맥으로 보고하므로 터치 지원 여부로 구분한다
  if (/Macintosh/i.test(ua) && navigator.maxTouchPoints > 1) return APP_STORE_URL;
  return null;
};

const InstallRedirect = () => {
  useEffect(() => {
    const url = detectStoreUrl();
    // 뒤로 가기로 이 페이지에 다시 갇히지 않도록 replace 로 이동한다
    if (url) window.location.replace(url);
  }, []);

  // 자동 이동이 안 되는 환경(PC, 일부 인앱 브라우저)을 위해 두 스토어 버튼을 함께 보여 준다
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>포도(Podo) 설치하기</h1>
      <p className={styles.desc}>스토어로 이동하지 않으면 아래에서 기기에 맞는 스토어를 눌러 주세요.</p>
      <div className={styles.buttons}>
        <a href={APP_STORE_URL} className={styles.button}>App Store에서 받기</a>
        <a href={PLAY_STORE_URL} className={styles.button}>Google Play에서 받기</a>
      </div>
    </div>
  );
};

export default InstallRedirect;

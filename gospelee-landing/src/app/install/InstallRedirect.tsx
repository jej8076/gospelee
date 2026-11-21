'use client';

import {useEffect, useState} from 'react';
import styles from './Install.module.css';

const APP_STORE_URL = 'https://apps.apple.com/app/id6766432856';
const PLAY_STORE_URL = 'https://play.google.com/store/apps/details?id=org.podo';

// App Store Connect 캠페인 링크의 제공자 토큰(pt). 앱 분석 > 캠페인 링크 생성 화면의 주소에 나오는 숫자.
// 비어 있으면 App Store 쪽은 추적 값 없이 보낸다(pt 없이 ct 만 붙이면 애플이 집계하지 않음)
const APP_STORE_PROVIDER_TOKEN = '';

type StoreUrls = {appStore: string; playStore: string};

/// 주소에 붙일 추적 값만 남긴다(영문·숫자·_·- 만, 길이 제한)
const cleanToken = (value: string | null): string | null => {
  if (!value) return null;
  const cleaned = value.replace(/[^A-Za-z0-9_-]/g, '').slice(0, 30);
  return cleaned || null;
};

/// /install?utm_source=instagram&utm_campaign=reels 처럼 들어온 추적 값을 각 스토어 형식으로 옮긴다
/// - 플레이스토어: referrer 파라미터(설치 리퍼러)로 utm 값을 넘겨 Play Console·Firebase 에서 경로별 설치를 센다
/// - 앱스토어: 캠페인 링크(pt·ct)로 App Store Connect 앱 분석에서 캠페인별 설치를 센다. ct 는 출처_캠페인
const buildStoreUrls = (search: string): StoreUrls => {
  const params = new URLSearchParams(search);
  const source = cleanToken(params.get('utm_source'));
  const campaign = cleanToken(params.get('utm_campaign'));
  if (!source && !campaign) return {appStore: APP_STORE_URL, playStore: PLAY_STORE_URL};

  const referrer = new URLSearchParams();
  if (source) referrer.set('utm_source', source);
  if (campaign) referrer.set('utm_campaign', campaign);
  const playStore = `${PLAY_STORE_URL}&referrer=${encodeURIComponent(referrer.toString())}`;

  let appStore = APP_STORE_URL;
  if (APP_STORE_PROVIDER_TOKEN) {
    const ct = [source, campaign].filter(Boolean).join('_');
    appStore = `${APP_STORE_URL}?pt=${APP_STORE_PROVIDER_TOKEN}&ct=${ct}&mt=8`;
  }
  return {appStore, playStore};
};

/// 기기 OS에 맞는 스토어 주소. 판별할 수 없으면(PC 등) null
const detectStoreUrl = (urls: StoreUrls): string | null => {
  const ua = navigator.userAgent;
  if (/android/i.test(ua)) return urls.playStore;
  if (/iPhone|iPad|iPod/i.test(ua)) return urls.appStore;
  // iPadOS 13+ 사파리는 맥으로 보고하므로 터치 지원 여부로 구분한다
  if (/Macintosh/i.test(ua) && navigator.maxTouchPoints > 1) return urls.appStore;
  return null;
};

const InstallRedirect = () => {
  const [urls, setUrls] = useState<StoreUrls>({appStore: APP_STORE_URL, playStore: PLAY_STORE_URL});

  useEffect(() => {
    const storeUrls = buildStoreUrls(window.location.search);
    setUrls(storeUrls);
    const url = detectStoreUrl(storeUrls);
    // 뒤로 가기로 이 페이지에 다시 갇히지 않도록 replace 로 이동한다
    if (url) window.location.replace(url);
  }, []);

  // 자동 이동이 안 되는 환경(PC, 일부 인앱 브라우저)을 위해 두 스토어 버튼을 함께 보여 준다
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>포도(Podo) 설치하기</h1>
      <p className={styles.desc}>스토어로 이동하지 않으면 아래에서 기기에 맞는 스토어를 눌러 주세요.</p>
      <div className={styles.buttons}>
        <a href={urls.appStore} className={styles.button}>App Store에서 받기</a>
        <a href={urls.playStore} className={styles.button}>Google Play에서 받기</a>
      </div>
    </div>
  );
};

export default InstallRedirect;

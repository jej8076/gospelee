import type {Metadata} from 'next';
import InstallRedirect from './InstallRedirect';

export const metadata: Metadata = {
  title: '포도(Podo) 설치하기',
  description: '사용 중인 기기에 맞는 스토어에서 포도(Podo) 앱을 설치합니다.',
};

// 랜딩 QR 코드가 가리키는 주소. 기기 OS를 보고 앱스토어 또는 플레이스토어로 보낸다
const InstallPage = () => <InstallRedirect/>;

export default InstallPage;

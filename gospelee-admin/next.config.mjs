/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  // 푸시 알림 화면을 /manage/push 하나로 합치기 전 주소로 들어와도 열리게 한다
  async redirects() {
    return [
      {source: '/manage/push-send', destination: '/manage/push?compose=1', permanent: false},
      {source: '/manage/push-history', destination: '/manage/push', permanent: false},
      {source: '/manage/push-group', destination: '/manage/push?tab=groups', permanent: false},
    ];
  },
};

export default nextConfig;

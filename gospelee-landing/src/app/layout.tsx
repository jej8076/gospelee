import type {Metadata, Viewport} from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Podo (포도) — 말씀과 교회를 잇는 미니멀 신앙 도구',
  description: '1인 개발자가 기독교 문화를 위해 빚어가는 말씀 필사 & 모바일 주보 신앙 플랫폼',
  keywords: '성경, 필사, 묵상, 주보, 교회, 크리스천, Podo, 포도, Gospelee',
  authors: [{name: 'ej (Indie Developer)'}],
  robots: 'index, follow',
  metadataBase: new URL('https://landing.podo.kr'),
  openGraph: {
    title: 'Podo (포도) — 말씀과 교회를 잇는 미니멀 신앙 도구',
    description: '1인 개발자가 기독교 문화를 위해 빚어가는 말씀 필사 & 모바일 주보 신앙 플랫폼',
    type: 'website',
    locale: 'ko_KR',
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
}

export default function RootLayout({
                                     children,
                                   }: {
  children: React.ReactNode
}) {
  return (
      <html lang="ko">
      <head>
        {/* 폰트 프리로드 */}
        <link
            rel="preload"
            href="/fonts/pretendard/Pretendard-Regular.otf"
            as="font"
            type="font/otf"
            crossOrigin="anonymous"
        />
        <link
            rel="preload"
            href="/fonts/pretendard/Pretendard-SemiBold.otf"
            as="font"
            type="font/otf"
            crossOrigin="anonymous"
        />
        <link
            rel="preload"
            href="/fonts/pretendard/Pretendard-Bold.otf"
            as="font"
            type="font/otf"
            crossOrigin="anonymous"
        />
      </head>
      <body>{children}</body>
      </html>
  )
}

import type {Metadata, Viewport} from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'OOG',
  description: '우리는 교회, 교회는 우리',
  keywords: '성경, 말씀, 묵상, 기도, 크리스천, 앱',
  authors: [{name: 'Gospelee Team'}],
  robots: 'index, follow',
  metadataBase: new URL('https://landing.oog.com'),
  openGraph: {
    title: 'OOG',
    description: '우리는 교회, 교회는 우리',
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

import Header from '@/components/Header'
import HeroSection from '@/components/HeroSection'
import TrustStrip from '@/components/TrustStrip'
import FeatureGrid from '@/components/FeatureGrid'
import CompareSection from '@/components/CompareSection'
import FeaturesSection from '@/components/FeaturesSection'
import DownloadSection from '@/components/DownloadSection'
import Footer from '@/components/Footer'

export default function Home() {
  return (
    <main>
      <Header />
      <HeroSection />
      <TrustStrip />
      <FeatureGrid />
      <CompareSection />
      <FeaturesSection />
      <DownloadSection />
      <Footer />
    </main>
  )
}

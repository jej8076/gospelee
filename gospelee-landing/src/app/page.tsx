import Header from '@/components/Header'
import HeroSection from '@/components/HeroSection'
import TrustStrip from '@/components/TrustStrip'
import FeatureGrid from '@/components/FeatureGrid'
import CompareSection from '@/components/CompareSection'
import OperatorSection from '@/components/OperatorSection'
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
      <OperatorSection />
      <FeaturesSection />
      <DownloadSection />
      <Footer />
    </main>
  )
}

export const layoutFadeStyle = (fadeState: string) => {
  const pageTransitionClass = "transition-opacity opacity-0";
  const defaultClasses = 'px-4 sm:px-6 lg:px-8 ' + pageTransitionClass;
  const fadeOutClass = " opacity-0";
  const fadeInClass = " opacity-100";
  switch (fadeState) {
    case 'hidden':
      return defaultClasses + fadeOutClass;
    case 'visible':
      return defaultClasses + fadeInClass;
    default:
      return defaultClasses;
  }
}

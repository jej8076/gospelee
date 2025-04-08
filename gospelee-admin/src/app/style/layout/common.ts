export const layoutFadeStyle = (
    state: 'hidden' | 'visible',
    duration: 150 | 200 | 300 | 500 | 700 | 1000 = 300
): string => {
  return state === 'visible'
      ? `opacity-100 transition-opacity duration-${duration} ease-in`
      : `opacity-0 transition-opacity duration-${duration} ease-out`;
};

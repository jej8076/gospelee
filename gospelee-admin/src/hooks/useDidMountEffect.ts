import {useEffect, useState} from 'react';

const useDidMountEffect = (func: () => void, deps: React.DependencyList) => {
  const [isFirstRender, setIsFirstRender] = useState(true);

  useEffect(() => {
    if (isFirstRender) {
      setIsFirstRender(false);
      return;
    }
    func();
  }, deps);
};

export default useDidMountEffect;

type Ecclesia = {
  ecclesiaUid: bigint,
  churchIdentificationNumber: string,
  status: string,
  ecclesiaName: string,
  masterAccountName: string,
  image: string,
  insertTime: string,
};

type EcclesiaStatusSelectorProps = {
  ecclesiaUid: bigint;
  status: string;
};

// 수정된 데이터를 상위 이미 불러와져있는 목록을 업데이트 하기위해 사용
interface EcclesiaStatusSelectorPropsWithCallback extends EcclesiaStatusSelectorProps {
  onStatusChange?: (newStatus: string) => void;
}

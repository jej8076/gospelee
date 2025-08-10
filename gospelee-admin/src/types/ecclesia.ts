type Ecclesia = {
  uid: bigint,
  churchIdentificationNumber: string,
  status: string,
  name: string,
  masterAccountUid: number;
  masterAccountName: string,
  telephone: string;
  image: string,
  insertTime: string,
  seniorPastorName?: string,
  churchAddress?: string,
};

type EcclesiaStatusSelectorProps = {
  ecclesiaUid: bigint;
  status: string;
};

type EcclesiaSeniorPastorNameSelectorProps = {
  ecclesiaUid: bigint;
  seniorPastorName: string;
};

type EcclesiaChurchAddressSelectorProps = {
  ecclesiaUid: bigint;
  churchAddress: string;
};

// 수정된 데이터를 상위 이미 불러와져있는 목록을 업데이트 하기위해 사용
interface EcclesiaStatusSelectorPropsWithCallback extends EcclesiaStatusSelectorProps {
  onStatusChange?: (newStatus: string) => void;
}

interface EcclesiaSeniorPastorNameSelectorPropsWithCallback extends EcclesiaSeniorPastorNameSelectorProps {
  onSeniorPastorNameChange?: (newSeniorPastorName: string) => void;
}

interface EcclesiaChurchAddressSelectorPropsWithCallback extends EcclesiaChurchAddressSelectorProps {
  onChurchAddressChange?: (newChurchAddress: string) => void;
}

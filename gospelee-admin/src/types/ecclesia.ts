type Ecclesia = {
  ecclesiaUid: bigint,
  churchIdentificationNumber: string,
  status: string,
  ecclesiaName: string,
  masterAccountName: string,
  insertTime: string,
};

type EcclesiaStatusSelectorProps = {
  ecclesiaUid: number;
  currentStatus: string;
};
import {EcclesiaStatus} from "@/enums/ecclesia/status";

export const ecclesiaStatusStyle = (status: string) => {
  const defaultClasses = 'inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ';
  switch (status) {
    case EcclesiaStatus.REQUEST:
      return defaultClasses + 'bg-yellow-50 text-yellow-600 ring-yellow-600/20';
    case EcclesiaStatus.REJECT:
      return defaultClasses + 'bg-red-50 text-red-700 ring-red-600/20';
    case EcclesiaStatus.APPROVAL:
      return defaultClasses + 'bg-green-50 text-green-700 ring-green-600/20';
    default:
      return defaultClasses + 'bg-gray-50 text-gray-700 ring-gray-600/20';
  }
}

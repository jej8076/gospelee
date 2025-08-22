import React from 'react';

interface Column {
  key: string;
  label: string;
  width: string;
  render?: (value: any, item: any) => React.ReactNode;
}

interface DataTableProps {
  columns: Column[];
  data: any[];
  onEdit?: (item: any) => void;
}

export default function DataTable({ columns, data, onEdit }: DataTableProps) {
  return (
    <div className="mt-8 flow-root">
      <div className="-mx-4 -my-2 sm:-mx-6 lg:-mx-8">
        <div className="inline-block min-w-full py-2 align-middle">
          <table className="min-w-full table-auto border-separate border-spacing-0">
            <thead>
              <tr>
                {columns.map((column) => (
                  <th
                    key={column.key}
                    scope="col"
                    className={`${column.width} border-b border-gray-300 bg-white/75 px-2 sm:px-8 py-3.5 pr-3 pl-4 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter sm:pl-6 lg:pl-8`}
                  >
                    {column.label}
                  </th>
                ))}
                {onEdit && (
                  <th
                    scope="col"
                    className="min-w-0 w-1/12 border-b border-gray-300 bg-white/75 px-8 py-3.5 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter"
                  >
                    수정
                  </th>
                )}
              </tr>
            </thead>
            <tbody>
              {data.length === 0 ? (
                <tr>
                  <td 
                    colSpan={columns.length + (onEdit ? 1 : 0)} 
                    className="border-b border-gray-200 py-8 text-center text-sm text-gray-500"
                  >
                    데이터가 없습니다.
                  </td>
                </tr>
              ) : (
                data.map((item, index) => (
                  <tr key={item.id || index}>
                    {columns.map((column) => (
                      <td
                        key={column.key}
                        className={`${column.width} border-b border-gray-200 py-4 pr-3 pl-4 text-sm font-medium text-gray-900 sm:pl-6 lg:pl-8`}
                      >
                        {column.render ? column.render(item[column.key], item) : item[column.key]}
                      </td>
                    ))}
                    {onEdit && (
                      <td className="w-1/12 border-b border-gray-200 py-4 pr-3 pl-4 text-sm font-medium text-gray-900 sm:pl-6 lg:pl-8">
                        <button
                          className="text-indigo-600 hover:text-indigo-900 cursor-pointer"
                          onClick={() => onEdit(item)}
                        >
                          수정
                        </button>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

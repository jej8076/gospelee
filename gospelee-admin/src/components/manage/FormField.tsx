import React from 'react';

interface FormFieldProps {
  label: string;
  children: React.ReactNode;
  className?: string;
  required?: boolean;
}

export default function FormField({ label, children, className = "col-span-full", required = false }: FormFieldProps) {
  return (
    <div className={className}>
      <label className="block text-sm/6 font-medium text-gray-900">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <div className="mt-2">
        {children}
      </div>
    </div>
  );
}

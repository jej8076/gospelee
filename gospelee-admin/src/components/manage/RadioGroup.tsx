import React from 'react';

interface RadioOption {
  value: string;
  label: string;
  description?: string;
}

interface RadioGroupProps {
  title: string;
  description?: string;
  options: RadioOption[];
  value: string;
  onChange: (value: string) => void;
  name: string;
}

export default function RadioGroup({ title, description, options, value, onChange, name }: RadioGroupProps) {
  return (
    <fieldset>
      <legend className="text-sm/6 font-semibold text-gray-900">{title}</legend>
      {description && (
        <p className="mt-1 text-sm/6 text-gray-600">{description}</p>
      )}
      <div className="mt-6 space-y-6">
        {options.map((option) => (
          <div key={option.value} className="flex items-center gap-x-3">
            <input
              id={`${name}-${option.value}`}
              name={name}
              type="radio"
              value={option.value}
              checked={value === option.value}
              onChange={(e) => onChange(e.target.value)}
              className="relative size-4 appearance-none rounded-full border border-gray-300 bg-white before:absolute before:inset-1 before:rounded-full before:bg-white not-checked:before:hidden checked:border-indigo-600 checked:bg-indigo-600 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:border-gray-300 disabled:bg-gray-100 disabled:before:bg-gray-400 forced-colors:appearance-auto forced-colors:before:hidden"
            />
            <label htmlFor={`${name}-${option.value}`} className="block text-sm/6 font-medium text-gray-900">
              {option.label}
            </label>
          </div>
        ))}
      </div>
    </fieldset>
  );
}

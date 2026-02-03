import React, { useEffect, useState } from "react";

interface SearchBarProps {
  searchValue: string;
  onSearchChange: (search: string) => void;
  placeholder?: string;
}

const SearchBar: React.FC<SearchBarProps> = ({
  searchValue,
  onSearchChange,
  placeholder = "Search tasks...",
}) => {
  const [searchTerm, setSearchTerm] = useState(searchValue);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchTerm !== searchValue) {
        console.log("🔍 Search term changed:", searchTerm);
        onSearchChange(searchTerm);
      }
    }, 500);
    return () => clearTimeout(timer);
  }, [searchTerm, searchValue, onSearchChange]);

  useEffect(() => {
    if (searchValue !== searchTerm) {
      setSearchTerm(searchValue);
    }
  }, [searchValue, searchTerm]);

  return (
    <div className="bg-white rounded-lg shadow-md p-4">
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <svg
            className="h-4 w-4 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </div>
        <input
          type="text"
          placeholder={placeholder}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500 text-sm"
        />
        {searchTerm && (
          <button
            onClick={() => setSearchTerm("")}
            className="absolute inset-y-0 right-0 pr-3 flex items-center"
          >
            <svg
              className="h-4 w-4 text-gray-400 hover:text-gray-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        )}
      </div>
    </div>
  );
};

export default SearchBar;

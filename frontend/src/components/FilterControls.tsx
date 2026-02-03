import React from "react";
import Select from "react-select";
import { Priority, TaskStatus } from "../types";
import { customSelectStyles } from "../utils/selectStyles";

export interface TaskFilters {
  status?: TaskStatus | null;
  priority?: Priority | null;
  sortBy?: string | null;
  sortDirection?: "asc" | "desc";
  tagName?: string | null;
}

interface FilterControlProps {
  filters: TaskFilters;
  onFiltersChange: (filters: TaskFilters) => void;
  onClearFilters: () => void;
}

const statusOption = [
  { value: null, label: "All" },
  { value: TaskStatus.TODO, label: "Todo" },
  { value: TaskStatus.IN_PROGRESS, label: "In Progress" },
  { value: TaskStatus.DONE, label: "Completed" },
];

const priorityOptions = [
  { value: null, label: "All" },
  { value: Priority.LOW, label: "Low" },
  { value: Priority.MEDIUM, label: "Medium" },
  { value: Priority.HIGH, label: "High" },
];

const sortOptions = [
  { value: null, label: "Default" },
  { value: "dueDate", label: "Due Date" },
  { value: "priority", label: "Priority" },
  { value: "createdAt", label: "Created Date" },
];

const FilterControls: React.FC<FilterControlProps> = ({
  filters,
  onFiltersChange: onFiltersChange,
  onClearFilters,
}) => {
  const getSelectedOption = (options: any[], value: any) => {
    return options.find((option) => option.value === value) || null;
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-sm border space-y-4">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Filter Tasks</h3>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Status
          </label>
          <Select
            value={getSelectedOption(statusOption, filters.status)}
            onChange={(newValue) => {
              onFiltersChange({ ...filters, status: newValue?.value || null });
            }}
            options={statusOption}
            placeholder="All Status"
            className="react-select-container"
            classNamePrefix="react-select"
            styles={customSelectStyles}
            isClearable={false}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Priority
          </label>
          <Select
            value={getSelectedOption(priorityOptions, filters.priority)}
            onChange={(newValue) => {
              onFiltersChange({
                ...filters,
                priority: newValue?.value || null,
              });
            }}
            options={priorityOptions}
            placeholder="All Priority"
            className="react-select-container"
            classNamePrefix="react-select"
            styles={customSelectStyles}
            isClearable={false}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Sort By
          </label>
          <Select
            value={getSelectedOption(sortOptions, filters.sortBy)}
            onChange={(newValue) => {
              onFiltersChange({ ...filters, sortBy: newValue?.value || null });
            }}
            options={sortOptions}
            placeholder="Default"
            className="react-select-container"
            classNamePrefix="react-select"
            styles={customSelectStyles}
            isClearable={false}
          />
        </div>
      </div>

      <div className="flex justify-between items-center">
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-600">Direction:</span>
          <button
            type="button"
            onClick={() => {
              const newDirection =
                filters.sortDirection === "asc" ? "desc" : "asc";
              onFiltersChange({ ...filters, sortDirection: newDirection });
            }}
            disabled={!filters.sortBy}
            className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
              !filters.sortBy
                ? "bg-gray-100 text-gray-400 cursor-not-allowed"
                : filters.sortDirection === "desc"
                  ? "bg-blue-600 text-white hover:bg-blue-700"
                  : "bg-gray-200 text-gray-700 hover:bg-gray-300"
            }`}
          >
            {filters.sortDirection === "desc" ? "↓ DESC" : "↑ ASC"}
          </button>
        </div>

        <button
          type="button"
          onClick={onClearFilters}
          className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded transition-colors border border-gray-300"
        >
          Clear All Filters
        </button>
      </div>
      {(filters.status ||
        filters.priority ||
        filters.sortBy ||
        filters.tagName) && (
        <div className="text-xs text-gray-500 bg-gray-50 p-2 rounded">
          <span className="font-medium">Active filters: </span>
          {filters.status && (
            <span className="mr-2">Status: {filters.status}</span>
          )}
          {filters.priority && (
            <span className="mr-2">Priority: {filters.priority}</span>
          )}
          {filters.sortBy && (
            <span className="mr-2">
              Sort: {filters.sortBy} ({filters.sortDirection})
            </span>
          )}
          {filters.tagName && (
            <span className="inline-flex items-center mr-2">
              Tag:{" "}
              <span className="ml-1 px-2 py-0.5 rounded-full bg-blue-100 text-blue-800 text-xs font-medium">
                {filters.tagName}
              </span>
            </span>
          )}
        </div>
      )}
    </div>
  );
};

export default FilterControls;

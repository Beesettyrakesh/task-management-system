export const customSelectStyles = {
  control: (provided: any, state: any) => ({
    ...provided,
    minHeight: '32px',
    height: '32px',
    fontSize: '12px',
    fontWeight: '500',
    borderRadius: '9999px', // Full rounded to match badge
    border: `1px solid ${state.isFocused ? '#3B82F6' : '#D1D5DB'}`,
    boxShadow: state.isFocused 
      ? '0 0 0 3px rgba(59, 130, 246, 0.1)' 
      : '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    backgroundColor: 'white',
    '&:hover': {
      borderColor: '#3B82F6',
      boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
    },
  }),
  valueContainer: (provided: any) => ({
    ...provided,
    height: '30px',
    padding: '0 12px',
  }),
  input: (provided: any) => ({
    ...provided,
    margin: '0px',
    padding: '0px',
  }),
  placeholder: (provided: any) => ({
    ...provided,
    color: '#6B7280',
    fontWeight: '500',
  }),
  indicatorSeparator: () => ({
    display: 'none',
  }),
  indicatorsContainer: (provided: any) => ({
    ...provided,
    height: '30px',
    paddingRight: '8px',
  }),
  dropdownIndicator: (provided: any) => ({
    ...provided,
    padding: '0px',
    color: '#6B7280',
  }),
  menu: (provided: any) => ({
    ...provided,
    borderRadius: '12px',
    border: '1px solid #E5E7EB',
    boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
    overflow: 'hidden',
  }),
  menuList: (provided: any) => ({
    ...provided,
    padding: '4px',
  }),
  option: (provided: any, state: any) => ({
    ...provided,
    fontSize: '12px',
    fontWeight: '500',
    padding: '8px 12px',
    borderRadius: '8px',
    margin: '2px 0',
    backgroundColor: state.isSelected 
      ? '#3B82F6' 
      : state.isFocused 
        ? '#F3F4F6' 
        : 'transparent',
    color: state.isSelected ? 'white' : '#374151',
    '&:hover': {
      backgroundColor: state.isSelected ? '#3B82F6' : '#F3F4F6',
    },
  }),
};

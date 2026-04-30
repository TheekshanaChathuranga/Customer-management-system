export const validateCustomerForm = (data) => {
  const errors = {};

  if (!data.name || data.name.trim() === '') {
    errors.name = 'Name is required';
  } else if (data.name.length > 200) {
    errors.name = 'Name must not exceed 200 characters';
  }

  if (!data.dateOfBirth) {
    errors.dateOfBirth = 'Date of birth is required';
  }

  if (!data.nicNumber || data.nicNumber.trim() === '') {
    errors.nicNumber = 'NIC number is required';
  } else if (data.nicNumber.length > 20) {
    errors.nicNumber = 'NIC number must not exceed 20 characters';
  }

  return errors;
};

export const formatDate = (dateStr) => {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-GB', {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  } catch {
    return dateStr;
  }
};

export const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  try {
    const date = new Date(dateTimeStr);
    return date.toLocaleString('en-GB', {
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  } catch {
    return dateTimeStr;
  }
};

export const hasErrors = (errors) => Object.keys(errors).length > 0;

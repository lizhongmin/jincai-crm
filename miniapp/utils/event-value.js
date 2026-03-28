function readEventValue(detail) {
  if (detail && typeof detail === 'object' && Object.prototype.hasOwnProperty.call(detail, 'value')) {
    return detail.value;
  }

  if (detail === undefined || detail === null) {
    return '';
  }

  return detail;
}

module.exports = {
  readEventValue
};

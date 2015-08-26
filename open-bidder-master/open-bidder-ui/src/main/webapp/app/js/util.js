/**
 * Clone an object retaining the prototype.
 * @param object {Object} Object to copy.
 * @return Clone with same prototype.
 */
function cloneObject(object) {
  var copy = new object.constructor;
  var keys = _.keys(object);
  var keyLength = keys.length;
  for (var i = 0; i < keyLength; i++) {
    copy[keys[i]] = _.clone(object[keys[i]]);
  }
  return copy;
}

/**
 * Copy selected keys from one object to another.
 * @param source {Object} Source object.
 * @param destination {Object} Destination object.
 * @param keysToCopy List of keys to copy.
 * @return {Object} Destination object.
 */
function copyTo(source, destination, keysToCopy) {
  var keyLength = keysToCopy.length;
  for (var i = 0; i < keyLength; i++) {
    destination[keysToCopy[i]] = _.clone(source[keysToCopy[i]]);
  }
  return destination;
}

/**
 * Return a filtered copy of an object.
 * @param object {Object} Object to copy.
 * @param keysToCopy {Array.<String>} List of keys to retain.
 * @return {Object} Shallow copy of the input with only the selected keys populated
 */
function cloneWithKeys(object, keysToCopy) {
  return copyTo(object, new object.constructor, keysToCopy);
}

/**
 * Return a filtered copy of an object.
 * @param object {Object} Object to copy.
 * @param keysToRemove {Array.<String>} List of keys to retain.
 * @return {Object} Shallow copy of the input with only the selected keys populated
 */
function cloneWithoutKeys(object, keysToRemove) {
  var copy = new object.constructor;
  var keysToCopy = _.difference(_.keys(object), keysToRemove);
  var keyLength = keysToCopy.length;
  for (var i = 0; i < keyLength; i++) {
    copy[keysToCopy[i]] = _.clone(object[keysToCopy[i]]);
  }
  return copy;
}

/**
 * Joins an array using an optional separator.
 * @param arr {Array.<String>} Array of strings.
 * @param opt_separator {String=} Optional separator. Defaults to newline.
 * @return {String} Array items joined with separator.
 */
function joinWithSeparator(arr, opt_separator) {
  opt_separator = _.isUndefined(opt_separator) ? '\n' : opt_separator;
  return _.isArray(arr) ? arr.join(opt_separator) : null;
}

/**
 * Splits an array an optional separator and trims each element.
 * @param string {String} String to split.
 * @param opt_separator {String=} String to separator on. Defaults to newline.
 * @return {Array} Array split on separator.
 */
function splitWithSeparator(string, opt_separator) {
  opt_separator = _.isUndefined(opt_separator) ? '\n' : opt_separator;
  var arr = _.isString(string) ? string.split(opt_separator) : [];
  return _.compact(_.map(arr, function(value) {
    return value.trim();
  }));
}

/**
 * @return {string} Absolute path to the current page.
 */
function absolutePath() {
  var href = window.location.href;
  var index = href.indexOf(window.location.host);
  index += window.location.host.length;
  return href.substr(index);
}

/**
 * We need our custom method because encodeURIComponent is too aggressive and doesn't follow
 * http://www.ietf.org/rfc/rfc3986.txt with regards to the character set (pchar) allowed in path
 * segments:
 *    segment       = *pchar
 *    pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
 *    pct-encoded   = "%" HEXDIG HEXDIG
 *    unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
 *    sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
 *                     / "*" / "+" / "," / ";" / "="
 */
function encodeUriSegment(val) {
  return encodeUriQuery(val, true).
      replace(/%26/gi, '&').
      replace(/%3D/gi, '=').
      replace(/%2B/gi, '+');
}

/**
 * This method is intended for encoding *key* or *value* parts of query component. We need a custom
 * method because encodeURIComponent is too aggressive and encodes stuff that doesn't have to be
 * encoded per http://tools.ietf.org/html/rfc3986:
 *    query       = *( pchar / "/" / "?" )
 *    pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
 *    unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
 *    pct-encoded   = "%" HEXDIG HEXDIG
 *    sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
 *                     / "*" / "+" / "," / ";" / "="
 */
function encodeUriQuery(val, pctEncodeSpaces) {
  return encodeURIComponent(val).
      replace(/%40/gi, '@').
      replace(/%3A/gi, ':').
      replace(/%24/g, '$').
      replace(/%2C/gi, ',').
      replace((pctEncodeSpaces ? null : /%20/g), '+');
}

/**
 * This method converts a date object into a date string in the format of mm/dd/yyyy
 */
function convertToFormattedDateStr(dateObject) {
  var d = dateObject.getDate();
  var m = dateObject.getMonth() + 1;
  var y = dateObject.getFullYear();
  return (m <= 9 ? '0' + m : m) + '/' + (d <= 9 ? '0' + d : d) + '/' + y;
}

/**
 * HTTP status codes.
 */
var HttpStatus = {
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  NOT_ACCEPTABLE: 406,
  CONFLICT: 409,
  PRECONDITION_FAILED: 412,
  INTERNAL_SERVER_ERROR: 500
};

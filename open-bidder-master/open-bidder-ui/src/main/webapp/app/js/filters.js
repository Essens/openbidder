'use strict';

/* Filters */

var openBidderFilters = angular.module('openBidder.filters', []);

/*
 * Encode a URI component.
 */
openBidderFilters.filter('encodeUri', function() {
  return function(value) {
    return _.isUndefined(value) || _.isNull(value) ? '' : encodeUriSegment(value);
  }
});

/**
 * Split the given input using separator and return the value at index.
 */
openBidderFilters.filter('splitAndGet', function() {
  return function(str, separator, index) {
    return str ? str.split(separator)[index] : undefined;
  };
});

/**
 * Capitalize the first letter of each word in the string.
 */
openBidderFilters.filter('titleCase', function() {
  return function(str) {
    return str ? str.replace(/\w\S*/g, function(txt) {
      return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    }) : '';
  }
});

/**
 * Return a default value for "falsy" values.
 */
openBidderFilters.filter('default', function() {
  return function(value, defaultValue) {
    return value ? value : defaultValue;
  };
});

/**
 * Convert an object to a GMT date string.
 */
openBidderFilters.filter('gmt', function() {
  return function(value) {
    if (value instanceof Date) {
      return value.toGMTString();
    } else if (_.isFinite(value)) {
      return new Date(value).toGMTString();
    } else if (_.isString(value)) {
      return new Date(value).toGMTString();
    }
    return value;
  };
});

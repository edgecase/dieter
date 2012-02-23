(function() {
  var _base, _base2, _base3, _base4, _base5, _base6, _base7;

  window.HAML || (window.HAML = {});

  (_base = window.HAML).escape || (_base.escape = function(text) {
    return ("" + text).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\"/g, "&quot;");
  });

  (_base2 = window.HAML).cleanValue || (_base2.cleanValue = function(text) {
    if (text === null || text === void 0) {
      return '';
    } else {
      return text;
    }
  });

  (_base3 = window.HAML).extend || (_base3.extend = function(globals, locals) {
    if (typeof jQuery !== "undefined" && jQuery !== null ? jQuery.extend : void 0) {
      return jQuery.extend({}, globals, locals);
    } else if (typeof _ !== "undefined" && _ !== null ? _.extend : void 0) {
      return _.extend({}, globals, locals);
    } else if (typeof Zepto !== "undefined" && Zepto !== null ? Zepto.extend : void 0) {
      return Zepto.extend(Zepto.extend({}, globals), locals);
    } else if (Object.extend) {
      return Object.extend(Object.extend({}, globals), locals);
    } else if (Object.append) {
      return Object.append(Object.append({}, globals), locals);
    } else {
      return locals;
    }
  });

  (_base4 = window.HAML).globals || (_base4.globals = function() {
    return {};
  });

  (_base5 = window.HAML).context || (_base5.context = function(locals) {
    return HAML.extend(HAML.globals(), locals);
  });

  (_base6 = window.HAML).preserve || (_base6.preserve = function(text) {
    return text.replace(/\\n/g, '&#x000A;');
  });

  (_base7 = window.HAML).findAndPreserve || (_base7.findAndPreserve = function(text) {
    return tags;
  });

  ({
    surround: function(start, end, fn) {
      return start + fn() + end;
    },
    succeed: function(end, fn) {
      return fn() + end;
    },
    precede: function(start, fn) {
      return start + fn();
    }
  });

}).call(this);

(function() {
  if (window.HAML == null) {
    window.HAML = {};
  }

  window.HAML['basic'] = function(context) {
    return (function() {
      var $o;
      $o = [];
      $o.push("<!DOCTYPE html>\n<html>\n  <head>\n    <title>\n      Title\n    </title>\n  </head>\n  <body>\n    <h1>\n      Header\n    </h1>\n  </body>\n</html>");
      return $o.join("\n");
    }).call(context);
  };

}).call(this);


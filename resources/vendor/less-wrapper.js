// stolen from less and modified so we can call it from runtime
// instead of commandline

var startingName;

function readFile(filename){
  var file = java.io.File(filename);
  result = new String(Packages.org.apache.commons.io.FileUtils.readFileToString(file));
  if (result == '')
    throw('lesscss: couldn\'t open file ' + filename);

  return result;
}

function loadStyleSheet(sheet, callback, reload, remaining) {
  var sheetName = startingName.slice(0, startingName.lastIndexOf('/') + 1) + sheet.href;
  var input = readFile(startingName);
  var parser = new less.Parser();
  parser.parse(input, function (e, root) {
    if (e) throw e
    callback(e, root, sheet);
  });

  // callback({}, sheet, { local: true, remaining: remaining });
}

function format_error(e) {
  return "ERROR:" + e.filename + ":" + e.line + ":" + e.column + ": \n" +
    e.message +
    "\nTYPE: " + e.type +
    "\nINDEX: " + e.index +
    "\nEXTRACT: " + e.extract
}

function compileLess(filename) {
  // save it for resolving imports
  startingName = filename;

  var lessResult;

  var input = readFile(filename);

  try {
    var parser = new less.Parser();
    parser.parse(input, function (e, root) {
      if (e)
        throw(e);

      // Write to an in-scope variable that can be returned.
      lessResult = root.toCSS();
    });
  } catch (e) { throw(format_error(e)); }
  return lessResult;
}

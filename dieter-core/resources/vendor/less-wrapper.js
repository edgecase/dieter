// stolen from less and modified so we can call it from runtime
// instead of commandline

var startingName;

function loadStyleSheet(sheet, callback, reload, remaining) {
    var endOfPath = Math.max(startingName.lastIndexOf('/'), startingName.lastIndexOf('\\')),
        sheetName = startingName.slice(0, endOfPath + 1) + sheet.href,
        input = readFile(sheetName);
    var parser = new less.Parser({
        paths: [sheet.href.replace(/[\w\.-]+$/, '')]
    });
    parser.parse(input, function (e, root) {
        if (e) {
            return error(e, sheetName);
        }
        try {
            callback(e, root, sheet, { local: false, lastModified: 0, remaining: remaining });
        } catch(e) {
            error(e, sheetName);
        }
    });
}

function format_error(e) {
  return "ERROR:" + e.filename + ":" + e.line + ":" + e.column + ": \n" +
    e.message +
    "\nTYPE: " + e.type +
    "\nINDEX: " + e.index +
    "\nEXTRACT: " + e.extract
}

function compileLess(input, name) {
    var output,
        compress = false,
        i;

    startingName = name;
    path = name.split("/");path.pop();path=path.join("/")

    var input = readFile(name);

    if (!input) {
        print('lesscss: couldn\'t open file ' + name);
    }

    var result;
    try {
        var parser = new less.Parser();
        parser.parse(input, function (e, root) {
            if (e) { throw(e); }
            result = root.toCSS();
        });
    }
    catch(e) { throw(format_error(e)); }
    return result;
};

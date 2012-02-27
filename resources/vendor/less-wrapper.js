// stolen from less and modified so we can call it from runtime
// instead of commandline

var name, lessResult, lessError;

function readFile(name){
  var file = java.io.File(name);
  return new String(Packages.org.apache.commons.io.FileUtils.readFileToString(file));
}

function loadStyleSheet(sheet, callback, reload, remaining) {
  var sheetName = name.slice(0, name.lastIndexOf('/') + 1) + sheet.href;
  var input = readFile(sheetName);
  var parser = new less.Parser({
//    paths: [sheet.href.replace(/[\w\.-]+$/, '')]
//    paths: ["test/fixtures/assets/stylesheets/"]
  });
  parser.parse(input, function (e, root) {
      callback(e, root, sheet);
  });

  // callback({}, sheet, { local: true, remaining: remaining });
}

// function writeFile(filename, content) {
//     var fstream = new java.io.FileWriter(filename);
//     var out = new java.io.BufferedWriter(fstream);
//     out.write(content);
//     out.close();
// }

function compileLess(filename) {
  name = filename;

  path = name.split("/");path.pop();path=path.join("/")

  var input = readFile(name);

  if (!input) {
    throw('lesscss: couldn\'t open file ' + name);
  }

  var parser = new less.Parser();
  parser.parse(input, function (e, root) {
    if (e) {
      throw(e);
    } else {
      lessResult = root.toCSS();
    }
  });
}

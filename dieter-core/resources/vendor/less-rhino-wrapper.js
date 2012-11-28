function readFile(filename){
  var file = java.io.File(filename);
  result = new String(Packages.org.apache.commons.io.FileUtils.readFileToString(file));
  if (result == '')
    throw('lesscss: couldn\'t open file ' + filename);

  return result;
}

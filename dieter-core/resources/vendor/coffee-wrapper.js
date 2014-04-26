function formatError(e, filename) {
  return filename + ": " + e.message;
}

function compileCoffeeScript(input, filename) {
  return CoffeeScript.compile(input, {filename: filename});
}

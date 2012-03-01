function formatError(e, filename) {
  return filename + ": " + e.message;
}

function compileCoffeeScript(input, filename) {
  try {
      return CoffeeScript.compile(input);
  } catch (e) {
    throw formatError(e, filename);
  }
}

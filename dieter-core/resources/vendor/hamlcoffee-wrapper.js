var coffeeError;

function formatError(e, filename) {
  return filename + ": " + e.message;
}

function compileHamlCoffee(input, filename) {
  try {
    return HamlCoffeeAssets.compile(filename,
                                    input,
                                    true,
                                    null,
                                    "html5",
                                    false, // dieter uglifies automatically
                                    false,
                                    false,
                                    false,
                                    false,
                                    '',
                                    '',
                                    '',

                                    '',
                                    '',
                                    '',

                                    '',
                                    '',
                                    '',
                                    '');
  } catch (e) {
    throw formatError(e, filename);
  }
}

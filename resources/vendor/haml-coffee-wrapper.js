var coffeeError;

function compileHamlCoffee(input, filename) {
  try {
    return HamlCoffeeAssets.compile(filename,
                                    input,
                                    true,
                                    "HAML",
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
    coffeeError = e;
    throw e;
  }
}

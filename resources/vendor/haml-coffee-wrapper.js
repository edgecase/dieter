var coffeeError;

function compileHamlCoffee(input, filename) {
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
}

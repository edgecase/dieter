var coffeeError;

function compileHamlCoffee(input, absolute, filename) {
    no_ext = filename.substr(0, filename.lastIndexOf('.')) || filename;
    //no_ext = no_ext.replace(new RegExp("/","gm"),"_"); // to use the fill name, add underscores
    return HamlCoffeeAssets.compile(no_ext, // name
                                    input, // source
                                    true, // jst
                                    null, // namespace
                                    "html5", // format
                                    false, // uglify
                                    true, // basename
                                    false, // escapeHtml
                                    false, // escapeAttributes
                                    false, // cleanValue
                                    "global", // placement
                                    {}, // dependencies
                                    '', // customeHtmlEscape
                                    '', // customCleanValue
                                    '', // customPreserve
                                    '', // customFindAndPreserve
                                    '', // customSurround
                                    '', // customSucceed
                                    '', // customPrecede
                                    '', // preserveTags
                                    '', // selfCloseTags
                                    '', // context
                                    false // extendScope
);
}

# Dieter

Dieter [dee-ter] is a clojure interpretation of the ruby Sprockets library.

## Usage

Dieter provides you with a ring middleware which will compile certain
static assets. Currently it supports concatenating javascript and CSS
files, compiling
[LESS CSS](http://lesscss.org/),
[CoffeeScript](http://jashkenas.github.com/coffee-script/) and
[Haml-coffee](https://github.com/9elements/haml-coffee).
In addition it minifies javascript using the Google Closure compiler.

Add dieter as a dependency in leiningen

    [dieter "0.3.0"]

Insert it into your ring middleware stack

```clojure
(-> app
    (asset-pipeline config-options))
```

Or if you use noir

```clojure
(server/add-middleware asset-pipeline config-options)
```

Concatenation of assets is handled by a Dieter manifest file.
A manifest is a file whose name ends in .dieter and whose contents are
a clojure vector of file names / directories to concatenate.

For example, a file named assets/javascripts/app.js.dieter with the following contents:

```clojure
[
  "./base.js"
  "framework.js"
  "./lib/"
  "./models/"
]
```

Dieter would look for base.js in the same directory, framework.js in any subdirectory,
and then concatenate each file from the lib and models directories.

## Linkage

In order to include links to your assets you may use the link-to-asset function.

```clojure
(link-to-asset "javascripts/app.js" config-options)
```

## Configuration Options

    :engine     :rhino                  ; defaults to :rhino; :v8 is much much faster
    :compress   false                   ; minify using Google Closure Compiler
    :asset-roots ["resources"]          ; must have a folder called 'assets'
    :cache-root "resources/asset-cache" ; compiled assets are cached here
    :cache-mode :development            ; or :production. :development disables cacheing
    :log-level  :normal                 ; or :quiet
    :precompiles ["./assets/myfile.js.dieter"] ; list of files for `lein dieter-precompile` to precompile. If left blank (the default), all files will be precompiled, and errors will be ignored.

Dieter searches for your assets in [asset-root]/assets.
Compiled assets are always written to the cache-root. In production mode this
means that the cached assets are served from the cache. However development mode
assets are always regenerated.

Note you need to pass your config options to asset-pipeline as well as link-to-asset.

## Contributing

It is easy to add new preprocessors to dieter. Each preprocessor (CoffeeScript, HamlCoffee, etc)
uses the default library for that language, hooked up to dieter using the Rhino
JavaScript library. See dieter-core/src/dieter/assets/ for easy-to-follow examples.

## Dancing

Now it's [time to dance](http://youtu.be/LxQ6olQjebg)

## License

Copyright (C) 2012 EdgeCase

Distributed under the Eclipse Public License, the same as Clojure.

## Breaking Changes

### Version 0.2.0
* Handlebars templates are now a separate library. [dieter-ember](https://github.com/edgecase/dieter-ember)

### Version 0.3.0
* Use v8 for Less, Hamlcoffee and CoffeeScript
* Cache and avoid recompiling CoffeeScript and HamlCoffee files which haven't changed
* Update to lein2
* Improve stack traces upon failure in Rhino
* Update Coffeescript (1.3.3), Less (1.3.0) and Hamlcoffee (1.2.0) versions
* Ignore transient files from vim and emacs
* Better error reporting of HamlCoffee
* Support multiple asset directories
* Add expire-never headers
* Improve Rhino speed by using one engine per thread
* Update to latest Rhino for better performance
* Support for `lein dieter-precompile`
* Add mime type headers for dieter files

# Dieter

Dieter [dee-ter] is a clojure interpretation of the ruby Sprockets library.

## Usage

Dieter provides you with a ring middleware which will compile certain
static assets. Currently it supports concatiating javascript and CSS
files, compiling [LESS CSS](http://lesscss.org/), and compiling
[Handlebars](https://github.com/wycats/handlebars.js). In addition it
minifies javascript using the Google Closure compiler.

Insert it into your ring middleware stack

```clojure
(-> app
    (asset-pipeline config-options))
```

Or if you use noir

```clojure
(server/add-middleware asset-pipeline config-options)
```

Concatination of assets is handled by a Dieter manifest file.
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

    :compress   false
    :asset-root "resources"             ; must have a folder called 'assets'
    :cache-root "resources/asset-cache" ; compiled assets are cached here
    :cache-mode :development            ; or :production. :development disables cacheing
    :hbs-mode   :handlebars             ; or :ember.

Dieter searches for your assets in [asset-root]/assets.
Compiled assets are always written to the cache-root. In production mode this
means that the cached assets are served from the cache. However development mode
assets are always regenerated.

If you use handlebars with Ember.js your .hbs templates need to be compiled with a
different function. In that case set :hbs-mode to :ember.

Note you need to pass your config options to asset-pipeline as well as link-to-asset.

## Dancing

Now it's [time to dance](http://youtu.be/LxQ6olQjebg)

## License

Copyright (C) 2012 EdgeCase

Distributed under the Eclipse Public License, the same as Clojure.

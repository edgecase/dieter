# Dieter precompile - a leiningen plugin

A leiningen plugin for precompiling assets using Dieter, similar to Rails' `rake asset-precompile`.

## Installation

In `:dev-dependencies`, add:

```clojure
[lein-dieter-precompile "0.1"]
```

## Usage

Run

```bash
lein dieter-precompile
```

and that's all.
Unless you changed dieter's default settings, in which case you must tell lein-dieter-precompile where to find the settings for your project.
In which case, add :dieter-options to your `project.clj` file.
There are a few options for expression :dieter-options:

1.
  A string containing the name of a var in your project, where the var is expected to hold the dieter-options map:

  ```clojure
  :dieter-options "circle.http.assets/dieter-options"
  ```

2.
  A string containing the name of a function in your project, where the function returns a dieter-optinos map:

  ```clojure
  :dieter-options "circle.http.assets/dieter-options-fn"
  ```

3.
  A map containing the dieter options

  ```clojure
  :dieter-options {:production true}
  ```
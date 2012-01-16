var DieterLastParse = "";
var DieterParseLess = function(text){
  var parser = new(less.Parser)({paths: ["resources/assets/stylesheets/"]})
  return parser.parse(text, function(err, tree){
    if(err){
      throw err;
    } else {
      DieterLastParse = tree.toCSS();
    }
  });
};

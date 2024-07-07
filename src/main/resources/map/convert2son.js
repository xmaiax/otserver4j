require('fs').writeFileSync("just-rookgaard.json", JSON.stringify(require("./otbm2json.js").read('./just-rookgaard.otbm'), null, 2))

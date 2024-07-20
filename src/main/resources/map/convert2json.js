require('fs').writeFileSync('rookgaard.json', JSON.stringify(require('./otbm2json.js').read('./rookgaard.otbm'), null, 2))

const path = require('path');

module.exports = {
    entry: './src/tiktok.js',
    output: {
        filename: 'tiktok.bundle.js',
        path: path.resolve(__dirname, 'dist'),
    },
    mode: 'development',
    devServer: {
        contentBase: path.join(__dirname, 'dist'),
        compress: true,
        port: 9000
    }
};

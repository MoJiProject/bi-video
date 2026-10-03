const { defineConfig } = require('@vue/cli-service')
const webpack = require('webpack')
const CompressionPlugin = require('compression-webpack-plugin')

// /img/... 指向 public/img，由 copy-webpack-plugin 原样拷贝，不参与打包。
// css-loader 默认会把根路径 url() 也当作模块去解析（Cannot find module '/img/xxx'），
// 这里过滤掉根路径，字体等相对路径引用仍照常走 webpack。
const skipRootRelativeUrl = {
  url: {
    filter: (url) => !url.startsWith('/'),
  },
}

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,

  css: {
    loaderOptions: {
      // loaderOptions.css 会作用于所有 css/scss/sass/less 规则（cli-service/lib/config/css.js:136）
      css: skipRootRelativeUrl,
    },
  },

  // 本地开发代理后端 API
  devServer: {
    // 启动后自动打开浏览器
    open: true,
    port: 8080,
    // history 模式的路由（/video、/systemManagement/* 等）直接访问时
    // 回退到 index.html，行为与生产环境 nginx 的 try_files 保持一致
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8081/',
        changeOrigin: true,
        pathRewrite: { '^/api': '' },
      },
    },
  },

  configureWebpack: {
    plugins: [
      new webpack.DefinePlugin({
        '__VUE_PROD_HYDRATION_MISMATCH_DETAILS__': JSON.stringify(true),
      }),
      // Gzip 压缩插件
      new CompressionPlugin({
        algorithm: 'gzip',
        test: /\.(js|css|html|svg)$/,
        threshold: 10240,
        minRatio: 0.8,
        deleteOriginalAssets: false,
      }),
    ],
  },

  productionSourceMap: false,

  pages: {
    index: {
      entry: 'src/main.js',
      template: 'public/index.html',
      filename: 'index.html',
      title: '哔哩哔哩(゜- ゜)つロ 干杯~-bilibili',
    },
  },
})

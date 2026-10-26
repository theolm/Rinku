// Direct links and reloads must serve the SPA entry point, not a 404.
config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;

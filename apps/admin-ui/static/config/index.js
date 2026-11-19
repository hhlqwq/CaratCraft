/** 初始化唯一站点运行配置, 由部署入口修改API地址. */
(function initializeSiteConfiguration() {
    window.SITE_CONFIG = {
        baseUrl: location.origin + '/platform-framework',
        domain: './',
        version: '',
        cdnUrl: './',
    };
})();

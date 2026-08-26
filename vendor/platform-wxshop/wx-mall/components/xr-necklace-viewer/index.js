/**
 * 项链 XR-Frame 预览组件 / Necklace XR-Frame preview component.
 */
Component({
  properties: {
    models: {
      type: Array,
      value: []
    }
  },
  data: {
    loaded: false,
    progress: 0
  },
  lifetimes: {
    /** 释放场景引用 / Releases scene references. */
    detached: function () {
      this.scene = null;
      this.setData({ loaded: false, progress: 0 });
    }
  },
  methods: {
    /** 保存就绪场景 / Stores the ready scene. */
    handleReady: function (event) {
      this.scene = event.detail && event.detail.value;
    },

    /** 更新资源加载进度 / Updates asset loading progress. */
    handleProgress: function (event) {
      var progress = event.detail && event.detail.progress;
      this.setData({ progress: Math.round(Number(progress || 0) * 100) });
    },

    /** 标记全部模型已加载 / Marks all models as loaded. */
    handleLoaded: function () {
      this.setData({ loaded: true, progress: 100 });
      this.triggerEvent('loaded');
    },

    /** 通知父页面降级到二维预览 / Notifies the page to fall back to 2D. */
    handleError: function (event) {
      this.triggerEvent('error', event.detail || {});
    }
  }
});

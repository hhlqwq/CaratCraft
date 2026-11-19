<template>
    <svg :class="getClassName" :width="width" :height="height" aria-hidden="true">
        <use :xlink:href="getName"></use>
    </svg>
</template>

<script>
export default {
    name: 'icon-svg',
    props: {
        name: {
            type: String,
            required: true,
        },
        className: {
            type: String,
        },
        width: {
            type: String,
        },
        height: {
            type: String,
        },
    },
    computed: {
        /** 生成当前图标在SVG雪碧图中的引用标识. */
        getName() {
            return `#icon-${this.name}`;
        },
        /** 按图标名称和样式选项生成SVG类名. */
        getClassName() {
            return [
                'icon-svg',
                `icon-svg__${this.name}`,
                this.className && /\S/.test(this.className) ? `${this.className}` : '',
            ];
        },
    },
};
</script>

<style>
.icon-svg {
    width: 1em;
    height: 1em;
    fill: currentColor;
    overflow: hidden;
}
</style>

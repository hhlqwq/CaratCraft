const svgFiles = import.meta.glob('./svg/*.svg', {query: '?raw', import: 'default', eager: true})
const names = Object.keys(svgFiles).map(file => file.split('/').pop().replace(/^icon-/, '').replace(/\.svg$/, ''))

export default {
  /** 只装载仓库内已审核的SVG, 不接受用户输入或远程标记. */
  install () {
    const sprite = document.createElementNS('http://www.w3.org/2000/svg', 'svg')
    sprite.setAttribute('aria-hidden', 'true')
    sprite.style.display = 'none'
    Object.entries(svgFiles).forEach(([file, source]) => {
      const original = new DOMParser().parseFromString(source, 'image/svg+xml').documentElement
      const symbol = document.createElementNS('http://www.w3.org/2000/svg', 'symbol')
      symbol.id = file.split('/').pop().replace(/\.svg$/, '')
      symbol.setAttribute('viewBox', original.getAttribute('viewBox') || '0 0 1024 1024')
      Array.from(original.children).forEach(child => symbol.appendChild(document.importNode(child, true)))
      sprite.appendChild(symbol)
    })
    document.body.appendChild(sprite)
  },
  // 获取图标icon-(*).svg名称列表, 例如[shouye, xitong, zhedie, ...]
  getNameList () {
    return names
  }
}

const sources = {FRESHWATER: '淡水珍珠', SEAWATER: '海水珍珠'};
const types = {NECKLACE: '项链', EARRINGS: '耳饰', BRACELET: '手链', RING: '戒指'};

/** 只展示商家填写的商品事实, 空参数不从名称或演示数据推断. */
function parameters(goods) {
  const values = [
    ['珍珠来源', sources[goods.pearlSource]],
    ['成品品类', types[goods.jewelryType]],
    ['珍珠品种', goods.pearlVariety],
    ['产地', goods.pearlOrigin],
    ['形状', goods.pearlShape],
    ['光泽', goods.pearlLuster],
    ['瑕疵说明', goods.pearlBlemish],
  ];
  if (Number(goods.pearlDiameterMin) > 0 && Number(goods.pearlDiameterMax) > 0) {
    const min = Number(goods.pearlDiameterMin);
    const max = Number(goods.pearlDiameterMax);
    values.push(['直径', min === max ? min + 'mm' : min + '–' + max + 'mm']);
  }
  return values.filter((item) => item[1]).map((item) => ({name: item[0], value: item[1]}));
}

module.exports = {sources, types, parameters};

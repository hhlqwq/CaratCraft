package com.platform.modules.job.task;

import com.platform.modules.mall.service.MallGoodsService;
import java.util.Calendar;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 商品回收站到期清理任务。 */
@Slf4j
@Component("goodsRecycleTask")
@RequiredArgsConstructor
public class GoodsRecycleTask {
  /** 商品回收站保留天数。 */
  private static final int RETENTION_DAYS = 30;

  private final MallGoodsService mallGoodsService;

  /** 清理超过三十天的商品和可重建关联数据。 */
  @SuppressWarnings("unused")
  public void purgeExpiredGoods() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -RETENTION_DAYS);
    Date expireTime = calendar.getTime();
    int deletedCount = mallGoodsService.purgeExpiredRecycleBin(expireTime);
    log.info("商品回收站到期清理完成，共永久删除 {} 个商品", deletedCount);
  }
}

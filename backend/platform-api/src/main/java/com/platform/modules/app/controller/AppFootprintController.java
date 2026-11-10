package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.DateUtils;
import com.platform.common.utils.Query;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.entity.MallFootprintEntity;
import com.platform.modules.mall.service.MallFootprintService;
import com.platform.modules.wx.entity.WxUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 作者: @author hehailong <br>
 * 时间: 2017-08-11 08:32<br>
 *
 * <p>描述: AppController <br>
 */
@Tag(name = "会员足迹-AppFootprintController")
@RestController
@RequestMapping("/app/footprint")
public class AppFootprintController extends AppBaseController {
    @Autowired private MallFootprintService footprintService;

    /** 按本人归属删除指定浏览足迹. */
    @Operation(summary = "删除足迹")
    @PostMapping("delete")
    public RestResponse<Map<String, Object>> delete(
            @LoginUser WxUserEntity loginUser, Integer footprintId) {
        if (footprintId == null || loginUser == null || loginUser.getId() == null) {
            return RestResponse.fail("删除出错");
        }
        MallFootprintEntity footprintEntity = footprintService.getById(footprintId);
        if (footprintEntity == null
                || !loginUser.getId().equals(footprintEntity.getUserId())
                || footprintEntity.getGoodsId() == null) {
            return RestResponse.fail(404, "足迹不存在或无权删除");
        }

        boolean deleted =
                footprintService.remove(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
                                        MallFootprintEntity>()
                                .eq("user_id", loginUser.getId())
                                .eq("goods_id", footprintEntity.getGoodsId()));
        if (!deleted) {
            return RestResponse.fail(404, "足迹不存在或无权删除");
        }

        return RestResponse.ok("删除成功");
    }

    /** 分页查询本人浏览足迹并按日期分组展示. */
    @Operation(summary = "获取足迹列表")
    @PostMapping("list")
    public RestResponse<Map<String, Object>> list(
            @LoginUser WxUserEntity loginUser, @RequestParam Map<String, Object> params) {
        Integer page = positiveInteger(params.get("page"), 1);
        Integer size = positiveInteger(params.get("limit"), 20);
        if (page == null || size == null || size > 100) {
            return RestResponse.fail("分页参数无效, 每页须为1到100条");
        }
        long offset = (long) (page - 1) * size;
        if (offset > Integer.MAX_VALUE) {
            return RestResponse.fail("分页参数无效");
        }
        Map<String, Object> resultObj = new HashMap<String, Object>();

        List<MallFootprintEntity> footprintVos =
                footprintService.queryListFootprint(loginUser.getId() + "", (int) offset, size);
        int returned = footprintVos == null ? 0 : footprintVos.size();
        int total = footprintService.countListFootprint(loginUser.getId() + "");

        Map<String, List<MallFootprintEntity>> footPrintMap =
                new TreeMap<String, List<MallFootprintEntity>>(
                        new Comparator<String>() {
                            /*
                             * int compare(Object o1, Object o2) 返回一个基本类型的整型，
                             * 返回负数表示：o1 小于o2，
                             * 返回0 表示：o1和o2相等，
                             * 返回正数表示：o1大于o2。
                             */
                            @Override
                            public int compare(String o1, String o2) {

                                // 指定排序器按照降序排列
                                return o2.compareTo(o1);
                            }
                        });

        List<List<MallFootprintEntity>> footprintVoList =
                new ArrayList<List<MallFootprintEntity>>();
        if (returned > 0) {
            for (MallFootprintEntity footprintVo : footprintVos) {
                String addTime =
                        DateUtils.timeToStr(footprintVo.getAddTime(), DateUtils.DATE_PATTERN);
                List<MallFootprintEntity> tmpList = footPrintMap.get(addTime);
                if (null == footPrintMap.get(addTime)) {
                    tmpList = new ArrayList<MallFootprintEntity>();
                }
                tmpList.add(footprintVo);
                footPrintMap.put(addTime, tmpList);
            }
            for (Map.Entry<String, List<MallFootprintEntity>> entry : footPrintMap.entrySet()) {
                footprintVoList.add(entry.getValue());
            }
        }
        resultObj.put("count", total);
        resultObj.put("totalPages", (total + size - 1) / size);
        resultObj.put("numsPerPage", size);
        resultObj.put("currentPage", page);
        resultObj.put("hasMore", offset + returned < total);
        resultObj.put("data", footprintVoList);

        return RestResponse.ok(resultObj);
    }

    /** 解析正整数分页参数, 缺省使用默认值, 非法文本返回空以拒绝请求. */
    private static Integer positiveInteger(Object value, int fallback) {
        if (value == null || value.toString().isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.toString().trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }

    /** 查询当前用户作为分享来源的商品浏览记录. */
    @Operation(summary = "分享足迹")
    @PostMapping("sharelist")
    public RestResponse<Map<String, List<MallFootprintEntity>>> sharelist(
            @LoginUser WxUserEntity loginUser,
            @RequestParam(value = "page") Integer page,
            @RequestParam(value = "size") Integer size) {
        if (page == null
                || size == null
                || page < 1
                || size < 1
                || size > 100
                || (long) (page - 1) * size > Integer.MAX_VALUE) {
            return RestResponse.fail("分页参数无效, 每页须为1到100条");
        }
        Map<String, List<MallFootprintEntity>> resultObj = new HashMap<>();

        // 查询列表数据
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sidx", "f.id");
        params.put("order", "desc");
        params.put("referrer", loginUser.getId());
        params.put("page", page);
        params.put("limit", size);
        Query<MallFootprintEntity> query = new Query<>(params);
        List<MallFootprintEntity> footprintVos = footprintService.shareList(query);
        //
        resultObj.put("data", footprintVos);
        return RestResponse.ok(resultObj);
    }
}

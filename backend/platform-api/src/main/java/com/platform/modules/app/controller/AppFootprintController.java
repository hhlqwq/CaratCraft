package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.Query;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.entity.MallFootprintEntity;
import com.platform.modules.mall.service.FootprintApplicationService;
import com.platform.modules.mall.service.FootprintApplicationService.FootprintPage;
import com.platform.modules.mall.service.MallFootprintService;
import com.platform.modules.wx.entity.WxUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本人足迹入口只提供认证归属和分页输入.
 *
 * @author hehailong
 */
@Tag(name = "会员足迹-AppFootprintController")
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/footprint")
public class AppFootprintController extends AppBaseController {
    private final MallFootprintService footprintService;
    private final FootprintApplicationService footprints;

    /** 删除本人所选商品的浏览记录, 不用外部记录推导商品身份. */
    @PostMapping("delete")
    public RestResponse<String> delete(
            @LoginUser WxUserEntity user, @RequestParam Integer footprintId) {
        footprints.delete(user.getId(), footprintId);
        return RestResponse.ok("删除成功");
    }

    /** 使用唯一标准分页返回本人每商品最新浏览. */
    @PostMapping("list")
    public RestResponse<FootprintPage> list(
            @LoginUser WxUserEntity user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return RestResponse.ok(footprints.list(user.getId(), page, size));
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

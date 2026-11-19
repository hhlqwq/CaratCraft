package com.platform.modules.app.controller;

import com.platform.annotation.IgnoreAuth;
import com.platform.cache.RegionCacheUtil;
import com.platform.common.utils.RestResponse;
import com.platform.common.utils.StringUtils;
import com.platform.modules.app.vo.RegionVo;
import com.platform.modules.sys.entity.SysRegionEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地区接口（当前仓库缺少地区基础数据模块，先保证接口可用并统一返回结构）
 *
 * @author hehailong
 */
@Tag(name = "地区-AppRegionController")
@RestController
@RequestMapping("/app/region")
public class AppRegionController extends AppBaseController {

    /** 按当前接口参数查询对应的业务列表. */
    @Operation(summary = "地区列表")
    @IgnoreAuth
    @PostMapping("list")
    public RestResponse<List<RegionVo>> list(Integer parentId) {
        List<SysRegionEntity> regionEntityList = RegionCacheUtil.getChildrenByParentId(parentId);
        List<RegionVo> regionVoList = new ArrayList<RegionVo>();
        if (!regionEntityList.isEmpty()) {
            for (SysRegionEntity sysRegionEntity : regionEntityList) {
                regionVoList.add(new RegionVo(sysRegionEntity));
            }
        }
        return RestResponse.ok(regionVoList);
    }

    /** 读取地区数据中的省级列表. */
    @Operation(summary = "省份列表")
    @IgnoreAuth
    @PostMapping("provinceList")
    public RestResponse<List<RegionVo>> provinceList() {
        List<SysRegionEntity> regionEntityList = RegionCacheUtil.getAllProvice();
        List<RegionVo> regionVoList = new ArrayList<RegionVo>();
        if (!regionEntityList.isEmpty()) {
            for (SysRegionEntity sysRegionEntity : regionEntityList) {
                regionVoList.add(new RegionVo(sysRegionEntity));
            }
        }
        return RestResponse.ok(regionVoList);
    }

    /** 根据省份名称读取城市列表. */
    @Operation(summary = "根据省查市")
    @IgnoreAuth
    @PostMapping("cityList")
    public RestResponse<List<RegionVo>> cityList(String proviceName) {
        List<SysRegionEntity> regionEntityList = RegionCacheUtil.getChildrenCity(proviceName);
        List<RegionVo> regionVoList = new ArrayList<RegionVo>();
        if (!regionEntityList.isEmpty()) {
            for (SysRegionEntity sysRegionEntity : regionEntityList) {
                regionVoList.add(new RegionVo(sysRegionEntity));
            }
        }
        return RestResponse.ok(regionVoList);
    }

    /** 根据省市名称读取区县列表. */
    @Operation(summary = "根据省市查区")
    @IgnoreAuth
    @PostMapping("distinctList")
    public RestResponse<List<RegionVo>> distinctList(String proviceName, String cityName) {
        List<SysRegionEntity> regionEntityList =
                RegionCacheUtil.getChildrenDistrict(proviceName, cityName);
        List<RegionVo> regionVoList = new ArrayList<RegionVo>();
        if (!regionEntityList.isEmpty()) {
            for (SysRegionEntity sysRegionEntity : regionEntityList) {
                regionVoList.add(new RegionVo(sysRegionEntity));
            }
        }
        return RestResponse.ok(regionVoList);
    }

    /** 按标识读取当前接口对应的记录详情. */
    @Operation(summary = "地区详情")
    @IgnoreAuth
    @PostMapping("info")
    public RestResponse<RegionVo> info(Integer regionId) {
        SysRegionEntity regionEntity = RegionCacheUtil.getAreaByAreaId(regionId);
        return RestResponse.ok(new RegionVo(regionEntity));
    }

    /** 根据明确的省市区名称查找地区标识. */
    @Operation(summary = "区域名称转区域ID")
    @IgnoreAuth
    @PostMapping("regionIdsByNames")
    public RestResponse<Map<String, Integer>> regionIdsByNames(
            String provinceName, String cityName, String districtName) {
        Map<String, Integer> resultObj = new HashMap<String, Integer>();
        Integer provinceId = 0;
        Integer cityId = 0;
        Integer districtId = 0;
        if (null != provinceName) {
            provinceId = RegionCacheUtil.getProvinceIdByName(provinceName);
        }
        if (null != provinceId && !StringUtils.isNullOrEmpty(cityName)) {
            cityId = RegionCacheUtil.getCityIdByName(provinceId, cityName);
        }
        if (null != provinceId && null != cityId && !StringUtils.isNullOrEmpty(districtName)) {
            districtId = RegionCacheUtil.getDistrictIdByName(provinceId, cityId, districtName);
        }
        resultObj.put("provinceId", provinceId);
        resultObj.put("cityId", cityId);
        resultObj.put("districtId", districtId);
        return RestResponse.ok(resultObj);
    }
}

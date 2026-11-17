package com.platform.modules.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.utils.CharUtil;
import com.platform.common.utils.DateUtils;
import com.platform.common.utils.Query;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.entity.MallAttributeEntity;
import com.platform.modules.mall.entity.MallBrandEntity;
import com.platform.modules.mall.entity.MallCartEntity;
import com.platform.modules.mall.entity.MallCategoryEntity;
import com.platform.modules.mall.entity.MallCollectEntity;
import com.platform.modules.mall.entity.MallCouponEntity;
import com.platform.modules.mall.entity.MallFootprintEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.entity.MallGoodsGalleryEntity;
import com.platform.modules.mall.entity.MallGoodsIssueEntity;
import com.platform.modules.mall.entity.MallGoodsSpecificationEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.entity.MallRelatedGoodsEntity;
import com.platform.modules.mall.entity.MallSearchHistoryEntity;
import com.platform.modules.mall.entity.MallUserCouponEntity;
import com.platform.modules.wx.entity.WxUserEntity;
import com.qiniu.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作者: @author hehailong <br>
 * 时间: 2017-08-11 08:32<br>
 *
 * <p>描述: 商城目录、详情与购物车商品展示服务 <br>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CatalogApplicationService {
    @Autowired private MallGoodsService goodsService;
    @Autowired private MallGoodsSpecificationService goodsSpecificationService;
    @Autowired private MallProductService productService;
    @Autowired private MallGoodsGalleryService goodsGalleryService;
    @Autowired private MallGoodsIssueService goodsIssueService;
    @Autowired private MallAttributeService attributeService;
    @Autowired private MallBrandService brandService;
    @Autowired private MallCollectService collectService;
    @Autowired private MallFootprintService footprintService;
    @Autowired private MallCategoryService categoryService;
    @Autowired private MallSearchHistoryService searchHistoryService;
    @Autowired private MallRelatedGoodsService relatedGoodsService;
    @Autowired private MallCouponService apiCouponService;
    @Autowired private MallUserCouponService apiUserCouponService;
    @Autowired private MallCartService cartService;
    @Autowired private CustomerReviewService reviews;

    /** 查询当前未删除且在售的商品. */
    public RestResponse<Object> index() {
        Map<String, Object> param = new HashMap<>();
        param.put("isDelete", 0);
        param.put("isOnSale", 1);
        List<MallGoodsEntity> goodsList = goodsService.queryList(param);
        return RestResponse.ok(goodsList);
    }

    /** 获取商品规格信息，用于购物车编辑时选择规格 */
    public RestResponse<Object> sku(Integer id) {
        Map<String, Object> resultObj = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("goodsId", id);
        List<MallGoodsSpecificationEntity> goodsSpecificationEntityList =
                goodsSpecificationService.queryList(param);
        List<MallProductEntity> productEntityList = productService.queryList(param);
        resultObj.put("specificationList", goodsSpecificationEntityList);
        resultObj.put("productList", productEntityList);
        return RestResponse.ok(resultObj);
    }

    /** 按首次出现的规格顺序分组, 保留现有名称和取值字段, 避免原始Map的强制转换. */
    List<Map<String, Object>> groupSpecifications(
            List<MallGoodsSpecificationEntity> specifications) {
        Map<Integer, List<MallGoodsSpecificationEntity>> groups = new LinkedHashMap<>();
        for (MallGoodsSpecificationEntity item : specifications) {
            groups.computeIfAbsent(item.getSpecificationId(), ignored -> new ArrayList<>())
                    .add(item);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, List<MallGoodsSpecificationEntity>> group : groups.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("specificationId", group.getKey());
            item.put("name", group.getValue().get(0).getValue());
            item.put("valueList", group.getValue());
            result.add(item);
        }
        return result;
    }

    /** 商品详情页数据 */
    public RestResponse<Object> detail(WxUserEntity loginUser, Integer id, Long referrer) {
        Map<String, Object> resultObj = new HashMap<>();
        Long userId = loginUser == null ? null : loginUser.getId();
        MallGoodsEntity info = goodsService.getById(id);
        if (info == null
                || !Integer.valueOf(0).equals(info.getIsDelete())
                || !Integer.valueOf(1).equals(info.getIsOnSale())) {
            throw new IllegalArgumentException("商品已下架或不存在");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("goodsId", id);
        Map<String, Object> specificationParam = new HashMap<>();
        specificationParam.put("fields", "gs.*, s.name");
        specificationParam.put("goodsId", id);
        specificationParam.put("specification", true);
        specificationParam.put("sidx", "s.sort_order");
        specificationParam.put("order", "asc");
        List<MallGoodsSpecificationEntity> goodsSpecificationEntityList =
                goodsSpecificationService.queryList(specificationParam);

        List<Map<String, Object>> specificationList =
                groupSpecifications(goodsSpecificationEntityList);
        List<MallProductEntity> productEntityList = productService.queryList(param);
        List<MallGoodsGalleryEntity> gallery = goodsGalleryService.queryList(param);
        Map<String, Object> ngaParam = new HashMap<>();
        ngaParam.put("fields", "nga.value, na.name");
        ngaParam.put("sidx", "nga.id");
        ngaParam.put("order", "asc");
        ngaParam.put("goodsId", id);
        List<MallAttributeEntity> attribute = attributeService.queryList(ngaParam);
        Map<String, Object> issueParam = new HashMap<>();
        List<MallGoodsIssueEntity> issue = goodsIssueService.queryList(issueParam);
        MallBrandEntity brand = brandService.getById(info.getBrandId());
        CustomerReviewService.ReviewPage approved = reviews.page(0, id, false, 1, 1);
        Map<String, Object> commentInfo = new LinkedHashMap<>();
        if (!approved.records().isEmpty()) {
            CustomerReviewService.PublicReview review = approved.records().get(0);
            commentInfo.put("id", review.id());
            commentInfo.put("content", review.content());
            commentInfo.put("userInfo", review.userInfo());
            commentInfo.put("picList", review.picList());
            commentInfo.put("nickname", review.userInfo().nickname());
            commentInfo.put("avatar", review.userInfo().avatar());
            commentInfo.put(
                    "addTime", DateUtils.timeToStr(review.addTime(), DateUtils.DATE_PATTERN));
        }
        Map<String, Object> comment = Map.of("count", approved.total(), "data", commentInfo);
        // 当前用户是否收藏
        long userHasCollect = 0L;
        if (userId != null) {
            userHasCollect =
                    collectService.count(
                            new QueryWrapper<MallCollectEntity>()
                                    .eq("USER_ID", userId)
                                    .eq("VALUE_ID", id)
                                    .eq("TYPE_ID", 0));
            if (userHasCollect > 0) {
                userHasCollect = 1L;
            }
            // 仅为已登录用户记录足迹 / Only authenticated users have footprints.
            MallFootprintEntity footprintEntity = new MallFootprintEntity();
            footprintEntity.setAddTime(System.currentTimeMillis() / 1000);
            footprintEntity.setGoodsId(info.getId());
            footprintEntity.setUserId(userId);
            footprintEntity.setReferrer(referrer == null ? 0L : referrer);
            footprintService.save(footprintEntity);
        }
        resultObj.put("info", info);
        resultObj.put("gallery", buildGalleryData(gallery, info));
        resultObj.put("attribute", attribute);
        resultObj.put("userHasCollect", userHasCollect);
        resultObj.put("issue", issue);
        resultObj.put("comment", comment);
        resultObj.put("brand", brand);
        resultObj.put("specificationList", specificationList);
        resultObj.put("productList", productEntityList);
        // 记录推荐人是否可以领取红包，用户登录时校验
        if (userId != null) {
            try {
                // 是否已经有可用的转发红包
                QueryWrapper<MallUserCouponEntity> userCouponQuery = new QueryWrapper<>();
                userCouponQuery.eq("USER_ID", userId).eq("COUPON_STATUS", 1);
                List<MallUserCouponEntity> userCouponList =
                        apiUserCouponService.list(userCouponQuery);
                Set<Integer> couponIdSet = new HashSet<>();
                for (MallUserCouponEntity userCoupon : userCouponList) {
                    if (userCoupon.getCouponId() != null) {
                        couponIdSet.add(userCoupon.getCouponId());
                    }
                }
                boolean hasSendTypeCoupon = false;
                if (!couponIdSet.isEmpty()) {
                    for (MallCouponEntity coupon : apiCouponService.listByIds(couponIdSet)) {
                        if (coupon != null && Integer.valueOf(2).equals(coupon.getSendType())) {
                            hasSendTypeCoupon = true;
                            break;
                        }
                    }
                }
                if (!hasSendTypeCoupon && referrer != null) {
                    // 获取优惠信息提示
                    Map<String, Object> couponParam = new HashMap<>();
                    couponParam.put("enabled", true);
                    Integer[] sendTypes = new Integer[] {2};
                    couponParam.put("sendTypes", sendTypes);
                    List<MallCouponEntity> couponVos = apiCouponService.queryList(couponParam);
                    if (null != couponVos && !couponVos.isEmpty()) {
                        MallCouponEntity couponVo = couponVos.get(0);
                        long footprintNum =
                                footprintService.count(
                                        new QueryWrapper<MallFootprintEntity>()
                                                .eq("goodsId", id)
                                                .eq("referrer", referrer));
                        if (footprintNum > 0
                                && null != couponVo.getMinTransmitNum()
                                && footprintNum > couponVo.getMinTransmitNum()) {
                            MallUserCouponEntity userCouponVo = new MallUserCouponEntity();
                            userCouponVo.setAddTime(new Date());
                            userCouponVo.setCouponId(couponVo.getId());
                            userCouponVo.setCouponNumber(CharUtil.getRandomString(12));
                            userCouponVo.setUserId(userId);
                            apiUserCouponService.save(userCouponVo);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("处理商品详情推荐券失败, goodsId={}, userId={}", id, userId, e);
            }
        }

        return RestResponse.ok(resultObj);
    }

    /**
     * 将图库二进制字段转换为小程序可用 URL, 并为空图库补充商品主图 / Converts gallery bytes to URLs with a product-image
     * fallback.
     */
    private List<Map<String, Object>> buildGalleryData(
            List<MallGoodsGalleryEntity> gallery, MallGoodsEntity goods) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MallGoodsGalleryEntity item : gallery) {
            String imageUrl =
                    item.getImgUrl() == null
                            ? ""
                            : new String(item.getImgUrl(), StandardCharsets.UTF_8).trim();
            if (imageUrl.isEmpty()) {
                continue;
            }
            Map<String, Object> image = new LinkedHashMap<>();
            image.put("id", item.getId());
            image.put("goodsId", item.getGoodsId());
            image.put("imgUrl", imageUrl);
            image.put("imgDesc", item.getImgDesc());
            image.put("sortOrder", item.getSortOrder());
            result.add(image);
        }
        if (result.isEmpty() && goods != null) {
            String fallbackUrl =
                    StringUtils.isNullOrEmpty(goods.getPrimaryPicUrl())
                            ? goods.getListPicUrl()
                            : goods.getPrimaryPicUrl();
            if (!StringUtils.isNullOrEmpty(fallbackUrl)) {
                Map<String, Object> fallback = new LinkedHashMap<>();
                fallback.put("id", 0);
                fallback.put("goodsId", goods.getId());
                fallback.put("imgUrl", fallbackUrl);
                fallback.put("imgDesc", goods.getName());
                fallback.put("sortOrder", 0);
                result.add(fallback);
            }
        }
        return result;
    }

    /** 　获取分类下的商品 */
    public RestResponse<Object> category(Integer id) {
        Map<String, Object> resultObj = new HashMap<>();
        MallCategoryEntity currentCategory = categoryService.getById(id);
        if (currentCategory == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        MallCategoryEntity parentCategory = categoryService.getById(currentCategory.getParentId());
        Map<String, Object> params = new HashMap<>();
        params.put("parentId", currentCategory.getParentId());
        List<MallCategoryEntity> brotherCategory = categoryService.queryList(params);
        resultObj.put("currentCategory", currentCategory);
        resultObj.put("parentCategory", parentCategory);
        resultObj.put("brotherCategory", brotherCategory);
        return RestResponse.ok(resultObj);
    }

    /** 　　获取商品列表 */
    public RestResponse<Object> list(
            WxUserEntity loginUser,
            Integer categoryId,
            Integer brandId,
            String keyword,
            Integer isNew,
            Integer isHot,
            Integer page,
            Integer size,
            String sort,
            String order) {
        if (page == null || page < 1 || size == null || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数不正确");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("isDelete", 0);
        params.put("isOnSale", 1);
        params.put("brandId", brandId);
        params.put("keyword", keyword);
        params.put("isNew", isNew);
        params.put("isHot", isHot);
        params.put("page", page);
        params.put("limit", size);
        params.put("order", sort);
        params.put("sidx", order);
        if (null != sort && "price".equals(sort)) {
            params.put("sidx", "retail_price");
            params.put("order", order);
        } else {
            params.put("sidx", "id");
            params.put("order", "desc");
        }
        // 添加到搜索历史
        if (!StringUtils.isNullOrEmpty(keyword)) {
            MallSearchHistoryEntity searchHistoryVo = new MallSearchHistoryEntity();
            searchHistoryVo.setAddTime(System.currentTimeMillis() / 1000);
            searchHistoryVo.setKeyword(keyword);
            searchHistoryVo.setUserId(null != loginUser ? loginUser.getId().toString() : "");
            searchHistoryVo.setFrom("");
            searchHistoryService.save(searchHistoryVo);
        }
        // 筛选的分类
        List<MallCategoryEntity> filterCategory = new ArrayList<>();
        MallCategoryEntity rootCategory = new MallCategoryEntity();
        rootCategory.setId(0);
        rootCategory.setName("全部");
        rootCategory.setChecked(false);
        filterCategory.add(rootCategory);
        params.put("fields", "category_id");
        Query<MallGoodsEntity> query = new Query<>(params);
        List<MallGoodsEntity> categoryEntityList = goodsService.queryList(query);
        params.remove("fields");
        if (null != categoryEntityList && !categoryEntityList.isEmpty()) {
            List<Integer> categoryIds = new ArrayList<>();
            for (MallGoodsEntity goodsVo : categoryEntityList) {
                categoryIds.add(goodsVo.getCategoryId());
            }
            // 查找二级分类的parentId
            Map<String, Object> categoryParam = new HashMap<>();
            categoryParam.put("ids", categoryIds);
            categoryParam.put("fields", "parent_id");
            List<MallCategoryEntity> parentCategoryList = categoryService.queryList(categoryParam);
            //
            List<Integer> parentIds = new ArrayList<>();
            for (MallCategoryEntity categoryEntity : parentCategoryList) {
                parentIds.add(categoryEntity.getParentId());
            }
            // 一级分类
            categoryParam = new HashMap<>();
            categoryParam.put("fields", "id,name");
            categoryParam.put("order", "asc");
            categoryParam.put("sidx", "sort_order");
            categoryParam.put("ids", parentIds);
            List<MallCategoryEntity> parentCategory = categoryService.queryList(categoryParam);
            if (null != parentCategory) {
                filterCategory.addAll(parentCategory);
            }
        }
        // 加入分类条件
        if (null != categoryId && categoryId > 0) {
            List<Integer> categoryIds = new ArrayList<>();
            Map<String, Object> categoryParam = new HashMap<>();
            categoryParam.put("parentId", categoryId);
            categoryParam.put("fields", "id");
            List<MallCategoryEntity> childIds = categoryService.queryList(categoryParam);
            for (MallCategoryEntity categoryEntity : childIds) {
                categoryIds.add(categoryEntity.getId());
            }
            categoryIds.add(categoryId);
            params.put("categoryIds", categoryIds);
        }
        // 查询列表数据
        params.put("fields", "id, name, list_pic_url, market_price, retail_price, goods_brief");

        query = new Query<>(params);
        List<MallGoodsEntity> list = goodsService.queryList(query);
        Page<MallGoodsEntity> goodsList = new Page<MallGoodsEntity>().setRecords(list);
        goodsList.setCurrent(page);
        goodsList.setTotal(goodsService.queryTotal(query));

        // 搜索到的商品
        for (MallCategoryEntity categoryEntity : filterCategory) {
            if (null != categoryId
                            && (categoryEntity.getId() == 0
                                    || categoryId.equals(categoryEntity.getId()))
                    || null == categoryId && null == categoryEntity.getId()) {
                categoryEntity.setChecked(true);
            } else {
                categoryEntity.setChecked(false);
            }
        }
        Map<String, Object> resultObj = new HashMap<>();
        resultObj.put("filterCategory", filterCategory);
        resultObj.put("goodsList", goodsList);

        return RestResponse.ok(resultObj);
    }

    /** 　　商品列表筛选的分类列表 */
    public RestResponse<Object> filter(
            Integer categoryId, String keyword, Integer isNew, Integer isHot) {
        Map<String, Object> params = new HashMap<>();
        params.put("isDelete", 0);
        params.put("isOnSale", 1);
        params.put("categoryId", categoryId);
        params.put("keyword", keyword);
        params.put("isNew", isNew);
        params.put("isHot", isHot);
        if (null != categoryId) {
            Map<String, Object> categoryParams = new HashMap<>();
            categoryParams.put("categoryId", categoryId);
            List<MallCategoryEntity> categoryEntityList = categoryService.queryList(categoryParams);
            List<Integer> categoryIds = new ArrayList<>();
            for (MallCategoryEntity categoryEntity : categoryEntityList) {
                categoryIds.add(categoryEntity.getId());
            }
            params.put("categoryId", categoryIds);
        }
        // 筛选的分类
        List<MallCategoryEntity> filterCategory = new ArrayList<>();
        MallCategoryEntity rootCategory = new MallCategoryEntity();
        rootCategory.setId(0);
        rootCategory.setName("全部");
        // 二级分类id
        List<MallGoodsEntity> goodsEntityList = goodsService.queryList(params);
        if (null != goodsEntityList && goodsEntityList.size() > 0) {
            List<Integer> categoryIds = new ArrayList<>();
            for (MallGoodsEntity goodsEntity : goodsEntityList) {
                categoryIds.add(goodsEntity.getCategoryId());
            }
            // 查找二级分类的parentId
            Map<String, Object> categoryParam = new HashMap<>();
            categoryParam.put("categoryIds", categoryIds);
            List<MallCategoryEntity> parentCategoryList = categoryService.queryList(categoryParam);
            //
            List<Integer> parentIds = new ArrayList<>();
            for (MallCategoryEntity categoryEntity : parentCategoryList) {
                parentIds.add(categoryEntity.getId());
            }
            // 一级分类
            categoryParam.put("categoryIds", parentIds);
            List<MallCategoryEntity> parentCategory = categoryService.queryList(categoryParam);
            if (null != parentCategory) {
                filterCategory.addAll(parentCategory);
            }
        }
        return RestResponse.ok(filterCategory);
    }

    /** 　　新品首发 */
    public RestResponse<Object> newAction() {
        Map<String, Object> resultObj = new HashMap<>();
        Map<String, Object> bannerInfo = new HashMap<>();
        bannerInfo.put("url", "");
        bannerInfo.put("name", "匠心设计，为你呈现璀璨珠宝");
        bannerInfo.put("imgUrl", "/static/images/demo/product-pearl-necklace-diy.webp");
        resultObj.put("bannerInfo", bannerInfo);
        return RestResponse.ok(resultObj);
    }

    /** 　　人气推荐 */
    public RestResponse<Object> hot() {
        Map<String, Object> resultObj = new HashMap<>();
        Map<String, Object> bannerInfo = new HashMap<>();
        bannerInfo.put("url", "");
        bannerInfo.put("name", "人气珠宝与灵感设计");
        bannerInfo.put("imgUrl", "/static/images/demo/product-akoya-necklace.webp");
        resultObj.put("bannerInfo", bannerInfo);
        return RestResponse.ok(resultObj);
    }

    /** 　　商品详情页的大家都在看的商品 */
    public RestResponse<Object> related(Integer id) {
        Map<String, Object> resultObj = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("GOODS_ID", id);
        List<MallRelatedGoodsEntity> relatedGoodsEntityList = relatedGoodsService.listByMap(param);

        List<Integer> relatedGoodsIds = new ArrayList<>();
        for (MallRelatedGoodsEntity relatedGoodsEntity : relatedGoodsEntityList) {
            relatedGoodsIds.add(relatedGoodsEntity.getRelatedGoodsId());
        }

        List<MallGoodsEntity> relatedGoods;

        if (relatedGoodsIds.isEmpty()) {
            // 查找同分类下的商品
            MallGoodsEntity goodsCategory = goodsService.getById(id);
            if (goodsCategory == null) {
                throw new IllegalArgumentException("商品不存在");
            }
            Map<String, Object> paramRelated = new HashMap<>();
            paramRelated.put("fields", "id, name, list_pic_url, retail_price");
            paramRelated.put("isDelete", 0);
            paramRelated.put("isOnSale", 1);
            paramRelated.put("categoryId", goodsCategory.getCategoryId());
            relatedGoods = goodsService.queryList(paramRelated);
        } else {
            Map<String, Object> paramRelated = new HashMap<>();
            paramRelated.put("goodsIds", relatedGoodsIds);
            paramRelated.put("fields", "id, name, list_pic_url, retail_price");
            paramRelated.put("isDelete", 0);
            paramRelated.put("isOnSale", 1);
            relatedGoods = goodsService.queryList(paramRelated);
        }
        resultObj.put("goodsList", relatedGoods);
        return RestResponse.ok(resultObj);
    }

    /** 　　在售的商品总数 */
    public RestResponse<Object> count() {
        Map<String, Object> resultObj = new HashMap<>();
        long goodsCount =
                goodsService.count(
                        new QueryWrapper<MallGoodsEntity>().eq("is_on_sale", 1).eq("is_delete", 0));
        resultObj.put("goodsCount", goodsCount);
        return RestResponse.ok(resultObj);
    }

    /** 　　获取商品列表 */
    public RestResponse<Object> productlist(
            WxUserEntity loginUser,
            Integer categoryId,
            Integer isNew,
            Integer discount,
            Integer page,
            Integer size,
            String sort,
            String order) {
        if (page == null || page < 1 || size == null || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数不正确");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("isNew", isNew);
        params.put("page", page);
        params.put("limit", size);
        params.put("order", sort);
        params.put("sidx", order);
        if ("price".equals(sort)) {
            params.put("sidx", "retail_price");
            params.put("order", order);
        } else if ("sell".equals(sort)) {
            params.put("sidx", "orderNum");
            params.put("order", order);
        } else {
            params.put("sidx", "id");
            params.put("order", "desc");
        }
        // 0不限 1特价 2团购
        if (null != discount && discount == 1) {
            params.put("isHot", 1);
        } else if (null != discount && discount == 2) {
            params.put("is_group", true);
        }
        // 加入分类条件
        if (null != categoryId && categoryId > 0) {
            List<Integer> categoryIds = new ArrayList<>();
            Map<String, Object> categoryParam = new HashMap<>();
            categoryParam.put("parentId", categoryId);
            categoryParam.put("fields", "id");
            List<MallCategoryEntity> childIds = categoryService.queryList(categoryParam);
            for (MallCategoryEntity categoryEntity : childIds) {
                categoryIds.add(categoryEntity.getId());
            }
            categoryIds.add(categoryId);
            params.put("categoryIds", categoryIds);
        }
        // 查询列表数据

        Query<MallGoodsEntity> query = new Query<>(params);
        List<MallGoodsEntity> list = goodsService.queryCatalogProductList(query);
        Page<MallGoodsEntity> data = new Page<MallGoodsEntity>().setRecords(list);
        data.setTotal(goodsService.count());
        data.setCurrent(page);

        Long userId = loginUser == null ? null : loginUser.getId();

        // 当前购物车中
        List<MallCartEntity> cartList = new ArrayList<>();
        if (null != userId) {
            // 查询列表数据
            Map<String, Object> cartParam = new HashMap<>();
            cartParam.put("userId", userId);
            cartList = cartService.queryList(cartParam);
        }
        if (null != cartList && cartList.size() > 0 && null != list && list.size() > 0) {
            for (MallGoodsEntity goodsVo : list) {
                for (MallCartEntity cartVo : cartList) {
                    if (goodsVo.getId().equals(cartVo.getGoodsId())) {
                        goodsVo.setCartNum(cartVo.getNumber());
                    }
                }
            }
        }
        return RestResponse.ok(data);
    }
}

package com.platform.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.Constant;
import com.platform.common.utils.RestResponse;
import com.platform.common.utils.SmsUtil;
import com.platform.common.utils.StringUtils;
import com.platform.modules.app.request.ApiUserBindMobileRequest;
import com.platform.modules.app.request.ApiUserProfileRequest;
import com.platform.modules.app.request.ApiUserSmsCodeRequest;
import com.platform.modules.app.service.MobileVerificationService;
import com.platform.modules.app.vo.LoginUserInfo;
import com.platform.modules.mall.service.LegalDocumentService;
import com.platform.modules.sys.entity.SmsConfig;
import com.platform.modules.sys.service.SysConfigService;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

/**
 * 会员操作接口
 *
 * @author pengjun
 */
@Tag(name = "会员操作-AppUserController")
@RestController
@RequestMapping("/app/user")
public class AppUserController extends AppBaseController {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final int MAX_NICKNAME_LENGTH = 32;
    private final WxUserService userService;
    private final SysConfigService sysConfigService;
    private final MobileVerificationService verification;
    private final LegalDocumentService legalDocuments;

    /** 注入本人资料、短信验证及当前协议检查, 不从客户端接收其他用户身份. */
    public AppUserController(
            WxUserService userService,
            SysConfigService sysConfigService,
            MobileVerificationService verification,
            LegalDocumentService legalDocuments) {
        this.userService = userService;
        this.sysConfigService = sysConfigService;
        this.verification = verification;
        this.legalDocuments = legalDocuments;
    }

    /** 发送短信验证码 */
    @Operation(summary = "发送短信")
    @PostMapping("smscode")
    public RestResponse<String> smscode(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiUserSmsCodeRequest request) {
        legalDocuments.requireAccepted(loginUser.getId());
        String phone = request.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            return RestResponse.fail("手机号不能为空");
        }
        if (!isValidMobile(phone)) {
            return RestResponse.fail("手机号格式不正确，仅支持中国大陆手机号");
        }
        String smsCode = verification.begin(loginUser.getId(), phone);
        SmsConfig config =
                sysConfigService.getConfigObject(Constant.SMS_CONFIG_KEY, SmsConfig.class);
        if (StringUtils.isNullOrEmpty(config)) {
            return RestResponse.fail("请先配置短信平台信息");
        }
        if (StringUtils.isNullOrEmpty(config.getAppid())) {
            return RestResponse.fail("请先配置短信平台APPID");
        }
        if (StringUtils.isNullOrEmpty(config.getAppkey())) {
            return RestResponse.fail("请先配置短信平台KEY");
        }
        if (StringUtils.isNullOrEmpty(config.getSign())) {
            return RestResponse.fail("请先配置短信平台签名");
        }
        try {
            SmsSingleSenderResult result =
                    SmsUtil.crSendSms(
                            config.getAppid(), config.getAppkey(), 0, "86", phone, smsCode);
            if (result.result != 0) {
                return RestResponse.fail("短信发送失败");
            }
        } catch (Exception e) {
            return RestResponse.fail("短信发送失败");
        }
        verification.issued(loginUser.getId(), phone, smsCode);
        return RestResponse.ok("短信发送成功");
    }

    /** 获取当前会员等级 */
    @Operation(summary = "获取当前会员等级")
    @PostMapping("getUserLevel")
    public RestResponse<String> getUserLevel(@LoginUser WxUserEntity loginUser) {
        return RestResponse.ok(
                StringUtils.isNullOrEmpty(loginUser.getUserLevelId())
                        ? "普通会员"
                        : loginUser.getUserLevelId());
    }

    /** 绑定手机 */
    @Operation(summary = "绑定手机")
    @PostMapping("bindMobile")
    public RestResponse<String> bindMobile(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiUserBindMobileRequest request) {
        legalDocuments.requireAccepted(loginUser.getId());
        String mobileCode = request.getMobileCode();
        String mobile = request.getMobile();
        if (StringUtils.isNullOrEmpty(mobile) || StringUtils.isNullOrEmpty(mobileCode)) {
            return RestResponse.fail("参数不完整");
        }
        if (!isValidMobile(mobile)) {
            return RestResponse.fail("手机号格式不正确，仅支持中国大陆手机号");
        }
        verification.consume(loginUser.getId(), mobile, mobileCode);
        WxUserEntity userEntity = userService.getById(loginUser.getId());
        if (userEntity == null) {
            return RestResponse.fail("用户不存在");
        }
        userEntity.setPhone(mobile);
        userService.update(
                null,
                new UpdateWrapper<WxUserEntity>().eq("ID", loginUser.getId()).set("PHONE", mobile));

        return RestResponse.ok("手机绑定成功");
    }

    /** 获取当前用户资料 / Returns the current user profile. */
    @Operation(summary = "获取用户资料")
    @PostMapping("profile")
    public RestResponse<LoginUserInfo> profile(@LoginUser WxUserEntity loginUser) {
        WxUserEntity userEntity = userService.getById(loginUser.getId());
        return userEntity == null
                ? RestResponse.fail("用户不存在")
                : RestResponse.ok(LoginUserInfo.from(userEntity));
    }

    /** 更新当前用户昵称和头像 / Updates the current user's nickname and avatar. */
    @Operation(summary = "更新用户资料")
    @PostMapping("profile/update")
    public RestResponse<LoginUserInfo> updateProfile(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiUserProfileRequest request) {
        legalDocuments.requireAccepted(loginUser.getId());
        if (request == null || StringUtils.isNullOrEmpty(request.getNickname())) {
            return RestResponse.fail("昵称不能为空");
        }
        String nickname = request.getNickname().trim();
        if (nickname.isEmpty() || nickname.length() > MAX_NICKNAME_LENGTH) {
            return RestResponse.fail("昵称长度应为1到32个字符");
        }
        WxUserEntity userEntity = userService.getById(loginUser.getId());
        if (userEntity == null) {
            return RestResponse.fail("用户不存在");
        }
        UpdateWrapper<WxUserEntity> update =
                new UpdateWrapper<WxUserEntity>()
                        .eq("ID", loginUser.getId())
                        .set("NICKNAME", nickname);
        if (!StringUtils.isNullOrEmpty(request.getAvatarUrl())) {
            update.set("HEAD_IMG_URL", request.getAvatarUrl().trim());
        }
        // 只写本次明确编辑的字段, 不回写旧手机号、积分或平台身份.
        userService.update(null, update);
        return RestResponse.ok(LoginUserInfo.from(userService.getById(loginUser.getId())));
    }

    /** 校验中国大陆手机号 / Validates a mainland China mobile number. */
    private boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
    }
}

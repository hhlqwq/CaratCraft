package com.platform;

import com.platform.modules.wx.service.WxUserService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@RequiredArgsConstructor
@SpringBootTest
@Disabled("需要真实微信用户数据，避免自动测试修改外部状态。")
public class PlatformAdminApplicationTests {

    private final WxUserService userService;


    @Test
    public void syncWxUsers() {
        userService.syncWxUsers();
        System.out.println("任务已建立");
    }
}

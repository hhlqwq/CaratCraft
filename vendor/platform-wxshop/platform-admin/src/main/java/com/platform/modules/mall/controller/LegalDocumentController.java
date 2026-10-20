package com.platform.modules.mall.controller;

import com.platform.common.annotation.SysLog;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.LegalDocumentService;
import com.platform.modules.sys.controller.AbstractController;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

/** 维护正文与发布版本分别授权, 所有发布动作写入管理审计日志. */
@RestController
@RequiredArgsConstructor
@RequestMapping("merchant/legal")
public class LegalDocumentController extends AbstractController {
  private final LegalDocumentService documents;

  /** 查看类别下最新修订与版本历史. */
  @GetMapping("/{kind}")
  @RequiresPermissions("merchant:legal:read")
  public RestResponse<Object> editor(@PathVariable String kind) {
    return RestResponse.ok(documents.editor(kind));
  }

  /** 追加草稿, 不直接改变消费者看到的正文. */
  @PostMapping
  @RequiresPermissions("merchant:legal:update")
  @SysLog("保存协议草稿")
  public RestResponse<Object> save(@RequestBody LegalDocumentService.Draft draft) {
    return RestResponse.ok(documents.save(draft, getUserId()));
  }

  /** 仅发布管理员已经核对的准确版本. */
  @PostMapping("/{kind}/{version}/publish")
  @RequiresPermissions("merchant:legal:publish")
  @SysLog("发布协议版本")
  public RestResponse<Object> publish(@PathVariable String kind, @PathVariable int version) {
    return RestResponse.ok(documents.publish(kind, version, getUserId()));
  }
}

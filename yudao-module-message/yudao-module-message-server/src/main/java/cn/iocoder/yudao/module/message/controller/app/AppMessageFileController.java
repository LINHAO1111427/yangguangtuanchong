package cn.iocoder.yudao.module.message.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadCompleteReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadCompleteRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadInitReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadInitRespVO;
import cn.iocoder.yudao.module.message.service.MessageFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "APP - 消息文件传输")
@RestController
@RequestMapping("/message/file")
@Validated
public class AppMessageFileController {

    @Resource
    private MessageFileService messageFileService;

    @PostMapping("/init")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "初始化分片上传")
    public CommonResult<FileUploadInitRespVO> initUpload(@Valid @RequestBody FileUploadInitReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(messageFileService.initUpload(userId, reqVO));
    }

    @PostMapping(value = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传文件分片")
    public CommonResult<Boolean> uploadChunk(
            @RequestParam("upload_id") @NotBlank String uploadId,
            @RequestParam("index") @NotNull @Min(0) Integer index,
            @RequestParam("total") @NotNull @Min(1) Integer total,
            @RequestParam("file") MultipartFile file) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        messageFileService.uploadChunk(userId, uploadId, index, total, file);
        return CommonResult.success(true);
    }

    @PostMapping("/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "完成分片上传并合并")
    public CommonResult<FileUploadCompleteRespVO> complete(@Valid @RequestBody FileUploadCompleteReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(messageFileService.completeUpload(userId, reqVO));
    }
}


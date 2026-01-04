package cn.iocoder.yudao.module.infra.controller.app.file;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.AppFileUploadReqVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadCompleteReqVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadInitReqVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadInitRespVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadPartUrlReqVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadPartUrlRespVO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 文件存储")
@RestController
@RequestMapping("/app-api/infra/file")
@Validated
@Slf4j
public class AppFileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @PermitAll
    public CommonResult<String> uploadFile(AppFileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        byte[] content = IoUtil.readBytes(file.getInputStream());
        return success(fileService.createFile(content, file.getOriginalFilename(),
                uploadReqVO.getDirectory(), file.getContentType()));
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "获取文件预签名地址（上传）", description = "模式二：前端上传文件：用于前端直接上传七牛、阿里云 OSS 等文件存储器")
    @Parameters({
            @Parameter(name = "name", description = "文件名称", required = true),
            @Parameter(name = "directory", description = "文件目录")
    })
    public CommonResult<FilePresignedUrlRespVO> getFilePresignedUrl(
            @RequestParam("name") String name,
            @RequestParam(value = "directory", required = false) String directory) {
        return success(fileService.presignPutUrl(name, directory));
    }

    @PostMapping("/create")
    @Operation(summary = "创建文件", description = "模式二：前端上传文件：配合 presigned-url 接口，记录已上传的文件")
    @PermitAll
    public CommonResult<Long> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return success(fileService.createFile(createReqVO));
    }

    @PostMapping("/multipart/init")
    @Operation(summary = "分片上传初始化", description = "返回 uploadId/path/推荐分片大小，用于大文件/视频直传")
    @PermitAll
    public CommonResult<MultipartUploadInitRespVO> initMultipart(@Valid @RequestBody MultipartUploadInitReqVO reqVO) {
        return success(fileService.initMultipartUpload(reqVO));
    }

    @GetMapping("/multipart/part-url")
    @Operation(summary = "获取分片上传 URL", description = "按照 partNumber 获取单片的预签名上传地址")
    @PermitAll
    public CommonResult<MultipartUploadPartUrlRespVO> getMultipartPartUrl(@Valid MultipartUploadPartUrlReqVO reqVO) {
        return success(fileService.getMultipartUploadPartUrl(reqVO));
    }

    @PostMapping("/multipart/complete")
    @Operation(summary = "完成分片上传", description = "将所有分片的 ETag 传入后完成合并")
    @PermitAll
    public CommonResult<Boolean> completeMultipart(@Valid @RequestBody MultipartUploadCompleteReqVO reqVO) {
        fileService.completeMultipartUpload(reqVO);
        return success(true);
    }

}

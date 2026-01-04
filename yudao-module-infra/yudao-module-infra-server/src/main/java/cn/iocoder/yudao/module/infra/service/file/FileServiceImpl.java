package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadCompleteReqVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadInitReqVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadInitRespVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadPartUrlReqVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadPartUrlRespVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.MultipartUploadPartVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.MultipartFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.MultipartFileClient.MultipartUploadContext;
import cn.iocoder.yudao.module.infra.framework.file.core.client.MultipartFileClient.MultipartUploadPart;
import cn.iocoder.yudao.module.infra.framework.file.core.utils.FileTypeUtils;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_STORAGE_NOT_SUPPORT_MULTIPART;

/**
 * 文件 Service 实现
 */
@Service
public class FileServiceImpl implements FileService {

    /**
     * 上传文件的前缀，是否包含日期（yyyyMMdd）
     */
    static boolean PATH_PREFIX_DATE_ENABLE = true;
    /**
     * 上传文件的后缀，是否包含时间戳
     */
    static boolean PATH_SUFFIX_TIMESTAMP_ENABLE = true;

    /** 推荐分片大小（8MB），兼顾 5k QPS 并发与上传耗时 */
    private static final long DEFAULT_PART_SIZE = 8L * 1024 * 1024;

    @Resource
    private FileConfigService fileConfigService;

    @Resource
    private FileMapper fileMapper;

    @Override
    public PageResult<FileDO> getFilePage(FilePageReqVO pageReqVO) {
        return fileMapper.selectPage(pageReqVO);
    }

    @Override
    @SneakyThrows
    public String createFile(byte[] content, String name, String directory, String type) {
        // 补齐类型与名称
        if (StrUtil.isEmpty(type)) {
            type = FileTypeUtils.getMineType(content, name);
        }
        if (StrUtil.isEmpty(name)) {
            name = DigestUtil.sha256Hex(content);
        }
        if (StrUtil.isEmpty(FileUtil.extName(name))) {
            String extension = FileTypeUtils.getExtension(type);
            if (StrUtil.isNotEmpty(extension)) {
                name = name + extension;
            }
        }

        String path = generateUploadPath(name, directory);
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        String url = client.upload(content, path, type);

        fileMapper.insert(new FileDO().setConfigId(client.getId())
                .setName(name).setPath(path).setUrl(url)
                .setType(type).setSize(content.length));
        return url;
    }

    @VisibleForTesting
    String generateUploadPath(String name, String directory) {
        String prefix = null;
        if (PATH_PREFIX_DATE_ENABLE) {
            prefix = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), PURE_DATE_PATTERN);
        }
        String suffix = null;
        if (PATH_SUFFIX_TIMESTAMP_ENABLE) {
            suffix = String.valueOf(System.currentTimeMillis());
        }

        if (StrUtil.isNotEmpty(suffix)) {
            String ext = FileUtil.extName(name);
            if (StrUtil.isNotEmpty(ext)) {
                name = FileUtil.mainName(name) + StrUtil.C_UNDERLINE + suffix + StrUtil.DOT + ext;
            } else {
                name = name + StrUtil.C_UNDERLINE + suffix;
            }
        }
        if (StrUtil.isNotEmpty(prefix)) {
            name = prefix + StrUtil.SLASH + name;
        }
        if (StrUtil.isNotEmpty(directory)) {
            name = directory + StrUtil.SLASH + name;
        }
        return name;
    }

    @Override
    @SneakyThrows
    public FilePresignedUrlRespVO presignPutUrl(String name, String directory) {
        String path = generateUploadPath(name, directory);
        FileClient fileClient = fileConfigService.getMasterFileClient();
        String uploadUrl = fileClient.presignPutUrl(path);
        String visitUrl = fileClient.presignGetUrl(path, null);
        return new FilePresignedUrlRespVO().setConfigId(fileClient.getId())
                .setPath(path).setUploadUrl(uploadUrl).setUrl(visitUrl);
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        FileClient fileClient = fileConfigService.getMasterFileClient();
        return fileClient.presignGetUrl(url, expirationSeconds);
    }

    @Override
    public Long createFile(FileCreateReqVO createReqVO) {
        createReqVO.setUrl(HttpUtils.removeUrlQuery(createReqVO.getUrl()));
        FileDO file = BeanUtils.toBean(createReqVO, FileDO.class);
        fileMapper.insert(file);
        return file.getId();
    }

    @Override
    public void deleteFile(Long id) throws Exception {
        FileDO file = validateFileExists(id);
        FileClient client = fileConfigService.getFileClient(file.getConfigId());
        Assert.notNull(client, "客户端 {} 不能为空", file.getConfigId());
        client.delete(file.getPath());
        fileMapper.deleteById(id);
    }

    @Override
    @SneakyThrows
    public void deleteFileList(List<Long> ids) {
        List<FileDO> files = fileMapper.selectByIds(ids);
        for (FileDO file : files) {
            FileClient client = fileConfigService.getFileClient(file.getConfigId());
            Assert.notNull(client, "客户端 {} 不能为空", file.getPath());
            client.delete(file.getPath());
        }
        fileMapper.deleteByIds(ids);
    }

    private FileDO validateFileExists(Long id) {
        FileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw exception(FILE_NOT_EXISTS);
        }
        return fileDO;
    }

    @Override
    public byte[] getFileContent(Long configId, String path) throws Exception {
        FileClient client = fileConfigService.getFileClient(configId);
        Assert.notNull(client, "客户端 {} 不能为空", configId);
        return client.getContent(path);
    }

    // ========== Multipart upload ==========

    @Override
    public MultipartUploadInitRespVO initMultipartUpload(MultipartUploadInitReqVO reqVO) {
        MultipartFileClient client = getMultipartClient();
        String path = generateUploadPath(reqVO.getName(), reqVO.getDirectory());
        MultipartUploadContext ctx = client.initiateMultipartUpload(path, reqVO.getContentType(), reqVO.getFileSize());

        MultipartUploadInitRespVO respVO = new MultipartUploadInitRespVO();
        respVO.setConfigId(client.getId());
        respVO.setUploadId(ctx.getUploadId());
        respVO.setPath(ctx.getPath());
        respVO.setPartSize(DEFAULT_PART_SIZE);
        respVO.setUrl(client.presignGetUrl(path, null));
        return respVO;
    }

    @Override
    public MultipartUploadPartUrlRespVO getMultipartUploadPartUrl(MultipartUploadPartUrlReqVO reqVO) {
        MultipartFileClient client = getMultipartClient();
        Duration duration = reqVO.getExpiresInSeconds() != null
                ? Duration.ofSeconds(reqVO.getExpiresInSeconds())
                : MultipartFileClient.DEFAULT_PART_URL_EXPIRATION;
        String uploadUrl = client.generatePresignedUploadPartUrl(reqVO.getPath(), reqVO.getUploadId(),
                reqVO.getPartNumber(), duration);
        MultipartUploadPartUrlRespVO respVO = new MultipartUploadPartUrlRespVO();
        respVO.setUploadUrl(uploadUrl);
        return respVO;
    }

    @Override
    public void completeMultipartUpload(MultipartUploadCompleteReqVO reqVO) {
        MultipartFileClient client = getMultipartClient();
        List<MultipartUploadPart> parts = reqVO.getParts().stream()
                .filter(Objects::nonNull)
                .map(part -> new MultipartUploadPart(part.getPartNumber(), part.getEtag()))
                .collect(Collectors.toList());
        client.completeMultipartUpload(reqVO.getPath(), reqVO.getUploadId(), parts);
    }

    private MultipartFileClient getMultipartClient() {
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        if (!(client instanceof MultipartFileClient multipartFileClient)) {
            throw exception(FILE_STORAGE_NOT_SUPPORT_MULTIPART);
        }
        return multipartFileClient;
    }

}

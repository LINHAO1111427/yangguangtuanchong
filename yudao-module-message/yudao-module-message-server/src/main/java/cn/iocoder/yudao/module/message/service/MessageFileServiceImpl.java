package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadCompleteReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadCompleteRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadInitReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadInitRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageFileServiceImpl implements MessageFileService {

    private static final String REDIS_PREFIX = "message:file:upload:";
    private static final Duration UPLOAD_TTL = Duration.ofHours(24);

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private FileApi fileApi;

    @Override
    public FileUploadInitRespVO initUpload(Long userId, FileUploadInitReqVO reqVO) {
        String uploadId = reqVO.getUploadId();
        if (StrUtil.isNotBlank(uploadId) && Boolean.TRUE.equals(stringRedisTemplate.hasKey(metaKey(uploadId)))) {
            return buildInitResp(uploadId);
        }

        uploadId = generateUploadId(userId);
        int totalChunks = calcTotalChunks(reqVO.getFileSize(), reqVO.getChunkSize());
        stringRedisTemplate.opsForHash().put(metaKey(uploadId), "userId", String.valueOf(userId));
        stringRedisTemplate.opsForHash().put(metaKey(uploadId), "fileName", reqVO.getFileName());
        stringRedisTemplate.opsForHash().put(metaKey(uploadId), "fileSize", String.valueOf(reqVO.getFileSize()));
        stringRedisTemplate.opsForHash().put(metaKey(uploadId), "chunkSize", String.valueOf(reqVO.getChunkSize()));
        stringRedisTemplate.opsForHash().put(metaKey(uploadId), "totalChunks", String.valueOf(totalChunks));
        if (StrUtil.isNotBlank(reqVO.getContentType())) {
            stringRedisTemplate.opsForHash().put(metaKey(uploadId), "contentType", reqVO.getContentType());
        }
        stringRedisTemplate.expire(metaKey(uploadId), UPLOAD_TTL);
        stringRedisTemplate.expire(chunksKey(uploadId), UPLOAD_TTL);

        FileUploadInitRespVO respVO = new FileUploadInitRespVO();
        respVO.setUploadId(uploadId);
        respVO.setChunkSize(reqVO.getChunkSize());
        respVO.setTotalChunks(totalChunks);
        respVO.setUploadedChunks(List.of());
        return respVO;
    }

    @Override
    public void uploadChunk(Long userId, String uploadId, Integer index, Integer total, MultipartFile file) {
        validateOwner(userId, uploadId);
        int totalChunks = getIntMeta(uploadId, "totalChunks", total);
        if (index == null || index < 0 || index >= totalChunks) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST);
        }
        Path dir = tempDir(uploadId);
        try {
            Files.createDirectories(dir);
            Path part = dir.resolve(String.format("%05d.part", index));
            file.transferTo(part.toFile());
            stringRedisTemplate.opsForSet().add(chunksKey(uploadId), String.valueOf(index));
            stringRedisTemplate.expire(chunksKey(uploadId), UPLOAD_TTL);
            stringRedisTemplate.expire(metaKey(uploadId), UPLOAD_TTL);
        } catch (IOException e) {
            log.error("[uploadChunk] store chunk failed uploadId={}, index={}", uploadId, index, e);
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public FileUploadCompleteRespVO completeUpload(Long userId, FileUploadCompleteReqVO reqVO) {
        String uploadId = reqVO.getUploadId();
        validateOwner(userId, uploadId);
        int totalChunks = getIntMeta(uploadId, "totalChunks", reqVO.getTotalChunks());
        Set<String> uploaded = stringRedisTemplate.opsForSet().members(chunksKey(uploadId));
        if (uploaded == null || uploaded.size() < totalChunks) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST);
        }

        Path dir = tempDir(uploadId);
        Path merged = dir.resolve("merged.bin");
        try (OutputStream out = Files.newOutputStream(merged)) {
            for (int i = 0; i < totalChunks; i++) {
                Path part = dir.resolve(String.format("%05d.part", i));
                try (InputStream in = Files.newInputStream(part)) {
                    in.transferTo(out);
                }
            }
        } catch (IOException e) {
            log.error("[completeUpload] merge failed uploadId={}", uploadId, e);
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        }

        String fileName = getMeta(uploadId, "fileName");
        String contentType = getMeta(uploadId, "contentType");
        try {
            byte[] bytes = Files.readAllBytes(merged);
            String url = fileApi.createFile(bytes, fileName, "message/file", contentType);
            cleanup(uploadId, dir);
            FileUploadCompleteRespVO resp = new FileUploadCompleteRespVO();
            resp.setUrl(url);
            resp.setFileName(fileName);
            resp.setSize((long) bytes.length);
            return resp;
        } catch (IOException e) {
            log.error("[completeUpload] read merged failed uploadId={}", uploadId, e);
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        }
    }

    private FileUploadInitRespVO buildInitResp(String uploadId) {
        FileUploadInitRespVO respVO = new FileUploadInitRespVO();
        respVO.setUploadId(uploadId);
        respVO.setChunkSize(getIntMeta(uploadId, "chunkSize", null));
        respVO.setTotalChunks(getIntMeta(uploadId, "totalChunks", null));
        Set<String> uploaded = stringRedisTemplate.opsForSet().members(chunksKey(uploadId));
        List<Integer> uploadedList = uploaded == null ? List.of() :
                uploaded.stream().map(Integer::valueOf).sorted().collect(Collectors.toList());
        respVO.setUploadedChunks(uploadedList);
        return respVO;
    }

    private void validateOwner(Long userId, String uploadId) {
        String owner = getMeta(uploadId, "userId");
        if (StrUtil.isBlank(owner) || !owner.equals(String.valueOf(userId))) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.FORBIDDEN);
        }
    }

    private String metaKey(String uploadId) {
        return REDIS_PREFIX + uploadId + ":meta";
    }

    private String chunksKey(String uploadId) {
        return REDIS_PREFIX + uploadId + ":chunks";
    }

    private Path tempDir(String uploadId) {
        String base = System.getProperty("java.io.tmpdir");
        return Paths.get(base, "message-upload", uploadId);
    }

    private String generateUploadId(Long userId) {
        return "u_" + userId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private int calcTotalChunks(Long fileSize, Integer chunkSize) {
        if (fileSize == null || chunkSize == null || chunkSize <= 0) {
            return 1;
        }
        return (int) ((fileSize + chunkSize - 1) / chunkSize);
    }

    private String getMeta(String uploadId, String field) {
        Object value = stringRedisTemplate.opsForHash().get(metaKey(uploadId), field);
        return value != null ? String.valueOf(value) : null;
    }

    private Integer getIntMeta(String uploadId, String field, Integer fallback) {
        String val = getMeta(uploadId, field);
        if (StrUtil.isBlank(val)) {
            return fallback;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void cleanup(String uploadId, Path dir) {
        try {
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                            }
                        });
            }
        } catch (IOException ignored) {
        }
        stringRedisTemplate.delete(List.of(metaKey(uploadId), chunksKey(uploadId)));
    }
}


package cn.iocoder.yudao.module.infra.framework.file.core.client;

import com.google.common.collect.ImmutableList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 扩展文件客户端，提供 S3 兼容的分片上传能�?
 *
 * @author 芋道源码
 */
public interface MultipartFileClient extends FileClient {

    Duration DEFAULT_PART_URL_EXPIRATION = Duration.ofMinutes(15);

    /**
     * 创建一个新的分片上传会�??
     *
     * @param path        对象路�?
     * @param contentType MIMEType
     * @param fileSize    文件大小，单位�?byte
     * @return 上传上下文
     */
    MultipartUploadContext initiateMultipartUpload(String path, String contentType, Long fileSize);

    /**
     * 生成指定分片的预签�?URL，前端可直接 PUT 到存储服务
     *
     * @param path       对象路�?
     * @param uploadId   会话ID
     * @param partNumber 分片编号，从 1 开始
     * @param expiresIn  URL �?效期
     * @return 预签名上传地址
     */
    default String generatePresignedUploadPartUrl(String path, String uploadId, int partNumber, Duration expiresIn) {
        throw new UnsupportedOperationException("当前文件客户端未实现分片上传");
    }

    /**
     * 完成分片上传
     *
     * @param path      对象路�?
     * @param uploadId  会话ID
     * @param partETags 分片 ETag 列表
     */
    void completeMultipartUpload(String path, String uploadId, List<MultipartUploadPart> partETags);

    /**
     * 取消分片上传
     */
    default void abortMultipartUpload(String path, String uploadId) {
        // 默认无需关�?由具�?实现决定是否支�?
    }

    class MultipartUploadContext {
        private final String uploadId;
        private final String path;

        public MultipartUploadContext(String uploadId, String path) {
            this.uploadId = uploadId;
            this.path = path;
        }

        public String getUploadId() {
            return uploadId;
        }

        public String getPath() {
            return path;
        }
    }

    class MultipartUploadPart {
        private final int partNumber;
        private final String etag;

        public MultipartUploadPart(int partNumber, String etag) {
            this.partNumber = partNumber;
            this.etag = etag;
        }

        public int getPartNumber() {
            return partNumber;
        }

        public String getEtag() {
            return etag;
        }

        public static List<MultipartUploadPart> immutableCopy(List<MultipartUploadPart> parts) {
            if (parts == null || parts.isEmpty()) {
                return Collections.emptyList();
            }
            return ImmutableList.copyOf(new ArrayList<>(parts));
        }
    }
}

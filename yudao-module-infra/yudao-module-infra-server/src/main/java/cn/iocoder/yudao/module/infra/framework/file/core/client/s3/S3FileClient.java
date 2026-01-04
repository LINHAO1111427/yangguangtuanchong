package cn.iocoder.yudao.module.infra.framework.file.core.client.s3;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.MultipartFileClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 S3 协议的文件客户端，兼容 MinIO/阿里云/腾讯云/七牛云/华为云等。
 *
 * <p>新增分片上传能力，用于大文件/视频的极速推流。</p>
 */
public class S3FileClient extends AbstractFileClient<S3FileClientConfig> implements MultipartFileClient {

    private static final Duration EXPIRATION_DEFAULT = Duration.ofHours(24);
    private static final Duration MULTIPART_EXPIRATION = Duration.ofMinutes(20);

    private S3Client client;
    private S3Presigner presigner;

    public S3FileClient(Long id, S3FileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 补全 domain
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        // 初始化 S3 客户端
        Region region = Region.of("us-east-1"); // 必填，但填什么都行；不填会报错
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.getAccessKey(), config.getAccessSecret()));
        URI endpoint = URI.create(buildEndpoint());
        S3Configuration serviceConfiguration = S3Configuration.builder() // Path-style 访问
                .pathStyleAccessEnabled(Boolean.TRUE.equals(config.getEnablePathStyleAccess()))
                .chunkedEncodingEnabled(false) // 禁用分块编码，提高兼容性
                .build();
        client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .endpointOverride(endpoint)
                .serviceConfiguration(serviceConfiguration)
                .build();
        presigner = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .endpointOverride(endpoint)
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .contentType(type)
                .contentLength((long) content.length)
                .build();
        client.putObject(putRequest, RequestBody.fromBytes(content));
        return presignGetUrl(path, null);
    }

    @Override
    public void delete(String path) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build();
        client.deleteObject(deleteRequest);
    }

    @Override
    public byte[] getContent(String path) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build();
        return IoUtil.readBytes(client.getObject(getRequest));
    }

    @Override
    public String presignPutUrl(String path) {
        return presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(EXPIRATION_DEFAULT)
                        .putObjectRequest(b -> b.bucket(config.getBucket()).key(path)).build())
                .url().toString();
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        String path = StrUtil.removePrefix(url, config.getDomain() + "/");
        path = HttpUtils.removeUrlQuery(path);

        if (!BooleanUtil.isFalse(config.getEnablePublicAccess())) {
            return config.getDomain() + "/" + path;
        }

        String finalPath = path;
        Duration expiration = expirationSeconds != null ? Duration.ofSeconds(expirationSeconds) : EXPIRATION_DEFAULT;
        URL signedUrl = presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(b -> b.bucket(config.getBucket()).key(finalPath)).build())
                .url();
        return signedUrl.toString();
    }

    // ========== Multipart upload extensions ==========

    @Override
    public MultipartUploadContext initiateMultipartUpload(String path, String contentType, Long fileSize) {
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .contentType(contentType)
                .build();
        var response = client.createMultipartUpload(request);
        return new MultipartUploadContext(response.uploadId(), path);
    }

    @Override
    public String generatePresignedUploadPartUrl(String path, String uploadId, int partNumber, Duration expiresIn) {
        Duration duration = expiresIn != null ? expiresIn : MULTIPART_EXPIRATION;
        UploadPartPresignRequest request = UploadPartPresignRequest.builder()
                .signatureDuration(duration)
                .uploadPartRequest(b -> b.bucket(config.getBucket())
                        .key(path)
                        .uploadId(uploadId)
                        .partNumber(partNumber))
                .build();
        return presigner.presignUploadPart(request).url().toString();
    }

    @Override
    public void completeMultipartUpload(String path, String uploadId, List<MultipartUploadPart> partETags) {
        List<CompletedPart> parts = MultipartUploadPart.immutableCopy(partETags).stream()
                .map(p -> CompletedPart.builder()
                        .partNumber(p.getPartNumber())
                        .eTag(p.getEtag())
                        .build())
                .collect(Collectors.toList());
        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build())
                .build();
        client.completeMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(String path, String uploadId) {
        AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .uploadId(uploadId)
                .build();
        client.abortMultipartUpload(request);
    }

    /**
     * 基于 bucket + endpoint 构建访问 Domain 地址
     */
    private String buildDomain() {
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return StrUtil.format("{}/{}", config.getEndpoint(), config.getBucket());
        }
        return StrUtil.format("https://{}.{}", config.getBucket(), config.getEndpoint());
    }

    /**
     * 节点地址补全协议
     */
    private String buildEndpoint() {
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return config.getEndpoint();
        }
        return StrUtil.format("https://{}", config.getEndpoint());
    }

}

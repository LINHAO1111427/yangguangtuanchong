package cn.iocoder.yudao.module.content.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentMediaUploadRespVO;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 视频上传功能单元测试
 *
 * @author Claude
 * @since 2025-01-08
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppContentMediaController 单元测试")
public class AppContentMediaControllerTest {

    @InjectMocks
    private AppContentMediaController controller;

    @Mock
    private FileApi fileApi;

    private static final String TEST_VIDEO_URL = "http://localhost:9000/xiaolvshu-dev/content/video/test.mp4";
    private static final String TEST_VIDEO_NAME = "test_video.mp4";
    private static final String VIDEO_CONTENT_TYPE = "video/mp4";

    @BeforeEach
    void setUp() {
        // 初始化设置
    }

    @Test
    @DisplayName("测试1：上传有效的MP4视频文件")
    void testUploadValidMp4Video() throws IOException {
        // 准备测试数据
        byte[] videoContent = createMockVideoContent(10 * 1024 * 1024); // 10MB
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                TEST_VIDEO_NAME,
                VIDEO_CONTENT_TYPE,
                videoContent
        );

        // Mock FileApi 返回
        when(fileApi.createFile(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(TEST_VIDEO_URL);

        // 执行测试
        CommonResult<ContentMediaUploadRespVO> result = controller.uploadVideo(videoFile, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(TEST_VIDEO_URL, result.getData().getUrl());
        assertEquals(TEST_VIDEO_NAME, result.getData().getFileName());
        assertEquals(VIDEO_CONTENT_TYPE, result.getData().getContentType());
        assertEquals(videoContent.length, result.getData().getSize());

        // 验证调用
        verify(fileApi, times(1)).createFile(any(byte[].class), eq(TEST_VIDEO_NAME), eq("content/video"), eq(VIDEO_CONTENT_TYPE));
    }

    @Test
    @DisplayName("测试2：上传自定义目录的视频")
    void testUploadVideoWithCustomDirectory() throws IOException {
        // 准备测试数据
        byte[] videoContent = createMockVideoContent(5 * 1024 * 1024); // 5MB
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "custom_video.mp4",
                VIDEO_CONTENT_TYPE,
                videoContent
        );
        String customDirectory = "content/user-videos";

        // Mock FileApi 返回
        when(fileApi.createFile(any(byte[].class), anyString(), eq(customDirectory), anyString()))
                .thenReturn(TEST_VIDEO_URL);

        // 执行测试
        CommonResult<ContentMediaUploadRespVO> result = controller.uploadVideo(videoFile, customDirectory);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证使用了自定义目录
        verify(fileApi, times(1)).createFile(any(byte[].class), anyString(), eq(customDirectory), anyString());
    }

    @Test
    @DisplayName("测试3：上传MOV格式视频")
    void testUploadMovVideo() throws IOException {
        // 准备测试数据
        byte[] videoContent = createMockVideoContent(15 * 1024 * 1024); // 15MB
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "test.mov",
                "video/quicktime",
                videoContent
        );

        // Mock FileApi 返回
        when(fileApi.createFile(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(TEST_VIDEO_URL);

        // 执行测试
        CommonResult<ContentMediaUploadRespVO> result = controller.uploadVideo(videoFile, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("video/quicktime", result.getData().getContentType());
    }

    @Test
    @DisplayName("测试4：上传空文件应失败")
    void testUploadEmptyFileShouldFail() {
        // 准备测试数据
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.mp4",
                VIDEO_CONTENT_TYPE,
                new byte[0]
        );

        // 执行测试并验证异常
        Exception exception = assertThrows(Exception.class, () -> {
            controller.uploadVideo(emptyFile, null);
        });

        // 验证没有调用 FileApi
        verify(fileApi, never()).createFile(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("测试5：上传null文件应失败")
    void testUploadNullFileShouldFail() {
        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            controller.uploadVideo(null, null);
        });

        // 验证没有调用 FileApi
        verify(fileApi, never()).createFile(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("测试6：上传超大文件(>600MB)应失败")
    void testUploadOversizedFileShouldFail() {
        // 准备测试数据 - 模拟601MB文件
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.mp4",
                VIDEO_CONTENT_TYPE,
                new byte[0] // 实际大小通过 getSize() mock
        ) {
            @Override
            public long getSize() {
                return 601L * 1024 * 1024; // 601MB
            }
        };

        // 执行测试并验证异常
        Exception exception = assertThrows(Exception.class, () -> {
            controller.uploadVideo(largeFile, null);
        });

        // 验证没有调用 FileApi
        verify(fileApi, never()).createFile(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("测试7：上传不支持的文件格式应失败")
    void testUploadUnsupportedFormatShouldFail() {
        // 准备测试数据 - 不支持的格式
        byte[] videoContent = createMockVideoContent(10 * 1024 * 1024);
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.avi",
                "video/x-msvideo", // 不在允许列表中
                videoContent
        );

        // 执行测试并验证异常
        Exception exception = assertThrows(Exception.class, () -> {
            controller.uploadVideo(invalidFile, null);
        });

        // 验证没有调用 FileApi
        verify(fileApi, never()).createFile(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("测试8：上传600MB边界文件应成功")
    void testUpload600MBFileShouldSuccess() throws IOException {
        // 准备测试数据 - 正好600MB
        MockMultipartFile boundaryFile = new MockMultipartFile(
                "file",
                "boundary.mp4",
                VIDEO_CONTENT_TYPE,
                new byte[0]
        ) {
            @Override
            public long getSize() {
                return 600L * 1024 * 1024; // 正好600MB
            }

            @Override
            public byte[] getBytes() {
                return createMockVideoContent(1024);
            }

            @Override
            public ByteArrayInputStream getInputStream() {
                return new ByteArrayInputStream(createMockVideoContent(1024));
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };

        // Mock FileApi 返回
        when(fileApi.createFile(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(TEST_VIDEO_URL);

        // 执行测试
        CommonResult<ContentMediaUploadRespVO> result = controller.uploadVideo(boundaryFile, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("测试9：上传WEBM格式视频")
    void testUploadWebmVideo() throws IOException {
        // 准备测试数据
        byte[] videoContent = createMockVideoContent(20 * 1024 * 1024);
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "test.webm",
                "video/webm",
                videoContent
        );

        // Mock FileApi 返回
        when(fileApi.createFile(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(TEST_VIDEO_URL);

        // 执行测试
        CommonResult<ContentMediaUploadRespVO> result = controller.uploadVideo(videoFile, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("video/webm", result.getData().getContentType());
    }

    @Test
    @DisplayName("测试10：FileApi异常处理")
    void testFileApiExceptionHandling() throws IOException {
        // 准备测试数据
        byte[] videoContent = createMockVideoContent(10 * 1024 * 1024);
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                TEST_VIDEO_NAME,
                VIDEO_CONTENT_TYPE,
                videoContent
        );

        // Mock FileApi 抛出异常
        when(fileApi.createFile(any(byte[].class), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("MinIO连接失败"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            controller.uploadVideo(videoFile, null);
        });
    }

    /**
     * 创建模拟视频内容
     */
    private byte[] createMockVideoContent(int size) {
        byte[] content = new byte[size];
        // 填充一些模拟数据
        for (int i = 0; i < Math.min(size, 1000); i++) {
            content[i] = (byte) (i % 256);
        }
        return content;
    }
}

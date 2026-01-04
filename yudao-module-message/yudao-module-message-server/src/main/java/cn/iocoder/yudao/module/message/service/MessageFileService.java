package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadCompleteReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadCompleteRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadInitReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.file.FileUploadInitRespVO;
import org.springframework.web.multipart.MultipartFile;

public interface MessageFileService {

    FileUploadInitRespVO initUpload(Long userId, FileUploadInitReqVO reqVO);

    void uploadChunk(Long userId, String uploadId, Integer index, Integer total, MultipartFile file);

    FileUploadCompleteRespVO completeUpload(Long userId, FileUploadCompleteReqVO reqVO);
}


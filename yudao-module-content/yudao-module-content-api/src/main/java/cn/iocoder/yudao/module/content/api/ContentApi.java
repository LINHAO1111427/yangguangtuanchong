package cn.iocoder.yudao.module.content.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.content.api.dto.ContentRespDTO;
import cn.iocoder.yudao.module.content.api.dto.ContentUserStatsRespDTO;
import cn.iocoder.yudao.module.content.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

/**
 * Content API 接口
 *
 * @author xiaolvshu
 */
@FeignClient(name = ApiConstants.NAME)
public interface ContentApi {

    String PREFIX = ApiConstants.PREFIX + "/content";

    /**
     * 获得内容信息
     *
     * @param id 内容编号
     * @return 内容信息
     */
    @GetMapping(PREFIX + "/get")
    CommonResult<ContentRespDTO> getContent(@RequestParam("id") Long id);

    /**
     * 获得内容信息列表
     *
     * @param ids 内容编号列表
     * @return 内容信息列表
     */
    @GetMapping(PREFIX + "/list")
    CommonResult<List<ContentRespDTO>> getContentList(@RequestParam("ids") Collection<Long> ids);

    /**
     * 根据用户ID获取内容列表
     *
     * @param userId 用户编号
     * @return 内容列表
     */
    @GetMapping(PREFIX + "/get-by-user")
    CommonResult<List<ContentRespDTO>> getContentListByUserId(@RequestParam("userId") Long userId);

    /**
     * 获取作者侧的聚合统计数据
     *
     * @param userId 用户编号
     * @return 统计信息
     */
    @GetMapping(PREFIX + "/author-stats")
    CommonResult<ContentUserStatsRespDTO> getAuthorStats(@RequestParam("userId") Long userId);

}

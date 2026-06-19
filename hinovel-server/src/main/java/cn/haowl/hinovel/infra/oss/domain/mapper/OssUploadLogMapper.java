package cn.haowl.hinovel.infra.oss.domain.mapper;

import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * OSS 上传日志 Mapper。
 *
 * <p>提供上传日志数据的 CRUD 操作，继承 MyBatis-Plus BaseMapper。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Mapper
public interface OssUploadLogMapper extends BaseMapper<OssUploadLog> {
}

package com.codereview.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codereview.review.entity.ReviewTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 代码审查任务Mapper
 * @author CodeReview
 */
@Mapper
public interface ReviewTaskMapper extends BaseMapper<ReviewTask> {
}

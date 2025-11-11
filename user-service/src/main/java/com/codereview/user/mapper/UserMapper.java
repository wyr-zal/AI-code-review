package com.codereview.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codereview.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 * @author CodeReview
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

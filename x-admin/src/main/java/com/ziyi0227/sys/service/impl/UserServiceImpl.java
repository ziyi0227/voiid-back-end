package com.ziyi0227.sys.service.impl;

import com.ziyi0227.sys.entity.User;
import com.ziyi0227.sys.mapper.UserMapper;
import com.ziyi0227.sys.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ziyi0227
 * @since 2023-10-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}

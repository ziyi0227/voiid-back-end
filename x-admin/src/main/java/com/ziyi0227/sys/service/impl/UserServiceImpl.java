package com.ziyi0227.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziyi0227.common.vo.Result;
import com.ziyi0227.sys.entity.User;
import com.ziyi0227.sys.mapper.UserMapper;
import com.ziyi0227.sys.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> login(User user) {
        //根据用户名和密码查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,user.getUsername());
        wrapper.eq(User::getPassword,user.getPassword());
        User loginUser = this.baseMapper.selectOne(wrapper);
        //查询结果不为空，输出token,并将token存入redis
        if (loginUser != null){
            //暂时用UUID，后期改为JWT
            String key = "user" + UUID.randomUUID();

            //存入redis
            loginUser.setPassword(null);
            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);

            //返回token
            Map<String,Object> data = new HashMap<>();
            data.put("token",key);
            return data;
        }
        return null;
    }
}

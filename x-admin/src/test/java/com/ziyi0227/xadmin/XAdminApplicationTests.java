package com.ziyi0227.xadmin;

import com.ziyi0227.sys.entity.User;
import com.ziyi0227.sys.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class XAdminApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Test
    void textMapper() {
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);
    }

}

package com.ziyi0227.xadmin;

import com.ziyi0227.common.utils.JwtUtil;
import com.ziyi0227.sys.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JwtUtilText {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void testCreateJwt(){
        User user = new User();
        user.setUsername("zhangsan");
        user.setPhone("123456789");
        String jwt = jwtUtil.createToken(user);
        System.out.println(jwt);
    }

    @Test
    public void testParesJwt(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJjMGNiNTllOC0yZGQxLTRmNDgtOTAwMC1hNjcxMjJkOTVlYTkiLCJzdWIiOiJ7XCJwaG9uZVwiOlwiMTIzNDU2Nzg5XCIsXCJ1c2VybmFtZVwiOlwiemhhbmdzYW5cIn0iLCJpc3MiOiJzeXN0ZW0iLCJpYXQiOjE2OTg3MTQ3MTAsImV4cCI6MTY5ODcxNjUxMH0.HpsuuOcqi7NE7ovf1bNJFH3mfZPwKowft7qRrrvNq6E";
        Claims claims = jwtUtil.parseToken(token);
        System.out.println(claims);
    }

    @Test
    public void testParesJwt2(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJjMGNiNTllOC0yZGQxLTRmNDgtOTAwMC1hNjcxMjJkOTVlYTkiLCJzdWIiOiJ7XCJwaG9uZVwiOlwiMTIzNDU2Nzg5XCIsXCJ1c2VybmFtZVwiOlwiemhhbmdzYW5cIn0iLCJpc3MiOiJzeXN0ZW0iLCJpYXQiOjE2OTg3MTQ3MTAsImV4cCI6MTY5ODcxNjUxMH0.HpsuuOcqi7NE7ovf1bNJFH3mfZPwKowft7qRrrvNq6E";
        User user = jwtUtil.parseToken(token, User.class);
        System.out.println(user);
    }
}

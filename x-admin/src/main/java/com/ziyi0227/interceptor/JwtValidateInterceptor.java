package com.ziyi0227.interceptor;

import com.alibaba.fastjson2.JSON;
import com.ziyi0227.common.utils.JwtUtil;
import com.ziyi0227.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtValidateInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("X-Token");
        log.debug(request.getRequestURI()+"需要验证："+token);
        if (token != null){
            try {
                jwtUtil.parseToken(token);
                log.debug(request.getRequestURI()+"验证成功！");
                return true; // 放行
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        log.debug(request.getRequestURI()+"验证失败");
        response.setContentType("application/json;charset=utf-8");
        Result<Object> fail = Result.fail(20003, "jwt失效，请重新登录");
        response.getWriter().write(JSON.toJSONString(fail));
        return false; // 拦截
    }
}

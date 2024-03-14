package com.ziyi0227.sys.service.impl;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziyi0227.common.utils.JwtUtil;
import com.ziyi0227.sys.entity.Menu;
import com.ziyi0227.sys.entity.User;
import com.ziyi0227.sys.entity.UserRole;
import com.ziyi0227.sys.mapper.UserMapper;
import com.ziyi0227.sys.mapper.UserRoleMapper;
import com.ziyi0227.sys.service.IMenuService;
import com.ziyi0227.sys.service.IUserService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private RestTemplateBuilder builder;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Autowired
    private IMenuService menuService;

    @Override
    public Map<String, Object> login(User user) {
        //根据用户名查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,user.getUsername());
        User loginUser = this.baseMapper.selectOne(wrapper);
        //查询结果不为空，并且密码与传入密码匹配，输出token,并将token存入redis
        if (loginUser != null && passwordEncoder.matches(user.getPassword(),loginUser.getPassword())){
            //暂时用UUID，后期改为JWT
            // String key = "user" + UUID.randomUUID();

            //存入redis
            loginUser.setPassword(null);
            // redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);

            //创建jwt
            String token = jwtUtil.createToken(loginUser);

            //返回token
            Map<String,Object> data = new HashMap<>();
            data.put("token",token);
            return data;
        }
        return null;
    }

    /*@Override
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
    }*/

    @Override
    public Map<String, Object> getUserInfo(String token) {
        //根据token查询redis
        // Object obj = redisTemplate.opsForValue().get(token);
        User loginUser = null;
        try {
            loginUser = jwtUtil.parseToken(token, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(loginUser != null){
            //返回用户信息
            // User loginUser = JSON.parseObject(JSON.toJSONString(obj),User.class);
            Map<String,Object> data = new HashMap<>();
            data.put("name",loginUser.getUsername());
            data.put("avatar",loginUser.getAvatar());

            //返回用户角色
            List<String> roleList = this.baseMapper.getRoleNameByUserId(loginUser.getId());
            data.put("roles",roleList);

            // 权限列表
            List<Menu> menuList = menuService.getMenuListByUserId(loginUser.getId());
            data.put("menuList",menuList);

            return data;
        }
        return null;
    }

    @Override
    public void logout(String token) {
        // redisTemplate.delete(token);
    }

    @Override
    @Transactional
    public void addUser(User user) {
        this.baseMapper.insert(user);
        List<Integer> roleIdList = user.getRoleIdList();
        if(roleIdList != null){
            for (Integer roleId : roleIdList) {
                userRoleMapper.insert(new UserRole(user.getId(),roleId,null));
            }
        }
    }

    @Override
    public User getUserById(Integer id) {
        User user = this.baseMapper.selectById(id);
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId,id);
        List<UserRole> userRoleList = userRoleMapper.selectList(wrapper);

        List<Integer> roleIdList = userRoleList.stream()
                                               .map(userRole -> {return userRole.getRoleId();})
                                               .collect(Collectors.toList());
        user.setRoleIdList(roleIdList);

        return user;
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        this.baseMapper.updateById(user);

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId,user.getId());
        userRoleMapper.delete(wrapper);

        List<Integer> roleIdList = user.getRoleIdList();
        if(roleIdList != null){
            for (Integer roleId : roleIdList) {
                userRoleMapper.insert(new UserRole(user.getId(),roleId,null));
            }
        }
    }

    @Override
    public void deleteUserById(Integer id) {
        this.baseMapper.deleteById(id);
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId,id);
        userRoleMapper.delete(wrapper);
    }

    @Override
    public Map<String, Object> loginByVoice(MultipartFile audioFile) {
        // 打印文件名和大小
        System.out.println("File name: " + audioFile.getOriginalFilename());
        System.out.println("File size: " + audioFile.getSize());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        try {
            File file = convertMultiPartToFile(audioFile);
            body.add("audio", new FileSystemResource(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 打印请求体内容
        System.out.println("Request body: " + body);

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .exchange("http://localhost:5000/predict", HttpMethod.POST, requestEntity, String.class);

        // // 删除临时文件
        // if (file != null && file.exists()) {
        //     file.delete();
        // }

        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 解析JSON响应
                JSONObject jsonResponse = JSONObject.parseObject(response.getBody());
                // 获取most_id作为username进行查询
                String username = jsonResponse.getString("most_similar_id");
                String similarity = jsonResponse.getString("similarity");
                // 根据用户名查询用户信息
                LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(User::getUsername, username);
                User loginUser = this.baseMapper.selectOne(wrapper);

                if (loginUser != null){
                    //存入redis
                    loginUser.setPassword(null);
                    // redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);

                    //创建jwt
                    String token = jwtUtil.createToken(loginUser);

                    //返回token
                    Map<String,Object> data = new HashMap<>();
                    data.put("similarity",similarity);
                    data.put("token",token);
                    return data;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // 返回错误信息或抛出异常
            }
        }
        // 处理请求失败的情况，可以返回错误信息或抛出异常
        return null;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile); // Transfer the MultipartFile to the File
        return convFile;
    }
}




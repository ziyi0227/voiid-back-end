package com.ziyi0227.sys.service;

import com.ziyi0227.sys.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ziyi0227
 * @since 2023-10-15
 */
public interface IUserService extends IService<User> {

    Map<String, Object> login(User user);

    Map<String, Object> getUserInfo(String token);

    void logout(String token);

    void addUser(User user);

    User getUserById(Integer id);

    void updateUser(User user);

    void deleteUserById(Integer id);

    Map<String, Object> loginByVoice(MultipartFile audioFile);
}

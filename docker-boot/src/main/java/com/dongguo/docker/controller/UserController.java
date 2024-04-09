package com.dongguo.docker.controller;

import cn.hutool.core.util.IdUtil;
import com.dongguo.docker.entity.User;
import com.dongguo.docker.entity.UserDTO;
import com.dongguo.docker.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Random;

@Api(description = "用户User接口")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("数据库新增记录")
    @PostMapping(value = "/user/add")
    public void addUser() {
        for (int i = 1; i <= 3; i++) {
            User user = new User();

            user.setUsername("dongguo" + i);
            user.setPassword(IdUtil.simpleUUID().substring(0, 6));
            user.setSex((byte) new Random().nextInt(2));

            userService.addUser(user);
        }
    }

    @ApiOperation("删除记录")
    @PostMapping(value = "/user/delete/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }

    @ApiOperation("修改记录")
    @PostMapping(value = "/user/update")
    public void updateUser(@RequestBody UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        userService.updateUser(user);
    }

    @ApiOperation("查询1条记录")
    @GetMapping(value = "/user/find/{id}")
    public User findUserById(@PathVariable Integer id) {
        return userService.findUserById(id);
    }
}

package com.haust.aispringboot.util;

import cn.hutool.json.JSONUtil;
import com.haust.aispringboot.DTO.response.UserLoginResponseDTO;
import com.haust.aispringboot.common.ResultCode;
import com.haust.aispringboot.config.SecurityConfig;
import com.haust.aispringboot.enumClass.UserStatus;
import com.haust.aispringboot.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private UserService userService;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        //检查请求路径是否为公开路径，如果是则不进行JWT认证
        return SecurityConfig.isPublicPath(requestURI);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        //获取请求的URI和请求方法
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        System.out.println("请求URI: " + requestURI);
        System.out.println("请求方法: " + requestMethod);
        //1.从请求头中获取JWT token
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            //2.验证JWT token并获取用户信息
            JwtTokenUtil.TokenVerificationResult validationResult = JwtTokenUtil.validateToken(token);
            if (validationResult != null && validationResult.isValid()) {
                //3.查询用户信息验证用户的状态
                UserLoginResponseDTO.UserDetailResponseDTO user = userService.getUserById(validationResult.getUserId());
                System.out.println(JSONUtil.parseObj(user));

                if(user !=null && UserStatus.NORMAL.getCode().equals(user.getStatus())){
                    //4.创建Spring Security认证对象.1. 构造权限集合：给 roleType 加 Spring Security 要求的 "ROLE_" 前缀
                            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                    new SimpleGrantedAuthority("ROLE_"+validationResult.getRoleType()));

                     //5.创建UsernamePasswordAuthenticationToken（Spring Security 的标准认证对象）
                     UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                             validationResult.getUsername(),// principal：用户唯一标识（这里用用户名）
                             null, // credentials：密码（JWT已验证，设为null即可）
                             authorities);// 权限集合

                     //6.设置认证信息到Spring Security上下文-整个后续请求线程都能拿到认证信息
                     SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                     //将token存储到请求头中
                     request.setAttribute("jwtToken",token);
                }else{
                    //清理上下文
                    clearSecurityContext();
                    //返回错误响应
                    ResponseUtil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
                    return;
                }

            }else {
                //清理上下文
                clearSecurityContext();
                //返回错误响应
                ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
                return;
            }
        }else {
        //清理上下文
        clearSecurityContext();
        chain.doFilter(request, response);
        return;
        }
        //继续过滤器链
        chain.doFilter(request,response);

        }

    //清理Spring Security上下文
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}

package com.project.demo.interceptor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录拦截器 - 用于处理用户认证和权限验证
 * 
 * 该拦截器实现了Spring的HandlerInterceptor接口，用于在请求处理之前进行认证检查。
 * 主要功能包括：
 * 1. 验证用户访问令牌(Token)
 * 2. 放行公共接口（登录、注册、状态查询）
 * 3. 处理跨域请求
 * 4. 记录请求日志
 * 
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 请求头中携带访问令牌的字段名称
     */
    private String tokenName = "x-auth-token";

    /**
     * 请求预处理方法 - 在控制器方法执行之前调用
     * 
     * 主要逻辑：
     * 1. 设置CORS响应头以支持跨域请求
     * 2. 记录请求日志
     * 3. 放行公共接口（登录、注册、状态查询）
     * 4. 验证访问令牌的存在性
     * 5. 对于POST请求强制要求Token，GET请求允许无Token访问
     * 
     * @param request HTTP请求对象，包含请求头、参数等信息
     * @param response HTTP响应对象，用于设置响应头和状态码
     * @param handler 处理器对象，代表被拦截的请求处理器
     * @return boolean true表示继续执行后续的拦截器和控制器，false表示中断请求
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取访问令牌
        String token = request.getHeader(this.tokenName);

        // 设置CORS响应头，支持跨域请求
        setHeader(request, response);
        
        // 记录请求日志，包含请求URL和HTTP方法
        log.info("[请求接口] - {} , [请求类型] - {}",request.getRequestURL().toString(),request.getMethod());
        
        // 白名单：放行用户登录接口
        if (request.getRequestURL().toString().contains("/api/user/login")){
            return true;
        }
        // 白名单：放行用户状态查询接口
        else if (request.getRequestURL().toString().contains("/api/user/state")){
            return true;
        }
        // 白名单：放行用户注册接口
        else if (request.getRequestURL().toString().contains("/api/user/register")){
            return true;
        }
        
        // 检查访问令牌是否存在
        if (token == null || "".equals(token)){
            // 对于POST请求，没有Token则拒绝访问
            if ("POST".equals(request.getMethod())){
                return false;
            }else {
                // 对于GET请求，允许无Token访问（可能用于浏览等操作）
                return true;
            }
        }else {
            // 存在Token，允许继续访问
            return true;
        }
    }

    /**
     * 请求后处理方法 - 在控制器方法执行完成后，视图渲染之前调用
     * 
     * 当前实现为空，主要用于：
     * 1. 更新Token有效期
     * 2. 记录响应日志
     * 3. 对响应进行后处理
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @param modelAndView 模型和视图对象
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // TODO: 实现Token更新逻辑，如Token续期等
        // 可以在这里更新Token的有效期，或者记录响应相关信息
    }

    /**
     * 请求完成处理方法 - 在整个请求处理完成后调用，无论是否发生异常
     * 
     * 主要用于：
     * 1. 清理资源
     * 2. 记录异常日志
     * 3. 进行最终的后处理操作
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @param ex 请求处理过程中发生的异常（如果没有异常则为null）
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // TODO: 实现异常处理和资源清理逻辑
        // 可以记录异常信息，或者进行清理操作
    }

    /**
     * 处理认证失败的情况
     * 
     * 当前实现有问题：使用了错误的重定向方式。
     * 在RESTful API中，认证失败应该返回401状态码和错误信息，而不是重定向。
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws IOException 可能发生的IO异常
     */
    private void failure(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置响应内容类型为JSON格式，编码为UTF-8
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        // 设置HTTP状态码为401（未授权）
        response.setStatus(401);
        // 注意：这里应该返回JSON错误信息，而不是重定向
        // response.getWriter().write("{\"code\":401,\"message\":\"认证失败，请登录\"}");
        
        // 错误的方式：在API接口中不应该重定向到外部网站
        response.sendRedirect("https://www.baidu.com");
    }

    /**
     * 设置CORS响应头，支持跨域请求
     * 
     * CORS（Cross-Origin Resource Sharing）跨域资源共享配置，
     * 允许浏览器向跨源服务器发出XMLHttpRequest请求。
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    private void setHeader(HttpServletRequest request, HttpServletResponse response) {
        // 允许的源地址（跨域来源）
        response.setHeader("Access-control-Allow-Origin", request.getHeader("Origin"));
        
        // 允许的HTTP方法
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
        
        // 允许携带认证信息
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        // 允许的自定义请求头
        response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
        
        // 预检请求的缓存时间（30分钟）
        response.setHeader("Access-Control-Max-Age", "1800");
        
        // 防止乱码，适用于传输JSON数据
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        
        // 设置响应状态码为200（正常）
        response.setStatus(HttpStatus.OK.value());
    }

}

package com.example.planvista.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理者権限チェックインターセプター
 * 管理者専用ページへのアクセス時に権限をチェック
 */
@Component
public class Adminauthinterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        
        // セッションが存在しない、または管理者フラグがfalseの場合
        if (session == null || session.getAttribute("isAdmin") == null) {
            response.sendRedirect("/login_admin?error=unauthorized");
            return false;
        }
        
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (!isAdmin) {
            response.sendRedirect("/login_admin?error=unauthorized");
            return false;
        }
        
        return true;
    }
}
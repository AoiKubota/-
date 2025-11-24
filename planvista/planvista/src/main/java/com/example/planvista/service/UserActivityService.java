package com.example.planvista.service;

import com.example.planvista.model.entity.UserActivityEntity;
import com.example.planvista.repository.UserActivityRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ユーザーアクティビティ記録サービス
 * ユーザーの各種操作を記録・管理
 */
@Service
public class UserActivityService {
    
    @Autowired
    private UserActivityRepository userActivityRepository;
    
    // アクティビティタイプの定数
    public static final String ACTIVITY_LOGIN = "ログイン";
    public static final String ACTIVITY_LOGOUT = "ログアウト";
    public static final String ACTIVITY_ADMIN_LOGIN = "管理者ログイン";
    public static final String ACTIVITY_SCHEDULE_CREATE = "スケジュール作成";
    public static final String ACTIVITY_SCHEDULE_UPDATE = "スケジュール更新";
    public static final String ACTIVITY_SCHEDULE_DELETE = "スケジュール削除";
    public static final String ACTIVITY_GOOGLE_SYNC = "Googleカレンダー同期";
    public static final String ACTIVITY_PASSWORD_RESET_REQUEST = "パスワードリセット要求";
    public static final String ACTIVITY_PASSWORD_CHANGE = "パスワード変更";
    public static final String ACTIVITY_TASK_CREATE = "タスク作成";
    public static final String ACTIVITY_RECORD_START = "作業記録開始";
    public static final String ACTIVITY_RECORD_END = "作業記録終了";
    public static final String ACTIVITY_MEMBER_ADD = "メンバー追加";
    public static final String ACTIVITY_MEMBER_REMOVE = "メンバー削除";
    public static final String ACTIVITY_AI_ANALYSIS_VIEW = "AI分析閲覧";
    public static final String ACTIVITY_PDF_EXPORT = "PDF出力";
    
    /**
     * アクティビティを記録（詳細版）
     * @param userId ユーザーID
     * @param activityType アクティビティタイプ
     * @param activityDescription 詳細説明
     * @param request HTTPリクエスト（IPアドレス、ユーザーエージェント取得用）
     */
    @Transactional
    public void logActivity(Integer userId, String activityType, String activityDescription, HttpServletRequest request) {
        UserActivityEntity activity = new UserActivityEntity();
        activity.setUserId(userId);
        activity.setActivityType(activityType);
        activity.setActivityDescription(activityDescription);
        
        if (request != null) {
            // IPアドレスを取得（プロキシ経由の場合も考慮）
            String ipAddress = getClientIpAddress(request);
            activity.setIpAddress(ipAddress);
            
            // ユーザーエージェントを取得
            String userAgent = request.getHeader("User-Agent");
            activity.setUserAgent(userAgent);
        }
        
        userActivityRepository.create(activity);
    }
    
    /**
     * アクティビティを記録（シンプル版）
     * @param userId ユーザーID
     * @param activityType アクティビティタイプ
     * @param activityDescription 詳細説明
     */
    @Transactional
    public void logActivity(Integer userId, String activityType, String activityDescription) {
        logActivity(userId, activityType, activityDescription, null);
    }
    
    /**
     * ログインアクティビティを記録
     * @param userId ユーザーID
     * @param email メールアドレス
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logLogin(Integer userId, String email, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_LOGIN, 
                "ユーザー「" + email + "」がログインしました", request);
    }
    
    /**
     * 管理者ログインアクティビティを記録
     * @param companyId 会社ID（管理者コード）
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logAdminLogin(String companyId, HttpServletRequest request) {
        // 管理者の場合はuserId=0として記録
        logActivity(0, ACTIVITY_ADMIN_LOGIN, 
                "管理者コード「" + companyId + "」でログインしました", request);
    }
    
    /**
     * ログアウトアクティビティを記録
     * @param userId ユーザーID
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logLogout(Integer userId, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_LOGOUT, 
                "ログアウトしました", request);
    }
    
    /**
     * スケジュール作成アクティビティを記録
     * @param userId ユーザーID
     * @param scheduleTitle スケジュールタイトル
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logScheduleCreate(Integer userId, String scheduleTitle, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_SCHEDULE_CREATE, 
                "スケジュール「" + scheduleTitle + "」を作成しました", request);
    }
    
    /**
     * スケジュール更新アクティビティを記録
     * @param userId ユーザーID
     * @param scheduleTitle スケジュールタイトル
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logScheduleUpdate(Integer userId, String scheduleTitle, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_SCHEDULE_UPDATE, 
                "スケジュール「" + scheduleTitle + "」を更新しました", request);
    }
    
    /**
     * スケジュール削除アクティビティを記録
     * @param userId ユーザーID
     * @param scheduleTitle スケジュールタイトル
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logScheduleDelete(Integer userId, String scheduleTitle, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_SCHEDULE_DELETE, 
                "スケジュール「" + scheduleTitle + "」を削除しました", request);
    }
    
    /**
     * Googleカレンダー同期アクティビティを記録
     * @param userId ユーザーID
     * @param syncedCount 同期件数
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logGoogleCalendarSync(Integer userId, int syncedCount, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_GOOGLE_SYNC, 
                "Googleカレンダーから" + syncedCount + "件のイベントを同期しました", request);
    }
    
    /**
     * パスワードリセット要求アクティビティを記録
     * @param email メールアドレス
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logPasswordResetRequest(String email, HttpServletRequest request) {
        // ユーザーIDが不明な場合は0を使用
        logActivity(0, ACTIVITY_PASSWORD_RESET_REQUEST, 
                "メールアドレス「" + email + "」でパスワードリセットを要求しました", request);
    }
    
    /**
     * パスワード変更アクティビティを記録
     * @param userId ユーザーID
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logPasswordChange(Integer userId, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_PASSWORD_CHANGE, 
                "パスワードを変更しました", request);
    }
    
    /**
     * タスク作成アクティビティを記録
     * @param userId ユーザーID
     * @param taskName タスク名
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logTaskCreate(Integer userId, String taskName, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_TASK_CREATE, 
                "タスク「" + taskName + "」を作成しました", request);
    }
    
    /**
     * 作業記録開始アクティビティを記録
     * @param userId ユーザーID
     * @param taskName タスク名
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logRecordStart(Integer userId, String taskName, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_RECORD_START, 
                "タスク「" + taskName + "」の作業を開始しました", request);
    }
    
    /**
     * 作業記録終了アクティビティを記録
     * @param userId ユーザーID
     * @param taskName タスク名
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logRecordEnd(Integer userId, String taskName, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_RECORD_END, 
                "タスク「" + taskName + "」の作業を終了しました", request);
    }
    
    /**
     * メンバー追加アクティビティを記録
     * @param userId ユーザーID
     * @param memberEmail メンバーのメールアドレス
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logMemberAdd(Integer userId, String memberEmail, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_MEMBER_ADD, 
                "メンバー「" + memberEmail + "」を追加しました", request);
    }
    
    /**
     * メンバー削除アクティビティを記録
     * @param userId ユーザーID
     * @param memberEmail メンバーのメールアドレス
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logMemberRemove(Integer userId, String memberEmail, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_MEMBER_REMOVE, 
                "メンバー「" + memberEmail + "」を削除しました", request);
    }
    
    /**
     * AI分析閲覧アクティビティを記録
     * @param userId ユーザーID
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logAiAnalysisView(Integer userId, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_AI_ANALYSIS_VIEW, 
                "AI分析ページを閲覧しました", request);
    }
    
    /**
     * PDF出力アクティビティを記録
     * @param userId ユーザーID
     * @param request HTTPリクエスト
     */
    @Transactional
    public void logPdfExport(Integer userId, HttpServletRequest request) {
        logActivity(userId, ACTIVITY_PDF_EXPORT, 
                "AI分析結果をPDF出力しました", request);
    }
    
    /**
     * 指定ユーザーのアクティビティ履歴を取得
     * @param userId ユーザーID
     * @return アクティビティリスト
     */
    public List<UserActivityEntity> getUserActivities(Integer userId) {
        return userActivityRepository.getByUserId(userId);
    }
    
    /**
     * 指定ユーザーのアクティビティ履歴を取得（件数制限付き）
     * @param userId ユーザーID
     * @param limit 取得件数
     * @return アクティビティリスト
     */
    public List<UserActivityEntity> getUserActivitiesWithLimit(Integer userId, int limit) {
        return userActivityRepository.getByUserIdWithLimit(userId, limit);
    }
    
    /**
     * 全アクティビティを取得
     * @return アクティビティリスト
     */
    public List<UserActivityEntity> getAllActivities() {
        return userActivityRepository.getAll();
    }
    
    /**
     * クライアントのIPアドレスを取得（プロキシ経由も考慮）
     * @param request HTTPリクエスト
     * @return IPアドレス
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 複数のIPがある場合は最初のものを使用
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
}
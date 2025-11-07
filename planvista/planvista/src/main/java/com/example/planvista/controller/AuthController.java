package com.example.planvista.controller;

import com.example.planvista.model.entity.UserEntity;
import com.example.planvista.repository.UserRepository;
import com.example.planvista.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    // 通常ログイン画面
    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }
    
    // 通常ログイン処理
    @PostMapping("/login")
    public String loginProcess(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            UserEntity user = userRepository.getByEmail(email);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "メールアドレスまたはパスワードが正しくありません");
                return "redirect:/login";
            }
            
            if (!user.getPassword().equals(password)) {
                redirectAttributes.addFlashAttribute("error", "メールアドレスまたはパスワードが正しくありません");
                return "redirect:/login";
            }
            
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("companyId", user.getCompanyId());
            session.setAttribute("isAdmin", false); // 通常ユーザー
            
            return "redirect:/main";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "ログイン処理中にエラーが発生しました");
            return "redirect:/login";
        }
    }

    // ============================================
    // 管理者ログイン機能
    // ============================================
    
    /**
     * 管理者ログイン画面
     */
    @GetMapping("/login_admin")
    public String loginAdmin(Model model) {
        return "login_admin";
    }
    
    /**
     * 管理者ログイン処理
     */
    @PostMapping("/login_admin")
    public String loginAdminProcess(
            @RequestParam("companyId") String companyId,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 所属コードの検証
            if (companyId == null || companyId.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "所属コードを入力してください");
                return "redirect:/login_admin";
            }
            
            // パスワードの検証
            if (password == null || password.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "パスワードを入力してください");
                return "redirect:/login_admin";
            }
            
            // 管理者パスワードの検証
            // TODO: 本番環境では環境変数やデータベースから取得すること
            String ADMIN_PASSWORD = "admin123";
            
            if (!password.equals(ADMIN_PASSWORD)) {
                redirectAttributes.addFlashAttribute("error", "所属コードまたはパスワードが正しくありません");
                return "redirect:/login_admin";
            }
            
            // セッションに管理者情報を保存
            session.setAttribute("adminCompanyId", companyId);
            session.setAttribute("isAdmin", true);
            session.setAttribute("adminLoginTime", System.currentTimeMillis());
            
            // 管理者専用ページにリダイレクト
            redirectAttributes.addFlashAttribute("success", "管理者としてログインしました");
            return "redirect:/all_users";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "ログイン処理中にエラーが発生しました: " + e.getMessage());
            return "redirect:/login_admin";
        }
    }

    // ============================================
    // 新規登録機能
    // ============================================

    @GetMapping("/signup")
    public String signup(Model model) {
        return "signup";
    }

    @PostMapping("/signup")
    public String signupProcess(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            @RequestParam(value = "companyId", defaultValue = "default") String companyId,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (username == null || username.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "ユーザー名を入力してください");
                return "redirect:/signup";
            }
            
            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "メールアドレスを入力してください");
                return "redirect:/signup";
            }
            
            if (password == null || password.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "パスワードを入力してください");
                return "redirect:/signup";
            }
            
            if (!password.equals(passwordConfirm)) {
                redirectAttributes.addFlashAttribute("error", "パスワードが一致しません");
                redirectAttributes.addFlashAttribute("username", username);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/signup";
            }
            
            if (userRepository.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "このメールアドレスは既に登録されています");
                redirectAttributes.addFlashAttribute("username", username);
                return "redirect:/signup";
            }
            
            if (userRepository.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "このユーザー名は既に使用されています");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/signup";
            }
            
            UserEntity newUser = new UserEntity();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setCompanyId(companyId);

            userRepository.create(newUser);

            redirectAttributes.addFlashAttribute("success", "アカウントの登録が完了しました。ログインしてください。");
            return "redirect:/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "登録処理中にエラーが発生しました: " + e.getMessage());
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/signup";
        }
    }

    // ============================================
    // ログアウト機能
    // ============================================

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        // 管理者かどうかをチェック
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        session.invalidate();
        
        redirectAttributes.addFlashAttribute("success", "ログアウトしました");
        
        // 管理者の場合は管理者ログインページへ
        if (isAdmin != null && isAdmin) {
            return "redirect:/login_admin";
        }
        
        // 通常ユーザーの場合は通常ログインページへ
        return "redirect:/login";
    }

    // ============================================
    // パスワードリセット機能
    // ============================================

    /**
     * パスワードリセット案内ページ
     */
    @GetMapping("/pwd_reset")
    public String passwordReset(Model model) {
        return "pwd_reset";
    }
    
    /**
     * パスワードリセットリクエストの処理
     */
    @PostMapping("/pwd_reset")
    public String passwordResetProcess(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "メールアドレスを入力してください");
                return "redirect:/pwd_reset";
            }
            
            // パスワードリセットリクエストを処理
            passwordResetService.requestPasswordReset(email);
            
            // セキュリティのため、ユーザーの存在に関わらず成功メッセージを表示
            redirectAttributes.addFlashAttribute("success", 
                "パスワードリセット用のメールを送信しました。メールをご確認ください。");
            return "redirect:/pwd_reset";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "処理中にエラーが発生しました");
            return "redirect:/pwd_reset";
        }
    }
    
    /**
     * パスワードリセットフォームページ
     */
    @GetMapping("/pwd_form")
    public String passwordResetForm(
            @RequestParam("token") String token,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // トークンの有効性を確認
        if (!passwordResetService.validateToken(token)) {
            redirectAttributes.addFlashAttribute("error", 
                "無効なリンクまたは期限切れです。再度パスワードリセットを申請してください。");
            return "redirect:/pwd_reset";
        }
        
        model.addAttribute("token", token);
        return "pwd_form";
    }
    
    /**
     * パスワードリセットの実行
     */
    @PostMapping("/pwd_form")
    public String passwordResetFormProcess(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            RedirectAttributes redirectAttributes) {
        
        try {
            // パスワードの検証
            if (password == null || password.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "パスワードを入力してください");
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/pwd_form?token=" + token;
            }
            
            if (!password.equals(passwordConfirm)) {
                redirectAttributes.addFlashAttribute("error", "パスワードが一致しません");
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/pwd_form?token=" + token;
            }
            
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "パスワードは6文字以上で設定してください");
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/pwd_form?token=" + token;
            }
            
            // パスワードをリセット
            boolean success = passwordResetService.resetPassword(token, password);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "パスワードの変更が完了しました。新しいパスワードでログインしてください。");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "無効なリンクまたは期限切れです。再度パスワードリセットを申請してください。");
                return "redirect:/pwd_reset";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "処理中にエラーが発生しました");
            return "redirect:/pwd_form?token=" + token;
        }
    }
}
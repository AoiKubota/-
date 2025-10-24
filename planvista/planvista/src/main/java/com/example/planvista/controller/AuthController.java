package com.example.planvista.controller;

import com.example.planvista.model.entity.UserEntity;
import com.example.planvista.repository.UserRepository;
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
    
    /**
     * ログインページを表示
     * @param model モデル
     * @return ログインページのテンプレート名
     */
    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }
    
    /**
     * ログイン処理
     * @param email メールアドレス
     * @param password パスワード
     * @param session HTTPセッション
     * @param redirectAttributes リダイレクト属性
     * @return リダイレクト先
     */
    @PostMapping("/login")
    public String loginProcess(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // メールアドレスでユーザーを検索
            UserEntity user = userRepository.getByEmail(email);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "メールアドレスまたはパスワードが正しくありません");
                return "redirect:/login";
            }
            
            // パスワードの検証（本来はハッシュ化したパスワードで比較）
            // TODO: Spring Securityを使用してパスワードのハッシュ化と検証を実装
            if (!user.getPassword().equals(password)) {
                redirectAttributes.addFlashAttribute("error", "メールアドレスまたはパスワードが正しくありません");
                return "redirect:/login";
            }
            
            // セッションにユーザー情報を保存
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("companyId", user.getCompanyId());
            
            // ログイン成功
            return "redirect:/main";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "ログイン処理中にエラーが発生しました");
            return "redirect:/login";
        }
    }
    
    /**
     * 新規登録ページを表示
     * @param model モデル
     * @return 新規登録ページのテンプレート名
     */
    @GetMapping("/signup")
    public String signup(Model model) {
        return "signup";
    }
    
    /**
     * 新規登録処理
     * @param username ユーザー名
     * @param email メールアドレス
     * @param password パスワード
     * @param passwordConfirm パスワード確認
     * @param companyId 所属ID
     * @param redirectAttributes リダイレクト属性
     * @return リダイレクト先
     */
    @PostMapping("/signup")
    public String signupProcess(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            @RequestParam(value = "companyId", defaultValue = "default") String companyId,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 入力値の検証
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
            
            // パスワード確認
            if (!password.equals(passwordConfirm)) {
                redirectAttributes.addFlashAttribute("error", "パスワードが一致しません");
                redirectAttributes.addFlashAttribute("username", username);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/signup";
            }
            
            // メールアドレスの重複チェック
            if (userRepository.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "このメールアドレスは既に登録されています");
                redirectAttributes.addFlashAttribute("username", username);
                return "redirect:/signup";
            }
            
            // ユーザー名の重複チェック
            if (userRepository.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "このユーザー名は既に使用されています");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/signup";
            }
            
            // 新規ユーザーを作成
            UserEntity newUser = new UserEntity();
            newUser.setUsername(username);
            newUser.setEmail(email);
            // TODO: パスワードをハッシュ化して保存
            newUser.setPassword(password);
            newUser.setCompanyId(companyId);
            
            // データベースに保存
            userRepository.create(newUser);
            
            // 登録成功メッセージ
            redirectAttributes.addFlashAttribute("success", "アカウントの登録が完了しました。ログインしてください。");
            return "redirect:/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "登録処理中にエラーが発生しました: " + e.getMessage());
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/signup";
        }
    }
    
    /**
     * ログアウト処理
     * @param session HTTPセッション
     * @return リダイレクト先
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "ログアウトしました");
        return "redirect:/login";
    }
    
    /**
     * パスワード再設定案内ページを表示
     * @param model モデル
     * @return パスワード再設定案内ページのテンプレート名
     */
    @GetMapping("/pwd_reset")
    public String passwordReset(Model model) {
        return "pwd_reset";
    }
}


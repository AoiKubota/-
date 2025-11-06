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
    
    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }
    
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
            
            return "redirect:/main";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "ログイン処理中にエラーが発生しました");
            return "redirect:/login";
        }
    }

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

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "ログアウトしました");
        return "redirect:/login";
    }

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
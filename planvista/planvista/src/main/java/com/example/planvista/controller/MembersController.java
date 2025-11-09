package com.example.planvista.controller;

import com.example.planvista.model.entity.UserEntity;
import com.example.planvista.repository.UserRepository;
import com.example.planvista.service.TeamMemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * メンバー管理コントローラー
 */
@Controller
@RequestMapping("/members")
public class MembersController {
    
    private final TeamMemberService teamMemberService;
    private final UserRepository userRepository;
    
    public MembersController(TeamMemberService teamMemberService,
                            UserRepository userRepository) {
        this.teamMemberService = teamMemberService;
        this.userRepository = userRepository;
    }
    
    /**
     * メンバー一覧ページ表示
     */
    @GetMapping
    public String showMembers(HttpSession session, Model model) {
        // セッションからユーザー情報を取得
        UserEntity currentUser = (UserEntity) session.getAttribute("user");
        
        // user オブジェクトがない場合は userId から取得を試みる
        if (currentUser == null) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj != null) {
                try {
                    Integer userId;
                    if (userIdObj instanceof Integer) {
                        userId = (Integer) userIdObj;
                    } else if (userIdObj instanceof Long) {
                        userId = ((Long) userIdObj).intValue();
                    } else {
                        userId = Integer.parseInt(userIdObj.toString());
                    }
                    currentUser = userRepository.getById(userId);
                    // セッションに保存
                    if (currentUser != null) {
                        session.setAttribute("user", currentUser);
                    }
                } catch (Exception e) {
                    // ログインページにリダイレクト
                    return "redirect:/login";
                }
            }
        }
        
        // ログインしていない場合は空のリストを表示
        List<UserEntity> members = List.of();
        if (currentUser != null) {
            members = teamMemberService.getMembers(currentUser.getId());
        }
        
        model.addAttribute("members", members);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isLoggedIn", currentUser != null);
        
        return "members";
    }
    
    /**
     * メンバー追加処理
     */
    @PostMapping("/add")
    public String addMember(@RequestParam String username,
                          @RequestParam String email,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        UserEntity currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "ログインが必要です");
            return "redirect:/login";
        }
        
        try {
            boolean success = teamMemberService.addMember(
                    currentUser.getId(),
                    email,
                    username
            );
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "メンバーを追加しました");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                        "指定されたメールアドレスとユーザー名に一致するユーザーが見つかりません");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "メンバーの追加に失敗しました");
        }
        
        return "redirect:/members";
    }
    
    /**
     * メンバー削除処理
     */
    @PostMapping("/remove/{userId}")
    public String removeMember(@PathVariable Integer userId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        UserEntity currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "ログインが必要です");
            return "redirect:/login";
        }
        
        try {
            teamMemberService.removeMember(currentUser.getId(), userId);
            redirectAttributes.addFlashAttribute("success", "メンバーを削除しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "メンバーの削除に失敗しました");
        }
        
        return "redirect:/members";
    }
    
    /**
     * セッションから現在のユーザーを取得するヘルパーメソッド
     */
    private UserEntity getCurrentUser(HttpSession session) {
        UserEntity currentUser = (UserEntity) session.getAttribute("user");
        
        // user オブジェクトがない場合は userId から取得を試みる
        if (currentUser == null) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj != null) {
                try {
                    Integer userId;
                    if (userIdObj instanceof Integer) {
                        userId = (Integer) userIdObj;
                    } else if (userIdObj instanceof Long) {
                        userId = ((Long) userIdObj).intValue();
                    } else {
                        userId = Integer.parseInt(userIdObj.toString());
                    }
                    currentUser = userRepository.getById(userId);
                    // セッションに保存
                    if (currentUser != null) {
                        session.setAttribute("user", currentUser);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        }
        
        return currentUser;
    }
}
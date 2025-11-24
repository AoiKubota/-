package com.example.planvista.controller;

import com.example.planvista.model.entity.UserActivityEntity;
import com.example.planvista.model.entity.UserEntity;
import com.example.planvista.repository.UserActivityRepository;
import com.example.planvista.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserActivityRepository userActivityRepository;
    

    @GetMapping("/all_users")
    public String allUsers(
            @RequestParam(value = "sort", defaultValue = "username") String sort,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpSession session,
            Model model) {
        
        try {
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                return "redirect:/login_admin?error=unauthorized";
            }
            
            String adminCode = (String) session.getAttribute("adminCompanyId");
            System.out.println("管理者コード: " + adminCode);
            

            List<UserEntity> users;
            if (keyword != null && !keyword.trim().isEmpty()) {
                System.out.println("キーワード検索: " + keyword);
                users = userRepository.findAll().stream()
                        .filter(u -> u.getUsername().toLowerCase().contains(keyword.trim().toLowerCase()) ||
                                   u.getEmail().toLowerCase().contains(keyword.trim().toLowerCase()) ||
                                   u.getCompanyId().toLowerCase().contains(keyword.trim().toLowerCase()))
                        .collect(Collectors.toList());
            } else {
                System.out.println("すべてのユーザーを取得中...");
                users = userRepository.findAll();
            }
            
            System.out.println("取得したユーザー数: " + (users != null ? users.size() : 0));

            if (users == null) {
                users = new java.util.ArrayList<>();
            }

            if (users.size() > 0) {
                System.out.println("最初のユーザー: " + users.get(0).getUsername() + " (所属ID: " + users.get(0).getCompanyId() + ")");
            }

            Map<Integer, LocalDateTime> lastLoginMap = new HashMap<>();
            for (UserEntity user : users) {
                try {
                    List<UserActivityEntity> activities = userActivityRepository.getByUserIdWithLimit(user.getId(), 100);

                    for (UserActivityEntity activity : activities) {
                        if ("ログイン".equals(activity.getActivityType())) {
                            lastLoginMap.put(user.getId(), activity.getCreatedAt());
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("ユーザーID " + user.getId() + " のアクティビティ取得エラー: " + e.getMessage());
                }
            }
            
            switch (sort) {
                case "created_at":
                    users.sort(Comparator.comparing(UserEntity::getCreatedAt).reversed());
                    break;
                case "last_login":
                    users.sort((u1, u2) -> {
                        LocalDateTime login1 = lastLoginMap.get(u1.getId());
                        LocalDateTime login2 = lastLoginMap.get(u2.getId());
                        if (login1 == null && login2 == null) return 0;
                        if (login1 == null) return 1;
                        if (login2 == null) return -1;
                        return login2.compareTo(login1);
                    });
                    break;
                case "username":
                default:
                    users.sort(Comparator.comparing(UserEntity::getUsername));
                    break;
            }
            
            model.addAttribute("users", users);
            model.addAttribute("lastLoginMap", lastLoginMap);
            model.addAttribute("currentSort", sort);
            model.addAttribute("keyword", keyword != null ? keyword : "");
            model.addAttribute("adminCode", adminCode);
            
            return "all_users";
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("エラー詳細: " + e.getMessage());
            model.addAttribute("error", "ユーザー一覧の取得中にエラーが発生しました: " + e.getMessage());
            model.addAttribute("users", new java.util.ArrayList<>());
            model.addAttribute("lastLoginMap", new HashMap<>());
            return "all_users";
        }
    }

    @GetMapping("/user_history/{userId}")
    public String userHistory(
            @PathVariable("userId") Integer userId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                return "redirect:/login_admin?error=unauthorized";
            }

            UserEntity user = userRepository.getById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "指定されたユーザーが見つかりません");
                return "redirect:/all_users";
            }

            List<UserActivityEntity> activities = userActivityRepository.getByUserId(userId);
            
            model.addAttribute("user", user);
            model.addAttribute("activities", activities);
            
            return "user_history";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "ユーザー履歴の取得中にエラーが発生しました: " + e.getMessage());
            return "redirect:/all_users";
        }
    }

    @PostMapping("/user_history/{userId}/delete")
    public String deleteUser(
            @PathVariable("userId") Integer userId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                return "redirect:/login_admin?error=unauthorized";
            }

            UserEntity user = userRepository.getById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "指定されたユーザーが見つかりません");
                return "redirect:/all_users";
            }

            userRepository.deleteById(userId);
 
            
            redirectAttributes.addFlashAttribute("success", 
                "ユーザー「" + user.getUsername() + "」を削除しました");
            return "redirect:/all_users";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "ユーザー削除中にエラーが発生しました: " + e.getMessage());
            return "redirect:/user_history/" + userId;
        }
    }
}
package com.example.planvista.controller;

import com.example.planvista.model.entity.UserEntity;
import com.example.planvista.repository.UserRepository;
import com.example.planvista.service.TeamMemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/members_calendar")
public class MembersCalendarController {
    
    private final TeamMemberService teamMemberService;
    private final UserRepository userRepository;
    
    public MembersCalendarController(TeamMemberService teamMemberService,
                                    UserRepository userRepository) {
        this.teamMemberService = teamMemberService;
        this.userRepository = userRepository;
    }
    

    @GetMapping("/{userId}")
    public String showMemberCalendar(@PathVariable Integer userId,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        UserEntity currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "ログインが必要です");
            return "redirect:/login";
        }

        UserEntity targetUser;
        try {
            targetUser = userRepository.getById(userId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "ユーザーが見つかりません");
            return "redirect:/members";
        }
        
        if (targetUser == null) {
            redirectAttributes.addFlashAttribute("error", "ユーザーが見つかりません");
            return "redirect:/members";
        }

        if (!teamMemberService.canViewMember(currentUser.getId(), userId)) {
            redirectAttributes.addFlashAttribute("error", "このユーザーのカレンダーを閲覧する権限がありません");
            return "redirect:/members";
        }
        
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("currentUser", currentUser);

        return "members_calendar";
    }
    

    private UserEntity getCurrentUser(HttpSession session) {
        UserEntity currentUser = (UserEntity) session.getAttribute("user");

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
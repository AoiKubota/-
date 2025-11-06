package com.example.planvista.controller;

import com.example.planvista.service.AiAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class AiAnalysisController {
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
    @GetMapping("/ai_analysis")
    public String aiAnalysis(Model model, HttpSession session) {

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/login";
        }

        Long userId;
        if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else {
            return "redirect:/login";
        }
        
        try {
            Map<String, Object> analysisResult = aiAnalysisService.getAnalysisForUser(userId);

            model.addAttribute("taskAverageTimes", analysisResult.get("taskAverageTimes"));
            model.addAttribute("accuracy", analysisResult.get("accuracy"));
            model.addAttribute("feedbacks", analysisResult.get("feedbacks"));
            model.addAttribute("username", session.getAttribute("username"));
            
            return "ai_analysis";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "分析データの取得中にエラーが発生しました");
            return "error";
        }
    }
}
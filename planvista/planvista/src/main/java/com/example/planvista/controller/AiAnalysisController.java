package com.example.planvista.controller;

import com.example.planvista.service.AiAnalysisService;
import com.example.planvista.service.Pdfexportservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class AiAnalysisController {
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
    @Autowired
    private Pdfexportservice pdfExportService;
    
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
    
    @GetMapping("/ai_analysis/export")
    public void exportPdf(HttpServletResponse response, HttpSession session) {

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            try {
                response.sendRedirect("/login");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        Long userId;
        if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else {
            try {
                response.sendRedirect("/login");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        try {
            Map<String, Object> analysisResult = aiAnalysisService.getAnalysisForUser(userId);
            
            @SuppressWarnings("unchecked")
            Map<String, String> taskAverageTimes = (Map<String, String>) analysisResult.get("taskAverageTimes");
            Long accuracy = (Long) analysisResult.get("accuracy");
            @SuppressWarnings("unchecked")
            List<String> feedbacks = (List<String>) analysisResult.get("feedbacks");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String filename = "ai_analysis_" + LocalDateTime.now().format(formatter) + ".pdf";

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            pdfExportService.generateAiAnalysisPdf(
                    response.getOutputStream(),
                    taskAverageTimes,
                    accuracy,
                    feedbacks
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "PDFの生成中にエラーが発生しました");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
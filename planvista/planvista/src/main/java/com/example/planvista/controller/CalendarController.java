package com.example.planvista.controller;

import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class CalendarController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @GetMapping("/calendar")
    public String calendar(Model model, HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/login";
        }

        Long userIdLong;

        if (userIdObj instanceof Integer) {
            userIdLong = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            userIdLong = (Long) userIdObj;
        } else {
            userIdLong = Long.parseLong(userIdObj.toString());
        }

        LocalDate today = LocalDate.now();

        LocalDate threeMonthsAgo = today.minusMonths(3);
        LocalDateTime startDate = threeMonthsAgo.withDayOfMonth(1).atStartOfDay();

        LocalDate threeMonthsLater = today.plusMonths(3);
        LocalDateTime endDate = threeMonthsLater.withDayOfMonth(threeMonthsLater.lengthOfMonth()).atTime(23, 59, 59);

        List<ScheduleEntity> schedules = scheduleService.getSchedulesByDateRange(userIdLong, startDate, endDate);

        List<Map<String, Object>> eventList = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (ScheduleEntity schedule : schedules) {
            Map<String, Object> scheduleMap = new HashMap<>();
            scheduleMap.put("id", schedule.getId());
            scheduleMap.put("title", schedule.getTitle());
            scheduleMap.put("description", schedule.getMemo());
            scheduleMap.put("date", schedule.getStartTime().format(dateFormatter));
            scheduleMap.put("startHour", schedule.getStartTime().getHour());
            scheduleMap.put("startMinute", schedule.getStartTime().getMinute());
            scheduleMap.put("endHour", schedule.getEndTime().getHour());
            scheduleMap.put("endMinute", schedule.getEndTime().getMinute());
            scheduleMap.put("memo", schedule.getMemo() != null ? schedule.getMemo() : "");
            scheduleMap.put("task", schedule.getTask() != null ? schedule.getTask() : "");
            scheduleMap.put("taskTime", schedule.getTaskTime() != null ? schedule.getTaskTime() : 0);

            scheduleMap.put("isSyncedFromGoogle", schedule.getIsSyncedFromGoogle());

            if (schedule.getIsSyncedFromGoogle()) {
                scheduleMap.put("type", "google");  
                scheduleMap.put("editable", false); 
                scheduleMap.put("deletable", false); 
            } else {
                scheduleMap.put("type", "schedule"); 
                scheduleMap.put("editable", true);   
                scheduleMap.put("deletable", true);  
            }
            
            eventList.add(scheduleMap);
        }

        eventList.sort((a, b) -> {
            String dateA = (String) a.get("date");
            String dateB = (String) b.get("date");
            int dateCompare = dateA.compareTo(dateB);
            if (dateCompare != 0) return dateCompare;
            
            int hourCompare = Integer.compare((Integer) a.get("startHour"), (Integer) b.get("startHour"));
            if (hourCompare != 0) return hourCompare;
            
            return Integer.compare((Integer) a.get("startMinute"), (Integer) b.get("startMinute"));
        });
        
        model.addAttribute("events", eventList);
        model.addAttribute("username", session.getAttribute("username"));
        
        return "calendar";
    }
}
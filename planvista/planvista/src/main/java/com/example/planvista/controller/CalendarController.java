package com.example.planvista.controller;

import com.example.planvista.model.entity.EventEntity;
import com.example.planvista.repository.EventRepository;
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
    private EventRepository eventRepository;
    
    @GetMapping("/calendar")
    public String calendar(Model model, HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/login";
        }

        Long userId;
        if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else {
            userId = (Long) userIdObj;
        }

        LocalDate today = LocalDate.now();

        LocalDate threeMonthsAgo = today.minusMonths(3);
        LocalDateTime startDate = threeMonthsAgo.withDayOfMonth(1).atStartOfDay();

        LocalDate threeMonthsLater = today.plusMonths(3);
        LocalDateTime endDate = threeMonthsLater.withDayOfMonth(threeMonthsLater.lengthOfMonth()).atTime(23, 59, 59);

        List<EventEntity> events = eventRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        List<Map<String, Object>> eventList = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (EventEntity event : events) {
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("title", event.getTitle());
            eventMap.put("description", event.getDescription());
            eventMap.put("date", event.getStartTime().format(dateFormatter));
            eventMap.put("startHour", event.getStartTime().getHour());
            eventMap.put("startMinute", event.getStartTime().getMinute());
            eventMap.put("endHour", event.getEndTime().getHour());
            eventMap.put("endMinute", event.getEndTime().getMinute());
            eventMap.put("memo", event.getDescription() != null ? event.getDescription() : "");
            eventMap.put("task", event.getTitle());
            eventMap.put("isSyncedFromGoogle", event.getIsSyncedFromGoogle());
            
            eventList.add(eventMap);
        }
        
        model.addAttribute("events", eventList);
        model.addAttribute("username", session.getAttribute("username"));
        
        return "calendar";
    }
}
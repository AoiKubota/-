package com.example.planvista.controller;

import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.model.entity.UserEntity;
import com.example.planvista.repository.ScheduleRepository;
import com.example.planvista.service.TeamMemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members_calendar")
public class MembersCalendarApiController {
    
    private final ScheduleRepository scheduleRepository;
    private final TeamMemberService teamMemberService;
    
    public MembersCalendarApiController(ScheduleRepository scheduleRepository,
                                       TeamMemberService teamMemberService) {
        this.scheduleRepository = scheduleRepository;
        this.teamMemberService = teamMemberService;
    }

    @GetMapping("/{userId}/schedules")
    public ResponseEntity<?> getMemberSchedules(
            @PathVariable Integer userId,
            @RequestParam String yearMonth,
            HttpSession session) {

        UserEntity currentUser = (UserEntity) session.getAttribute("user");
        if (currentUser == null) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "ログインが必要です"));
            }
        }

        if (!teamMemberService.canViewMember(currentUser.getId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "閲覧権限がありません"));
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate date = LocalDate.parse(yearMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            List<ScheduleEntity> schedules = scheduleRepository.findByUserIdAndMonth(
                    userId,
                    date.getYear(),
                    date.getMonthValue()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("userId", userId);
            response.put("yearMonth", yearMonth);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "スケジュールの取得に失敗しました: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/schedules/day")
    public ResponseEntity<?> getMemberDaySchedules(
            @PathVariable Integer userId,
            @RequestParam String date,
            HttpSession session) {

        UserEntity currentUser = (UserEntity) session.getAttribute("user");
        if (currentUser == null) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "ログインが必要です"));
            }
        }

        if (!teamMemberService.canViewMember(currentUser.getId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "閲覧権限がありません"));
        }
        
        try {
            LocalDate targetDate = LocalDate.parse(date);

            List<ScheduleEntity> schedules = scheduleRepository.findByUserIdAndDate(
                    userId,
                    targetDate
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("userId", userId);
            response.put("date", date);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "スケジュールの取得に失敗しました: " + e.getMessage()));
        }
    }
}
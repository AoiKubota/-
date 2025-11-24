package com.example.planvista.service;

import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.model.entity.RecordEntity;
import com.example.planvista.repository.ScheduleRepository;
import com.example.planvista.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAnalysisService {
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private RecordRepository recordRepository;

    public Map<String, Object> getAnalysisForUser(Long userId) {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        LocalDateTime now = LocalDateTime.now();

        List<ScheduleEntity> schedules = scheduleRepository.findByUserIdAndDateRange(userId, threeMonthsAgo, now);
        List<RecordEntity> records = recordRepository.findByUserIdAndDateRange(userId, threeMonthsAgo, now);

        Map<String, String> taskAverageTimes = calculateTaskAverageTimes(records);
        result.put("taskAverageTimes", taskAverageTimes);

        double accuracy = calculateScheduleAccuracy(schedules, records);
        result.put("accuracy", Math.round(accuracy));

        List<String> feedbacks = generateFeedbacks(schedules, records, taskAverageTimes, accuracy);
        result.put("feedbacks", feedbacks);
        
        return result;
    }

    private Map<String, String> calculateTaskAverageTimes(List<RecordEntity> records) {
        Map<String, List<Long>> taskDurations = new HashMap<>();
        
        for (RecordEntity record : records) {
            String taskName = record.getTaskName();
            long durationMinutes = record.getDurationMinutes();
            
            taskDurations.computeIfAbsent(taskName, k -> new ArrayList<>()).add(durationMinutes);
        }
        
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Long>> entry : taskDurations.entrySet()) {
            long avgMinutes = (long) entry.getValue().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            
            result.put(entry.getKey(), formatDuration(avgMinutes));
        }
        
        return result;
    }
    
    private double calculateScheduleAccuracy(List<ScheduleEntity> schedules, List<RecordEntity> records) {
        if (schedules.isEmpty() || records.isEmpty()) {
            return 0.0;
        }

        Map<Long, RecordEntity> recordMap = records.stream()
                .filter(r -> r.getScheduleId() != null)
                .collect(Collectors.toMap(RecordEntity::getScheduleId, r -> r, (r1, r2) -> r1));
        
        int totalComparisons = 0;
        double totalAccuracy = 0.0;
        
        for (ScheduleEntity schedule : schedules) {
            RecordEntity record = recordMap.get(schedule.getId());
            if (record != null) {
                long plannedMinutes = Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes();
                long actualMinutes = record.getDurationMinutes();
                
                if (plannedMinutes > 0) {
                    double difference = Math.abs(plannedMinutes - actualMinutes);
                    double accuracy = Math.max(0, 100.0 - (difference / plannedMinutes * 100.0));
                    totalAccuracy += accuracy;
                    totalComparisons++;
                }
            }
        }
        
        return totalComparisons > 0 ? totalAccuracy / totalComparisons : 0.0;
    }

    private List<String> generateFeedbacks(List<ScheduleEntity> schedules, List<RecordEntity> records, 
                                           Map<String, String> taskAverageTimes, double accuracy) {
        List<String> feedbacks = new ArrayList<>();

        if (accuracy < 60) {
            feedbacks.add("スケジュールの正確度が" + Math.round(accuracy) + "%と低めです。" +
                    "予定時間を実際より20～30%多めに見積もると、より正確なスケジュールになります。");
        } else if (accuracy >= 60 && accuracy < 80) {
            feedbacks.add("スケジュールの正確度は" + Math.round(accuracy) + "%です。" +
                    "良い精度ですが、さらに向上の余地があります。");
        } else {
            feedbacks.add("スケジュールの正確度が" + Math.round(accuracy) + "%と非常に高いです！" +
                    "この調子で計画的なスケジュール管理を続けましょう。");
        }

        Map<String, TaskDelayInfo> taskDelays = analyzeTaskDelays(schedules, records);
        for (Map.Entry<String, TaskDelayInfo> entry : taskDelays.entrySet()) {
            TaskDelayInfo info = entry.getValue();
            if (info.delayRate > 0.6 && info.totalCount >= 3) {
                long avgDelayMinutes = info.totalDelayMinutes / info.delayCount;
                feedbacks.add("「" + entry.getKey() + "」のタスクが予定より平均" + 
                        avgDelayMinutes + "分長引いています。" +
                        "次回から" + (int)(avgDelayMinutes * 1.3) + "分多めに予定を組むことをおすすめします。");
            }
        }

        if (!taskAverageTimes.isEmpty()) {
            Map.Entry<String, String> longestTask = taskAverageTimes.entrySet().stream()
                    .max((e1, e2) -> compareDuration(e1.getValue(), e2.getValue()))
                    .orElse(null);
            
            if (longestTask != null) {
                feedbacks.add("「" + longestTask.getKey() + "」が平均" + longestTask.getValue() + 
                        "と最も時間がかかっています。このタスクを細分化することで、" +
                        "より効率的に進められる可能性があります。");
            }
        }

        if (records.size() < 5) {
            feedbacks.add("記録されたデータがまだ少ないため、詳細な分析ができません。" +
                    "より正確な分析のために、日々の作業記録を続けることをおすすめします。");
        }

        if (feedbacks.isEmpty()) {
            feedbacks.add("現在のところ、スケジュール管理は順調です。" +
                    "この調子で記録を続けて、より詳細な分析を行いましょう。");
        }
        
        return feedbacks;
    }

    private Map<String, TaskDelayInfo> analyzeTaskDelays(List<ScheduleEntity> schedules, List<RecordEntity> records) {
        Map<String, TaskDelayInfo> taskDelays = new HashMap<>();

        Map<Long, RecordEntity> recordMap = records.stream()
                .filter(r -> r.getScheduleId() != null)
                .collect(Collectors.toMap(RecordEntity::getScheduleId, r -> r, (r1, r2) -> r1));
        
        for (ScheduleEntity schedule : schedules) {
            RecordEntity record = recordMap.get(schedule.getId());
            if (record != null) {
                long plannedMinutes = Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes();
                long actualMinutes = record.getDurationMinutes();
                long delayMinutes = actualMinutes - plannedMinutes;
                
                String taskName = record.getTaskName();
                TaskDelayInfo info = taskDelays.computeIfAbsent(taskName, k -> new TaskDelayInfo());
                
                info.totalCount++;
                if (delayMinutes > 0) {
                    info.delayCount++;
                    info.totalDelayMinutes += delayMinutes;
                }
                info.delayRate = (double) info.delayCount / info.totalCount;
            }
        }
        
        return taskDelays;
    }

    private String formatDuration(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
    }

    private int compareDuration(String d1, String d2) {
        long m1 = parseDurationToMinutes(d1);
        long m2 = parseDurationToMinutes(d2);
        return Long.compare(m1, m2);
    }

    private long parseDurationToMinutes(String duration) {
        String[] parts = duration.split(":");
        return Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
    }

    private static class TaskDelayInfo {
        int totalCount = 0;
        int delayCount = 0;
        long totalDelayMinutes = 0;
        double delayRate = 0.0;
    }
}
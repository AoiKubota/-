package com.example.planvista.controller;

import com.example.planvista.model.entity.RecordEntity;
import com.example.planvista.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/record")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @GetMapping("/{recordId}")
    @ResponseBody
    public Map<String, Object> getRecord(@PathVariable Long recordId) {
        Map<String, Object> response = new HashMap<>();

        try {
            RecordEntity record = recordService.getRecordById(recordId);

            response.put("success", true);
            response.put("record", record);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "レコードの取得に失敗しました");
        }

        return response;
    }
}
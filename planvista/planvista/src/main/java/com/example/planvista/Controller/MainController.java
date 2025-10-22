package com.example.planvista.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    
    /**
     * メインページを表示
     * @param model モデル
     * @return メインページのテンプレート名
     */
    @GetMapping("/main")
    public String main(Model model){
        // 必要に応じてモデルにデータを追加
        // 例: model.addAttribute("userName", "ユーザー名");
        
        return "main";  // src/main/resources/templates/main.html を表示
    }

}
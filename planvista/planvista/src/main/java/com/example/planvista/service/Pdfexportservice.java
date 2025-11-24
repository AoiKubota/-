package com.example.planvista.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class Pdfexportservice {

    private final TemplateEngine templateEngine;

    public Pdfexportservice(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }


    public void generateAiAnalysisPdf(
            OutputStream outputStream,
            Map<String, String> taskAverageTimes,
            Long accuracy,
            List<String> feedbacks) throws Exception {
        
        // Thymeleafコンテキストを作成
        Context context = new Context();
        
        // データを設定
        context.setVariable("taskAverageTimes", taskAverageTimes);
        context.setVariable("accuracy", accuracy);
        context.setVariable("feedbacks", feedbacks);
        
        // エクスポート日時を追加
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        String exportDate = LocalDateTime.now().format(formatter);
        context.setVariable("exportDate", exportDate);

        // HTMLを生成
        String htmlContent = templateEngine.process("ai_analysis_pdf", context);

        // PDFに変換
        ITextRenderer renderer = new ITextRenderer();
        
        // 日本語フォントを設定
        ITextFontResolver fontResolver = renderer.getFontResolver();
        
        // 複数のフォントパスを試す
        boolean fontLoaded = false;
        
        // 1. Windowsのフォント
        String[] windowsFonts = {
            "C:/Windows/Fonts/msgothic.ttc",
            "C:/Windows/Fonts/msmincho.ttc",
            "C:/Windows/Fonts/meiryo.ttc"
        };
        
        for (String fontPath : windowsFonts) {
            try {
                fontResolver.addFont(fontPath, "Identity-H", true);
                System.out.println("フォント読み込み成功: " + fontPath);
                fontLoaded = true;
                break;
            } catch (Exception e) {
                // 次のフォントを試す
            }
        }
        
        // 2. Macのフォント
        if (!fontLoaded) {
            String[] macFonts = {
                "/System/Library/Fonts/ヒラギノ角ゴシック W3.ttc",
                "/Library/Fonts/Osaka.ttf",
                "/System/Library/Fonts/Hiragino Sans GB.ttc"
            };
            
            for (String fontPath : macFonts) {
                try {
                    fontResolver.addFont(fontPath, "Identity-H", true);
                    System.out.println("フォント読み込み成功: " + fontPath);
                    fontLoaded = true;
                    break;
                } catch (Exception e) {
                    // 次のフォントを試す
                }
            }
        }
        
        // 3. Linuxのフォント
        if (!fontLoaded) {
            String[] linuxFonts = {
                "/usr/share/fonts/truetype/fonts-japanese-gothic.ttf",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/takao-gothic/TakaoPGothic.ttf"
            };
            
            for (String fontPath : linuxFonts) {
                try {
                    fontResolver.addFont(fontPath, "Identity-H", true);
                    System.out.println("フォント読み込み成功: " + fontPath);
                    fontLoaded = true;
                    break;
                } catch (Exception e) {
                    // 次のフォントを試す
                }
            }
        }
        
        if (!fontLoaded) {
            System.err.println("警告: 日本語フォントが見つかりませんでした。文字化けする可能性があります。");
        }
        
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
    }
}
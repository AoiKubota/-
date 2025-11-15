package com.example.planvista.service;

import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
    /**
     * AI分析結果をPDFとして生成
     * 
     * @param userId ユーザーID
     * @return PDFのバイト配列
     * @throws IOException
     * @throws DocumentException
     */
    public byte[] generateAnalysisPdf(Long userId) throws IOException, DocumentException {
        // AI分析データを取得
        Map<String, Object> analysisResult = aiAnalysisService.getAnalysisForUser(userId);
        
        // Thymeleafコンテキストを作成
        Context context = new Context();
        context.setVariable("taskAverageTimes", analysisResult.get("taskAverageTimes"));
        context.setVariable("accuracy", analysisResult.get("accuracy"));
        context.setVariable("feedbacks", analysisResult.get("feedbacks"));
        
        // 生成日時を追加
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        context.setVariable("generatedDate", now.format(formatter));
        
        // HTMLをレンダリング
        String htmlContent = templateEngine.process("ai_analysis_pdf", context);
        
        // PDFに変換
        return convertHtmlToPdf(htmlContent);
    }
    
    /**
     * HTMLをPDFに変換
     * 
     * @param htmlContent HTML文字列
     * @return PDFのバイト配列
     * @throws DocumentException
     */
    private byte[] convertHtmlToPdf(String htmlContent) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            ITextRenderer renderer = new ITextRenderer();
            
            // 日本語フォントの設定（システムにインストールされているフォントを使用）
            // Linuxの場合は /usr/share/fonts/ 配下のフォントを使用
            // Windowsの場合は C:/Windows/Fonts/ 配下のフォントを使用
            try {
                // IPAフォント（日本語対応）を設定
                renderer.getFontResolver().addFont(
                    "/usr/share/fonts/truetype/fonts-japanese-gothic.ttf",
                    "Identity-H",
                    true
                );
            } catch (Exception e) {
                // フォントが見つからない場合はデフォルトフォントを使用
                System.err.println("日本語フォントが見つかりません。デフォルトフォントを使用します。");
            }
            
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            return outputStream.toByteArray();
            
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
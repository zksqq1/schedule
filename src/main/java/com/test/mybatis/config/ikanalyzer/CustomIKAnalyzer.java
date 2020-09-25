package com.test.mybatis.config.ikanalyzer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class CustomIKAnalyzer {
    @NonNull
    private IKAnalyzerConfiguration.IkAnalyzerConfig ikAnalyzerConfig;

    public List<String> divide(String msg) throws IOException {
        StringReader sr = new StringReader(msg);
        IKSegmenter ik = new IKSegmenter(sr, ikAnalyzerConfig);
        Lexeme lex;
        List<String> list = new ArrayList<>();
        while ((lex = ik.next()) != null) {
            String text = lex.getLexemeText();
            if (text.length() > 1) {
                list.add(text);
            }
        }
        return list;
    }
}

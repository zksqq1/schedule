package com.test.mybatis.config.ikanalyzer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class CustomIKAnalyzer {
    @NonNull
    private IKAnalyzerConfiguration.IkAnalyzerConfig ikAnalyzerConfig;

    public LinkedHashSet<String> divide(String msg) throws IOException {
        StringReader sr = new StringReader(msg);
        IKSegmenter ik = new IKSegmenter(sr, ikAnalyzerConfig);
        Lexeme lex;
        LinkedHashSet<String> list = new LinkedHashSet<>();
        while ((lex = ik.next()) != null && !lex.getLexemeText().matches("^.*\\d+.*$")) {
            String text = lex.getLexemeText();
            if (text.length() > 1) {
                list.add(text);
            }
        }
        return list;
    }
}

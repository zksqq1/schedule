package com.test.mybatis.config.ikanalyzer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;

import java.util.List;

/**
 *
 */
@Configuration
public class IKAnalyzerConfiguration {
    @Bean
    public IkAnalyzerConfig ikAnalyzerConfig() {
        IkAnalyzerConfig config = new IkAnalyzerConfig();
        config.setUseSmart(true);
        return config;
    }


    static class IkAnalyzerConfig implements org.wltea.analyzer.cfg.Configuration {
        private org.wltea.analyzer.cfg.Configuration config = DefaultConfig.getInstance();

        @Override
        public boolean useSmart() {
            return config.useSmart();
        }

        @Override
        public void setUseSmart(boolean useSmart) {
            config.setUseSmart(useSmart);
        }

        @Override
        public String getMainDictionary() {
            return "province.dic";
        }

        @Override
        public String getQuantifierDicionary() {
            return "stop.dic";
        }

        @Override
        public List<String> getExtDictionarys() {
            return config.getExtDictionarys();
        }

        @Override
        public List<String> getExtStopWordDictionarys() {
            return config.getExtStopWordDictionarys();
        }
    }
}

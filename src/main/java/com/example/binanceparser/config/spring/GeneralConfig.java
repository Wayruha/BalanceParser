package com.example.binanceparser.config.spring;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.Constants;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.StatsVisualizerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.IOException;

@Configuration
//@ComponentScan
public class GeneralConfig {
    @Bean
    public AppProperties futuresProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.FUTURES_PROPS_PATH);
    }

    @Bean
    public AppProperties spotProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.SPOT_PROPS_PATH);
    }

    @Bean
    public AppProperties statsProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.STATS_PROPS_PATH);
    }

    @Bean(name = BeanNames.FUTURES_CONFIG)
    @DependsOn({"futuresProps"})
    public BalanceVisualizerConfig futuresConfig(@Autowired AppProperties futuresBalanceProperties) {
        return ConfigUtil.loadVisualizerConfig(futuresBalanceProperties);
    }

    @Bean(name = BeanNames.SPOT_CONFIG)
    @DependsOn({"spotProps"})
    public BalanceVisualizerConfig spotConfig(@Autowired AppProperties spotBalanceProperties) {
        return ConfigUtil.loadVisualizerConfig(spotBalanceProperties);
    }

    @Bean(name = BeanNames.STATS_CONFIG)
    @DependsOn({"statsProps"})
    public StatsVisualizerConfig statsConfig(@Autowired AppProperties statsBalanceProperties) {
        return ConfigUtil.loadStatsConfig(statsBalanceProperties);
    }
}

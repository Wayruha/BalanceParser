package com.example.binanceparser;

import com.example.binanceparser.config.spring.BeanNames;
import com.example.binanceparser.config.spring.GeneralConfig;
import com.example.binanceparser.run.service.FuturesRunner;
import com.example.binanceparser.run.service.IncomeRunner;
import com.example.binanceparser.run.service.SpotRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Collections;

//TODO delete
@ComponentScan("com.example.binanceparser")
public class TestSpringEntryPoint {
    private static AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GeneralConfig.class);

    public static void main(String[] args) {

        System.out.println("start");
        var x = context.getBean(BeanNames.FUTURES_PROPS);
        FuturesRunner fr = (FuturesRunner) context.getBean(BeanNames.FUTURES_RUNNER);
        SpotRunner sr = (SpotRunner) context.getBean(BeanNames.SPOT_RUNNER);
        IncomeRunner ir = (IncomeRunner) context.getBean(BeanNames.INCOME_RUNNER);
        System.out.println("rep:" + fr.getReport(Collections.emptyList()));
    }
}
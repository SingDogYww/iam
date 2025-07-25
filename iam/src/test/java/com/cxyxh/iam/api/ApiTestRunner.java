/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cxyxh.iam.api;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * API测试运行器，用于自动化执行所有API测试
 */
public class ApiTestRunner {
    
    public static void main(String[] args) {
        System.out.println("==== API测试开始执行 ====");
        System.out.println("时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("=======================");
        
        // 创建测试发现请求
        LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request();
        
        // 如果指定了特定测试类，则只运行该类的测试
        if (args.length > 0) {
            String testClassName = args[0];
            requestBuilder.selectors(DiscoverySelectors.selectClass("com.cxyxh.iam.api." + testClassName));
            System.out.println("运行指定测试类: " + testClassName);
        } else {
            // 否则运行所有API测试
            requestBuilder.selectors(DiscoverySelectors.selectPackage("com.cxyxh.iam.api"));
            System.out.println("运行所有API测试");
        }
        
        LauncherDiscoveryRequest request = requestBuilder.build();
        
        // 创建测试执行监听器
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        // 创建并配置测试执行器
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        
        try {
            // 执行测试
            launcher.execute(request);
            
            // 打印测试结果摘要
            TestExecutionSummary summary = listener.getSummary();
            summary.printTo(new PrintWriter(System.out));
            
            System.out.println("==== API测试执行完成 ====");
            System.out.println("总测试数: " + summary.getTestsFoundCount());
            System.out.println("成功: " + summary.getTestsSucceededCount());
            System.out.println("失败: " + summary.getTestsFailedCount());
            System.out.println("跳过: " + summary.getTestsSkippedCount());
            System.out.println("==========================");
            
            // 如果有失败的测试，退出码为1
            if (summary.getTestsFailedCount() > 0) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("测试执行过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
} 
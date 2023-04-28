/*-
 * =====LICENSE-START=====
 * flink-spring
 * ------
 * Copyright (C) 2020 - 2022 Organization Name
 * ------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * =====LICENSE-END=====
 */
package com.getindata.fink.spring.context;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ContextRegistry {

    private transient final ConcurrentHashMap<String, ApplicationContext> contextRegistry;

    public ContextRegistry() {
        contextRegistry = new ConcurrentHashMap<>();
    }

    public ApplicationContext getContext(String configurationPackageName) {
        return contextRegistry.computeIfAbsent(configurationPackageName,
            AnnotationConfigApplicationContext::new);
    }

    public <T> T autowiredBean(T bean, String configurationPackageName) {
        ApplicationContext context = getContext(configurationPackageName);
        AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
        factory.autowireBean(bean);
        return bean;
    }

    // For Testing
    void addContext(String configPackage, ApplicationContext context) {
        this.contextRegistry.put(configPackage, context);
    }
}

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

import com.getindata.fink.spring.context.objects.Family;
import com.getindata.fink.spring.context.objects.House;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ContextRegistrySpringTest {

    @Test
    void shouldAutowiredBeans() {
        House house = new House();
        assertThat(house.getElectricalInstallation()).isNull();
        assertThat(house.getWaterInstallation()).isNull();
        assertThat(house.getFamily()).isNull();

        new ContextRegistry().autowiredBean(house,
            "com.getindata.fink.spring.context.objects.configuration");

        assertThat(house.getElectricalInstallation()).isNotNull();
        assertThat(house.getWaterInstallation()).isNotNull();
        assertThat(house.getFamily()).isNotNull();
    }

    @Test
    void shouldGetBean() {
        Family myFamily = new ContextRegistry()
            .getContext("com.getindata.fink.spring.context.objects.configuration")
            .getBean("myFamily", Family.class);

        assertThat(myFamily).isNotNull();
    }
}

/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
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
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.plugin.spring.mvc.commons;

/**
 * Interceptor class name constant variables
 *
 * 拦截器类的枚举值
 *
 * @author zhangxin
 */
public class Constants {
    public static final String GET_BEAN_INTERCEPTOR = "org.skywalking.apm.plugin.spring.mvc.commons.interceptor.GetBeanInterceptor";

    public static final String INVOKE_FOR_REQUEST_INTERCEPTOR = "org.skywalking.apm.plugin.spring.mvc.commons.interceptor.InvokeForRequestInterceptor";

    public static final String REQUEST_MAPPING_METHOD_INTERCEPTOR = "org.skywalking.apm.plugin.spring.mvc.commons.interceptor.RequestMappingMethodInterceptor";

    public static final String REST_MAPPING_METHOD_INTERCEPTOR = "org.skywalking.apm.plugin.spring.mvc.commons.interceptor.RestMappingMethodInterceptor";
}

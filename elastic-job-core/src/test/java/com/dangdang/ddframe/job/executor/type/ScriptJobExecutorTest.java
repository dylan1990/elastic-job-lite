/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.job.executor.type;

import com.dangdang.ddframe.job.executor.JobFacade;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.fixture.ElasticJobVerify;
import com.dangdang.ddframe.job.fixture.ShardingContextsBuilder;
import com.dangdang.ddframe.job.fixture.config.TestScriptJobConfiguration;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ScriptJobExecutorTest {
    
    @Mock
    private JobFacade jobFacade;
    
    @Mock
    private Executor executor;
    
    private ScriptJobExecutor scriptJobExecutor;
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenExecuteFailure() throws IOException, NoSuchFieldException {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, ShardingContextsBuilder.getMultipleShardingContexts());
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("not_exists_file"));
        scriptJobExecutor = new ScriptJobExecutor(jobFacade);
        ReflectionUtils.setFieldValue(scriptJobExecutor, "executor", executor);
        when(executor.execute(Matchers.<CommandLine>any())).thenThrow(IOException.class);
        try {
            scriptJobExecutor.execute();
        } finally {
            verify(executor, times(2)).execute(Matchers.<CommandLine>any());
        }
    }
    
    @Test
    public void assertExecuteSuccessForMultipleShardingItems() throws IOException, NoSuchFieldException {
        assertExecuteSuccess(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    @Test
    public void assertExecuteSuccessForSingleShardingItems() throws IOException, NoSuchFieldException {
        assertExecuteSuccess(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    private void assertExecuteSuccess(final ShardingContexts shardingContexts) throws IOException, NoSuchFieldException {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("exists_file param0 param1"));
        scriptJobExecutor = new ScriptJobExecutor(jobFacade);
        ReflectionUtils.setFieldValue(scriptJobExecutor, "executor", executor);
        scriptJobExecutor.execute();
        verify(jobFacade).loadJobRootConfiguration(true);
        verify(executor, times(shardingContexts.getShardingTotalCount())).execute(Matchers.<CommandLine>any());
    }
}

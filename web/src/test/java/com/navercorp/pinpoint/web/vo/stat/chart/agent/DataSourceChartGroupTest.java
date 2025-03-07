/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.DataSourceSampler;
import com.navercorp.pinpoint.web.test.util.DataSourceTestUtils;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class DataSourceChartGroupTest {

    private static final int MIN_VALUE_OF_MAX_CONNECTION_SIZE = 20;
    private static final int CREATE_TEST_OBJECT_MAX_SIZE = 10;

    private final DataSourceSampler sampler = new DataSourceSampler();

    @Mock
    private ServiceTypeRegistryService serviceTypeRegistryService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(serviceTypeRegistryService.findServiceType(any(Short.class))).thenReturn(ServiceType.UNKNOWN);
    }

    @Test
    public void basicFunctionTest1() {
        long currentTimeMillis = System.currentTimeMillis();
        TimeWindow timeWindow = new TimeWindow(Range.between(currentTimeMillis - 300000, currentTimeMillis));

        List<SampledDataSource> sampledDataSourceList = createSampledDataSourceList(timeWindow);
        StatChartGroup<AgentStatPoint<Integer>> dataSourceChartGroup = DataSourceChart.newDataSourceChartGroup(timeWindow, sampledDataSourceList, serviceTypeRegistryService);

        assertEquals(sampledDataSourceList, dataSourceChartGroup);
    }

    @Test
    public void basicFunctionTest2() {
        long currentTimeMillis = System.currentTimeMillis();
        TimeWindow timeWindow = new TimeWindow(Range.between(currentTimeMillis - 300000, currentTimeMillis));

        List<SampledDataSource> sampledDataSourceList = List.of();
        DataSourceChart dataSourceChartGroup = new DataSourceChart(timeWindow, sampledDataSourceList, serviceTypeRegistryService);

        Assertions.assertEquals(-1, dataSourceChartGroup.getId());
        Assertions.assertNull(dataSourceChartGroup.getJdbcUrl());
        Assertions.assertNull(dataSourceChartGroup.getDatabaseName());
        Assertions.assertNull(dataSourceChartGroup.getServiceType());

        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Integer>>> charts = dataSourceChartGroup.getCharts().getCharts();
        Assertions.assertEquals(2, charts.size());

        for (Chart<? extends Point> chart : charts.values()) {
            Assertions.assertTrue(CollectionUtils.isEmpty(chart.getPoints()));
        }
    }

    private List<SampledDataSource> createSampledDataSourceList(TimeWindow timeWindow) {
        List<SampledDataSource> sampledDataSourceList = new ArrayList<>();

        int maxConnectionSize = ThreadLocalRandom.current().nextInt(MIN_VALUE_OF_MAX_CONNECTION_SIZE) + MIN_VALUE_OF_MAX_CONNECTION_SIZE;

        long from = timeWindow.getWindowRange().getFrom();
        long to = timeWindow.getWindowRange().getTo();

        for (long i = from; i < to; i += timeWindow.getWindowSlotSize()) {
            sampledDataSourceList.add(createSampledDataSource(i, maxConnectionSize));
        }

        return sampledDataSourceList;
    }

    private SampledDataSource createSampledDataSource(long timestamp, int maxConnectionSize) {
        int testObjectSize = ThreadLocalRandom.current().nextInt(CREATE_TEST_OBJECT_MAX_SIZE) + 1;
        List<DataSourceBo> dataSourceBoList = DataSourceTestUtils.createDataSourceBoList(1, testObjectSize, maxConnectionSize);
        return sampler.sampleDataPoints(0, timestamp, dataSourceBoList, null);
    }

    private void assertEquals(List<SampledDataSource> sampledDataSourceList, StatChartGroup<AgentStatPoint<Integer>> dataSourceChartGroup) {
        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Integer>>> charts = dataSourceChartGroup.getCharts();

        Chart<? extends Point> activeConnectionSizeChart = charts.get(DataSourceChart.DataSourceChartType.ACTIVE_CONNECTION_SIZE);
        List<? extends Point> activeConnectionSizeChartPointList = activeConnectionSizeChart.getPoints();

        for (int i = 0; i < sampledDataSourceList.size(); i++) {
            SampledDataSource sampledDataSource = sampledDataSourceList.get(i);
            Point point = sampledDataSource.getActiveConnectionSize();

            Assertions.assertEquals(activeConnectionSizeChartPointList.get(i), point);
        }

        Chart<AgentStatPoint<Integer>> maxConnectionSizeChart = charts.get(DataSourceChart.DataSourceChartType.MAX_CONNECTION_SIZE);
        List<AgentStatPoint<Integer>> maxConnectionSizeChartPointList = maxConnectionSizeChart.getPoints();
        for (int i = 0; i < sampledDataSourceList.size(); i++) {
            SampledDataSource sampledDataSource = sampledDataSourceList.get(i);
            AgentStatPoint<Integer> point = sampledDataSource.getMaxConnectionSize();

            Assertions.assertEquals(maxConnectionSizeChartPointList.get(i), point);
        }
    }

}

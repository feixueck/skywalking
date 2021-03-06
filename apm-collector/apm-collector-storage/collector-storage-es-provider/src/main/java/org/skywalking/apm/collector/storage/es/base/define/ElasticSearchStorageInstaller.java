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

package org.skywalking.apm.collector.storage.es.base.define;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.IndexNotFoundException;
import org.skywalking.apm.collector.client.Client;
import org.skywalking.apm.collector.client.elasticsearch.ElasticSearchClient;
import org.skywalking.apm.collector.core.data.ColumnDefine;
import org.skywalking.apm.collector.core.data.TableDefine;
import org.skywalking.apm.collector.storage.StorageInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * 基于 ES 存储安装器实现类。基于 TableDefine ，初始化存储组件的表
 *
 * @author peng-yongsheng
 */
public class ElasticSearchStorageInstaller extends StorageInstaller {

    private final Logger logger = LoggerFactory.getLogger(ElasticSearchStorageInstaller.class);

    private final int indexShardsNumber;
    private final int indexReplicasNumber;

    public ElasticSearchStorageInstaller(int indexShardsNumber, int indexReplicasNumber) {
        this.indexShardsNumber = indexShardsNumber;
        this.indexReplicasNumber = indexReplicasNumber;
    }

    @Override protected void defineFilter(List<TableDefine> tableDefines) {
        // 过滤非 ElasticSearchTableDefine
        int size = tableDefines.size();
        for (int i = size - 1; i >= 0; i--) {
            if (!(tableDefines.get(i) instanceof ElasticSearchTableDefine)) {
                tableDefines.remove(i);
            }
        }
    }

    @Override protected boolean createTable(Client client, TableDefine tableDefine) {
        ElasticSearchClient esClient = (ElasticSearchClient) client;
        ElasticSearchTableDefine esTableDefine = (ElasticSearchTableDefine) tableDefine;
        // mapping
        XContentBuilder mappingBuilder = null;

        // Settings
        Settings settings = createSettingBuilder(esTableDefine);
        try {
            // XContentBuilder
            mappingBuilder = createMappingBuilder(esTableDefine);
            logger.info("mapping builder str: {}", mappingBuilder.string());
        } catch (Exception e) {
            logger.error("create {} index mapping builder error", esTableDefine.getName());
        }

        // 调用
        boolean isAcknowledged = esClient.createIndex(esTableDefine.getName(), esTableDefine.type(), settings, mappingBuilder);
        logger.info("create {} index with type of {} finished, isAcknowledged: {}", esTableDefine.getName(), esTableDefine.type(), isAcknowledged);
        return isAcknowledged;
    }

    private Settings createSettingBuilder(ElasticSearchTableDefine tableDefine) {
        return Settings.builder()
            // 参见 https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html
            .put("index.number_of_shards", indexShardsNumber)
            .put("index.number_of_replicas", indexReplicasNumber)

            // 参见 https://www.elastic.co/guide/cn/elasticsearch/guide/current/near-real-time.html#refresh-api
            .put("index.refresh_interval", String.valueOf(tableDefine.refreshInterval()) + "s") // 索引刷新频率

             // 参见 https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-standard-analyzer.html
            .put("analysis.analyzer.collector_analyzer.tokenizer", "collector_tokenizer")
            .put("analysis.tokenizer.collector_tokenizer.type", "standard")
            .put("analysis.tokenizer.collector_tokenizer.max_token_length", 5)
            .build();
    }

    private XContentBuilder createMappingBuilder(ElasticSearchTableDefine tableDefine) throws IOException {
        XContentBuilder mappingBuilder = XContentFactory.jsonBuilder()
            .startObject()
            .startObject("properties");

        // 循环 ColumnDefine 数组
        for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
            ElasticSearchColumnDefine elasticSearchColumnDefine = (ElasticSearchColumnDefine)columnDefine;

            if (ElasticSearchColumnDefine.Type.Text.name().toLowerCase().equals(elasticSearchColumnDefine.getType().toLowerCase())) {
                mappingBuilder
                    .startObject(elasticSearchColumnDefine.getName())
                    .field("type", elasticSearchColumnDefine.getType().toLowerCase())
                    .field("fielddata", true) // 目前使用的字段都是 SERVICE_NAME 。参见 http://cwiki.apachecn.org/pages/viewpage.action?pageId=10028596
                    .endObject();
            } else {
                mappingBuilder
                    .startObject(elasticSearchColumnDefine.getName())
                    .field("type", elasticSearchColumnDefine.getType().toLowerCase())
                    .endObject();
            }
        }

        mappingBuilder
            .endObject()
            .endObject();
        logger.debug("create elasticsearch index: {}", mappingBuilder.string());
        return mappingBuilder;
    }

    @Override protected boolean deleteTable(Client client, TableDefine tableDefine) {
        ElasticSearchClient esClient = (ElasticSearchClient)client;
        try {
            return esClient.deleteIndex(tableDefine.getName());
        } catch (IndexNotFoundException e) {
            logger.info("{} index not found", tableDefine.getName());
        }
        return false;
    }

    @Override protected boolean isExists(Client client, TableDefine tableDefine) {
        ElasticSearchClient esClient = (ElasticSearchClient)client;
        return esClient.isExistsIndex(tableDefine.getName());
    }
}

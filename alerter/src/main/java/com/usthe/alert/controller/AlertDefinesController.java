/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.usthe.alert.controller;

import com.usthe.common.entity.alerter.AlertDefine;
import com.usthe.alert.service.AlertDefineService;
import com.usthe.common.entity.dto.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * 告警定义批量API
 * @author tom
 * @date 2021/12/9 10:32
 */
@Tag(name = "Alert Define Batch API | 告警定义管理API")
@RestController
@RequestMapping(path = "/api/alert/defines", produces = {APPLICATION_JSON_VALUE})
public class AlertDefinesController {

    @Autowired
    private AlertDefineService alertDefineService;

    @GetMapping
    @Operation(summary = "查询告警定义列表", description = "根据查询过滤项获取告警定义信息列表")
    public ResponseEntity<Message<Page<AlertDefine>>> getAlertDefines(
            @Parameter(description = "告警定义ID", example = "6565463543") @RequestParam(required = false) List<Long> ids,
            @Parameter(description = "告警定义级别", example = "6565463543") @RequestParam(required = false) Byte priority,
            @Parameter(description = "排序字段，默认id", example = "id") @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "排序方式，asc:升序，desc:降序", example = "desc") @RequestParam(defaultValue = "desc") String order,
            @Parameter(description = "列表当前分页", example = "0") @RequestParam(defaultValue = "0") int pageIndex,
            @Parameter(description = "列表分页数量", example = "8") @RequestParam(defaultValue = "8") int pageSize) {

        Specification<AlertDefine> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> andList = new ArrayList<>();
            if (ids != null && !ids.isEmpty()) {
                CriteriaBuilder.In<Long> inPredicate= criteriaBuilder.in(root.get("id"));
                for (long id : ids) {
                    inPredicate.value(id);
                }
                andList.add(inPredicate);
            }
            if (priority != null) {
                Predicate predicate = criteriaBuilder.equal(root.get("priority"), priority);
                andList.add(predicate);
            }
            Predicate[] predicates = new Predicate[andList.size()];
            return criteriaBuilder.and(andList.toArray(predicates));
        };
        // 分页是必须的
        Sort sortExp = Sort.by(new Sort.Order(Sort.Direction.fromString(order), sort));
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, sortExp);
        Page<AlertDefine> alertDefinePage = alertDefineService.getAlertDefines(specification,pageRequest);
        Message<Page<AlertDefine>> message = new Message<>(alertDefinePage);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping
    @Operation(summary = "批量删除告警定义", description = "根据告警定义ID列表批量删除告警定义")
    public ResponseEntity<Message<Void>> deleteAlertDefines(
            @Parameter(description = "告警定义IDs", example = "6565463543") @RequestParam(required = false) List<Long> ids
    ) {
        if (ids != null && !ids.isEmpty()) {
            alertDefineService.deleteAlertDefines(new HashSet<>(ids));
        }
        Message<Void> message = new Message<>();
        return ResponseEntity.ok(message);
    }

}

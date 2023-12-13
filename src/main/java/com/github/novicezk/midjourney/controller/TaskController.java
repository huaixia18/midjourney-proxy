package com.github.novicezk.midjourney.controller;

import cn.hutool.core.comparator.CompareUtil;
import com.github.novicezk.midjourney.baidu.CheckContent;
import com.github.novicezk.midjourney.baidu.ImageCheckReturn;
import com.github.novicezk.midjourney.dto.TaskConditionDTO;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.loadbalancer.DiscordLoadBalancer;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Api(tags = "任务查询")
@Slf4j
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
	private final TaskStoreService taskStoreService;
	private final DiscordLoadBalancer discordLoadBalancer;
	private final CheckContent checkContent;

	@ApiOperation(value = "指定ID获取任务")
	@GetMapping("/{id}/fetch")
	public Task fetch(@ApiParam(value = "任务ID") @PathVariable String id) {
		log.info("调用指定ID获取任务");
        return this.taskStoreService.get(id);
	}

	@ApiOperation(value = "查询任务队列")
	@GetMapping("/queue")
	public List<Task> queue() {
		log.info("调用查询任务队列");
        return this.discordLoadBalancer.getQueueTaskIds().stream()
				.map(this.taskStoreService::get).filter(Objects::nonNull)
				.sorted(Comparator.comparing(Task::getSubmitTime))
				.toList();
	}

	@ApiOperation(value = "查询所有任务")
	@GetMapping("/list")
	public List<Task> list() {
		log.info("调用查询所有任务");
        return this.taskStoreService.list().stream()
				.sorted((t1, t2) -> CompareUtil.compare(t2.getSubmitTime(), t1.getSubmitTime()))
				.toList();
	}


	@ApiOperation(value = "根据ID列表查询任务")
	@PostMapping("/list-by-condition")
	public List<Task> listByIds(@RequestBody TaskConditionDTO conditionDTO) {
		log.info("调用根据ID列表查询任务");
		if (conditionDTO.getIds() == null) {
			return Collections.emptyList();
		}
        return conditionDTO.getIds().stream().map(this.taskStoreService::get).filter(Objects::nonNull).toList();
	}

}

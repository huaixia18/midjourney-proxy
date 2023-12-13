package com.github.novicezk.midjourney.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.exceptions.CheckedUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.baidu.CheckContent;
import com.github.novicezk.midjourney.baidu.ImageCheckReturn;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.support.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private final ThreadPoolTaskExecutor executor;
	private final TimedCache<String, Object> taskLocks = CacheUtil.newTimedCache(Duration.ofHours(1).toMillis());

	private final CheckContent checkContent;

	public NotifyServiceImpl(ProxyProperties properties, CheckContent checkContent) {
		this.executor = new ThreadPoolTaskExecutor();
		this.executor.setCorePoolSize(properties.getNotifyPoolSize());
		this.executor.setThreadNamePrefix("TaskNotify-");
		this.executor.initialize();
		this.checkContent = checkContent;
	}

	@Override
	public void notifyTaskChange(Task task) {
		String notifyHook = task.getPropertyGeneric(Constants.TASK_PROPERTY_NOTIFY_HOOK);
		if (CharSequenceUtil.isBlank(notifyHook)) {
			return;
		}
		String taskId = task.getId();
		TaskStatus taskStatus = task.getStatus();
		Object taskLock = this.taskLocks.get(taskId, (CheckedUtil.Func0Rt<Object>) Object::new);
		try {
			log.info("图片生成进度：" + task.getProgress());
			if ("100%".equals(task.getProgress())) {
				ImageCheckReturn imageCheckReturn = checkContent.checkImage(task.getImageUrl());
				if (imageCheckReturn.getConclusionType() != 1) {
					task.setImageUrl("https://ai.caomaoweilai.com/images/%E8%BF%9D%E8%A7%84%E6%8E%A7%E7%8A%B6%E6%80%812.png");
					task.setStatus(TaskStatus.FAILURE);
					task.setDescription("可能包含敏感词");
					task.setFailReason("可能包含敏感词");
				}
			}
			String paramsStr = OBJECT_MAPPER.writeValueAsString(task);
			this.executor.execute(() -> {
				synchronized (taskLock) {
					try {
						ResponseEntity<String> responseEntity = postJson(notifyHook, paramsStr);
						if (responseEntity.getStatusCode() == HttpStatus.OK) {
							log.debug("推送任务变更成功, 任务ID: {}, status: {}, notifyHook: {}", taskId, task.getStatus(), notifyHook);
						} else {
							log.warn("推送任务变更失败, 任务ID: {}, notifyHook: {}, code: {}, msg: {}", taskId, notifyHook, responseEntity.getStatusCodeValue(), responseEntity.getBody());
						}
					} catch (Exception e) {
						log.warn("推送任务变更失败, 任务ID: {}, notifyHook: {}, 描述: {}", taskId, notifyHook, e.getMessage());
					}
				}
			});
		} catch (JsonProcessingException e) {
			log.warn("推送任务变更失败, 任务ID: {}, notifyHook: {}, 描述: {}", taskId, notifyHook, e.getMessage());
		}
	}

	private ResponseEntity<String> postJson(String notifyHook, String paramsJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsJson, headers);
		return new RestTemplate().postForEntity(notifyHook, httpEntity, String.class);
	}

}

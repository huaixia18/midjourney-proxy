package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.loadbalancer.DiscordLoadBalancer;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.tencent.QcUploadUtil;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import eu.maxschuster.dataurl.DataUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
	private final TaskStoreService taskStoreService;
	private final DiscordLoadBalancer discordLoadBalancer;
	private final QcUploadUtil qcUploadUtil;

	@Override
	public SubmitResultVO submitImagine(Task task, List<DataUrl> dataUrls) {
		DiscordInstance instance = this.discordLoadBalancer.chooseInstance();
		if (instance == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "无可用的账号实例");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, instance.getInstanceId());
		return instance.submitTask(task, () -> {
			List<String> imageUrls = new ArrayList<>();
			for (DataUrl dataUrl : dataUrls) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
//				Message<String> uploadResult = instance.upload(taskFileName, dataUrl);
//				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
//					return Message.of(uploadResult.getCode(), uploadResult.getDescription());
//				}
//				String finalFileName = uploadResult.getResult();
//				Message<String> sendImageResult = instance.sendImageMessage("upload image: " + finalFileName, finalFileName);
//				if (sendImageResult.getCode() != ReturnCode.SUCCESS) {
//					return Message.of(sendImageResult.getCode(), sendImageResult.getDescription());
//				}
				byte[] data = dataUrl.getData();
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(image, "png", os);
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(os.toByteArray());
				MultipartFile multipartFile = createMultipartFile(byteArrayInputStream.readAllBytes(), taskFileName, "image/png");
				String upload = qcUploadUtil.upload(multipartFile, "/image");
				imageUrls.add(upload);
			}
			if (!imageUrls.isEmpty()) {
				task.setPrompt(String.join(" ", imageUrls) + " " + task.getPrompt());
				task.setPromptEn(String.join(" ", imageUrls) + " " + task.getPromptEn());
				task.setDescription("/imagine " + task.getPrompt());
				this.taskStoreService.save(task);
			}
			return instance.imagine(task.getPromptEn(), task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		});
	}

	@Override
	public SubmitResultVO submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "账号不可用: " + instanceId);
		}
		return discordInstance.submitTask(task, () -> discordInstance.upscale(targetMessageId, index, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public SubmitResultVO submitVariation(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "账号不可用: " + instanceId);
		}
		return discordInstance.submitTask(task, () -> discordInstance.variation(targetMessageId, index, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public SubmitResultVO submitReroll(Task task, String targetMessageId, String targetMessageHash, int messageFlags) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "账号不可用: " + instanceId);
		}
		return discordInstance.submitTask(task, () -> discordInstance.reroll(targetMessageId, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public SubmitResultVO submitDescribe(Task task, DataUrl dataUrl) {
		DiscordInstance discordInstance = this.discordLoadBalancer.chooseInstance();
		if (discordInstance == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "无可用的账号实例");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, discordInstance.getInstanceId());
		return discordInstance.submitTask(task, () -> {
			String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
			Message<String> uploadResult = discordInstance.upload(taskFileName, dataUrl);
			if (uploadResult.getCode() != ReturnCode.SUCCESS) {
				return Message.of(uploadResult.getCode(), uploadResult.getDescription());
			}
			String finalFileName = uploadResult.getResult();
			return discordInstance.describe(finalFileName, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		});
	}

	@Override
	public SubmitResultVO submitBlend(Task task, List<DataUrl> dataUrls, BlendDimensions dimensions) {
		DiscordInstance discordInstance = this.discordLoadBalancer.chooseInstance();
		if (discordInstance == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "无可用的账号实例");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, discordInstance.getInstanceId());
		return discordInstance.submitTask(task, () -> {
			List<String> finalFileNames = new ArrayList<>();
			for (DataUrl dataUrl : dataUrls) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
				Message<String> uploadResult = discordInstance.upload(taskFileName, dataUrl);
				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
					return Message.of(uploadResult.getCode(), uploadResult.getDescription());
				}
				finalFileNames.add(uploadResult.getResult());
			}
			return discordInstance.blend(finalFileNames, dimensions, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		});
	}

	/**
	 * 创建一个MultipartFile接口的实现类对象
	 * 主要用于在没有文件上传的情况下模拟文件对象，以进行文件相关的操作
	 *
	 * @param bytes       文件的字节内容，用于存储文件的实际数据
	 * @param filename    文件名，用于在界面或其他地方显示文件的名称
	 * @param contentType 文件的MIME类型，表示文件的内容类型，如"text/plain"表示纯文本文件
	 * @return 返回实现了MultipartFile接口的对象，该对象包含了指定的文件数据、文件名和文件类型
	 */
	public static MultipartFile createMultipartFile(byte[] bytes, String filename, String contentType) {
		return new MultipartFile() {
			// 使用ByteArrayResource包装字节数组，以便安全地提供给其他Bean使用
			private final ByteArrayResource byteArrayResource = new ByteArrayResource(bytes);

			@Override
			public String getName() {
				// 此处统一返回"file"，满足MultipartFile接口要求，但实际用途不大
				return "file";
			}

			@Override
			public String getOriginalFilename() {
				// 返回预设的文件名，用于在界面或其他地方显示
				return filename;
			}

			@Override
			public String getContentType() {
				// 返回预设的文件内容类型
				return contentType;
			}

			@Override
			public boolean isEmpty() {
				// 判断文件是否为空
				return bytes.length == 0;
			}

			@Override
			public long getSize() {
				// 返回文件的大小，单位为字节
				return bytes.length;
			}

			@Override
			public byte[] getBytes() {
				// 返回文件的字节内容
				return bytes;
			}

			@Override
			public InputStream getInputStream() {
				// 返回一个输入流，用于读取文件的字节内容
				return new ByteArrayInputStream(bytes);
			}

			@Override
			public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
				// 将文件内容写入到指定的目的文件中，实现文件的物理存储
				java.nio.file.Files.write(dest.toPath(), bytes);
			}
		};
	}

}

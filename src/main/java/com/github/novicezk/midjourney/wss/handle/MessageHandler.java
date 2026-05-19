package com.github.novicezk.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.loadbalancer.DiscordLoadBalancer;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Resource;
import java.util.Comparator;

public abstract class MessageHandler {
	@Resource
	protected DiscordLoadBalancer discordLoadBalancer;
	@Resource
	protected DiscordHelper discordHelper;

	public abstract void handle(MessageType messageType, DataObject message);

	protected String getMessageContent(DataObject message) {
		return message.hasKey("content") ? message.getString("content") : "";
	}

	protected String getMessageNonce(DataObject message) {
		return message.hasKey("nonce") ? message.getString("nonce") : "";
	}

	protected void findAndFinishImageTask(TaskCondition condition, String finalPrompt, DataObject message) {
		String imageUrl = getImageUrl(message);
		String messageHash = this.discordHelper.getMessageHash(imageUrl);
		condition.setMessageHash(messageHash);
		Task task = this.discordLoadBalancer.findRunningTask(condition)
				.findFirst().orElseGet(() -> {
					condition.setMessageHash(null);
					return this.discordLoadBalancer.findRunningTask(condition)
							.min(Comparator.comparing(Task::getStartTime))
							.orElse(null);
				});
		if (task == null) {
			return;
		}
		if (CharSequenceUtil.isBlank(imageUrl)) {
			imageUrl = task.getImageUrl();
		}
		if (CharSequenceUtil.isBlank(imageUrl)) {
			return;
		}
		messageHash = this.discordHelper.getMessageHash(imageUrl);
		task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, finalPrompt);
		task.setProperty(Constants.TASK_PROPERTY_MESSAGE_HASH, messageHash);
		task.setImageUrl(imageUrl);
		finishTask(task, message);
		task.awake();
	}

	protected void finishTask(Task task, DataObject message) {
		task.setProperty(Constants.TASK_PROPERTY_MESSAGE_ID, message.getString("id"));
		task.setProperty(Constants.TASK_PROPERTY_FLAGS, message.getInt("flags", 0));
		task.setProperty(Constants.TASK_PROPERTY_MESSAGE_HASH, this.discordHelper.getMessageHash(task.getImageUrl()));
		task.success();
	}

	protected boolean hasImage(DataObject message) {
		return CharSequenceUtil.isNotBlank(getImageUrl(message));
	}

	protected String getImageUrl(DataObject message) {
		DataArray attachments = message.optArray("attachments").orElse(DataArray.empty());
		if (!attachments.isEmpty()) {
			DataObject attachment = attachments.getObject(0);
			String imageUrl = getFirstImageUrl(
					attachment.getString("url", null),
					attachment.getString("proxy_url", null));
			return replaceCdnUrl(imageUrl);
		}
		DataArray embeds = message.optArray("embeds").orElse(DataArray.empty());
		for (int i = 0; i < embeds.length(); i++) {
			DataObject embed = embeds.getObject(i);
			String imageUrl = getEmbedImageUrl(embed);
			if (CharSequenceUtil.isNotBlank(imageUrl)) {
				return replaceCdnUrl(imageUrl);
			}
		}
		return null;
	}

	private String getEmbedImageUrl(DataObject embed) {
		String imageUrl = embed.optObject("image").map(image -> image.getString("url")).orElse(null);
		String thumbnailUrl = embed.optObject("thumbnail").map(thumbnail -> thumbnail.getString("url")).orElse(null);
		String embedUrl = embed.opt("url").map(Object::toString).orElse(null);
		return getFirstImageUrl(imageUrl, thumbnailUrl, embedUrl);
	}

	private String getFirstImageUrl(String... urls) {
		for (String url : urls) {
			if (isImageUrl(url)) {
				return url;
			}
		}
		return null;
	}

	private boolean isImageUrl(String url) {
		if (CharSequenceUtil.isBlank(url)) {
			return false;
		}
		String normalized = CharSequenceUtil.subBefore(url, "?", false).toLowerCase();
		return CharSequenceUtil.endWithAny(normalized, ".png", ".jpg", ".jpeg", ".webp", ".gif");
	}

	protected String replaceCdnUrl(String imageUrl) {
		if (CharSequenceUtil.isBlank(imageUrl)) {
			return imageUrl;
		}
		String cdn = this.discordHelper.getCdn();
		if (CharSequenceUtil.startWith(imageUrl, cdn)) {
			return imageUrl;
		}
		return CharSequenceUtil.replaceFirst(imageUrl, DiscordHelper.DISCORD_CDN_URL, cdn);
	}

}

package com.github.novicezk.midjourney.wss.user;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.github.novicezk.midjourney.domain.DiscordAccount;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserMessageListener {
	private final DiscordAccount account;
	private final List<MessageHandler> messageHandlers;

	public UserMessageListener(DiscordAccount account, List<MessageHandler> messageHandlers) {
		this.account = account;
		this.messageHandlers = messageHandlers;
	}

	public void onMessage(DataObject raw) {
		MessageType messageType = MessageType.of(raw.getString("t"));
		if (messageType == null || MessageType.DELETE == messageType) {
			return;
		}
		DataObject data = raw.getObject("d");
		if (ignoreAndLogMessage(data, messageType)) {
			return;
		}
		ThreadUtil.sleep(50);
		for (MessageHandler messageHandler : this.messageHandlers) {
			messageHandler.handle(messageType, data);
		}
	}

	private boolean ignoreAndLogMessage(DataObject data, MessageType messageType) {
		String channelId = data.getString("channel_id");
		if (!CharSequenceUtil.equals(channelId, this.account.getChannelId())) {
			return true;
		}
		String authorName = data.optObject("author").map(a -> a.getString("username")).orElse("System");
		log.debug("{} - {} - {}: {}", this.account.getDisplay(), messageType.name(), authorName, data.opt("content").orElse(""));
		logImageCandidates(data, messageType);
		return false;
	}

	private void logImageCandidates(DataObject data, MessageType messageType) {
		if (!log.isDebugEnabled()) {
			return;
		}
		List<String> urls = new ArrayList<>();
		collectAttachmentUrls(data, urls);
		collectEmbedUrls(data, urls);
		collectComponentUrls(data, urls);
		if (!urls.isEmpty()) {
			log.debug("{} - {} - image candidates: {}", this.account.getDisplay(), messageType.name(), urls);
		}
	}

	private void collectAttachmentUrls(DataObject data, List<String> urls) {
		DataArray attachments = data.optArray("attachments").orElse(DataArray.empty());
		for (int i = 0; i < attachments.length(); i++) {
			DataObject attachment = attachments.getObject(i);
			attachment.opt("url").map(Object::toString).ifPresent(urls::add);
			attachment.opt("proxy_url").map(Object::toString).ifPresent(urls::add);
		}
	}

	private void collectEmbedUrls(DataObject data, List<String> urls) {
		DataArray embeds = data.optArray("embeds").orElse(DataArray.empty());
		for (int i = 0; i < embeds.length(); i++) {
			DataObject embed = embeds.getObject(i);
			embed.opt("url").map(Object::toString).ifPresent(urls::add);
			embed.optObject("image").flatMap(image -> image.opt("url")).map(Object::toString).ifPresent(urls::add);
			embed.optObject("thumbnail").flatMap(thumbnail -> thumbnail.opt("url")).map(Object::toString).ifPresent(urls::add);
		}
	}

	private void collectComponentUrls(DataObject data, List<String> urls) {
		DataArray components = data.optArray("components").orElse(DataArray.empty());
		for (int i = 0; i < components.length(); i++) {
			DataArray nestedComponents = components.getObject(i).optArray("components").orElse(DataArray.empty());
			for (int j = 0; j < nestedComponents.length(); j++) {
				nestedComponents.getObject(j).opt("url").map(Object::toString).ifPresent(urls::add);
			}
		}
	}
}

package com.github.novicezk.midjourney.tencent;

import cn.hutool.core.text.CharSequenceUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
public class QcUploadUtil {

    private final String secretId;
    private final String secretKey;
    private final String region;

    private static final String SHOW_URL = "https://ai-image.caomaoai.com";

    public QcUploadUtil(TencentConfig tencentConfig) {
        this.secretId = tencentConfig.getSECRET_ID();
        this.secretKey = tencentConfig.getSECRET_KEY();
        this.region = tencentConfig.getREGION();
        if (!CharSequenceUtil.isAllNotBlank(this.secretId, this.secretKey, this.region)) {
            throw new BeanDefinitionValidationException("tencent.secret-id或tencent.secret-key或tencent.region未配置");
        }
    }

    public COSClient cosClient() {
        String secretId     = this.secretId;
        String secretKey    = this.secretKey;
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region region = new Region(this.region);
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(cred, clientConfig);
    }

    /**
     * 上传文件
     *
     * @param multipartFile 文件对象
     * @param route 文件名称 20220331/11.png
     */
    public String upload(MultipartFile multipartFile, String route) {
        try {
            String  fileExtension= multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
            String originalFilename = UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();
            byte[] data = multipartFile.getBytes();
            InputStream inputStream = new ByteArrayInputStream(data);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(multipartFile.getSize());
            objectMetadata.setContentType(getcontentType(fileExtension));
            PutObjectRequest putObjectRequest = new PutObjectRequest("caomaoai-1318092316", route+"/"+originalFilename, inputStream, objectMetadata);
            this.cosClient().putObject(putObjectRequest);
            return SHOW_URL+route+"/"+originalFilename;
        } catch (CosClientException e) {
            log.error("上传腾讯cos出错", e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    /**
     * Description: 判断Cos服务文件上传时文件的contentType
     * @param filenameExtension 文件后缀
     * @return String
     */
    public static String getcontentType(String filenameExtension) {
        String bmp = "bmp";
        if (bmp.equalsIgnoreCase(filenameExtension)) {
            return "image/bmp";
        }
        String gif = "gif";
        if (gif.equalsIgnoreCase(filenameExtension)) {
            return "image/gif";
        }
        String jpeg = "jpeg";
        String jpg = "jpg";
        String png = "png";
        if (jpeg.equalsIgnoreCase(filenameExtension) || jpg.equalsIgnoreCase(filenameExtension)
                || png.equalsIgnoreCase(filenameExtension)) {
            return "image/jpeg";
        }
        String html = "html";
        if (html.equalsIgnoreCase(filenameExtension)) {
            return "text/html";
        }
        String txt = "txt";
        if (txt.equalsIgnoreCase(filenameExtension)) {
            return "text/plain";
        }
        String vsd = "vsd";
        if (vsd.equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.visio";
        }
        String pptx = "pptx";
        String ppt = "ppt";
        if (pptx.equalsIgnoreCase(filenameExtension) || ppt.equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        String docx = ".docx";
        String doc = ".doc";
        if (docx.equalsIgnoreCase(filenameExtension) || doc.equalsIgnoreCase(filenameExtension)) {
            return "application/msword";
        }
        String xml = "xml";
        if (xml.equalsIgnoreCase(filenameExtension)) {
            return "text/xml";
        }
        String mp4 = ".mp4";
        if (mp4.equalsIgnoreCase(filenameExtension)) {
            return "application/octet-stream";
        }
        String pdf = ".pdf";
        if (pdf.equalsIgnoreCase(filenameExtension)) {
            // 使用流的形式进行上传，防止下载文件的时候访问url会预览而不是下载。  return "application/pdf";
            return "application/pdf";
        }
        String xls = ".xls";
        String xlsx = ".xlsx";
        if (xls.equalsIgnoreCase(filenameExtension) || xlsx.equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.ms-excel";
        }
        String mp3 = ".mp3";
        if (mp3.equalsIgnoreCase(filenameExtension)) {
            return "audio/mp3";
        }
        String wav = ".wav";
        if (wav.equalsIgnoreCase(filenameExtension)) {
            return "audio/wav";
        }
        return "image/jpeg";
    }

}

package com.xxl.job.admin.core.util;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.taobao.api.ApiException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 钉钉机器人工具类
 * @author Lancer
 * @title: DingtalkRobotUtils
 * @projectName xxl-job-dingtalk
 * @date 2024/1/2 11:00
 */
public class DingtalkRobotUtils {
    private final static Logger logger = LoggerFactory.getLogger(DingtalkRobotUtils.class);
    private static final String HMAC_SHA256 ="HmacSHA256";

    public static final String RED = "#F25643";
    public static final String GREEN = "#15BC83";
    public static final String YELLOW = "#FF9900";
    public static final String BLUE = "#0089FF";

    public static final String MARKDOWN_TITLE = "# <font color=\"%s\">%s</font>\n ---\n";

    /** 任务执行完毕 **/
    public static final String TEMPLATE_MARKDOWN_SHEDULEJOB_WASEXECUTED = MARKDOWN_TITLE +
            "- 任务【%s】执行完成\n"  +
            "- 耗时：%s\n"  +
            "- 下一次执行时间：%s";

    /** 任务执行失败 **/
    public static final String TEMPLATE_MARKDOWN_SHEDULEJOB_FAIL = MARKDOWN_TITLE +
            "- 执行器【%s】\n"  +
            "- 任务id：%s\n"  +
            "- 任务描述：%s\n"  +
            "- 触发时间 ：%s\n"+
            "- 日志id ：%s\n"+
            "- %s ：%s";

    private final String dingTalkToken;

    private final String secret;

    public DingtalkRobotUtils (String dingTalkToken,String secret) {
        this.dingTalkToken = dingTalkToken;
        this.secret = secret;
    }

    /**
     * 发送text消息
     * @param messageText
     */
    public void sendMessageText(String messageText){
        sendMessageText(messageText,null);
    }

    /**
     * 发送text消息
     * @param messageText
     * @param atPhones
     */
    public void sendMessageText(String messageText, List<String> atPhones){
        Long timestamp = System.currentTimeMillis();
        DingTalkClient client = new DefaultDingTalkClient(String.format("https://oapi.dingtalk.com/robot/send?access_token=%s&timestamp=%d&sign=%s",dingTalkToken,timestamp,sign(timestamp,secret)));
        OapiRobotSendRequest request = new OapiRobotSendRequest();

        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent(messageText);
        request.setText(text);

        if(!CollectionUtils.isEmpty(atPhones)){
            OapiRobotSendRequest.At at=new OapiRobotSendRequest.At();
            at.setAtMobiles(atPhones);
            request.setAt(at);
        }

        try {
            client.execute(request);
        } catch (ApiException e) {
            logger.error("[ERROR] sendMessage", e);
        }
    }

    /**
     * 发送markdown消息
     * @param title
     * @param text
     */
    public void sendMessageMarkdown(String title,String text){
        sendMessageMarkdown(title, text, null);
    }

    /**
     * 发送markdown消息
     * @param title
     * @param text
     * @param atPhones
     */
    public void sendMessageMarkdown(String title,String text,List<String> atPhones){
        Long timestamp = System.currentTimeMillis();
        DingTalkClient client = new DefaultDingTalkClient(String.format("https://oapi.dingtalk.com/robot/send?access_token=%s&timestamp=%d&sign=%s",dingTalkToken,timestamp,sign(timestamp,secret)));
        OapiRobotSendRequest request = new OapiRobotSendRequest();

        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        request.setMarkdown(markdown);

        if(!CollectionUtils.isEmpty(atPhones)){
            OapiRobotSendRequest.At at=new OapiRobotSendRequest.At();
            at.setAtMobiles(atPhones);
            request.setAt(at);
        }

        try {
            client.execute(request);
        } catch (ApiException e) {
            logger.error("[ERROR] sendMessage", e);
        }
    }

    /**
     * 签名
     * @param timestamp
     * @param secret
     * @return
     */
    static String sign(Long timestamp,String secret){
        String sign = null;
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            logger.error("sign Mac.getInstance", e);
        } catch (InvalidKeyException e) {
            logger.error("sign mac.init", e);
        }

        return sign;
    }
}

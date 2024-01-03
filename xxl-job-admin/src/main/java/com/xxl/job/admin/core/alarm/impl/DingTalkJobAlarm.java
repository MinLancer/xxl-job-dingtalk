package com.xxl.job.admin.core.alarm.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.util.DingtalkRobotUtils;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * job alarm by DingTalk
 * @author Lancer
 * @title: DingTalkJobAlarm
 * @projectName xxl-job-dingtalk
 * @date 2024/1/2 10:47
 */
@Component
public class DingTalkJobAlarm implements JobAlarm {

    private final static Logger logger = LoggerFactory.getLogger(DingTalkJobAlarm.class);

    /** 钉钉 定时任务通知群 **/
    private DingtalkRobotUtils dingtalkRobotUtils;

    /** 钉钉 报表通知群 **/
    private DingtalkRobotUtils dingtalkRobotUtilsToStatis;

    @Value("${dingtalk.robot.schedulejob.enabled}")
    private Boolean schedulejobEnable;

    @Value("${dingtalk.robot.schedulejob.token}")
    private String schedulejobToken;

    @Value("${dingtalk.robot.schedulejob.secret}")
    private String schedulejobSecret;

    @Value("${dingtalk.robot.schedulejob.include}")
    private String schedulejobInclude;

    @Value("${dingtalk.robot.statis.enabled}")
    private Boolean statisEnable;

    @Value("${dingtalk.robot.statis.token}")
    private String statisToken;

    @Value("${dingtalk.robot.statis.secret}")
    private String statisSecret;

    @Value("${dingtalk.robot.statis.include}")
    private String statisInclude;

    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        if(info == null){
            return true;
        }

        logger.info("jobLog.ExecutorParam = {}",jobLog.getExecutorParam());

        // 告警内容 、类型
        String alarmContent = null,faileType = null;
        if (jobLog.getTriggerCode() != ReturnT.SUCCESS_CODE) {
            faileType = "调度失败";
            alarmContent = jobLog.getTriggerMsg();
        }
        if (jobLog.getHandleCode()>0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
            faileType = "执行失败";
            alarmContent = jobLog.getHandleMsg();
        }

        //不发告警通知的，保存日志
        if(Boolean.FALSE.equals(schedulejobEnable) && Boolean.FALSE.equals(statisEnable)){
            logger.error("任务 {} {} 失败 ,msg= {}",info.getJobDesc(),faileType,alarmContent);
            return true;
        }

        XxlJobGroup group = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(info.getJobGroup());

        String text = String.format(DingtalkRobotUtils.TEMPLATE_MARKDOWN_SHEDULEJOB_FAIL
                ,DingtalkRobotUtils.RED
                ,"任务执行失败"
                ,group!=null?group.getTitle():"null"
                ,info.getId()
                ,info.getJobDesc()
                , DateUtil.formatDateTime(jobLog.getTriggerTime())
                ,jobLog.getId()
                ,faileType
                , StrUtil.sub(alarmContent,0,700) );

        //钉钉通知的类型 0=不通知 ，1=任务通知 2=报表通知
        int dingTalkType = 0;
        if(JSONUtil.isTypeJSON(jobLog.getExecutorParam())){
            dingTalkType = JSONUtil.parseObj(jobLog.getExecutorParam()).getInt("dingTalkType",0);
        }

        if(Boolean.TRUE.equals(schedulejobEnable) && (1 == dingTalkType || StrUtil.contains(schedulejobInclude, info.getJobDesc()))) {
            dingtalkRobotUtils.sendMessageMarkdown("任务执行结果", text);
        }

        if(Boolean.TRUE.equals(statisEnable) && (2 == dingTalkType || StrUtil.contains(statisInclude, info.getJobDesc()))) {
            dingtalkRobotUtilsToStatis.sendMessageMarkdown("报表任务执行结果", text);
        }
        return true;
    }

    @PostConstruct
    public void init(){
        logger.info("xxl-job 钉钉机器人 上线啦");

        if(Boolean.TRUE.equals(schedulejobEnable)){
            dingtalkRobotUtils = new DingtalkRobotUtils(schedulejobToken,schedulejobSecret);
            dingtalkRobotUtils.sendMessageText("xxl-job 钉钉机器人 上线啦！");
        }

        if(Boolean.TRUE.equals(statisEnable)){
            dingtalkRobotUtilsToStatis = new DingtalkRobotUtils(statisToken,statisSecret);
            dingtalkRobotUtilsToStatis.sendMessageText("xxl-job 报表钉钉机器人 上线啦！");
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("xxl-job 钉钉机器人 下线啦");

        if(Boolean.TRUE.equals(schedulejobEnable)) {
            dingtalkRobotUtils.sendMessageText("xxl-job 钉钉机器人 下线啦！");
        }

        if(Boolean.TRUE.equals(statisEnable)){
            dingtalkRobotUtils.sendMessageText("xxl-job 报表钉钉机器人 下线啦！");
        }
    }
}

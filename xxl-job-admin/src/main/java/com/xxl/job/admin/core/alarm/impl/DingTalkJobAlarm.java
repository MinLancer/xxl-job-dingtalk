package com.xxl.job.admin.core.alarm.impl;

import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;

/**
 * job alarm by DingTalk
 * @author Lancer
 * @title: DingTalkJobAlarm
 * @projectName xxl-job-dingtalk
 * @date 2024/1/2 10:47
 */
public class DingTalkJobAlarm implements JobAlarm {
    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        return false;
    }
}

package com.iict.sms;

import model.SmsEmailNotification;
import model.SmsToSend;
import utils.SmsEmailNotificationUtil;
import utils.SmsToSendUtil;
import java.util.List;

/**
 * @author ASUS
 */
public class SmsSender {

    public static void main(String[] args) {

        List<SmsToSend> list = SmsToSendUtil.getAllPendingSms();
        SmsToSendUtil.dataInsert(list);

        List<SmsEmailNotification> list1 = SmsEmailNotificationUtil.getAllSmsPending();
        SmsEmailNotificationUtil.updatePendingList(list1);
    }

}

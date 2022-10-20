package org.opengms.admin.config.socket;

import java.nio.channels.SelectionKey;
import java.util.Date;
import java.util.TimerTask;

/**
 * @author 7bin
 * @date 2022/10/19
 */
public class CustomTimerTask extends TimerTask {
    private String taskName;

    private SelectionKey key;

    public CustomTimerTask(String taskName, SelectionKey key) {
        this.taskName = taskName;
        this.key = key;
    }

    @Override
    public void run() {
        System.out.println(new Date() + " : 任务「" + taskName + "」被执行。");
        key.attach("send: " + new Date());
    }
}


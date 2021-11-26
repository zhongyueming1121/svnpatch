package com.ymz.ui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 日志输出器
 *
 * @author zym
 */
@Slf4j
public class GuiLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private Executor asyncExecutor = Executors.newFixedThreadPool(1);

    @Override
    protected void append(ILoggingEvent eventObject) {
        StringBuilder stringBuilder = new StringBuilder();
        asyncExecutor.execute(() -> {
                stringBuilder.append(" ").append(eventObject.getLevel().toString());
                //stringBuilder.append(" ").append(eventObject.getLoggerName());
                stringBuilder.append(" ").append(eventObject.getFormattedMessage());
                SvnGUI.logQueue.offer(stringBuilder.toString());
                stringBuilder.setLength(0);
        });

    }
}
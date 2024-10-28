package com.bigdata.kafkastream;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;


public class LogProcessor implements Processor<byte[],byte[]> {

    private ProcessorContext context;



    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void process(byte[] key, byte[] value) {
        String ratingValue = new String(value);
        if(ratingValue.contains("USER_RATING_LOG_PREFIX:")){
            String bValue = ratingValue.split("USER_RATING_LOG_PREFIX:")[1].trim();
            context.forward("log".getBytes(),bValue.getBytes());
        }
    }

    @Override
    public void punctuate(long l) {

    }

    @Override
    public void close() {

    }
}

package com.tonasolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import static com.tonasolution.Main.EOF;

public class Main {
    public static final String EOF = "eof";
    public static void main(String[] args) {
        List<String> buffer = new ArrayList<>();
        ReentrantLock reentrantLock = new ReentrantLock();
        MyProducer myProducer = new MyProducer(buffer, ThreadColor.ANSI_PURPLE, reentrantLock);
        MyConsumer myConsumer1 = new MyConsumer(buffer, ThreadColor.ANSI_GREEN, reentrantLock);
        MyConsumer myConsumer2 = new MyConsumer(buffer, ThreadColor.ANSI_BLUE, reentrantLock);

        new Thread(myProducer).start();
        new Thread(myConsumer1).start();
        new Thread(myConsumer2).start();
    }
}

class MyProducer implements Runnable {
    private List<String> buffer;
    private String color;
    private ReentrantLock reentrantLock;

    public MyProducer(List<String> buffer, String color, ReentrantLock reentrantLock) {
        this.buffer = buffer;
        this.color = color;
        this.reentrantLock = reentrantLock;
    }

    @Override
    public void run() {
        Random random = new Random();
        String[] nums = {"1", "2", "3", "4", "5"};
        for(String num: nums){
            try {
                System.out.println(color + "Adding ..." + num );
                reentrantLock.lock();
                try {
                    buffer.add(num);
                } finally {
                    reentrantLock.unlock();
                }
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
                System.out.println("Producer was interupted");
                e.printStackTrace();
            }
        }
        System.out.println(color + "adding the EOF and exiting ...");
        reentrantLock.lock();
        try {
            buffer.add("EOF");
        } finally {
            reentrantLock.unlock();
        }
    }
}

class MyConsumer implements Runnable {
    private List<String> buffer;
    private String color;
    private ReentrantLock reentrantLock;

    public MyConsumer(
            List<String> buffer,
            String color,
            ReentrantLock reentrantLock
    ) {
        this.buffer = buffer;
        this.color = color;
        this.reentrantLock = reentrantLock;
    }

    @Override
    public void run() {
        int counter = 0;
        while(true){
            if (reentrantLock.tryLock()){
                try {
                    if(buffer.isEmpty()){
                        continue;
                    }
                    System.out.println(color + "The counter " + counter);
                    counter = 0;
                    if(buffer.get(0).equals(EOF)){
                        System.out.println("This is the end of the buffer ");
                        break;
                    }
                    else {
                        System.out.println(color + " is removed" + buffer.remove(0));
                    }
                } finally {
                    reentrantLock.unlock();
                }
            } else {
                counter++;
            }
        }
    }
}

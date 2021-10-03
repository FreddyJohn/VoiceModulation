package com.adams.voicemodulation.structures.stack;

public class Ring {
    private final int length;
    private final short[] buf;
    private int pos;
    private int head;

    public Ring(int length){
        this.length = length;
        this.buf = new short[length];
        this.head = 0;
        this.pos = 0;
    }

    public void enqueue(short s){
        int tail = (this.head + this.pos) % this.length;
        this.pos += 1;
        this.buf[tail] = s;
    }

    public void dequeue(){
        this.pos -= 1;
        short s = this.buf[this.head];
        this.head = (this.head+1) % this.length;
    }

    public short loopback(int n){
        int i = (this.head+n) % this.length;
        return this.buf[i];
    }

}

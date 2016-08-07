/*
Copyright 2015-2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.github.terma.fastselect.data;

import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

public class LongDataTest {

    @Test
    public void provideAllocatedSize() {
        LongData data = new LongData(100);
        Assert.assertEquals(Data.DEFAULT_SIZE, data.allocatedSize());

        for (long i = 0; i < 50; i++) data.add(i);
        Assert.assertEquals(Data.DEFAULT_SIZE + 100, data.allocatedSize());
    }

    @Test
    public void supportCompact() {
        LongData data = new LongData(100);
        for (long i = 0; i < 17; i++) data.add(i);
        Assert.assertEquals(116, data.allocatedSize());

        data.compact();

        Assert.assertEquals(17, data.allocatedSize());
        for (long i = 0; i < data.size(); i++) Assert.assertEquals(i, data.get((int) i));
    }

    @Test
    public void provideMemSize() {
        LongData data = new LongData(100);
        Assert.assertEquals(156, data.mem());

        for (short i = 0; i < 50; i++) data.add(i);
        Assert.assertEquals(956, data.mem());
    }

    @Test
    public void provideInc() {
        Assert.assertEquals(33, new LongData(33).inc());
    }

    @Test
    public void load() throws IOException {
        LongData data = new LongData(100);
        File f = Files.createTempFile("a", "b").toFile();
        FileChannel fc = new RandomAccessFile(f, "rw").getChannel();
        long[] t = new long[]{0, 1, 2, 3, 4};
        ByteBuffer b = fc.map(FileChannel.MapMode.READ_WRITE, 0, Data.LONG_BYTES * t.length);
        for (long l : t) b.putLong(l);
        fc.force(true);

        data.load(fc, 5);
        f.delete();

        Assert.assertEquals(data.size(), 5);
    }

    @Test
    public void saveAndLoad() throws IOException {
        File f = Files.createTempFile("a", "b").toFile();
        FileChannel fc = new RandomAccessFile(f, "rw").getChannel();

        LongData data = new LongData(100);
        data.add((byte) -1);
        data.add((byte) 2);
        data.add((byte) 0);
        data.add(Long.MIN_VALUE);
        data.add(Long.MAX_VALUE);
        data.save(fc);

        fc.position(0);
        LongData data1 = new LongData(100);
        data1.load(fc, 5);

        f.delete();

        Assert.assertEquals(data1.size(), 5);
        Assert.assertEquals(Long.MIN_VALUE, data1.get(3));
        Assert.assertEquals(Long.MAX_VALUE, data1.get(4));
    }

}

package com.example.voicemodulation.audio;
import com.example.voicemodulation.audio.util.Generate;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
public class Format {
    public static class wav implements Runnable{
        private AudioFile data;
        public wav(AudioFile _data) {
            this.data=_data;
        }
        @Override
        public void run() {
            String file_path = data.getFilePath();
            byte[] raw_data = AudioCon.Data.getBytes(file_path);
            long total_bytes = raw_data.length;
            int bit_depth = data.getBitDepth();
            long sample_rate = data.getPlaybackRate();
            int num_channels_in = data.getNumChannelsIn();
            System.out.println("num channels in "+num_channels_in);
            byte[] wav_header = Generate.wavHeader(total_bytes, total_bytes / bit_depth,
                    sample_rate,
                    num_channels_in, (byte) (bit_depth * 8));
            byte[] raw_data_plus_header = new byte[wav_header.length + raw_data.length];
            ByteBuffer buff = ByteBuffer.wrap(raw_data_plus_header);
            buff.put(wav_header);
            buff.put(raw_data);
            byte[] formatted_file = buff.array();
            FileOutputStream out = AudioCon.IO_F.setFileOutputStream(data.getFilePath().replace(".pcm",".wav"));
            AudioCon.IO_F.closeFileOutputStream(out,formatted_file);
        }
    }
    public static class aiff implements Runnable {
        private AudioFile data;
        public aiff(AudioFile _data) {
            this.data=_data;
        }
        @Override
        public void run() {
        }
    }
}

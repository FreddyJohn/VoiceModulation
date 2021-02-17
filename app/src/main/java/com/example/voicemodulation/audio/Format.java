package com.example.voicemodulation.audio;
import com.example.voicemodulation.audio.util.Generate;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
//TODO read below and implement. padding has reason
/*
This block allows for an arbitrary amount of padding. The contents of a PADDING block have no meaning. This block is useful when it is known that metadata will be edited after encoding; the user can instruct the encoder to reserve a PADDING block of sufficient size so that when metadata is added, it will simply overwrite the padding (which is relatively quick) instead of having to insert it into the right place in the existing file (which would normally require rewriting the entire file).
*/
public class Format {
    public static class wav implements Runnable{
        private final AudioFile data;
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
            byte[] wav_header = Generate.wavHeader(total_bytes, total_bytes + 36,
                    sample_rate,
                    num_channels_in, (byte) (bit_depth * 8));
            byte[] raw_data_plus_header = new byte[wav_header.length + raw_data.length];
            ByteBuffer buff = ByteBuffer.wrap(raw_data_plus_header);
            buff.put(wav_header);
            buff.put(raw_data);
            byte[] formatted_file = buff.array();
            AudioCon.IO_F con = new AudioCon.IO_F();
            FileOutputStream out = con.setFileOutputStream(data.getFilePath().replace(".pcm",".wav"));
            con.closeFileOutputStream(out,formatted_file);
        }
    }
    public static class aiff implements Runnable {
        private final AudioFile data;
        public aiff(AudioFile _data) {
            this.data=_data;
        }
        @Override
        public void run() {
        }
    }
}

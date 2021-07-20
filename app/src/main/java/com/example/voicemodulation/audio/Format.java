package com.example.voicemodulation.audio;
//import com.example.voicemodulation.project.AudioData;
import com.example.voicemodulation.database.tables.Project;
import com.example.voicemodulation.sequence.PieceTable;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
public class Format {
    public static class wav implements Runnable{
        private final Project project;
        private PieceTable audioPieceTable;
        public wav(Project _data, PieceTable audioPieceTable) {
            this.project =_data;
            this.audioPieceTable = audioPieceTable;
        }
        @Override
        //TODO implement extra padding bytes so we can insert the header there overwriting instead of using bytebuffer and getting out of memory errors.
        public void run() {
            byte[] raw_data = audioPieceTable.get_text();
            long total_bytes = raw_data.length;
            int bit_depth = project.audioData.bit_depth;
            long sample_rate = project.audioData.sample_rate;
            int num_channels_in = project.audioData.num_channels_in;
            byte[] wav_header = Generate.wavHeader(total_bytes, total_bytes + 36,
                    sample_rate,
                    num_channels_in, (byte) (bit_depth * 8));
            byte[] raw_data_plus_header = new byte[wav_header.length + raw_data.length];
            ByteBuffer buff = ByteBuffer.wrap(raw_data_plus_header);
            buff.put(wav_header);
            buff.put(raw_data);
            byte[] formatted_file = buff.array();
            AudioConnect.IO_F con = new AudioConnect.IO_F();
            //FileOutputStream out = con.setFileOutputStream(data.getFilePath().replace(".pcm",".wav"));
            FileOutputStream out = con.setFileOutputStream(project.paths.audio.replace(".pcm",".wav"));
            con.closeFileOutputStream(out,formatted_file);
        }
    }
    /*
    public static class aiff implements Runnable {
        private final AudioData data;
        public aiff(AudioData _data) {
            this.data=_data;
        }
        @Override
        public void run() {
        }
    }

     */
}

package com.example.voicemodulation.audio;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class PieceTable{
    private static long _text_len;
    private static ArrayList<_Piece> pieces;
    private static RandomAccessFile _edits;
    private static RandomAccessFile _original;
    public PieceTable(String data){
        _text_len = data.length();
        pieces = new ArrayList<>();
        pieces.add(new _Piece(false,0,data.length()));
        try {
            _edits = new RandomAccessFile("C:\\Users\\Nick\\Downloads\\audioResearch\\hello.txt","rw");
            _edits.setLength(0);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            _original = new RandomAccessFile("C:\\Users\\Nick\\Downloads\\audioResearch\\hello1.txt","rw");
            _original.setLength(0);
            _original.write(data.getBytes());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public long get_length(RandomAccessFile f){
        long length=0;
        try {
            length= f.length();
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return length;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList splice(int start, int count, List items){
        ArrayList<_Piece> list = new ArrayList<>();
        for(int i=0;i<start;i++){
            list.add(pieces.get(i));
        }
        items.forEach((piece)->{
            list.add((_Piece) piece);
        });
        for(int i =start+count; i<pieces.size(); i++){
            list.add(pieces.get(i));
        }
        return list;
    }
    private static Pair get_pieces_and_offset(long index){
        if (index<0){
            return null;
        }
        long remainingOffset=index;
        int i;
        for(i=0;i<pieces.size();i++){
            _Piece p=pieces.get(i);
            if (remainingOffset <= p.length){
                return new Pair(i,p.offset+remainingOffset);
            }
            else{
                remainingOffset-=p.length;
            }
        }
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public int add(String data, int index){
        if (data.length()==0){
            return 0;
        }
        Pair pair = get_pieces_and_offset(index);
        int piece_index = (int) pair.first;
        _Piece curr_piece = pieces.get(piece_index);
        long piece_offset= (long) pair.second;
        long added_offset = get_length(_edits);
        try {
            _edits.write(data.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        _text_len += data.length();
        if (curr_piece.in_added && piece_offset == curr_piece.offset + (curr_piece.length == added_offset ? 1:0)){
            curr_piece.length += data.length();
            return 0;
        }
        ArrayList<_Piece> insert_pieces = new ArrayList<>();
        insert_pieces.add(new _Piece(curr_piece.in_added,curr_piece.offset, piece_offset - curr_piece.offset));
        insert_pieces.add(new _Piece(true, added_offset, data.length()));
        insert_pieces.add(new _Piece(curr_piece.in_added,piece_offset,curr_piece.length-(piece_offset - curr_piece.offset)));
        int size = insert_pieces.size();
        List<_Piece> greaterThanZero = insert_pieces
                .stream()
                .filter(piece -> piece.length > 0 )
                .collect(Collectors.toList());

        pieces = splice(piece_index,1,greaterThanZero);

        return 0;
    }
    public String get_text(){
        String doc = "";
        for(_Piece piece: pieces){
            if (piece.in_added){
                doc+=new String(get_chunk(_edits,piece.offset,piece.offset+piece.length),StandardCharsets.UTF_8);
            }
            else{
                doc+=new String(get_chunk(_original,piece.offset,piece.offset + piece.length), StandardCharsets.UTF_8);
            }
        }
        return doc;
    }
    private byte[] get_chunk(RandomAccessFile file,long start,long stop){
        long length = stop-start;
        byte[] bytes = new byte[(int)length];
        try {
            file.seek(start);
            file.read(bytes,0, (int) length);
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bytes;
    }

    public String find(long index,long length){
        if(length<0){
            return null;
        }
        String doc = "";
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index+length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second;
        _Piece start_piece = pieces.get(start_piece_index);
        RandomAccessFile buffer = start_piece.in_added ? _edits : _original;
        if(start_piece_index==stop_piece_index){
            doc = new String(get_chunk(buffer,start_piece_offset,start_piece_offset + length));
        }
        else{
            doc = new String(get_chunk(buffer,start_piece_offset,start_piece.offset + start_piece.length));
            for(int i =start_piece_index+1;i<stop_piece_index+1;i++){
                _Piece cur_piece=pieces.get(i);
                buffer = cur_piece.in_added ? _edits : _original;
                if (i==stop_piece_index){
                    doc+=new String(get_chunk(buffer,cur_piece.offset,stop_piece_offset));
                }
                else{
                    doc+=new String(get_chunk(buffer,cur_piece.offset,cur_piece.offset+cur_piece.length));
                }
            }
        }return doc;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void remove(long index, long length) {
        if(length==0){
            return;
        }
        if(length<0){
            remove(index+length,-length);
        }
        if(index<0){
            try {
                throw new Exception("Index out of Bounds");
            } catch (Exception ex) {
                Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index+length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second;
        _text_len -= length;
        if (start_piece_index == stop_piece_index){
            _Piece piece = pieces.get(start_piece_index);
            if (start_piece_offset==piece.offset){
                piece.offset+=length;
                piece.length-=length;
                return;
            }
            else if (stop_piece_offset == piece.offset+piece.length){
                piece.length-=length;
                return;
            }
        }
        _Piece start_piece = pieces.get(start_piece_index);
        _Piece end_piece = pieces.get(stop_piece_index);
        ArrayList<_Piece> delete_pieces = new ArrayList<>();
        delete_pieces.add(new _Piece(start_piece.in_added,start_piece.offset, start_piece_offset - start_piece.offset));
        delete_pieces.add(new _Piece(end_piece.in_added, stop_piece_offset, end_piece.length -(stop_piece_offset-end_piece.offset)));
        List<_Piece> greaterThanZero = delete_pieces
                .stream()
                .filter(delete_piece -> delete_piece.length > 0 )
                .collect(Collectors.toList());
        int delete_count = stop_piece_index - start_piece_index + 1;
        pieces = splice(start_piece_index,delete_count,greaterThanZero);
    }
}

class _Piece{
    public boolean in_added;
    public long offset;
    public long length;
    public _Piece(boolean in_added, long offset, long length){
        this.in_added=in_added;
        this.offset=offset;
        this.length=length;
    }
}

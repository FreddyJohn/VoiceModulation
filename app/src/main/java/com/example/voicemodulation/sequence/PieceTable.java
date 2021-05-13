package com.example.voicemodulation.sequence;
import android.util.Pair;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PieceTable implements Serializable {
    public int _text_len;
    public ArrayList<_Piece> pieces;
    public RandomAccessFile _edits;

    public PieceTable(String editPath){
        pieces = new ArrayList<>();
        try {
            _edits = new RandomAccessFile(editPath,"rw");
            _edits.setLength(0);
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
    public void print_pieces(){
        for(_Piece piece: pieces){
            System.out.println(piece.in_added+","+piece.length+","+piece.offset);
        }
    }
    public ArrayList filter(ArrayList<_Piece> pieces){
        ArrayList<_Piece> filtered =new ArrayList<>();
        for (int i = 0, piecesSize = pieces.size(); i < piecesSize; i++) {
            _Piece piece = pieces.get(i);
            if (piece.length > 0) {
                filtered.add(piece);
            }
        }
        return filtered;
    }

    public ArrayList splice(int start, int count, ArrayList<_Piece> items){
        ArrayList<_Piece> list = new ArrayList<>();
        for(int i=0;i<start;i++){
            list.add(pieces.get(i));
        }
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            _Piece piece = items.get(i);
            list.add(piece);
        }
        for(int i =start+count; i<pieces.size(); i++){
            list.add(pieces.get(i));
        }
        return list;
    }
    public Pair get_pieces_and_offset(long index){
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
    public void add_original(long length){
        _text_len = (int) length;
        pieces.add(new _Piece(false,0,length));
    }
    public void add(int length,int index){
        if (length==0){
            return;
        }
        Pair pair = get_pieces_and_offset(index);
        int piece_index = (int) pair.first;
        _Piece curr_piece = pieces.get(piece_index);
        long piece_offset= (long) pair.second;
        long added_offset = _text_len;
        _text_len += length;
        if (curr_piece.in_added && piece_offset == curr_piece.offset + (curr_piece.length == added_offset ? 1:0)){
            curr_piece.length += length;
            return;
        }
        ArrayList<_Piece> insert_pieces = new ArrayList<>();
        insert_pieces.add(new _Piece(curr_piece.in_added,curr_piece.offset, piece_offset - curr_piece.offset));
        insert_pieces.add(new _Piece(true, added_offset, length));
        insert_pieces.add(new _Piece(curr_piece.in_added,piece_offset,curr_piece.length-(piece_offset - curr_piece.offset)));
        insert_pieces =filter(insert_pieces);
        pieces = splice(piece_index,1,insert_pieces);
    }
    public byte[] get_text(){
        ByteBuffer doc = ByteBuffer.allocate(_text_len);
        for (int i = 0, piecesSize = pieces.size(); i < piecesSize; i++) {
            _Piece piece = pieces.get(i);
            doc.put(get_chunk(piece.offset, piece.offset + piece.length));
        }
        return doc.array();
    }
    private byte[] get_chunk(long start,long stop){
        long length = stop-start;
        byte[] bytes = new byte[(int)length];
        try {
            _edits.seek(start);
            _edits.read(bytes,0, (int) length);
        } catch (IOException ex) {
            System.out.println("WHAT IS HAPPENING GOD WHY ARE YOU DOING THIS TO ME FUCKKKKKKKKK");

            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bytes;
    }
    public byte[] find(long index,long length){
        if(length<0){
            return find(index+length, -length);
        }
        ByteBuffer doc = ByteBuffer.allocate((int) length);
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index+length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second;
        _Piece start_piece = pieces.get(start_piece_index);
        if(start_piece_index==stop_piece_index){
            doc.put(get_chunk(start_piece_offset,start_piece_offset + length));
        }
        else{
            doc.put(get_chunk(start_piece_offset,start_piece.offset + start_piece.length));
            for(int i =start_piece_index+1;i<stop_piece_index+1;i++){
                _Piece cur_piece=pieces.get(i);
                if (i==stop_piece_index){
                    doc.put(get_chunk(cur_piece.offset,stop_piece_offset));
                }
                else{
                    doc.put(get_chunk(cur_piece.offset,cur_piece.offset+cur_piece.length));
                }
            }

        }
        return doc.array();
    }

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
        delete_pieces = filter(delete_pieces);
        int delete_count = stop_piece_index - start_piece_index + 1;
        pieces = splice(start_piece_index,delete_count,delete_pieces);
    }

    public class _Piece{
        public boolean in_added;
        public long offset;
        public long length;
        public _Piece(boolean in_added, long offset, long length){
            this.in_added=in_added;
            this.offset=offset;
            this.length=length;
        }
    }
}


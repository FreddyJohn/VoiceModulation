package com.adams.voicemodulation.structures.sequence;
import android.util.Pair;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
public class PieceTable implements Serializable{
    public int byte_length;
    public ArrayList<Piece> pieces;
    public long position;
    public int unit;
    public PieceTable(){
        pieces = new ArrayList<>();
    }
    public void print_pieces(){
        System.out.println("PieceTable Pieces Stack");
        for(Piece piece: pieces)  {
            System.out.println(piece.in_added+","+piece.length+","+piece.offset);
        }
    }

    public void setUnit(int unit){
        this.unit = unit;
    }

    private ArrayList<Piece> filter(ArrayList<Piece> pieces) {
        ArrayList<Piece> filtered = new ArrayList<>();
        for (int i = 0, piecesSize = pieces.size(); i < piecesSize; i++) {
            Piece piece = pieces.get(i);
            if (piece.length > 0) {
                filtered.add(piece);
            }
        }
        return filtered;
    }

    private ArrayList<Piece> splice(int start, int count, ArrayList<Piece> items) {
        ArrayList<Piece> list = new ArrayList<>();
        for (int i = 0; i < start; i++) {
            list.add(pieces.get(i));
        }
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            Piece piece = items.get(i);
            list.add(piece);
        }
        for (int i = start + count; i < pieces.size(); i++) {
            list.add(pieces.get(i));
        }
        return list;
    }
    private Pair get_pieces_and_offset(long index){
        if (index<0){
            return null;
        }
        long remainingOffset=index;
        for(int i=0;i<pieces.size();i++){
            Piece p=pieces.get(i);
            if (remainingOffset <= p.length){
                return new Pair(i, p.offset + remainingOffset);
            }
            remainingOffset -= p.length;
        }
        return null;
    }
    public void add_original(int offset, int length){
        byte_length = length;
        pieces.add(new Piece(false,offset,length));
    }
    public PieceTable add(int length,int index,RandomAccessFile buffer){
        if (length==0){
            return this;
        }

        Pair pair = get_pieces_and_offset(index);

        int piece_index = (int) pair.first;
        long piece_offset= (long) pair.second;
        Piece curr_piece = pieces.get(piece_index);

        try {
            position = buffer.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //this.position += length;
        long added_offset = this.position - length;
        byte_length += length;

        if (curr_piece.in_added && piece_offset == curr_piece.offset + (curr_piece.length == added_offset ? 1:0)){
            curr_piece.length += length;
            return this;
        }

        ArrayList<Piece> insert_pieces = new ArrayList<>();
        insert_pieces.add(new Piece(curr_piece.in_added,curr_piece.offset, piece_offset - curr_piece.offset));
        insert_pieces.add(new Piece(true, added_offset, length));
        insert_pieces.add(new Piece(curr_piece.in_added,piece_offset,curr_piece.length-(piece_offset - curr_piece.offset)));
        insert_pieces = filter(insert_pieces);
        pieces = splice(piece_index,1,insert_pieces);
        return this;
    }

    public byte[] get_text(RandomAccessFile _edits, RandomAccessFile _origPiece){
        ByteBuffer doc = ByteBuffer.allocate(byte_length);
        for(Piece piece: pieces) {
            if (piece.in_added){
                doc.put(get_chunk(_edits, piece.offset, piece.offset + piece.length));
            }else{
                doc.put(get_chunk(_origPiece, piece.offset, piece.offset + piece.length));
            }
        }
        return doc.array();
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
    public byte[] find(long index,long length,RandomAccessFile _edits,RandomAccessFile origPiece){
        if(length<0){
            return find(index+length, -length, _edits, origPiece);
        }
        ByteBuffer doc = ByteBuffer.allocate((int) length);
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index+length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second;

        Piece start_piece = pieces.get(start_piece_index);
        RandomAccessFile buffer = start_piece.in_added ? _edits : origPiece;
        if(start_piece_index==stop_piece_index){
            doc.put(get_chunk(buffer,start_piece_offset,start_piece_offset + length));
        }
        else{
            doc.put(get_chunk(buffer,start_piece_offset,start_piece.offset + start_piece.length));
            for(int i =start_piece_index+1;i<stop_piece_index+1;i++){
                Piece cur_piece=pieces.get(i);
                buffer = cur_piece.in_added ? _edits : origPiece;
                if (i==stop_piece_index){
                    doc.put(get_chunk(buffer,cur_piece.offset,stop_piece_offset));
                }
                else{
                    doc.put(get_chunk(buffer,cur_piece.offset,cur_piece.offset+cur_piece.length));
                }
            }

        }
        return doc.array();
    }
    public PieceTable remove(long index, long length) {
        if(length==0){
            return this;
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
        byte_length -= length;
        if (start_piece_index == stop_piece_index){
            Piece piece = pieces.get(start_piece_index);
            if (start_piece_offset==piece.offset){
                piece.offset+=length;
                piece.length-=length;
                return this;
            }
            else if (stop_piece_offset == piece.offset+piece.length){
                piece.length-=length;
                return this;
            }
        }
        Piece start_piece = pieces.get(start_piece_index);
        Piece end_piece = pieces.get(stop_piece_index);
        ArrayList<Piece> delete_pieces = new ArrayList<>();
        delete_pieces.add(new Piece(start_piece.in_added,start_piece.offset, start_piece_offset - start_piece.offset));
        delete_pieces.add(new Piece(end_piece.in_added, stop_piece_offset, end_piece.length -(stop_piece_offset-end_piece.offset)));
        delete_pieces = filter(delete_pieces);
        int delete_count = stop_piece_index - start_piece_index + 1;
        pieces = splice(start_piece_index,delete_count,delete_pieces);
        return this;
    }
}
/*
public class PieceTable implements Serializable {
    public int byte_length;
    public ArrayList<Piece> pieces;
    public long position;

    public PieceTable() {
        this.pieces = new ArrayList<>();
    }

    private ArrayList filter(ArrayList<Piece> pieces) {
        ArrayList<Piece> filtered = new ArrayList<>();
        for (int i = 0, piecesSize = pieces.size(); i < piecesSize; i++) {
            Piece piece = pieces.get(i);
            if (piece.length > 0) {
                filtered.add(piece);
            }
        }
        return filtered;
    }

    private ArrayList splice(int start, int count, ArrayList<Piece> items) {
        ArrayList<Piece> list = new ArrayList<>();
        for (int i = 0; i < start; i++) {
            list.add(pieces.get(i));
        }
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            Piece piece = items.get(i);
            list.add(piece);
        }
        for (int i = start + count; i < pieces.size(); i++) {
            list.add(pieces.get(i));
        }
        return list;
    }

    private Pair get_pieces_and_offset(long index) {
        if (index < 0) {
            return null;
        }
        long remainingOffset = index;
        int i;
        for (i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            if (remainingOffset <= p.length) {
                return new Pair(i, p.offset + remainingOffset);
            } else {
                remainingOffset -= p.length;
            }
        }
        return null;
    }

    public void add_original(int length,int offset) {
        byte_length = length;
        pieces.add(new Piece(false, offset, length));
    }

    public PieceTable add(int length, int index){//, RandomAccessFile edits) {
        if (length == 0) {
            return this;
        }

        Pair pair = get_pieces_and_offset(index);
        int piece_index = (int) pair.first;
        long piece_offset = (long) pair.second;
        Piece curr_piece = pieces.get(piece_index);

        /*
        long added_offset = 0;
        try {
            added_offset = edits.length() - length;
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
         *

        this.position += length;
        long added_offset = this.position - length;
        byte_length += length;


        if (curr_piece.in_added && piece_offset == curr_piece.offset + (curr_piece.length == added_offset ? 1 : 0)) {
            curr_piece.length += length;
            return this;
        }
        ArrayList<Piece> insert_pieces = new ArrayList<>();
        insert_pieces.add(new Piece(curr_piece.in_added, curr_piece.offset, piece_offset - curr_piece.offset));
        insert_pieces.add(new Piece(true, added_offset, length));
        insert_pieces.add(new Piece(curr_piece.in_added, piece_offset, curr_piece.length - (piece_offset - curr_piece.offset)));
        insert_pieces = filter(insert_pieces);
        pieces = splice(piece_index, 1, insert_pieces);
        return this;
    }

    public byte[] get_text(RandomAccessFile _edits){//, RandomAccessFile _origPiece) {
        ByteBuffer doc = ByteBuffer.allocate(byte_length);
        for (Piece piece : pieces) {
            if (piece.in_added) {
                doc.put(get_chunk(_edits, piece.offset, piece.offset + piece.length));
            //} else {
            //    doc.put(get_chunk(_origPiece, piece.offset, piece.offset + piece.length));
            }
        }
        return doc.array();
    }

    private byte[] get_chunk(RandomAccessFile file, long start, long stop) {
        long length = stop - start;
        byte[] bytes = new byte[(int) length];
        try {
            file.seek(start);
            file.read(bytes, 0, (int) length);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }
    /*
    public byte[] find(long index, long length, RandomAccessFile _edits, RandomAccessFile origPiece) {
        if (length < 0) {
            return find(index + length, -length, _edits, origPiece);
        }
        ByteBuffer doc = ByteBuffer.allocate((int) length);
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index + length);
        int start_piece_index = (int) start_pair.first;
        long start_piece_offset = (long) start_pair.second;
        int stop_piece_index = (int) stop_pair.first;
        long stop_piece_offset = (long) stop_pair.second;
        Piece start_piece = pieces.get(start_piece_index);
        RandomAccessFile buffer = start_piece.in_added ? _edits : origPiece;
        if (start_piece_index == stop_piece_index) {
            doc.put(get_chunk(buffer, start_piece_offset, start_piece_offset + length));
        } else {
            doc.put(get_chunk(buffer, start_piece_offset, start_piece.offset + start_piece.length));
            for (int i = start_piece_index + 1; i < stop_piece_index + 1; i++) {
                Piece cur_piece = pieces.get(i);
                buffer = cur_piece.in_added ? _edits : origPiece;
                if (i == stop_piece_index) {
                    doc.put(get_chunk(buffer, cur_piece.offset, stop_piece_offset));
                } else {
                    doc.put(get_chunk(buffer, cur_piece.offset, cur_piece.offset + cur_piece.length));
                }
            }
        }
        return doc.array();
    }
     *
    public byte[] find(long index,long length,RandomAccessFile _edits){//,RandomAccessFile origPiece){
        if(length<0){
            return find(index+length, -length, _edits);//, origPiece);
        }
        ByteBuffer doc = ByteBuffer.allocate((int) length);
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index+length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second;

        Piece start_piece = pieces.get(start_piece_index);
        //RandomAccessFile buffer = start_piece.in_added ? _edits : origPiece;
        RandomAccessFile buffer = _edits;
        if(start_piece_index==stop_piece_index){
            doc.put(get_chunk(buffer,start_piece_offset,start_piece_offset + length));
        }
        else{
            doc.put(get_chunk(buffer,start_piece_offset,start_piece.offset + start_piece.length));
            for(int i =start_piece_index+1;i<stop_piece_index+1;i++){
                Piece cur_piece=pieces.get(i);
                //buffer = cur_piece.in_added ? _edits : origPiece;
                if (i==stop_piece_index){
                    doc.put(get_chunk(buffer,cur_piece.offset,stop_piece_offset));
                }
                else{
                    doc.put(get_chunk(buffer,cur_piece.offset,cur_piece.offset+cur_piece.length));
                }
            }

        }
        return doc.array();
    }
    public PieceTable remove(long index, long length) {
        if (length == 0) {
            return this;
        }
        if (length < 0) {
            remove(index + length, -length);
        }
        if (index < 0) {
            try {
                throw new Exception("Index out of Bounds");
            } catch (Exception ex) {
                Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index + length);
        int start_piece_index = (int) start_pair.first;
        long start_piece_offset = (long) start_pair.second;
        int stop_piece_index = (int) stop_pair.first;
        long stop_piece_offset = (long) stop_pair.second;
        byte_length -= length;
        if (start_piece_index == stop_piece_index) {
            Piece piece = pieces.get(start_piece_index);
            if (start_piece_offset == piece.offset) {
                piece.offset += length;
                piece.length -= length;
                return this;
            } else if (stop_piece_offset == piece.offset + piece.length) {
                piece.length -= length;
                return this;
            }
        }
        Piece start_piece = pieces.get(start_piece_index);
        Piece end_piece = pieces.get(stop_piece_index);
        ArrayList<Piece> delete_pieces = new ArrayList<>();
        delete_pieces.add(new Piece(start_piece.in_added, start_piece.offset, start_piece_offset - start_piece.offset));
        delete_pieces.add(new Piece(end_piece.in_added, stop_piece_offset, end_piece.length - (stop_piece_offset - end_piece.offset)));
        delete_pieces = filter(delete_pieces);
        int delete_count = stop_piece_index - start_piece_index + 1;
        pieces = splice(start_piece_index, delete_count, delete_pieces);
        return this;
    }

    public void print_pieces() {
        for (Piece piece : pieces) {
            System.out.println(piece.in_added + "," + piece.length + "," + piece.offset);
        }
    }
}
*/

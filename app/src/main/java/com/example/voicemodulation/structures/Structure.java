package com.example.voicemodulation.structures;

import com.example.voicemodulation.structures.sequence.PieceTable;
import com.example.voicemodulation.structures.stack.Edit;
import com.example.voicemodulation.structures.stack.Edits;
import com.example.voicemodulation.util.Persist;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
    TODO refactor Structure
        1.) add member variables for BOTH the audio and bitmap PieceTable
        2.) in each wrapper for the dynamic set operations include code to add to each representation
            by using a new parameter BytePoints

 */

public class Structure {
    public Persist<PieceTable> pieceTablePersist;
    public Persist<Edits> editsPersist;
    private String removeStackPath;
    private String editsStackPath;
    private String objectPath;
    private String editPath;
    private RandomAccessFile removeStack;
    private RandomAccessFile editsStack;
    private RandomAccessFile editsBuffer;
    private PieceTable pieceTable;
    public int byte_length;
    private Edits edits;

    public Structure(String oPath, String ePath, String origPath, String editsPath) {
        this.removeStackPath = origPath;
        this.editsStackPath = editsPath;
        this.objectPath = oPath;
        this.editPath = ePath;
        this.pieceTablePersist = new Persist<>();
        this.editsPersist = new Persist<>();
        pieceTablePersist.setOutputFile(objectPath);
        editsPersist.setOutputFile(editsPath);
        try {

            removeStack = new RandomAccessFile(removeStackPath,"rw");
            editsBuffer = new RandomAccessFile(editPath, "rw");
            editsStack = new RandomAccessFile(editsStackPath,"rw");
            pieceTable = removeStack.length()!=0 || editsBuffer.length()!=0 ? pieceTablePersist.deserialize() : pieceTable;
            byte_length = pieceTable!=null ? pieceTable.byte_length : 0;
            edits = editsStack.length()!=0 ? editsPersist.deserialize() : edits;

        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void add_original(int length) {
        edits = new Edits();
        pieceTable = new PieceTable();
        //edits.pushEdit(new Edit(0, 0,"addition"));
        pieceTable.add_original(0,0); //Recall that based on the encoding we cannot have x offset
        pieceTablePersist.serialize(pieceTable);     //  an offset of one in any 16bit sequence absolutely ruins decoding
        editsPersist.serialize(edits);               //  so if we go down this route we would have to change this x based on selected encoding
        add(length,0);
    }

    public Structure add(int length, int index) {
        edits.emptyEdits();
        edits.pushEdit(new Edit(length, index,"addition"));
        //pieceTable = pieceTablePersist.deserialize();
        pieceTable.print_pieces();
        pieceTable.add(length,index,editsBuffer);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
        return this;
    }

    public byte[] getByteSequence() {
        return pieceTable.get_text(editsBuffer);//,originalPiece);
    }

    public byte[] find(long index, long length) {
        pieceTable = pieceTablePersist.deserialize();
        return pieceTable.find(index,length, editsBuffer);//, originalPiece);
    }

    public Structure remove(long index, long length) {
        edits.emptyEdits();
        try {
            removeStack.setLength(0);
            removeStack.seek(0);
            removeStack.write(find(index,length));
        } catch (IOException ex) { }
        edits.pushEdit(new Edit((int)length,(int)index,"remove"));
        pieceTable = pieceTablePersist.deserialize();
        pieceTable.remove(index,length);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
        return this;
    }

    public void printPieces() {
        pieceTable.print_pieces();
    }

    public Structure getMostRecent(){
        pieceTable = pieceTablePersist.deserialize();
        return this;
    }

    public void undo(){
        pieceTable = pieceTablePersist.deserialize();
        edits = editsPersist.deserialize();
        pieceTable = edits.undo(pieceTable, editsBuffer, removeStack);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
    }
    public void redo(){
        pieceTable = pieceTablePersist.deserialize();
        edits = editsPersist.deserialize();
        pieceTable = edits.redo(pieceTable,editsBuffer);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
    }
    public void printEditStack(){
        edits.printEdits();
    }
    public void printRedoStack(){
        edits.printRedo();
    }
}







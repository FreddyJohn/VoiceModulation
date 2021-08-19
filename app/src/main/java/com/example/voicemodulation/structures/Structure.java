package com.example.voicemodulation.structures;

import com.example.voicemodulation.structures.sequence.PieceTable;
import com.example.voicemodulation.structures.stack.Edit;
import com.example.voicemodulation.structures.stack.Edits;
import com.example.voicemodulation.util.Persist;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Structure {
    public Persist<PieceTable> pieceTablePersist;
    public Persist<Edits> editsPersist;
    private String originalPath;
    private String objectPath;
    private String editPath;
    private RandomAccessFile originalPiece;
    private RandomAccessFile editsStack;
    private RandomAccessFile editsBuffer;
    private PieceTable pieceTable;
    public int byte_length;
    private Edits edits;

    public Structure(String oPath, String ePath, String origPath, String editsPath) {
        this.originalPath = origPath;
        this.objectPath = oPath;
        this.editPath = ePath;
        this.pieceTablePersist = new Persist<>();
        this.editsPersist = new Persist<>();
        pieceTablePersist.setOutputFile(objectPath);
        editsPersist.setOutputFile(editsPath);
        try {

            originalPiece = new RandomAccessFile(originalPath,"rw");
            editsBuffer = new RandomAccessFile(editPath, "rw");
            editsStack = new RandomAccessFile(editsPath,"rw");
            pieceTable = originalPiece.length()!=0 || editsBuffer.length()!=0 ? pieceTablePersist.deserialize() : pieceTable;
            byte_length = pieceTable!=null ? pieceTable.byte_length : 0;
            edits = editsStack.length()!=0 ? editsPersist.deserialize() : edits;

        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /*
    public void add_original(int length) {
        edits = new Edits();
        pieceTable = new PieceTable();
        edits.pushEdit(new Edit(length, 0,"addition"));
        pieceTable.add_original(length);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
    }
     */
    public void add_original(int length) {
        edits = new Edits();
        pieceTable = new PieceTable();
        edits.pushEdit(new Edit(0, 0,"addition"));
        pieceTable.add_original(0,1);
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
        //pieceTable.print_pieces();
        add(length,1);
    }
    public Structure add(int length, int index) {
        //updatePosition();
        edits.pushEdit(new Edit(length, index,"addition"));
        //pieceTable = pieceTablePersist.deserialize();
        pieceTable.print_pieces();
        pieceTable.add(length,index);//,editsBuffer);
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
        //updatePosition();
        edits.pushEdit(new Edit((int)length,(int)index,"remove"));
        pieceTable = pieceTablePersist.deserialize();
        pieceTable.remove(index,length);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
        return this;
    }

    private void updatePosition() {
        try {
            pieceTable.position = editsBuffer.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        pieceTable = edits.undo(pieceTable, editsBuffer);
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







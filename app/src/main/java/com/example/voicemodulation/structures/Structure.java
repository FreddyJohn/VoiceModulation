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
    private String originalBufferPath;
    private String removeStackPath;
    private String editsStackPath;
    private String objectPath;
    private String editBufferPath;
    private RandomAccessFile removeStack;
    private RandomAccessFile editsStack;
    private RandomAccessFile editsBuffer;
    private RandomAccessFile originalBuffer;
    private PieceTable pieceTable;
    public int byte_length;
    private Edits edits;

    public Structure(String objectPath, String editPath, String originalPath, String editsPath,String rPath) {
        this.removeStackPath = rPath;
        this.originalBufferPath = originalPath;
        this.editsStackPath = editsPath;
        this.objectPath = objectPath;
        this.editBufferPath = editPath;
        this.pieceTablePersist = new Persist<>();
        this.editsPersist = new Persist<>();
        pieceTablePersist.setOutputFile(this.objectPath);
        editsPersist.setOutputFile(editsPath);
        try {

            originalBuffer = new RandomAccessFile(originalBufferPath,"rw");
            removeStack = new RandomAccessFile(removeStackPath,"rw");
            editsBuffer = new RandomAccessFile(editBufferPath, "rw");
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
        edits.pushEdit(new Edit(length,0,"addition"));
        pieceTable.add_original(0,length);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);     //  an offset of one in any 16bit sequence absolutely ruins decoding
        editsPersist.serialize(edits);               //  so if we go down this route we would have to change this x based on selected encoding
    }

    public Structure add(int length, int index) {
        index = (index==0) ? 2 : index;  // TODO fix inserts at 0. Personally im sick of working on this so this will do for now
        edits.emptyEdits();              //  no one will notice one little audio sample. and for now since only encoding is 16bit this is fine
        edits.pushEdit(new Edit(length, index,"addition"));
        pieceTable.print_pieces();
        pieceTable.add(length,index,editsBuffer);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
        return this;
    }

    public byte[] getByteSequence() {
        return pieceTable.get_text(editsBuffer, originalBuffer);
    }

    public byte[] find(long index, long length) {
        pieceTable = pieceTablePersist.deserialize();
        return pieceTable.find(index,length, editsBuffer, originalBuffer);
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
        pieceTable = edits.undo(pieceTable, editsBuffer, removeStack, originalBuffer);
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






